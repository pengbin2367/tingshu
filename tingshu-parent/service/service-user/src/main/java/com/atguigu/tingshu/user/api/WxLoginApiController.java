package com.atguigu.tingshu.user.api;

import com.atguigu.tingshu.common.login.GuiguLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.user.service.UserInfoService;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "微信授权登录接口")
@RestController
@RequestMapping("/api/user/wxLogin")
@Slf4j
public class WxLoginApiController {

    @Autowired
    private UserInfoService userInfoService;

    @GetMapping("/wxLogin/{code}")
    public Result wxLogin(@PathVariable("code") String code) {
        return Result.ok(userInfoService.wxLogin(code));
    }

    @GuiguLogin
    @GetMapping("/getUserInfo")
    public Result getUserInfo(){
        Long userId = AuthContextHolder.getUserId();
        return Result.ok(userInfoService.getUserInfoVoByUserId(userId));
    }

    @GuiguLogin
    @PostMapping("/updateUser")
    public Result updateUser(@RequestBody UserInfoVo userInfoVo){
        userInfoService.updateUser(userInfoVo);
        return Result.ok();
    }

    @GuiguLogin
    @GetMapping("/getNewToken")
    public Result<String> getNewToken() {
        return Result.ok(userInfoService.getNewToken());
    }
}
