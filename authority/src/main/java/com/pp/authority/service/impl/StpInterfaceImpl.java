package com.pp.authority.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pp.authority.common.Constant;
import com.pp.authority.entity.Role;
import com.pp.authority.service.PermissionServcice;
import com.pp.authority.service.RolePermissionService;
import com.pp.authority.service.RoleService;
import com.pp.authority.service.UserRoleService;
import com.pp.authority.service.UserService;
import com.pp.authority.utils.RedisUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
@Slf4j
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private UserRoleService userRoleService;
    @Resource
    private UserService userService;
    @Resource
    private RoleService roleService;
    @Resource
    private PermissionServcice permissionServcice;

    @Resource
    private RolePermissionService rolePermissionService;

    @Resource
    private   RedisUtils redisUtils;
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        //先从redis内存数据库中取数据
//        if (ObjectUtil.isNotEmpty(redisUtils.hget(Constant.PERMISSION_PREFIX,String.valueOf(loginId)))){
//           log.info("redis中获取到了数据");
//           return (List<String>)redisUtils.hget(Constant.PERMISSION_PREFIX,String.valueOf(loginId));
//        }
        return null;
    }

    /**
     * 返回一个账号的角色标识集合
     * @param loginId
     * @param loginType
     * @return
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        //redis中取数据
        if (ObjectUtil.isNotEmpty(redisUtils.hget(Constant.ROLE_PREFIX,loginId.toString()))){
            log.info("从redis中获取到了数据");
            return   (List<String>) redisUtils.hget(Constant.ROLE_PREFIX,loginId.toString());
        }
        //从数据库中获取数据然后存储到redis中
        List<Long> userRoleIds = userRoleService.getRoleIds(loginId, loginType);
        if (userRoleIds.size()==0){
             return new ArrayList<>();
        }
        List<String> rolePermsList=roleService.list(new LambdaQueryWrapper<Role>()
                .in(Role::getId,userRoleIds)
                .eq(Role::getStatu,1l))
                .stream().map(Role::getPerms)
                .collect(Collectors.toList());

        if (ObjectUtil.isNotEmpty(rolePermsList)){
            redisUtils.hset(Constant.ROLE_PREFIX,(String) loginId,rolePermsList,3000000);
        }
            return rolePermsList;
        }




    }

