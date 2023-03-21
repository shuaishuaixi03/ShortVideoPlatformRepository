package com.wcx.video.domain;

import lombok.Data;

import java.util.Date;

@Data
public class VideoLike {
    private Long id;

    //用户id
    private Long userId;

    //投稿的视频id
    private Long videoId;

    private Date createTime;
}
