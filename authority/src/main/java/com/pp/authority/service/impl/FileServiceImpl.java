package com.pp.authority.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pp.authority.entity.Files;
import com.pp.authority.entity.RolePermission;
import com.pp.authority.mapper.FileMapper;
import com.pp.authority.mapper.RolePermissionMapper;
import com.pp.authority.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FileServiceImpl  extends ServiceImpl<FileMapper, Files> implements FileService {


}
