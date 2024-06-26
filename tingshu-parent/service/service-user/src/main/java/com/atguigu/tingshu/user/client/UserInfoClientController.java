package com.atguigu.tingshu.user.client;

import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client/user/userInfo")
public class UserInfoClientController {

    @Autowired
    private UserInfoService userInfoService;

    @GetMapping("/getUserInfo/{userId}")
    public UserInfo getUserInfo(@PathVariable(value = "userId") Long userId) {
        return userInfoService.getById(userId);
    }
}
