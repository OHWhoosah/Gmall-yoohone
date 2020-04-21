package com.atguigu.gmall.service;

import com.atguigu.gmall.beans.PmsSkuInfo;

import java.util.List;

public interface SkuService {
    void saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo getSkuById(String skuId,String ip);

    List<PmsSkuInfo> getSaleAttrValuesBySpuId(String spuId);

    List<PmsSkuInfo> getSearchSkuInfo();
}
