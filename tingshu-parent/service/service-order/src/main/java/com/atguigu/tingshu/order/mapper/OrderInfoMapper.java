package com.atguigu.tingshu.order.mapper;

import com.atguigu.tingshu.model.order.OrderInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    @Select("select count(1) from tingshu_order.order_info t1 inner join tingshu_order.order_detail t2 on t1.id = t2.order_id where t1.user_id = #{userId} and t1.item_type = '1001' and t2.item_id = #{albumId} and (t1.order_status = '0901' or t1.order_status = '0902') and t1.is_deleted = 0 and t2.is_deleted = 0")
    int selectAlbumOrderCount(@Param("userId") Long userId, @Param("albumId") Long itemId);

    @Select("select t2.item_id from tingshu_order.order_info t1 inner join tingshu_order.order_detail t2 on t1.id = t2.order_id where t1.user_id = #{userId} and t1.item_type = '1002' and t1.order_status = '0901' and t1.is_deleted = 0 and t2.is_deleted = 0 group by t2.item_id")
    Set<Long> selectTrackOrderIds(@Param("userId") Long userId);
}
