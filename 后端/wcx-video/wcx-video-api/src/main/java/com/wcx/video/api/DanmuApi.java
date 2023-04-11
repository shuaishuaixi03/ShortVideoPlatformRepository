package com.wcx.video.api;


import com.wcx.video.api.support.UserSupport;
import com.wcx.video.domain.Danmu;
import com.wcx.video.domain.JsonResponse;
import com.wcx.video.service.DanmuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DanmuApi {

    @Autowired
    private DanmuService danmuService;

    @Autowired
    private UserSupport userSupport;

    @GetMapping("/danmus")
    public JsonResponse<List<Danmu>> getDanmus(@RequestParam Long videoId,
                                               String startTime,
                                               String endTime) throws Exception {
        List<Danmu> list;
        try {
            userSupport.getCurrentUserId();
            list = danmuService.getDanmus(videoId, startTime, endTime);
        } catch (Exception ignored) {
            list = danmuService.getDanmus(videoId, null, null);
        }
        return new JsonResponse<>(list);
    }

}
