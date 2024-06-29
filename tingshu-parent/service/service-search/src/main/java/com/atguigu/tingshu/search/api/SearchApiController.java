package com.atguigu.tingshu.search.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.query.search.AlbumIndexQuery;
import com.atguigu.tingshu.search.service.SearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "搜索专辑管理")
@RestController
@RequestMapping("/api/search/albumInfo")
@SuppressWarnings({"unchecked", "rawtypes"})
public class SearchApiController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/channel/{category1Id}")
    public Result channel(@PathVariable("category1Id") Long category1Id) {
        return Result.ok(searchService.channel(category1Id));
    }

    @PostMapping
    public Result search(@RequestBody AlbumIndexQuery albumIndexQuery) {
        return Result.ok(searchService.search(albumIndexQuery));
    }

    @GetMapping("/completeSuggest/{keywords}")
    public Result completeSuggest(@PathVariable(value = "keywords") String keywords) {
        return Result.ok(searchService.completeSuggest(keywords));
    }

    @GetMapping("/{albumId}")
    public Result<JSONObject> getAlbumDetails(@PathVariable(value = "albumId") Long albumId) {
        return Result.ok(searchService.getAlbumDetails(albumId));
    }
}

