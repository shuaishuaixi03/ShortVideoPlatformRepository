package com.wcx.video.domain.auth;


import lombok.Data;

import java.util.Date;

@Data
public class AuthRoleElementOperation {
    private Long id;

    //角色id
    private Long roleId;

    //元素操作id
    private Long elementOperationId;

    private Date createTime;

    //VO层
    private AuthElementOperation authElementOperation;
}
