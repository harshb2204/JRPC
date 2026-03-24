package com.harsh.rpc.handler;

import com.alibaba.fastjson2.JSON;
import com.harsh.rpc.model.MessagePayload;
import com.harsh.rpc.model.MessageType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class JsonCallMessageEncoder extends MessageToByteEncoder<MessagePayload> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, MessagePayload messagePayload, ByteBuf byteBuf) throws Exception {
        //bytebuf is a memory area, we can write whatever into it

        // the first byte is the message type
        byte type;
        if(messagePayload.getMessageType().equals(MessageType.REGISTER)){
            type = 1;
        }
        else if(messagePayload.getMessageType().equals(MessageType.CALL)){
            type = 2;
        }
        else if(messagePayload.getMessageType().equals(MessageType.FORWARD)){
            type = 3;
        }
        else if(messagePayload.getMessageType().equals(MessageType.RESPONSE)){
            type = 4;
        }
        else{
            type = 5;
        }

        byteBuf.writeByte(type);
        byte[] jsonBytes = JSON.toJSONBytes(messagePayload);

        byteBuf.writeInt(jsonBytes.length);

        byteBuf.writeBytes(jsonBytes);
    }
}
