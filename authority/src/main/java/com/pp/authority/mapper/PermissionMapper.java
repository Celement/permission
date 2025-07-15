package com.pp.authority.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pp.authority.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PermissionMapper  extends BaseMapper<Permission> {
}
