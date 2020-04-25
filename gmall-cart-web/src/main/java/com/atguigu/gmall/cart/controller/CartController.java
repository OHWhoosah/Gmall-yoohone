package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.beans.OmsCartItem;
import com.atguigu.gmall.beans.PmsSkuInfo;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CartController {

    @Reference
    CartService cartService;

    @Reference
    SkuService skuService;

    @LoginRequired(isNeededSuccess = false)
    @RequestMapping("checkCart")
    public String checkCart(ModelMap map, HttpServletRequest request, OmsCartItem omsCartItem) {

        List<OmsCartItem> omsCartItems = new ArrayList<>();

        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        if (StringUtils.isNotBlank(memberId)) {
            // 用户已经登录
            omsCartItem.setMemberId(memberId);
            // 更新db的选中状态
            cartService.checkCart(omsCartItem);

            omsCartItems = cartService.getCartList(memberId);
        } else {
            // 更新cookie的选中状态
        }

        map.put("cartList", omsCartItems);
        BigDecimal totalAmount = getTotalAmount(omsCartItems);
        map.put("totalAmount", totalAmount);
        return "cartListInner";
    }

    @LoginRequired(isNeededSuccess = false)
    @RequestMapping("cartList")
    public String cartList(ModelMap map, HttpServletRequest request) {
        List<OmsCartItem> cartList = new ArrayList<>();
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        if (StringUtils.isBlank(memberId)) {
            // 查cookie
            String cartListCookieStr = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookieStr)) {
                cartList = JSON.parseArray(cartListCookieStr, OmsCartItem.class);
            }
        } else {
            // 查db
            cartList = cartService.getCartList(memberId);
        }

        map.put("cartList", cartList);
        BigDecimal totalAmount = getTotalAmount(cartList);
        map.put("totalAmount", totalAmount);
        return "cartList";
    }

    private BigDecimal getTotalAmount(List<OmsCartItem> cartList) {

        BigDecimal totalAmount = new BigDecimal("0");

        if (cartList != null && cartList.size() > 0) {
            for (OmsCartItem omsCartItem : cartList) {
                if (omsCartItem.getIsChecked().equals("1")) {
                    totalAmount = totalAmount.add(omsCartItem.getTotalPrice());
                }
            }
        }

        return totalAmount;
    }

    @LoginRequired(isNeededSuccess = false)
    @RequestMapping("addToCart")
    public String addToCart(HttpServletRequest request, HttpSession session, HttpServletResponse response,
                            OmsCartItem omsCartItem) {
        PmsSkuInfo skuInfo = skuService.getSkuById(omsCartItem.getProductSkuId(), "");

        omsCartItem.setIsChecked("1");
        omsCartItem.setPrice(skuInfo.getPrice());
        omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductName(skuInfo.getSkuName());
        omsCartItem.setProductSkuId(skuInfo.getId());
        omsCartItem.setProductId(skuInfo.getProductId());
        omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
        omsCartItem.setCreateDate(new Date());

        // 添加购物车
        // 判断用户是否登录
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        List<OmsCartItem> cartList = new ArrayList<>();

        if (StringUtils.isBlank(memberId)) {
            // 用户没登录，数据放入浏览器客户端(cookie)
            String cartListCookieStr = CookieUtil.getCookieValue(request, "cartListCookie", true);// 获得浏览器cookie中的购物车数据
            if (StringUtils.isNotBlank(cartListCookieStr)) {
                cartList = JSON.parseArray(cartListCookieStr, OmsCartItem.class);
                // 判断添加的商品是否添加过
                boolean b = if_new_cart(cartList, omsCartItem);
                if (b) {
                    // 新车，添加
                    cartList.add(omsCartItem);
                } else {
                    // 老车，修改
                    for (OmsCartItem cartItem : cartList) {
                        if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
                            cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                            cartItem.setTotalPrice(cartItem.getPrice().multiply(cartItem.getQuantity()));
                            break;
                        }
                    }
                }
            } else {
                // 新建cookie，插入浏览器
                cartList = new ArrayList<>();
                cartList.add(omsCartItem);
            }
            // 覆盖cookie
            CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(cartList), 60 * 60 * 24, true);
        } else {
            omsCartItem.setMemberId(memberId);
            //omsCartItem.setMemberNickname();
            // 先判断是添加db还是更新db
            OmsCartItem omsCartItemFromDb = cartService.isCartExists(omsCartItem);
            // 用户登录，数据放入服务器的数据库
            if (omsCartItemFromDb == null || StringUtils.isBlank(omsCartItemFromDb.getProductSkuId())) {
                // 当前用户没有添加过当前sku商品，新增数据库
                cartService.addCart(omsCartItem);
            } else {
                // 当前用户添加过当前sku商品，更新数据库
                omsCartItemFromDb.setQuantity(omsCartItemFromDb.getQuantity().add(omsCartItem.getQuantity()));
                omsCartItemFromDb.setTotalPrice(omsCartItemFromDb.getPrice().multiply(omsCartItemFromDb.getQuantity()));
                cartService.updateCart(omsCartItemFromDb);
            }
        }

        return "redirect:/success.html";
    }

    private boolean if_new_cart(List<OmsCartItem> cartList, OmsCartItem omsCartItem) {

        boolean b = true;

        for (OmsCartItem cartItem : cartList) {
            if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
                b = false;
                break;
            }
        }

        return b;
    }
}
