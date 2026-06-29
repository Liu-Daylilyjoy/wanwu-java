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

    Map<String, Object> platformConfig();
}
