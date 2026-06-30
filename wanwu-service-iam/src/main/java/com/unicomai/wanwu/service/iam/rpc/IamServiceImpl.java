package com.unicomai.wanwu.service.iam.rpc;

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
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@DubboService(version = RpcConstants.VERSION, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
public class IamServiceImpl implements IamService {

    private static final List<String> APP_PERMISSIONS = Arrays.asList("app", "app.agent");
    private static final List<String> IMPLEMENTED_FRONTEND_PERMISSIONS = Collections.unmodifiableList(Arrays.asList(
            "permission",
            "permission.user",
            "permission.org",
            "permission.role",
            "model",
            "model.model_management",
            "app",
            "app.agent",
            "api_key",
            "api_key.api_key_management",
            "resource.knowledge"
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
    public PermissionResult permission(String token) {
        DevAccount account = accountByToken(token);
        PermissionResult result = new PermissionResult();
        result.setOrgPermission(orgPermission(account));
        result.setIsUpdatePassword(true);
        result.setAvatar(singletonMap("path", ""));
        result.setLanguage(language());
        return result;
    }

    @Override
    public OrganizationSelectResult selectOrganizations() {
        return new OrganizationSelectResult(Collections.singletonList(DEFAULT_ORG));
    }

    @Override
    public Map<String, Object> listUsers(String orgId, String name, int pageNo, int pageSize) {
        java.util.ArrayList<Map<String, Object>> users = new java.util.ArrayList<>();
        for (DevAccount account : ACCOUNTS_BY_USERNAME.values()) {
            if (Strings.hasText(name) && !account.username.toLowerCase().contains(name.trim().toLowerCase())) {
                continue;
            }
            users.add(userInfo(account));
        }
        return page(users, pageNo, pageSize);
    }

    @Override
    public Map<String, Object> selectRoles(String orgId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("select", roleIdNames(Arrays.asList("admin", "app")));
        return result;
    }

    @Override
    public Map<String, Object> roleTemplate(String userId, String orgId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("routes", roleRoutes());
        return result;
    }

    @Override
    public Map<String, Object> listRoles(String userId, String orgId, String name, int pageNo, int pageSize) {
        java.util.ArrayList<Map<String, Object>> roles = new java.util.ArrayList<>();
        for (String roleId : Arrays.asList("admin", "app")) {
            Map<String, Object> role = roleInfo(userId, orgId, roleId);
            if (Strings.hasText(name) && !((String) role.get("name")).toLowerCase().contains(name.trim().toLowerCase())) {
                continue;
            }
            roles.add(role);
        }
        return page(roles, pageNo, pageSize);
    }

    @Override
    public Map<String, Object> roleInfo(String userId, String orgId, String roleId) {
        boolean admin = "admin".equals(roleId);
        List<String> permissions = admin ? IMPLEMENTED_FRONTEND_PERMISSIONS : APP_PERMISSIONS;
        Map<String, Object> role = new LinkedHashMap<>();
        role.put("roleId", admin ? "admin" : "app");
        role.put("name", admin ? "System Admin" : "App User");
        role.put("remark", admin ? "Built-in development administrator" : "Agent-only development role");
        role.put("createdAt", CREATED_AT);
        role.put("creator", idName("system", "System"));
        role.put("status", true);
        role.put("isAdmin", admin);
        role.put("routes", roleRoutes());
        role.put("permissions", permissionItems(permissions));
        return role;
    }

    @Override
    public Map<String, Object> listOrganizations(String parentId, String name, int pageNo, int pageSize) {
        java.util.ArrayList<Map<String, Object>> orgs = new java.util.ArrayList<>();
        Map<String, Object> org = organizationInfo(DEFAULT_ORG.getId());
        if (!Strings.hasText(name) || ((String) org.get("name")).toLowerCase().contains(name.trim().toLowerCase())) {
            orgs.add(org);
        }
        return page(orgs, pageNo, pageSize);
    }

    @Override
    public Map<String, Object> organizationInfo(String orgId) {
        Map<String, Object> org = new LinkedHashMap<>();
        org.put("orgId", DEFAULT_ORG.getId());
        org.put("name", DEFAULT_ORG.getName());
        org.put("remark", "Default development organization");
        org.put("createdAt", CREATED_AT);
        org.put("creator", idName("system", "System"));
        org.put("status", true);
        return org;
    }

    @Override
    public Map<String, Object> platformConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        Map<String, Object> login = new LinkedHashMap<>();
        login.put("logo", Collections.emptyMap());
        login.put("background", Collections.emptyMap());
        login.put("loginButtonColor", "#5983FF");
        login.put("platformDesc", "Wanwu Java development environment");

        Map<String, Object> home = new LinkedHashMap<>();
        home.put("logo", Collections.emptyMap());
        home.put("backgroundColor", "#F7F8FA");

        Map<String, Object> tab = new LinkedHashMap<>();
        tab.put("logo", Collections.emptyMap());
        tab.put("title", "Wanwu Java");

        Map<String, Object> about = new LinkedHashMap<>();
        about.put("version", "0.1.0-SNAPSHOT");

        Map<String, Object> defaultIcon = new LinkedHashMap<>();
        defaultIcon.put("agentIcon", "");
        defaultIcon.put("ragIcon", "");
        defaultIcon.put("modelIcon", "");

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
        result.put("avatar", singletonMap("path", ""));

        Map<String, Object> orgRole = new LinkedHashMap<>();
        orgRole.put("org", org(DEFAULT_ORG));
        orgRole.put("roles", roleIdNames(account.roles));
        result.put("orgs", Collections.singletonList(orgRole));
        return result;
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
        return idName("app", "App User");
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
                route("Application", "app", Collections.singletonList(
                        route("Agent", "app.agent", Collections.<Map<String, Object>>emptyList())
                )),
                route("API Key", "api_key", Collections.singletonList(
                        route("API Key Management", "api_key.api_key_management", Collections.<Map<String, Object>>emptyList())
                )),
                route("Knowledge", "resource.knowledge", Collections.<Map<String, Object>>emptyList())
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
        if ("app".equals(perm)) {
            return "Application";
        }
        if ("app.agent".equals(perm)) {
            return "Agent";
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
        if ("resource.knowledge".equals(perm)) {
            return "Knowledge";
        }
        return perm;
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

    private Map<String, Object> singletonMap(String key, Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(key, value);
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
