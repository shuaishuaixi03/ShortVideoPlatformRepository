package com.wcx.video.domain.auth;

import lombok.Data;

import java.util.Date;


@Data
public class AuthRole {
    private Long id;

    //角色名称
    private String name;

    //角色唯一编码
    private String code;

    private Date createTime;
}
