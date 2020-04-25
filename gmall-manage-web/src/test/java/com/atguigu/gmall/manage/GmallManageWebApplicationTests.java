package com.atguigu.gmall.manage;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageWebApplicationTests {



	@Test
	public void contextLoads() throws IOException, MyException {

		String path = GmallManageWebApplicationTests.class.getClassLoader().getResource("traker.conf").getPath();

		ClientGlobal.init(path);// tracker地址，超时时间，链接时间

		// 根据配置获得tracker
		TrackerClient trackerClient = new TrackerClient();

		TrackerServer connection = trackerClient.getConnection();// 通过tracker获得一个可用的storage

		StorageClient storageClient = new StorageClient(connection,null);// 使用storage上传文件

		String[] jpgs = storageClient.upload_file("d:/a.jpg", "jpg", null);

		String url = "http://192.168.174.100";
		for (String jpg : jpgs) {
			url = url + "/" + jpg;
		}
		System.out.println(url);


	}

}
