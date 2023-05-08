package com.wcx.video.dao;


import com.wcx.video.domain.UserFollowing;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserFollowingDao {

    /**
     * 删除用户关注up主这个关注关系
     * @param userId
     * @param followingId
     * @return
     */
    Integer deleteUserFollowing(@Param("userId") Long userId, @Param("followingId") Long followingId);

    /**
     * 添加用户关注up主这个关注关系
     * @param userFollowing
     * @return
     */
    Integer addUserFollowing(UserFollowing userFollowing);

    List<UserFollowing> getUserFollowings(Long userId);

    List<UserFollowing> getUserFans(Long followingId);
}
