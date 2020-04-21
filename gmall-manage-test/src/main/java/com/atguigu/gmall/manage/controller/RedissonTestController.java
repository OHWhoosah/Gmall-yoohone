package com.atguigu.gmall.manage.controller;

import com.atguigu.gmall.util.RedisUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Controller
public class RedissonTestController {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedissonClient redissonClient;

    @RequestMapping("getMyStock")
    @ResponseBody
    public String getMyStock(){
        Jedis jedis = null;
        Long myStock = null;
        RLock lock = redissonClient.getLock("sku:lock");
        try {
            lock.lock(10, TimeUnit.SECONDS);// 10秒后自动解锁
            jedis = redisUtil.getJedis();
            BigDecimal b1 = new BigDecimal(jedis.get("myStock"));
            int i = b1.compareTo(new BigDecimal("0"));
            if(i>0){
                jedis.incrBy("myStock", -1);
                myStock = Long.parseLong(jedis.get("myStock"));
                System.out.println("目前库存剩余数量："+myStock);
            }
        }finally {
            lock.unlock();
            jedis.close();

        }

        return myStock+"";
    }
}
