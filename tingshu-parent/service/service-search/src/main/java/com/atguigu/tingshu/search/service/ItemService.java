package com.atguigu.tingshu.search.service;

public interface ItemService {

    void addAlbumFromDbToEs(Long albumId);

    void removeAlbumFromEs(Long albumId);
}
