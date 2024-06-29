package com.atguigu.tingshu.album.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.common.login.GuiguLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Parameter;
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

	@GuiguLogin
	@PostMapping("/uploadTrack")
	public Result<JSONObject> uploadTrack(@RequestParam MultipartFile file) {
		return Result.ok(trackInfoService.uploadTrack(file));
	}

	@GuiguLogin
	@PostMapping("/saveTrackInfo")
	public Result saveTrackInfo(@RequestBody @Validated TrackInfoVo trackInfoVo) {
		//	调用服务层方法
		trackInfoService.saveTrackInfo(trackInfoVo);
		return Result.ok();
	}

	@GuiguLogin
	@PostMapping("/findUserTrackPage/{page}/{size}")
	public Result<IPage<TrackListVo>> findUserTrackPage(@Parameter(name = "page",description = "当前页面",required = true)
														@PathVariable Long page,
														@Parameter(name = "size",description = "每页记录数",required = true)
														@PathVariable Long size,
														@Parameter(name = "trackInfoQuery",description = "查询对象",required = false)
														@RequestBody TrackInfoQuery trackInfoQuery){
		return Result.ok(trackInfoService.findUserTrackPage(page, size, trackInfoQuery));
	}

	@GuiguLogin
	@DeleteMapping("/removeTrackInfo/{id}")
	public Result removeTrackInfo(@PathVariable("id") Long id) {
		trackInfoService.removeTrackInfo(id);
		return Result.ok();
	}

	@GuiguLogin
	@GetMapping("/getTrackInfo/{id}")
	public Result<TrackInfo> getTrackInfo(@PathVariable Long id) {
		return Result.ok(trackInfoService.getById(id));
	}

	@GuiguLogin
	@PutMapping("/updateTrackInfo/{id}")
	public Result updateById(@PathVariable Long id, @RequestBody @Validated TrackInfoVo trackInfoVo) {
		trackInfoService.updateTrackInfo(id, trackInfoVo);
		return Result.ok();
	}
}

