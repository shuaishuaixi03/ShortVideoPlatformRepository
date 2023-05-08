package com.wcx.video.dao;


import com.wcx.video.domain.File;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileDao {

    /**
     * 向数据库中写入文件类
     * @param file
     * @return
     */
    Integer addFile(File file);

    /**
     * 根据Md5值获取文件类
     * @param fileMd5
     * @return
     */
    File getFileByMD5(String fileMd5);
}
