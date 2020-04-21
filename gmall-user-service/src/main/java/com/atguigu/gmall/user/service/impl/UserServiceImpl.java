package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.beans.UmsMember;
import com.atguigu.gmall.beans.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.user.mapper.UmsMemberMapper;
import com.atguigu.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UmsMemberMapper umsMemberMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;


    @Override
    public List<UmsMember> getAllUser() {
        List<UmsMember> umsMembers = umsMemberMapper.selectAllUser();
        return umsMembers;
    }

    @Override
    public UmsMember getUserById(String id) {

        UmsMember umsMember = new UmsMember();
        umsMember.setId(id);
        UmsMember umsMember1 = umsMemberMapper.selectOne(umsMember);

        return umsMember1;
    }

    @Override
    public UmsMember login(UmsMember umsMember) {

        UmsMember umsMemberParam = new UmsMember();

        umsMemberParam.setUsername(umsMember.getUsername());
        umsMemberParam.setPassword(umsMember.getPassword());

        UmsMember umsMember1 = umsMemberMapper.selectOne(umsMemberParam);

        return umsMember1;
    }

    @Override
    public void addUserCache(String token, UmsMember umsMemberFromDb) {
        String tokenKey = "user:"+umsMemberFromDb.getId()+":token";
        String userKey = "user:"+umsMemberFromDb.getId()+":info";

        Jedis jedis = null;

        jedis = redisUtil.getJedis();

        jedis.setex(tokenKey,60*60,token);
        jedis.setex(userKey,60*60, JSON.toJSONString(umsMemberFromDb));

        jedis.close();
    }

    @Override
    public UmsMember addUser(UmsMember umsMember) {
        umsMemberMapper.insertSelective(umsMember);

        return umsMember;
    }

    @Override
    public UmsMember isUidExists(String uid) {

        UmsMember umsMember = new UmsMember();
        umsMember.setSourceUid(uid);
        UmsMember umsMember1 = umsMemberMapper.selectOne(umsMember);

        return umsMember1;
    }

    @Override
    public List<UmsMemberReceiveAddress> getAddressListByMemberId(String memberId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);

        return umsMemberReceiveAddresses;
    }

    @Override
    public UmsMemberReceiveAddress getAddressById(String deliveryAddress) {

        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setId(deliveryAddress);
        UmsMemberReceiveAddress umsMemberReceiveAddress1 = umsMemberReceiveAddressMapper.selectOne(umsMemberReceiveAddress);
        return umsMemberReceiveAddress1;
    }
}
