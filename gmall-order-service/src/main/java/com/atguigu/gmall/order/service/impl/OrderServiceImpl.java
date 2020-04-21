package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.beans.OmsOrder;
import com.atguigu.gmall.beans.OmsOrderItem;
import com.atguigu.gmall.order.mapper.OmsOrderItemMapper;
import com.atguigu.gmall.order.mapper.OmsOrderMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.util.ActiveMQUtil;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OmsOrderMapper omsOrderMapper;

    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public String genTradeCode(String memberId) {
        Jedis jedis = null;
        jedis = redisUtil.getJedis();
        String tradeCode = UUID.randomUUID().toString();
        jedis.setex("user:" + memberId + ":tradeCode", 60 * 15,tradeCode );
        jedis.close();
        return tradeCode;
    }

    @Override
    public boolean checkTradeCode(String tradeCode,String memberId) {
        boolean b = false;

        Jedis jedis = null;
        jedis = redisUtil.getJedis();
//        String tradeCodeFromCache = jedis.get("user:" + memberId + ":tradeCode");
//        if(StringUtils.isNotBlank(tradeCodeFromCache)&&tradeCodeFromCache.equals(tradeCode)){
//            b = true;
//            jedis.del("user:" + memberId + ":tradeCode");
//        }
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long eval = (Long) jedis.eval(script, Collections.singletonList("user:" + memberId + ":tradeCode"),
                Collections.singletonList(tradeCode));
        if(new BigDecimal(eval).compareTo(new BigDecimal("0"))!=0){
            b = true;
        }
        jedis.close();
        return b;
    }

    @Override
    public void addOrder(OmsOrder omsOrder) {

        omsOrderMapper.insertSelective(omsOrder);
        String orderId = omsOrder.getId();

        List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();

        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItem.setOrderId(orderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);
        }

    }

    @Override
    public OmsOrder getOrderBySn(String orderSn) {
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(orderSn);
        OmsOrder omsOrder1 = omsOrderMapper.selectOne(omsOrder);

        OmsOrderItem omsOrderItem = new OmsOrderItem();
        omsOrderItem.setOrderSn(orderSn);
        List<OmsOrderItem> omsOrderItems = omsOrderItemMapper.select(omsOrderItem);

        omsOrder1.setOmsOrderItems(omsOrderItems);
        return omsOrder1;
    }

    @Override
    public void updateOrder(OmsOrder omsOrder) {

        Example example = new Example(OmsOrder.class);
        example.createCriteria().andEqualTo("orderSn",omsOrder.getOrderSn());

        omsOrderMapper.updateByExampleSelective(omsOrder,example);


    }

    @Override
    public void sendOrderSuccessQueue(OmsOrder omsOrder) {

        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();

        Connection connection = null;
        Session session = null;// 开启消息事务
        Queue paymentResultQueue = null; // 队列
        MessageProducer producer = null;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            paymentResultQueue = session.createQueue("ORDER_SUCCESS_QUEUE");
            //text文本格式，map键值格式
            TextMessage textMessage=new ActiveMQTextMessage();
            OmsOrder omsOrderDbParam = new OmsOrder();
            omsOrderDbParam.setOrderSn(omsOrder.getOrderSn());
            OmsOrder omsOrderResult = omsOrderMapper.selectOne(omsOrderDbParam);

            OmsOrderItem omsOrderItem = new OmsOrderItem();
            omsOrderItem.setOrderId(omsOrderResult.getId());
            List<OmsOrderItem> omsOrderItems = omsOrderItemMapper.select(omsOrderItem);

            omsOrderResult.setOmsOrderItems(omsOrderItems);

            textMessage.setText(JSON.toJSONString(omsOrderResult));

            producer = session.createProducer(paymentResultQueue);// 消息的生成者
            producer.send(textMessage);
            session.commit();
        } catch (JMSException e) {
            e.printStackTrace();
        }finally {
            try {
                producer.close();
                session.close();
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
