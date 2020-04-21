package com.atguigu.gmall.service;

import com.atguigu.gmall.beans.OmsOrder;

public interface OrderService {
    String genTradeCode(String memberId);

    boolean checkTradeCode(String tradeCode,String memberId);

    void addOrder(OmsOrder omsOrder);

    OmsOrder getOrderBySn(String orderSn);

    void updateOrder(OmsOrder omsOrder);

    void sendOrderSuccessQueue(OmsOrder omsOrder);
}
