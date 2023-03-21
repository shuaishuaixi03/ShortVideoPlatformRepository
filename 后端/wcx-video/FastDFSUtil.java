package com.wcx.video.service.utils;


import com.alibaba.fastjson.JSONObject;
import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.wcx.video.domain.exception.ConditionException;
import io.netty.util.internal.StringUtil;
import org.apache.ibatis.cache.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class FastDFSUtil {


    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    @Autowired
    private AppendFileStorageClient appendFileStorageClient;

    @Autowired
    private RedisTemplate redisTemplate;

//    @Value({"${fdfs.http.storage-addr}"})
//    private String httpFdfsStorageAddr;

    private static final String PATH_KEY = "path-key:";

    private static final String UPLOADED_SIZE_KEY = "uploaded-size-key:";

    private static final String UPLOADED_NO_KEY = "uploaded-no-key:";

    private static final String DEFAULT_GROUP = "group1";


    //文件分片，每一个分片的大小:2MB
    private static final int SLICE_SIZE = 1024 * 1024 * 2;

    /**
     * 返回文件的文件类型，如txt,zip
     * @param file
     * @return
     */
    public String getFileType(MultipartFile file) {
        if (file == null) {
            throw new ConditionException("非法文件!");
        }
        //获取文件的完整名称
        String fileName = file.getOriginalFilename();
        int index = fileName.lastIndexOf(".");
        return fileName.substring(index + 1);
    }

    /**
     * 上传文件到FastDFS，返回:访问文件路径(url)
     * @param file
     * @return
     * @throws IOException
     */
    public String uploadCommonFile(MultipartFile file) throws IOException {
        Set<MetaData> metaDataSet = new HashSet<>();
        String fileType = this.getFileType(file);
        StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), fileType, metaDataSet);
        return storePath.getPath();
    }

    /**
     * 输入文件路径，删除FastDFS中的文件
     * @param filePath
     */
    public void deleteFile(String filePath) {
        fastFileStorageClient.deleteFile(filePath);
    }


    public String uploadAppenderFile(MultipartFile file) throws IOException {
        String fileType = this.getFileType(file);
        StorePath storePath = appendFileStorageClient.uploadAppenderFile(DEFAULT_GROUP, file.getInputStream(), file.getSize(), fileType);
        return storePath.getPath();
    }

    public void modifyAppenderFile(MultipartFile file, String filePath, long offset) throws Exception {
        appendFileStorageClient.modifyFile(DEFAULT_GROUP, filePath, file.getInputStream(), file.getSize(), offset);
    }

    /**
     * 断点上传
     * @param file
     * @param fileMd5
     * @param sliceNo 分片的位置，表示这是文件的第几个分片
     * @param totalSliceNo 这个文件的分片总数，表示文件分成了多少分片
     * @return
     */
    public String uploadFileBySlices(MultipartFile file, String fileMd5, Integer sliceNo, Integer totalSliceNo) throws Exception {
        if(file == null || sliceNo == null || totalSliceNo == null){
            throw new ConditionException("参数异常！");
        }
        String pathKey = PATH_KEY + fileMd5;
        String uploadedSizeKey = UPLOADED_SIZE_KEY + fileMd5;
        String uploadedNoKey = UPLOADED_NO_KEY + fileMd5;
        Long uploadedSize = (Long) redisTemplate.opsForValue().get(uploadedSizeKey);
//        if(!StringUtil.isNullOrEmpty(uploadedSize)){
//            uploadedSize = Long.valueOf(uploadedSize);
//        }
        if (uploadedSize == null) {
            uploadedSize = 0L;
        }
        if(sliceNo == 1){ //上传的是第一个分片
            String path = this.uploadAppenderFile(file);
            if(StringUtil.isNullOrEmpty(path)){
                throw new ConditionException("上传失败！");
            }
            redisTemplate.opsForValue().set(pathKey, path);
            redisTemplate.opsForValue().set(uploadedNoKey, 1);
        }else{
            String filePath = String.valueOf(redisTemplate.opsForValue().get(pathKey));
            if(StringUtil.isNullOrEmpty(filePath)){
                throw new ConditionException("上传失败！");
            }
            this.modifyAppenderFile(file, filePath, uploadedSize);
            redisTemplate.opsForValue().increment(uploadedNoKey);
        }
        // 修改历史上传分片文件大小
        uploadedSize  += file.getSize();
        redisTemplate.opsForValue().set(uploadedSizeKey, uploadedSize);
        //如果所有分片全部上传完毕，则清空redis里面相关的key和value
        Integer uploadedNo = (Integer) redisTemplate.opsForValue().get(uploadedNoKey);
//        Integer uploadedNo = Integer.valueOf(uploadedNoStr);
        String resultPath = "";
        if(uploadedNo.equals(totalSliceNo)){
            resultPath = (String) redisTemplate.opsForValue().get(pathKey);
            List<String> keyList = Arrays.asList(uploadedNoKey, pathKey, uploadedSizeKey);
            redisTemplate.delete(keyList);
        }
        return resultPath;
    }

    /**
     * 测试文件分片
     * 将文件分片，保存到本机指定路径
     * @param multipartFile
     * @throws Exception
     */
    public void convertFileToSlices(MultipartFile multipartFile) throws Exception {
        String fileType = this.getFileType(multipartFile);
        //生成临时文件，将MultipartFile转为JavaIO中的File
        File file = this.multipartFileToFile(multipartFile);
        long fileLength = file.length();
        int count = 1;
        for (int i = 0; i < fileLength; i += SLICE_SIZE) {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            randomAccessFile.seek(i);
            byte[] bytes = new byte[SLICE_SIZE];
            int len = randomAccessFile.read(bytes);
            String path = "C:/Users/wcx/Desktop/FileTest/" + count + "." + fileType;
            File slice = new File(path);
            FileOutputStream fos = new FileOutputStream(slice);
            fos.write(bytes, 0, len);
            fos.close();
            randomAccessFile.close();
            count++;
        }
        //删除临时文件
        file.delete();
    }

    /**
     * 将MultipartFile转为JavaIO中的File
     * @param multipartFile
     * @return
     * @throws Exception
     */
    private File multipartFileToFile(MultipartFile multipartFile) throws Exception {
        String originalFilename = multipartFile.getOriginalFilename();
        String[] fileName = originalFilename.split("\\.");
        File file = File.createTempFile(fileName[0], "." + fileName[1]);
        multipartFile.transferTo(file);
        return file;
    }
}


