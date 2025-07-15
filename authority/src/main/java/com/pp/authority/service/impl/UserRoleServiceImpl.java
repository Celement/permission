package com.pp.authority.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pp.authority.entity.User;
import com.pp.authority.entity.UserRole;
import com.pp.authority.mapper.UserMapper;
import com.pp.authority.mapper.UserRoleMapper;
import com.pp.authority.service.UserRoleService;
import com.pp.authority.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

    @Override
    public List<Long> getRoleIds(Object loginId, String loginType) {

        LambdaQueryWrapper<UserRole> userRoleLambdaQueryWrapper = new LambdaQueryWrapper<UserRole>().in(UserRole::getUserId, loginId);
        //将userRoleLambdaQueryWrapper转换成list
        return this.list(userRoleLambdaQueryWrapper).stream().map(UserRole::getRoleId).collect(Collectors.toList());
    }
}
