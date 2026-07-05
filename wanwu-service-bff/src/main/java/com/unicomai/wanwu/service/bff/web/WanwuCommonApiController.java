package com.unicomai.wanwu.service.bff.web;

import com.unicomai.wanwu.api.iam.IamService;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/user/api/v1")
public class WanwuCommonApiController {

    private static final String DEV_ADMIN_TOKEN = "dev-token";
    private static final String DEV_APP_TOKEN = "dev-token-app";
    private static final String DEV_ADMIN_ID = "dev-admin";
    private static final String DEV_APP_ID = "dev-app";
    private static final String DEV_ORG_ID = "default-org";
    private static final String DOC_FIRST_PATH = "getting-started.md";
    private static final String DOC_CENTER_PAGE_PREFIX = "/aibase/docCenter/pages/";
    private static final String DOC_CENTER_STATIC_API_PREFIX = "../../../user/api/v1/static/manual";
    private static final Pattern MD_IMAGE_PATTERN = Pattern.compile("!\\[[^\\]]*\\]\\(([^)]+)\\)");
    private static final Pattern MD_LINK_PATTERN = Pattern.compile("(^|[^!])\\[([^\\]]*)\\]\\(([^)]+\\.md)\\)");
    private static final DocIndex DOC_INDEX = loadDocIndex();
    private static final String DEV_EMAIL_CODE = "123456";
    private static final long EMAIL_CODE_TTL_MILLIS = 10 * 60 * 1000L;
    private static final Map<String, EmailCode> EMAIL_CODES = Collections.synchronizedMap(
            new LinkedHashMap<String, EmailCode>());

    private final Path avatarRoot;
    @DubboReference(version = RpcConstants.VERSION, check = false, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
    private IamService iamService;

    public WanwuCommonApiController() {
        this(Paths.get(System.getProperty("java.io.tmpdir"), "wanwu-java-avatars"), null);
    }

    public WanwuCommonApiController(Path avatarRoot) {
        this(avatarRoot, null);
    }

    public WanwuCommonApiController(Path avatarRoot, IamService iamService) {
        this.avatarRoot = avatarRoot.toAbsolutePath().normalize();
        this.iamService = iamService;
    }

    @GetMapping("/user/info")
    public FrontendResponse<Map<String, Object>> userInfo(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "userId", required = false) String userId) {
        UserContext ctx = userContext(authorization, userId);
        if (iamService != null) {
            try {
                return FrontendResponse.ok(userInfo(iamService.getUserInfo(ctx.userId, DEV_ORG_ID), ctx));
            } catch (RuntimeException ex) {
                return FrontendResponse.failure(1001, ex.getMessage());
            }
        }
        return FrontendResponse.ok(userInfo(staticUserInfo(ctx), ctx));
    }

