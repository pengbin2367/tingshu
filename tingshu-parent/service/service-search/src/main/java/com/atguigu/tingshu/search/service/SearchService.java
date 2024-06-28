package com.atguigu.tingshu.search.service;

import com.atguigu.tingshu.query.search.AlbumIndexQuery;

public interface SearchService {

    Object channel(Long category1Id);

    Object search(AlbumIndexQuery albumIndexQuery);
}
