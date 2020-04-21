package com.atguigu.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.beans.PaymentInfo;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.service.PaymentService;
import com.atguigu.gmall.util.ActiveMQUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    AlipayClient alipayClient;

    @Override
    public void addPayment(PaymentInfo paymentInfo) {

        paymentInfoMapper.insertSelective(paymentInfo);

    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {

        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderSn",paymentInfo.getOrderSn());

        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);
    }

    @Override
    public void sendPaymentResult(PaymentInfo paymentInfo) {

        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();

        Connection connection = null;
        Session session = null;// 开启消息事务
        Queue paymentResultQueue = null; // 队列
        MessageProducer producer = null;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            paymentResultQueue = session.createQueue("PAYMENT_SUCCESS_QUEUE");
            //text文本格式，map键值格式
            MapMessage mapMessage=new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no",paymentInfo.getOrderSn());
            mapMessage.setDouble("pay_amount",paymentInfo.getTotalAmount().doubleValue());
            producer = session.createProducer(paymentResultQueue);// 消息的生成者
            producer.send(mapMessage);
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

    @Override
    public void sendDelayPaymentCheckQueue(String orderSn,long count) {
        // 发送orderSn的延迟检查队列
        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();

        Connection connection = null;
        Session session = null;// 开启消息事务
        Queue paymentResultQueue = null; // 队列
        MessageProducer producer = null;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            paymentResultQueue = session.createQueue("PAYMENT_CHECK_QUEUE");
            //text文本格式，map键值格式
            MapMessage mapMessage=new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no",orderSn);
            mapMessage.setLong("count",count);

            //消息延迟消费
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,1000*30);

            producer = session.createProducer(paymentResultQueue);// 消息的生成者
            producer.send(mapMessage);
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

    @Override
    public Map<String, Object> checkPayment(String out_trade_no) {

        // 调用支付宝，检查支付状态
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();

        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no",out_trade_no);
        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }


        if(response.isSuccess()){// isSuccess
            System.out.println("调用成功，交易已经创建");
            String tradeNo = response.getTradeNo();
            String tradeStatus = response.getTradeStatus();
            map.put("trade_no",tradeNo);
            map.put("trade_status",tradeStatus);
            return map;
        } else {
            System.out.println("调用失败，用户尚未创建交易");//
        }

        return null;
    }

    @Override
    public String checkPayStatus(String order_sn) {

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderSn(order_sn);
        PaymentInfo paymentInfo1 = paymentInfoMapper.selectOne(paymentInfo);

        return paymentInfo1.getPaymentStatus();
    }
}
