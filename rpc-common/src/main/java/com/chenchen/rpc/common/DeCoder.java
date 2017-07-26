package com.chenchen.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * netty的inhandler 用于反序列化，然后将结果交个下一个handler处理
 * @author chenchen
 * @create 2017-07-26 14:23
 **/
public class DeCoder extends ByteToMessageDecoder {

    // 需要反序列化的类文件
    private Class<?> clazz;

    public DeCoder(Class<?> clazz) {
        this.clazz = clazz;
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 查看是否存在可以读的字节，小于4说明不正常
        if (byteBuf.readableBytes() < 4) {
            return;
        }
        byteBuf.markReaderIndex();
        int dataLength = byteBuf.readInt();
        if (dataLength < 0) {
            ctx.close();
        }
        if (byteBuf.readableBytes() < dataLength) {
            // 不完整，
            byteBuf.resetReaderIndex();
        }

        //将ByteBuf转换为byte[]
        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);

        Object obj = SerializationUtil.deserialize(data, clazz);
        // 交给下一个handler
        list.add(obj);
    }
}
