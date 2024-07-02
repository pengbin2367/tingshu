package com.atguigu.tingshu.user.client;

import com.atguigu.tingshu.model.user.VipServiceConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * <p>
 * 产品列表API接口
 * </p>
 *
 * @author qy
 */
@FeignClient(value = "service-user", path = "/client/user/vipServiceConfig", contextId = "vipServiceConfigFeignClient")
public interface VipServiceConfigFeignClient {

    @GetMapping("/getVipServiceConfig/{itemId}")
    public VipServiceConfig getVipServiceConfig(@PathVariable(value = "itemId") Long itemId);
}