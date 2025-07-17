package com.pp.authority.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pp.authority.annotations.ClearPerms;
import com.pp.authority.annotations.IgnoreParam;
import com.pp.authority.annotations.IgoreResult;
import com.pp.authority.common.Constant;
import com.pp.authority.common.Result;
import com.pp.authority.entity.User;
import com.pp.authority.entity.UserRole;
import com.pp.authority.entity.dto.LoginDto;
import com.pp.authority.entity.dto.MailBean;
import com.pp.authority.entity.dto.RegisterDto;
import com.pp.authority.exception.EmailException;
import com.pp.authority.service.PermissionServcice;
import com.pp.authority.service.UserRoleService;
import com.pp.authority.service.UserService;
import com.pp.authority.service.impl.UserRoleServiceImpl;
import com.pp.authority.utils.MailUtil;
import com.pp.authority.utils.RedisUtils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Permissions;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
    private MailUtil mailUtil;

    @Resource

    private PermissionServcice permissionServcice;
    @Autowired
    private UserRoleService userRoleService;

    /**
     * 登录接口
     * @param loginDto
     * @return SaResult
     */
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

    /**
     * 验证码接口
     * @return Result
     */
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
        redisUtils.hset(Constant.CAPTCHA_PREFIX,key,shearCaptcha.getCode(),1200);
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

    /**
     *  判断会话是否登录
      * @return String
     */
    @GetMapping("isLogin")
    @ClearPerms
    public String  isLogin(){
        User user=userService.getUserInfo();
        return  "当前会话是否登录:"+StpUtil.isLogin();
    }

    /**
     * token信息
     * @return SaResult
     */
    @GetMapping("/tokenInfo")
    @ClearPerms(value = true)
    public SaResult tokenInfo(){
        return SaResult.data(StpUtil.getTokenInfo());
    }

    /*
     * 权限验证
     * @return SaResult
     */
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

    /**
     * 退出登录
     * @return
     */
    @GetMapping("/logout")
    @ClearPerms(value = true)
    public SaResult loginOut(){
       StpUtil.logout();
       return SaResult.ok();
    }

    /**
     * 发送邮箱验证码
     *
     */
    @GetMapping("/email/{username}")
    public Result getEmailCode(@PathVariable String  username){
        //查询数据库
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        //找用户名是否存在
        queryWrapper.eq(User::getUsername,username);
        User user = userService.getOne(queryWrapper);
        //
        if (null==user){
            throw new EmailException("账户名错误:请你核对一下重新输入");
        }
        //查询到了用户但是没有绑定邮箱抛出异常提示
        if ("".equals(user.getEmail())){
            throw new EmailException("邮箱为空");
        }

        //判断发送验证码的时间发送过就不发送了
        if (redisUtils.hget(Constant.EMAIL_PREFIX,user.getUsername())!=null){
            throw new EmailException("验证码以及发送到你的邮箱了不要重复发送");
        }
        //生成随机的数字
        String code = RandomUtil.randomNumbers(5);
        MailBean mailBean = new MailBean();
        mailBean.setRecipient(user.getEmail());
        mailBean.setSubject("找回密码");
        mailBean.setContent("重置邮箱验证码为:"+code+"请在五分钟内使用不要泄露"+ DateUtil.now());
        mailUtil.sendSimpleMail(mailBean);

        redisUtils.hset(Constant.EMAIL_PREFIX,user.getUsername(),code,300);
        return Result.success();
    }
    /**
     * 注册用户
     */
    @PostMapping("/register")
    public Result register(@RequestBody RegisterDto  registerDto){
        log.info("注册用户");
        log.info(registerDto.getCode());
        log.info(registerDto.getToken());
        //通过传递过来的token
        Object hget = redisUtils.hget(Constant.CAPTCHA_PREFIX, registerDto.getToken());
        log.info(hget.toString());
        String code=(String)redisUtils.hget(Constant.CAPTCHA_PREFIX,registerDto.getToken());
        log.info("redis");
        log.info(code);
        //删除掉redis的记录
        redisUtils.hdel(Constant.CAPTCHA_PREFIX,registerDto.getToken());
        if (!code.equals(registerDto.getCode())){
            return Result.error("验证码错误");
        }
        //构造条件基于username查询
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername,registerDto.getUsername());

        List<User> users = userService.list(queryWrapper);

        //判断一下拥有该名字的用户是否大于0大于0就提醒用户更换
        if (users.size()>0){
            return Result.error("账户名已经存在,请更换");
        }
        //在根据邮箱进行查询
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        List<User> userList = userService.list(lambdaQueryWrapper.eq(User::getEmail, registerDto.getEmail()));
        //查询邮箱记录
        if (userList.size()>0){
            throw new EmailException("邮箱已经被其他用户占用,请更换");
        }else {
            //进行保存
           User  user
                   = new User().setPassword(passwordEncoder.encode(registerDto.getPassword()))
                    .setAvatar(registerDto.getAvatar())
                    .setNickname(registerDto.getNickname())
                   .setUsername(registerDto.getUsername())
                    .setPhone(registerDto.getPhone())
                    .setEmail(registerDto.getEmail())
                    .setStatu(1l);
           userService.saveOrUpdate(user);
           //对用户关联的表页要保存记录
            UserRole userRole = new UserRole().setRoleId(Constant.DEFAULT_ROLE)
                    .setUserId(user.getId());

            userRoleService.saveOrUpdate(userRole);

        }

        return Result.success();

    }


    /**
     * 修改密码的功能
     */

    @PostMapping("/repassword")
    @ClearPerms(value = true)
    public Result repassword(@RequestBody HashMap<String,String> passwordMap){
        //获取当前的用户
        User user = userService.getUserInfo();
        //和原密码进行校验
        if (!passwordEncoder.matches(passwordMap.get("oldpass"),user.getPassword())){
            return  Result.error("原始密码错误");
        }
        //判断两次输入密码是否一致
        if (!passwordMap.get("password").equals(passwordMap.get("checkPass"))){
            return Result.error("两次密码输入不一致");
        }
        //重置密码
        user.setPassword(passwordEncoder.encode(passwordMap.get("password")));
        //保存返回
        return Result.success(userService.saveOrUpdate(user));
    }


}
