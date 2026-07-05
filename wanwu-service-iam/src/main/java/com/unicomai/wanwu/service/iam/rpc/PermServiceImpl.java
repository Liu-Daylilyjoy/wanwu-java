package com.unicomai.wanwu.service.iam.rpc;

import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.api.iam.IamService;
import com.unicomai.wanwu.api.iam.dto.PermissionResult;
import com.unicomai.wanwu.api.perm.PermService;
import com.unicomai.wanwu.api.perm.dto.CheckUserEnableCommand;
import com.unicomai.wanwu.api.perm.dto.CheckUserEnableResult;
import com.unicomai.wanwu.api.perm.dto.CheckUserPermCommand;
import com.unicomai.wanwu.api.perm.dto.CheckUserPermResult;
import com.unicomai.wanwu.common.core.model.ServiceNames;
import com.unicomai.wanwu.common.core.util.Strings;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

@DubboService(version = RpcConstants.VERSION, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
public class PermServiceImpl implements PermService {

    private static final String DEFAULT_ORG_ID = "default-org";
    private static final String ADMIN_USER_ID = "dev-admin";
    private static final String APP_USER_ID = "dev-app";
    private static final String ADMIN_TOKEN = "dev-token";
    private static final String APP_TOKEN = "dev-token-app";

    private final IamService iamService;

    @Autowired
    public PermServiceImpl(IamService iamService) {
        this.iamService = iamService;
    }

    PermServiceImpl(IamServiceImpl iamService) {
        this.iamService = iamService;
    }

    @Override
    public ServiceDescriptor describe() {
        return ServiceDescriptor.of(ServiceNames.IAM, "Permission Service", "perm");
    }

    @Override
    public CheckUserEnableResult checkUserEnable(CheckUserEnableCommand command) {
        Map<String, Object> user = checkedUser(command == null ? null : command.getUserId(),
                command == null ? null : command.getGenTokenAt());
        return new CheckUserEnableResult(languageCode(user), lastUpdatePasswordAt(user));
    }

    @Override
    public CheckUserPermResult checkUserPerm(CheckUserPermCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("permission command is required");
        }
        Map<String, Object> user = checkedUser(command.getUserId(), command.getGenTokenAt());
        String orgId = Strings.hasText(command.getOrgId()) ? command.getOrgId() : DEFAULT_ORG_ID;
        if (!belongsToOrg(user, orgId)) {
            throw new IllegalArgumentException("user org permission denied");
        }

        PermissionSnapshot snapshot = permissionSnapshot(user, orgId);
        List<String> oneOfPerms = command.getOneOfPerms() == null
                ? Collections.<String>emptyList() : command.getOneOfPerms();
        if (!snapshot.admin && !oneOfPerms.isEmpty() && !containsAny(snapshot.permissions, oneOfPerms)) {
            throw new IllegalArgumentException("user permission denied");
        }

        CheckUserPermResult result = new CheckUserPermResult();
        result.setIsAdmin(snapshot.admin);
        result.setIsSystem(DEFAULT_ORG_ID.equals(orgId));
        result.setLanguage(languageCode(user));
        result.setLastUpdatePasswordAt(lastUpdatePasswordAt(user));
        return result;
    }

    private Map<String, Object> checkedUser(String userId, String genTokenAt) {
        Map<String, Object> user = iamService.getUserInfo(Strings.hasText(userId) ? userId : ADMIN_USER_ID, DEFAULT_ORG_ID);
        if (!booleanValue(user.get("status"), true)) {
            throw new IllegalArgumentException("user disabled");
        }
        long tokenAt = longValue(genTokenAt);
        long updatedAt = lastUpdatePasswordAt(user);
        if (tokenAt > 0 && updatedAt > 0 && tokenAt < updatedAt) {
            throw new IllegalArgumentException("user token expired");
        }
        return user;
    }

    private PermissionSnapshot permissionSnapshot(Map<String, Object> user, String orgId) {
        String userId = stringValue(user.get("userId"));
        if (ADMIN_USER_ID.equals(userId) || APP_USER_ID.equals(userId)) {
            PermissionResult permission = iamService.permission(ADMIN_USER_ID.equals(userId) ? ADMIN_TOKEN : APP_TOKEN);
            Map<String, Object> orgPermission = permission == null ? Collections.<String, Object>emptyMap()
                    : mapValue(permission.getOrgPermission());
            return new PermissionSnapshot(booleanValue(orgPermission.get("isAdmin"), false),
                    permissionsFromItems(orgPermission.get("permissions")));
        }

        boolean admin = false;
        Set<String> permissions = new LinkedHashSet<String>();
        for (String roleId : roleIds(user, orgId)) {
            Map<String, Object> role = iamService.roleInfo(userId, orgId, roleId);
            if (!booleanValue(role.get("status"), true)) {
                continue;
            }
            admin = admin || booleanValue(role.get("isAdmin"), false);
            permissions.addAll(permissionsFromItems(role.get("permissions")));
        }
        return new PermissionSnapshot(admin, permissions);
    }

    private boolean belongsToOrg(Map<String, Object> user, String orgId) {
        return !roleIds(user, orgId).isEmpty();
    }

    private List<String> roleIds(Map<String, Object> user, String orgId) {
        List<String> result = new ArrayList<String>();
        Object orgs = user.get("orgs");
        if (!(orgs instanceof List)) {
            return result;
        }
        for (Object item : (List<?>) orgs) {
            Map<String, Object> orgRole = mapValue(item);
            Map<String, Object> org = mapValue(orgRole.get("org"));
            if (!orgId.equals(stringValue(org.get("id")))) {
                continue;
            }
            Object roles = orgRole.get("roles");
            if (roles instanceof List) {
                for (Object role : (List<?>) roles) {
                    String roleId = stringValue(mapValue(role).get("id"));
                    if (Strings.hasText(roleId)) {
                        result.add(roleId);
                    }
                }
            }
        }
        return result;
    }

    private Set<String> permissionsFromItems(Object value) {
        Set<String> result = new LinkedHashSet<String>();
        if (!(value instanceof List)) {
            return result;
        }
        for (Object item : (List<?>) value) {
            Map<String, Object> permission = mapValue(item);
            String perm = stringValue(permission.get("perm"));
            if (Strings.hasText(perm)) {
                result.add(perm);
            }
        }
        return result;
    }

    private boolean containsAny(Set<String> permissions, List<String> oneOfPerms) {
        for (String perm : oneOfPerms) {
            if (permissions.contains(perm)) {
                return true;
            }
        }
        return false;
    }

    private String languageCode(Map<String, Object> user) {
        Map<String, Object> language = mapValue(user.get("language"));
        String code = stringValue(language.get("code"));
        return Strings.hasText(code) ? code : "zh";
    }

    private long lastUpdatePasswordAt(Map<String, Object> user) {
        long changedAt = timeValue(user.get("passwordChangedAt"));
        if (changedAt > 0) {
            return changedAt;
        }
        long updatedAt = timeValue(user.get("updatedAt"));
        return updatedAt > 0 ? updatedAt : timeValue(user.get("createdAt"));
    }

    private long timeValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        String text = stringValue(value);
        if (!Strings.hasText(text)) {
            return 0L;
        }
        long numeric = longValue(text);
        if (numeric > 0) {
            return numeric;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            return format.parse(text).getTime();
        } catch (ParseException ignored) {
            return 0L;
        }
    }

    private long longValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapValue(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : Collections.<String, Object>emptyMap();
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private boolean booleanValue(Object value, boolean fallback) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value == null) {
            return fallback;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private static final class PermissionSnapshot {
        private final boolean admin;
        private final Set<String> permissions;

        private PermissionSnapshot(boolean admin, Set<String> permissions) {
            this.admin = admin;
            this.permissions = permissions;
        }
    }
}
