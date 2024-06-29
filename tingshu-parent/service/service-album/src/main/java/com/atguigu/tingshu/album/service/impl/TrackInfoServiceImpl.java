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
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.common.util.UploadFileUtil;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.model.album.TrackStat;
import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.user.client.UserInfoFeignClient;
import com.atguigu.tingshu.vo.album.AlbumTrackListVo;
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
import com.tencentcloudapi.vod.v20180717.models.DeleteMediaRequest;
import com.tencentcloudapi.vod.v20180717.models.DescribeMediaInfosRequest;
import com.tencentcloudapi.vod.v20180717.models.DescribeMediaInfosResponse;
import com.tencentcloudapi.vod.v20180717.models.MediaInfo;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    @Resource
    private UserInfoFeignClient userInfoFeignClient;

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
	public void saveTrackInfo(TrackInfoVo trackInfoVo) {
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
		trackInfo.setUserId(AuthContextHolder.getUserId());
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
		boolean remove = remove(new LambdaQueryWrapper<TrackInfo>().eq(TrackInfo::getId, id).eq(TrackInfo::getUserId, AuthContextHolder.getUserId()));
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
			trackInfo.setUserId(AuthContextHolder.getUserId());
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

	@Override
	public IPage<AlbumTrackListVo> findAlbumTrackPage(Long albumId, Long page, Long size) {
		IPage<AlbumTrackListVo> result = null;
		IPage<AlbumTrackListVo> albumTrackListVoIPage = trackInfoMapper.selectAlbumTrancPage(new Page<>(page, size), albumId);
		// 获取专辑的类型
		AlbumInfo albumInfo = albumInfoMapper.selectById(albumId);
		// 获取专辑的免费集数
		Integer tracksForFree = albumInfo.getTracksForFree();
		// 判断类型：0101-免费、0102-vip免费、0103-付费
		switch (albumInfo.getPayType()) {
			case SystemConstant.ALBUM_PAY_TYPE_FREE -> result = albumTrackListVoIPage;
			case SystemConstant.ALBUM_PAY_TYPE_VIPFREE -> result = albumTrackVipFree(albumTrackListVoIPage, tracksForFree);
			case SystemConstant.ALBUM_PAY_TYPE_REQUIRE -> result = albumTrackNotFree(albumTrackListVoIPage, tracksForFree, albumInfo.getPriceType(), albumId);
		}
		return result;
	}

	private IPage<AlbumTrackListVo> albumTrackVipFree(IPage<AlbumTrackListVo> albumTrackListVoIPage, Integer tracksForFree) {
		List<AlbumTrackListVo> albumTrackListVoList = albumTrackListVoIPage.getRecords();
		// 获取用户信息
		Long userId = AuthContextHolder.getUserId();
		UserInfo userInfo = userInfoFeignClient.getUserInfo(userId);
		// 非vip用户，无法观看orderNum > tracksForFree
		if (userInfo.getIsVip().equals(0)) {
			List<AlbumTrackListVo> albumTrackListVoListNew = albumTrackListVoList.stream().peek(albumTrackListVo -> {
				if (albumTrackListVo.getOrderNum() > tracksForFree) {
					albumTrackListVo.setIsShowPaidMark(true);
				}
            }).toList();
			albumTrackListVoIPage.setRecords(albumTrackListVoListNew);
		}
		return albumTrackListVoIPage;
	}

	private IPage<AlbumTrackListVo> albumTrackNotFree(IPage<AlbumTrackListVo> albumTrackListVoIPage, Integer tracksForFree, String priceType, Long albumId) {
		List<AlbumTrackListVo> albumTrackListVoList = albumTrackListVoIPage.getRecords();
		if (priceType.equals(SystemConstant.ALBUM_PRICE_TYPE_ONE)) {
			// 单集购买
			Map<String, String> userTrackIds = userInfoFeignClient.getUserTrackIds(albumId);
			List<AlbumTrackListVo> albumTrackListVoListNew = albumTrackListVoList.stream().peek(albumTrackListVo -> {
				if (albumTrackListVo.getOrderNum() > tracksForFree &&
						StringUtils.isEmpty(userTrackIds.get(albumTrackListVo.getTrackId().toString()))) {
					albumTrackListVo.setIsShowPaidMark(true);
				}
            }).toList();
			albumTrackListVoIPage.setRecords(albumTrackListVoListNew);
		} else {
			// 整专购买
			if (!userInfoFeignClient.getUserIsBuyAlbum(albumId)) {
				List<AlbumTrackListVo> albumTrackListVoListNew = albumTrackListVoList.stream().peek(albumTrackListVo -> {
					if (albumTrackListVo.getOrderNum() > tracksForFree) {
						albumTrackListVo.setIsShowPaidMark(true);
					}
				}).toList();
				albumTrackListVoIPage.setRecords(albumTrackListVoListNew);
			}
		}
		return albumTrackListVoIPage;
	}
}
