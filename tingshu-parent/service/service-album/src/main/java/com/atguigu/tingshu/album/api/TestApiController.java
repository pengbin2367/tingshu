package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.TestService;
import com.atguigu.tingshu.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/album")
public class TestApiController {

    @Autowired
    private TestService testService;

    @GetMapping("/test")
    public Result testSetRedis() {
        testService.setRedisson();
        return Result.ok();
    }
}
