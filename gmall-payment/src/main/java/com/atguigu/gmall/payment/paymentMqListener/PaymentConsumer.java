package com.atguigu.gmall.payment.paymentMqListener;

import com.atguigu.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Map;

@Component
public class PaymentConsumer {

    @Autowired
    PaymentService paymentService;

    @JmsListener(containerFactory = "jmsQueueListener",destination = "PAYMENT_CHECK_QUEUE")
    public void consumePaymentCheckQueue(MapMessage mapMessage) throws JMSException {
        String out_trade_no = mapMessage.getString("out_trade_no");
        long count = mapMessage.getLong("count");

        System.out.println("开始检查支付状态。。。订单号："+out_trade_no);

        // 系统支付的幂等性检查
        String status = paymentService.checkPayStatus(out_trade_no);
        if(status!=null&&!status.equals("已支付")){
            // 检查支付状态
            Map<String,Object> map = paymentService.checkPayment(out_trade_no);

            if(map!=null&&(((String)map.get("trade_status")).equals("TRADE_SUCCESS")||((String)map.get("trade_status")).equals("TRADE_FINISHED"))){
                // 保存支付信息，发送支付成功队列
                System.out.println("订单号："+out_trade_no+"已经支付成功，继续后续操作");
                //
            }else{
                // 没有支付，再次发送检查队列
                System.out.println("订单号："+out_trade_no+"尚未支付成功，再次发送检查队列，剩余检查次数："+count);
                if(count>0){
                    System.out.println("订单号："+out_trade_no+"检查次数尚未用尽，再次发送检查队列"+count);
                    count --;
                    paymentService.sendDelayPaymentCheckQueue(out_trade_no,count);
                }else {
                    System.out.println("订单号："+out_trade_no+"检查次数耗尽，停止检查，调用关单服务");
                }
            }
        }
    }
}
