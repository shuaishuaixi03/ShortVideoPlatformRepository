package com.wcx.video.dao;

import com.wcx.video.domain.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface VideoDao {
    /**
     * 写入视频记录
     * @param video
     * @return
     */
    Integer addVideos(Video video);

    /**
     * 给视频添加一些标签元素
     * @param videoTagList
     * @return
     */
    Integer batchAddVideoTags(@Param("videoTagList") List<VideoTag> videoTagList);

    /**
     * 分页查询指定分区的视频的总个数
     * @param params
     * @return
     */
    Integer pageCountVideos(Map<String, Object> params);

    /**
     * 分页查询指定分区的视频的所有记录
     * @param params
     * @return
     */
    List<Video> pageListVideos(Map<String, Object> params);

    /**
     * 根据id查找视频
     * @param videoId
     * @return
     */
    Video getVideoById(Long videoId);

    /**
     * 根据视频id和userId查找视频点赞记录
     * @param videoId
     * @param userId
     * @return
     */
    VideoLike getVideoLikeByVideoIdAndUserId(@Param("videoId")Long videoId,
                                             @Param("userId") Long userId);

    /**
     * 写入视频点赞记录到数据库表中
     * @param videoLike
     * @return
     */
    Integer addVideoLike(VideoLike videoLike);

    /**
     * 根据视频id和userId删除对应的视频点赞记录
     * @param videoId
     * @param userId
     * @return
     */
    Integer deleteVideoLike(@Param("videoId") Long videoId,
                            @Param("userId") Long userId);

    /**
     * 根据videoId获取视频的总点赞数
     * @param videoId
     * @return
     */
    Long getVideoLikes(Long videoId);

    /**
     * 根据videoId和userId删除视频收藏
     * @param videoId
     * @param userId
     */
    Integer deleteVideoCollection(@Param("videoId") Long videoId,
                                  @Param("userId") Long userId);

    /**
     * 添加视频收藏记录
     * @param videoCollection
     * @return
     */
    Integer addVideoCollection(VideoCollection videoCollection);

    /**
     * 获取视频收藏总数
     * @param videoId
     * @return
     */
    Long getVideoCollections(Long videoId);

    /**
     * 根据videoId和userId查找视频收藏记录
     * @param videoId
     * @param userId
     * @return
     */
    VideoCollection getVideoCollectionByVideoIdAndUserId(@Param("videoId") Long videoId,
                                                         @Param("userId") Long userId);

    /**
     * 根据videoId和userId查询视频投币记录
     * @param videoId
     * @param userId
     * @return
     */
    VideoCoin getVideoCoinByVideoIdAndUserId(@Param("videoId")Long videoId,
                                             @Param("userId") Long userId);

    /**
     * 添加视频投币记录
     * @param videoCoin
     * @return
     */
    Integer addVideoCoin(VideoCoin videoCoin);

    /**
     * 更新视频投币记录
     * @param videoCoin
     */
    Integer updateVideoCoin(VideoCoin videoCoin);

    /**
     * 根据videoId查询视频的硬币总数
     * @param videoId
     * @return
     */
    Long getVideoCoinsAmount(Long videoId);

    /**
     * 添加视频评论记录
     * @param videoComment
     * @return
     */
    Integer addVideoComment(VideoComment videoComment);

    /**
     * 查询视频的一级评论的总评论数
     * @param params
     * @return
     */
    Integer pageCountVideoComments(Map<String, Object> params);

    /**
     * 根据一级评论Id列表查询出二级评论列表
     * @param rootIdList
     * @return
     */
    List<VideoComment> batchGetVideoCommentsByRootIds(@Param("rootIdList") List<Long> rootIdList);

    /**
     * 根据id查询视频记录
     * @param videoId
     * @return
     */
    Video getVideoDetails(Long videoId);

    /**
     * 根据 日期时间和videoId 查询视频观看记录
     * @param params
     * @return
     */
    VideoView getVideoView(Map<String, Object> params);

    /**
     * 根据videoId查询t_video_view中多少条观看记录
     * @param videoId
     * @return
     */
    Integer getVideoViewCounts(Long videoId);

    /**
     * 根据userId和videoId分组, 从t_video_operation表计算出所有的UserPerference
     * @return
     */
    List<UserPreference> getAllUserPreference();

    /**
     * 根据videoId列表查询具体的内容
     * @param itemIds
     * @return
     */
    List<Video> batchGetVideoByIds(@Param("idList") List<Long> itemIds);

    /**
     * 批量添加一个视频视频的剪影文件
     */
    Integer batchAddVideoBinaryPictures(@Param("pictureList")List<VideoBinaryPicture> pictureList);

    List<VideoBinaryPicture> getVideoBinaryImages(Map<String, Object> params);

    /**
     * 联表查询
     * 查询出视频的所有标签
     * @param videoId
     * @return
     */
    List<VideoTag> getVideoTagsByVideoId(Long videoId);

    /**
     * 视频标签表中删除视频标签的记录
     * @param tagIdList
     * @param videoId
     * @return
     */
    Integer deleteVideoTags(@Param("tagIdList") List<Long> tagIdList,
                            @Param("videoId") Long videoId);
}
