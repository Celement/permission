package com.pp.authority.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pp.authority.annotations.ClearPerms;
import com.pp.authority.common.Result;
import com.pp.authority.entity.Role;
import com.pp.authority.entity.RolePermission;
import com.pp.authority.service.RolePermissionService;
import com.pp.authority.service.RoleService;
import com.pp.authority.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/role")
@RestController
public class RoleController {


    @Resource
    private RoleService  roleService;
    @Resource
    private RolePermissionService  rolePermissionService;

    @Resource
    private UserService  userService;

    @PostMapping
    @ClearPerms
    public Result save(@RequestBody Role role){
        roleService.save(role);
        //新增角色对应的权限
        this.saveBatchRolePermissions(role);

        return Result.success();
    }

    private void saveBatchRolePermissions(Role role) {
        role.getPermissionIds().forEach(pid->{
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(role.getId());
            rolePermission.setPermissionId(pid);
            rolePermissionService.save(rolePermission);
        });
    }


    @PutMapping("/{id}")
    @ClearPerms
    public Result update(@PathVariable Long id,@RequestBody Role role){
        rolePermissionService.remove(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId,id));
        saveBatchRolePermissions(role);
        return Result.success(roleService.updateById(role));
    }

}
