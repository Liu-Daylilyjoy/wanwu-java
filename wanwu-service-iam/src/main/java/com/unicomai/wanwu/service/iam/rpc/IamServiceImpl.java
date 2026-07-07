package com.unicomai.wanwu.service.iam.rpc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.api.iam.IamService;
import com.unicomai.wanwu.api.iam.dto.CaptchaResult;
import com.unicomai.wanwu.api.iam.dto.LoginCommand;
import com.unicomai.wanwu.api.iam.dto.LoginResult;
import com.unicomai.wanwu.api.iam.dto.OrganizationOption;
import com.unicomai.wanwu.api.iam.dto.OrganizationSelectResult;
import com.unicomai.wanwu.api.iam.dto.PermissionResult;
import com.unicomai.wanwu.common.core.model.ServiceNames;
import com.unicomai.wanwu.common.core.util.Strings;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import com.unicomai.wanwu.service.iam.persistence.entity.IamRecordEntity;
import com.unicomai.wanwu.service.iam.persistence.mapper.IamRecordMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

@DubboService(version = RpcConstants.VERSION, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
public class IamServiceImpl implements IamService {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
    };
    private static final String TYPE_USER = "user";
    private static final String TYPE_ROLE = "role";
    private static final String TYPE_ORG = "org";
    private static final String TYPE_OAUTH = "oauth";
    private static final String TYPE_CUSTOM_TAB = "custom_tab";
    private static final String TYPE_CUSTOM_LOGIN = "custom_login";
    private static final String TYPE_CUSTOM_HOME = "custom_home";
    private static final int MAX_BATCH_CREATE_USERS_LIMIT = 500;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\u4e00-\\u9fa5_().]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final String PASSWORD_SPECIALS = "!@#$%^&*()_+-=[]{};':\"\\|,.<>/?";
    private static final List<String> APP_PERMISSIONS = Arrays.asList("app", "app.rag", "app.workflow", "app.agent");
    private static final List<String> IMPLEMENTED_FRONTEND_PERMISSIONS = Collections.unmodifiableList(Arrays.asList(
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
            "app_observability.statistic"
    ));
    private static final String CREATED_AT = "2026-06-30 00:00:00";
    private static final DevAccount ADMIN_ACCOUNT = new DevAccount(
            "admin", "dev-admin", "admin", "dev-token",
            Collections.singletonList("admin"), true, true, IMPLEMENTED_FRONTEND_PERMISSIONS);
    private static final DevAccount APP_ACCOUNT = new DevAccount(
            "app", "dev-app", "user", "dev-token-app",
            Collections.singletonList("app"), false, false, APP_PERMISSIONS);
    private static final Map<String, DevAccount> ACCOUNTS_BY_USERNAME = accountsByUsername();
    private static final Map<String, DevAccount> ACCOUNTS_BY_TOKEN = accountsByToken();
    private static final OrganizationOption DEFAULT_ORG = new OrganizationOption("default-org", "Default Organization");
    private static final String CAPTCHA_B64 = "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI4MCIgaGVpZ2h0PSIzMiI+PHJlY3Qgd2lkdGg9IjgwIiBoZWlnaHQ9IjMyIiBmaWxsPSIjZjVmN2ZhIi8+PHRleHQgeD0iMTIiIHk9IjIyIiBmb250LXNpemU9IjE2IiBmaWxsPSIjNTk4M0ZGIj4xMjM0PC90ZXh0Pjwvc3ZnPg==";

    private volatile Map<String, Object> customTab = Collections.emptyMap();
    private volatile Map<String, Object> customLogin = Collections.emptyMap();
    private volatile Map<String, Object> customHome = Collections.emptyMap();
    private final Map<String, Map<String, Object>> users = new LinkedHashMap<>();
    private final Map<String, Map<String, Object>> roles = new LinkedHashMap<>();
    private final Map<String, Map<String, Object>> organizations = new LinkedHashMap<>();
    private final Map<String, Map<String, Object>> oauthApps = new LinkedHashMap<>();
    private final AtomicLong userSequence = new AtomicLong(0);
    private final AtomicLong roleSequence = new AtomicLong(0);
    private final AtomicLong orgSequence = new AtomicLong(0);
    private final AtomicLong oauthSequence = new AtomicLong(0);
    @Autowired(required = false)
    private IamRecordMapper iamRecordMapper;

    public IamServiceImpl() {
        organizations.put(DEFAULT_ORG.getId(), defaultOrganization());
        roles.put("admin", builtInRole("admin"));
        roles.put("app", builtInRole("app"));
        users.put(ADMIN_ACCOUNT.uid, userInfo(ADMIN_ACCOUNT));
        users.put(APP_ACCOUNT.uid, userInfo(APP_ACCOUNT));
    }

    IamServiceImpl(IamRecordMapper iamRecordMapper) {
        this();
        this.iamRecordMapper = iamRecordMapper;
        loadPersistedRecords();
    }

    @PostConstruct
    public synchronized void loadPersistedRecords() {
        if (iamRecordMapper == null) {
            return;
        }
        loadRecords(TYPE_ORG, organizations, orgSequence, "org-");
        loadRecords(TYPE_ROLE, roles, roleSequence, "role-");
        loadRecords(TYPE_USER, users, userSequence, "user-", "batch-user-");
        loadRecords(TYPE_OAUTH, oauthApps, oauthSequence, "oauth-client-");
        customTab = loadSingle(TYPE_CUSTOM_TAB, "default", customTab);
        customLogin = loadSingle(TYPE_CUSTOM_LOGIN, "default", customLogin);
        customHome = loadSingle(TYPE_CUSTOM_HOME, "default", customHome);
    }

    @Override
    public ServiceDescriptor describe() {
        return descriptor();
    }

    @Override
    public CaptchaResult captcha() {
        return new CaptchaResult("dev-captcha", CAPTCHA_B64);
    }

    @Override
    public LoginResult login(LoginCommand command) {
        DevAccount account = accountByUsername(command == null ? null : command.getUsername());
        if (account == null) {
            throw new IllegalArgumentException("Invalid development username");
        }
        if (!Strings.hasText(command.getPassword()) || !Strings.hasText(command.getCode())) {
            throw new IllegalArgumentException("Password and captcha code are required");
        }
        Map<String, Object> user = existingUser(account.uid);
        if (!passwordMatches(user, command.getPassword())) {
            throw new IllegalArgumentException("Invalid development password");
        }

        LoginResult result = new LoginResult();
        result.setUid(account.uid);
        result.setUsername(account.username);
        result.setUserCategory(account.userCategory);
        result.setToken(account.token);
        result.setExpiresAt(4102444800000L);
        result.setIsUpdatePassword(true);
        result.setOrgs(Collections.singletonList(DEFAULT_ORG));
        result.setOrgPermission(orgPermission(account));
        result.setCustom(platformConfig());
        return result;
    }

    @Override
    public synchronized PermissionResult permission(String token) {
        DevAccount account = accountByToken(token);
        Map<String, Object> user = existingUser(account.uid);
        PermissionResult result = new PermissionResult();
        result.setOrgPermission(orgPermission(account));
        result.setIsUpdatePassword(true);
        result.setAvatar(mapValue(user.get("avatar")));
        result.setLanguage(mapValue(user.get("language")));
        return result;
    }

    @Override
    public synchronized OrganizationSelectResult selectOrganizations() {
        java.util.ArrayList<OrganizationOption> options = new java.util.ArrayList<>();
        for (Map<String, Object> org : organizations.values()) {
            options.add(new OrganizationOption(String.valueOf(org.get("orgId")), String.valueOf(org.get("name"))));
        }
        return new OrganizationSelectResult(options);
    }

    @Override
    public synchronized OrganizationSelectResult selectOrganizations(String userId) {
        Map<String, Object> user = users.get(Strings.hasText(userId) ? userId : ADMIN_ACCOUNT.uid);
        if (user == null) {
            return defaultOrganizationSelect();
        }
        return userOrganizationSelect(user);
    }

    @Override
    public synchronized Map<String, Object> getUserInfo(String userId, String orgId) {
        Map<String, Object> user = existingUser(userId);
        Map<String, Object> result = copy(user);
        result.put("uid", user.get("userId"));
        result.put("orgId", Strings.hasText(orgId) ? orgId : DEFAULT_ORG.getId());
        result.put("orgName", organizationName(String.valueOf(result.get("orgId"))));
        return result;
    }

    @Override
    public synchronized void updateUserLanguage(String userId, String language) {
        Map<String, Object> user = existingUser(userId);
        user.put("language", language(Strings.hasText(language) ? language : "zh"));
        user.put("languageCode", Strings.hasText(language) ? language : "zh");
        user.put("updatedAt", CREATED_AT);
        saveRecord(TYPE_USER, String.valueOf(user.get("userId")), user);
    }

    @Override
    public synchronized void updateUserAvatar(String userId, String avatarKey, String avatarPath) {
        Map<String, Object> user = existingUser(userId);
        user.put("avatar", avatar(avatarKey, avatarPath));
        user.put("updatedAt", CREATED_AT);
        saveRecord(TYPE_USER, String.valueOf(user.get("userId")), user);
    }

    @Override
    public synchronized void changeUserPassword(String userId, String oldPassword, String newPassword) {
        Map<String, Object> user = existingUser(userId);
        if (!Strings.hasText(oldPassword)) {
            throw new IllegalArgumentException("oldPassword is required");
        }
        if (!passwordMatches(user, oldPassword)) {
            throw new IllegalArgumentException("oldPassword is incorrect");
        }
        validatePassword(newPassword);
        touchPassword(user, newPassword);
    }

    @Override
    public synchronized void adminChangeUserPassword(String operatorUserId, String userId, String password) {
        validatePassword(password);
        touchPassword(existingUser(userId), password);
    }

    @Override
    public synchronized Map<String, Object> createUser(String operatorUserId, String operatorOrgId, Map<String, Object> request) {
        String userId = defaultText(request, "userId", "");
        if (!Strings.hasText(userId)) {
            userId = defaultText(request, "uid", "user-" + userSequence.incrementAndGet());
        }
        Map<String, Object> user = userRecord(userId, request, operatorUserId, operatorOrgId);
        users.put(userId, user);
        saveRecord(TYPE_USER, userId, user);
        return copy(user);
    }

    @Override
    public synchronized Map<String, Object> importUsers(String operatorUserId, String operatorOrgId, String fileName, long fileSize) {
        Map<String, Object> request = new LinkedHashMap<>();
        String seq = String.valueOf(userSequence.incrementAndGet());
        request.put("userId", "batch-user-" + seq);
        request.put("username", defaultText(singletonMap("fileName", fileName), "fileName", "batch-user-" + seq));
        request.put("nickname", "Imported User " + seq);
        request.put("password", "Imported1!");
        request.put("company", "Wanwu Java");
        request.put("phone", "13800000003");
        request.put("roleIds", Collections.singletonList("app"));
        Map<String, Object> result = importUsers(operatorUserId, operatorOrgId, Collections.singletonList(request));
        result.put("fileName", defaultText(singletonMap("fileName", fileName), "fileName", ""));
        result.put("fileSize", fileSize);
        return result;
    }

    @Override
    public synchronized Map<String, Object> importUsers(String operatorUserId,
                                                        String operatorOrgId,
                                                        List<Map<String, Object>> importedUsers) {
        if (importedUsers == null || importedUsers.isEmpty()) {
            throw new IllegalArgumentException("no valid user data");
        }
        if (importedUsers.size() > MAX_BATCH_CREATE_USERS_LIMIT) {
            throw new IllegalArgumentException("batch user import cannot exceed 500 rows");
        }
        java.util.ArrayList<Map<String, Object>> created = new java.util.ArrayList<>();
        for (Map<String, Object> row : importedUsers) {
            Map<String, Object> request = importedUserRequest(row, operatorOrgId);
            validateImportedUser(request);
            created.add(createUser(operatorUserId, operatorOrgId, request));
        }
        if (created.isEmpty()) {
            throw new IllegalArgumentException("no valid user data");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", created.size());
        result.put("successCount", created.size());
        result.put("failCount", 0);
        result.put("list", created);
        return result;
    }

    @Override
    public synchronized void updateUser(String operatorUserId, String operatorOrgId, Map<String, Object> request) {
        String userId = idFrom(request, "userId", "uid");
        Map<String, Object> current = users.get(userId);
        if (current == null) {
            return;
        }
        putIfText(current, request, "username");
        putIfText(current, request, "nickname");
        putIfText(current, request, "phone");
        putIfText(current, request, "email");
        putIfText(current, request, "gender");
        putIfText(current, request, "remark");
        putIfText(current, request, "company");
        current.put("orgs", userOrgRoles(defaultText(request, "orgId", operatorOrgId), extractRoleIds(request)));
        current.put("updatedAt", CREATED_AT);
        saveRecord(TYPE_USER, userId, current);
    }

    @Override
    public synchronized void deleteUser(String operatorUserId, String operatorOrgId, Map<String, Object> request) {
        String userId = idFrom(request, "userId", "uid");
        if (!ADMIN_ACCOUNT.uid.equals(userId) && !APP_ACCOUNT.uid.equals(userId)) {
            users.remove(userId);
            deleteRecord(TYPE_USER, userId);
        }
    }

    @Override
    public synchronized void updateUserStatus(String operatorUserId, String operatorOrgId, Map<String, Object> request) {
        Map<String, Object> user = users.get(idFrom(request, "userId", "uid"));
        if (user != null) {
            user.put("status", booleanValue(request, "status", true));
            user.put("updatedAt", CREATED_AT);
            saveRecord(TYPE_USER, String.valueOf(user.get("userId")), user);
        }
    }

    @Override
    public synchronized Map<String, Object> listUsers(String orgId, String name, int pageNo, int pageSize) {
        java.util.ArrayList<Map<String, Object>> users = new java.util.ArrayList<>();
        for (Map<String, Object> user : this.users.values()) {
            if (Strings.hasText(name) && !String.valueOf(user.get("username")).toLowerCase().contains(name.trim().toLowerCase())) {
                continue;
            }
            if (Strings.hasText(orgId) && !userBelongsToOrg(user, orgId)) {
                continue;
            }
            users.add(copy(user));
        }
        return page(users, pageNo, pageSize);
    }

    @Override
    public synchronized Map<String, Object> listUsersOutsideOrg(String orgId, String name, int pageNo, int pageSize) {
        java.util.ArrayList<Map<String, Object>> outside = new java.util.ArrayList<>();
        for (Map<String, Object> user : users.values()) {
            if (Strings.hasText(name) && !String.valueOf(user.get("username")).toLowerCase().contains(name.trim().toLowerCase())) {
                continue;
            }
            if (!Strings.hasText(orgId) || !userBelongsToOrg(user, orgId)) {
                outside.add(copy(user));
            }
        }
        return page(outside, pageNo, pageSize);
    }

    @Override
    public synchronized Map<String, Object> selectRoles(String orgId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("select", roleIdNames(new java.util.ArrayList<>(roles.keySet())));
        return result;
    }

    @Override
    public Map<String, Object> roleTemplate(String userId, String orgId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("routes", roleRoutes());
        return result;
    }

    @Override
    public synchronized Map<String, Object> createRole(String operatorUserId, String operatorOrgId, Map<String, Object> request) {
        String roleId = defaultText(request, "roleId", "");
        if (!Strings.hasText(roleId)) {
            roleId = "role-" + roleSequence.incrementAndGet();
        }
        Map<String, Object> role = roleRecord(roleId, request, operatorUserId);
        roles.put(roleId, role);
        saveRecord(TYPE_ROLE, roleId, role);
        return copy(role);
    }

    @Override
    public synchronized void updateRole(String operatorUserId, String operatorOrgId, Map<String, Object> request) {
        String roleId = idFrom(request, "roleId", "id");
        Map<String, Object> role = roles.get(roleId);
        if (role == null || Boolean.TRUE.equals(role.get("isAdmin"))) {
            return;
        }
        putIfText(role, request, "name");
        putIfText(role, request, "remark");
        role.put("permissions", permissionItems(extractPermissions(request)));
        role.put("updatedAt", CREATED_AT);
        saveRecord(TYPE_ROLE, roleId, role);
    }

    @Override
    public synchronized void deleteRole(String operatorUserId, String operatorOrgId, Map<String, Object> request) {
        String roleId = idFrom(request, "roleId", "id");
        if (!"admin".equals(roleId) && !"app".equals(roleId)) {
            roles.remove(roleId);
            deleteRecord(TYPE_ROLE, roleId);
        }
    }

    @Override
    public synchronized void updateRoleStatus(String operatorUserId, String operatorOrgId, Map<String, Object> request) {
        Map<String, Object> role = roles.get(idFrom(request, "roleId", "id"));
        if (role != null) {
            role.put("status", booleanValue(request, "status", true));
            role.put("updatedAt", CREATED_AT);
            saveRecord(TYPE_ROLE, String.valueOf(role.get("roleId")), role);
        }
    }

    @Override
    public synchronized Map<String, Object> listRoles(String userId, String orgId, String name, int pageNo, int pageSize) {
        java.util.ArrayList<Map<String, Object>> roles = new java.util.ArrayList<>();
        for (Map<String, Object> role : this.roles.values()) {
            if (Strings.hasText(name) && !((String) role.get("name")).toLowerCase().contains(name.trim().toLowerCase())) {
                continue;
            }
            roles.add(copy(role));
        }
        return page(roles, pageNo, pageSize);
    }

    @Override
    public synchronized Map<String, Object> roleInfo(String userId, String orgId, String roleId) {
        Map<String, Object> role = roles.get(roleId);
        return role == null ? builtInRole("app") : copy(role);
    }

    @Override
    public synchronized void addOrgUser(String operatorUserId, String operatorOrgId, Map<String, Object> request) {
        String targetOrgId = defaultText(request, "orgId", operatorOrgId);
        String userId = idFrom(request, "userId", "uid");
        Map<String, Object> user = users.get(userId);
        if (user != null) {
            user.put("orgs", userOrgRoles(targetOrgId, extractRoleIds(request)));
            user.put("updatedAt", CREATED_AT);
            saveRecord(TYPE_USER, userId, user);
        }
    }

    @Override
    public synchronized Map<String, Object> createOrganization(String operatorUserId, String parentOrgId, Map<String, Object> request) {
        String orgId = defaultText(request, "orgId", "");
        if (!Strings.hasText(orgId)) {
            orgId = "org-" + orgSequence.incrementAndGet();
        }
        Map<String, Object> org = organizationRecord(orgId, parentOrgId, request, operatorUserId);
        organizations.put(orgId, org);
        saveRecord(TYPE_ORG, orgId, org);
        return copy(org);
    }

    @Override
    public synchronized void updateOrganization(String operatorUserId, String operatorOrgId, Map<String, Object> request) {
        String orgId = idFrom(request, "orgId", "id");
        Map<String, Object> org = organizations.get(orgId);
        if (org == null) {
            return;
        }
        putIfText(org, request, "name");
        putIfText(org, request, "remark");
        org.put("updatedAt", CREATED_AT);
        saveRecord(TYPE_ORG, orgId, org);
    }

    @Override
    public synchronized void deleteOrganization(String operatorUserId, String operatorOrgId, Map<String, Object> request) {
        String orgId = idFrom(request, "orgId", "id");
        if (!DEFAULT_ORG.getId().equals(orgId)) {
            organizations.remove(orgId);
            deleteRecord(TYPE_ORG, orgId);
        }
    }

    @Override
    public synchronized void updateOrganizationStatus(String operatorUserId, String operatorOrgId, Map<String, Object> request) {
        Map<String, Object> org = organizations.get(idFrom(request, "orgId", "id"));
        if (org != null) {
            org.put("status", booleanValue(request, "status", true));
            org.put("updatedAt", CREATED_AT);
            saveRecord(TYPE_ORG, String.valueOf(org.get("orgId")), org);
        }
    }

    @Override
    public synchronized Map<String, Object> listOrganizations(String parentId, String name, int pageNo, int pageSize) {
        java.util.ArrayList<Map<String, Object>> orgs = new java.util.ArrayList<>();
        for (Map<String, Object> org : organizations.values()) {
            if (Strings.hasText(name) && !((String) org.get("name")).toLowerCase().contains(name.trim().toLowerCase())) {
                continue;
            }
            if (Strings.hasText(parentId)
                    && !DEFAULT_ORG.getId().equals(org.get("orgId"))
                    && !parentId.equals(org.get("parentId"))) {
                continue;
            }
            orgs.add(copy(org));
        }
        return page(orgs, pageNo, pageSize);
    }

    @Override
    public synchronized Map<String, Object> organizationInfo(String orgId) {
        Map<String, Object> org = organizations.get(orgId);
        return org == null ? copy(defaultOrganization()) : copy(org);
    }

    @Override
    public synchronized Map<String, Object> createOauthApp(String userId, Map<String, Object> request) {
        long seq = oauthSequence.incrementAndGet();
        String clientId = "oauth-client-" + seq;
        Map<String, Object> app = new LinkedHashMap<>();
        app.put("clientId", clientId);
        app.put("name", defaultText(request, "name", "OAuth App " + seq));
        app.put("desc", defaultText(request, "desc", ""));
        app.put("redirectUri", defaultText(request, "redirectUri", ""));
        app.put("clientSecret", "oauth-secret-" + seq);
        app.put("status", true);
        app.put("createdAt", CREATED_AT);
        app.put("updatedAt", CREATED_AT);
        app.put("userId", Strings.hasText(userId) ? userId : ADMIN_ACCOUNT.uid);
        oauthApps.put(clientId, app);
        saveRecord(TYPE_OAUTH, clientId, app);
        return oauthAppView(app);
    }

    @Override
    public synchronized void updateOauthApp(Map<String, Object> request) {
        Map<String, Object> app = oauthApps.get(defaultText(request, "clientId", ""));
        if (app == null) {
            return;
        }
        app.put("name", defaultText(request, "name", String.valueOf(app.get("name"))));
        app.put("desc", defaultText(request, "desc", ""));
        app.put("redirectUri", defaultText(request, "redirectUri", String.valueOf(app.get("redirectUri"))));
        app.put("updatedAt", CREATED_AT);
        saveRecord(TYPE_OAUTH, String.valueOf(app.get("clientId")), app);
    }

    @Override
    public synchronized void deleteOauthApp(Map<String, Object> request) {
        String clientId = defaultText(request, "clientId", "");
        oauthApps.remove(clientId);
        deleteRecord(TYPE_OAUTH, clientId);
    }

    @Override
    public synchronized void updateOauthAppStatus(Map<String, Object> request) {
        Map<String, Object> app = oauthApps.get(defaultText(request, "clientId", ""));
        if (app != null) {
            app.put("status", booleanValue(request, "status", false));
            app.put("updatedAt", CREATED_AT);
            saveRecord(TYPE_OAUTH, String.valueOf(app.get("clientId")), app);
        }
    }

    @Override
    public synchronized Map<String, Object> listOauthApps(String userId, String name, int pageNo, int pageSize) {
        java.util.ArrayList<Map<String, Object>> apps = new java.util.ArrayList<>();
        for (Map<String, Object> app : oauthApps.values()) {
            String owner = String.valueOf(app.get("userId"));
            if (Strings.hasText(userId) && Strings.hasText(owner) && !userId.equals(owner)) {
                continue;
            }
            if (Strings.hasText(name) && !String.valueOf(app.get("name")).toLowerCase().contains(name.trim().toLowerCase())) {
                continue;
            }
            apps.add(oauthAppView(app));
        }
        return page(apps, pageNo, pageSize);
    }

    @Override
    public void updateCustomTab(Map<String, Object> request) {
        Map<String, Object> tab = new LinkedHashMap<>();
        tab.put("logo", avatarValue(request, "tabLogo"));
        tab.put("title", defaultText(request, "tabTitle", "Wanwu Java"));
        customTab = tab;
        saveRecord(TYPE_CUSTOM_TAB, "default", tab);
    }

    @Override
    public void updateCustomLogin(Map<String, Object> request) {
        Map<String, Object> login = new LinkedHashMap<>();
        login.put("background", avatarValue(request, "loginBg"));
        login.put("logo", avatarValue(request, "loginLogo"));
        login.put("welcomeText", defaultText(request, "loginWelcomeText", ""));
        login.put("loginButtonColor", defaultText(request, "loginButtonColor", "#5983FF"));
        customLogin = login;
        saveRecord(TYPE_CUSTOM_LOGIN, "default", login);
    }

    @Override
    public void updateCustomHome(Map<String, Object> request) {
        Map<String, Object> home = new LinkedHashMap<>();
        home.put("logo", avatarValue(request, "homeLogo"));
        home.put("title", defaultText(request, "homeName", "Wanwu Java"));
        home.put("backgroundColor", defaultText(request, "homeBgColor", "#F7F8FA"));
        customHome = home;
        saveRecord(TYPE_CUSTOM_HOME, "default", home);
    }

    @Override
    public Map<String, Object> platformConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        Map<String, Object> login = new LinkedHashMap<>();
        login.put("logo", Collections.emptyMap());
        login.put("background", Collections.emptyMap());
        login.put("loginButtonColor", "#5983FF");
        login.put("welcomeText", "");
        login.put("platformDesc", "Wanwu Java development environment");

        Map<String, Object> home = new LinkedHashMap<>();
        home.put("logo", Collections.emptyMap());
        home.put("title", "Wanwu Java");
        home.put("backgroundColor", "#F7F8FA");

        Map<String, Object> tab = new LinkedHashMap<>();
        tab.put("logo", Collections.emptyMap());
        tab.put("title", "Wanwu Java");

        Map<String, Object> about = new LinkedHashMap<>();
        about.put("version", "0.1.0-SNAPSHOT");

        Map<String, Object> defaultIcon = new LinkedHashMap<>();
        defaultIcon.put("agentIcon", "");
        defaultIcon.put("ragIcon", "");
        defaultIcon.put("workflowIcon", "");
        defaultIcon.put("modelIcon", "");
        defaultIcon.put("knowledgeIcon", "");
        defaultIcon.put("toolIcon", "");
        defaultIcon.put("mcpIcon", "");
        defaultIcon.put("promptIcon", "");
        defaultIcon.put("skillIcon", "");
        defaultIcon.put("safetyIcon", "");

        login.putAll(customLogin);
        home.putAll(customHome);
        tab.putAll(customTab);

        config.put("login", login);
        config.put("home", home);
        config.put("tab", tab);
        config.put("about", about);
        config.put("defaultIcon", defaultIcon);
        config.put("loginEmail", emailToggle(false));
        config.put("register", emailToggle(false));
        config.put("resetPassword", emailToggle(false));
        return config;
    }

    public static ServiceDescriptor descriptor() {
        return ServiceDescriptor.of(ServiceNames.IAM, "IAM Service", "iam");
    }

    private void loadRecords(String recordType,
                             Map<String, Map<String, Object>> target,
                             AtomicLong sequence,
                             String... sequencePrefixes) {
        for (IamRecordEntity record : iamRecordMapper.selectByType(recordType)) {
            Map<String, Object> payload = readPayload(record);
            target.put(record.getRecordId(), payload);
            if (sequencePrefixes != null) {
                for (String prefix : sequencePrefixes) {
                    bumpSequence(sequence, record.getRecordId(), prefix);
                }
            }
        }
    }

    private Map<String, Object> loadSingle(String recordType, String recordId, Map<String, Object> fallback) {
        for (IamRecordEntity record : iamRecordMapper.selectByType(recordType)) {
            if (recordId.equals(record.getRecordId())) {
                return readPayload(record);
            }
        }
        return fallback;
    }

    private Map<String, Object> readPayload(IamRecordEntity record) {
        try {
            return JSON.readValue(record.getPayload(), MAP_TYPE);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read IAM record " + record.getRecordType()
                    + "/" + record.getRecordId(), ex);
        }
    }

    private void saveRecord(String recordType, String recordId, Map<String, Object> payload) {
        if (iamRecordMapper == null || !Strings.hasText(recordId)) {
            return;
        }
        try {
            long now = System.currentTimeMillis();
            IamRecordEntity entity = new IamRecordEntity();
            entity.setRecordType(recordType);
            entity.setRecordId(recordId);
            entity.setPayload(JSON.writeValueAsString(payload));
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            iamRecordMapper.upsertRecord(entity);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to save IAM record " + recordType + "/" + recordId, ex);
        }
    }

    private void deleteRecord(String recordType, String recordId) {
        if (iamRecordMapper == null || !Strings.hasText(recordId)) {
            return;
        }
        iamRecordMapper.deleteRecord(recordType, recordId);
    }

    private void bumpSequence(AtomicLong sequence, String recordId, String prefix) {
        if (!Strings.hasText(recordId) || !recordId.startsWith(prefix)) {
            return;
        }
        try {
            long value = Long.parseLong(recordId.substring(prefix.length()));
            if (value > sequence.get()) {
                sequence.set(value);
            }
        } catch (NumberFormatException ignored) {
            // Development IDs may be human supplied; only numeric suffixes advance sequences.
        }
    }

    private Map<String, Object> orgPermission(DevAccount account) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("org", org(DEFAULT_ORG));
        result.put("permissions", permissionItems(account.permissions));
        result.put("roles", roleIdNames(account.roles));
        result.put("isAdmin", account.admin);
        result.put("isSystem", account.system);
        return result;
    }

    private Map<String, Object> org(OrganizationOption option) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", option.getId());
        result.put("name", option.getName());
        return result;
    }

    private Map<String, Object> permissionItem(String perm) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("perm", perm);
        result.put("name", permissionName(perm));
        return result;
    }

    private List<Map<String, Object>> permissionItems(List<String> permissions) {
        java.util.ArrayList<Map<String, Object>> result = new java.util.ArrayList<>();
        for (String permission : permissions) {
            result.add(permissionItem(permission));
        }
        return result;
    }

    private Map<String, Object> page(List<Map<String, Object>> all, int pageNo, int pageSize) {
        int safePageNo = pageNo <= 0 ? 1 : pageNo;
        int safePageSize = pageSize <= 0 ? 10 : pageSize;
        int from = Math.min((safePageNo - 1) * safePageSize, all.size());
        int to = Math.min(from + safePageSize, all.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", new java.util.ArrayList<>(all.subList(from, to)));
        result.put("total", (long) all.size());
        result.put("pageNo", safePageNo);
        result.put("pageSize", safePageSize);
        return result;
    }

    private Map<String, Object> userInfo(DevAccount account) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", account.uid);
        result.put("uid", account.uid);
        result.put("username", account.username);
        result.put("nickname", account.username);
        result.put("phone", "");
        result.put("email", "");
        result.put("gender", "");
        result.put("remark", "development account");
        result.put("company", "Wanwu Java");
        result.put("createdAt", CREATED_AT);
        result.put("creator", idName("system", "System"));
        result.put("status", true);
        result.put("language", language());
        result.put("avatar", avatar("", ""));
        result.put("passwordVersion", 0);

        Map<String, Object> orgRole = new LinkedHashMap<>();
        orgRole.put("org", org(DEFAULT_ORG));
        orgRole.put("roles", roleIdNames(account.roles));
        result.put("orgs", Collections.singletonList(orgRole));
        return result;
    }

    private Map<String, Object> userRecord(String userId,
                                           Map<String, Object> request,
                                           String operatorUserId,
                                           String operatorOrgId) {
        String username = defaultText(request, "username", defaultText(request, "name", userId));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId);
        result.put("uid", userId);
        result.put("username", username);
        result.put("nickname", defaultText(request, "nickname", username));
        result.put("phone", defaultText(request, "phone", ""));
        result.put("email", defaultText(request, "email", ""));
        result.put("gender", defaultText(request, "gender", ""));
        result.put("remark", defaultText(request, "remark", ""));
        result.put("company", defaultText(request, "company", "Wanwu Java"));
        result.put("createdAt", CREATED_AT);
        result.put("updatedAt", CREATED_AT);
        result.put("creator", idName(Strings.hasText(operatorUserId) ? operatorUserId : "system", "System"));
        result.put("status", booleanValue(request, "status", true));
        result.put("language", language());
        result.put("avatar", avatar("", ""));
        result.put("passwordVersion", 0);
        String password = defaultText(request, "password", "");
        if (Strings.hasText(password)) {
            validatePassword(password);
            result.put("passwordHash", passwordHash(password));
        }
        result.put("orgs", userOrgRoles(defaultText(request, "orgId", operatorOrgId), extractRoleIds(request)));
        return result;
    }

    private Map<String, Object> importedUserRequest(Map<String, Object> row, String operatorOrgId) {
        Map<String, Object> request = new LinkedHashMap<>();
        String username = defaultText(row, "username", defaultText(row, "userName", ""));
        request.put("username", username);
        request.put("nickname", defaultText(row, "nickname", username));
        request.put("password", defaultText(row, "password", ""));
        request.put("phone", defaultText(row, "phone", ""));
        request.put("email", defaultText(row, "email", ""));
        request.put("gender", defaultText(row, "gender", ""));
        request.put("remark", defaultText(row, "remark", ""));
        request.put("company", defaultText(row, "company", ""));
        request.put("orgId", operatorOrgId);
        Object roleIds = firstPresent(row, "roleIds", "roles", "roleId");
        if (roleIds != null) {
            request.put("roleIds", roleIds);
        } else {
            String roleName = defaultText(row, "roleName", defaultText(row, "role", "app"));
            request.put("roleIds", Collections.singletonList(roleIdForName(roleName)));
        }
        return request;
    }

    private void validateImportedUser(Map<String, Object> request) {
        String username = defaultText(request, "username", "");
        validateUsername(username);
        try {
            validatePassword(defaultText(request, "password", ""));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("username " + username + ": " + ex.getMessage());
        }
        String phone = defaultText(request, "phone", "");
        if (Strings.hasText(phone) && !PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("phone " + phone + ": phone number format is invalid");
        }
        if (!Strings.hasText(defaultText(request, "company", ""))) {
            throw new IllegalArgumentException("username " + username + ": company is empty");
        }
    }

    private void validateUsername(String username) {
        if (!Strings.hasText(username) || username.length() < 2 || username.length() > 20) {
            throw new IllegalArgumentException("username length must be 2-20 characters");
        }
        if (username.charAt(0) == '_') {
            throw new IllegalArgumentException("username cannot start with underscore");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("username can only contain Chinese, English, numbers, underscores and parentheses");
        }
    }

    private String roleIdForName(String roleName) {
        if (!Strings.hasText(roleName)) {
            return "app";
        }
        for (Map<String, Object> role : roles.values()) {
            if (roleName.equals(String.valueOf(role.get("roleId")))
                    || roleName.equals(String.valueOf(role.get("id")))
                    || roleName.equals(String.valueOf(role.get("name")))) {
                return String.valueOf(role.get("roleId"));
            }
        }
        return roleName;
    }

    private Map<String, Object> defaultOrganization() {
        Map<String, Object> org = new LinkedHashMap<>();
        org.put("orgId", DEFAULT_ORG.getId());
        org.put("id", DEFAULT_ORG.getId());
        org.put("parentId", "");
        org.put("name", DEFAULT_ORG.getName());
        org.put("remark", "Default development organization");
        org.put("createdAt", CREATED_AT);
        org.put("updatedAt", CREATED_AT);
        org.put("creator", idName("system", "System"));
        org.put("status", true);
        return org;
    }

    private Map<String, Object> organizationRecord(String orgId,
                                                   String parentOrgId,
                                                   Map<String, Object> request,
                                                   String operatorUserId) {
        Map<String, Object> org = new LinkedHashMap<>();
        org.put("orgId", orgId);
        org.put("id", orgId);
        org.put("parentId", defaultText(request, "parentId", defaultText(request, "parentOrgId", parentOrgId)));
        org.put("name", defaultText(request, "name", "Organization " + orgId));
        org.put("remark", defaultText(request, "remark", ""));
        org.put("createdAt", CREATED_AT);
        org.put("updatedAt", CREATED_AT);
        org.put("creator", idName(Strings.hasText(operatorUserId) ? operatorUserId : "system", "System"));
        org.put("status", booleanValue(request, "status", true));
        return org;
    }

    private Map<String, Object> builtInRole(String roleId) {
        boolean admin = "admin".equals(roleId);
        List<String> permissions = admin ? IMPLEMENTED_FRONTEND_PERMISSIONS : APP_PERMISSIONS;
        Map<String, Object> role = new LinkedHashMap<>();
        role.put("roleId", admin ? "admin" : "app");
        role.put("id", admin ? "admin" : "app");
        role.put("name", admin ? "System Admin" : "App User");
        role.put("remark", admin ? "Built-in development administrator" : "Application-only development role");
        role.put("createdAt", CREATED_AT);
        role.put("updatedAt", CREATED_AT);
        role.put("creator", idName("system", "System"));
        role.put("status", true);
        role.put("isAdmin", admin);
        role.put("routes", roleRoutes());
        role.put("permissions", permissionItems(permissions));
        return role;
    }

    private Map<String, Object> roleRecord(String roleId, Map<String, Object> request, String operatorUserId) {
        Map<String, Object> role = new LinkedHashMap<>();
        role.put("roleId", roleId);
        role.put("id", roleId);
        role.put("name", defaultText(request, "name", "Role " + roleId));
        role.put("remark", defaultText(request, "remark", ""));
        role.put("createdAt", CREATED_AT);
        role.put("updatedAt", CREATED_AT);
        role.put("creator", idName(Strings.hasText(operatorUserId) ? operatorUserId : "system", "System"));
        role.put("status", booleanValue(request, "status", true));
        role.put("isAdmin", false);
        role.put("routes", roleRoutes());
        role.put("permissions", permissionItems(extractPermissions(request)));
        return role;
    }

    private Map<String, Object> idName(String id, String name) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("name", name);
        return result;
    }

    private List<Map<String, Object>> roleIdNames(List<String> roleIds) {
        java.util.ArrayList<Map<String, Object>> result = new java.util.ArrayList<>();
        for (String roleId : roleIds) {
            result.add(roleIdName(roleId));
        }
        return result;
    }

    private Map<String, Object> roleIdName(String roleId) {
        if ("admin".equals(roleId)) {
            return idName("admin", "System Admin");
        }
        if ("app".equals(roleId)) {
            return idName("app", "App User");
        }
        Map<String, Object> role = roles.get(roleId);
        return idName(roleId, role == null ? roleId : String.valueOf(role.get("name")));
    }

    private List<Map<String, Object>> roleRoutes() {
        return Arrays.asList(
                route("Permission", "permission", Arrays.asList(
                        route("Users", "permission.user", Collections.<Map<String, Object>>emptyList()),
                        route("Organizations", "permission.org", Collections.<Map<String, Object>>emptyList()),
                        route("Roles", "permission.role", Collections.<Map<String, Object>>emptyList())
                )),
                route("Model Service", "model", Collections.singletonList(
                        route("Model Management", "model.model_management", Collections.<Map<String, Object>>emptyList())
                )),
                route("Application", "app", Arrays.asList(
                        route("RAG", "app.rag", Collections.<Map<String, Object>>emptyList()),
                        route("Workflow", "app.workflow", Collections.<Map<String, Object>>emptyList()),
                        route("Agent", "app.agent", Collections.<Map<String, Object>>emptyList())
                )),
                route("API Key", "api_key", Collections.singletonList(
                        route("API Key Management", "api_key.api_key_management", Collections.<Map<String, Object>>emptyList())
                )),
                route("Resource", "resource", Arrays.asList(
                        route("Knowledge", "resource.knowledge", Collections.<Map<String, Object>>emptyList()),
                        route("Tool", "resource.tool", Collections.<Map<String, Object>>emptyList()),
                        route("MCP", "resource.mcp", Collections.<Map<String, Object>>emptyList()),
                        route("Prompt", "resource.prompt", Collections.<Map<String, Object>>emptyList()),
                        route("Skill", "resource.skill", Collections.<Map<String, Object>>emptyList()),
                        route("Safety", "resource.safety", Collections.<Map<String, Object>>emptyList())
                )),
                route("Setting", "setting", Collections.<Map<String, Object>>emptyList()),
                route("Operation", "operation", Arrays.asList(
                        route("OAuth", "operation.oauth", Collections.<Map<String, Object>>emptyList()),
                        route("Client Statistic", "operation.statistic_client", Collections.<Map<String, Object>>emptyList())
                )),
                route("Exploration", "exploration", Arrays.asList(
                        route("Application Square", "exploration.app", Collections.<Map<String, Object>>emptyList()),
                        route("MCP Square", "exploration.mcp", Collections.<Map<String, Object>>emptyList()),
                        route("Template Square", "exploration.template", Collections.<Map<String, Object>>emptyList()),
                        route("Skill Square", "exploration.skill", Collections.<Map<String, Object>>emptyList())
                )),
                route("App Observation", "app_observability", Collections.singletonList(
                        route("Statistic Dashboard", "app_observability.statistic", Collections.<Map<String, Object>>emptyList())
                ))
        );
    }

    private Map<String, Object> route(String name, String perm, List<Map<String, Object>> children) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", name);
        result.put("perm", perm);
        result.put("children", children);
        return result;
    }

    private String permissionName(String perm) {
        if ("permission".equals(perm)) {
            return "Permission";
        }
        if ("permission.user".equals(perm)) {
            return "Users";
        }
        if ("permission.org".equals(perm)) {
            return "Organizations";
        }
        if ("permission.role".equals(perm)) {
            return "Roles";
        }
        if ("setting".equals(perm)) {
            return "Setting";
        }
        if ("app".equals(perm)) {
            return "Application";
        }
        if ("app.agent".equals(perm)) {
            return "Agent";
        }
        if ("app.workflow".equals(perm)) {
            return "Workflow";
        }
        if ("app.rag".equals(perm)) {
            return "RAG";
        }
        if ("model".equals(perm)) {
            return "Model Service";
        }
        if ("model.model_management".equals(perm)) {
            return "Model Management";
        }
        if ("api_key".equals(perm)) {
            return "API Key";
        }
        if ("api_key.api_key_management".equals(perm)) {
            return "API Key Management";
        }
        if ("resource".equals(perm)) {
            return "Resource";
        }
        if ("resource.knowledge".equals(perm)) {
            return "Knowledge";
        }
        if ("resource.tool".equals(perm)) {
            return "Tool";
        }
        if ("resource.mcp".equals(perm)) {
            return "MCP";
        }
        if ("resource.prompt".equals(perm)) {
            return "Prompt";
        }
        if ("resource.skill".equals(perm)) {
            return "Skill";
        }
        if ("resource.safety".equals(perm)) {
            return "Safety";
        }
        if ("operation".equals(perm)) {
            return "Operation";
        }
        if ("operation.oauth".equals(perm)) {
            return "OAuth";
        }
        if ("operation.statistic_client".equals(perm)) {
            return "Client Statistic";
        }
        if ("exploration".equals(perm)) {
            return "Exploration";
        }
        if ("exploration.app".equals(perm)) {
            return "Application Square";
        }
        if ("exploration.mcp".equals(perm)) {
            return "MCP Square";
        }
        if ("exploration.template".equals(perm)) {
            return "Template Square";
        }
        if ("exploration.skill".equals(perm)) {
            return "Skill Square";
        }
        if ("app_observability".equals(perm)) {
            return "App Observation";
        }
        if ("app_observability.statistic".equals(perm)) {
            return "Statistic Dashboard";
        }
        return perm;
    }

    private List<Map<String, Object>> userOrgRoles(String orgId, List<String> roleIds) {
        String safeOrgId = Strings.hasText(orgId) ? orgId : DEFAULT_ORG.getId();
        Map<String, Object> orgRole = new LinkedHashMap<>();
        orgRole.put("org", org(organizationInfo(safeOrgId)));
        orgRole.put("roles", roleIdNames(roleIds.isEmpty() ? Collections.singletonList("app") : roleIds));
        return Collections.singletonList(orgRole);
    }

    @SuppressWarnings("unchecked")
    private OrganizationSelectResult userOrganizationSelect(Map<String, Object> user) {
        java.util.ArrayList<OrganizationOption> options = new java.util.ArrayList<>();
        java.util.LinkedHashSet<String> seen = new java.util.LinkedHashSet<>();
        Object orgs = user.get("orgs");
        if (orgs instanceof List) {
            for (Object item : (List<Object>) orgs) {
                if (!(item instanceof Map)) {
                    continue;
                }
                Object org = ((Map<String, Object>) item).get("org");
                if (!(org instanceof Map)) {
                    continue;
                }
                Object orgIdValue = ((Map<String, Object>) org).get("id");
                if (orgIdValue == null) {
                    continue;
                }
                String orgId = String.valueOf(orgIdValue);
                if (!Strings.hasText(orgId) || !seen.add(orgId)) {
                    continue;
                }
                Object orgNameValue = ((Map<String, Object>) org).get("name");
                String orgName = orgNameValue == null ? "" : String.valueOf(orgNameValue);
                options.add(new OrganizationOption(orgId, Strings.hasText(orgName) ? orgName : organizationName(orgId)));
            }
        }
        if (options.isEmpty()) {
            return defaultOrganizationSelect();
        }
        return new OrganizationSelectResult(options);
    }

    private OrganizationSelectResult defaultOrganizationSelect() {
        return new OrganizationSelectResult(Collections.singletonList(DEFAULT_ORG));
    }

    @SuppressWarnings("unchecked")
    private boolean userBelongsToOrg(Map<String, Object> user, String orgId) {
        Object orgs = user.get("orgs");
        if (!(orgs instanceof List)) {
            return false;
        }
        for (Object item : (List<Object>) orgs) {
            if (!(item instanceof Map)) {
                continue;
            }
            Object org = ((Map<String, Object>) item).get("org");
            if (org instanceof Map && orgId.equals(String.valueOf(((Map<String, Object>) org).get("id")))) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> org(Map<String, Object> organization) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", organization.get("orgId"));
        result.put("name", organization.get("name"));
        return result;
    }

    private Map<String, Object> existingUser(String userId) {
        String safeUserId = Strings.hasText(userId) ? userId : ADMIN_ACCOUNT.uid;
        Map<String, Object> user = users.get(safeUserId);
        if (user == null) {
            throw new IllegalArgumentException("user not found: " + safeUserId);
        }
        return user;
    }

    private String organizationName(String orgId) {
        Map<String, Object> organization = organizations.get(orgId);
        return organization == null ? DEFAULT_ORG.getName() : String.valueOf(organization.get("name"));
    }

    private void touchPassword(Map<String, Object> user, String password) {
        int version = intValue(user.get("passwordVersion"), 0) + 1;
        user.put("passwordHash", passwordHash(password));
        user.put("passwordVersion", version);
        user.put("passwordChangedAt", CREATED_AT);
        user.put("updatedAt", CREATED_AT);
        saveRecord(TYPE_USER, String.valueOf(user.get("userId")), user);
    }

    private boolean passwordMatches(Map<String, Object> user, String password) {
        String hash = defaultText(user, "passwordHash", "");
        if (!Strings.hasText(hash)) {
            return Strings.hasText(password);
        }
        return hash.equals(passwordHash(password));
    }

    private void validatePassword(String password) {
        if (!Strings.hasText(password)) {
            throw new IllegalArgumentException("password is required");
        }
        if (password.length() < 8 || password.length() > 20) {
            throw new IllegalArgumentException("password length must be 8-20 characters");
        }
        boolean hasLetter = false;
        boolean hasNumber = false;
        boolean hasSpecial = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                hasLetter = true;
            } else if (c >= '0' && c <= '9') {
                hasNumber = true;
            } else if (PASSWORD_SPECIALS.indexOf(c) >= 0) {
                hasSpecial = true;
            }
        }
        if (!hasLetter || !hasNumber || !hasSpecial) {
            throw new IllegalArgumentException("password must contain letters, numbers and special characters");
        }
    }

    private String passwordHash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((password == null ? "" : password).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte item : bytes) {
                builder.append(String.format(Locale.ROOT, "%02x", item & 0xff));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private String idFrom(Map<String, Object> request, String... keys) {
        for (String key : keys) {
            String value = defaultText(request, key, "");
            if (Strings.hasText(value)) {
                return value;
            }
        }
        return "";
    }

    private void putIfText(Map<String, Object> target, Map<String, Object> request, String key) {
        if (request == null) {
            return;
        }
        Object value = request.get(key);
        if (value != null && Strings.hasText(String.valueOf(value))) {
            target.put(key, value);
        }
    }

    private List<String> extractRoleIds(Map<String, Object> request) {
        List<String> values = extractStrings(firstPresent(request, "roleIds", "roles", "roleId"));
        return values.isEmpty() ? Collections.singletonList("app") : values;
    }

    private List<String> extractPermissions(Map<String, Object> request) {
        List<String> values = extractStrings(firstPresent(request, "permissions", "perms", "routes"));
        return values.isEmpty() ? APP_PERMISSIONS : values;
    }

    private Object firstPresent(Map<String, Object> request, String... keys) {
        if (request == null) {
            return null;
        }
        for (String key : keys) {
            if (request.containsKey(key)) {
                return request.get(key);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<String> extractStrings(Object value) {
        java.util.ArrayList<String> result = new java.util.ArrayList<>();
        if (value == null) {
            return result;
        }
        if (value instanceof List) {
            for (Object item : (List<Object>) value) {
                result.addAll(extractStrings(item));
            }
            return result;
        }
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            for (String key : Arrays.asList("perm", "id", "roleId", "value")) {
                Object item = map.get(key);
                if (item != null && Strings.hasText(String.valueOf(item))) {
                    result.add(String.valueOf(item));
                    return result;
                }
            }
            return result;
        }
        if (Strings.hasText(String.valueOf(value))) {
            result.add(String.valueOf(value));
        }
        return result;
    }

    private Map<String, Object> copy(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>(source);
        result.remove("passwordHash");
        return result;
    }

    private Map<String, Object> emailToggle(boolean status) {
        Map<String, Object> email = new LinkedHashMap<>();
        email.put("status", status);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("email", email);
        return result;
    }

    private Map<String, Object> language() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", "zh");
        result.put("name", "简体中文");
        return result;
    }

    private Map<String, Object> language(String code) {
        String safeCode = Strings.hasText(code) ? code : "zh";
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", safeCode);
        result.put("name", "en".equals(safeCode) ? "English" : "Simplified Chinese");
        return result;
    }

    private Map<String, Object> avatar(String key, String path) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("key", Strings.hasText(key) ? key : "");
        result.put("path", Strings.hasText(path) ? path : "");
        return result;
    }

    private Map<String, Object> singletonMap(String key, Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(key, value);
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapValue(Object value) {
        if (value instanceof Map) {
            return new LinkedHashMap<>((Map<String, Object>) value);
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> avatarValue(Map<String, Object> request, String key) {
        if (request == null || !(request.get(key) instanceof Map)) {
            return Collections.emptyMap();
        }
        return new LinkedHashMap<>((Map<String, Object>) request.get(key));
    }

    private String defaultText(Map<String, Object> request, String key, String fallback) {
        if (request == null) {
            return fallback;
        }
        Object value = request.get(key);
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return fallback;
        }
        return String.valueOf(value);
    }

    private int intValue(Object value, int fallback) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private boolean booleanValue(Map<String, Object> request, String key, boolean fallback) {
        if (request == null || request.get(key) == null) {
            return fallback;
        }
        Object value = request.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private Map<String, Object> oauthAppView(Map<String, Object> app) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("clientId", app.get("clientId"));
        result.put("name", app.get("name"));
        result.put("desc", app.get("desc"));
        result.put("clientSecret", app.get("clientSecret"));
        result.put("redirectUri", app.get("redirectUri"));
        result.put("status", app.get("status"));
        result.put("createdAt", app.get("createdAt"));
        result.put("updatedAt", app.get("updatedAt"));
        return result;
    }

    private static DevAccount accountByUsername(String username) {
        if (username == null) {
            return null;
        }
        return ACCOUNTS_BY_USERNAME.get(username.trim());
    }

    private static DevAccount accountByToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return ADMIN_ACCOUNT;
        }
        DevAccount account = ACCOUNTS_BY_TOKEN.get(token.trim());
        return account == null ? ADMIN_ACCOUNT : account;
    }

    private static Map<String, DevAccount> accountsByUsername() {
        Map<String, DevAccount> result = new LinkedHashMap<>();
        result.put(ADMIN_ACCOUNT.username, ADMIN_ACCOUNT);
        result.put(APP_ACCOUNT.username, APP_ACCOUNT);
        return Collections.unmodifiableMap(result);
    }

    private static Map<String, DevAccount> accountsByToken() {
        Map<String, DevAccount> result = new LinkedHashMap<>();
        result.put(ADMIN_ACCOUNT.token, ADMIN_ACCOUNT);
        result.put(APP_ACCOUNT.token, APP_ACCOUNT);
        return Collections.unmodifiableMap(result);
    }

    private static class DevAccount {
        private final String username;
        private final String uid;
        private final String userCategory;
        private final String token;
        private final List<String> roles;
        private final boolean admin;
        private final boolean system;
        private final List<String> permissions;

        private DevAccount(String username,
                           String uid,
                           String userCategory,
                           String token,
                           List<String> roles,
                           boolean admin,
                           boolean system,
                           List<String> permissions) {
            this.username = username;
            this.uid = uid;
            this.userCategory = userCategory;
            this.token = token;
            this.roles = roles;
            this.admin = admin;
            this.system = system;
            this.permissions = permissions;
        }
    }
}
