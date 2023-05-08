package com.wcx.video.dao;

import com.wcx.video.domain.auth.UserRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserRoleDao {

    /**
     * 根据userId查询用户的角色，以链表形式返回
     * @param userId
     * @return
     */
    List<UserRole> getUserRoleByUserId(Long userId);

    Integer addUserRole(UserRole userRole);
}
