package com.newcoder.community;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.util.Date;

public class Demo {
    public static void main(String[] args) {
        System.out.println(new Date(System.currentTimeMillis()));
        long i = 3600 * 24 * 30;
        System.out.println(i * 1000);
        System.out.println(new Date(System.currentTimeMillis() + i));
    }
}
