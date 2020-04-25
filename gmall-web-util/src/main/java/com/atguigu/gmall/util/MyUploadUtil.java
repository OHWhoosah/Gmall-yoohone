package com.atguigu.gmall.util;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class MyUploadUtil {
    public static String uploadImage(MultipartFile file) {
        String path = MyUploadUtil.class.getClassLoader().getResource("traker.conf").getPath();
        try {
            // tracker地址，超时时间，链接时间
            ClientGlobal.init(path);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        // 根据配置获得tracker
        TrackerClient trackerClient = new TrackerClient();

        // 通过tracker获得一个可用的storage
        TrackerServer connection = null;
        try {
            connection = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 使用storage上传文件
        StorageClient storageClient = new StorageClient(connection,null);

        String url = "http://192.168.174.100";
        try {
            //a.jpg
            String originalFilename = file.getOriginalFilename();
            int i = originalFilename.lastIndexOf(".");
            String substring = originalFilename.substring(i + 1);
            String[] jpgs = storageClient.upload_file(file.getBytes(), substring, null);
            for (String jpg : jpgs) {
                url = url + "/" + jpg;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
        return url;
    }
}
