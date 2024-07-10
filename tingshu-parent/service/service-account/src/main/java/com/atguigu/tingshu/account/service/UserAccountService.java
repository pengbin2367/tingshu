package com.atguigu.tingshu.account.service;

import com.atguigu.tingshu.model.account.UserAccount;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserAccountService extends IService<UserAccount> {

    void decountUserAccount(String msg);

    void userAccountPaymentInfo(String msg);

    Object findAccountTradePage(Integer page, Integer size, String type);
}