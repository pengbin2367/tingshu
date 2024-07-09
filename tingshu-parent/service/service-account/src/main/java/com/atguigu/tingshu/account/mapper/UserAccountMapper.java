package com.atguigu.tingshu.account.mapper;

import com.atguigu.tingshu.model.account.UserAccount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccount> {

    @Update("update tingshu_account.user_account set available_amount = available_amount - #{money} where user_id = #{userId} and available_amount >= #{money} and is_deleted = 0")
    int updateAvailableAmount(@Param("userId") Long userId, @Param("money") BigDecimal money);

    @Update("update tingshu_account.user_account set available_amount = available_amount + #{money} where user_id = #{userId} and is_deleted = 0")
    int addAvailableAmount(@Param("userId") Long userId, @Param("money") BigDecimal money);
}
