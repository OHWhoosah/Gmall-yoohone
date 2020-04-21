package com.atguigu.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.beans.UmsMember;
import com.atguigu.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserController {

    @Reference
    UserService userService;

    @RequestMapping("index")
    @ResponseBody
    public String index(){
        return "index1";
    }

    @RequestMapping("getUserById")
    @ResponseBody
    public UmsMember getUserById(String id){

        UmsMember umsMember = userService.getUserById(id);
        return umsMember;
    }


    @RequestMapping("getAllUser")
    @ResponseBody
    public List<UmsMember> getAllUser(){

        List<UmsMember>  umsMembers = userService.getAllUser();
        return umsMembers;
    }
}
