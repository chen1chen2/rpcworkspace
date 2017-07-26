package com.chenchen.rpc.server;

import com.chenchen.rpc.common.EnCoder;
import com.chenchen.rpc.common.Request;
import com.chenchen.rpc.common.DeCoder;
import com.chenchen.rpc.common.Response;
import com.chenchen.rpc.register_discover.Register;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * @author
 * @create 2017-07-26 13:24
 **/
public class Server  implements ApplicationContextAware, InitializingBean {
    // 需要注册的地址
    private String serverAddress;

    private Register register;
    // 用于放入实体
    private Map<String, Object> serviceMap = new HashMap<String, Object>();
    public Server(String serverAddress, Register register) {
        this.register = register;
        this.serverAddress = serverAddress;
    }


    /**
     * server开启netty的nio 的socket服务
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup).channel(
                NioServerSocketChannel.class).childHandler(
                        new ChannelInitializer() {
                            protected void initChannel(Channel channel) throws Exception {
                                channel.pipeline().addLast(new DeCoder(Request.class))// 注册解码 IN-1
                                                  .addLast(new EnCoder(Response.class))// 注册编码 OUT-3
                                                  .addLast(new Handler(serviceMap));//注册RpcHandler IN-2
                            }
                        }).option(ChannelOption.SO_BACKLOG, 128) // 设置socket 中等待的连接数
                          .childOption(ChannelOption.SO_KEEPALIVE, true); // 设置当长时间没有数据交流，进行socket测试包转发

        String[] array = serverAddress.split(":");
        String host = array[0]; // 主机ip
        int port = Integer.parseInt(array[1]); // socket开放端口

        ChannelFuture future = bootstrap.bind(host, port).sync(); // 同步等到socket开启服务成功
        // 当sokcet服务开启成功后，进行服务注册
        register.register(serverAddress);

        // 只要有future的就要关闭, 服务开启和服务注册成功
        future.channel().closeFuture().sync();
    }

    /**
     * 在构造本实体类的时候会调用这个方法
     * 用于获得所有标记了自定义注解@RpcInterface注解的类，放入自己的容器中
     * @param ctx
     * @throws BeansException
     */
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        // 从spring中获得标记自己的注解的类的实体
        Map<String, Object> beans = ctx.getBeansWithAnnotation(RpcInterface.class);
        // 放入serviceMap中
        for(Object object : beans.values()) {
            String key = object.getClass().getAnnotation(RpcInterface.class).value().getName();
            serviceMap.put(key, object);
        }
    }
}
