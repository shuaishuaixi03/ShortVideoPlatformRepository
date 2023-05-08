package com.wcx.video.api.aspect;


import com.wcx.video.api.support.UserSupport;
import com.wcx.video.domain.annotation.ApiLimitedRole;
import com.wcx.video.domain.auth.UserRole;
import com.wcx.video.domain.exception.ConditionException;
import com.wcx.video.service.UserRoleService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

//设置优先级为1
@Order(1)
@Component
@Aspect
public class ApiLimitedRoleAspect {

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserRoleService userRoleService;


    //定义切点
    @Pointcut("@annotation(com.wcx.video.domain.annotation.ApiLimitedRole)")
    public void check() {
    }

    //定义前置通知
    @Before("check() && @annotation(apiLimitedRole)")
    public void doBefore(JoinPoint joinPoint, ApiLimitedRole apiLimitedRole) {
        Long userId = userSupport.getCurrentUserId();
        //查询用户的角色列表，得到对应的Code列表
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        Set<String> roleCodeSet = userRoleList.stream()
                .map(UserRole::getRoleCode)
                .collect(Collectors.toSet());
        //获取该接口定义的受限接口列表，得到对应的Code列表
        String[] limitedRoleCodeList = apiLimitedRole.limitedRoleCodeList();
        Set<String> limitedRoleCodeSet = Arrays.stream(limitedRoleCodeList)
                .collect(Collectors.toSet());
        //将两个Code列表做交集，看用户的角色列表是否处于受限角色列表中
        roleCodeSet.retainAll(limitedRoleCodeSet);
        if (roleCodeSet.size() > 0) {
            throw new ConditionException("权限不足!");
        }
    }
}
