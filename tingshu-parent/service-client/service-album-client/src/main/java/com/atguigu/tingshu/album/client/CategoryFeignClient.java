package com.atguigu.tingshu.album.client;

import com.atguigu.tingshu.model.album.BaseCategory3;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * <p>
 * 产品列表API接口
 * </p>
 *
 * @author qy
 */
@FeignClient(value = "service-album", path = "/client/album/category", contextId = "categoryFeignClient")
public interface CategoryFeignClient {

    @GetMapping("/getBaseCategoryView/{category3Id}")
    public BaseCategoryView getBaseCategoryView(@PathVariable(value = "category3Id") Long category3Id);

    @GetMapping("/getBaseCategory3/{category1Id}")
    public List<BaseCategory3> getBaseCategory3(@PathVariable(value = "category1Id") Long category1Id);
}