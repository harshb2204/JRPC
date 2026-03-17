package com.harsh.rpc.model;

public enum MessageType {
    REGISTER, // when client is successfully connected to the server, the client needs to send a registration
    // message to tell the server its details

    CALL, // the rpc
    FORWARD,
    RESPONSE,
    HEART_BEAT// client should keep sending heart beat to tell the server they are active
}
