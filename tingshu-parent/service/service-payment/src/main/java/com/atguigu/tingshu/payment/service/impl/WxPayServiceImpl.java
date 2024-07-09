package com.atguigu.tingshu.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.common.util.HttpClient;
import com.atguigu.tingshu.model.payment.PaymentInfo;
import com.atguigu.tingshu.payment.service.PaymentInfoService;
import com.atguigu.tingshu.payment.service.WxPayService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.wxpay.sdk.WXPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class WxPayServiceImpl implements WxPayService {

	@Autowired
	private PaymentInfoService paymentInfoService;

	@Autowired
	private RedissonClient redissonClient;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Value("${appid}")
	private String appid;
	@Value("${partner}")
	private String partner;
	@Value("${partnerkey}")
	private String partnerkey;
	@Value("${notifyUrl}")
	private String notifyUrl;

	@Override
	public String getWxPayUrl(Map<String, String> paramsMap) {
		String body = paramsMap.get("body");
		String orderNo = paramsMap.get("orderNo");
		String money = paramsMap.get("money");
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
		// 包装附加数据
		Map<String, String> attach = new HashMap<>();
		attach.put("exchange", paramsMap.get("exchange"));
		attach.put("routingKey", paramsMap.get("routingKey"));
		params.put("attach", JSONObject.toJSONString(attach));
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
				savePaymentInfo(paramsMap);
				return result.get("code_url");
			} else {
				return JSONObject.toJSONString(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void savePaymentInfo(Map<String, String> paramsMap) {
		String orderNo = "tingshujava0628111" + paramsMap.get("orderNo");
		paymentInfoService.remove(new LambdaQueryWrapper<PaymentInfo>().eq(PaymentInfo::getOrderNo, orderNo));
		PaymentInfo paymentInfo = new PaymentInfo();
		paymentInfo.setUserId(AuthContextHolder.getUserId());
		paymentInfo.setPaymentType(paramsMap.get("paymentType"));
		paymentInfo.setOrderNo(orderNo);
		paymentInfo.setPayWay(SystemConstant.ORDER_PAY_WAY_WEIXIN);
		paymentInfo.setAmount(new BigDecimal(paramsMap.get("money")));
		paymentInfo.setContent(paramsMap.get("body"));
		paymentInfo.setPaymentStatus(SystemConstant.PAYMENT_STATUS_UNPAID);
		paymentInfoService.save(paymentInfo);
	}

	@Override
	public String getWxPayResult(String orderNo) {
		if (StringUtils.isEmpty(orderNo)) {
			return null;
		}
		Map<String, String> params = new HashMap<>();
		params.put("appid", appid);
		params.put("mch_id", partner);
		params.put("nonce_str", WXPayUtil.generateNonceStr());
		params.put("out_trade_no", "tingshujava0628111" + orderNo);
		try {
			String url = "https://api.mch.weixin.qq.com/pay/orderquery";
			HttpClient httpClient = new HttpClient(url);
			httpClient.setHttps(true);
			String paramsXml = WXPayUtil.generateSignedXml(params, partnerkey);
			httpClient.setXmlParam(paramsXml);
			httpClient.post();
			String contentXmlString = httpClient.getContent();
			Map<String, String> result = WXPayUtil.xmlToMap(contentXmlString);
			return JSONObject.toJSONString(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@SneakyThrows
	@Override
	public String wxNotifyUrl(HttpServletRequest request, String orderNo) {
		//        ServletInputStream inputStream = request.getInputStream();
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        byte[] buffer = new byte[1024];
//        int offset = 0;
//        while ((offset = inputStream.read(buffer)) != -1) {
//            outputStream.write(buffer, 0, offset);
//        }
//        outputStream.flush();
//        String xmlString = new String(outputStream.toByteArray());
//        Map<String, String> result = WXPayUtil.xmlToMap(xmlString);
//        System.out.println(JSONObject.toJSONString(result));
//        inputStream.close(); outputStream.close();
		// TODO 方便测试，未使用真实接口
//        String resultString = "{\"transaction_id\":\"4200002401202407089871520059\",\"nonce_str\":\"f8FFiQco0tsMb7vI\",\"trade_state\":\"SUCCESS\",\"bank_type\":\"OTHERS\",\"openid\":\"oHwsHuJDSnjwhH20Jyj4uqta1Iog\",\"sign\":\"816B864E179D072BF5D098DC46AA30F2\",\"return_msg\":\"OK\",\"fee_type\":\"CNY\",\"mch_id\":\"1558950191\",\"cash_fee\":\"1\",\"out_trade_no\":\"tingshujava0628111110\",\"cash_fee_type\":\"CNY\",\"appid\":\"wx74862e0dfcf69954\",\"total_fee\":\"1\",\"trade_state_desc\":\"支付成功\",\"trade_type\":\"NATIVE\",\"result_code\":\"SUCCESS\",\"attach\":\"\",\"time_end\":\"20240708182345\",\"is_subscribe\":\"N\",\"return_code\":\"SUCCESS\"}";
		String resultString = "{\"transaction_id\":\"4200002319202407086287304125\",\"nonce_str\":\"zYDW2NsQfWG0D9XB\",\"trade_state\":\"SUCCESS\",\"bank_type\":\"OTHERS\",\"openid\":\"oHwsHuJDSnjwhH20Jyj4uqta1Iog\",\"sign\":\"B74AF44CDC21303FBE38143E31D5CF6E\",\"return_msg\":\"OK\",\"fee_type\":\"CNY\",\"mch_id\":\"1558950191\",\"cash_fee\":\"1\",\"out_trade_no\":\"tingshujava0628111120\",\"cash_fee_type\":\"CNY\",\"appid\":\"wx74862e0dfcf69954\",\"total_fee\":\"1\",\"trade_state_desc\":\"支付成功\",\"trade_type\":\"NATIVE\",\"result_code\":\"SUCCESS\",\"attach\":\"{\\\"exchange\\\":\\\"payment\\\",\\\"routingKey\\\":\\\"payment.tingshu.order\\\"}\",\"time_end\":\"20240708191213\",\"is_subscribe\":\"N\",\"return_code\":\"SUCCESS\"}";
		Map<String, String> map = JSONObject.parseObject(resultString, Map.class);
		map.put("out_trade_no", "tingshujava0628111" + orderNo);

		CompletableFuture.runAsync(() -> {
			updatePaymentInfo(map);
		});

		Map<String, String> resultMap = new HashMap<>();
		resultMap.put("return_code", "SUCCESS");
		resultMap.put("return_msg", "OK");
		return WXPayUtil.mapToXml(resultMap);
	}

	private void updatePaymentInfo(Map<String, String> map) {
		String orderNo = map.get("out_trade_no");
		RLock lock = redissonClient.getLock("Payment_Info_Lock" + orderNo);
		lock.lock();
		try {
			PaymentInfo paymentInfo = paymentInfoService.getOne(new LambdaQueryWrapper<PaymentInfo>().eq(PaymentInfo::getOrderNo, map.get("out_trade_no")));
			if (paymentInfo == null) {
				paymentInfo = new PaymentInfo();
				paymentInfo.setUserId(AuthContextHolder.getUserId());
				paymentInfo.setPaymentType("1303");
				paymentInfo.setOrderNo("不存在的订单");
				paymentInfo.setPayWay(SystemConstant.ORDER_PAY_WAY_WEIXIN);
				paymentInfo.setAmount(new BigDecimal(map.get("total_fee")));
				paymentInfo.setContent("未知订单的莫名收款");
				paymentInfo.setPaymentStatus(SystemConstant.PAYMENT_STATUS_PAID);
				paymentInfoService.save(paymentInfo);
				return ;
			}
			if (paymentInfo.getPaymentStatus().equals(SystemConstant.PAYMENT_STATUS_UNPAID)) {
				// 未付款订单
				paymentInfo.setOutTradeNo(map.get("transaction_id"));
				paymentInfo.setCallbackTime(new Date());
				paymentInfo.setCallbackContent(JSONObject.toJSONString(map));
				paymentInfo.setPaymentStatus(SystemConstant.PAYMENT_STATUS_PAID);
				paymentInfoService.updateById(paymentInfo);
				// 获取附加数据
				String attachJsonString = map.get("attach");
				Map<String, String> attach = JSONObject.parseObject(attachJsonString, Map.class);
				String exchange = attach.get("exchange");
				String routingKey = attach.get("routingKey");
				// 发消息
				rabbitTemplate.convertAndSend(exchange, routingKey, JSONObject.toJSONString(paymentInfo));
			} else {
				// 已支付，判断流水号，一致则原路退款
				String outTradeNo = paymentInfo.getOutTradeNo();
				if (!outTradeNo.equals(map.get("transaction_id"))) {
					log.info("原路退款");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("某一笔订单流水保存失败，订单信息：{}", JSONObject.toJSONString(map));
		} finally {
			lock.unlock();
		}
	}
}
