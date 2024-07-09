package com.atguigu.tingshu.user.mapper;

import com.atguigu.tingshu.model.user.UserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {

    @Update("update tingshu_user.user_info set is_vip = #{i} where id = #{id}")
    void updateUserVip(Long id, int i);
}
