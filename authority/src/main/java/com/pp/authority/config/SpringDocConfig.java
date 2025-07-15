package com.pp.authority.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: kael
 * @ClassName:SpringDocConfig
 * @description: 接口文档SwaagerUI的配置类
 * 主要是配置Swaager页面的一些页面介绍参数等等
 * @Version 1.0
 **/
@Configuration
@Slf4j
public class SpringDocConfig {
    @Bean
    public OpenAPI restfulOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("基于Springboot3+Vue3代码生成器系统")
                        .description("Springboot3 and Vue3 代码生成器平台")
                        .version("v1.0.0")
                        .license(new License().name("kael").url("https://baidu.com")));
    }

}
