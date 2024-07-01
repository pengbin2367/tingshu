package com.atguigu.tingshu.user.client;

import com.atguigu.tingshu.common.cache.GuiguCache;
import com.atguigu.tingshu.common.login.GuiguLogin;
import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/client/user/userInfo")
public class UserInfoClientController {

    @Autowired
    private UserInfoService userInfoService;

    @GuiguCache(prefix = "getUserInfo:")
    @GetMapping("/getUserInfo/{userId}")
    public UserInfo getUserInfo(@PathVariable(value = "userId") Long userId) {
        return userInfoService.getById(userId);
    }

    @GuiguLogin
    @GetMapping("/getUserIsBuyAlbum/{albumId}")
    public Boolean getUserIsBuyAlbum(@PathVariable(value = "albumId") Long albumId) {
        return userInfoService.getUserIsBuyAlbum(albumId);
    }

    @GuiguLogin
    @GetMapping("/getUserTrackIds/{albumId}")
    public Map<String, String> getUserTrackIds(@PathVariable(value = "albumId") Long albumId) {
        return userInfoService.getUserTrackIds(albumId);
    }
}
