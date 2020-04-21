package com.atguigu.gmall.service;

import com.atguigu.gmall.beans.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    void addPayment(PaymentInfo paymentInfo);

    void updatePayment(PaymentInfo paymentInfo);

    void sendPaymentResult(PaymentInfo paymentInfo);

    void sendDelayPaymentCheckQueue(String orderSn,long count);

    Map<String,Object> checkPayment(String out_trade_no);

    String checkPayStatus(String order_sn);
}
