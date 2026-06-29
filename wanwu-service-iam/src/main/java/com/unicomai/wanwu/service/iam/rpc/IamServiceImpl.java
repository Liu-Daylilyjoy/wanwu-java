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
import java.util.Map;

@DubboService(version = RpcConstants.VERSION, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
public class IamServiceImpl implements IamService {

    private static final String DEV_USERNAME = "admin";
    private static final String DEV_UID = "dev-admin";
    private static final String DEV_TOKEN = "dev-token";
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
        if (command == null || !DEV_USERNAME.equals(command.getUsername())) {
            throw new IllegalArgumentException("Invalid development username");
        }
        if (!Strings.hasText(command.getPassword()) || !Strings.hasText(command.getCode())) {
            throw new IllegalArgumentException("Password and captcha code are required");
        }

        LoginResult result = new LoginResult();
        result.setUid(DEV_UID);
        result.setUsername(DEV_USERNAME);
        result.setUserCategory("admin");
        result.setToken(DEV_TOKEN);
        result.setExpiresAt(4102444800000L);
        result.setIsUpdatePassword(true);
        result.setOrgs(Collections.singletonList(DEFAULT_ORG));
        result.setOrgPermission(orgPermission());
        result.setCustom(platformConfig());
        return result;
    }

    @Override
    public PermissionResult permission(String token) {
        PermissionResult result = new PermissionResult();
        result.setOrgPermission(orgPermission());
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

    private Map<String, Object> orgPermission() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("org", org(DEFAULT_ORG));
        result.put("permissions", Arrays.asList(permissionItem("app"), permissionItem("app.agent")));
        result.put("roles", Collections.singletonList("admin"));
        result.put("isAdmin", true);
        result.put("isSystem", true);
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

    private Map<String, Object> singletonMap(String key, Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(key, value);
        return result;
    }
}
