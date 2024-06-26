package com.atguigu.tingshu.album.client;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * <p>
 * 产品列表API接口
 * </p>
 *
 * @author qy
 */
@FeignClient(value = "service-album")
public interface CategoryFeignClient {


}