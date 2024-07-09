package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.mapper.*;
import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.constant.KafkaConstant;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.album.*;
import com.atguigu.tingshu.model.base.BaseEntity;
import com.atguigu.tingshu.model.order.OrderInfo;
import com.atguigu.tingshu.order.client.OrderInfoFeignClient;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumAttributeValueVo;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/***
 * 专辑管理相关的接口类的实现类
 */
@Service
public class AlbumInfoServiceImpl extends ServiceImpl<AlbumInfoMapper, AlbumInfo> implements AlbumInfoService {

    @Autowired
    private AlbumInfoMapper albumInfoMapper;

    @Autowired
    private AlbumAttributeValueMapper albumAttributeValueMapper;

    @Autowired
    private AlbumStatMapper albumStatMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Resource
    private OrderInfoFeignClient orderInfoFeignClient;

    @Autowired
    private TrackInfoMapper trackInfoMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveAlbumInfo(AlbumInfoVo albumInfoVo) {
        // 保存对应的专辑信息
        AlbumInfo albumInfo = new AlbumInfo();
        BeanUtils.copyProperties(albumInfoVo, albumInfo);
        albumInfo.setStatus(SystemConstant.ALBUM_STATUS_NO_PASS);
        albumInfo.setUserId(AuthContextHolder.getUserId());
        if (!save(albumInfo)) {
            throw new GuiguException(201, "保存专辑信息失败");
        }
        Long albumId = albumInfo.getId();
        // 保存对应的标签数据
        saveAlbumAttributeValue(albumInfoVo.getAlbumAttributeValueVoList(), albumId);
        // 初始化专辑统计信息
        initAlbumStat(albumId);
        // 上架数据写入es
        kafkaTemplate.send(KafkaConstant.QUEUE_ALBUM_UPPER, "专辑上架", albumId + "");
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

    @Override
    public Page<AlbumListVo> findUserAlbumPage(Integer page, Integer size, AlbumInfoQuery albumInfoQuery) {
        return albumInfoMapper.selectAlbumListPage(new Page<AlbumListVo>(page, size), albumInfoQuery);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeAlbumInfo(Long albumId) {
        boolean remove = remove(
                new LambdaQueryWrapper<AlbumInfo>()
                        .eq(AlbumInfo::getId, albumId)
                        .eq(AlbumInfo::getUserId, AuthContextHolder.getUserId())
        );
        if (!remove) throw new GuiguException(201, "删除专辑信息失败");
        int delete = albumAttributeValueMapper.delete(new LambdaQueryWrapper<AlbumAttributeValue>().eq(AlbumAttributeValue::getAlbumId, albumId));
        if (delete <= 0) throw new GuiguException(201, "删除专辑标签失败");
        delete = albumStatMapper.delete(new LambdaQueryWrapper<AlbumStat>().eq(AlbumStat::getAlbumId, albumId));
        if (delete <= 0) throw new GuiguException(201, "删除专辑统计信息失败");
        kafkaTemplate.send(KafkaConstant.QUEUE_ALBUM_LOWER, "专辑下架", albumId + "");
    }

    @Override
    public AlbumInfo getAlbumInfoById(Long albumId) {
        AlbumInfo albumInfo = getById(albumId);
        if (albumInfo != null) {
            List<AlbumAttributeValue> albumAttributeValues = albumAttributeValueMapper.selectList(new LambdaQueryWrapper<AlbumAttributeValue>().eq(AlbumAttributeValue::getAlbumId, albumId));
            albumInfo.setAlbumAttributeValueVoList(albumAttributeValues);
        }
        return albumInfo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAlbumInfo(Long albumId, AlbumInfoVo albumInfoVo) {
        AlbumInfo albumInfo = new AlbumInfo();
        BeanUtils.copyProperties(albumInfoVo, albumInfo);
        update(albumInfo, new LambdaQueryWrapper<AlbumInfo>().eq(AlbumInfo::getId, albumId).eq(AlbumInfo::getUserId, AuthContextHolder.getUserId()));
        // 先删除旧标签，再新增标签
        int delete = albumAttributeValueMapper.delete(new LambdaQueryWrapper<AlbumAttributeValue>().eq(AlbumAttributeValue::getAlbumId, albumId));
        if (delete < 0) throw new GuiguException(201, "删除专辑标签失败");
        saveAlbumAttributeValue(albumInfoVo.getAlbumAttributeValueVoList(), albumId);
        if (albumInfo.getIsOpen().equals("0")) {
            kafkaTemplate.send(KafkaConstant.QUEUE_ALBUM_LOWER, "专辑下架", albumId + "");
        } else {
            kafkaTemplate.send(KafkaConstant.QUEUE_ALBUM_UPPER, "专辑上架", albumId + "");
        }
    }

    @Override
    public List<AlbumInfo> findUserAllAlbumList() {
        return list(new LambdaQueryWrapper<AlbumInfo>().eq(AlbumInfo::getUserId, 1L));
    }

    @Override
    public Map<String, Integer> getAlbumStatInfo(Long albumId) {
        List<AlbumStat> albumStats = albumStatMapper.selectList(new LambdaQueryWrapper<AlbumStat>().eq(AlbumStat::getAlbumId, albumId));
        return albumStats.stream().collect(Collectors.toMap(
                AlbumStat::getStatType,
                AlbumStat::getStatNum
        ));
    }

    @Override
    public List<AlbumAttributeValue> getAlbumAttributeValue(Long albumId) {
        return albumAttributeValueMapper.selectList(new LambdaQueryWrapper<AlbumAttributeValue>().eq(AlbumAttributeValue::getAlbumId, albumId));
    }

    @Override
    public void updateAlbumStat(String orderNo) {
        OrderInfo orderInfo = orderInfoFeignClient.getOrderInfo(orderNo);
        if (orderInfo == null) return ;
        String itemType = orderInfo.getItemType();
        if (SystemConstant.ORDER_ITEM_TYPE_ALBUM.equals(itemType)) {
            Long albumId = orderInfo.getOrderDetailList().get(0).getItemId();
            albumStatMapper.updateByOrder(albumId, 1, SystemConstant.ALBUM_STAT_BROWSE);
        } else if (SystemConstant.ORDER_ITEM_TYPE_TRACK.equals(itemType)) {
            Long trackId = orderInfo.getOrderDetailList().get(0).getItemId();
            int size = orderInfo.getOrderDetailList().size();
            TrackInfo trackInfo = trackInfoMapper.selectById(trackId);
            if (trackInfo == null) return ;
            albumStatMapper.updateByOrder(trackInfo.getAlbumId(), size, SystemConstant.ALBUM_STAT_BROWSE);
        }
    }

    @Override
    public void getAlbumInfoByStat(String type) {
        List<AlbumStat> albumStatList = albumStatMapper.selectPage(new Page<>(1, 30), new LambdaQueryWrapper<AlbumStat>().eq(AlbumStat::getStatType, type)).getRecords();
        Set<Long> collect = albumStatList.stream().map(AlbumStat::getAlbumId).collect(Collectors.toSet());
        List<AlbumInfo> albumInfos = albumInfoMapper.selectList(new LambdaQueryWrapper<AlbumInfo>().in(BaseEntity::getId, collect));
        redisTemplate.opsForValue().set("Album_Rank_" + type, albumInfos, 1, TimeUnit.DAYS);
    }
}
