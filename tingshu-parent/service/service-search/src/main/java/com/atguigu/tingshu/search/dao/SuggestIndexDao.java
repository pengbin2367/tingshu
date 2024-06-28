package com.atguigu.tingshu.search.dao;

import com.atguigu.tingshu.model.search.SuggestIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuggestIndexDao extends ElasticsearchRepository<SuggestIndex, String> {
}
