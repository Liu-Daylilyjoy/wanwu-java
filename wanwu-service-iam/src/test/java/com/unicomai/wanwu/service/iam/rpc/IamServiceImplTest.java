package com.unicomai.wanwu.service.iam.rpc;

import com.unicomai.wanwu.api.iam.dto.CaptchaResult;
import com.unicomai.wanwu.api.iam.dto.LoginCommand;
import com.unicomai.wanwu.api.iam.dto.LoginResult;
import com.unicomai.wanwu.api.iam.dto.OrganizationSelectResult;
import com.unicomai.wanwu.api.iam.dto.PermissionResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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
    public void loginReturnsAdminSessionWithAppAgentPermission() {
        LoginResult result = service.login(new LoginCommand("admin", "encrypted", "dev-captcha", "1234"));

        assertEquals("dev-admin", result.getUid());
        assertEquals("admin", result.getUsername());
        assertEquals("dev-token", result.getToken());
        assertTrue(result.getIsUpdatePassword());
        assertEquals("default-org", ((Map) result.getOrgPermission().get("org")).get("id"));
        assertEquals("app", ((Map) ((List) result.getOrgPermission().get("permissions")).get(0)).get("perm"));
        assertEquals("app.agent", ((Map) ((List) result.getOrgPermission().get("permissions")).get(1)).get("perm"));
        assertFalse((Boolean) ((Map) ((Map) result.getCustom().get("loginEmail")).get("email")).get("status"));
    }

    @Test
    public void permissionReturnsAvatarAndCurrentOrgPermissions() {
        PermissionResult result = service.permission("dev-token");

        assertEquals("", ((Map) result.getAvatar()).get("path"));
        assertTrue(result.getIsUpdatePassword());
        assertEquals("default-org", ((Map) result.getOrgPermission().get("org")).get("id"));
    }

    @Test
    public void selectOrganizationsReturnsDefaultOrganization() {
        OrganizationSelectResult result = service.selectOrganizations();

        assertEquals(1, result.getSelect().size());
        assertEquals("default-org", result.getSelect().get(0).getId());
    }
}
