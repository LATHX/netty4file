package com.file.client.second.main;

import com.file.client.second.bound.MsgSendFileBound;
import com.file.code.MarshallingCodeCFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ClientSecondMain {
    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap().group(bossGroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast("decoder",MarshallingCodeCFactory.buildMarshallingDecoder());
                            socketChannel.pipeline().addLast("encoder",MarshallingCodeCFactory.buildMarshallingEncoder());
//                            socketChannel.pipeline().addLast(new MsgInitInBound());
                            socketChannel.pipeline().addLast(new MsgSendFileBound());
                        }
                    });
            ChannelFuture future = bootstrap.connect("localhost", 8765).sync();//绑定端口
            future.channel().closeFuture().sync();//等待关闭(程序阻塞在这里等待客户端请求)
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
        }
    }
}
