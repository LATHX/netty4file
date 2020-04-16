package com.file.client.bound;

import com.file.modal.Msg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.unix.Buffer;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

public class MsgOutBound extends SimpleChannelInboundHandler<Msg> {
    //    private static final String filePath = "D:/netty/send";
    private static final String filePath = "/Users/ljl/Documents/netty/send";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Msg msg) throws Exception {
//        Msg msg = (Msg) obj;
        Long position = 0L;
        if (msg.getPosition() != 0L) {
            position = msg.getPosition();
        }
        File file = new File(filePath, msg.getFileName());
        try (FileChannel fileChannel = (FileChannel.open(file.toPath(),
                StandardOpenOption.READ))) {
            Long size = 10240L;
            if ((position + size) >= fileChannel.size()) {
                size = fileChannel.size() - position;
                msg.setFinish(true);
            }
            MappedByteBuffer map = fileChannel.map(FileChannel.MapMode.READ_ONLY, position, size).load();
            map.asReadOnlyBuffer().flip();
            byte[] arr = new byte[map.asReadOnlyBuffer().remaining()];
            map.asReadOnlyBuffer().get(arr);
            msg.setFileByte(arr);
            ctx.pipeline().writeAndFlush(msg);
            System.out.println("Client Info:" + msg.toString());
            map.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
