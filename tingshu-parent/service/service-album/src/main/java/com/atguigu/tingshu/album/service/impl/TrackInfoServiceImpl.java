package com.atguigu.tingshu.album.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.config.VodConstantProperties;
import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.mapper.TrackInfoMapper;
import com.atguigu.tingshu.album.mapper.TrackStatMapper;
import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.common.util.UploadFileUtil;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.model.album.TrackStat;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qcloud.vod.VodUploadClient;
import com.qcloud.vod.model.VodUploadRequest;
import com.qcloud.vod.model.VodUploadResponse;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.vod.v20180717.VodClient;
import com.tencentcloudapi.vod.v20180717.models.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class TrackInfoServiceImpl extends ServiceImpl<TrackInfoMapper, TrackInfo> implements TrackInfoService {

	@Autowired
	private TrackInfoMapper trackInfoMapper;

	@Autowired
	private AlbumInfoMapper albumInfoMapper;

	@Autowired
	private TrackStatMapper trackStatMapper;

	@Autowired
	private VodConstantProperties vodConstantProperties;

	@SneakyThrows
	@Override
	public JSONObject uploadTrack(MultipartFile file) {
		VodUploadClient vodUploadClient = new VodUploadClient(vodConstantProperties.getSecretId(), vodConstantProperties.getSecretKey());
		VodUploadRequest request = new VodUploadRequest();
		String tempPath = UploadFileUtil.uploadTempPath(vodConstantProperties.getTempPath(), file);
		request.setMediaFilePath(tempPath);
		VodUploadResponse response = vodUploadClient.upload(vodConstantProperties.getRegion(), request);
		JSONObject result = new JSONObject();
		result.put("mediaFileId", response.getFileId());
		result.put("mediaUrl", response.getMediaUrl());
		return result;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void saveTrackInfo(TrackInfoVo trackInfoVo, Long userId) {
		String mediaFileId = trackInfoVo.getMediaFileId();
		DescribeMediaInfosResponse response = getVodFileInfo(mediaFileId);
		if (response == null || response.getMediaInfoSet() == null) {
			throw new GuiguException(201, "获取腾讯云声音信息失败");
		}
		TrackInfo trackInfo = new TrackInfo();
		BeanUtils.copyProperties(trackInfoVo, trackInfo);
		MediaInfo mediaInfo = response.getMediaInfoSet()[0];
		trackInfo.setMediaDuration(new BigDecimal(mediaInfo.getMetaData().getDuration().toString()));
		trackInfo.setMediaSize(mediaInfo.getMetaData().getSize());
		trackInfo.setMediaType(mediaInfo.getBasicInfo().getType());
		trackInfo.setMediaFileId(mediaFileId);
		trackInfo.setUserId(userId);
		if (!save(trackInfo)) {
			throw new GuiguException(201, "保存声音失败");
		}
		Long trackInfoId = trackInfo.getId();
		initTrackInfoStat(trackInfoId);
		albumInfoMapper.updateAlbumTrackCount(trackInfo.getAlbumId(), 1);
	}

	@SneakyThrows
    private DescribeMediaInfosResponse getVodFileInfo(String mediaFileId) {
		Credential credential = new Credential(vodConstantProperties.getSecretId(), vodConstantProperties.getSecretKey());
		VodClient vodClient = new VodClient(credential, vodConstantProperties.getRegion());
		DescribeMediaInfosRequest request = new DescribeMediaInfosRequest();
		request.setFileIds(new String[]{mediaFileId});
		return vodClient.DescribeMediaInfos(request);
	}

	private void initTrackInfoStat(Long trackInfoId) {
		TrackStat trackStat = new TrackStat();
		trackStat.setTrackId(trackInfoId);
		trackStat.setStatType(SystemConstant.TRACK_STAT_PLAY);
		trackStatMapper.insert(trackStat);
		trackStat.setId(null);
		trackStat.setStatType(SystemConstant.TRACK_STAT_COLLECT);
		trackStatMapper.insert(trackStat);
		trackStat.setId(null);
		trackStat.setStatType(SystemConstant.TRACK_STAT_PRAISE);
		trackStatMapper.insert(trackStat);
		trackStat.setId(null);
		trackStat.setStatType(SystemConstant.TRACK_STAT_COMMENT);
		trackStatMapper.insert(trackStat);
	}

	@Override
	public IPage<TrackListVo> findUserTrackPage(Long page, Long size, TrackInfoQuery trackInfoQuery) {
		return trackInfoMapper.selectUserTrackPage(new Page<>(page, size), trackInfoQuery);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void removeTrackInfo(Long id) {
		TrackInfo trackInfo = getById(id);
		Long albumId = trackInfo.getAlbumId();
		// TODO 用户系统完成后，改为真实用户
		boolean remove = remove(new LambdaQueryWrapper<TrackInfo>().eq(TrackInfo::getId, id).eq(TrackInfo::getUserId, 10086L));
		if (!remove) throw new GuiguException(201, "删除声音信息失败");
		int delete = trackStatMapper.delete(new LambdaQueryWrapper<TrackStat>().eq(TrackStat::getTrackId, id));
		if (delete <= 0) throw new GuiguException(201, "删除声音统计信息失败");
		albumInfoMapper.updateAlbumTrackCount(albumId, -1);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateTrackInfo(Long id, TrackInfoVo trackInfoVo) {
		//	获取到声音对象
		TrackInfo trackInfo = this.getById(id);
		//  获取传递的fileId
		String mediaFileId = trackInfo.getMediaFileId();
		//	进行属性拷贝
		BeanUtils.copyProperties(trackInfoVo, trackInfo);
		//	获取声音信息 页面传递的fileId 与 数据库的 fileId 不相等就修改
		if (!trackInfoVo.getMediaFileId().equals(mediaFileId)) {
			deleteMediaFileId(trackInfo.getMediaFileId());
			//	说明已经修改过了.
			DescribeMediaInfosResponse response = getVodFileInfo(trackInfoVo.getMediaFileId());
			//	判断对象不为空.
			if (null == response || response.getMediaInfoSet() == null){
				//	抛出异常
				throw new GuiguException(ResultCodeEnum.VOD_FILE_ID_ERROR);
			}
			MediaInfo mediaInfo = response.getMediaInfoSet()[0];
			trackInfo.setMediaFileId(trackInfoVo.getMediaFileId());
			trackInfo.setMediaUrl(mediaInfo.getBasicInfo().getMediaUrl());
			trackInfo.setMediaType(mediaInfo.getBasicInfo().getType());
			trackInfo.setMediaDuration(BigDecimal.valueOf(mediaInfo.getMetaData().getDuration()));
			trackInfo.setMediaSize(mediaInfo.getMetaData().getSize());
		}
		//	修改数据
		this.updateById(trackInfo);
	}

	@SneakyThrows
    private void deleteMediaFileId(String mediaFileId) {
		Credential credential = new Credential(vodConstantProperties.getSecretId(), vodConstantProperties.getSecretKey());
		VodClient vodClient = new VodClient(credential, vodConstantProperties.getRegion());
		DeleteMediaRequest request = new DeleteMediaRequest();
		request.setFileId(mediaFileId);
		vodClient.DeleteMedia(request);
	}
}
