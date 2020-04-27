package com.atguigu.gmall.manage;

public class DoubSigleton {

    private static DoubSigleton instance;

    private DoubSigleton() {
    }

    public static synchronized DoubSigleton getInstance() {

        if (instance != null) {
            instance = new DoubSigleton();
            System.out.println(instance.toString());
        }
        return instance;

    }

    public static void main(String[] args) {
        DoubSigleton doubSigleton = new DoubSigleton();
        DoubSigleton instance = DoubSigleton.getInstance();
        System.out.println(instance);
        System.out.println(doubSigleton.getClass());


    }


}
