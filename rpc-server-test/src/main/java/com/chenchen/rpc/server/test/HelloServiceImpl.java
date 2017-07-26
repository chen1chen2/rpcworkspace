package com.chenchen.rpc.server.test;


import com.chenchen.rpc.common.HelloService;
import com.chenchen.rpc.server.RpcInterface;

@RpcInterface(HelloService.class)
public class HelloServiceImpl implements HelloService {

    public String hello(String name) {
    	System.out.println("已经调用服务端接口实现，业务处理结果为：");
    	System.out.println("Hello! " + name);
        return "Hello! " + name;
    }
}
