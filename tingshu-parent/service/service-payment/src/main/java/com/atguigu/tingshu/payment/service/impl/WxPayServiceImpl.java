package com.atguigu.tingshu.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.common.util.HttpClient;
import com.atguigu.tingshu.payment.service.PaymentInfoService;
import com.atguigu.tingshu.payment.service.WxPayService;
import com.github.wxpay.sdk.WXPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WxPayServiceImpl implements WxPayService {

	@Autowired
	private PaymentInfoService paymentInfoService;

	@Value("${appid}")
	private String appid;
	@Value("${partner}")
	private String partner;
	@Value("${partnerkey}")
	private String partnerkey;
	@Value("${notifyUrl}")
	private String notifyUrl;

	@Override
	public String getWxPayUrl(String body, String orderNo, String money) {
		if (StringUtils.isEmpty(body) || StringUtils.isEmpty(orderNo) || StringUtils.isEmpty(money)) {
			return null;
		}
		Map<String, String> params = new HashMap<>();
		params.put("appid", appid);
		params.put("mch_id", partner);
		params.put("nonce_str", WXPayUtil.generateNonceStr());
		params.put("body", body);
		params.put("out_trade_no", "tingshujava0628111" + orderNo);
		params.put("total_fee", money);
		params.put("spbill_create_ip", "192.168.0.166");
		params.put("notify_url", notifyUrl);
		params.put("trade_type", "NATIVE");
		try {
			String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
			HttpClient httpClient = new HttpClient(url);
			httpClient.setHttps(true);
			String paramsXml = WXPayUtil.generateSignedXml(params, partnerkey);
			httpClient.setXmlParam(paramsXml);
			httpClient.post();
			String contentXmlString = httpClient.getContent();
			Map<String, String> result = WXPayUtil.xmlToMap(contentXmlString);
			if (result.get("return_code").equals("SUCCESS") && result.get("result_code").equals("SUCCESS")) {
				return result.get("code_url");
			} else {
				return JSONObject.toJSONString(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
