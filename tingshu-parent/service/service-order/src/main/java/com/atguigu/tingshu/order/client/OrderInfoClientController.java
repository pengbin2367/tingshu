package com.atguigu.tingshu.order.client;


import com.atguigu.tingshu.model.order.OrderInfo;
import com.atguigu.tingshu.order.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client/order/orderInfo")
public class OrderInfoClientController {

    @Autowired
    private OrderInfoService orderInfoService;

    @GetMapping("/getOrderInfo/{orderNo}")
    public OrderInfo getOrderInfo(@PathVariable(value = "orderNo") String orderNo) {
        return orderInfoService.getOrderInfo(orderNo);
    }
}
