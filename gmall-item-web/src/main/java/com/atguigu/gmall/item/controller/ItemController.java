package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.beans.PmsProductSaleAttr;
import com.atguigu.gmall.beans.PmsSkuInfo;
import com.atguigu.gmall.beans.PmsSkuSaleAttrValue;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    @Reference
    SkuService skuService;

    @Reference
    SpuService spuService;


    @RequestMapping("index")
    public String index(ModelMap map) {

        List<String> list = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            list.add("元素" + i);
        }
        map.put("list", list);
        map.put("hello", "hello thymeleaf");

        return "index";
    }

    @RequestMapping("{skuId}.html")
    public String html(@PathVariable String skuId, ModelMap map, HttpServletRequest request) {
        // 查询商品详情
        //获取客户端的IP地址的方法remoteAddr
        String remoteAddr = request.getRemoteAddr();
        PmsSkuInfo pmsSkuInfo = skuService.getSkuById(skuId, remoteAddr);
        map.put("skuInfo", pmsSkuInfo);

        // 查出属性的双重集合
        if (pmsSkuInfo != null) {
            String spuId = pmsSkuInfo.getProductId();
            List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrListCheckBySku(spuId, skuId);

            map.put("spuSaleAttrListCheckBySku", pmsProductSaleAttrs);

            // 生成一个销售属性值对应skuId的hash表
            List<PmsSkuInfo> pmsSkuInfosForSaleAttrValueMap = skuService.getSaleAttrValuesBySpuId(spuId);
            Map<String, String> pmsSkuInfosMap = new HashMap<>();
            for (PmsSkuInfo skuInfo : pmsSkuInfosForSaleAttrValueMap) {
                String skuIdv = skuInfo.getId();
                List<PmsSkuSaleAttrValue> skuSaleAttrValueListk = skuInfo.getSkuSaleAttrValueList();
                String skuSaleAttrValuesk = "";
                for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueListk) {
                    String saleAttrValueId = pmsSkuSaleAttrValue.getSaleAttrValueId();

                    skuSaleAttrValuesk = skuSaleAttrValuesk + "|" + saleAttrValueId;
                }
                pmsSkuInfosMap.put(skuSaleAttrValuesk, skuIdv);
            }
            map.put("skuSaleAttrValueJSON", JSON.toJSONString(pmsSkuInfosMap));
        }

        return "item";
    }

}
