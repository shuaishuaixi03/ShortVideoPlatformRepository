package com.wcx.video.domain.auth;

import lombok.Data;

import java.util.Date;

@Data
public class UserRole {
    private Long id;

    //用户id
    private Long userId;

    //角色id
    private Long roleId;

    private Date createTime;


    //VO层
    private String roleName;

    //VO层
    private String roleCode;
}
