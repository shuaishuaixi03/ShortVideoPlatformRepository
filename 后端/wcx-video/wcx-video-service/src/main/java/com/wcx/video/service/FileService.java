package com.wcx.video.service;


import com.mysql.cj.util.StringUtils;
import com.wcx.video.dao.FileDao;
import com.wcx.video.domain.File;
import com.wcx.video.service.utils.FastDFSUtil;
import com.wcx.video.service.utils.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Service
public class FileService {

    @Autowired
    private FileDao fileDao;

    @Autowired
    private FastDFSUtil fastDFSUtil;

    public String getFileMD5(MultipartFile file) throws Exception {
        return MD5Util.getFileMD5(file);
    }

    public File getFileByMD5(String fileMd5) {
        return fileDao.getFileByMD5(fileMd5);
    }

    public String uploadFileBySlices(MultipartFile slice,
                                     String fileMd5,
                                     Integer sliceNo,
                                     Integer totalSliceNo) throws Exception {
        //从数据库根据文件的MD5值查询之前是否已经上传完文件，如果已经上传，直接返回url，实现秒传
        File dbFileMD5 = fileDao.getFileByMD5(fileMd5);
        if (dbFileMD5 != null) {
            return dbFileMD5.getUrl();
        }
        //使用断点上传文件
        String url = fastDFSUtil.uploadFileBySlices(slice, fileMd5, sliceNo, totalSliceNo);
        //文件的分片已经全部上传，将记录写入数据库
        if (!StringUtils.isNullOrEmpty(url)) {
            dbFileMD5 = new File();
            dbFileMD5.setCreateTime(new Date());
            dbFileMD5.setMd5(fileMd5);
            dbFileMD5.setUrl(url);
            dbFileMD5.setType(fastDFSUtil.getFileType(slice));
            fileDao.addFile(dbFileMD5);
        }
        return url;

    }
}
