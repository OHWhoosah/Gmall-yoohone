package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.beans.OmsCartItem;
import com.atguigu.gmall.beans.OmsOrder;
import com.atguigu.gmall.beans.OmsOrderItem;
import com.atguigu.gmall.beans.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    CartService cartService;

    @Reference
    OrderService orderService;

    @Reference
    UserService userService;

    @LoginRequired
    @RequestMapping("submitOrder")
    public String submitOrder(HttpServletRequest request, ModelMap map,String deliveryAddressId,String tradeCode){
        // 获取用户信息
        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");
        boolean b = orderService.checkTradeCode(tradeCode,memberId);

        if(b){

            // 用户收获信息
           UmsMemberReceiveAddress umsMemberReceiveAddress =  userService.getAddressById(deliveryAddressId);

            // 根据用户获取购物车数据
            List<OmsCartItem> cartList = cartService.getCartList(memberId);
            // 根据购物车数据生成订单数据
            OmsOrder omsOrder = new OmsOrder();
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String orderSn = "gmall"+sdf.format(date)+System.currentTimeMillis();
            omsOrder.setOrderSn(orderSn);
            omsOrder.setPayAmount(getTotalAmount(cartList));
            omsOrder.setStatus("0");
            omsOrder.setTotalAmount(getTotalAmount(cartList));
            omsOrder.setSourceType(0);
            omsOrder.setReceiverRegion(umsMemberReceiveAddress.getRegion());
            omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
            omsOrder.setReceiverPostCode(umsMemberReceiveAddress.getPostCode());
            omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());
            omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
            omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
            omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());
            omsOrder.setOrderType(0);
            omsOrder.setMemberUsername(nickname);
            omsOrder.setMemberId(memberId);
            omsOrder.setCreateTime(new Date());

            List<OmsOrderItem> omsOrderItems = new ArrayList<OmsOrderItem>();
            List<String> cartIds = new ArrayList<>();
            for (OmsCartItem omsCartItem : cartList) {
                OmsOrderItem omsOrderItem = new OmsOrderItem();
                omsOrderItem.setProductSkuCode(omsCartItem.getProductSkuCode());
                omsOrderItem.setProductPrice(omsCartItem.getPrice());
                omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                omsOrderItem.setProductPic(omsCartItem.getProductPic());
                omsOrderItem.setProductName(omsCartItem.getProductName());
                omsOrderItem.setProductId(omsCartItem.getProductId());
                omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                omsOrderItem.setOrderSn(orderSn);

                omsOrderItems.add(omsOrderItem);
                cartIds.add(omsCartItem.getId());
            }
            omsOrder.setOmsOrderItems(omsOrderItems);

            // 将订单数据保存到数据库
            orderService.addOrder(omsOrder);

            // 删除购物车数据
            // cartService.delCarts(cartIds);

            System.out.println("获取用户信息");
            System.out.println("根据用户获取购物车数据");
            System.out.println("根据购物车数据生成订单数据");
            System.out.println("将订单数据保存到数据库");
            System.out.println("删除购物车数据");
            System.out.println("重定向到支付页面");
            return "redirect:http://payment.gmall.com:8099/index?orderSn="+orderSn+"&payAmount="+getTotalAmount(cartList)+"&nickname="+nickname;// 重定向到支付页面
        }else{
            return "tradeFail";
        }
    }

    @LoginRequired
    @RequestMapping("toTrade")
    public String toTrade(HttpServletRequest request, ModelMap map){

        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");

        // 查询购物车数据
        List<OmsCartItem> omsCartItems = cartService.getCartList(memberId);

        // 转化成订单数据
        List<OmsOrderItem> omsOrderItems = new ArrayList<>();
        for (OmsCartItem omsCartItem : omsCartItems) {
            if(omsCartItem.getIsChecked().equals("1")){
                OmsOrderItem omsOrderItem = new OmsOrderItem();
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                String orderSn = "gmall"+sdf.format(date)+System.currentTimeMillis();
                omsOrderItem.setOrderSn(orderSn);
                omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                omsOrderItem.setProductId(omsCartItem.getProductId());
                omsOrderItem.setProductName(omsCartItem.getProductName());
                omsOrderItem.setProductPic(omsCartItem.getProductPic());
                omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                omsOrderItem.setProductPrice(omsCartItem.getPrice());
                omsOrderItem.setProductSkuCode(omsCartItem.getProductSkuCode());
                omsOrderItems.add(omsOrderItem);
            }

        }
        map.put("orderDetailList",omsOrderItems);

        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = userService.getAddressListByMemberId(memberId);
        map.put("userAddressList",umsMemberReceiveAddresses);
        // 防止表单重复提交
        String tradeCode = orderService.genTradeCode(memberId);

        map.put("tradeCode",tradeCode);

        return "trade";
    }


    private BigDecimal getTotalAmount(List<OmsCartItem> cartList) {

        BigDecimal totalAmount = new BigDecimal("0");

        if(cartList!=null&&cartList.size()>0){
            for (OmsCartItem omsCartItem : cartList) {
                if(omsCartItem.getIsChecked().equals("1")){
                    totalAmount = totalAmount.add(omsCartItem.getTotalPrice());
                }
            }
        }

        return totalAmount;
    }
}
