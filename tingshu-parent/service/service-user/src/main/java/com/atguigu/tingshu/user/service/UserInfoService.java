package com.atguigu.tingshu.user.service;

import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserInfoService extends IService<UserInfo> {

    Object wxLogin(String code);

    UserInfoVo getUserInfoVoByUserId(Long userId);

    void updateUser(UserInfoVo userInfoVo);

    String getNewToken();
}
