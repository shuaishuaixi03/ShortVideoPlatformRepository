package com.wcx.video.domain.auth;


import lombok.Data;

import java.util.Date;

@Data
public class AuthRoleMenu {

    private Long id;

    //用户角色id
    private Long roleId;

    //页面菜单id
    private Long menuId;

    private Date createTime;

    //VO层
    private AuthMenu authMenu;
}
