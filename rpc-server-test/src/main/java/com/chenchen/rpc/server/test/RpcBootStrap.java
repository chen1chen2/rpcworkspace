package com.chenchen.rpc.server.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 程序入口
 * @author chenchen
 * @create 2017-07-26 13:14
 **/
public class RpcBootStrap {
    public static void main(String[] args) {
        // spring扫描spring.xml
        new ClassPathXmlApplicationContext("spring.xml");
    }
}
