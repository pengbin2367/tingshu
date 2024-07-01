package com.atguigu.tingshu.album.client;

import com.atguigu.tingshu.album.mapper.BaseCategoryViewMapper;
import com.atguigu.tingshu.album.service.BaseCategoryService;
import com.atguigu.tingshu.common.cache.GuiguCache;
import com.atguigu.tingshu.model.album.BaseCategory3;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value="/client/album/category")
public class BaseCategoryClientController {

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    @Autowired
    private BaseCategoryService baseCategoryService;

    @GuiguCache(prefix = "getBaseCategoryView:")
    @GetMapping("/getBaseCategoryView/{category3Id}")
    public BaseCategoryView getBaseCategoryView(@PathVariable(value = "category3Id") Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    @GuiguCache(prefix = "getBaseCategory3:")
    @GetMapping("/getBaseCategory3/{category1Id}")
    public List<BaseCategory3> getBaseCategory3(@PathVariable(value = "category1Id") Long category1Id) {
        return baseCategoryService.findTopBaseCategory3(category1Id);
    }
}
