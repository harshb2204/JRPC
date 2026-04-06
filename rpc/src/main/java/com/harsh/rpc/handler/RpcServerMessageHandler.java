package com.harsh.rpc.handler;

import com.harsh.rpc.model.MessagePayload;
import com.harsh.rpc.model.MessageType;
import com.harsh.rpc.server.ClientSessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


// Core handler to process the message
public class RpcServerMessageHandler extends SimpleChannelInboundHandler<MessagePayload> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessagePayload messagePayload) throws Exception {
        // Tasks of the server
        // 1. Forward the message
        // 2. Register client
        // 3. Return the response

        MessageType messageType = messagePayload.getMessageType();

        if(messageType.equals(MessageType.REGISTER)){
            registerClientIntoSession(messagePayload, channelHandlerContext.channel());
        }

        if(messageType.equals(MessageType.CALL)){
            // Find out the channel of the producer

            MessagePayload.RpcRequest rpcRequest = (MessagePayload.RpcRequest) messagePayload.getPayload();

            // Consumer calls itself
            if(messagePayload.getClientID().equals(rpcRequest.getRequestClientId())){
                throw new RuntimeException("Client Id and request client id are the same");
            }
            // looking for producer channel
            Channel channel = ClientSessionManager.getClientChannel(rpcRequest.getRequestClientId());

            if(channel == null){
                throw new RuntimeException("Channel does not exist");
            }

            forwardRequestToClient(messagePayload, channel);
        }

        if(messageType.equals(MessageType.RESPONSE)){
            // When the response is returned we need to send it back to the consumer
            returnResponseToClient(messagePayload);
        }


    }

    private void returnResponseToClient(MessagePayload messagePayload) {
        MessagePayload.RpcResponse rpcResponse = (MessagePayload.RpcResponse) messagePayload.getPayload();

        String requestId = rpcResponse.getRequestId();
        MessagePayload requestMessage = ClientSessionManager.getRequestClientMessage(requestId);
        Channel channel = ClientSessionManager.getClientChannel(requestMessage.getClientID());
        channel.writeAndFlush(messagePayload);
    }

    private void forwardRequestToClient(MessagePayload messagePayload, Channel channel) {
        ClientSessionManager.putRequest(messagePayload);
        messagePayload.setMessageType(MessageType.FORWARD);
        channel.writeAndFlush(messagePayload);
    }

    private void registerClientIntoSession(MessagePayload messagePayload, Channel channel) {
        ClientSessionManager.register(messagePayload.getClientID(), channel);
    }
}
