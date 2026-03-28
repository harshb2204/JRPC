package com.harsh.rpc.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.harsh.rpc.model.MessagePayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.ByteBuffer;
import java.util.List;

public class JsonMessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

        // 1 byte -> message type
        // 4 byte -> length of message
        // therefore size <5 doesnt make sense

        //return early
        // we have a list as param in the end we need to add the java obj in the list
        // when we add it, it means this decode method is completed
        if(byteBuf.readableBytes()<5){
            return;
        }
        //bytebuf is a memory and it has 2 types:-
        // 1) Heap ByteBuffer
        // 2) Direct ByteBuffer

        // which is better heap or direct - depends on heaps
        // heap backed by a regular java byte[]
        // faster allocation
        // subject to gc


        // Pooled and Unpool
        // Pooled buffer: reuse previously allocated memory chunks (similar to threadpool)
        // Unpooled not better than pooled in performance

        // frequent heap allocation slows the system
        // Netty defaults to use pooled direct buffer



       // this is provided by java
        // ByteBuffer allocate = ByteBuffer.allocate(128);
        // ByteBuffer byteBuffer = ByteBuffer.allocateDirect(128);

        // netty
        // byteBuf seperates read and write pointers


        // if we dont add anything to the list netty will not discard the current buffer
        // when next chunk arrives it merges the new data with the prev leftover bytes

        byteBuf.markReaderIndex();

        //whenever the content is read the reader index is increased (ptr to mark the current read location)
        byte messageType = byteBuf.readByte();

        int length = byteBuf.readInt();

        //remaining bytes
        //here this means message is incomplete
        if(byteBuf.readableBytes()<length){
            byteBuf.resetReaderIndex(); // resets to where u call the markreaderindex method
            return;
        }

        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);

        MessagePayload messagePayload = JSON.parseObject(bytes, MessagePayload.class);

        switch (messageType) {
            case 1, 5:
                messagePayload.setPayload(null);
                break;
            case 2, 3:
                JSONObject payload = (JSONObject) messagePayload.getPayload();
                MessagePayload.RpcRequest rpcRequest = payload.toJavaObject(MessagePayload.RpcRequest.class);
                messagePayload.setPayload(rpcRequest);
                break;
            case 4:
                JSONObject responsePayload = (JSONObject) messagePayload.getPayload();
                MessagePayload.RpcResponse rpcResponse = responsePayload.toJavaObject(MessagePayload.RpcResponse.class);
                messagePayload.setPayload(rpcResponse);

        }
        list.add(messagePayload);

    }
}
