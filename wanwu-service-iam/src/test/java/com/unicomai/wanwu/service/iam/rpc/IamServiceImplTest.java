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
        assertTrue(permissions.contains("app.rag"));
        assertTrue(permissions.contains("app.workflow"));
        assertTrue(permissions.contains("app.agent"));
        assertTrue(permissions.contains("api_key"));
        assertTrue(permissions.contains("api_key.api_key_management"));
        assertTrue(permissions.contains("permission"));
        assertTrue(permissions.contains("permission.user"));
        assertTrue(permissions.contains("permission.org"));
        assertTrue(permissions.contains("permission.role"));
        assertTrue(permissions.contains("setting"));
        assertTrue(permissions.contains("model"));
        assertTrue(permissions.contains("model.model_management"));
        assertTrue(permissions.contains("resource"));
        assertTrue(permissions.contains("resource.knowledge"));
        assertTrue(permissions.contains("resource.tool"));
        assertTrue(permissions.contains("resource.mcp"));
        assertTrue(permissions.contains("resource.prompt"));
        assertTrue(permissions.contains("resource.skill"));
        assertTrue(permissions.contains("resource.safety"));
        assertTrue(permissions.contains("operation"));
        assertTrue(permissions.contains("operation.oauth"));
        assertTrue(permissions.contains("operation.statistic_client"));
        assertTrue(permissions.contains("exploration"));
        assertTrue(permissions.contains("exploration.app"));
        assertTrue(permissions.contains("exploration.mcp"));
        assertTrue(permissions.contains("exploration.template"));
        assertTrue(permissions.contains("exploration.skill"));
        assertTrue(permissions.contains("app_observability"));
        assertTrue(permissions.contains("app_observability.statistic"));
        assertFalse(permissions.contains("ontology"));
        assertFalse(permissions.contains("ontology.knowledge_network"));
        assertFalse(permissions.contains("ontology.data_source"));
        assertEquals(30, permissions.size());
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
        assertEquals(java.util.Arrays.asList("app", "app.rag", "app.workflow", "app.agent"), permissions(result.getOrgPermission()));
    }

    @Test
    public void permissionReturnsAvatarAndTokenSpecificOrgPermissions() {
        PermissionResult result = service.permission("dev-token");

        assertEquals("", ((Map) result.getAvatar()).get("path"));
        assertTrue(result.getIsUpdatePassword());
        assertEquals("default-org", ((Map) result.getOrgPermission().get("org")).get("id"));
        assertEquals(java.util.Arrays.asList(
                "permission", "permission.user", "permission.org", "permission.role",
                "setting",
                "model", "model.model_management",
                "app", "app.rag", "app.workflow", "app.agent", "api_key", "api_key.api_key_management",
                "resource", "resource.knowledge", "resource.tool", "resource.mcp", "resource.prompt",
                "resource.skill", "resource.safety",
                "operation", "operation.oauth", "operation.statistic_client",
                "exploration", "exploration.app", "exploration.mcp", "exploration.template", "exploration.skill",
                "app_observability", "app_observability.statistic"),
                permissions(result.getOrgPermission()));

        PermissionResult appResult = service.permission("dev-token-app");
        assertEquals(java.util.Arrays.asList("app", "app.rag", "app.workflow", "app.agent"), permissions(appResult.getOrgPermission()));
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
        Map appRoute = (Map) ((List) template.get("routes")).get(2);
        assertEquals("app", appRoute.get("perm"));
        assertEquals(3, ((List) appRoute.get("children")).size());
        assertEquals("app.rag", ((Map) ((List) appRoute.get("children")).get(0)).get("perm"));
        assertEquals("app.workflow", ((Map) ((List) appRoute.get("children")).get(1)).get("perm"));
        Map resourceRoute = (Map) ((List) template.get("routes")).get(4);
        assertEquals("resource", resourceRoute.get("perm"));
        assertEquals(6, ((List) resourceRoute.get("children")).size());
        assertEquals("resource.knowledge", ((Map) ((List) resourceRoute.get("children")).get(0)).get("perm"));
        assertEquals("resource.tool", ((Map) ((List) resourceRoute.get("children")).get(1)).get("perm"));
        assertEquals("resource.skill", ((Map) ((List) resourceRoute.get("children")).get(4)).get("perm"));
        assertEquals("resource.safety", ((Map) ((List) resourceRoute.get("children")).get(5)).get("perm"));
        Map settingRoute = (Map) ((List) template.get("routes")).get(5);
        assertEquals("setting", settingRoute.get("perm"));
        Map operationRoute = (Map) ((List) template.get("routes")).get(6);
        assertEquals("operation", operationRoute.get("perm"));
        assertEquals(2, ((List) operationRoute.get("children")).size());
        assertEquals("operation.oauth", ((Map) ((List) operationRoute.get("children")).get(0)).get("perm"));
        assertEquals("operation.statistic_client", ((Map) ((List) operationRoute.get("children")).get(1)).get("perm"));
        Map explorationRoute = (Map) ((List) template.get("routes")).get(7);
        assertEquals("exploration", explorationRoute.get("perm"));
        assertEquals(4, ((List) explorationRoute.get("children")).size());
        assertEquals("exploration.app", ((Map) ((List) explorationRoute.get("children")).get(0)).get("perm"));
        assertEquals("exploration.skill", ((Map) ((List) explorationRoute.get("children")).get(3)).get("perm"));
        Map statisticRoute = (Map) ((List) template.get("routes")).get(8);
        assertEquals("app_observability", statisticRoute.get("perm"));
        assertEquals(1, ((List) statisticRoute.get("children")).size());
        assertEquals("app_observability.statistic", ((Map) ((List) statisticRoute.get("children")).get(0)).get("perm"));

        Map<String, Object> orgs = service.listOrganizations("default-org", "", 1, 10);
        assertEquals(1L, orgs.get("total"));
        assertEquals("default-org", ((Map) ((List) orgs.get("list")).get(0)).get("orgId"));
    }

    @Test
    public void platformCustomConfigCanBeUpdatedAndReadBack() {
        service.updateCustomTab(map("tabTitle", "Smoke Tab", "tabLogo", map("path", "/tab.png", "key", "tab.png")));
        service.updateCustomLogin(map("loginBg", map("path", "/bg.png", "key", "bg.png"),
                "loginLogo", map("path", "/logo.png", "key", "logo.png"),
                "loginWelcomeText", "Welcome",
                "loginButtonColor", "#111111"));
        service.updateCustomHome(map("homeName", "Smoke Home",
                "homeLogo", map("path", "/home.png", "key", "home.png"),
                "homeBgColor", "#ffffff"));

        Map<String, Object> config = service.platformConfig();
        assertEquals("Smoke Tab", ((Map) config.get("tab")).get("title"));
        assertEquals("/tab.png", ((Map) ((Map) config.get("tab")).get("logo")).get("path"));
        assertEquals("Welcome", ((Map) config.get("login")).get("welcomeText"));
        assertEquals("#111111", ((Map) config.get("login")).get("loginButtonColor"));
        assertEquals("/logo.png", ((Map) ((Map) config.get("login")).get("logo")).get("path"));
        assertEquals("Smoke Home", ((Map) config.get("home")).get("title"));
        assertEquals("#ffffff", ((Map) config.get("home")).get("backgroundColor"));
    }

    @Test
    public void oauthAppsCanBeCreatedListedUpdatedDisabledAndDeleted() {
        Map<String, Object> created = service.createOauthApp("dev-admin",
                map("name", "Console", "desc", "dev oauth", "redirectUri", "http://localhost/callback"));
        String clientId = (String) created.get("clientId");

        Map<String, Object> page = service.listOauthApps("dev-admin", "Console", 1, 10);
        assertEquals(1L, page.get("total"));
        Map first = (Map) ((List) page.get("list")).get(0);
        assertEquals(clientId, first.get("clientId"));
        assertEquals("oauth-secret-1", first.get("clientSecret"));
        assertEquals(true, first.get("status"));

        service.updateOauthApp(map("clientId", clientId,
                "name", "Console 2",
                "desc", "updated",
                "redirectUri", "http://localhost/callback2"));
        service.updateOauthAppStatus(map("clientId", clientId, "status", false));
        Map updatedPage = service.listOauthApps("dev-admin", "Console 2", 1, 10);
        Map updated = (Map) ((List) updatedPage.get("list")).get(0);
        assertEquals("Console 2", updated.get("name"));
        assertEquals("http://localhost/callback2", updated.get("redirectUri"));
        assertEquals(false, updated.get("status"));

        service.deleteOauthApp(map("clientId", clientId));
        assertEquals(0L, service.listOauthApps("dev-admin", "", 1, 10).get("total"));
    }

    private List<String> permissions(Map<String, Object> orgPermission) {
        return ((List<Map<String, Object>>) orgPermission.get("permissions")).stream()
                .map(item -> (String) item.get("perm"))
                .collect(Collectors.toList());
    }

    private Map<String, Object> map(Object... pairs) {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            result.put(String.valueOf(pairs[i]), pairs[i + 1]);
        }
        return result;
    }
}
