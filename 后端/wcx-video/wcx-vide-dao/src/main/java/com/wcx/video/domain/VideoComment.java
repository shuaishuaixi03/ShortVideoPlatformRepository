package com.wcx.video.domain;


import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class VideoComment {

    private Long id;

    //视频Id
    private Long videoId;

    //用户id
    private Long userId;

    //评论
    private String comment;

    //回复用户id
    private Long replyUserId;

    //根节点评论id
    private Long rootId;

    private Date createTime;

    private Date updateTime;

    //二级评论列表 VO层
    private List<VideoComment> childList;

    //评论用户的基本信息 VO层
    private UserInfo userInfo;

    //回复评论用户的基本信息 VO层
    private UserInfo replyUserInfo;
}
