package com.pp.authority.entity.dto;

import lombok.Data;

import java.io.Serializable;
/**
 * @program: kael
 * @ClassName:MailBean
 * @description: 邮箱的实体类
 * @author:dyy
 * @Version 1.0
 **/
@Data
public class MailBean implements Serializable {
    private static final long serialVersionUID = -2116367492649751914L;
    private String recipient;//邮件接收人
    private String subject; //邮件主题
    private String content; //邮件内容
    // 省略setget方法
}