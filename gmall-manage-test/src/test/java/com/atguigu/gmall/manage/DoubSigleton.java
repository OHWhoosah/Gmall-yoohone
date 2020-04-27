package com.atguigu.gmall.manage;

public class DoubSigleton {

    private static DoubSigleton instance;

    private DoubSigleton() {
    }

    public static synchronized DoubSigleton getInstance() {

        if (instance != null) {
            instance = new DoubSigleton();
        }
        return instance;

    }


}
