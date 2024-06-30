package com.atguigu.tingshu.user.api;

import com.atguigu.tingshu.common.login.GuiguLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.user.service.UserListenProcessService;
import com.atguigu.tingshu.vo.user.UserListenProcessVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "用户声音播放进度管理接口")
@RestController
@RequestMapping("/api/user/userListenProcess")
@SuppressWarnings({"unchecked", "rawtypes"})
public class UserListenProcessApiController {

	@Autowired
	private UserListenProcessService userListenProcessService;

	@GuiguLogin
	@PostMapping("/updateListenProcess")
	public Result updateListenProcess(@RequestBody UserListenProcessVo userListenProcessVo) {
		userListenProcessService.updateListenProcess(userListenProcessVo);
		return Result.ok();
	}

	@GuiguLogin
	@GetMapping("/getTrackBreakSecond/{trackId}")
	public Result<BigDecimal> getTrackBreakSecond(@PathVariable("trackId") Long trackId) {
		return Result.ok(userListenProcessService.getTrackBreakSecond(trackId));
	}
}

