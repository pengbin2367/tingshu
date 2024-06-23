package com.atguigu.tingshu.album.mapper;

import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/***
 * 专辑表的mapper映射
 */
@Mapper
public interface AlbumInfoMapper extends BaseMapper<AlbumInfo> {
    Page<AlbumListVo> selectAlbumListPage(Page<AlbumListVo> albumListVos,@Param("vo") AlbumInfoQuery albumInfoQuery);

    @Update("update tingshu_album.album_info set include_track_count = include_track_count + #{num} where id = #{albumId}")
    int updateAlbumTrackCount(@Param("albumId") Long albumId, @Param("num") int num);
}
