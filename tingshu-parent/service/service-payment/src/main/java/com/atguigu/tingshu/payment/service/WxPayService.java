package com.atguigu.tingshu.payment.service;

import java.util.Map;

public interface WxPayService {

    String getWxPayUrl(Map<String, String> paramsMap);

    String getWxPayResult(String orderNo);
}
