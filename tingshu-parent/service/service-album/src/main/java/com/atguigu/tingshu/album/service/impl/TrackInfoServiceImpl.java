package com.atguigu.tingshu.album.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.config.VodConstantProperties;
import com.atguigu.tingshu.album.mapper.TrackInfoMapper;
import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.common.util.UploadFileUtil;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qcloud.vod.VodUploadClient;
import com.qcloud.vod.model.VodUploadRequest;
import com.qcloud.vod.model.VodUploadResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class TrackInfoServiceImpl extends ServiceImpl<TrackInfoMapper, TrackInfo> implements TrackInfoService {

	@Autowired
	private TrackInfoMapper trackInfoMapper;

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
}
