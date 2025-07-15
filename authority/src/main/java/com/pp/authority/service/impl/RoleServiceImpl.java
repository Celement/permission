package com.pp.authority.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pp.authority.entity.Permission;
import com.pp.authority.entity.Role;
import com.pp.authority.mapper.PermissionMapper;
import com.pp.authority.mapper.RoleMapper;
import com.pp.authority.service.PermissionServcice;
import com.pp.authority.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RoleServiceImpl  extends ServiceImpl<RoleMapper, Role> implements RoleService {


}
