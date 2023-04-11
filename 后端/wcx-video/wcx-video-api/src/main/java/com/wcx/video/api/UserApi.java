package com.wcx.video.api;


import com.alibaba.fastjson.JSONObject;
import com.wcx.video.api.support.UserSupport;
import com.wcx.video.domain.JsonResponse;
import com.wcx.video.domain.PageResult;
import com.wcx.video.domain.User;
import com.wcx.video.domain.UserInfo;
import com.wcx.video.service.UserFollowingService;
import com.wcx.video.service.UserService;
import com.wcx.video.service.utils.RSAUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
public class UserApi {

    @Autowired
    private UserService userService;

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserFollowingService userFollowingService;

    //向接收方返回服务端的公钥
    @GetMapping("/rsa-pks")
    public JsonResponse<String> getRsaPublicKey() {
        String publicKey = RSAUtil.getPublicKeyStr();
        return new JsonResponse<>(publicKey);
    }

    //用户获取用户基本信息接口
    @GetMapping("/users")
    public JsonResponse<User> getUserInfo() {
        Long userId = userSupport.getCurrentUserId();
        User user = userService.getUserInfo(userId);
        return new JsonResponse<>(user);
    }

    //用户注册接口
    @PostMapping("/users")
    public JsonResponse<String> addUser(@RequestBody User user) {
        userService.addUser(user);
        return JsonResponse.success();
    }

    //用户登录接口，返回一个有过期时间的token
    @PostMapping("/user-tokens")
    public JsonResponse<String> login(@RequestBody User user) throws Exception {
        String token = userService.login(user);
        return new JsonResponse<>(token);
    }

    //更新用户的UserInfo信息
    @PutMapping("/user-infos")
    public JsonResponse<String> updateUserInfos(@RequestBody UserInfo userInfo) {
        Long userId = userSupport.getCurrentUserId();
        userInfo.setUserId(userId);
        userService.updateUserInfo(userInfo);
        return JsonResponse.success();
    }

    //根据用户昵称进行模糊查询，返回所有UserInfo信息,并显示用户是否关注自己
    @GetMapping("/user-infos")
    public JsonResponse<PageResult<UserInfo>> pageListUserInfos(@RequestParam Integer no,
                                                                @RequestParam Integer size,
                                                                String nick) {
        Long userId = userSupport.getCurrentUserId();
        JSONObject params = new JSONObject();
        //no是分页的页数，从no页开始
        params.put("no", no);
        //size是每页有多少条记录
        params.put("size", size);
        //nick是要查询的用户昵称
        params.put("nick", nick);
        params.put("userId", userId);
        PageResult<UserInfo> res = userService.pageListUserInfos(params);
        if (res.getTotal() > 0) {
            List<UserInfo> checkedUserInfoList = userFollowingService.checkFollowingStatus(res.getList(), userId);
            res.setList(checkedUserInfoList);
        }
        return new JsonResponse<>(res);
    }

    //双Token登录接口
    @PostMapping("/user-dts")
    public JsonResponse<Map<String, Object>> loginForDts(@RequestBody User user) throws Exception {
        Map<String, Object> map = userService.loginForDts(user);
        return new JsonResponse<>(map);
    }

    //退出接口
    @DeleteMapping("/refresh-tokens")
    public JsonResponse<String> logout(HttpServletRequest request) {
        String refreshToken = request.getHeader("refreshToken");
        Long userId = userSupport.getCurrentUserId();
        userService.logout(refreshToken, userId);
        return JsonResponse.success();
    }

    //根据刷新令牌放回一个新的token
    @PostMapping("/access-tokens")
    public JsonResponse<String> refreshAccessToken(HttpServletRequest request) throws Exception {
        String refreshToken = request.getHeader("refreshToken");
        String accessToken = userService.refreshAccessToken(refreshToken);
        return new JsonResponse<>(accessToken);
    }

}
