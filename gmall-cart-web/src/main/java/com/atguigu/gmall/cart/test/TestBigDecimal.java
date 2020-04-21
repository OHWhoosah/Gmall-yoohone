package com.atguigu.gmall.cart.test;

import java.math.BigDecimal;

public class TestBigDecimal {

    public static void main(String[] args) {
        // 1 数值的初始化
        BigDecimal b1 = new BigDecimal(0.01f);
        BigDecimal b2 = new BigDecimal(0.01d);
        System.out.println(b1);
        System.out.println(b2);
        BigDecimal b3 = new BigDecimal("0.01");
        System.out.println(b3);

        // 2 数值的比较
        int i = b1.compareTo(b2);// -1 0 1
        System.out.println(i);

        // 3 数值的运算
        BigDecimal b4 = new BigDecimal("6");
        BigDecimal b5 = new BigDecimal("7");
        BigDecimal add = b4.add(b5);
        System.out.println(add);
        BigDecimal subtract = b4.subtract(b5);
        System.out.println(subtract);
        BigDecimal multiply = b4.multiply(b5);
        System.out.println(multiply);
        BigDecimal divide = b4.divide(b5,2,BigDecimal.ROUND_HALF_DOWN);
        System.out.println(divide);

        // 4 约等于
        BigDecimal add1 = b1.add(b2);
        System.out.println(add1);
        BigDecimal bigDecimal = add1.setScale(4, BigDecimal.ROUND_HALF_DOWN);
        System.out.println(bigDecimal);
    }
}
