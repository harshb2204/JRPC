package com.harsh.rpc.server;


import com.harsh.rpc.model.MessagePayload;
import io.netty.channel.Channel;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// here we will maintain the information of our client channels
public class ClientSessionManager {

    private static final Map<String, Channel> registerClients = new ConcurrentHashMap<>();
    private static final Map<String, MessagePayload> requestMap = new ConcurrentHashMap<>();

    // every request we need to save in a map, the reason we save it, is because we need to
    // find the consumer from the message (the message has the consumer id), we need this id
    // to find the channel of the consumer , because we need to return the result backwards
    // after a rpc is init

    public static void register(String clientId, Channel channel){
        registerClients.put(clientId, channel);
    }

    public static boolean isClientRegistered(String clientId){
        return registerClients.containsKey(clientId);

    }

    public static Channel getClientChannel(String clientId){
        return registerClients.get(clientId);
    }

    public static void putRequest(MessagePayload messagePayload){
        MessagePayload.RpcRequest rpcRequest =  (MessagePayload.RpcRequest) messagePayload.getPayload();

        requestMap.put(rpcRequest.getRequestId(), messagePayload);
    }

    public static MessagePayload getRequestClientMessage(String requestId){
        return requestMap.get(requestId);
    }


}
