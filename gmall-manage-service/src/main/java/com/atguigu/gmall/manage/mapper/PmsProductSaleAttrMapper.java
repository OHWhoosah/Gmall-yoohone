package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.beans.PmsProductSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsProductSaleAttrMapper extends Mapper<PmsProductSaleAttr>{

    List<PmsProductSaleAttr> selectSpuSaleAttrListCheckBySku(@Param("spuId") String spuId, @Param("skuId")String skuId);

}
