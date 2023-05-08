package com.wcx.video.domain.annotation;


import org.springframework.stereotype.Component;

import java.lang.annotation.*;

//注解什么时候生效：运行时
@Retention(RetentionPolicy.RUNTIME)
//注解的目标：方法
@Target({ElementType.METHOD})
@Documented
@Component
public @interface ApiLimitedRole {

    String[] limitedRoleCodeList() default {};
}
