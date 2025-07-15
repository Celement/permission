package com.pp.authority.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pp.authority.entity.Permission;

import java.util.List;

public interface PermissionServcice extends IService<Permission> {
    List<Permission> getRouters(Object loginId, String pc);

    List<Permission> buildTrees(List<Permission> permissions);
}
