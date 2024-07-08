package com.atguigu.tingshu.payment.service;

public interface WxPayService {

    String getWxPayUrl(String body, String orderNo, String money);
}
