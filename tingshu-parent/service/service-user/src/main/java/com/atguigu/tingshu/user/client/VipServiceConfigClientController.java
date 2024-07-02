package com.atguigu.tingshu.user.client;


import com.atguigu.tingshu.common.cache.GuiguCache;
import com.atguigu.tingshu.model.user.VipServiceConfig;
import com.atguigu.tingshu.user.service.VipServiceConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client/user/vipServiceConfig")
public class VipServiceConfigClientController {

    @Autowired
    private VipServiceConfigService vipServiceConfigService;

    @GuiguCache(prefix = "vipServiceConfig:")
    @GetMapping("/getVipServiceConfig/{itemId}")
    public VipServiceConfig getVipServiceConfig(@PathVariable(value = "itemId") Long itemId) {
        return vipServiceConfigService.getById(itemId);
    }
}
