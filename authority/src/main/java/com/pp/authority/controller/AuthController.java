package com.pp.authority.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pp.authority.annotations.ClearPerms;
import com.pp.authority.annotations.IgnoreParam;
import com.pp.authority.annotations.IgoreResult;
import com.pp.authority.common.Constant;
import com.pp.authority.common.Result;
import com.pp.authority.entity.User;
import com.pp.authority.entity.dto.LoginDto;
import com.pp.authority.service.PermissionServcice;
import com.pp.authority.service.UserService;
import com.pp.authority.utils.MailUtil;
import com.pp.authority.utils.RedisUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Permissions;
import java.util.UUID;

@Tag(name = "认证鉴权")
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    @Autowired
    private UserService userService;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private RedisUtils redisUtils;

    @Resource

    private PermissionServcice permissionServcice;

    @PostMapping("/login")
    @IgnoreParam
    @IgoreResult
    public SaResult doLogin(@RequestBody LoginDto loginDto){
        //根据用户名查询数据库(没有做密码匹配)
        User queryUser = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, loginDto.getUsername()));
        //从redis中获取数据  验证码
        String code = String.valueOf(redisUtils.hget(Constant.CAPTCHA_PREFIX, loginDto.getToken()));
        redisUtils.hdel(Constant.CAPTCHA_PREFIX,loginDto.getToken());
        if (!code.equals(loginDto.getCode())){
            return SaResult.error("验证码错误");
        }
        //数据库中的密码是加密后的密码
        if (ObjectUtil.isEmpty(queryUser)||!passwordEncoder.matches(loginDto.getPassword(),queryUser.getPassword())){
            return SaResult.error("账户和密码错误");
        }
        if (queryUser.getStatu()==0l){
                return  SaResult.error("账户被封禁,请联系管理员");
        }
        //第一步我们先登录上
        StpUtil.login(queryUser.getId(),"PC");
        //第二步获取token
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        //第三步返回前端
        return SaResult.data(tokenInfo);

    }
    //验证码接口
    @GetMapping("/captcha")
    public Result getCaptcha(){

        //设置key
        String key = UUID.randomUUID().toString();
        //定义图形的长宽高验证码字符数干扰线宽度
        ShearCaptcha shearCaptcha = CaptchaUtil.createShearCaptcha(150, 40, 5, 0);
        //存入redis中设置过期时间
        String  base64String="";
        try {
            base64String = "data:image/png;base64," +shearCaptcha.getImageBase64();
        }catch (Exception e){
            e.printStackTrace();
        }
        log.info("获取验证码");
        log.info(shearCaptcha.getCode());
        log.info("token值:");
        log.info(key);
        redisUtils.hset(Constant.CAPTCHA_PREFIX,key,shearCaptcha.getCode(),120);
        return Result.success(
                MapUtil.builder()
                        .put("token",key)
                        .put("captcha",base64String)
                        .put("code",shearCaptcha.getCode())
                        .build()
        );
//        return Result.success(
//                MapUtil.builder()
//                        .put("token",key)
//                        .put("captcha",base64String)
//                        .build()
//        );
    }

    //查询用户登录状态
    @GetMapping("isLogin")
    @ClearPerms
    public String  isLogin(){
        User user=userService.getUserInfo();
        return  "当前会话是否登录:"+StpUtil.isLogin();
    }
    //查询token信息
    @GetMapping("/tokenInfo")
    @ClearPerms(value = true)
    public SaResult tokenInfo(){
        return SaResult.data(StpUtil.getTokenInfo());
    }
    //获取认证信息
    @GetMapping("/authInfo")
    public SaResult getPerms(){

        //第一步获取路由数据,将路由数据转换成Tree
        return SaResult.data(
                MapUtil.builder().put("rolePerms",StpUtil.getRoleList())
                        .put("permissionPerms",StpUtil.getPermissionList())
                        .put("userInfo",userService.getUserInfo())
                        .put("routers",permissionServcice.buildTrees(permissionServcice.getRouters(StpUtil.getLoginId(),"PC")))
                        .build()
        );
    }
}
