package com.file.server.second.bound;

import com.file.modal.FileMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;

import java.nio.channels.FileChannel;

public class ReceiveFileBound extends ChannelInboundHandlerAdapter {
    private FileChannel fileChannel;
    private FileMsg fileMsg;

    public ReceiveFileBound(FileChannel fileChannel,FileMsg fileMsg) {
        this.fileChannel = fileChannel;
        this.fileMsg = fileMsg;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        fileChannel.write(byteBuf.nioBuffer());
        fileChannel.force(true);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        fileChannel.close();
        ctx.pipeline().remove(this);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        fileChannel.close();
        ctx.pipeline().remove(this);
    }

}
