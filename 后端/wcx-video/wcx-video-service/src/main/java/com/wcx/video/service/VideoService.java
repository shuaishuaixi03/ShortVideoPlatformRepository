package com.wcx.video.service;


import com.wcx.video.service.utils.ImageUtil;
import com.wcx.video.dao.VideoDao;
import com.wcx.video.domain.*;
import com.wcx.video.domain.exception.ConditionException;
import com.wcx.video.service.utils.FastDFSUtil;
import com.wcx.video.service.utils.IpUtil;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericItemPreferenceArray;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VideoService {

    @Autowired
    private VideoDao videoDao;

    @Autowired
    private FastDFSUtil fastDFSUtil;

    @Autowired
    private UserCoinService userCoinService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ImageUtil imageUtil;

    private static final int FRAME_NO = 256;

    @Transactional
    public void addVideos(Video video) {
        Date now = new Date();
        video.setCreateTime(now);
        videoDao.addVideos(video);
        Long videoId = video.getId();
        List<VideoTag> tagList = video.getVideoTagList();
        tagList.forEach(item -> {
            item.setCreateTime(now);
            item.setVideoId(videoId);
        });
        videoDao.batchAddVideoTags(tagList);
    }

    public PageResult<Video> pageListVideos(Integer size, Integer no, String area) {
        if (size == null || no == null) {
            throw new ConditionException("参数异常!");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("start", (no - 1) * size);
        params.put("limit", size);
        params.put("area", area);
        List<Video> videoList = new ArrayList<>();
        Integer total = videoDao.pageCountVideos(params);
        if (total > 0) {
            videoList = videoDao.pageListVideos(params);
        }
        return new PageResult<>(total, videoList);
    }

    public void viewVideoOnlineBySlices(HttpServletRequest request, HttpServletResponse response, String url) {
        try {
            fastDFSUtil.viewVideoOnlineBySlices(request, response, url);
        } catch (Exception e) {
            throw new ConditionException("视频在线播放异常!");
        }
    }


    public void addVideoLike(Long videoId, Long userId) {
        Video video = videoDao.getVideoById(videoId);
        if (video == null) {
            throw new ConditionException("非法视频!");
        }
        VideoLike videoLike = videoDao.getVideoLikeByVideoIdAndUserId(videoId, userId);
        if (videoLike != null) {
            throw new ConditionException("已经点赞过!");
        }
        videoLike = new VideoLike();
        videoLike.setVideoId(videoId);
        videoLike.setUserId(userId);
        videoLike.setCreateTime(new Date());
        //将该用户对视频的点赞写入到视频点赞关联表中
        videoDao.addVideoLike(videoLike);
    }

    public void deleteVideoLike(Long videoId, Long userId) {
        videoDao.deleteVideoLike(videoId, userId);
    }

    public Map<String, Object> getVideoLikes(Long videoId, Long userId) {
        Long count = videoDao.getVideoLikes(videoId);
        //判断当前用户是否点赞了视频，方便前端展示
        VideoLike videoLike = videoDao.getVideoLikeByVideoIdAndUserId(videoId, userId);
        boolean like = videoLike != null;
        Map<String, Object> res = new HashMap<>();
        res.put("count", count);
        res.put("like", like);
        return res;
    }


    @Transactional
    public void addVideoCollection(VideoCollection videoCollection, Long userId) {
        Long videoId = videoCollection.getVideoId();
        Long groupId = videoCollection.getGroupId();
        if (videoId == null || groupId == null) {
            throw new ConditionException("参数异常！");
        }
        Video video = videoDao.getVideoById(videoId);
        if (video == null) {
            throw new ConditionException("非法视频!");
        }
        //删除原有视频收藏
        videoDao.deleteVideoCollection(videoId, userId);
        //添加新的视频收藏
        videoCollection.setUserId(userId);
        videoCollection.setCreateTime(new Date());
        videoDao.addVideoCollection(videoCollection);
    }

    public void deleteVideoCollection(Long videoId, Long userId) {
        videoDao.deleteVideoCollection(videoId, userId);
    }

    public Map<String, Object> getVideoCollections(Long videoId, Long userId) {
        Long count = videoDao.getVideoCollections(videoId);
        VideoCollection videoCollection = videoDao.getVideoCollectionByVideoIdAndUserId(videoId, userId);
        boolean like = videoCollection != null;
        Map<String, Object> res = new HashMap<>();
        res.put("count", count);
        res.put("like", like);
        return res;
    }


    public void addVideoCoins(VideoCoin videoCoin, Long userId) {
        Long videoId = videoCoin.getVideoId();
        //需要的投币数量
        Integer amount = videoCoin.getAmount();
        if (videoId == null) {
            throw new ConditionException("参数异常!");
        }
        //查询视频是否存在
        Video video = videoDao.getVideoById(videoId);
        if (video == null) {
            throw new ConditionException("非法视频!");
        }
        //查询当前登录用户是否用于足够的硬币
        Integer userCoinsAmount = userCoinService.getUserCoinsAmount(userId);
        userCoinsAmount = userCoinsAmount == null ? 0 : userCoinsAmount;
        if (amount > userCoinsAmount) {
            throw new ConditionException("硬币数量不足");
        }
        //查询当前是否已经对视频投过币
        VideoCoin dbVideoCoin = videoDao.getVideoCoinByVideoIdAndUserId(videoId, userId);
        //新增视频投币数
        if (dbVideoCoin == null) {
            videoCoin.setUserId(userId);
            videoCoin.setCreateTime(new Date());
            videoDao.addVideoCoin(videoCoin);
        } else {
            Integer dbAmount = dbVideoCoin.getAmount();
            dbAmount += amount;
            //跟新视频投币
            videoCoin.setUserId(userId);
            videoCoin.setAmount(dbAmount);
            videoCoin.setUpdateTime(new Date());
            videoDao.updateVideoCoin(videoCoin);
        }
        //跟新用户的硬币总数
        userCoinService.updateUserCoinsAmount(userId, (userCoinsAmount - amount));
    }


    public Map<String, Object> getVideoCoins(Long videoId, Long userId) {
        Long count = videoDao.getVideoCoinsAmount(videoId);
        VideoCoin videoCollection = videoDao.getVideoCoinByVideoIdAndUserId(videoId, userId);
        boolean like = videoCollection != null;
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("like", like);
        return result;
    }

    public void addVideoComment(VideoComment videoComment, Long userId) {
        //获取添加评论的视频的videoId
        Long videoId = videoComment.getVideoId();
        if (videoId == null) {
            throw new ConditionException("参数异常！");
        }
        //查询要评论的视频是否存在
        Video video = videoDao.getVideoById(videoId);
        if (video == null) {
            throw new ConditionException("非法视频！");
        }
        videoComment.setUserId(userId);
        videoComment.setCreateTime(new Date());
        videoDao.addVideoComment(videoComment);
    }

    public PageResult<VideoComment> pageListVideoComments(Integer size, Integer no, Long videoId) {
        Video video = videoDao.getVideoById(videoId);
        if (video == null) {
            throw new ConditionException("非法视频！");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("start", (no - 1) * size);
        params.put("limit", size);
        params.put("videoId", videoId);
        //查询视频的一级评论的总评论数
        Integer total = videoDao.pageCountVideoComments(params);
        List<VideoComment> commentList = new ArrayList<>();
        if (total > 0) {
            //批量查询二级评论
            //查询视频评论的一级评论的Id
            List<Long> parentIdList = commentList.stream()
                    .map(VideoComment::getId)
                    .collect(Collectors.toList());
            //根据一级评论的Id查询出所有的二级评论
            List<VideoComment> chlidCommentList = videoDao.batchGetVideoCommentsByRootIds(parentIdList);


            //批量查询用户信息
            //查询一级评论的用户id集合，就是评论这个视频的用户id
            Set<Long> userIdSet = commentList.stream()
                    .map(VideoComment::getUserId)
                    .collect(Collectors.toSet());
            //查询回复一级评论的用户id集合，就是二级评论的用户自身的id集合，就是这个评论是谁回复的
            Set<Long> replyUserIdSet = chlidCommentList.stream()
                    .map(VideoComment::getUserId)
                    .collect(Collectors.toSet());
            //查询回复一级评论的目标回复用户id集合，就是这个评论的回复用户id集合，就是这个评论是回复谁的
            Set<Long> childUserIdSet = chlidCommentList.stream()
                    .map(VideoComment::getReplyUserId)
                    .collect(Collectors.toSet());
            userIdSet.addAll(replyUserIdSet);
            userIdSet.addAll(childUserIdSet);

            //构建userId和userInfo对应的Map集合，方便根据userId查找用户基本信息
            List<UserInfo> userInfoList = userService.batchGetUserInfoByUserIds(userIdSet);
            Map<Long, UserInfo> userInfoMap = userInfoList.stream()
                    .collect(Collectors.toMap(UserInfo::getUserId, userInfo -> userInfo));

            commentList.forEach(comment -> {
                Long id = comment.getId();
                //创建存放一级评论的二级评论的列表
                List<VideoComment> childList = new ArrayList<>();
                chlidCommentList.forEach(child -> {
                    //如果当前评论是这个评论的父级评论，把这个评论加入到它的子级评论中
                    if (id.equals(child.getRootId())) {
                        child.setUserInfo(userInfoMap.get(child.getUserId()));
                        child.setReplyUserInfo(userInfoMap.get(child.getReplyUserId()));
                        childList.add(child);
                    }
                });
                comment.setChildList(childList);
                comment.setUserInfo(userInfoMap.get(comment.getUserId()));
            });
        }
        return new PageResult<>(total, commentList);
    }

    public Map<String, Object> getVideoDetails(Long videoId) {
        Video video = videoDao.getVideoDetails(videoId);
        Long userId = video.getUserId();
        User user = userService.getUserInfo(userId);
        UserInfo userInfo = user.getUserInfo();
        Map<String, Object> res = new HashMap<>();
        res.put("video", video);
        res.put("userInfo", userInfo);
        return res;
    }

    public void addVideoView(VideoView videoView, HttpServletRequest request) {
        Long userId = videoView.getUserId();
        Long videoId = videoView.getVideoId();
        //生成cilentId
        String agent = request.getHeader("User-Agent");
        UserAgent userAgent = UserAgent.parseUserAgentString(agent);
        String clientId = String.valueOf(userAgent.getId());
        String ip = IpUtil.getIP(request);
        Map<String, Object> params = new HashMap<>();
        if (userId != null) {
            params.put("userId", userId);
        } else {
            params.put("ip", ip);
            params.put("clientId", clientId);
        }
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd");
        params.put("today", sdf.format(now));
        params.put("videoId", videoId);
        //添加观看记录
        VideoView dbVideoView = videoDao.getVideoView(params);

    }

    public Integer getVideoViewCounts(Long videoId) {
        return videoDao.getVideoViewCounts(videoId);
    }

    /**
     * 基于用户的协同推荐
     * @param userId 用户id
     * @return
     */
    public List<Video> recommend(Long userId) throws TasteException {
        List<UserPreference> list = videoDao.getAllUserPreference();
        //创建数据模型
        DataModel dataModel = this.createDataModel(list);
        //获取用户的相似程度
        UserSimilarity similarity = new UncenteredCosineSimilarity(dataModel);
        System.out.println(similarity.userSimilarity(11, 12));
        //获取用户邻居
        UserNeighborhood userNeighborhood = new NearestNUserNeighborhood(2, similarity, dataModel);
        long[] ar = userNeighborhood.getUserNeighborhood(userId);
        //构建推荐器
        Recommender recommender = new GenericUserBasedRecommender(dataModel, userNeighborhood, similarity);
        //推荐视频
        List<RecommendedItem> recommendedItems = recommender.recommend(userId, 5);
        List<Long> itemIds = recommendedItems.stream().map(RecommendedItem::getItemID).collect(Collectors.toList());
        return videoDao.batchGetVideoByIds(itemIds);
    }

    /**
     * 基于内容的协同推荐
     * @param userId 用户Id
     * @param itemId 参考内容id(根据这个内容进行相似度推荐)
     * @param howMany 需要推荐的数量
     * @return
     * @throws TasteException
     */
    public List<Video> recommendByItem(Long userId, Long itemId, int howMany) throws TasteException {
        List<UserPreference> list = videoDao.getAllUserPreference();
        //创建数据模型
        DataModel dataModel = this.createDataModel(list);
        //获取内容相似度
        ItemSimilarity similarity = new UncenteredCosineSimilarity(dataModel);
        GenericItemBasedRecommender genericItemBasedRecommender = new GenericItemBasedRecommender(dataModel, similarity);
        // 物品推荐相似度, 计算两个物品同时出现的次数,次数越多任务的相似度越高
        List<Long> itemIds = genericItemBasedRecommender.recommendedBecause(userId, itemId, howMany)
                .stream()
                .map(RecommendedItem::getItemID)
                .collect(Collectors.toList());
        //推荐视频
        return videoDao.batchGetVideoByIds(itemIds);
    }

    private DataModel createDataModel(List<UserPreference> userPreferenceList) {
        FastByIDMap<PreferenceArray> fastByIDMap = new FastByIDMap<>();
        Map<Long, List<UserPreference>> map = userPreferenceList.stream().collect(Collectors.groupingBy(UserPreference::getUserId));
        Collection<List<UserPreference>> list = map.values();
        for (List<UserPreference> userPreferences : list) {
            GenericPreference[] array = new GenericPreference[userPreferences.size()];
            for (int i = 0; i < userPreferences.size(); i ++) {
                UserPreference userPreference = userPreferences.get(i);
                GenericPreference item = new GenericPreference(userPreference.getUserId(), userPreference.getVideoId(), userPreference.getValue());
                array[i] = item;
            }
            fastByIDMap.put(array[0].getUserID(), new GenericItemPreferenceArray(Arrays.asList(array)));
        }
        return new GenericDataModel(fastByIDMap);
    }

    public List<VideoBinaryPicture> convertVideoToImage(Long videoId, String fileMd5) throws Exception {
        com.wcx.video.domain.File file = fileService.getFileByMD5(fileMd5);
        String filePath = "/User/hat/tmpfile/fileForVideoId" + videoId + "." + file.getType();
        fastDFSUtil.downLoadFile(file.getUrl(), filePath);
        FFmpegFrameGrabber fFmpegFrameGrabber = FFmpegFrameGrabber.createDefault(filePath);
        fFmpegFrameGrabber.start();
        int ffLength = fFmpegFrameGrabber.getLengthInFrames();
        Frame frame;
        Java2DFrameConverter converter = new Java2DFrameConverter();
        int count = 1;
        List<VideoBinaryPicture> pictures = new ArrayList<>();
        for (int i = 1; i <= ffLength; i ++) {
            long timestamp = fFmpegFrameGrabber.getTimestamp();
            frame = fFmpegFrameGrabber.grabImage();
            if (count == i) {
                if (frame == null) {
                    throw new ConditionException("无效帧");
                }
                BufferedImage bufferedImage = converter.getBufferedImage(frame);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", os);
                InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
                //输出黑白剪影文件
                java.io.File outputFile = java.io.File.createTempFile("convert-" + videoId + "-", ".png");
                BufferedImage binaryImg = imageUtil.getBodyOutFile(bufferedImage, inputStream);
                ImageIO.write(binaryImg, "png", outputFile);
                //将图片的白色部分转换成透明色
                imageUtil.transferAlpha(outputFile, outputFile);
                //上传视频剪影文件
                String imgUrl = fastDFSUtil.uploadCommonFile(outputFile, "png");
                VideoBinaryPicture videoBinaryPicture = new VideoBinaryPicture();
                videoBinaryPicture.setFrameNo(i);
                videoBinaryPicture.setUrl(imgUrl);
                videoBinaryPicture.setVideoId(videoId);
                videoBinaryPicture.setVideoTimestamp(timestamp);
                pictures.add(videoBinaryPicture);
                count += FRAME_NO;
                //删除临时文件
                outputFile.delete();
            }
        }
        //删除临时文件
        java.io.File tmpFile = new java.io.File(filePath);
        tmpFile.delete();
        //批量添加视频剪影文件
        videoDao.batchAddVideoBinaryPictures(pictures);
        return pictures;
    }


    public List<VideoBinaryPicture> getVideoBinaryImages(Map<String, Object> params) {
        return videoDao.getVideoBinaryImages(params);
    }

    public List<VideoTag> getVideoTagsByVideoId(Long videoId) {
        return videoDao.getVideoTagsByVideoId(videoId);
    }

    public void deleteVideoTags(List<Long> tagIdList, Long videoId) {
        videoDao.deleteVideoTags(tagIdList, videoId);
    }
}
