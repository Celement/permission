package com.pp.authority.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pp.authority.entity.User;
import com.pp.authority.entity.UserRole;

import java.util.List;

public interface UserRoleService  extends IService<UserRole> {
    List<Long> getRoleIds(Object loginId, String loginType);
}
