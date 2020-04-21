package com.atguigu.gmall.service;

import com.atguigu.gmall.beans.PmsBaseAttrInfo;

import java.util.List;
import java.util.Set;

public interface AttrService {
    List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);

    void saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    List<PmsBaseAttrInfo> attrInfoListByValueId(Set<String> valueIdSet);
}
