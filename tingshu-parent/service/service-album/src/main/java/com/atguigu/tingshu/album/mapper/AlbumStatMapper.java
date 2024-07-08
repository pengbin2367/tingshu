package com.atguigu.tingshu.album.mapper;

import com.atguigu.tingshu.model.album.AlbumStat;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AlbumStatMapper extends BaseMapper<AlbumStat> {

    @Update("update tingshu_album.album_stat set stat_num = stat_num + #{num} where album_id = #{albumId} and stat_type = #{type} and is_deleted = 0")
    void updateByOrder(@Param("albumId") Long albumId, @Param("num") int num, @Param("type") String type);
}
