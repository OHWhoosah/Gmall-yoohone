package com.atguigu.gmall.seckill.controller;

import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@Controller
public class SeckillController {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedissonClient redissonClient;

    @RequestMapping("seckill2")
    @ResponseBody
    public String seckill2(HttpServletRequest request){

        RSemaphore semaphore = redissonClient.getSemaphore("sku:118:stock");
        boolean b = semaphore.tryAcquire();
        if(b){
            System.out.println("抢购成功");
        }else{
            System.out.println("抢购失败");
        }

        return "success";
    }

    @RequestMapping("seckill")
    @ResponseBody
    public String seckill(HttpServletRequest request){
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String userId = UUID.randomUUID().toString();// 防止一个用户反复抢购
            String OK = jedis.set("user:"+userId+":118stock", "1", "nx", "px", 60 * 5 * 1000);
            if(StringUtils.isNotBlank(OK)&&OK.equals("OK")){
                // 抢库存
                jedis.watch("sku:118:stock");// jedis链接线程进行watch
                long stock = Long.parseLong(jedis.get("sku:118:stock"));
                if(stock>0){
                    Transaction multi = jedis.multi();
                    multi.incrBy("sku:118:stock",-1);
                    List<Object> exec = multi.exec();
                    if(exec!=null&&exec.size()>0){
                        Object o = exec.get(0);
                        System.out.println(request.getRemoteAddr()+"抢购成功，目前库存剩余数量："+o);
                    }else{
                        System.out.println(request.getRemoteAddr()+"抢购失败，原因：有其他小伙伴已经抢先下手");
                    }

                }else {
                    System.out.println("抢购失败，商品库存已经抢购完毕");
                }
            }
        }finally {
            jedis.close();
        }



        return "success";
    }
}
