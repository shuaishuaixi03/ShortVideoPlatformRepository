package com.wcx.video.domain;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

@Data
@Document(indexName = "videos")
public class Video {


    @Id
    private Long id;

    @Field(type = FieldType.Long)

    //用户id
    private Long userId;

    //视频地址(文件服务器中的相对地址)
    private String url;

    //封面链接
    private String thumbnail;


    @Field(type = FieldType.Text)
    //视频标题
    private String title;

    //视频类型, 0原创，1转载
    private String type;

    //视频时长
    private String duration;

    //视频所在分区: 0搞笑，1音乐，2影视
    private String area;

    @Field(type = FieldType.Text)
    //视频简介
    private String description;

    @Field(type = FieldType.Date)
    private Date createTime;

    @Field(type = FieldType.Date)
    private Date updateTime;

    //视频的标签列表
    private List<VideoTag> videoTagList;
}
