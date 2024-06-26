package com.atguigu.tingshu.search.dao;

import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumInfoIndexDao extends ElasticsearchRepository<AlbumInfoIndex, Long> {
}
