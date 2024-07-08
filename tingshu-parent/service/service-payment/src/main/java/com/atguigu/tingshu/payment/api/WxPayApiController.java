package com.atguigu.tingshu.payment.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.payment.service.WxPayService;
import com.github.wxpay.sdk.WXPayUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "微信支付接口")
@RestController
@RequestMapping("/api/payment/wxPay")
@Slf4j
public class WxPayApiController {

    @Autowired
    private WxPayService wxPayService;

    @GetMapping("/getWxPayUrl")
    public String getWxPayUrl(@RequestParam Map<String, String> paramsMap) {
        return wxPayService.getWxPayUrl(paramsMap);
    }

    @GetMapping("/getWxPayResult")
    public String getWxPayResult(@RequestParam String orderNo) {
        return wxPayService.getWxPayResult(orderNo);
    }

    @SneakyThrows
    @RequestMapping("/notifyUrl")
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

        // 获取附加数据
        String attachJsonString = map.get("attach");
        Map<String, String> attach = JSONObject.parseObject(attachJsonString, Map.class);
        String exchange = attach.get("exchange");
        String routingKey = attach.get("routingKey");
        // TODO 发消息

        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("return_code", "SUCCESS");
        resultMap.put("return_msg", "OK");
        return WXPayUtil.mapToXml(resultMap);
    }
}
