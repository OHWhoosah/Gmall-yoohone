package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.beans.PmsBaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsBaseAttrInfoMapper extends Mapper<PmsBaseAttrInfo> {
    List<PmsBaseAttrInfo> selectAttrInfoListByValueId(@Param("valueIds") String valueIds);
}
