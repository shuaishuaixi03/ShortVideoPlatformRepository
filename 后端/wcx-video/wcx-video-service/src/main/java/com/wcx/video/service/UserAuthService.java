package com.wcx.video.service;


import com.wcx.video.domain.auth.*;
import com.wcx.video.domain.constant.AuthRoleConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserAuthService {

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private AuthRoleService authRoleService;

    public UserAuthorities getUserAuthorities(Long userId) {
        //查询用户的角色列表
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        //得到用户的角色列表的roleId
        Set<Long> roleIdSet = userRoleList.stream()
                .map(UserRole :: getRoleId)
                .collect(Collectors.toSet());

        List<AuthRoleElementOperation> authRoleElementOperationList = authRoleService.getRoleElementOperationsByRoleIds(roleIdSet);

        List<AuthRoleMenu> authRoleMenuList = authRoleService.getAuthMenusByRoleIds(roleIdSet);

        UserAuthorities userAuthorities = new UserAuthorities();
        userAuthorities.setRoleElementOperationList(authRoleElementOperationList);
        userAuthorities.setRoleMenuList(authRoleMenuList);
        return userAuthorities;
    }

    public void addUserDefaultRole(Long id) {
        UserRole userRole = new UserRole();
        AuthRole role = authRoleService.getRoleByCode(AuthRoleConstant.ROLE_LV0);
        userRole.setUserId(id);
        userRole.setRoleId(role.getId());
        userRoleService.addUserRole(userRole);
    }
}
