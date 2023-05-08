package com.wcx.video.service;


import com.wcx.video.dao.UserFollowingDao;
import com.wcx.video.domain.FollowingGroup;
import com.wcx.video.domain.User;
import com.wcx.video.domain.UserFollowing;
import com.wcx.video.domain.UserInfo;
import com.wcx.video.domain.constant.UserConstant;
import com.wcx.video.domain.exception.ConditionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserFollowingService {

    @Autowired
    private FollowingGroupService followingGroupService;

    @Autowired
    private UserFollowingDao userFollowingDao;

    @Autowired
    private UserService userService;


    @Transactional
    public void addUserFollowings(UserFollowing userFollowing) {
        Long groupId = userFollowing.getGroupId();
        if (groupId == null) {
            //用户关注up主时没有设置分组，默认设置为默认分组
            FollowingGroup followingGroup = followingGroupService.getByType(UserConstant.USER_FOLLOWING_GROUP_TYPE_DEFAULT);
            userFollowing.setGroupId(followingGroup.getId());
        } else {
            //用户关注up主时设置了分组
            FollowingGroup followingGroup = followingGroupService.getById(groupId);
            if (followingGroup == null) {
                throw new ConditionException("关注分组不存在!");
            }
        }
        //查找用户关注的up主的用户信息
        Long followingId = userFollowing.getFollowingId();
        User user = userService.getUserById(followingId);
        if (user == null) {
            throw new ConditionException("关注的用户不存在!");
        }

        //先删除在添加，完成用户关注up主的操作记录，所以要添加事务，
        //为什么不写个更新操作呢，因为MySQL底层更新操作就是先删除后添加，因为更新后数据记录在数据页中的位置会变，与其移动，还不如按递增顺序插入一条新的记录
        //因为删除个添加是在一个方法里，所以是一次网络传输的时间
        userFollowingDao.deleteUserFollowing(userFollowing.getUserId(), followingId);
        userFollowing.setCreateTime(new Date());
        userFollowingDao.addUserFollowing(userFollowing);
    }

    /**
     * 获取用户关注的所有up主信息
     * 1.获取关注的up主列表
     * 2.获取关注的up主的UserInfo列表
     * 3.获取用户的分组列表
     * 4.将关注的up主的UserInfo按分组分好，以链表的形式存储
     * @param userId
     * @return
     */
    public List<FollowingGroup> getUserFollowings(Long userId) {
        List<UserFollowing> list = userFollowingDao.getUserFollowings(userId);
        Set<Long> followingIdSet = list.stream()
                .map(UserFollowing::getFollowingId)
                .collect(Collectors.toSet());
        //获取关注的up主的UserInfo信息，用userInfoList存储
        List<UserInfo> userInfoList = new ArrayList<>();
        if (followingIdSet.size() > 0) {
            userInfoList = userService.getUserInfoByUserIds(followingIdSet);
        }
        for (UserFollowing userFollowing : list) {
            for (UserInfo userInfo : userInfoList) {
                if (userFollowing.getFollowingId().equals(userInfo.getUserId())) {
                    userFollowing.setUserInfo(userInfo);
                }
            }
        }

        //获取用户的所有关注分组，用groupList存储
        List<FollowingGroup> groupList = followingGroupService.getByUserId(userId);
        //创建一个全部关注分组
        FollowingGroup allGroup = new FollowingGroup();
        allGroup.setName(UserConstant.USER_FOLLOWING_GROUP_ALL_NAME);
        allGroup.setFollowingUserInfoList(userInfoList);

        List<FollowingGroup> res = new ArrayList<>();
        res.add(allGroup);

        //获取每个分组的关注up主列表
        for(FollowingGroup group : groupList) {
            List<UserInfo> infoList = new ArrayList<>();
            for (UserFollowing userFollowing : list) {
                if (group.getId().equals(userFollowing.getGroupId())) {
                    infoList.add(userFollowing.getUserInfo());
                }
            }
            group.setFollowingUserInfoList(infoList);
            res.add(group);
        }
        return res;
    }

    public List<UserFollowing> getUserFans(Long userId) {
        //查询followingId=userId的数据记录，即查询关注了userId的用户
        List<UserFollowing> fanList = userFollowingDao.getUserFans(userId);
        Set<Long> fanIdSet = fanList.stream()
                .map(UserFollowing::getUserId)
                .collect(Collectors.toSet());
        List<UserInfo> fanUserInfoList = new ArrayList<>();
        if (fanList.size() > 0) {
            fanUserInfoList = userService.getUserInfoByUserIds(fanIdSet);
        }
        //获取userId关注的用户列表
        List<UserFollowing> followingList = userFollowingDao.getUserFollowings(userId);

        //填充粉丝用户的UserInfo对象，并判断用户和粉丝是否为互关
        for (UserFollowing fan : fanList) {
            for(UserInfo userInfo : fanUserInfoList){
                if(fan.getUserId().equals(userInfo.getUserId())){
                    userInfo.setFollowed(false);
                    fan.setUserInfo(userInfo);
                }
            }
            for(UserFollowing following : followingList){
                if(following.getFollowingId().equals(fan.getUserId())){
                    fan.getUserInfo().setFollowed(true);
                }
            }
        }
        return fanList;
    }

    //添加用户自定义的关注分组
    public Long addUserFollowingGroups(FollowingGroup followingGroup) {
        followingGroup.setCreateTime(new Date());
        followingGroup.setType(UserConstant.USER_FOLLOWING_GROUP_TYPE_USER);
        followingGroupService.addFollowingGroup(followingGroup);
        return followingGroup.getId();
    }

    //获取用户的所有关注分组
    public List<FollowingGroup> getUserFollowingGroups(Long userId) {
        return followingGroupService.getUserFollowingGroups(userId);
    }


    //检查用户列表中用户是否关注userId，如关注，该用户的UserInfo中的followed设为true
    public List<UserInfo> checkFollowingStatus(List<UserInfo> userInfoList, Long userId) {
        List<UserFollowing> userFollowingList = userFollowingDao.getUserFollowings(userId);
        for(UserInfo userInfo : userInfoList){
            userInfo.setFollowed(false);
            for(UserFollowing userFollowing : userFollowingList){
                if(userFollowing.getFollowingId().equals(userInfo.getUserId())){
                    userInfo.setFollowed(true);
                }
            }
        }
        return userInfoList;
    }
}
