package com.pp.authority.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pp.authority.common.Constant;
import com.pp.authority.entity.User;
import com.pp.authority.mapper.UserMapper;
import com.pp.authority.service.UserService;
import com.pp.authority.utils.RedisUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService {
    @Resource
    private RedisUtils  redisUtils;

    @Override
    public User getUserInfo() {
        if (ObjectUtil.isNotEmpty(redisUtils.hget(Constant.ONLINE_PREFIX,String.valueOf(StpUtil.getLoginId())))){
            log.info("从redis中获取在线的用户缓存信息");
            return (User) redisUtils.hget(Constant.ONLINE_PREFIX,String.valueOf(StpUtil.getLoginId()));
        }else {
            User onlineUser = this.getById((Serializable) StpUtil.getLoginId());
            redisUtils.hset(Constant.ONLINE_PREFIX,String.valueOf(StpUtil.getLoginId()),onlineUser);
            return onlineUser;
        }
    }
}
