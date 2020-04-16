package com.file.client.bound;

import com.file.modal.Msg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

public class MsgInitInBound extends ChannelInboundHandlerAdapter {
    private static final String filePath = "D:/netty/send";

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Msg msg = new Msg();
        msg.setFileName("123.pdf");
        msg.setPosition(0L);
        msg.setFinish(false);
        ctx.writeAndFlush(msg);
        ctx.fireChannelActive();
        ctx.channel().read();
    }
}
