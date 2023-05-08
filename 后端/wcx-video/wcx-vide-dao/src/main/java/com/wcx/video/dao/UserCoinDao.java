package com.wcx.video.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

@Mapper
public interface UserCoinDao {

    /**
     * 查询userId的硬币数
     * @param userId
     * @return
     */
    Integer getUserCoinsAmount(Long userId);

    /**
     *
     * @param userId
     * @param amount
     * @param updateTime
     */
    Integer updateUserCoinAmount(@Param("userId") Long userId,
                              @Param("amount") Integer amount,
                              @Param("updateTime") Date updateTime);
}
