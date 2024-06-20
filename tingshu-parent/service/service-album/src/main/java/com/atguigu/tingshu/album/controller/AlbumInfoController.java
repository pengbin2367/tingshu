package com.atguigu.tingshu.album.controller;

import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.AlbumInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/***
 * 专辑管理的控制层
 */
@RestController
@RequestMapping(value = "/api/album/info")
public class AlbumInfoController {


    @Autowired
    private AlbumInfoService albumInfoService;

    /**
     * 查询所有的专辑数据
     * @return
     */
    @GetMapping(value = "/findAll")
    public Result findAll(){
       return Result.ok(albumInfoService.findAll());
    }

    /**
     * 查询单条数据
     * 地址(外部用户使用): http://localhost:8501/api/album/info/findOne?id=1
     * 地址(内部调用或者feign): http://localhost:8501/api/album/info/findOne/1
     * @param id
     * @return
     */
    @GetMapping(value = "/findOne/{id}")
    public Result findOne(@PathVariable(value = "id") Long id){
        return Result.ok(albumInfoService.findOne(id));
    }

    /**
     * 条件查询
     * @param albumInfo
     * @return
     */
    @PostMapping(value = "/find")
    public Result find(@RequestBody AlbumInfo albumInfo){
        return Result.ok(albumInfoService.find(albumInfo));
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/page/{page}/{size}")
    public Result page(@PathVariable(value = "page") Integer page,
                       @PathVariable(value = "size") Integer size){
        return Result.ok(albumInfoService.page(page, size));
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/page/{page}/{size}")
    public Result page(@PathVariable(value = "page") Integer page,
                       @PathVariable(value = "size") Integer size,
                       @RequestBody AlbumInfo albumInfo){
        return Result.ok(albumInfoService.page(page, size, albumInfo));
    }
}
