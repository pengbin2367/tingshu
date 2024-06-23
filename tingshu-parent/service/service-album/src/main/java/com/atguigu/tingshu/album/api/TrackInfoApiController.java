package com.atguigu.tingshu.album.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "声音管理")
@RestController
@RequestMapping("/api/album/trackInfo")
@SuppressWarnings({"unchecked", "rawtypes"})
public class TrackInfoApiController {

	@Autowired
	private TrackInfoService trackInfoService;

	@PostMapping("/uploadTrack")
	public Result<JSONObject> uploadTrack(@RequestParam MultipartFile file) {
		return Result.ok(trackInfoService.uploadTrack(file));
	}

	@PostMapping("/saveTrackInfo")
	public Result saveTrackInfo(@RequestBody @Validated TrackInfoVo trackInfoVo) {
		//	调用服务层方法
		trackInfoService.saveTrackInfo(trackInfoVo, 10086L);
		return Result.ok();
	}
}

