package com.pp.authority.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * @program: kael
 * @ClassName: IgoreResult
 * @description: 忽略返回体，也是搭配LogAspect使用的
 * @author:dyy
 * @Version 1.0
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgoreResult {
}
