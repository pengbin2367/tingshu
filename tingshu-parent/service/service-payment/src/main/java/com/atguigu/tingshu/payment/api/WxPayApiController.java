package com.atguigu.tingshu.payment.api;

import com.atguigu.tingshu.common.login.GuiguLogin;
import com.atguigu.tingshu.payment.service.WxPayService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "微信支付接口")
@RestController
@RequestMapping("/api/payment/wxPay")
@Slf4j
public class WxPayApiController {

    @Autowired
    private WxPayService wxPayService;

    @GuiguLogin
    @GetMapping("/getWxPayUrl")
    public String getWxPayUrl(@RequestParam Map<String, String> paramsMap) {
        return wxPayService.getWxPayUrl(paramsMap);
    }

    @GetMapping("/getWxPayResult")
    public String getWxPayResult(@RequestParam String orderNo) {
        return wxPayService.getWxPayResult(orderNo);
    }

    @RequestMapping("/notifyUrl")
    public String wxNotifyUrl(HttpServletRequest request, String orderNo) {
       return wxPayService.wxNotifyUrl(request, orderNo);
    }
}
