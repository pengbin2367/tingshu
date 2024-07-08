package com.atguigu.tingshu.order.client;

import com.atguigu.tingshu.model.order.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * <p>
 * 产品列表API接口
 * </p>
 *
 * @author qy
 */
@FeignClient(value = "service-order", path = "/client/order/orderInfo", contextId = "orderInfoFeignClient")
public interface OrderInfoFeignClient {

    @GetMapping("/getOrderInfo/{orderNo}")
    public OrderInfo getOrderInfo(@PathVariable(value = "orderNo") String orderNo);
}