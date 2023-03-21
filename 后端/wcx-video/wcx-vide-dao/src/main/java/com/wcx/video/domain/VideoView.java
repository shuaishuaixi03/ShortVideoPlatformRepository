package com.wcx.video.domain;


import lombok.Data;

import java.util.Date;

@Data
public class VideoView {

    private Long id;

    //视频id
    private Long videoId;

    //用户id
    private Long userId;

    //客户端id
    private String clientId;

    //ip地址
    private String ip;

    private Date createTime;
}
