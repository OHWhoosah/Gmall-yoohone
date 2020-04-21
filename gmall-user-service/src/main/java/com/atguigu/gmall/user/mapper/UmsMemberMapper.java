package com.atguigu.gmall.user.mapper;

import com.atguigu.gmall.beans.UmsMember;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UmsMemberMapper extends Mapper<UmsMember>{
    List<UmsMember> selectAllUser();
}
