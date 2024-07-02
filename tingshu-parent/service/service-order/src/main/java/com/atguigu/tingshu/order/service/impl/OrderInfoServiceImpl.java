package com.atguigu.tingshu.order.service.impl;

import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.model.order.OrderInfo;
import com.atguigu.tingshu.model.user.VipServiceConfig;
import com.atguigu.tingshu.order.mapper.OrderInfoMapper;
import com.atguigu.tingshu.order.service.OrderInfoService;
import com.atguigu.tingshu.user.client.VipServiceConfigFeignClient;
import com.atguigu.tingshu.vo.order.OrderDerateVo;
import com.atguigu.tingshu.vo.order.OrderDetailVo;
import com.atguigu.tingshu.vo.order.OrderInfoVo;
import com.atguigu.tingshu.vo.order.TradeVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Resource
    private VipServiceConfigFeignClient vipServiceConfigFeignClient;

    @Override
    public Object trade(TradeVo tradeVo) {
        OrderInfoVo result = new OrderInfoVo();

        switch (tradeVo.getItemType()) {
            case SystemConstant.ORDER_ITEM_TYPE_ALBUM -> result = tradeAlbum(tradeVo);
            case SystemConstant.ORDER_ITEM_TYPE_TRACK -> result = tradeTrack(tradeVo);
            case SystemConstant.ORDER_ITEM_TYPE_VIP -> result = tradeVip(tradeVo);
        }
        return result;
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
        // TODO 签名
        orderInfoVo.setSign("");
        return orderInfoVo;
    }

    private OrderInfoVo tradeTrack(TradeVo tradeVo) {
        return null;
    }

    private OrderInfoVo tradeAlbum(TradeVo tradeVo) {
        return null;
    }
}
