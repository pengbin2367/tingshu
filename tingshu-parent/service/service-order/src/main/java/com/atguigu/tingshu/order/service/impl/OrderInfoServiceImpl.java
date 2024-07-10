package com.atguigu.tingshu.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.client.AlbumInfoFeignClient;
import com.atguigu.tingshu.album.client.TrackInfoFeignClient;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.model.base.BaseEntity;
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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public Object trade(TradeVo tradeVo) {
        OrderInfoVo result = new OrderInfoVo();

        // 校验未支付订单中是否包含当前购买的内容
        checkOrder(tradeVo);

        switch (tradeVo.getItemType()) {
            case SystemConstant.ORDER_ITEM_TYPE_ALBUM -> result = tradeAlbum(tradeVo);
            case SystemConstant.ORDER_ITEM_TYPE_TRACK -> result = tradeTrack(tradeVo);
            case SystemConstant.ORDER_ITEM_TYPE_VIP -> result = tradeVip(tradeVo);
        }
        Jwt encode = JwtHelper.encode(JSONObject.toJSONString(result), new RsaSigner(rsaPrivateKey));
        result.setSign(encode.getEncoded());
        return result;
    }

    private void checkOrder(TradeVo tradeVo) {
        Long userId = AuthContextHolder.getUserId();
        Long itemId = tradeVo.getItemId();
        switch (tradeVo.getItemType()) {
            case SystemConstant.ORDER_ITEM_TYPE_ALBUM -> {
                // 查询所有已支付/未支付专辑中是否包含当前要购买的专辑
                int count = orderInfoMapper.selectAlbumOrderCount(userId, itemId);
                if (count > 0) {
                    throw new GuiguException(201, "专辑已经购买过或存在未支付的该专辑订单，请确认后处理");
                }
            }
            case SystemConstant.ORDER_ITEM_TYPE_TRACK -> {
                // 获取当前要购买的声音集合A
                Set<Long> trackIds = trackInfoFeignClient.getTrackPaidList(itemId, tradeVo.getTrackCount()).stream().map(BaseEntity::getId).collect(Collectors.toSet());
                // 获取当前未支付的声音集合B
                Set<Long> list = orderInfoMapper.selectTrackOrderIds(userId);
                // B.contains(A) ?
                trackIds.stream().forEach(trackId -> {
                    if (list.contains(trackId)) {
                        throw new GuiguException(201, "声音在其他未支付订单中");
                    }
                });
            }
            case SystemConstant.ORDER_ITEM_TYPE_VIP -> {
                // 查询是否有未支付的订单
                OrderInfo orderInfo = getOne(new LambdaQueryWrapper<OrderInfo>()
                        .eq(OrderInfo::getUserId, userId)
                        .eq(OrderInfo::getItemType, SystemConstant.ORDER_ITEM_TYPE_VIP)
                        .eq(OrderInfo::getOrderStatus, SystemConstant.ORDER_STATUS_UNPAID));
                if (null != orderInfo) {
                    throw new GuiguException(201, "您存在未支付的会员订单，请先取消支付或支付订单后处理");
                }
            }
        }
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
                    // checkOrder
                    String payWay = orderInfoVo.getPayWay();
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

                    if (payWay.equals(SystemConstant.ORDER_PAY_ACCOUNT)) {
                        // 发送余额扣款消息
                        rabbitTemplate.convertAndSend("order_pay_change", "order.pay", JSONObject.toJSONString(orderInfo));
                    } else {
                        // 发送延迟消息，开始倒计时（非余额支付）
                        rabbitTemplate.convertAndSend("order_normal_change", "order.dead", userId + ":" + orderInfo.getOrderNo(), message -> {
                            MessageProperties messageProperties = message.getMessageProperties();
                            messageProperties.setExpiration(900000 + "");
                            return message;
                        });
                    }

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

    @Override
    public void cancelOrder(Long userId, String orderNo) {
        String status = SystemConstant.ORDER_STATUS_CANCEL;
        if (userId == null) {
            userId = AuthContextHolder.getUserId();
        } else {
            status = SystemConstant.ORDER_STATUS_AUTO_CANCEL;
        }
        RLock lock = redissonClient.getLock("Cancel_OrderInfo_UserId_" + userId + "_" + orderNo);
        try {
            if (lock.tryLock()) {
                try {
                    OrderInfo orderInfo = getOne(new LambdaQueryWrapper<OrderInfo>()
                            .eq(OrderInfo::getUserId, userId)
                            .eq(OrderInfo::getOrderNo, orderNo)
                            .eq(OrderInfo::getOrderStatus, SystemConstant.ORDER_STATUS_UNPAID)
                    );
                    orderInfo.setOrderStatus(status);
                    updateById(orderInfo);
                }catch (Exception e) {
                    throw e;
                } finally {
                    lock.unlock();
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateOrderInfo(String orderNo) {
        OrderInfo orderInfo = getOne(new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getOrderNo, orderNo));
        if (orderInfo == null) return ;
        Long userId = orderInfo.getUserId();
        RLock lock = redissonClient.getLock("Cancel_OrderInfo_UserId_" + userId + "_" + orderNo);
        lock.lock();
        try {
            if (orderInfo.getOrderStatus().equals(SystemConstant.ORDER_STATUS_PAID)) {
                // TODO 已支付，此时出现重复消费（同渠道消费两次，不同渠道分别消费一次）
            } else {
                // 已取消/未支付，将支付状态修改为已支付即可
                orderInfo.setOrderStatus(SystemConstant.ORDER_STATUS_PAID);
            }
            updateById(orderInfo);
        } catch (Exception e) {
            throw e;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public OrderInfo getOrderInfo(String orderNo) {
        OrderInfo orderInfo = getOne(new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getOrderNo, orderNo));
        if (orderInfo == null) return null;
        List<OrderDetail> orderDetail = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orderInfo.getId()));
        orderInfo.setOrderDetailList(orderDetail);
        return orderInfo;
    }
}