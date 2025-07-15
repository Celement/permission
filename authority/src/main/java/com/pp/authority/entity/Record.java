package com.pp.authority.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * @program: kael
 * @ClassName:Record
 * @description: 记录实体类
 * @author:dyy
 * @Version 1.0
 **/

@Data
@TableName("sys_record")
@Accessors(chain = true)
@Schema(title = "Record对象", description = "")
public class Record implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(title = "序列号")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(title = "操作人")
    @TableField("uid")
    private Long uid;
    @TableField(exist = false)
    private User user=new User();


    @Schema(title = "sql语句")
    @TableField("sql_str")
    private String sqlStr;

    @Schema(title = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(title = "表名")
    @TableField("table_name")
    private String tableName;

    @Schema(title = "表注释")
    @TableField("description")
    private String description;


}