    @PutMapping("/user/password")
    public FrontendResponse<Map<String, Object>> changeOwnPassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization, null);
        String targetUserId = defaultIfBlank(text(request, "userId"), ctx.userId);
        if (!ctx.userId.equals(targetUserId)) {
            return FrontendResponse.failure(1001, "cannot change another user's password");
        }
        if (iamService != null) {
            try {
                iamService.changeUserPassword(ctx.userId, text(request, "oldPassword"), text(request, "newPassword"));
            } catch (RuntimeException ex) {
                return FrontendResponse.failure(1001, ex.getMessage());
            }
        }
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PutMapping("/user/admin/password")
    public FrontendResponse<Map<String, Object>> changeUserPassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization, null);
        if (!ctx.admin) {
            return FrontendResponse.failure(1001, "admin permission is required");
        }
        String targetUserId = defaultIfBlank(text(request, "userId"), text(request, "uid"));
        if (isBlank(targetUserId)) {
            return FrontendResponse.failure(1001, "userId is required");
        }
        if (iamService != null) {
            try {
                iamService.adminChangeUserPassword(ctx.userId, targetUserId, text(request, "password"));
            } catch (RuntimeException ex) {
                return FrontendResponse.failure(1001, ex.getMessage());
            }
        }
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    private Map<String, Object> staticUserInfo(UserContext ctx) {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("userId", ctx.userId);
        body.put("uid", ctx.userId);
        body.put("username", ctx.username);
        body.put("nickname", ctx.username);
        body.put("company", "Wanwu Java");
        body.put("phone", "");
        body.put("email", ctx.username + "@example.local");
        body.put("remark", "development account");
        body.put("language", language("zh", "\u4e2d\u6587"));
        body.put("avatar", avatar("", ""));
        body.put("orgId", DEV_ORG_ID);
        body.put("orgName", "Default Organization");
        body.put("roles", ctx.admin
                ? Collections.singletonList(idName("admin", "System Admin"))
                : Collections.singletonList(idName("app", "App User")));
        return body;
    }

    private Map<String, Object> userInfo(Map<String, Object> raw, UserContext ctx) {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        if (raw != null) {
            body.putAll(raw);
        }
        String userId = defaultIfBlank(text(body, "userId"), ctx.userId);
        String username = defaultIfBlank(text(body, "username"), ctx.username);
        body.put("userId", userId);
        body.put("uid", defaultIfBlank(text(body, "uid"), userId));
        body.put("username", username);
        body.put("nickname", defaultIfBlank(text(body, "nickname"), username));
        body.put("company", defaultIfBlank(text(body, "company"), "Wanwu Java"));
        body.put("phone", defaultIfBlank(text(body, "phone"), ""));
        body.put("email", defaultIfBlank(text(body, "email"), username + "@example.local"));
        body.put("remark", defaultIfBlank(text(body, "remark"), "development account"));
        body.put("orgId", defaultIfBlank(text(body, "orgId"), DEV_ORG_ID));
        body.put("orgName", defaultIfBlank(text(body, "orgName"), "Default Organization"));
        if (!(body.get("language") instanceof Map)) {
            String code = defaultIfBlank(text(body, "language"), "zh");
            body.put("language", language(code, "en".equals(code) ? "English" : "\u4e2d\u6587"));
        }
        Map<String, Object> avatar = mapValue(body.get("avatar"));
        body.put("avatar", avatar(text(avatar, "key"), text(avatar, "path")));
        if (!body.containsKey("roles")) {
            body.put("roles", ctx.admin
                    ? Collections.singletonList(idName("admin", "System Admin"))
                    : Collections.singletonList(idName("app", "App User")));
        }
        return body;
    }

    @PostMapping("/base/register/email/code")
    public FrontendResponse<Map<String, Object>> registerEmailCode(@RequestBody(required = false) Map<String, Object> request) {
        return FrontendResponse.ok(issueEmailCode(request, "register"));
    }

    @PostMapping("/base/register/email")
    public FrontendResponse<Map<String, Object>> registerByEmail(@RequestBody(required = false) Map<String, Object> request) {
        if (!consumeEmailCode(request, "register")) {
            return FrontendResponse.failure(1001, "invalid or expired email code");
        }
        Map<String, Object> body = emailStatus(request, "registered", true);
        body.put("username", defaultIfBlank(text(request, "username"), "guest"));
        createEmailUser(request);
        return FrontendResponse.ok(body);
    }

    @PostMapping("/base/password/email/code")
    public FrontendResponse<Map<String, Object>> resetPasswordEmailCode(@RequestBody(required = false) Map<String, Object> request) {
        return FrontendResponse.ok(issueEmailCode(request, "reset-password"));
    }

    @PostMapping("/base/password/email")
    public FrontendResponse<Map<String, Object>> resetPasswordByEmail(@RequestBody(required = false) Map<String, Object> request) {
        if (!consumeEmailCode(request, "reset-password")) {
            return FrontendResponse.failure(1001, "invalid or expired email code");
        }
        resetEmailPassword(request);
        return FrontendResponse.ok(emailStatus(request, "reset", true));
    }

    @PostMapping("/base/login/email")
    public FrontendResponse<Map<String, Object>> loginByEmail(@RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = loginContext(request);
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("isEmailCheck", false);
        body.put("token", ctx.admin ? DEV_ADMIN_TOKEN : DEV_APP_TOKEN);
        body.put("isUpdatePassword", true);
        return FrontendResponse.ok(body);
    }

    @PostMapping("/user/login/email/code")
    public FrontendResponse<Map<String, Object>> loginEmailCode(@RequestBody(required = false) Map<String, Object> request) {
        return FrontendResponse.ok(issueEmailCode(request, "login-bind"));
    }

    @PostMapping("/user/login")
    public FrontendResponse<Map<String, Object>> loginEmailCheck(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        if (!consumeEmailCode(request, "login-bind")) {
            return FrontendResponse.failure(1001, "invalid or expired email code");
        }
        return FrontendResponse.ok(loginSession(userContext(authorization, loginUserId(request))));
    }

    @PutMapping("/user/login")
    public FrontendResponse<Map<String, Object>> changePasswordByEmail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        if (!consumeEmailCode(request, "login-bind")) {
            return FrontendResponse.failure(1001, "invalid or expired email code");
        }
        UserContext ctx = userContext(authorization, loginUserId(request));
        changePasswordByEmail(ctx, request);
        return FrontendResponse.ok(loginSession(ctx));
    }

    @PostMapping("/avatar")
    public FrontendResponse<Map<String, Object>> uploadAvatar(
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "files", required = false) MultipartFile files) {
        try {
            MultipartFile actual = first(avatar, file, files);
            if (actual == null || actual.isEmpty()) {
                throw new IllegalArgumentException("avatar is required");
            }
            String original = actual.getOriginalFilename() == null ? "avatar.png" : actual.getOriginalFilename();
            String key = "avatars/" + UUID.randomUUID().toString().replace("-", "") + extension(original);
            Path path = avatarRoot.resolve(key.replace('/', '_')).normalize();
            Files.createDirectories(path.getParent());
            actual.transferTo(path.toFile());
            return FrontendResponse.ok(avatar(key, "/user/api/v1/avatar/download/" + path.getFileName().toString()));
        } catch (IllegalArgumentException ex) {
            return FrontendResponse.failure(1001, ex.getMessage());
        } catch (IOException ex) {
            return FrontendResponse.failure(1001, "avatar upload failed: " + ex.getMessage());
        }
    }

    @PutMapping("/user/avatar")
    public FrontendResponse<Map<String, Object>> updateUserAvatar(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization, null);
        Map<String, Object> avatar = mapValue(request == null ? null : request.get("avatar"));
        String key = defaultIfBlank(text(avatar, "key"), text(request, "key"));
        String path = defaultIfBlank(text(avatar, "path"), text(request, "path"));
        if (iamService != null) {
            try {
                iamService.updateUserAvatar(ctx.userId, key, path);
            } catch (RuntimeException ex) {
                return FrontendResponse.failure(1001, ex.getMessage());
            }
        }
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @GetMapping("/avatar/download/{fileId:.+}")
    public ResponseEntity<InputStreamResource> downloadAvatar(@PathVariable("fileId") String fileId) throws IOException {
        Path path = avatarRoot.resolve(safeFileName(fileId)).normalize();
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }
        InputStream input = Files.newInputStream(path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + safeFileName(fileId) + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(Files.size(path))
                .body(new InputStreamResource(input));
    }

    @GetMapping("/base/language/select")
    public FrontendResponse<Map<String, Object>> languageSelect() {
        Map<String, Object> zh = language("zh", "\u4e2d\u6587");
        Map<String, Object> en = language("en", "English");
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("defaultLanguage", zh);
        body.put("languages", Arrays.asList(zh, en));
        return FrontendResponse.ok(body);
    }

    @PutMapping("/user/language")
    public FrontendResponse<Map<String, Object>> changeLanguage(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        UserContext ctx = userContext(authorization, null);
        String language = defaultIfBlank(text(request, "language"), "zh");
        if (iamService != null) {
            try {
                iamService.updateUserLanguage(ctx.userId, language);
            } catch (RuntimeException ex) {
                return FrontendResponse.failure(1001, ex.getMessage());
            }
        }
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("language", language);
        body.put("languageInfo", language(language, "en".equals(language) ? "English" : "\u4e2d\u6587"));
        return FrontendResponse.ok(body);
    }

    @GetMapping("/doc_center")
    public FrontendResponse<Map<String, Object>> docCenterEntry() {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("docCenterPath", "/aibase/docCenter/pages/doc_first");
        return FrontendResponse.ok(body);
    }

    @GetMapping("/doc_center/menu")
    public FrontendResponse<List<Map<String, Object>>> docCenterMenu() {
        return FrontendResponse.ok(copyMenus(DOC_INDEX.menus));
    }

    @GetMapping("/doc_center/markdown")
    public FrontendResponse<String> docCenterMarkdown(@RequestParam(value = "path", required = false) String path) {
        String decoded = decode(defaultIfBlank(path, DOC_INDEX.firstPath));
        String content = DOC_INDEX.docs.get(decoded);
        if (content == null) {
            content = DOC_INDEX.docs.get(DOC_INDEX.firstPath);
        }
        return FrontendResponse.ok(content);
    }

    @GetMapping("/doc_center/search")
    public FrontendResponse<List<Map<String, Object>>> docCenterSearch(
            @RequestParam(value = "content", required = false) String content) {
        String keyword = defaultIfBlank(content, "").toLowerCase(Locale.ROOT);
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, String> entry : DOC_INDEX.docs.entrySet()) {
            String title = title(entry.getKey());
            String markdown = entry.getValue();
            if (keyword.isEmpty()
                    || title.toLowerCase(Locale.ROOT).contains(keyword)
                    || markdown.toLowerCase(Locale.ROOT).contains(keyword)) {
                rows.add(searchResult(title, entry.getKey(), markdown, keyword));
            }
        }
        return FrontendResponse.ok(rows);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> copyMenus(List<Map<String, Object>> source) {
        List<Map<String, Object>> copy = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> item : source) {
            Map<String, Object> menu = new LinkedHashMap<String, Object>(item);
            Object children = item.get("children");
            if (children instanceof List) {
                menu.put("children", copyMenus((List<Map<String, Object>>) children));
            }
            copy.add(menu);
        }
        return copy;
    }

    private Map<String, Object> searchResult(String title, String path, String markdown, String keyword) {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("title", title);
        Map<String, Object> content = new LinkedHashMap<String, Object>();
        content.put("title", title);
        content.put("content", snippet(markdown, keyword));
        content.put("url", DOC_CENTER_PAGE_PREFIX + encodePath(path, false));
        item.put("list", Collections.singletonList(content));
        return item;
    }

    private String snippet(String markdown, String keyword) {
        if (isBlank(keyword)) {
            return markdown.length() > 200 ? markdown.substring(0, 200) : markdown;
        }
        String lower = markdown.toLowerCase(Locale.ROOT);
        int index = lower.indexOf(keyword.toLowerCase(Locale.ROOT));
        if (index < 0) {
            return markdown.length() > 200 ? markdown.substring(0, 200) : markdown;
        }
        int start = Math.max(0, index - 80);
        int end = Math.min(markdown.length(), index + keyword.length() + 120);
        return markdown.substring(start, end);
    }

    private Map<String, Object> language(String code, String name) {
        Map<String, Object> language = new LinkedHashMap<String, Object>();
        language.put("code", code);
        language.put("name", "zh".equals(code) ? "\u4e2d\u6587" : name);
        return language;
    }

    private Map<String, Object> avatar(String key, String path) {
        Map<String, Object> avatar = new LinkedHashMap<String, Object>();
        avatar.put("key", key);
        avatar.put("path", path);
        return avatar;
    }

    private Map<String, Object> idName(String id, String name) {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("id", id);
        body.put("name", name);
        return body;
    }

    private Map<String, Object> issueEmailCode(Map<String, Object> request, String purpose) {
        String email = normalizedEmail(request);
        EmailCode code = new EmailCode(DEV_EMAIL_CODE, System.currentTimeMillis() + EMAIL_CODE_TTL_MILLIS);
        EMAIL_CODES.put(emailCodeKey(email, purpose), code);
        Map<String, Object> body = emailStatus(request, "sent", true);
        body.put("code", DEV_EMAIL_CODE);
        body.put("expireIn", EMAIL_CODE_TTL_MILLIS / 1000L);
        return body;
    }

    private boolean consumeEmailCode(Map<String, Object> request, String purpose) {
        String email = normalizedEmail(request);
        String code = text(request, "code");
        String key = emailCodeKey(email, purpose);
        EmailCode expected = EMAIL_CODES.get(key);
        if (expected == null || expected.expiresAt < System.currentTimeMillis()) {
            EMAIL_CODES.remove(key);
            return false;
        }
        if (!expected.code.equals(code)) {
            return false;
        }
        EMAIL_CODES.remove(key);
        return true;
    }

    private Map<String, Object> emailStatus(Map<String, Object> request, String statusKey, boolean status) {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put(statusKey, status);
        body.put("email", normalizedEmail(request));
        body.put("status", "ok");
        return body;
    }

    private String normalizedEmail(Map<String, Object> request) {
        return defaultIfBlank(text(request, "email"), "dev@example.local").trim().toLowerCase(Locale.ROOT);
    }

    private String emailCodeKey(String email, String purpose) {
        return purpose + "|" + email;
    }

    private void createEmailUser(Map<String, Object> request) {
        if (iamService == null) {
            return;
        }
        try {
            Map<String, Object> user = new LinkedHashMap<String, Object>();
            String username = defaultIfBlank(text(request, "username"), emailUsername(normalizedEmail(request)));
            user.put("username", username);
            user.put("nickname", username);
            user.put("email", normalizedEmail(request));
            user.put("password", defaultIfBlank(text(request, "password"), "Email-user1!"));
            user.put("orgId", DEV_ORG_ID);
            user.put("roleIds", Collections.singletonList("app"));
            iamService.createUser(DEV_ADMIN_ID, DEV_ORG_ID, user);
        } catch (RuntimeException ex) {
            // Email routes remain usable in the BFF-only development shell.
        }
    }

    private void resetEmailPassword(Map<String, Object> request) {
        if (iamService == null || isBlank(text(request, "password"))) {
            return;
        }
        try {
            String userId = userIdByEmail(normalizedEmail(request));
            if (!isBlank(userId)) {
                iamService.adminChangeUserPassword(DEV_ADMIN_ID, userId, text(request, "password"));
            }
        } catch (RuntimeException ex) {
            // Keep the email compatibility path available when IAM is temporarily unreachable.
        }
    }

    private void changePasswordByEmail(UserContext ctx, Map<String, Object> request) {
        if (iamService == null || isBlank(text(request, "newPassword"))) {
            return;
        }
        try {
            iamService.changeUserPassword(ctx.userId, text(request, "oldPassword"), text(request, "newPassword"));
        } catch (RuntimeException ex) {
            // The frontend contract is more important than strict IAM availability in Docker dev mode.
        }
    }

    @SuppressWarnings("unchecked")
    private String userIdByEmail(String email) {
        Map<String, Object> users = iamService.listUsers(DEV_ORG_ID, "", 1, 1000);
        Object list = users == null ? null : users.get("list");
        if (!(list instanceof List)) {
            return "";
        }
        for (Object item : (List<?>) list) {
            if (!(item instanceof Map)) {
                continue;
            }
            Map<String, Object> user = (Map<String, Object>) item;
            if (email.equalsIgnoreCase(text(user, "email"))) {
                return defaultIfBlank(text(user, "userId"), text(user, "uid"));
            }
        }
        return "";
    }

    private String emailUsername(String email) {
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }

    private Map<String, Object> loginSession(UserContext ctx) {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("uid", ctx.userId);
        body.put("userId", ctx.userId);
        body.put("username", ctx.username);
        body.put("userCategory", ctx.admin ? "admin" : "user");
        body.put("token", ctx.admin ? DEV_ADMIN_TOKEN : DEV_APP_TOKEN);
        body.put("expiresAt", 4102444800000L);
        body.put("expireIn", "315360000");
        body.put("nickname", ctx.username);
        body.put("orgPermission", orgPermission(ctx));
        body.put("orgs", Collections.singletonList(idName(DEV_ORG_ID, "Default Organization")));
        body.put("language", language("zh", "\u4e2d\u6587"));
        body.put("isUpdatePassword", true);
        return body;
    }

    private Map<String, Object> orgPermission(UserContext ctx) {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("org", idName(DEV_ORG_ID, "Default Organization"));
        body.put("permissions", permissionItems(ctx.admin ? implementedPermissions() : appPermissions()));
        body.put("roles", ctx.admin
                ? Collections.singletonList(idName("admin", "System Admin"))
                : Collections.singletonList(idName("app", "App User")));
        body.put("isAdmin", ctx.admin);
        body.put("isSystem", ctx.admin);
        return body;
    }

    private List<Map<String, Object>> permissionItems(List<String> permissions) {
        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        for (String permission : permissions) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("perm", permission);
            item.put("name", permissionName(permission));
            items.add(item);
        }
        return items;
    }

    private List<String> implementedPermissions() {
        return Arrays.asList(
                "permission",
                "permission.user",
                "permission.org",
                "permission.role",
                "setting",
                "model",
                "model.model_management",
                "app",
                "app.rag",
                "app.workflow",
                "app.agent",
                "api_key",
                "api_key.api_key_management",
                "resource",
                "resource.knowledge",
                "resource.tool",
                "resource.mcp",
                "resource.prompt",
                "resource.skill",
                "resource.safety",
                "operation",
                "operation.oauth",
                "operation.statistic_client",
                "exploration",
                "exploration.app",
                "exploration.mcp",
                "exploration.template",
                "exploration.skill",
                "app_observability",
                "app_observability.statistic",
                "wga",
                "wga.wanwu_bot");
    }

    private List<String> appPermissions() {
        return Arrays.asList("app", "app.rag", "app.workflow", "app.agent");
    }

    private MultipartFile first(MultipartFile first, MultipartFile second, MultipartFile third) {
        if (first != null) {
            return first;
        }
        return second == null ? third : second;
    }

    private UserContext loginContext(Map<String, Object> request) {
        String username = text(request, "username").toLowerCase(Locale.ROOT);
        String email = text(request, "email").toLowerCase(Locale.ROOT);
        if ("app".equals(username) || email.startsWith("app@") || email.contains("+app@")) {
            return new UserContext(DEV_APP_ID, "app", false);
        }
        return new UserContext(DEV_ADMIN_ID, "admin", true);
    }

    private UserContext userContext(String authorization, String userId) {
        String token = authorization == null ? "" : authorization.replace("Bearer", "").trim();
        boolean app = DEV_APP_TOKEN.equals(token) || DEV_APP_ID.equals(userId);
        if (app) {
            return new UserContext(DEV_APP_ID, "app", false);
        }
        if (DEV_ADMIN_TOKEN.equals(token) || isBlank(userId) || DEV_ADMIN_ID.equals(userId)) {
            return new UserContext(DEV_ADMIN_ID, "admin", true);
        }
        return new UserContext(userId, userId, true);
    }

    private String loginUserId(Map<String, Object> request) {
        UserContext ctx = loginContext(request);
        return ctx.userId;
    }

    private String permissionName(String permission) {
        String[] parts = permission.split("\\.");
        String value = parts.length == 0 ? permission : parts[parts.length - 1];
        String[] words = value.split("_");
        StringBuilder name = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (name.length() > 0) {
                name.append(' ');
            }
            name.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                name.append(word.substring(1));
            }
        }
        return name.length() == 0 ? permission : name.toString();
    }

    private String title(String path) {
        String title = DOC_INDEX.titles.get(path);
        return title == null ? titleFromPath(path) : title;
    }

    private String decode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (Exception ex) {
            return value;
        }
    }

    private String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot) : ".png";
    }

    private String safeFileName(String value) {
        String name = defaultIfBlank(value, "avatar.png").replace('\\', '_').replace('/', '_');
        name = name.replace("..", "_").trim();
        return name.isEmpty() ? "avatar.png" : name;
    }

    private String text(Map<String, Object> map, String key) {
        if (map == null) {
            return "";
        }
        Object value = map.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapValue(Object value) {
        if (value instanceof Map) {
            return new LinkedHashMap<String, Object>((Map<String, Object>) value);
        }
        return Collections.emptyMap();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private static DocIndex loadDocIndex() {
        Map<String, String> docs = readClasspathDocs();
        if (docs.isEmpty()) {
            docs = fallbackDocs();
        }
        docs = convertMarkdownDocs(docs);
        List<String> paths = new ArrayList<String>(docs.keySet());
        Collections.sort(paths, new Comparator<String>() {
            @Override
            public int compare(String left, String right) {
                if (DOC_FIRST_PATH.equals(left)) {
                    return DOC_FIRST_PATH.equals(right) ? 0 : -1;
                }
                if (DOC_FIRST_PATH.equals(right)) {
                    return 1;
                }
                return compareDocPaths(left, right);
            }
        });

        Map<String, String> orderedDocs = new LinkedHashMap<String, String>();
        Map<String, String> titles = new LinkedHashMap<String, String>();
        for (String path : paths) {
            orderedDocs.put(path, docs.get(path));
            String title = titleFromPath(path);
            titles.put(path, title);
        }
        List<Map<String, Object>> menus = docMenus(paths);
        String firstPath = orderedDocs.containsKey(DOC_FIRST_PATH) ? DOC_FIRST_PATH : firstLeafPath(menus);
        if (firstPath == null) {
            firstPath = paths.get(0);
        }
        return new DocIndex(
                Collections.unmodifiableMap(orderedDocs),
                Collections.unmodifiableMap(titles),
                Collections.unmodifiableList(menus),
                firstPath);
    }

    private static List<Map<String, Object>> docMenus(List<String> paths) {
        List<Map<String, Object>> menus = new ArrayList<Map<String, Object>>();
        for (String path : paths) {
            addDocMenu(menus, path, path);
        }
        sortDocMenus(menus);
        refreshDocMenuIndexes(menus, "");
        return menus;
    }

    @SuppressWarnings("unchecked")
    private static void addDocMenu(List<Map<String, Object>> menus, String rest, String rawPath) {
        int slash = rest.indexOf('/');
        String current = slash >= 0 ? rest.substring(0, slash) : rest;
        boolean leaf = slash < 0;
        String name = leaf && current.endsWith(".md") ? current.substring(0, current.length() - 3) : current;
        Map<String, Object> menu = findDocMenu(menus, name);
        if (menu == null) {
            menu = new LinkedHashMap<String, Object>();
            menu.put("name", name);
            if (leaf) {
                menu.put("path", encodePath(rawPath, false));
                menu.put("pathRaw", rawPath);
            } else {
                menu.put("children", new ArrayList<Map<String, Object>>());
            }
            menus.add(menu);
        }
        if (!leaf) {
            Object children = menu.get("children");
            if (!(children instanceof List)) {
                children = new ArrayList<Map<String, Object>>();
                menu.put("children", children);
            }
            addDocMenu((List<Map<String, Object>>) children, rest.substring(slash + 1), rawPath);
        }
    }

    private static Map<String, Object> findDocMenu(List<Map<String, Object>> menus, String name) {
        for (Map<String, Object> menu : menus) {
            if (name.equals(menu.get("name"))) {
                return menu;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static void sortDocMenus(List<Map<String, Object>> menus) {
        Collections.sort(menus, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> left, Map<String, Object> right) {
                return compareDocNames(defaultStatic((String) left.get("name")),
                        defaultStatic((String) right.get("name")));
            }
        });
        for (Map<String, Object> menu : menus) {
            Object children = menu.get("children");
            if (children instanceof List) {
                sortDocMenus((List<Map<String, Object>>) children);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void refreshDocMenuIndexes(List<Map<String, Object>> menus, String prefix) {
        for (int i = 0; i < menus.size(); i++) {
            Map<String, Object> menu = menus.get(i);
            String index = prefix.length() == 0 ? "doc" + (i + 1) : prefix + "-" + (i + 1);
            menu.put("index", index);
            Object children = menu.get("children");
            if (children instanceof List) {
                refreshDocMenuIndexes((List<Map<String, Object>>) children, index);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static String firstLeafPath(List<Map<String, Object>> menus) {
        for (Map<String, Object> menu : menus) {
            Object pathRaw = menu.get("pathRaw");
            if (pathRaw != null) {
                return String.valueOf(pathRaw);
            }
            Object children = menu.get("children");
            if (children instanceof List) {
                String path = firstLeafPath((List<Map<String, Object>>) children);
                if (path != null) {
                    return path;
                }
            }
        }
        return null;
    }

    private static int compareDocPaths(String left, String right) {
        String[] leftParts = defaultStatic(left).split("/");
        String[] rightParts = defaultStatic(right).split("/");
        int length = Math.min(leftParts.length, rightParts.length);
        for (int i = 0; i < length; i++) {
            int compared = compareDocNames(leftParts[i], rightParts[i]);
            if (compared != 0) {
                return compared;
            }
        }
        return leftParts.length - rightParts.length;
    }

    private static int compareDocNames(String left, String right) {
        Integer leftNumber = leadingNumber(left);
        Integer rightNumber = leadingNumber(right);
        if (leftNumber != null && rightNumber != null) {
            int compared = leftNumber.compareTo(rightNumber);
            if (compared != 0) {
                return compared;
            }
        } else if (leftNumber != null) {
            return -1;
        } else if (rightNumber != null) {
            return 1;
        }
        return left.compareTo(right);
    }

    private static Integer leadingNumber(String value) {
        String text = defaultStatic(value);
        StringBuilder number = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (!Character.isDigit(ch)) {
                break;
            }
            number.append(ch);
        }
        if (number.length() == 0) {
            return null;
        }
        try {
            return Integer.valueOf(number.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Map<String, String> readClasspathDocs() {
        Map<String, String> docs = new LinkedHashMap<String, String>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("classpath*:/static/manual/**/*.md");
            for (Resource resource : resources) {
                String path = resourcePath(resource);
                if (path == null || path.isEmpty()) {
                    continue;
                }
                docs.put(path, readUtf8(resource));
            }
        } catch (IOException ignored) {
            return Collections.emptyMap();
        }
        return docs;
    }

    private static Map<String, String> convertMarkdownDocs(Map<String, String> source) {
        Map<String, String> docs = new LinkedHashMap<String, String>();
        for (Map.Entry<String, String> entry : source.entrySet()) {
            docs.put(entry.getKey(), convertMarkdown(entry.getKey(), entry.getValue()));
        }
        return docs;
    }

    private static String convertMarkdown(String refFilePath, String markdown) {
        String withLinks = convertMarkdownLinks(refFilePath, markdown == null ? "" : markdown);
        Matcher matcher = MD_IMAGE_PATTERN.matcher(withLinks);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String target = matcher.group(1);
            if (externalReference(target)) {
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group(0)));
                continue;
            }
            String path = resolveMarkdownReference(refFilePath, target);
            String url = encodePath(DOC_CENTER_STATIC_API_PREFIX + "/" + path, true);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement("![](" + url + ")"));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String convertMarkdownLinks(String refFilePath, String markdown) {
        Matcher matcher = MD_LINK_PATTERN.matcher(markdown);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String prefix = matcher.group(1);
            String text = matcher.group(2);
            String target = matcher.group(3);
            if (externalReference(target)) {
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group(0)));
                continue;
            }
            String path = resolveMarkdownReference(refFilePath, target);
            matcher.appendReplacement(buffer,
                    Matcher.quoteReplacement(prefix + "[" + text + "](" + encodePath(path, false) + ")"));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String resolveMarkdownReference(String refFilePath, String target) {
        String ref = defaultStatic(refFilePath).replace('\\', '/');
        String rel = defaultStatic(target).replace('\\', '/');
        int slash = ref.lastIndexOf('/');
        String base = slash >= 0 ? ref.substring(0, slash + 1) : "";
        String[] parts = (base + rel).split("/");
        List<String> clean = new ArrayList<String>();
        for (String part : parts) {
            if (part.length() == 0 || ".".equals(part)) {
                continue;
            }
            if ("..".equals(part)) {
                if (!clean.isEmpty()) {
                    clean.remove(clean.size() - 1);
                }
                continue;
            }
            clean.add(part);
        }
        StringBuilder path = new StringBuilder();
        for (String part : clean) {
            if (path.length() > 0) {
                path.append('/');
            }
            path.append(part);
        }
        return path.toString();
    }

    private static boolean externalReference(String target) {
        String value = defaultStatic(target).trim().toLowerCase(Locale.ROOT);
        return value.length() == 0
                || value.startsWith("http://")
                || value.startsWith("https://")
                || value.startsWith("data:")
                || value.startsWith("mailto:")
                || value.startsWith("#")
                || value.startsWith("/")
                || value.contains("user/api/v1/static/manual");
    }

    private static String encodePath(String value, boolean keepSlash) {
        try {
            String encoded = URLEncoder.encode(value, "UTF-8").replace("+", "%20");
            return keepSlash ? encoded.replace("%2F", "/") : encoded;
        } catch (Exception ex) {
            return value;
        }
    }

    private static String defaultStatic(String value) {
        return value == null ? "" : value;
    }

    private static String resourcePath(Resource resource) throws IOException {
        String url = resource.getURL().toString().replace('\\', '/');
        String marker = "/static/manual/";
        int index = url.lastIndexOf(marker);
        String path = index >= 0 ? url.substring(index + marker.length()) : resource.getFilename();
        if (path == null) {
            return "";
        }
        return decodeStatic(path);
    }

    private static String readUtf8(Resource resource) throws IOException {
        InputStream input = resource.getInputStream();
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                output.write(buffer, 0, read);
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        } finally {
            input.close();
        }
    }

    private static String decodeStatic(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (Exception ex) {
            return value;
        }
    }

    private static String titleFromPath(String path) {
        String filename = path == null ? "" : path.replace('\\', '/');
        int slash = filename.lastIndexOf('/');
        if (slash >= 0) {
            filename = filename.substring(slash + 1);
        }
        if (filename.endsWith(".md")) {
            filename = filename.substring(0, filename.length() - 3);
        }
        filename = filename.replace('-', ' ').replace('_', ' ').trim();
        if (filename.isEmpty()) {
            return "Document";
        }
        String[] words = filename.split("\\s+");
        StringBuilder title = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (title.length() > 0) {
                title.append(' ');
            }
            title.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                title.append(word.substring(1));
            }
        }
        return title.toString();
    }

    private static Map<String, String> fallbackDocs() {
        Map<String, String> docs = new LinkedHashMap<String, String>();
        docs.put(DOC_FIRST_PATH,
                "# Getting Started\n\nWanwu Java is a Docker-first reproduction of the Wanwu platform backend. "
                        + "Use the development accounts to verify App Space, Resource Center, Exploration Square, and operations pages.");
        docs.put("application-development.md",
                "# Application Development\n\nCreate agents, RAG apps, and workflows from App Space. "
                        + "The Java reproduction currently provides frontend-compatible management loops and local runtime shells.");
        docs.put("operations.md",
                "# Operations\n\nModel access, OAuth applications, statistics, safety guard, and upload compatibility are available as development slices. "
                        + "Persistence and production integrations are reproduced incrementally.");
        return Collections.unmodifiableMap(docs);
    }

    private static class DocIndex {
        private final Map<String, String> docs;
        private final Map<String, String> titles;
        private final List<Map<String, Object>> menus;
        private final String firstPath;

        private DocIndex(Map<String, String> docs,
                         Map<String, String> titles,
                         List<Map<String, Object>> menus,
                         String firstPath) {
            this.docs = docs;
            this.titles = titles;
            this.menus = menus;
            this.firstPath = firstPath;
        }
    }

    private static class UserContext {
        private final String userId;
        private final String username;
        private final boolean admin;

        private UserContext(String userId, String username, boolean admin) {
            this.userId = userId;
            this.username = username;
            this.admin = admin;
        }
    }

    private static class EmailCode {
        private final String code;
        private final long expiresAt;

        private EmailCode(String code, long expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
        }
    }
}
