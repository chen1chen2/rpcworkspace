package com.chenchen.rpc.client;

import com.chenchen.rpc.common.DeCoder;
import com.chenchen.rpc.common.EnCoder;
import com.chenchen.rpc.common.Request;
import com.chenchen.rpc.common.Response;
import com.chenchen.rpc.register_discover.Discover;
import com.sun.org.apache.regexp.internal.RE;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import javax.swing.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * 客户端代理类
 */
public class Client {
    private Discover discover;
    public Client(Discover discover) {
        this.discover = discover;
    }

    Object obj = new Object(); // 新建立一个obj当做锁

    public <T> T getProxy(final Class<?> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[] { clazz }, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 当使用代理对象调用方法得时候，将调用到这个地方来
                // 封装rpc的自定义请求实体request
                Request request = new Request();
                request.setRequestId(UUID.randomUUID().toString());
                request.setClassName(clazz.getName());
                request.setMethodName(method.getName());
                request.setParameters(args);
                request.setParameterTypes(method.getParameterTypes());

                // 获得一个可以访问的sever地址
                String serverAddress = discover.getServerAddress();

                Response response = send(serverAddress, request);

                //返回信息
                if (response.isError()) { // 有错误，同样抛出来错误
                    throw response.getError();
                } else {
                    return response.getResult();
                }
            }
        });
    }

    // 连接server的socket
    public Response send(String serverAddress, Request request) {

        try {
            EventLoopGroup group = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            final Handler handler = new Handler(obj);
            bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel)
                        throws Exception {
                    // 向pipeline中添加编码、解码、业务处理的handler
                    channel.pipeline()
                            .addLast(new EnCoder(Request.class))  //OUT - 1
                            .addLast(new DeCoder(Response.class)) //IN - 2
                            .addLast(handler);             //IN - 3
                }
            }).option(ChannelOption.SO_KEEPALIVE, true);


            String[] array = serverAddress.split(":");
            String host = array[0];
            int port = Integer.parseInt(array[1]);


            // 链接服务器
            ChannelFuture future = bootstrap.connect(host, port).sync();
            //将request对象写入outbundle处理后发出（即RpcEncoder编码器）
            future.channel().writeAndFlush(request).sync();

            // 用线程等待的方式决定是否关闭连接
            // 其意义是：先在此阻塞，等待获取到服务端的返回后，被唤醒，从而关闭网络连接
            synchronized (obj) {
                obj.wait();
            }
            // 当真正得到值得时候才进行操作
            Response response = handler.getResponse();
            if (response != null) {
                future.channel().closeFuture().sync();
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
