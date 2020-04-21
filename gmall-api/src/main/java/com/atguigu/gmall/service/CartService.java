package com.atguigu.gmall.service;

import com.atguigu.gmall.beans.OmsCartItem;

import java.util.List;

public interface CartService {
    void addCart(OmsCartItem omsCartItem);

    OmsCartItem isCartExists(OmsCartItem omsCartItem);

    void updateCart(OmsCartItem omsCartItemFromDb);

    List<OmsCartItem> getCartList(String memberId);

    void checkCart(OmsCartItem omsCartItem);

    void delCarts(List<String> cartIds);
}
