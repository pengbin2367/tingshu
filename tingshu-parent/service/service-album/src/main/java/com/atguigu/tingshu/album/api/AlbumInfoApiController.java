package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "专辑管理")
@RestController
@RequestMapping("/api/album/albumInfo")
@SuppressWarnings({"unchecked", "rawtypes"})
public class AlbumInfoApiController {

    @Autowired
    private AlbumInfoService albumInfoService;

    @PostMapping("/saveAlbumInfo")
    public Result saveAlbumInfo(@RequestBody AlbumInfoVo albumInfoVo) {
        albumInfoService.saveAlbumInfo(albumInfoVo);
        return Result.ok();
    }

    @PostMapping("/findUserAlbumPage/{page}/{size}")
    public Result<Page<AlbumListVo>> findUserAlbumPage(@PathVariable("page") Integer page, @PathVariable("size") Integer size, @RequestBody AlbumInfoQuery albumInfoQuery) {
        return Result.ok(albumInfoService.findUserAlbumPage(page, size, albumInfoQuery));
    }
}

