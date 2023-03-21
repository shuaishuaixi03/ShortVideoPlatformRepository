package com.wcx.video.domain;


import lombok.Data;

import java.util.Date;

@Data
public class File {
    private Long id;

    private String url;

    private String type;

    private String md5;

    private Date createTime;
}
