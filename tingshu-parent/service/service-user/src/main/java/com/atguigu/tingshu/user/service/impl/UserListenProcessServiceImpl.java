package com.atguigu.tingshu.user.service.impl;

import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.common.util.MongoUtil;
import com.atguigu.tingshu.model.user.UserListenProcess;
import com.atguigu.tingshu.user.service.UserListenProcessService;
import com.atguigu.tingshu.vo.user.UserListenProcessVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class UserListenProcessServiceImpl implements UserListenProcessService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public void updateListenProcess(UserListenProcessVo userListenProcessVo) {
		Long userId = AuthContextHolder.getUserId();
		// 查询用户播放进度
		Query query = Query.query(Criteria.where("userId").is(userId).and("trackId").is(userListenProcessVo.getTrackId()));
		UserListenProcess userListenProcess = mongoTemplate.findOne(query, UserListenProcess.class,
				MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId));
		if (null == userListenProcess) {
			// 若无则新增
			userListenProcess = new UserListenProcess();
			BeanUtils.copyProperties(userListenProcessVo, userListenProcess);
			userListenProcess.setId(UUID.randomUUID().toString().replaceAll("-", ""));
			userListenProcess.setUserId(userId);
			userListenProcess.setIsShow(1);
			userListenProcess.setCreateTime(new Date());
			userListenProcess.setUpdateTime(new Date());
		} else {
			// 有则更新
			userListenProcess.setUpdateTime(new Date());
			userListenProcess.setBreakSecond(userListenProcessVo.getBreakSecond());
		}
		mongoTemplate.save(userListenProcess, MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId));
	}

	@Override
	public BigDecimal getTrackBreakSecond(Long trackId) {
		Long userId = AuthContextHolder.getUserId();
		Query query = Query.query(Criteria.where("userId").is(userId).and("trackId").is(trackId));
		UserListenProcess userListenProcess = mongoTemplate.findOne(query, UserListenProcess.class,
				MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId));
		if (null == userListenProcess) {
			return new BigDecimal(0);
		}
		return userListenProcess.getBreakSecond();
	}
}
