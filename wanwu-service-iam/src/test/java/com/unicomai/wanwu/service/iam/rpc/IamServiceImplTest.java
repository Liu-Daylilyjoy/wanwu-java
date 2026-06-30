package com.unicomai.wanwu.service.iam.rpc;

import com.unicomai.wanwu.api.iam.dto.CaptchaResult;
import com.unicomai.wanwu.api.iam.dto.LoginCommand;
import com.unicomai.wanwu.api.iam.dto.LoginResult;
import com.unicomai.wanwu.api.iam.dto.OrganizationSelectResult;
import com.unicomai.wanwu.api.iam.dto.PermissionResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IamServiceImplTest {

    private final IamServiceImpl service = new IamServiceImpl();

    @Test
    public void captchaReturnsStaticDevelopmentImage() {
        CaptchaResult result = service.captcha();

        assertEquals("dev-captcha", result.getKey());
        assertTrue(result.getB64().startsWith("data:image/svg+xml;base64,"));
    }

    @Test
    public void loginReturnsAdminSessionWithAllFrontendPermissions() {
        LoginResult result = service.login(new LoginCommand("admin", "encrypted", "dev-captcha", "1234"));

        assertEquals("dev-admin", result.getUid());
        assertEquals("admin", result.getUsername());
        assertEquals("dev-token", result.getToken());
        assertTrue(result.getIsUpdatePassword());
        assertEquals("default-org", ((Map) result.getOrgPermission().get("org")).get("id"));
        List<String> permissions = permissions(result.getOrgPermission());
        assertTrue(permissions.contains("permission"));
        assertTrue(permissions.contains("model.model_management"));
        assertTrue(permissions.contains("resource.knowledge"));
        assertTrue(permissions.contains("app.agent"));
        assertTrue(permissions.contains("api_key.api_key_management"));
        assertEquals(36, permissions.size());
        assertFalse((Boolean) ((Map) ((Map) result.getCustom().get("loginEmail")).get("email")).get("status"));
    }

    @Test
    public void loginReturnsAppSessionWithOnlyAgentPermissions() {
        LoginResult result = service.login(new LoginCommand("app", "encrypted", "dev-captcha", "1234"));

        assertEquals("dev-app", result.getUid());
        assertEquals("app", result.getUsername());
        assertEquals("dev-token-app", result.getToken());
        assertEquals("user", result.getUserCategory());
        assertFalse((Boolean) result.getOrgPermission().get("isAdmin"));
        assertEquals(java.util.Arrays.asList("app", "app.agent"), permissions(result.getOrgPermission()));
    }

    @Test
    public void permissionReturnsAvatarAndTokenSpecificOrgPermissions() {
        PermissionResult result = service.permission("dev-token");

        assertEquals("", ((Map) result.getAvatar()).get("path"));
        assertTrue(result.getIsUpdatePassword());
        assertEquals("default-org", ((Map) result.getOrgPermission().get("org")).get("id"));
        assertTrue(permissions(result.getOrgPermission()).contains("permission.role"));

        PermissionResult appResult = service.permission("dev-token-app");
        assertEquals(java.util.Arrays.asList("app", "app.agent"), permissions(appResult.getOrgPermission()));
    }

    @Test
    public void selectOrganizationsReturnsDefaultOrganization() {
        OrganizationSelectResult result = service.selectOrganizations();

        assertEquals(1, result.getSelect().size());
        assertEquals("default-org", result.getSelect().get(0).getId());
    }

    private List<String> permissions(Map<String, Object> orgPermission) {
        return ((List<Map<String, Object>>) orgPermission.get("permissions")).stream()
                .map(item -> (String) item.get("perm"))
                .collect(Collectors.toList());
    }
}
