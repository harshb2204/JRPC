package com.harsh.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactoryFriend;
import org.springframework.beans.factory.annotation.Value;




public class RpcClient {

    private NioEventLoopGroup worker = new NioEventLoopGroup(); // as client does not accept connections we need only one worker thread

    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    @Value("${harsh.rpc.server.port}")
    private String port;

    @Value("${harsh.rpc.client.host}")
    private String host;

    private Channel channel;
    // in netty channel -> active connection (like a tcp socket)

    public RpcClient() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    connect();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        logger.info("Client finished init process");
    }

    private void connect() throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap(); // netty client builder

        bootstrap.group(worker).channel(NioSocketChannel.class) //Use NIO TCP client socket
                .handler(new ChannelInitializer<SocketChannel>() {


                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {

                    }
                });

        ChannelFuture channelFuture = bootstrap.connect(host, Integer.parseInt(port)).
                addListener( future -> {
                    if(future.isSuccess()){
                        ChannelFuture channelFuture1 = (ChannelFuture) future;
                        this.channel = channelFuture1.channel();
                    }else {
                        logger.error("failed to connect the server, trying to reconnect");
                        reconnect();
                    }
                });

        channelFuture.channel().closeFuture().sync();
    }

    public void reconnect() {

    }
}
