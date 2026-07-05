package com.unicomai.wanwu.api.perm;

import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.api.perm.dto.CheckUserEnableCommand;
import com.unicomai.wanwu.api.perm.dto.CheckUserEnableResult;
import com.unicomai.wanwu.api.perm.dto.CheckUserPermCommand;
import com.unicomai.wanwu.api.perm.dto.CheckUserPermResult;

public interface PermService {

    ServiceDescriptor describe();

    CheckUserEnableResult checkUserEnable(CheckUserEnableCommand command);

    CheckUserPermResult checkUserPerm(CheckUserPermCommand command);
}
