package com.wcx.video.api;

import com.wcx.video.domain.JsonResponse;
import com.wcx.video.service.ElasticSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SystemApi {

    @Autowired
    private ElasticSearchService elasticSearchService;

    @GetMapping("/contents")
    public JsonResponse<List<Map<String, Object>>> getContents(@RequestParam String keyword,
                                                          @RequestParam Integer pageNo,
                                                          @RequestParam Integer pageSize) throws IOException {
        List<Map<String, Object>> list = elasticSearchService.getContents(keyword, pageNo, pageSize);
        return new JsonResponse<>(list);
    }

}
