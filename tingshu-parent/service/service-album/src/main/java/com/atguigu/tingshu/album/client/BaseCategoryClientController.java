package com.atguigu.tingshu.album.client;

import com.atguigu.tingshu.album.mapper.BaseCategoryViewMapper;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="/client/album/category")
public class BaseCategoryClientController {

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    @GetMapping("/getBaseCategoryView/{category3Id}")
    public BaseCategoryView getBaseCategoryView(@PathVariable(value = "category3Id") Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }
}
