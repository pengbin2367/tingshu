package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.model.album.AlbumInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/***
 * 专辑管理相关的接口类
 */
public interface AlbumInfoService extends IService<AlbumInfo> {

    /**
     * 查询所有专辑的数据
     * @return
     */
    public List<AlbumInfo> findAll();

    /**
     * 查询单条数据
     * @param id
     * @return
     */
    public AlbumInfo findOne(Long id);

    /**
     * 新增
     * @param albumInfo
     */
    public void add(AlbumInfo albumInfo);

    /**
     * 修改
     * @param albumInfo
     */
    public void update(AlbumInfo albumInfo);

    /**
     * 删除
     * @param id
     */
    public void deleteById(Long id);

    /**
     * 条件查询
     * @param albumInfo
     * @return
     */
    public List<AlbumInfo> find(AlbumInfo albumInfo);

    /**
     * 分页查询
     *
     * @param page
     * @param size
     * @return
     */
    public Page<AlbumInfo> page(Integer page, Integer size);


    /**
     * 分页条件查询
     * @param page
     * @param size
     * @param albumInfo
     * @return
     */
    public Page<AlbumInfo> page(Integer page, Integer size, AlbumInfo albumInfo);


}
