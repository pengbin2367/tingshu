package com.atguigu.tingshu.user.client;

import com.atguigu.tingshu.model.user.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * <p>
 * 产品列表API接口
 * </p>
 *
 * @author qy
 */
@FeignClient(value = "service-user", path = "/client/user/userInfo", contextId = "userInfoFeignClient")
public interface UserInfoFeignClient {

    @GetMapping("/getUserInfo/{userId}")
    public UserInfo getUserInfo(@PathVariable(value = "userId") Long userId);

    @GetMapping("/getUserIsBuyAlbum/{albumId}")
    public Boolean getUserIsBuyAlbum(@PathVariable(value = "albumId") Long albumId);

    @GetMapping("/getUserTrackIds/{albumId}")
    public Map<String, String> getUserTrackIds(@PathVariable(value = "albumId") Long albumId);
}