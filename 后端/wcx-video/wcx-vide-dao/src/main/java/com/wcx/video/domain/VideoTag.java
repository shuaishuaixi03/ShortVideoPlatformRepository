package com.wcx.video.domain;

import lombok.Data;

import java.util.Date;

@Data
public class VideoTag {
    private Long id;

    //视频id
    private Long videoId;

    //标签id
    private Long tagId;

    private Date createTime;

}
