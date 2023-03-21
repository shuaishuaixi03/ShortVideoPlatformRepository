package com.wcx.video.api.aspect;


import com.wcx.video.api.support.UserSupport;
import com.wcx.video.domain.UserMoment;
import com.wcx.video.domain.auth.UserRole;
import com.wcx.video.domain.constant.AuthRoleConstant;
import com.wcx.video.domain.exception.ConditionException;
import com.wcx.video.service.UserRoleService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Order(1)
@Component
@Aspect
public class DataLimitedAspect {

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserRoleService userRoleService;

    //定义切点
    @Pointcut("@annotation(com.wcx.video.domain.annotation.DataLimited)")
    public void check() {
    }

    @Before("check()")
    public void doBefore(JoinPoint joinPoint) {
        Long userId = userSupport.getCurrentUserId();
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        Set<String> roleCodeSet = userRoleList.stream()
                .map(UserRole::getRoleCode)
                .collect(Collectors.toSet());
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof UserMoment) {
                UserMoment userMoment = (UserMoment) arg;
                String type = userMoment.getType();
                //等级为LV1的用户不能发布type不等于0的动态
                if (roleCodeSet.contains(AuthRoleConstant.ROLE_LV1) && !"0".equals(userMoment.getType())) {
                    throw new ConditionException("参数异常");
                }
            }
        }
    }
}
