package com.atguigu.tingshu.album.mapper;

import com.atguigu.tingshu.model.album.AlbumInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/***
 * 专辑表的mapper映射
 */
@Mapper
public interface AlbumInfoMapper extends BaseMapper<AlbumInfo> {
}
