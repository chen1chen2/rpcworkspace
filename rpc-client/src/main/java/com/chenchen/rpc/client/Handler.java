package com.chenchen.rpc.client;

import com.chenchen.rpc.common.Response;
import com.sun.org.apache.regexp.internal.RE;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 转到这个handler中，然后传入到参数中
 */
public class Handler extends SimpleChannelInboundHandler<Response> {

    private Response response;

    private Object obj; // 一个锁的索引

    public Handler(Object obj) {
        this.obj = obj;
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Response response) throws Exception {
        this.response = response;
        // 当得到了服务器返回后，唤醒等待的锁
        synchronized (obj) {
            obj.notifyAll();
        }
    }

    public Response getResponse() {
        return response;
    }
}
