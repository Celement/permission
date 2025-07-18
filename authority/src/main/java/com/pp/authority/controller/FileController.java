package com.pp.authority.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pp.authority.common.Result;
import com.pp.authority.entity.Files;
import com.pp.authority.entity.User;
import com.pp.authority.mapper.FileMapper;
import com.pp.authority.service.FileService;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

@RequestMapping("/file")
@RestController
public class FileController {

    @Value("${files.upload.path}")
    private String fileUploadPath;

    @Resource
    private FileService fileService;

    @Resource
    private FileMapper fileMapper;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public Result upload(@RequestParam MultipartFile file) throws IOException {
        //获取原始名称
        String originalFilename = file.getOriginalFilename();
        //获取文件类型
        String type = FileUtil.extName(originalFilename);
        //文件大小
        long size = file.getSize();
        //定一个uuid  文件的id
        String uuid = IdUtil.fastSimpleUUID();
        String  FileUUid=uuid+ StrUtil.DOT+type;
        File uploadFile = new File(fileUploadPath + FileUUid);
        //判断目录是否存在
        if (!uploadFile.getParentFile().exists()){
            uploadFile.getParentFile().mkdirs();
        }

        //获取文件url
        String url;
        //把文件上传到了磁盘中
        file.transferTo(uploadFile);
        //获取文件md5
        String md5 = SecureUtil.md5(uploadFile);
        //去数据库中进行查询 根据md5值查询对应的文件存在不存在
        Files dbFiles = getFileByMd5(md5);
        if (dbFiles!=null){
            url=dbFiles.getUrl();
            //文件存在的
            uploadFile.delete();
        }else {
            //数据库中没有重复文件
            url="http://localhost:9090/file/"+FileUUid;
        }

        //在存储到数据库中
        Files saveFiles = new Files();
        saveFiles.setName(originalFilename);
        saveFiles.setType(type);
        saveFiles.setSize(size/1024);  //kb
        saveFiles.setUrl(url);
        saveFiles.setMd5(md5);
        fileService.save(saveFiles);
        return Result.success(url);
    }
    
    private Files getFileByMd5(String md5){
        List<Files> filesList = fileMapper.selectList(new LambdaQueryWrapper<Files>().eq(Files::getMd5, md5));
        return filesList.size()==0?null:filesList.get(0);
    }

    /**
     * 文件下载
     */
    @GetMapping("/{fileUUID}")
    public void download(@PathVariable String  fileUUID, HttpServletResponse response)throws IOException{
        //根据文件标识获取文件
        File uploadFile = new File(fileUploadPath + fileUUID);
        //设置输出流
        ServletOutputStream os = response.getOutputStream();
        response.setHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(fileUUID,"UTF-8"));
        response.setContentType("application/octet-stream");
        os.write(FileUtil.readBytes(uploadFile));
        os.flush();
        os.close();
    }

    /**
     * 分页展示文件
     */
    @GetMapping("/page")
    public Result  findPage( @RequestParam(defaultValue = "") String  name,
                             @RequestParam(defaultValue = "1") Integer  pageNum,
                             @RequestParam(defaultValue = "10") Integer  pageSize
    ){
        return Result.success(fileMapper.selectPage(new Page<>(pageNum,pageSize),new LambdaQueryWrapper<Files>().eq(Files::getName,name).orderByDesc(Files::getId)));
    }

    /**
     * 修改文件
     */
   @PostMapping
    public Result update(@RequestBody Files files){
       return Result.success(fileService.saveOrUpdate(files));
   }

    //删除文件
    @DeleteMapping("{id}")
    public Result delete(@PathVariable Integer id){
        Files files = fileService.getById(id);
        files.setIsDelete(true);
        fileService.saveOrUpdate(files);
        return Result.success();
    }
    //批量删除
    @DeleteMapping("/batch/{ids}")
    public Result delete(@PathVariable long[] ids){
        fileService.list(new LambdaQueryWrapper<Files>().in(Files::getId,ids)).forEach(files -> {
            files.setIsDelete(true);
            fileService.saveOrUpdate(files);
        });

        return Result.success();
    }
}
