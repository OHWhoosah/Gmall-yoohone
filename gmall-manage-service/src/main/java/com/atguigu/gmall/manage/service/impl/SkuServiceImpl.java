package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.beans.PmsSkuAttrValue;
import com.atguigu.gmall.beans.PmsSkuImage;
import com.atguigu.gmall.beans.PmsSkuInfo;
import com.atguigu.gmall.beans.PmsSkuSaleAttrValue;
import com.atguigu.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuImageMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuInfoMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {

        // 插入sku信息
        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);

        String skuId = pmsSkuInfo.getId();

        // 插入平台属性
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();

        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
            pmsSkuAttrValue.setSkuId(skuId);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }

        // 插入图片
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(skuId);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }

        // 插入销售属性
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }

    }

    public PmsSkuInfo getSkuByIdFromDb(String skuId) {
        // 缓存不存在，查询数据库
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo pmsSkuInfoReturn = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(pmsSkuImage);

        if (pmsSkuInfoReturn != null) {
            pmsSkuInfoReturn.setSkuImageList(pmsSkuImages);
        }
        return pmsSkuInfoReturn;
    }

    @Override
    public PmsSkuInfo getSkuById(String skuId, String ip) {
        System.out.println(Thread.currentThread().getName() + ":" + ip + "开始访问" + skuId + "商品详情页面");
        Jedis jedis = null;
        PmsSkuInfo pmsSkuInfoReturn = new PmsSkuInfo();
        try {
            // 查询缓存
            jedis = redisUtil.getJedis();
            String skuJSON = jedis.get("sku:" + skuId + ":info");
            if (StringUtils.isNotBlank(skuJSON)) {
                // 查询缓存成功
                System.out.println(Thread.currentThread().getName() + ":" + ip + "缓存查询成功：" + skuId + "直接返回结果");
                pmsSkuInfoReturn = JSON.parseObject(skuJSON, PmsSkuInfo.class);
            } else {
                // 查询缓存失败，查询db
                // 查询完数据库，返回结果，同步缓存
                System.out.println(Thread.currentThread().getName() + ":" + ip + "申请分布式锁");
                // 防止删除其他人的锁
                String lockNo = UUID.randomUUID().toString();
                String OK = jedis.set("sku:" + skuId + ":lock", lockNo, "nx", "px", 3000);
                if (StringUtils.isNotBlank(OK) && OK.equals("OK")) {
                    System.out.println(Thread.currentThread().getName() + ":" + ip + "申请分布式锁成功");
                    // 访问db是需要加入分布式锁的限制，限制db的访问频率
                    pmsSkuInfoReturn = getSkuByIdFromDb(skuId);
                    if (pmsSkuInfoReturn != null) {
                        System.out.println(Thread.currentThread().getName() + ":" + ip + "同步缓存，然后解锁");
                        jedis.set("sku:" + skuId + ":info", JSON.toJSONString(pmsSkuInfoReturn));
                        //将锁解开
                        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', " +
                                "KEYS[1]) else return 0 end";
                        Object eval = jedis.eval(script, Collections.singletonList("sku:" + skuId + ":lock"),
                                Collections.singletonList(lockNo));
                        /**
                         *我们解锁只需要两行代码就搞定了！第一行代码，我们写了一个简单的Lua脚本代码
                         *第二行代码，我们将Lua代码传到jedis.eval()方法里，并使参数KEYS[1]赋值为lockKey，
                         * ARGV[1]赋值为requestId。eval()方法是将Lua代码交给Redis服务端执行。
                         *那么这段Lua代码的功能是什么呢？其实很简单，首先获取锁对应的value值，检查是否与requestId相等，如果相等则删除锁（解锁）。
                         * 那么为什么要使用Lua语言来实现呢？因为要确保上述操作是原子性的。
                         *那么为什么执行eval()方法可以确保原子性，源于Redis的特性，下面是官网对eval命令的部分解释：
                         * 简单来说，就是在eval命令执行Lua代码的时候，Lua代码将被当成一个命令去执行，并且直到eval命令执行完成，Redis才会执行其他命令。
                         *
                         * **/
                    }
                } else {
                    // 自旋
                    System.out.println(Thread.currentThread().getName() + ":" + ip + "申请分布式锁失败，开始自选，三妙后重新访问该功能。。。");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return getSkuById(skuId, ip);
                }

            }
        } finally {
            jedis.close();
        }
        return pmsSkuInfoReturn;
    }

    @Override
    public List<PmsSkuInfo> getSaleAttrValuesBySpuId(String spuId) {

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setProductId(spuId);
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.select(pmsSkuInfo);

        for (PmsSkuInfo skuInfo : pmsSkuInfos) {
            String skuId = skuInfo.getId();

            PmsSkuSaleAttrValue pmsSkuSaleAttrValue = new PmsSkuSaleAttrValue();
            pmsSkuSaleAttrValue.setSkuId(skuId);
            List<PmsSkuSaleAttrValue> pmsSkuSaleAttrValues = pmsSkuSaleAttrValueMapper.select(pmsSkuSaleAttrValue);

            skuInfo.setSkuSaleAttrValueList(pmsSkuSaleAttrValues);
        }

        return pmsSkuInfos;
    }

    @Override
    public List<PmsSkuInfo> getSearchSkuInfo() {

        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();

        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            String skuId = pmsSkuInfo.getId();

            PmsSkuImage pmsSkuImage = new PmsSkuImage();
            pmsSkuImage.setSkuId(skuId);
            List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(pmsSkuImage);

            pmsSkuInfo.setSkuImageList(pmsSkuImages);

            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(skuId);
            List<PmsSkuAttrValue> pmsSkuAttrValues = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);
            pmsSkuInfo.setSkuAttrValueList(pmsSkuAttrValues);
        }


        return pmsSkuInfos;
    }
}
