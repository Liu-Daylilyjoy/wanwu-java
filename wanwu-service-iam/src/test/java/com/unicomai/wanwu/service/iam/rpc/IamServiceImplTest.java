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
    public void loginReturnsAdminSessionWithImplementedFrontendPermissions() {
        LoginResult result = service.login(new LoginCommand("admin", "encrypted", "dev-captcha", "1234"));

        assertEquals("dev-admin", result.getUid());
        assertEquals("admin", result.getUsername());
        assertEquals("dev-token", result.getToken());
        assertTrue(result.getIsUpdatePassword());
        assertEquals("default-org", ((Map) result.getOrgPermission().get("org")).get("id"));
        List<String> permissions = permissions(result.getOrgPermission());
        assertTrue(permissions.contains("app"));
        assertTrue(permissions.contains("app.agent"));
        assertTrue(permissions.contains("api_key"));
        assertTrue(permissions.contains("api_key.api_key_management"));
        assertTrue(permissions.contains("permission"));
        assertTrue(permissions.contains("permission.user"));
        assertTrue(permissions.contains("permission.org"));
        assertTrue(permissions.contains("permission.role"));
        assertTrue(permissions.contains("model"));
        assertTrue(permissions.contains("model.model_management"));
        assertTrue(permissions.contains("resource.knowledge"));
        assertFalse(permissions.contains("ontology"));
        assertFalse(permissions.contains("ontology.knowledge_network"));
        assertFalse(permissions.contains("ontology.data_source"));
        assertEquals(11, permissions.size());
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
        assertEquals(java.util.Arrays.asList(
                "permission", "permission.user", "permission.org", "permission.role",
                "model", "model.model_management",
                "app", "app.agent", "api_key", "api_key.api_key_management",
                "resource.knowledge"),
                permissions(result.getOrgPermission()));

        PermissionResult appResult = service.permission("dev-token-app");
        assertEquals(java.util.Arrays.asList("app", "app.agent"), permissions(appResult.getOrgPermission()));
    }

    @Test
    public void selectOrganizationsReturnsDefaultOrganization() {
        OrganizationSelectResult result = service.selectOrganizations();

        assertEquals(1, result.getSelect().size());
        assertEquals("default-org", result.getSelect().get(0).getId());
    }

    @Test
    public void permissionManagementReadModelsFollowFrontendContract() {
        Map<String, Object> users = service.listUsers("default-org", "", 1, 10);
        assertEquals(2L, users.get("total"));
        assertEquals(1, users.get("pageNo"));
        Map firstUser = (Map) ((List) users.get("list")).get(0);
        assertEquals("dev-admin", firstUser.get("userId"));
        assertEquals("admin", firstUser.get("username"));
        assertEquals(true, firstUser.get("status"));
        assertEquals("Default Organization", ((Map) ((Map) ((List) firstUser.get("orgs")).get(0)).get("org")).get("name"));

        Map<String, Object> roleSelect = service.selectRoles("default-org");
        assertEquals("admin", ((Map) ((List) roleSelect.get("select")).get(0)).get("id"));

        Map<String, Object> roles = service.listRoles("dev-admin", "default-org", "", 1, 10);
        Map firstRole = (Map) ((List) roles.get("list")).get(0);
        assertEquals("admin", firstRole.get("roleId"));
        assertEquals(true, firstRole.get("isAdmin"));
        assertEquals("permission", ((Map) ((List) firstRole.get("permissions")).get(0)).get("perm"));

        Map<String, Object> template = service.roleTemplate("dev-admin", "default-org");
        Map firstRoute = (Map) ((List) template.get("routes")).get(0);
        assertEquals("permission", firstRoute.get("perm"));
        assertEquals(3, ((List) firstRoute.get("children")).size());

        Map<String, Object> orgs = service.listOrganizations("default-org", "", 1, 10);
        assertEquals(1L, orgs.get("total"));
        assertEquals("default-org", ((Map) ((List) orgs.get("list")).get(0)).get("orgId"));
    }

    private List<String> permissions(Map<String, Object> orgPermission) {
        return ((List<Map<String, Object>>) orgPermission.get("permissions")).stream()
                .map(item -> (String) item.get("perm"))
                .collect(Collectors.toList());
    }
}
