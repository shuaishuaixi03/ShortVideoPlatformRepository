package com.wcx.video.domain;

import lombok.Data;

import java.util.Date;
import java.util.List;


@Data
public class FollowingGroup {
    private Long id;

    private Long userId;

    private String name;

    private String type;

    private Date createTime;

    private Date updateTime;

    private List<UserInfo> followingUserInfoList;
}
