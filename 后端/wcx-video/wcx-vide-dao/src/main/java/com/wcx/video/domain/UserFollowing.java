package com.wcx.video.domain;

import lombok.Data;

import java.util.Date;


@Data
public class UserFollowing {
    private Long id;

    private Long userId;

    private Long followingId;

    private Long groupId;

    private Date createTime;

    //VO层
    private UserInfo userInfo;
}
