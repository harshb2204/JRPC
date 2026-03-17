package com.harsh.rpc.model;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.coyote.Request;
import org.apache.coyote.RequestInfo;
import org.apache.logging.log4j.message.Message;

import java.io.Serializable;

public class MessagePayload implements Serializable {

    private String clientID;


    @JSONField(serializeUsing = MessageTypeSerializer.class, deserializeUsing = MessageTypeDeserializer.class)
    private MessageType messageType;

    private Object payload; // rpc req or res

    public MessagePayload() {

    }

    public MessagePayload(RequestMessageBuilder builder){
         this.clientID = builder.clientID;
         this.messageType = builder.messageType;
         if(messageType.equals(MessageType.CALL)){
            this.payload = new RpcRequest(builder);
         }
         else if(messageType.equals(MessageType.RESPONSE)){
            this.payload = new RpcResponse(builder);
         }


    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    // builder pattern

    public static class RequestMessageBuilder {
        private String clientID;

        private MessageType messageType;

        private String requestClientId;

        //uuid
        private String requestId;

        private String requestMethodName;

        private String requestClassName;

        private String returnValueType;

        private String[] paramTypes;

        private Object[] params;

        private Object returnValue;

        public RequestMessageBuilder setClientId(String clientID) {
            this.clientID = clientID;
            return this;
        }

        public RequestMessageBuilder setMessageType(MessageType messageType) {
            this.messageType = messageType;
            return this;
        }

        public RequestMessageBuilder setRequestClientId(String requestClientId) {
            this.requestClientId = requestClientId;
            return this;
        }

        public RequestMessageBuilder setRequestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public RequestMessageBuilder setRequestMethodName(String requestMethodName) {
            this.requestMethodName = requestMethodName;
            return this;
        }

        public RequestMessageBuilder setRequestClassName(String requestClassName) {
            this.requestClassName = requestClassName;
            return this;
        }

        public RequestMessageBuilder setReturnValueType(String returnValueType) {
            this.returnValueType = returnValueType;
            return this;
        }

        public RequestMessageBuilder setParamTypes(String[] paramTypes) {
            this.paramTypes = paramTypes;
            return this;
        }

        public RequestMessageBuilder setParams(Object[] params) {
            this.params = params;
            return this;
        }

        public RequestMessageBuilder setReturnValue(Object returnValue) {
            this.returnValue = returnValue;
            return this;
        }

        public MessagePayload build() {
            return new MessagePayload();
        }
    }


    public static class RpcRequest implements Serializable{
        // producer id
        private String requestClientId;

        //uuid
        private String requestId;

        private String requestMethodName;

        private String requestClassName;

        private String returnValueType;

        private String[] paramTypes;

        private Object[] params;

        public RpcRequest() {

        }

        public RpcRequest(RequestMessageBuilder builder){
            this.requestClientId = builder.requestClientId;
        }

        public String getRequestClientId() {
            return requestClientId;
        }

        public void setRequestClientId(String requestClientId) {
            this.requestClientId = requestClientId;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getRequestMethodName() {
            return requestMethodName;
        }

        public void setRequestMethodName(String requestMethodName) {
            this.requestMethodName = requestMethodName;
        }

        public String getRequestClassName() {
            return requestClassName;
        }

        public void setRequestClassName(String requestClassName) {
            this.requestClassName = requestClassName;
        }

        public String getReturnValueType() {
            return returnValueType;
        }

        public void setReturnValueType(String returnValueType) {
            this.returnValueType = returnValueType;
        }

        public String[] getParamTypes() {
            return paramTypes;
        }

        public void setParamTypes(String[] paramTypes) {
            this.paramTypes = paramTypes;
        }

        public Object[] getParams() {
            return params;
        }

        public void setParams(Object[] params) {
            this.params = params;
        }
    }

    public static class RpcResponse implements Serializable{
        private String requestId;
        private Object result;

        public RpcResponse() {

        }

        public RpcResponse(RequestMessageBuilder builder){
            this.requestId = builder.requestId;
            this.result = builder.returnValue;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
        }
    }


}
