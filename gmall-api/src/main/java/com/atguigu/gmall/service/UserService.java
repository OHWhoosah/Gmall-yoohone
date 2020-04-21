package com.atguigu.gmall.service;

import com.atguigu.gmall.beans.UmsMember;
import com.atguigu.gmall.beans.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {
    List<UmsMember> getAllUser();

    UmsMember getUserById(String id);

    UmsMember login(UmsMember umsMember);

    void addUserCache(String token, UmsMember umsMemberFromDb);

    UmsMember addUser(UmsMember umsMember);

    UmsMember isUidExists(String uid);

    List<UmsMemberReceiveAddress> getAddressListByMemberId(String memberId);

    UmsMemberReceiveAddress getAddressById(String deliveryAddress);
}
