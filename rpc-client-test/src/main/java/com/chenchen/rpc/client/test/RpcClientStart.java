package com.chenchen.rpc.client.test;

import com.chenchen.rpc.client.Client;
import com.chenchen.rpc.common.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * rpcclient端启动代码
 */
@Component
public class RpcClientStart {

    public static void main(String[] args) {

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("spring.xml");
        Client client = (Client) ctx.getBean("client");
        HelloService proxy = client.getProxy(HelloService.class);
        String success = proxy.hello("终于成功了");
        System.out.println(success);
    }
}
