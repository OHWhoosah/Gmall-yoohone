package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.beans.OmsOrder;
import com.atguigu.gmall.beans.PaymentInfo;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    @Autowired
    AlipayClient alipayClient;

    @Autowired
    PaymentService paymentService;

    @Reference
    OrderService orderService;

    @RequestMapping("alipay/callback/return")

    public String alipay_callback(Model model, HttpServletRequest request){

        String alipay_trade_no = request.getParameter("trade_no");//支付宝的交易单号
        String order_sn = request.getParameter("out_trade_no");// 外部订单号total_amount
        String pay_amount = request.getParameter("total_amount");


        // 系统支付的幂等性检查
        String status = paymentService.checkPayStatus(order_sn);

        if(status!=null&&!status.equals("已支付")){
            // 更新支付信息
            PaymentInfo paymentInfo = new PaymentInfo();
            // 交易单号
            // 支付状态
            String payment_status = "已支付";
            // 回调内容
            String callback_content = request.getQueryString();
            // 回调时间
            Date callback_time = new Date();

            paymentInfo.setOrderSn(order_sn);
            paymentInfo.setPaymentStatus(payment_status);
            paymentInfo.setCallbackTime(callback_time);
            paymentInfo.setAlipayTradeNo(alipay_trade_no);
            paymentInfo.setCallbackContent(callback_content);
            paymentInfo.setTotalAmount(new BigDecimal(pay_amount));
            paymentService.updatePayment(paymentInfo);

            // 发送系统消息 =》 更新订单状态  锁定商品库存  物流订单等等
            paymentService.sendPaymentResult(paymentInfo);

        }

        return "finish";
    }

    @RequestMapping("/alipay/submit")
    @ResponseBody
    public String alipay(Model model, HttpServletRequest request,String orderSn){

        // 查询对应订单信息
        OmsOrder omsOrder = orderService.getOrderBySn(orderSn);

        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址

        Map<String,Object> requestMap= new HashMap<>();
        requestMap.put("out_trade_no",orderSn);
        requestMap.put("product_code","FAST_INSTANT_TRADE_PAY");
        requestMap.put("total_amount",0.01);
        requestMap.put("subject",omsOrder.getOmsOrderItems().get(0).getProductName());

        alipayRequest.setBizContent(JSON.toJSONString(requestMap));//填充业务参数
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        // 保存支付数据
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderSn(orderSn);
        paymentInfo.setPaymentStatus("0");
        paymentInfo.setTotalAmount(omsOrder.getPayAmount());
        paymentInfo.setSubject(omsOrder.getOmsOrderItems().get(0).getProductName());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentService.addPayment(paymentInfo);

        System.out.println(form);

        // 发送一个检查支付结果的队列(延迟队列)
        paymentService.sendDelayPaymentCheckQueue(orderSn,7);// 调用支付宝的支付状态接口程序

        return form;
    }



    @RequestMapping("index")
    public String index(Model model, HttpServletRequest request){
        String orderSn = request.getParameter("orderSn");
        String nickname = request.getParameter("nickname");
        String payAmount = request.getParameter("payAmount");

        model.addAttribute("orderSn",orderSn);
        model.addAttribute("nickname",nickname);
        model.addAttribute("payAmount",payAmount);
        return "index";
    }
}
