package com.atguigu.gmall.search.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CatalogJsonTest {

    public static void main(String[] args) throws IOException {
        String catalogJson = "[{'id':1,'name':'衣服'},{'id':2,'name':'电脑'}]";

        File file = new File("d:/catalog.json");

        FileOutputStream fileOutputStream = new FileOutputStream(file);

        fileOutputStream.write(catalogJson.getBytes());

    }
}
