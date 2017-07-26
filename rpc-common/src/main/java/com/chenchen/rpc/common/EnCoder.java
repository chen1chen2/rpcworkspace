package com.chenchen.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.List;

/**
 * netty的outhandler 用于序列化，然后直接写到bytebuffer中发送到socket中
 * @author chenchen
 * @create 2017-07-26 14:22
 **/
public class EnCoder extends MessageToByteEncoder {

    // 需要序列化的类文件
    private Class<?> clazz;

    public EnCoder(Class<?> clazz) {
        this.clazz = clazz;
    }

    protected void encode(ChannelHandlerContext ctx, Object o, ByteBuf byteBuf) throws Exception {
        if (clazz.isInstance(o)) {
            byte[] data = SerializationUtil.serialize(o);
            byteBuf.writeInt(data.length); // 标志文件的字节大小
            byteBuf.writeBytes(data); // 将结果写到client端
        }
    }
}
