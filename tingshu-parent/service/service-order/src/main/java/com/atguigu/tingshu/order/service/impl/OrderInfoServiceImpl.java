package com.atguigu.tingshu.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.client.AlbumInfoFeignClient;
import com.atguigu.tingshu.album.client.TrackInfoFeignClient;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.model.order.OrderDerate;
import com.atguigu.tingshu.model.order.OrderDetail;
import com.atguigu.tingshu.model.order.OrderInfo;
import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.model.user.VipServiceConfig;
import com.atguigu.tingshu.order.mapper.OrderDerateMapper;
import com.atguigu.tingshu.order.mapper.OrderDetailMapper;
import com.atguigu.tingshu.order.mapper.OrderInfoMapper;
import com.atguigu.tingshu.order.service.OrderInfoService;
import com.atguigu.tingshu.user.client.UserInfoFeignClient;
import com.atguigu.tingshu.user.client.VipServiceConfigFeignClient;
import com.atguigu.tingshu.vo.order.OrderDerateVo;
import com.atguigu.tingshu.vo.order.OrderDetailVo;
import com.atguigu.tingshu.vo.order.OrderInfoVo;
import com.atguigu.tingshu.vo.order.TradeVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Resource
    private VipServiceConfigFeignClient vipServiceConfigFeignClient;

    @Resource
    private AlbumInfoFeignClient albumInfoFeignClient;

    @Resource
    private UserInfoFeignClient userInfoFeignClient;

    @Resource
    private TrackInfoFeignClient trackInfoFeignClient;

    @Autowired
    private RSAPrivateKey rsaPrivateKey;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderDerateMapper orderDerateMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public Object trade(TradeVo tradeVo) {
        OrderInfoVo result = new OrderInfoVo();

        // TODO 校验未支付订单中是否包含当前购买的内容

        switch (tradeVo.getItemType()) {
            case SystemConstant.ORDER_ITEM_TYPE_ALBUM -> result = tradeAlbum(tradeVo);
            case SystemConstant.ORDER_ITEM_TYPE_TRACK -> result = tradeTrack(tradeVo);
            case SystemConstant.ORDER_ITEM_TYPE_VIP -> result = tradeVip(tradeVo);
        }
        Jwt encode = JwtHelper.encode(JSONObject.toJSONString(result), new RsaSigner(rsaPrivateKey));
        result.setSign(encode.getEncoded());
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> submitOrder(OrderInfoVo orderInfoVo) {
        Long userId = AuthContextHolder.getUserId();
        RLock lock = redissonClient.getLock("User_Submit_Order_Lock_" + userId);
        try {
            if (lock.tryLock()) {
                try {
                    String sign = orderInfoVo.getSign();
                    Jwt jwt = JwtHelper.decodeAndVerify(sign, new RsaVerifier(SystemConstant.PUBLIC_KEY));
                    // TODO 场景校验（订单是否存在未支付/已支付）
                    String payWay = orderInfoVo.getPayWay();
                    if (payWay.equals(SystemConstant.ORDER_PAY_ACCOUNT)) {
                        // TODO 余额扣款
                    }
                    String claims = jwt.getClaims();
                    orderInfoVo = JSONObject.parseObject(claims, OrderInfoVo.class);

                    OrderInfo orderInfo = new OrderInfo();
                    BeanUtils.copyProperties(orderInfoVo, orderInfo);
                    orderInfo.setUserId(userId);
                    orderInfo.setOrderNo(orderInfoVo.getTradeNo());
                    orderInfo.setPayWay(payWay);
                    String itemType = orderInfoVo.getItemType();
                    switch (itemType) {
                        case SystemConstant.ORDER_ITEM_TYPE_ALBUM -> orderInfo.setOrderTitle("购买【" + orderInfoVo.getOrderDetailVoList().get(0).getItemName() + "】专辑");
                        case SystemConstant.ORDER_ITEM_TYPE_TRACK -> orderInfo.setOrderTitle("购买【" + orderInfoVo.getOrderDetailVoList().get(0).getItemName() + "】等声音");
                        case SystemConstant.ORDER_ITEM_TYPE_VIP -> orderInfo.setOrderTitle("购买【" + orderInfoVo.getOrderDetailVoList().get(0).getItemName() + "】会员");
                    }
                    if (!save(orderInfo)) {
                        throw new GuiguException(201, "保存订单信息失败");
                    }
                    Long orderId = orderInfo.getId();

                    orderInfoVo.getOrderDetailVoList().stream().forEach(orderDetailVo -> {
                        OrderDetail orderDetail = new OrderDetail();
                        BeanUtils.copyProperties(orderDetailVo, orderDetail);
                        orderDetail.setOrderId(orderId);
                        int insert = orderDetailMapper.insert(orderDetail);
                        if (insert <= 0) {
                            throw new GuiguException(201, "保存订单详情失败");
                        }
                    });

                    orderInfoVo.getOrderDerateVoList().stream().forEach(orderDerateVo -> {
                        OrderDerate orderDerate = new OrderDerate();
                        BeanUtils.copyProperties(orderDerateVo, orderDerate);
                        orderDerate.setOrderId(orderId);
                        int insert = orderDerateMapper.insert(orderDerate);
                        if (insert <= 0) {
                            throw new GuiguException(201, "保存订单折扣信息失败");
                        }
                    });

                    JSONObject result = new JSONObject();
                    result.put("orderNo", orderInfo.getOrderNo());

                    // TODO 发送延迟消息，开始倒计时（非余额支付）

                    return result;
                } catch (Exception e) {
                    log.error("下单逻辑异常，原因是：{}", e.getMessage());
                } finally {
                    lock.unlock();
                }
            } else {
                throw new GuiguException(201, "并发下单失败，请重试");
            }
        } catch (Exception e) {
            log.error("下单失败，原因是：{}", e.getMessage());
        }
        return null;
    }

    private OrderInfoVo tradeVip(TradeVo tradeVo) {
        OrderInfoVo orderInfoVo = new OrderInfoVo();
        Long itemId = tradeVo.getItemId();

        orderInfoVo.setTradeNo(UUID.randomUUID().toString().replaceAll("-", ""));
        orderInfoVo.setItemType(SystemConstant.ORDER_ITEM_TYPE_VIP);

        VipServiceConfig vipServiceConfig = vipServiceConfigFeignClient.getVipServiceConfig(itemId);

        BigDecimal price = vipServiceConfig.getPrice();
        BigDecimal discountPrice = vipServiceConfig.getDiscountPrice();
        orderInfoVo.setOriginalAmount(price);
        orderInfoVo.setDerateAmount(price.subtract(discountPrice));
        orderInfoVo.setOrderAmount(discountPrice);

        List<OrderDetailVo> orderDetailVoList = new ArrayList<>();
        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setItemId(itemId);
        orderDetailVo.setItemName(vipServiceConfig.getName());
        orderDetailVo.setItemUrl(vipServiceConfig.getImageUrl());
        orderDetailVo.setItemPrice(discountPrice);
        orderDetailVoList.add(orderDetailVo);
        orderInfoVo.setOrderDetailVoList(orderDetailVoList);

        List<OrderDerateVo> orderDerateVoList = new ArrayList<>();
        OrderDerateVo orderDerateVo = new OrderDerateVo();
        orderDerateVo.setDerateAmount(price.subtract(discountPrice));
        orderDerateVo.setDerateType(SystemConstant.ORDER_DERATE_VIP_SERVICE_DISCOUNT);
        orderDerateVo.setRemarks("会员充值折扣！");
        orderDerateVoList.add(orderDerateVo);

        orderInfoVo.setOrderDerateVoList(orderDerateVoList);
        orderInfoVo.setTimestamp(System.currentTimeMillis());
        return orderInfoVo;
    }

    private OrderInfoVo tradeTrack(TradeVo tradeVo) {
        OrderInfoVo orderInfoVo = new OrderInfoVo();
        Long trackId = tradeVo.getItemId();

        orderInfoVo.setTradeNo(UUID.randomUUID().toString().replaceAll("-", ""));
        orderInfoVo.setItemType(SystemConstant.ORDER_ITEM_TYPE_TRACK);

        AlbumInfo albumInfo = albumInfoFeignClient.getAlbumInfoByTrackId(trackId);
        Integer trackCount = tradeVo.getTrackCount();
        List<TrackInfo> trackPaidList = trackInfoFeignClient.getTrackPaidList(trackId, trackCount);

        BigDecimal price = albumInfo.getPrice();
        orderInfoVo.setOriginalAmount(price.multiply(new BigDecimal(trackCount)));
        orderInfoVo.setDerateAmount(new BigDecimal(0));
        orderInfoVo.setOrderAmount(price.multiply(new BigDecimal(trackCount)));

        List<OrderDetailVo> orderDetailVoList = trackPaidList.stream().map(trackInfo -> {
            OrderDetailVo orderDetailVo = new OrderDetailVo();
            orderDetailVo.setItemId(trackInfo.getId());
            orderDetailVo.setItemName(trackInfo.getTrackTitle());
            orderDetailVo.setItemUrl(trackInfo.getCoverUrl());
            orderDetailVo.setItemPrice(price);
            return orderDetailVo;
        }).toList();
        orderInfoVo.setOrderDetailVoList(orderDetailVoList);

        List<OrderDerateVo> orderDerateVoList = new ArrayList<>();
        orderInfoVo.setOrderDerateVoList(orderDerateVoList);

        orderInfoVo.setTimestamp(System.currentTimeMillis());
        return orderInfoVo;
    }

    private OrderInfoVo tradeAlbum(TradeVo tradeVo) {
        OrderInfoVo orderInfoVo = new OrderInfoVo();
        Long albumId = tradeVo.getItemId();
        // 用户已经购买过本专辑，不可重复购买
        if (userInfoFeignClient.getUserIsBuyAlbum(albumId)) {
            throw new GuiguException(201, "您已经购买过本专辑了，无需重复购买");
        }

        orderInfoVo.setTradeNo(UUID.randomUUID().toString().replaceAll("-", ""));
        orderInfoVo.setItemType(SystemConstant.ORDER_ITEM_TYPE_ALBUM);

        AlbumInfo albumInfo = albumInfoFeignClient.getAlbumInfo(albumId);
        UserInfo userInfo = userInfoFeignClient.getUserInfo(AuthContextHolder.getUserId());
        Integer isVip = userInfo.getIsVip();

        // 原价
        BigDecimal price = albumInfo.getPrice();
        BigDecimal divide = null;
        if (isVip == 0) {
            // 普通折扣
            BigDecimal discount = albumInfo.getDiscount().intValue() == -1 ? BigDecimal.valueOf(10) : albumInfo.getDiscount();
            // 折后价
            divide = price.multiply(discount).divide(BigDecimal.valueOf(10));
        } else {
            // 会员折扣
            BigDecimal vipDiscount = albumInfo.getVipDiscount().intValue() == -1 ? BigDecimal.valueOf(10) : albumInfo.getVipDiscount();
            divide = price.multiply(vipDiscount).divide(BigDecimal.valueOf(10));
        }
        orderInfoVo.setOriginalAmount(price);
        orderInfoVo.setDerateAmount(price.subtract(divide));
        orderInfoVo.setOrderAmount(divide);

        List<OrderDetailVo> orderDetailVoList = new ArrayList<>();
        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setItemId(albumId);
        orderDetailVo.setItemName(albumInfo.getAlbumTitle());
        orderDetailVo.setItemUrl(albumInfo.getCoverUrl());
        orderDetailVo.setItemPrice(divide);
        orderDetailVoList.add(orderDetailVo);
        orderInfoVo.setOrderDetailVoList(orderDetailVoList);

        List<OrderDerateVo> orderDerateVoList = new ArrayList<>();
        OrderDerateVo orderDerateVo = new OrderDerateVo();
        orderDerateVo.setDerateAmount(price.subtract(divide));
        orderDerateVo.setDerateType(SystemConstant.ORDER_DERATE_ALBUM_DISCOUNT);
        orderDerateVo.setRemarks("专辑购买折扣！");
        orderDerateVoList.add(orderDerateVo);

        orderInfoVo.setOrderDerateVoList(orderDerateVoList);
        orderInfoVo.setTimestamp(System.currentTimeMillis());
        return orderInfoVo;
    }
}
