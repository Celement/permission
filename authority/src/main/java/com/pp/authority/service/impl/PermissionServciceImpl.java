package com.pp.authority.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pp.authority.common.Constant;
import com.pp.authority.entity.Permission;
import com.pp.authority.entity.Role;
import com.pp.authority.entity.RolePermission;
import com.pp.authority.entity.dto.Meta;
import com.pp.authority.mapper.PermissionMapper;
import com.pp.authority.service.PermissionServcice;
import com.pp.authority.service.RolePermissionService;
import com.pp.authority.service.RoleService;
import com.pp.authority.service.UserRoleService;
import com.pp.authority.utils.RedisUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PermissionServciceImpl extends ServiceImpl<PermissionMapper,Permission> implements PermissionServcice {
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private RolePermissionService rolePermissionService;
    @Resource
    private RedisUtils redisUtils;

    @Override
    public List<Permission> getRouters(Object loginId, String loginType) {
        //先去redis数据库中取对应的权限数据
        List<Permission>  routers=(List<Permission>)redisUtils.hget(Constant.ROUTERS_PREFIX,String.valueOf(loginId));
        if (ObjectUtil.isNotEmpty(routers)){
            return  routers;
        }else {
            //根据用户id 查询对应的角色(id集合)--->查询对应的权限--->(集合)--->前端可以使用的路由数据
            //role id的集合
            List<Long> roleIds=userRoleService.getRoleIds(loginId,loginType);
            //role集合
            if (roleIds.size()==0){
                return new ArrayList<>();   //空的
            }
            //查询到了role集合
            List<Role> roles = roleService.list(new LambdaQueryWrapper<Role>().in(Role::getId, roleIds).eq(Role::getStatu, 1l));
            //role的集合进行判断
            if (roles.size()==0){
                return new ArrayList<>();
            }
            //创建Permission对应的构造条件
            LambdaQueryWrapper<RolePermission> rolePermissionLambdaQueryWrapper = new LambdaQueryWrapper<RolePermission>().in(RolePermission::getRoleId, roles.stream().map(Role::getId).collect(Collectors.toList()));
            //基于构造条件获取当前用户角色权限关联集合(id集合)
            List<Long>  permissionIds= rolePermissionService.list(rolePermissionLambdaQueryWrapper).stream().map(RolePermission::getPermissionId).collect(Collectors.toList());
            if (permissionIds.size()==0){
                return new ArrayList<>();
            }
            /**
             * 基于权限id获取菜单数据
             */
            List<Permission> routerByDataSource=this.listByIds(permissionIds).stream().filter(permission -> permission.getStatu() == 1l && (permission.getStatu() == 1 && (permission.getMenuType() == 1 || permission.getMenuType() == 2))).distinct()
                    .sorted(Comparator.comparing(Permission::getOrderNum, Comparator.reverseOrder()).reversed()).collect(Collectors.toList());

            /**
             * 数据转换  vue3路由   path   title commonent meta
             *
             * */
            routerByDataSource.forEach(r->r.setMeta(new Meta(r.getIcon(),r.getTitle(),r.getHidden())));
            if (ObjectUtil.isNotEmpty(routerByDataSource)){
                redisUtils.hset(Constant.ROUTERS_PREFIX,(String) loginId,routerByDataSource);
            }

            return routerByDataSource;
        }
    }

    /**
     * 创建一个双重for循环构造树结构
     * @param permissions
     * @return
     */
    @Override
    public List<Permission> buildTrees(List<Permission> permissions) {
        List<Permission> finalyPermissions = new ArrayList<>();
        for (Permission firstPermission:permissions){
            //第二层
            for (Permission secondPermission:permissions){
                //第二层循环的Permission的父id等于第一层的id将第二层permissionid添加到 第一层 permission中的children集合里面
                if (secondPermission.getParentId().equals(firstPermission.getId())){
                    firstPermission.getChildren().add(secondPermission);
                }

            }
            //第一层的parentid=0第一层没有父菜单 将permission添加到初始创建的permission集合中
            if (firstPermission.getParentId()==0l){
                finalyPermissions.add(firstPermission);
            }
        }

        return finalyPermissions;
    }
}
