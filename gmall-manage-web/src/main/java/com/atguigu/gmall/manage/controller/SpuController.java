package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.beans.PmsBaseSaleAttr;
import com.atguigu.gmall.beans.PmsProductImage;
import com.atguigu.gmall.beans.PmsProductInfo;
import com.atguigu.gmall.beans.PmsProductSaleAttr;
import com.atguigu.gmall.service.SpuService;
import com.atguigu.gmall.util.MyUploadUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@CrossOrigin
public class SpuController {

    @Reference
    SpuService spuService;

    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId){

        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrList(spuId);

        return pmsProductSaleAttrs;
    }



    @RequestMapping("spuImageList")
    @ResponseBody
    public List<PmsProductImage> spuImageList(String spuId){

        List<PmsProductImage> pmsProductImages = spuService.spuImageList(spuId);

        return pmsProductImages;
    }

    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(MultipartFile file){

        // 上传文件服务器
        String imgUrl = MyUploadUtil.uploadImage(file);

        return imgUrl;
    }


    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){

       spuService.saveSpuInfo(pmsProductInfo);

        return "success";
    }



    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<PmsBaseSaleAttr> baseSaleAttrList(){

        List<PmsBaseSaleAttr> pmsProductInfos = spuService.baseSaleAttrList();

        return pmsProductInfos;
    }

    @RequestMapping("spuList")
    @ResponseBody
    public List<PmsProductInfo> spuList(String catalog3Id){

        List<PmsProductInfo> pmsProductInfos = spuService.spuList(catalog3Id);

        return pmsProductInfos;
    }
}
