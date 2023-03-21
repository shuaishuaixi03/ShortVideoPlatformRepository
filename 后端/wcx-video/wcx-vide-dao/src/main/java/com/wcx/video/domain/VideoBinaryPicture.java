package com.wcx.video.domain;


import lombok.Data;

import java.util.Date;


@Data
public class VideoBinaryPicture {

    private Long id;

    //视频id
    private Long videoId;

    //帧数
    private Integer frameNo;

    //图片链接
    private String url;

    //视频时间戳
    private Long videoTimestamp;

    private Date createTime;
}
