package com.wcx.video.domain.auth;

import lombok.Data;

import java.util.Date;


@Data
public class AuthElementOperation {

    private Long id;


    //页面元素名称
    private String elementName;

    //页面元素唯一编码
    private String elementCode;

    //操作类型，0表示可以点击，1表示可见，不可以点击
    private String operationType;

    private Date createTime;

    private Date updateTime;
}
