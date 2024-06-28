package com.atguigu.tingshu.search.service.impl;

import com.atguigu.tingshu.album.client.AlbumInfoFeignClient;
import com.atguigu.tingshu.album.client.CategoryFeignClient;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.util.PinYinUtils;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import com.atguigu.tingshu.model.search.AttributeValueIndex;
import com.atguigu.tingshu.model.search.SuggestIndex;
import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.search.dao.AlbumInfoIndexDao;
import com.atguigu.tingshu.search.dao.SuggestIndexDao;
import com.atguigu.tingshu.search.service.ItemService;
import com.atguigu.tingshu.user.client.UserInfoFeignClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class ItemServiceImpl implements ItemService {

    @Autowired
    private AlbumInfoIndexDao albumInfoIndexDao;

    @Autowired
    private SuggestIndexDao suggestIndexDao;

    @Resource
    private AlbumInfoFeignClient albumInfoFeignClient;

    @Resource
    private UserInfoFeignClient userInfoFeignClient;

    @Resource
    private CategoryFeignClient categoryFeignClient;

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
        // 查询作者名字
        UserInfo userInfo = userInfoFeignClient.getUserInfo(albumInfo.getUserId());
        if (null == userInfo) {
            albumInfoIndex.setAnnouncerName(albumInfo.getUserId().toString());
        } else {
            albumInfoIndex.setAnnouncerName(userInfo.getNickname());
        }
        albumInfoIndex.setCoverUrl(albumInfo.getCoverUrl());
        albumInfoIndex.setIncludeTrackCount(albumInfo.getIncludeTrackCount());
        albumInfoIndex.setIsFinished(albumInfo.getIsFinished().toString());
        albumInfoIndex.setPayType(albumInfo.getPayType());
        albumInfoIndex.setCreateTime(new Date());
        // 查询分类信息
        BaseCategoryView baseCategoryView = categoryFeignClient.getBaseCategoryView(albumInfo.getCategory3Id());
        albumInfoIndex.setCategory1Id(baseCategoryView.getCategory1Id());
        albumInfoIndex.setCategory2Id(baseCategoryView.getCategory2Id());
        albumInfoIndex.setCategory3Id(albumInfo.getCategory3Id());
        // 查询专辑统计信息
        Map<String, Integer> albumStatInfo = albumInfoFeignClient.getAlbumStatInfo(albumId);
        albumInfoIndex.setPlayStatNum(albumStatInfo.get(SystemConstant.ALBUM_STAT_PLAY));
        albumInfoIndex.setSubscribeStatNum(albumStatInfo.get(SystemConstant.ALBUM_STAT_SUBSCRIBE));
        albumInfoIndex.setBuyStatNum(albumStatInfo.get(SystemConstant.ALBUM_STAT_BROWSE));
        albumInfoIndex.setCommentStatNum(albumStatInfo.get(SystemConstant.ALBUM_STAT_COMMENT));
        albumInfoIndex.setHotScore(0d);
        // 查询专辑标签
        List<AlbumAttributeValue> albumAttributeValues = albumInfoFeignClient.getAlbumAttributeValue(albumId);
        List<AttributeValueIndex> attributeValueIndexList = albumAttributeValues.stream().map(albumAttributeValue -> {
            AttributeValueIndex attributeValueIndex = new AttributeValueIndex();
            attributeValueIndex.setAttributeId(albumAttributeValue.getAttributeId());
            attributeValueIndex.setValueId(albumAttributeValue.getValueId());
            return attributeValueIndex;
        }).collect(Collectors.toList());
        albumInfoIndex.setAttributeValueIndexList(attributeValueIndexList);
        albumInfoIndexDao.save(albumInfoIndex);
        // 存储搜索提示词
        saveSuggestInfo(albumInfoIndex.getAlbumTitle());
        saveSuggestInfo(albumInfoIndex.getAlbumIntro());
        saveSuggestInfo(albumInfoIndex.getAnnouncerName());
    }

    private void saveSuggestInfo(String title) {
        try {
            SuggestIndex suggestIndex = new SuggestIndex();
            suggestIndex.setId(UUID.randomUUID().toString().replaceAll("-", ""));
            suggestIndex.setTitle(title);
            suggestIndex.setKeyword(new Completion(new String[]{title}));
            suggestIndex.setKeywordPinyin(new Completion(new String[]{PinYinUtils.toHanyuPinyin(title)}));
            suggestIndex.setKeywordSequence(new Completion(new String[]{PinYinUtils.getFirstLetter(title)}));
            suggestIndexDao.save(suggestIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeAlbumFromEs(Long albumId) {
        albumInfoIndexDao.deleteById(albumId);
    }
}
