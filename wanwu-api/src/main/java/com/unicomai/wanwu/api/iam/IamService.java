package com.unicomai.wanwu.api.iam;

import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.api.iam.dto.CaptchaResult;
import com.unicomai.wanwu.api.iam.dto.LoginCommand;
import com.unicomai.wanwu.api.iam.dto.LoginResult;
import com.unicomai.wanwu.api.iam.dto.OrganizationSelectResult;
import com.unicomai.wanwu.api.iam.dto.PermissionResult;

import java.util.Map;

public interface IamService {

    ServiceDescriptor describe();

    CaptchaResult captcha();

    LoginResult login(LoginCommand command);

    PermissionResult permission(String token);

    OrganizationSelectResult selectOrganizations();

    Map<String, Object> listUsers(String orgId, String name, int pageNo, int pageSize);

    Map<String, Object> selectRoles(String orgId);

    Map<String, Object> roleTemplate(String userId, String orgId);

    Map<String, Object> listRoles(String userId, String orgId, String name, int pageNo, int pageSize);

    Map<String, Object> roleInfo(String userId, String orgId, String roleId);

    Map<String, Object> listOrganizations(String parentId, String name, int pageNo, int pageSize);

    Map<String, Object> organizationInfo(String orgId);

    Map<String, Object> createOauthApp(String userId, Map<String, Object> request);

    void updateOauthApp(Map<String, Object> request);

    void deleteOauthApp(Map<String, Object> request);

    void updateOauthAppStatus(Map<String, Object> request);

    Map<String, Object> listOauthApps(String userId, String name, int pageNo, int pageSize);

    void updateCustomTab(Map<String, Object> request);

    void updateCustomLogin(Map<String, Object> request);

    void updateCustomHome(Map<String, Object> request);

    Map<String, Object> platformConfig();
}
