package com.atguigu.tingshu.album.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

public interface TrackInfoService extends IService<TrackInfo> {

    JSONObject uploadTrack(MultipartFile file);

    void saveTrackInfo(TrackInfoVo trackInfoVo, Long userId);
}
