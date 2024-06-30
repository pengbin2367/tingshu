package com.atguigu.tingshu.search.service;

import com.atguigu.tingshu.query.search.AlbumIndexQuery;

import java.util.Map;

public interface SearchService {

    Object channel(Long category1Id);

    Object search(AlbumIndexQuery albumIndexQuery);

    Object completeSuggest(String keywords);

    Map<String, Object> getAlbumDetails(Long albumId);
}
