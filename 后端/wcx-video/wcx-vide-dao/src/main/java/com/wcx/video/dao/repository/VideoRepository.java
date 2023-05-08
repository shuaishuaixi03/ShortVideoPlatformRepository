package com.wcx.video.dao.repository;


import com.wcx.video.domain.Video;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface VideoRepository extends ElasticsearchRepository<Video, Long> {
    Video findByTitleLike(String keyword);
}
