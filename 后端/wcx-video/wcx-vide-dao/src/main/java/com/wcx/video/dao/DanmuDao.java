package com.wcx.video.dao;


import com.wcx.video.domain.Danmu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface DanmuDao {


    /**
     * 将弹幕记录写入数据库
     * @param danmu
     * @return
     */
    Integer addDanmu(Danmu danmu);


    /**
     * 查询指定范围时间的弹幕记录
     * @param params
     * @return
     */
    List<Danmu> getDanmus(Map<String, Object> params);
}
