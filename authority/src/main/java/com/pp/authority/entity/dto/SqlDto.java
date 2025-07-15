package com.pp.authority.entity.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @program: kael
 * @ClassName:SqlDto
 * @description: SQL语句构建类
 * @author:dyy
 * @Version 1.0
 **/
@Data
@Accessors(chain = true)
public class SqlDto implements Serializable {
    private static final long serialVersionUID = 1L;
//    名称
    private String name;
//    字段类型和常用长度
    private String  typeAndsize;

//    注释描述
    private String  description;
//  默认值
    private String  defaultValue;

}