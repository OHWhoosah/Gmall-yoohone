package com.atguigu.gmall;

import org.junit.experimental.categories.Categories;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class GmallManageWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallManageWebApplication.class, args);
	}

}
