package com.atguigu.tingshu.user.task;

import com.atguigu.tingshu.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UserVipCheckTask {

    @Autowired
    private UserInfoService userInfoService;

    @Scheduled(cron = "20/10 * * * * *")
    public void userVipCheckTask() {
        userInfoService.userVipCheckTask();
    }
}
