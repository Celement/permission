package com.pp.authority.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: kael
 * @ClassName:GeneratorDto
 * @description: 代码生成器需要的实体类
 * @author:dyy
 * @Version 1.0
 **/
@Data
public class GeneratorDto implements Serializable {
    private static final long serialVersionUID = 1L;
    //创建表名称
    private String tableName;
    //创建表描述或者备注
    private String description;
    //创建时间
    private LocalDateTime createTime;
    //    是否存在富文本
    private String width="30%";
    //sql数组
    private List<SqlDto> domains=new ArrayList<>();


}