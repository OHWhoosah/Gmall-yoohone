package com.atguigu.gmall.manage.test;

public class TestThread {

    public String a(){



        return  a();
    }

    public void b(){

    }

    public static void main(String[] args) {
        MyTicket myTicket = new MyTicket();
        for (int i = 0; i < 100; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    myTicket.buy();
                }
            }).start();
        }
    }
}
