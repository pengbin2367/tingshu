package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.mapper.AlbumAttributeValueMapper;
import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.mapper.AlbumStatMapper;
import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.AlbumStat;
import com.atguigu.tingshu.vo.album.AlbumAttributeValueVo;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/***
 * 专辑管理相关的接口类的实现类
 */
@Service
public class AlbumInfoServiceImpl extends ServiceImpl<AlbumInfoMapper, AlbumInfo> implements AlbumInfoService {

    @Autowired
    private AlbumAttributeValueMapper albumAttributeValueMapper;

    @Autowired
    private AlbumStatMapper albumStatMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveAlbumInfo(AlbumInfoVo albumInfoVo) {
        // 保存对应的专辑信息
        AlbumInfo albumInfo = new AlbumInfo();
        BeanUtils.copyProperties(albumInfoVo, albumInfo);
        albumInfo.setStatus(SystemConstant.ALBUM_STATUS_NO_PASS);
        // TODO 用户系统完成后，改为真实用户
        albumInfo.setUserId(10086L);
        if (!save(albumInfo)) {
            throw new GuiguException(201, "保存专辑信息失败");
        }
        Long albumId = albumInfo.getId();
        // 保存对应的标签数据
        saveAlbumAttributeValue(albumInfoVo.getAlbumAttributeValueVoList(), albumId);
        // 初始化专辑统计信息
        initAlbumStat(albumId);
    }

    private void initAlbumStat(Long albumId) {
        AlbumStat albumStat = new AlbumStat();
        albumStat.setAlbumId(albumId);
        albumStat.setStatType(SystemConstant.ALBUM_STAT_PLAY);
        int insert1 = albumStatMapper.insert(albumStat);
        albumStat.setId(null);
        albumStat.setStatType(SystemConstant.ALBUM_STAT_SUBSCRIBE);
        int insert2 = albumStatMapper.insert(albumStat);
        albumStat.setId(null);
        albumStat.setStatType(SystemConstant.ALBUM_STAT_BROWSE);
        int insert3 = albumStatMapper.insert(albumStat);
        albumStat.setId(null);
        albumStat.setStatType(SystemConstant.ALBUM_STAT_COMMENT);
        int insert4 = albumStatMapper.insert(albumStat);
        if (insert1 <= 0 || insert2 <= 0 || insert3 <= 0 || insert4 <= 0) {
            throw new GuiguException(201, "初始化专辑统计信息失败");
        }
    }

    private void saveAlbumAttributeValue(List<AlbumAttributeValueVo> albumAttributeValueVoList, Long albumId) {
        albumAttributeValueVoList.forEach(albumAttributeValueVo -> {
            AlbumAttributeValue albumAttributeValue = new AlbumAttributeValue();
            BeanUtils.copyProperties(albumAttributeValueVo, albumAttributeValue);
            albumAttributeValue.setAlbumId(albumId);
            int insert = albumAttributeValueMapper.insert(albumAttributeValue);
            if (insert <= 0) {
                throw new GuiguException(201, "保存专辑标签失败");
            }
        });
    }
}
