package com.atguigu.tingshu.user.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.user.mapper.UserInfoMapper;
import com.atguigu.tingshu.user.service.UserInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

	@Autowired
	private UserInfoMapper userInfoMapper;

	@Autowired
	private WxMaService wxMaService;

	@Autowired
	private RSAPrivateKey rsaPrivateKey;

	@SneakyThrows
    @Override
	public Object wxLogin(String code) {
		// 参数校验
		if (StringUtils.isEmpty(code)) {
			throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
		}
		// 使用这个code 和微信端进行交互
		WxMaJscode2SessionResult wxResult = wxMaService.jsCode2SessionInfo(code);
		// 确认当前登陆的用户是谁（微信系统的谁）
		String openid = wxResult.getOpenid();
		// 这个用户在我这个系统有没有注册过
		UserInfo userInfo = getOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getWxOpenId, openid));
		if (userInfo == null) {
			// 没有注册过，完成注册，生成一个token返回
			userInfo = new UserInfo();
			userInfo.setWxOpenId(openid);
			userInfo.setNickname("PengBin");
			userInfo.setAvatarUrl("https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif?imageView2/1/w/80/h/80");
			// TODO 初始化一个账户
		}
		// 注册过，获取这个用户的信息，生成一个token返回
		// 生成一个token
		JSONObject jwtInfo = new JSONObject();
		jwtInfo.put("userId", userInfo.getId());
		jwtInfo.put("role", userInfo.getIsVip());
		Jwt jwt = JwtHelper.encode(jwtInfo.toJSONString(), new RsaSigner(rsaPrivateKey));
		JSONObject result = new JSONObject();
		result.put("token", jwt.getEncoded());
		return result;
	}
}
