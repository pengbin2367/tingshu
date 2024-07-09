package com.atguigu.tingshu.account.api;

import com.atguigu.tingshu.account.service.UserAccountService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.login.GuiguLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.account.UserAccount;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户账户管理")
@RestController
@RequestMapping("/api/account/userAccount")
@SuppressWarnings({"unchecked", "rawtypes"})
public class UserAccountApiController {

	@Autowired
	private UserAccountService userAccountService;

	@GuiguLogin
	@GetMapping("/getAvailableAmount")
	public Result getAvailableAmount() {
		UserAccount account = userAccountService.getOne(
				new LambdaQueryWrapper<UserAccount>().eq(UserAccount::getUserId, AuthContextHolder.getUserId()));
		return Result.ok(account.getAvailableAmount());
	}

	@GuiguLogin
	@GetMapping("/findUserConsumePage/{page}/{size}")
	public Result findUserConsumePage(@PathVariable("page") Integer page, @PathVariable("size") Integer size) {
		return Result.ok(userAccountService.findAccountTradePage(page, size, SystemConstant.ACCOUNT_TRADE_TYPE_MINUS));
	}

	@GuiguLogin
	@GetMapping("/findUserRechargePage/{page}/{size}")
	public Result findUserRechargePage(@PathVariable("page") Integer page, @PathVariable("size") Integer size) {
		return Result.ok(userAccountService.findAccountTradePage(page, size, SystemConstant.ACCOUNT_TRADE_TYPE_DEPOSIT));
	}
}

