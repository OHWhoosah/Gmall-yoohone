package com.atguigu.gmall.manage;

public class Sigleton {
    private static final Sigleton instance = new Sigleton();

    private Sigleton() {
    }

    public static Sigleton getInstance() {

        return instance;
    }
}
