package com.atguigu.tingshu.album.mapper;

import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/***
 * 专辑表的mapper映射
 */
@Mapper
public interface AlbumInfoMapper extends BaseMapper<AlbumInfo> {
    Page<AlbumListVo> selectAlbumListPage(Page<AlbumListVo> albumListVos,@Param("vo") AlbumInfoQuery albumInfoQuery);
}
