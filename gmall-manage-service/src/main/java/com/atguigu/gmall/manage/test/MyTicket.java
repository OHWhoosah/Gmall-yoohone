package com.atguigu.gmall.manage.test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyTicket {

    private long num = 100l;

    Lock lock = new ReentrantLock();

    public void  buy(){
        try {
            lock.lock();
            //System.out.println("线程"+Thread.currentThread().getName()+"买了一张票");
            num--;
            System.out.println("当前剩余票数：" + num + "张");
        }finally {
            lock.unlock();
        }

    }

}
