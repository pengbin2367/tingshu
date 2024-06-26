package com.atguigu.tingshu.search.service.impl;

import com.atguigu.tingshu.album.client.AlbumInfoFeignClient;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import com.atguigu.tingshu.search.dao.AlbumInfoIndexDao;
import com.atguigu.tingshu.search.service.ItemService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class ItemServiceImpl implements ItemService {

    @Autowired
    private AlbumInfoIndexDao albumInfoIndexDao;

    @Resource
    private AlbumInfoFeignClient albumInfoFeignClient;

    @Override
    public void addAlbumFromDbToEs(Long albumId) {
        AlbumInfo albumInfo = albumInfoFeignClient.getAlbumInfo(albumId);
        if (albumInfo == null) {
            throw new GuiguException(201, "专辑不存在");
        }
        AlbumInfoIndex albumInfoIndex = new AlbumInfoIndex();
        albumInfoIndex.setId(albumInfo.getId());
        albumInfoIndex.setAlbumTitle(albumInfo.getAlbumTitle());
        albumInfoIndex.setAlbumIntro(albumInfo.getAlbumIntro());
        // TODO 查询作者名字
        albumInfoIndex.setAnnouncerName(albumInfo.getUserId().toString());
        albumInfoIndex.setCoverUrl(albumInfo.getCoverUrl());
        albumInfoIndex.setIncludeTrackCount(albumInfo.getIncludeTrackCount());
        albumInfoIndex.setIsFinished(albumInfo.getIsFinished().toString());
        albumInfoIndex.setPayType(albumInfo.getPayType());
        albumInfoIndex.setCreateTime(new Date());
        // TODO 查询分类信息
        albumInfoIndex.setCategory1Id(1L);
        albumInfoIndex.setCategory2Id(2L);
        albumInfoIndex.setCategory3Id(albumInfo.getCategory3Id());
        // TODO 查询专辑统计信息
        albumInfoIndex.setPlayStatNum(0);
        albumInfoIndex.setSubscribeStatNum(0);
        albumInfoIndex.setBuyStatNum(0);
        albumInfoIndex.setCommentStatNum(0);
        albumInfoIndex.setHotScore(0d);
        // TODO 查询专辑标签
        albumInfoIndex.setAttributeValueIndexList(null);
        albumInfoIndexDao.save(albumInfoIndex);
    }

    @Override
    public void removeAlbumFromEs(Long albumId) {
        albumInfoIndexDao.deleteById(albumId);
    }
}
