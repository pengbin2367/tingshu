package com.atguigu.tingshu.search.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.query.search.AlbumIndexQuery;

public interface SearchService {

    Object channel(Long category1Id);

    Object search(AlbumIndexQuery albumIndexQuery);

    Object completeSuggest(String keywords);

    JSONObject getAlbumDetails(Long albumId);
}
