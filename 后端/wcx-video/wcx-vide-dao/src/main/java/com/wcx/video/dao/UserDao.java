package com.wcx.video.dao;

import com.alibaba.fastjson.JSONObject;
import com.wcx.video.domain.RefreshTokenDetail;
import com.wcx.video.domain.User;
import com.wcx.video.domain.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Mapper
public interface UserDao {

    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return User 用户
     */
    User getUserByPhone(String phone);

    /**
     * 添加用户
     * @param user
     * @return 影响的行数
     */
    Integer addUser(User user);

    /**
     * 添加用户信息
     * @param userInfo
     * @return 影响的行数
     */
    Integer addUserInfo(UserInfo userInfo);


    /**
     * 根据id查询用户
     * @param id
     * @return User 用户
     */
    User getUserById(Long id);

    /**
     * 根据t_user的id在t_user_info查找对应的UserInfo
     * @param userId
     * @return
     */
    UserInfo getUserInfoByUserId(Long userId);


    Integer updateUserInfos(UserInfo userInfo);

    List<UserInfo> getUserInfoByUserIds(@Param("userIdList") Set<Long> userIdList);

    Integer pageCountUserInfos(JSONObject params);

    List<UserInfo> pageListUserInfos(JSONObject params);

    User getUserByPhoneOrEmail(@Param("phone") String phone, @Param("email") String email);

    Integer deleteRefreshTokenByUserId(Long userId);

    Integer addRefreshToken(@Param("refreshToken")String refreshToken,
                            @Param("userId") Long userId,
                            @Param("createTime") Date createTime);

    Integer deleteRefreshToken(@Param("refreshToken") String refreshToken,
                               @Param("userId") Long userId);

    RefreshTokenDetail getRefreshTokenDetail(String refreshToken);

    /**
     * 根据userId集合批量查询用户基本信息
     * @param userIdSet
     * @return
     */
    List<UserInfo> batchGetUserInfoByUserIds(@Param("userIdSet") Set<Long> userIdSet);

    String getRefreshTokenByUserId(Long userId);
}
