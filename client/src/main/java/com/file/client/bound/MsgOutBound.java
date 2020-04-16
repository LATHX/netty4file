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
    private static final String filePath = "D:/netty/send";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Msg msg) throws Exception {
//        Msg msg = (Msg) obj;
        Long position = 0L;
        if (msg.getPosition() != 0L) {
            position = msg.getPosition() + 1L;
        }
        File file = new File(filePath, msg.getFileName());
        try (FileChannel fileChannel = (FileChannel.open(Paths.get(file.getAbsolutePath()),
                EnumSet.of(StandardOpenOption.READ)))) {
            Long size = 1024L;
            if (size >= fileChannel.size()) {
                size = fileChannel.size();
                msg.setFinish(true);
            }
            MappedByteBuffer map = fileChannel.map(FileChannel.MapMode.READ_ONLY, position, size).load();
            System.out.println("!11:"+map.isDirect());
            map.flip();
            msg.setFileByte(map.asReadOnlyBuffer().array());
            ctx.pipeline().writeAndFlush(msg);
            System.out.println("Client Info:" + msg.toString());
            map.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
