package com.wcx.video.domain;

import lombok.Data;

import java.util.Date;


@Data
public class User {

    private Long id;

    private String phone;

    private String email;

    private String password;

    private String salt;

    private Date createTime;

    private Date updateTime;


    //VO层
    private UserInfo userInfo;
}
