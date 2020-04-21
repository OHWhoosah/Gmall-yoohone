package com.atguigu.gmall.order.orderMqListener;

import com.atguigu.gmall.beans.OmsOrder;
import com.atguigu.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.math.BigDecimal;
import java.util.Date;

@Component
public class OrderConsumer {

    @Autowired
    OrderService orderService;

    @JmsListener(containerFactory = "jmsQueueListener",destination = "PAYMENT_SUCCESS_QUEUE")
    public void consumePaymentSuccess(MapMessage mapMessage) throws JMSException {
        String out_trade_no = mapMessage.getString("out_trade_no");
        Double pay_amount = mapMessage.getDouble("pay_amount");

        // 根据支付状态，更新订单信息
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setPayAmount(new BigDecimal(pay_amount));
        omsOrder.setPaymentTime(new Date());
        omsOrder.setOrderSn(out_trade_no);
        omsOrder.setStatus("1");
        orderService.updateOrder(omsOrder);

        System.out.println("已监听到"+out_trade_no+"号订单，订单消费PAYMENT_SUCCESS_QUEUE队列");

        // 发送订单支付成功队列
        orderService.sendOrderSuccessQueue(omsOrder);

    }

}
