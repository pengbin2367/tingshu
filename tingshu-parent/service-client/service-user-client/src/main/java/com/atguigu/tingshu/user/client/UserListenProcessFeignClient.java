package com.atguigu.tingshu.user.client;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * <p>
 * 产品列表API接口
 * </p>
 *
 * @author qy
 */
@FeignClient(value = "service-user")
public interface UserListenProcessFeignClient {

}