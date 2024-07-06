package com.atguigu.tingshu.account.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.account.mapper.UserAccountDetailMapper;
import com.atguigu.tingshu.account.mapper.UserAccountMapper;
import com.atguigu.tingshu.account.service.UserAccountService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.account.UserAccount;
import com.atguigu.tingshu.model.account.UserAccountDetail;
import com.atguigu.tingshu.model.order.OrderInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@SuppressWarnings({"unchecked", "rawtypes"})
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {

	@Autowired
	private UserAccountMapper userAccountMapper;

	@Autowired
	private RedissonClient redissonClient;

	@Autowired
	private UserAccountDetailMapper userAccountDetailMapper;

	@Override
	public void decountUserAccount(String msg) {
		OrderInfo orderInfo = JSONObject.parseObject(msg, OrderInfo.class);
		String orderNo = orderInfo.getOrderNo();
		RLock lock = redissonClient.getLock("Order_User_Account_Lock_" + orderNo);
		lock.lock();
		try {
			Long userId = AuthContextHolder.getUserId();
			UserAccountDetail userAccountDetail = userAccountDetailMapper.selectOne(new LambdaQueryWrapper<UserAccountDetail>()
					.eq(UserAccountDetail::getOrderNo, orderNo)
					.eq(UserAccountDetail::getUserId, userId));
			if (userAccountDetail != null) {
				return ;
			}
			BigDecimal orderAmount = orderInfo.getOrderAmount();
			int i = userAccountMapper.updateAvailableAmount(userId, orderAmount);
			if (i <= 0) {
				throw new GuiguException(201, "余额不足");
			}
			userAccountDetail.setUserId(userId);
			userAccountDetail.setTitle(orderInfo.getOrderTitle());
			userAccountDetail.setTradeType(SystemConstant.ACCOUNT_TRADE_TYPE_MINUS);
			userAccountDetail.setAmount(orderAmount);
			userAccountDetail.setOrderNo(orderNo);
			int insert = userAccountDetailMapper.insert(userAccountDetail);
			if (insert <= 0) {
				throw new GuiguException(201, "保存支付明细失败");
			}
			// TODO 发通知
			//  1. service-order 	修改订单状态
			//  2. service-album 	增加购买次数
			//  3. service-search 	增加购买次数和热度值
			//  4. service-user 	记录用户的购买流水
		} catch (Exception e) {
			throw e;
		} finally {
			lock.unlock();
		}
	}
}
