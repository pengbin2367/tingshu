package com.atguigu.tingshu.order.api;

import com.atguigu.tingshu.common.login.GuiguLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.order.OrderInfo;
import com.atguigu.tingshu.order.service.OrderInfoService;
import com.atguigu.tingshu.vo.order.OrderInfoVo;
import com.atguigu.tingshu.vo.order.TradeVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "订单管理")
@RestController
@RequestMapping("/api/order/orderInfo")
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderInfoApiController {

	@Autowired
	private OrderInfoService orderInfoService;

	@GuiguLogin
	@PostMapping("/trade")
	public Result trade(@RequestBody TradeVo tradeVo) {
		return Result.ok(orderInfoService.trade(tradeVo));
	}

	@GuiguLogin
	@PostMapping("/submitOrder")
	public Result<Map<String, Object>> submitOrder(@RequestBody OrderInfoVo orderInfoVo) {
		return Result.ok(orderInfoService.submitOrder(orderInfoVo));
	}

	@GuiguLogin
	@GetMapping("/findUserPage/{page}/{size}")
	public Result findUserPage(@PathVariable("page") Integer page, @PathVariable("size") Integer size) {
		return Result.ok(orderInfoService.page(new Page<>(page, size),
				new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getUserId, AuthContextHolder.getUserId())
						.orderByDesc(OrderInfo::getCreateTime)));
	}

	@GuiguLogin
	@GetMapping("/getOrderInfo/{orderNo}")
	public Result<OrderInfo> getOrderInfo(@PathVariable("orderNo") String orderNo) {
		return Result.ok(orderInfoService.getOne(new LambdaQueryWrapper<OrderInfo>()
				.eq(OrderInfo::getUserId, AuthContextHolder.getUserId())
				.eq(OrderInfo::getOrderNo, orderNo)));
	}
}

