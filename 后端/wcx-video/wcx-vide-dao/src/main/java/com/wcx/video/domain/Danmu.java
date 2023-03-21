package com.wcx.video.domain;


import lombok.Data;

import java.util.Date;

@Data
public class Danmu {
    private Long id;

    //用户id
    private Long userId;

    //视频id
    private Long videoId;

    //弹幕内容
    private String content;

    //弹幕出现时间
    private String danmuTime;

    private Date createTime;
}
