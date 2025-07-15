package com.pp.authority.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pp.authority.entity.User;
import com.pp.authority.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRoleMapper  extends BaseMapper<UserRole> {
}
