package com.wcx.video.domain;


import lombok.Data;

import java.util.Date;

@Data
public class VideoCoin {
    private Long id;

    //用户id
    private Long userId;

    //视频id
    private Long videoId;

    //视频投币数
    private Integer amount;

    private Date createTime;

    private Date updateTime;
}
