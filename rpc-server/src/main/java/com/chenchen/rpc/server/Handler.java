package com.chenchen.rpc.server;

import com.chenchen.rpc.common.Request;
import com.chenchen.rpc.common.Response;
import com.sun.org.apache.regexp.internal.RE;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author netty的inhandler，用于反射然后执行对应的方法，然后写入到outhandler中，等待socket返回
 * @create 2017-07-26 14:46
 **/
public class Handler extends SimpleChannelInboundHandler<Request> {

    // 实体map
    private final Map<String, Object> serviceMap;

    public Handler(Map<String, Object> serviceMap) {
        this.serviceMap = serviceMap;
    }

    // 从decoder中过来的request
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object rq) throws Exception {
        // 新建返回对象
        Request request = (Request) rq;
        Response response = new Response();
        response.setRequestId(request.getRequestId());
        try {
            // 进行反射,调用本地方法
            Object handler = handler(request);
            response.setResult(handler);
        } catch (Exception e) {
            // 如果有异常则写入到到response中
            response.setError(e);
        }

        //写入 outbundle（即RpcEncoder）进行下一步处理（即编码）后发送到channel中给客户端
        // 只要有writeandflush就是写到outhandler中
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

    }


    private Object handler(Request request) throws Exception {
        String className = request.getClassName();

        Object bean = serviceMap.get(className);

        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();
        // 运用反射进行方法调用
        Method method = bean.getClass().getMethod(methodName, parameterTypes);

        Object invoke = method.invoke(bean, parameters);

        return invoke;
    }

    protected void messageReceived(ChannelHandlerContext channelHandlerContext, Request request) throws Exception {

    }

    /**
     * 抛异常的时候会到这来
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }

}
