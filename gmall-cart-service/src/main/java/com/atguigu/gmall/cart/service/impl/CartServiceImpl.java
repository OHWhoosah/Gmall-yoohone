package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.beans.OmsCartItem;
import com.atguigu.gmall.cart.mapper.OmsCartItemMapper;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    OmsCartItemMapper cartItemMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public void addCart(OmsCartItem omsCartItem) {

        cartItemMapper.insertSelective(omsCartItem);

        // 同步redis缓存
        Jedis jedis = redisUtil.getJedis();

        jedis.hset("user:"+omsCartItem.getMemberId()+":cart",omsCartItem.getId(), JSON.toJSONString(omsCartItem));

        jedis.close();

    }

    @Override
    public OmsCartItem isCartExists(OmsCartItem omsCartItem) {

        OmsCartItem omsCartItemParam = new OmsCartItem();
        omsCartItemParam.setMemberId(omsCartItem.getMemberId());
        omsCartItemParam.setProductSkuId(omsCartItem.getProductSkuId());
        OmsCartItem omsCartItemFromDb = cartItemMapper.selectOne(omsCartItemParam);

        return omsCartItemFromDb;
    }

    @Override
    public void updateCart(OmsCartItem omsCartItemFromDb) {

        Example e = new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("id",omsCartItemFromDb.getId());
        cartItemMapper.updateByExampleSelective(omsCartItemFromDb,e);
        // 同步redis缓存
        Jedis jedis = redisUtil.getJedis();

        jedis.hset("user:"+omsCartItemFromDb.getMemberId()+":cart",omsCartItemFromDb.getId(), JSON.toJSONString(omsCartItemFromDb));

        jedis.close();
    }

    @Override
    public List<OmsCartItem> getCartList(String memberId) {
        List<OmsCartItem> cartItems = new ArrayList<>();
        Jedis jedis = redisUtil.getJedis();
        List<String> hvals = jedis.hvals("user:" + memberId + ":cart");
        if(hvals!=null&&hvals.size()>0){
            for (String hval : hvals) {
                OmsCartItem omsCartItem = JSON.parseObject(hval,OmsCartItem.class);
                cartItems.add(omsCartItem);
            }
        }
        jedis.close();
        return cartItems;
    }

    @Override
    public void checkCart(OmsCartItem omsCartItem) {

        OmsCartItem omsCartItemForUpdate = new OmsCartItem();
        omsCartItemForUpdate.setIsChecked(omsCartItem.getIsChecked());

        Example e = new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("memberId",omsCartItem.getMemberId()).andEqualTo("productSkuId",omsCartItem.getProductSkuId());

        cartItemMapper.updateByExampleSelective(omsCartItemForUpdate,e);

        // 更新缓存的数据
        OmsCartItem omsCartItem1 = new OmsCartItem();
        omsCartItem1.setMemberId(omsCartItem.getMemberId());
        omsCartItem1.setProductSkuId(omsCartItem.getProductSkuId());
        OmsCartItem omsCartItem2 = cartItemMapper.selectOne(omsCartItem1);
        omsCartItem2.setTotalPrice(omsCartItem2.getPrice().multiply(omsCartItem2.getQuantity()));// 数据库中没有存储totalPrice字段

        // 同步redis缓存
        Jedis jedis = redisUtil.getJedis();

        jedis.hset("user:"+omsCartItem2.getMemberId()+":cart",omsCartItem2.getId(), JSON.toJSONString(omsCartItem2));

        jedis.close();

    }

    @Override
    public void delCarts(List<String> cartIds) {

        // 删除购物车数据

    }
}
