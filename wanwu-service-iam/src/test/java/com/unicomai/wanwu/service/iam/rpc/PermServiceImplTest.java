package com.unicomai.wanwu.service.iam.rpc;

import com.unicomai.wanwu.api.perm.dto.CheckUserEnableCommand;
import com.unicomai.wanwu.api.perm.dto.CheckUserEnableResult;
import com.unicomai.wanwu.api.perm.dto.CheckUserPermCommand;
import com.unicomai.wanwu.api.perm.dto.CheckUserPermResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PermServiceImplTest {

    private IamServiceImpl iamService;
    private PermServiceImpl service;

    @BeforeEach
    public void setUp() {
        iamService = new IamServiceImpl();
        service = new PermServiceImpl(iamService);
    }

    @Test
    public void checkUserEnableReturnsLanguageAndPasswordTimestamp() {
        CheckUserEnableResult result = service.checkUserEnable(new CheckUserEnableCommand("dev-admin", "0"));

        assertEquals("zh", result.getLanguage());
        assertTrue(result.getLastUpdatePasswordAt() > 0L);
    }

    @Test
    public void checkUserPermHonorsAdminAndAppPermissionSets() {
        CheckUserPermResult admin = service.checkUserPerm(new CheckUserPermCommand(
                "dev-admin", "0", "default-org", Collections.singletonList("resource.knowledge")));

        assertTrue(admin.getIsAdmin());
        assertTrue(admin.getIsSystem());

        CheckUserPermResult app = service.checkUserPerm(new CheckUserPermCommand(
                "dev-app", "0", "default-org", Collections.singletonList("app.rag")));
        assertFalse(app.getIsAdmin());
        assertTrue(app.getIsSystem());

        IllegalArgumentException denied = assertThrows(IllegalArgumentException.class,
                () -> service.checkUserPerm(new CheckUserPermCommand(
                        "dev-app", "0", "default-org", Collections.singletonList("permission.user"))));
        assertEquals("user permission denied", denied.getMessage());
    }

    @Test
    public void checkUserPermUsesMutableRolePermissionsFromIamService() {
        Map<String, Object> createdRole = iamService.createRole("dev-admin", "default-org",
                map("name", "Knowledge Operator", "permissions", Arrays.asList("resource.knowledge", "app.rag")));
        String roleId = (String) createdRole.get("roleId");
        Map<String, Object> createdUser = iamService.createUser("dev-admin", "default-org",
                map("username", "permAlice", "nickname", "Perm Alice", "roleIds", Collections.singletonList(roleId)));
        String userId = (String) createdUser.get("userId");

        CheckUserPermResult allowed = service.checkUserPerm(new CheckUserPermCommand(
                userId, "0", "default-org", Collections.singletonList("resource.knowledge")));

        assertFalse(allowed.getIsAdmin());
        assertTrue(allowed.getIsSystem());
        assertThrows(IllegalArgumentException.class,
                () -> service.checkUserPerm(new CheckUserPermCommand(
                        userId, "0", "default-org", Collections.singletonList("permission.role"))));
    }

    @Test
    public void checkUserEnableRejectsDisabledUsers() {
        Map<String, Object> createdUser = iamService.createUser("dev-admin", "default-org",
                map("username", "disabledUser", "nickname", "Disabled User"));
        String userId = (String) createdUser.get("userId");
        iamService.updateUserStatus("dev-admin", "default-org", map("userId", userId, "status", false));

        IllegalArgumentException denied = assertThrows(IllegalArgumentException.class,
                () -> service.checkUserEnable(new CheckUserEnableCommand(userId, "0")));

        assertEquals("user disabled", denied.getMessage());
    }

    private Map<String, Object> map(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            result.put(String.valueOf(pairs[i]), pairs[i + 1]);
        }
        return result;
    }
}
