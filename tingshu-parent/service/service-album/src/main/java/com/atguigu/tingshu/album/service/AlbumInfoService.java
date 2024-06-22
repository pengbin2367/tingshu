package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/***
 * 专辑管理相关的接口类
 */
public interface AlbumInfoService extends IService<AlbumInfo> {

    void saveAlbumInfo(AlbumInfoVo albumInfoVo);

    Page<AlbumListVo> findUserAlbumPage(Integer page, Integer size, AlbumInfoQuery albumInfoQuery);

    void removeAlbumInfo(Long albumId);

    AlbumInfo getAlbumInfoById(Long albumId);

    void updateAlbumInfo(Long albumId, AlbumInfoVo albumInfoVo);
}
