package com.file.server.bound;

import com.file.modal.Msg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

public class MsgInBound extends SimpleChannelInboundHandler<Msg> {
    private static final String filePath = "D:/netty/receive";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Msg msg) throws Exception {

//        Msg msg = (Msg) obj;
        File file = new File(filePath, msg.getFileName());
        if (msg.getFinish()) {
            ctx.close();
        }
        if (!file.exists()) {
            msg.setPosition(0L);
            file.createNewFile();
            ctx.writeAndFlush(msg);
        } else {
            try (FileChannel fileChannel = (FileChannel.open(Paths.get(file.getAbsolutePath()),
                    EnumSet.of(StandardOpenOption.WRITE)))) {
                fileChannel.position(msg.getPosition());
                fileChannel.write(ByteBuffer.wrap(msg.getFileByte()));
                fileChannel.force(true);
                msg.setPosition(fileChannel.position());
                ctx.writeAndFlush(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
