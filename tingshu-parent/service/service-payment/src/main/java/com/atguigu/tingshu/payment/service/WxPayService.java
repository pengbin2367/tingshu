package com.atguigu.tingshu.payment.service;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface WxPayService {

    String getWxPayUrl(Map<String, String> paramsMap);

    String getWxPayResult(String orderNo);

    String wxNotifyUrl(HttpServletRequest request, String orderNo);
}
