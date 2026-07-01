package com.unicomai.wanwu.service.bff.web;

import org.springframework.core.io.InputStreamResource;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user/api/v1")
public class WanwuCommonApiController {

    private static final String DEV_ADMIN_TOKEN = "dev-token";
    private static final String DEV_APP_TOKEN = "dev-token-app";
    private static final String DEV_ADMIN_ID = "dev-admin";
    private static final String DEV_APP_ID = "dev-app";
    private static final String DEV_ORG_ID = "default-org";
    private static final String DOC_FIRST_PATH = "getting-started.md";
    private static final Map<String, String> DOCS = docs();

    private final Path avatarRoot;

    public WanwuCommonApiController() {
        this(Paths.get(System.getProperty("java.io.tmpdir"), "wanwu-java-avatars"));
    }

    public WanwuCommonApiController(Path avatarRoot) {
        this.avatarRoot = avatarRoot.toAbsolutePath().normalize();
    }

    @GetMapping("/user/info")
    public FrontendResponse<Map<String, Object>> userInfo(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "userId", required = false) String userId) {
        UserContext ctx = userContext(authorization, userId);
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("userId", ctx.userId);
        body.put("uid", ctx.userId);
        body.put("username", ctx.username);
        body.put("nickname", ctx.username);
        body.put("company", "Wanwu Java");
        body.put("phone", "");
        body.put("email", ctx.username + "@example.local");
        body.put("remark", "development account");
        body.put("language", "zh");
        body.put("avatar", avatar("", ""));
        body.put("orgId", DEV_ORG_ID);
        body.put("orgName", "Default Organization");
        body.put("roles", ctx.admin
                ? Collections.singletonList(idName("admin", "System Admin"))
                : Collections.singletonList(idName("app", "App User")));
        return FrontendResponse.ok(body);
    }

    @PutMapping("/user/password")
    public FrontendResponse<Map<String, Object>> changeOwnPassword() {
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PutMapping("/user/admin/password")
    public FrontendResponse<Map<String, Object>> changeUserPassword() {
        return FrontendResponse.ok(Collections.<String, Object>emptyMap());
    }

    @PostMapping("/base/register/email/code")
    public FrontendResponse<Map<String, Object>> registerEmailCode(@RequestBody(required = false) Map<String, Object> request) {
        return FrontendResponse.ok(emailNoop(request, "sent", true));
    }

    @PostMapping("/base/register/email")
    public FrontendResponse<Map<String, Object>> registerByEmail(@RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> body = emailNoop(request, "registered", true);
        body.put("username", defaultIfBlank(text(request, "username"), "guest"));
        return FrontendResponse.ok(body);
    }

    @PostMapping("/base/password/email/code")
    public FrontendResponse<Map<String, Object>> resetPasswordEmailCode(@RequestBody(required = false) Map<String, Object> request) {
        return FrontendResponse.ok(emailNoop(request, "sent", true));
    }

    @PostMapping("/base/password/email")
    public FrontendResponse<Map<String, Object>> resetPasswordByEmail(@RequestBody(required = false) Map<String, Object> request) {
        return FrontendResponse.ok(emailNoop(request, "reset", true));
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
        return FrontendResponse.ok(emailNoop(request, "sent", true));
    }

    @PostMapping("/user/login")
    public FrontendResponse<Map<String, Object>> loginEmailCheck(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return FrontendResponse.ok(loginSession(userContext(authorization, loginUserId(request))));
    }

    @PutMapping("/user/login")
    public FrontendResponse<Map<String, Object>> changePasswordByEmail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, Object> request) {
        return FrontendResponse.ok(loginSession(userContext(authorization, loginUserId(request))));
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
    public FrontendResponse<Map<String, Object>> updateUserAvatar(@RequestBody(required = false) Map<String, Object> request) {
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
    public FrontendResponse<Map<String, Object>> changeLanguage(@RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("language", request == null ? "zh" : defaultIfBlank(text(request, "language"), "zh"));
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
        List<Map<String, Object>> menus = new ArrayList<Map<String, Object>>();
        menus.add(docMenu("Getting Started", "doc1", DOC_FIRST_PATH));
        menus.add(docMenu("Application Development", "doc2", "application-development.md"));
        menus.add(docMenu("Operations", "doc3", "operations.md"));
        return FrontendResponse.ok(menus);
    }

    @GetMapping("/doc_center/markdown")
    public FrontendResponse<String> docCenterMarkdown(@RequestParam(value = "path", required = false) String path) {
        String decoded = decode(defaultIfBlank(path, DOC_FIRST_PATH));
        String content = DOCS.get(decoded);
        if (content == null) {
            content = DOCS.get(DOC_FIRST_PATH);
        }
        return FrontendResponse.ok(content);
    }

    @GetMapping("/doc_center/search")
    public FrontendResponse<List<Map<String, Object>>> docCenterSearch(
            @RequestParam(value = "content", required = false) String content) {
        String keyword = defaultIfBlank(content, "").toLowerCase(Locale.ROOT);
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, String> entry : DOCS.entrySet()) {
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

    private Map<String, Object> docMenu(String name, String index, String path) {
        Map<String, Object> menu = new LinkedHashMap<String, Object>();
        menu.put("name", name);
        menu.put("index", index);
        menu.put("path", path);
        menu.put("pathRaw", path);
        menu.put("children", Collections.emptyList());
        return menu;
    }

    private Map<String, Object> searchResult(String title, String path, String markdown, String keyword) {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("title", title);
        Map<String, Object> content = new LinkedHashMap<String, Object>();
        content.put("title", title);
        content.put("content", snippet(markdown, keyword));
        content.put("url", "/aibase/docCenter/pages/" + path);
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

    private Map<String, Object> emailNoop(Map<String, Object> request, String statusKey, boolean status) {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put(statusKey, status);
        body.put("email", defaultIfBlank(text(request, "email"), "dev@example.local"));
        body.put("status", "ok");
        return body;
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
        if (DOC_FIRST_PATH.equals(path)) {
            return "Getting Started";
        }
        if ("application-development.md".equals(path)) {
            return "Application Development";
        }
        return "Operations";
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private static Map<String, String> docs() {
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
}
