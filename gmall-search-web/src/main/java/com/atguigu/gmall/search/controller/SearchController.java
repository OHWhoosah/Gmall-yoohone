package com.atguigu.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.beans.*;
import com.atguigu.gmall.service.AttrService;
import com.atguigu.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
public class SearchController {

    @Reference
    SearchService searchService;

    @Reference
    AttrService attrService;

    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap map) {

        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.search(pmsSearchParam);
        Set<String> valueIdSet = new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue.getValueId();
                valueIdSet.add(valueId);
            }
        }

        // 面包屑
        List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
        // 属性列表
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.attrInfoListByValueId(valueIdSet);
        // 删除被选中的属性
        String[] valueIds = pmsSearchParam.getValueId();
        if (valueIds != null && valueIds.length > 0) {
            for (String valueId : valueIds) {
                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                pmsSearchCrumb.setValueId(valueId);
                pmsSearchCrumb.setUrlParam(getCurrentUrl(pmsSearchParam,valueId));
                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
                while (iterator.hasNext()) {
                    PmsBaseAttrInfo next = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = next.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        String id = pmsBaseAttrValue.getId();
                        if(id.equals(valueId)){
                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                            iterator.remove();
                        }
                    }
                }
                pmsSearchCrumbs.add(pmsSearchCrumb);
            }
        }

        map.put("skuLsInfoList", pmsSearchSkuInfos);
        map.put("attrList", pmsBaseAttrInfos);
        String urlParam = getCurrentUrl(pmsSearchParam);
        map.put("urlParam", urlParam);
        map.put("attrValueSelectedList", pmsSearchCrumbs);
        return "list";
    }

    private String getCurrentUrl(PmsSearchParam pmsSearchParam,String... ids) {
        String currentUrl = "";

        // 根据参数拼接当前请求
        String keyword = pmsSearchParam.getKeyword();

        if (StringUtils.isNotBlank(keyword)) {
            currentUrl = currentUrl + "keyword=" + keyword;
        }

        String catalog3Id = pmsSearchParam.getCatalog3Id();
        if (StringUtils.isNotBlank(catalog3Id)) {
            currentUrl = currentUrl + "catalog3Id=" + catalog3Id;
        }

        String[] valueIds = pmsSearchParam.getValueId();

        if (valueIds != null && valueIds.length > 0) {
            for (String valueId : valueIds) {
                if(ids!=null&&ids.length>0){
                    if(!valueId.equals(ids[0])){
                        currentUrl = currentUrl + "&valueId=" + valueId;
                    }
                }else{
                    currentUrl = currentUrl + "&valueId=" + valueId;
                }
            }
        }

        return currentUrl;
    }

    @RequestMapping("index")
    @LoginRequired(isNeededSuccess = false)
    public String index() {
        return "index";
    }
}
