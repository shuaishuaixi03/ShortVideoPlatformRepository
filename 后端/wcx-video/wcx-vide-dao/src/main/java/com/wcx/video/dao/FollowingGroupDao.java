package com.wcx.video.dao;


import com.wcx.video.domain.FollowingGroup;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FollowingGroupDao {

    /**
     * 根据id查询用户关注分组信息
     * @param id
     * @return
     */
    FollowingGroup getById(Long id);

    /**
     * 根据分组类型查询用户关注分组信息
     *
     */
    FollowingGroup getByType(String type);

    /**
     * 获取userId的所有分组，以列表形式返回
     * @param userId
     * @return
     */
    List<FollowingGroup> getByUserId(Long userId);

    void addFollowingGroup(FollowingGroup followingGroup);

    List<FollowingGroup> getUserFollowingGroups(Long userId);
}
