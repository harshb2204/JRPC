package com.harsh.rpc.server;

import com.harsh.rpc.handler.JsonCallMessageEncoder;
import com.harsh.rpc.handler.JsonMessageDecoder;
import com.harsh.rpc.handler.RpcServerMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class RpcServer {

    public void startServer() throws InterruptedException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                start();
            }
        }).start();

        System.out.println("Server started");

        Thread.sleep(20000);
    }

    public void start() {
        //thread pools used by netty
        NioEventLoopGroup boss = new NioEventLoopGroup(); // listens for new connections
        NioEventLoopGroup worker = new NioEventLoopGroup(); // handle reading the data, sending responses

        try {



            ServerBootstrap serverBootstrap = new ServerBootstrap(); // helper class to configure the server
            serverBootstrap.group(boss, worker) // setting thread groups
                    .channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 128)// use a non blocking tcp and a queue size

                    .childOption(ChannelOption.SO_KEEPALIVE, true).childHandler(new ChannelInitializer<
                            SocketChannel>() { // keepts TCP connection alive
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // inside this method we configure the pipeline

                            socketChannel.pipeline().addLast(new JsonCallMessageEncoder());
                            socketChannel.pipeline().addLast(new JsonMessageDecoder());
                            socketChannel.pipeline().addLast(new RpcServerMessageHandler());
                        }
                    });

            // networking operations are asynchrounous so netty returns a future
            ChannelFuture channelFuture = serverBootstrap.bind(8800).sync();

            System.out.println("Server started on port 8800");

            channelFuture.channel().closeFuture().sync(); // keep server running until it is closed
            // the thread is actually blocking on this line so nothing after this is executed
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
