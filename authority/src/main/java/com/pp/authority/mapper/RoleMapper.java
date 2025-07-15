package com.pp.authority.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pp.authority.entity.Role;
import com.pp.authority.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMapper   extends BaseMapper<Role> {
}
