package com.pp.authority.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pp.authority.entity.User;

public interface UserService extends IService<User> {

    User getUserInfo();
}
