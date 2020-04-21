package com.atguigu.gmall.manage.test;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

public class Test {

    public static void main(String[] args) throws Exception{

        // 先连链接tracker
        String path = Test.class.getClassLoader().getResource("tracker.conf").getPath();
        System.out.println(path);
        ClientGlobal.init(path);

        TrackerClient trackerClient = new TrackerClient();

        TrackerServer connection = trackerClient.getConnection();

        // 通过tracker获得storage
        StorageClient storageClient = new StorageClient(connection,null);

        String imgUrl = "Http://192.168.222.20";

        // 通过storage上传
        String[] jpgs = storageClient.upload_file("D:\\a.jpg", "jpg", null);

        for (String jpg : jpgs) {
            imgUrl += "/"+jpg;
        }
        System.out.println(imgUrl);

    }
}
