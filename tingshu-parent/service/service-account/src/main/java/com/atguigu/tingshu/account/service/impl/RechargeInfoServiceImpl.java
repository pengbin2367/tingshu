package com.atguigu.tingshu.account.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.account.mapper.RechargeInfoMapper;
import com.atguigu.tingshu.account.service.RechargeInfoService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.account.RechargeInfo;
import com.atguigu.tingshu.vo.account.RechargeInfoVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class RechargeInfoServiceImpl extends ServiceImpl<RechargeInfoMapper, RechargeInfo> implements RechargeInfoService {

	@Autowired
	private RechargeInfoMapper rechargeInfoMapper;

	@Override
	public Object submitRecharge(RechargeInfoVo rechargeInfoVo) {
		if (rechargeInfoVo.getAmount().intValue() <= 0) {
			throw new GuiguException(201, "金额不合法");
		}
		RechargeInfo rechargeInfo = new RechargeInfo();
		rechargeInfo.setRechargeAmount(rechargeInfoVo.getAmount());
		rechargeInfo.setRechargeStatus(SystemConstant.ORDER_STATUS_UNPAID);
		rechargeInfo.setUserId(AuthContextHolder.getUserId());
		rechargeInfo.setOrderNo(UUID.randomUUID().toString().replaceAll("-", ""));
		rechargeInfo.setPayWay(rechargeInfoVo.getPayWay());
		save(rechargeInfo);
		JSONObject result = new JSONObject();
		result.put("orderNo", rechargeInfo.getOrderNo());
		return result;
	}
}
