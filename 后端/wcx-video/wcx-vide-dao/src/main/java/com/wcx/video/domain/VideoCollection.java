package com.wcx.video.domain;


import lombok.Data;

import java.util.Date;

@Data
public class VideoCollection {
    private Long id;

    //投稿的视频id
    private Long videoId;

    //用户id
    private Long userId;

    //所属的收藏分组id
    private Long groupId;

    private Date createTime;
}
