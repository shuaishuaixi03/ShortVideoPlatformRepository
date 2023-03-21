package com.wcx.video.domain.auth;


import lombok.Data;

import java.util.Date;

@Data
public class AuthMenu {

    private Long id;

    //菜单项目名称
    private String name;

    //菜单项目唯一编码
    private String code;

    private Date createTime;

    private Date updateTime;
}
