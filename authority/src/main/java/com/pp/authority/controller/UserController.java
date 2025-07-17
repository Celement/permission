package com.pp.authority.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.http.server.HttpServerResponse;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pp.authority.annotations.ClearPerms;
import com.pp.authority.common.Constant;
import com.pp.authority.common.Result;
import com.pp.authority.entity.User;
import com.pp.authority.entity.UserRole;
import com.pp.authority.service.UserRoleService;
import com.pp.authority.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private UserRoleService  userRoleService;

    /**
     * 新增
     */
    @PostMapping
    public Result save(@RequestBody User user){
        if (userService.exists(new LambdaQueryWrapper<User>().eq(User::getUsername,user.getUsername()))){
            return Result.error("用户名存在");
        }
        user.setPassword(passwordEncoder.encode(Constant.DEFAULT_PASSWORD));
        userService.save(user);
        //报错关联表
        this.saveBatchUserRoles(user);
        return Result.success();

    }

    private void  saveBatchUserRoles(User user){
        user.getRoleIds().forEach(rid->{
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(rid);
            userRoleService.save(userRole);
        });
    }
    /**
     * 修改
     */
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id,@RequestBody User user){
        //先删除掉关联表的数据
        userRoleService.remove(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId,id));
        this.saveBatchUserRoles(user);
        return Result.success(userService.updateById(user));
    }

    /**
     * 修改个人信息接口
     */
    @ClearPerms(value = true)
    @PutMapping
    public  Result updateByInfo(@RequestBody User user){
        return Result.success(userService.updateById(user));
    }
    /**
     * 查询所有的用户
     */
    @GetMapping
    public Result findAll(){
        return Result.success(userService.list());
    }

    /**
     * 获取单个用户
     */
    @GetMapping("/{id}")
    public  Result  findOne(@PathVariable Integer id){
        User user = userService.getById(id);
        user.setRoleIds(userRoleService.list(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId,user.getId())).stream().map(UserRole::getRoleId).collect(Collectors.toList()));
        return Result.success(user);
    }

    /**
     * 分页展示数据
     */
    @GetMapping("/page")
    public Result  findPage( @RequestParam(defaultValue = "") String  name,
                             @RequestParam(defaultValue = "1") Integer  pageNum,
                             @RequestParam(defaultValue = "10") Integer  pageSize
                             ){
        return Result.success(userService.page(new Page<>(pageNum,pageSize),new LambdaQueryWrapper<User>().like(User::getUsername,name).orderByDesc(User::getId)));
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id){
        removeandLogout(id);
        return Result.success(userService.removeById(id));
    }


    /**
     * 删除和下线
     */
    private  void removeandLogout(Object id){
        userRoleService.remove(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId,id));
        StpUtil.logout(id,"PC");
    }


    /**
     * 批量删除
     */
    @DeleteMapping("/batch/{ids}")
    @Transactional
    public Result deleteByIds(@PathVariable String[] ids){
        Arrays.asList(ids).forEach(id->removeandLogout(id));
        return Result.success(userService.removeByIds(Arrays.asList(ids)));
    }

    /**
     * excel导出功能
     */
    @GetMapping("/batch/export/{ids}")
    public void exportids(@PathVariable String[]  ids , HttpServletResponse response) throws IOException {
        List<User> list = userService.listByIds(Arrays.asList(ids));
        //设置res   swagger
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String filename = URLEncoder.encode("导出数据", "utf-8").replaceAll("\\+", "%20");

        response.setHeader("Content-Disposition", "attachment; filename*=utf-8''"+filename+".xlsx");
        EasyExcel.write(response.getOutputStream(),User.class).sheet("sheet1").doWrite(list);

    }
}
