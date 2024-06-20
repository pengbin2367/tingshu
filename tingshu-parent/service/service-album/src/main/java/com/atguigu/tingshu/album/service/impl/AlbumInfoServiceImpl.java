package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/***
 * 专辑管理相关的接口类的实现类
 */
@Service
public class AlbumInfoServiceImpl extends ServiceImpl<AlbumInfoMapper, AlbumInfo> implements AlbumInfoService {

    @Autowired
    private AlbumInfoMapper albumInfoMapper;

    /**
     * 查询所有专辑的数据
     *
     * @return
     */
    @Override
    public List<AlbumInfo> findAll() {
        return albumInfoMapper.selectList(null);
    }

    /**
     * 查询单条数据
     *
     * @param id
     * @return
     */
    @Override
    public AlbumInfo findOne(Long id) {
        return albumInfoMapper.selectById(id);
    }

    /**
     * 新增
     *
     * @param albumInfo
     */
    @Override
    public void add(AlbumInfo albumInfo) {
        //校验

        //保存
        int insert = albumInfoMapper.insert(albumInfo);
        if(insert <= 0){
            throw new GuiguException(201, "新增专辑失败");
        }
    }

    /**
     * 修改
     *
     * @param albumInfo
     */
    @Override
    public void update(AlbumInfo albumInfo) {
        //参数校验

        int i = albumInfoMapper.updateById(albumInfo);
        if(i < 0){
            throw new GuiguException(201, "修改专辑失败");
        }
    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        //delete语句,update set is_delete=1 where id = ?
        int i = albumInfoMapper.deleteById(id);
        if(i < 0){
            throw new GuiguException(201, "删除专辑失败");
        }
    }

    /**
     * 条件查询
     *
     * @param albumInfo
     * @return
     */
    @Override
    public List<AlbumInfo> find(AlbumInfo albumInfo) {
        //拼接条件
        LambdaQueryWrapper<AlbumInfo> wrapper = buildQueryWapper(albumInfo);
        //执行查询
        return albumInfoMapper.selectList(wrapper);
    }



    /**
     * 分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<AlbumInfo> page(Integer page, Integer size) {
        return albumInfoMapper.selectPage(new Page<>(page, size), null);
    }

    /**
     * 分页条件查询
     *
     * @param page
     * @param size
     * @param albumInfo
     * @return
     */
    @Override
    public Page<AlbumInfo> page(Integer page, Integer size, AlbumInfo albumInfo) {
        //拼接条件
        LambdaQueryWrapper<AlbumInfo> wrapper = buildQueryWapper(albumInfo);
        //分页条件查询
        return albumInfoMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 拼接条件
     * @param albumInfo
     * @return
     */
    private LambdaQueryWrapper<AlbumInfo> buildQueryWapper(AlbumInfo albumInfo) {
        //声明查询条件构造器
        LambdaQueryWrapper<AlbumInfo> wrapper = new LambdaQueryWrapper<>();
        //专辑名字: "" null
        String albumTitle = albumInfo.getAlbumTitle();
        if(StringUtils.isNotEmpty(albumTitle)){
            //不为空的时候作为查询条件
            wrapper.like(AlbumInfo::getAlbumTitle, albumTitle);
        }
        //三级分类id
        Long category3Id = albumInfo.getCategory3Id();
        if(category3Id != null){
            //不为空作为查询条件
            wrapper.eq(AlbumInfo::getCategory3Id, category3Id);
        }

        return wrapper;
    }
}
