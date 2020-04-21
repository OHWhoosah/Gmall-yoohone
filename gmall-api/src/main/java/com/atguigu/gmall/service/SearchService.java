package com.atguigu.gmall.service;

import com.atguigu.gmall.beans.PmsSearchParam;
import com.atguigu.gmall.beans.PmsSearchSkuInfo;

import java.util.List;

public interface SearchService {
    List<PmsSearchSkuInfo> search(PmsSearchParam pmsSearchParam);
}
