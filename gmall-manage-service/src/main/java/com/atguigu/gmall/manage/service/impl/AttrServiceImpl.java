package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.beans.PmsBaseAttrInfo;
import com.atguigu.gmall.beans.PmsBaseAttrValue;
import com.atguigu.gmall.manage.mapper.PmsBaseAttrInfoMapper;
import com.atguigu.gmall.manage.mapper.PmsBaseAttrValueMapper;
import com.atguigu.gmall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

@Service
public class AttrServiceImpl implements AttrService {


    @Autowired
    PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;


    @Autowired
    PmsBaseAttrValueMapper pmsBaseAttrValueMapper;
    
    @Override
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {

        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);

        for (PmsBaseAttrInfo baseAttrInfo : pmsBaseAttrInfos) {
            String attrId = baseAttrInfo.getId();
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(attrId);
            List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
            baseAttrInfo.setAttrValueList(pmsBaseAttrValues);
        }
        
        return pmsBaseAttrInfos;
    }

    @Override
    public void saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {

        String id = pmsBaseAttrInfo.getId();

        if(StringUtils.isBlank(id)){
            // 新增
            pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);

            String atrrId = pmsBaseAttrInfo.getId();

            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();

            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                pmsBaseAttrValue.setAttrId(atrrId);

                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
            }

        }else{
            // 修改属性

            // 删除对应的所有属性值

            // 重新插入属性值
        }

    }

    @Override
    public List<PmsBaseAttrInfo> attrInfoListByValueId(Set<String> valueIdSet) {

        String valueIds = StringUtils.join(valueIdSet, ",");

        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.selectAttrInfoListByValueId(valueIds);
        return pmsBaseAttrInfos;
    }
}
