package com.atguigu.tingshu.user.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.common.util.IpUtil;
import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.model.user.UserPaidAlbum;
import com.atguigu.tingshu.model.user.UserPaidTrack;
import com.atguigu.tingshu.user.mapper.UserInfoMapper;
import com.atguigu.tingshu.user.mapper.UserPaidAlbumMapper;
import com.atguigu.tingshu.user.mapper.UserPaidTrackMapper;
import com.atguigu.tingshu.user.service.UserInfoService;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.interfaces.RSAPrivateKey;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private UserPaidAlbumMapper userPaidAlbumMapper;

	@Autowired
	private UserPaidTrackMapper userPaidTrackMapper;

	@SneakyThrows
    @Override
	public Object wxLogin(String code) {
		// 参数校验
		if (StringUtils.isEmpty(code)) {
			throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
		}
		JSONObject result = new JSONObject();
		// 重复登陆问题
		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		String ipAddress = IpUtil.getIpAddress(servletRequestAttributes.getRequest());
		Object o = redisTemplate.opsForValue().get("User_Login_Info_" + ipAddress);
		if (o != null) {
			result.put("token", o);
		} else {
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
				userInfo.setIsVip(0);
				save(userInfo);
				// 初始化一个账户
				rabbitTemplate.convertAndSend("user_exchange", "user.account", userInfo.getId() + "");
			}
			// 注册过，获取这个用户的信息，生成一个token返回
			// 生成一个token
			JSONObject jwtInfo = new JSONObject();
			jwtInfo.put("userId", userInfo.getId().toString());
			jwtInfo.put("role", userInfo.getIsVip().toString());
			jwtInfo.put("e_times", System.currentTimeMillis() + "");
			Jwt jwt = JwtHelper.encode(jwtInfo.toJSONString(), new RsaSigner(rsaPrivateKey));
			String token = jwt.getEncoded();
			redisTemplate.opsForValue().set("User_Login_Info_" + ipAddress, token, 1, TimeUnit.DAYS);
			result.put("token", token);
		}
		return result;
	}

	@Override
	public UserInfoVo getUserInfoVoByUserId(Long userId) {
		UserInfo userInfo = this.getById(userId);
		UserInfoVo userInfoVo = new UserInfoVo();
		BeanUtils.copyProperties(userInfo,userInfoVo);
		return userInfoVo;
	}

	@Override
	public void updateUser(UserInfoVo userInfoVo) {
		Long userId = AuthContextHolder.getUserId();
		UserInfo userInfo = new UserInfo();
		BeanUtils.copyProperties(userInfoVo, userInfo);
		userInfo.setId(userId);
		updateById(userInfo);
	}

	@Override
	public String getNewToken() {
		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = servletRequestAttributes.getRequest();
		// 获取请求头中的token
		String token = request.getHeader("token");
		// 获取旧的载荷
		Jwt decode = JwtHelper.decode(token);
		String claims = decode.getClaims();
		// 反序列化
		Map<String, String> newClaims = JSONObject.parseObject(claims, Map.class);
		newClaims.put("e_times", System.currentTimeMillis() + "");
		//申请新令牌
		Jwt encode = JwtHelper.encode(newClaims.toString(), new RsaSigner(rsaPrivateKey));
		// 将本次的ip和令牌绑定
		String encoded = encode.getEncoded();
		redisTemplate.opsForValue().set("User_Login_Info_" + IpUtil.getIpAddress(request), token, 1, TimeUnit.DAYS);
		return encoded;
	}

	@Override
	public Boolean getUserIsBuyAlbum(Long albumId) {
		UserPaidAlbum userPaidAlbum = userPaidAlbumMapper.selectOne(new LambdaQueryWrapper<UserPaidAlbum>()
				.eq(UserPaidAlbum::getUserId, AuthContextHolder.getUserId())
				.eq(UserPaidAlbum::getAlbumId, albumId));
		return null != userPaidAlbum;
	}

	@Override
	public Map<String, String> getUserTrackIds(Long albumId) {
		List<UserPaidTrack> userPaidTracks = userPaidTrackMapper.selectList(new LambdaQueryWrapper<UserPaidTrack>()
				.eq(UserPaidTrack::getAlbumId, albumId)
				.eq(UserPaidTrack::getUserId, AuthContextHolder.getUserId()));
		return userPaidTracks.stream().collect(Collectors.toMap(
				key -> key.getTrackId().toString(),
				value -> "1"
		));
	}
}
