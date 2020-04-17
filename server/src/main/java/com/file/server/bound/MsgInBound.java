package com.file.server.bound;

import com.file.global.Constant;
import com.file.global.UploadSignal;
import com.file.modal.FileMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

public class MsgInBound extends SimpleChannelInboundHandler<FileMsg> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileMsg fileMsg) throws Exception {
        switch (fileMsg.getUploadSignal()) {
            case CREATE:
                String endPoint = "";
                if (fileMsg.getFileName().lastIndexOf(".") != -1) {
                    endPoint = fileMsg.getFileName().substring(fileMsg.getFileName().lastIndexOf("."));
                }
                fileMsg.setAliasName(UUID.randomUUID().toString().replaceAll("-", "") + endPoint);
                boolean newFile = new File(Constant.fileReceivePath, fileMsg.getAliasName()).createNewFile();
                if (newFile) {
                    fileMsg.setUploadSignal(UploadSignal.GENERAL);
                } else {
                    fileMsg.setUploadSignal(UploadSignal.STOP);
                }
                ctx.writeAndFlush(fileMsg);
                break;
            case UPLOAD:
                writeFile(fileMsg);
                ctx.writeAndFlush(fileMsg);
                break;
            case FINISH:
                writeFile(fileMsg);
                fileMsg.setUploadSignal(UploadSignal.CONFIRM);
                ctx.writeAndFlush(fileMsg);
                break;
            case CONTINUE:
                writeFile(fileMsg);
                ctx.writeAndFlush(fileMsg);
                break;
            default:
                break;
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.pipeline().close();
    }

    private void writeFile(FileMsg fileMsg) throws IOException {
        if (fileMsg.getFileByte() != null) {
            File files = new File(Constant.fileReceivePath, fileMsg.getAliasName());
            if (files.exists()) {
                try (FileChannel fileChannel = (FileChannel.open(files.toPath(),
                        StandardOpenOption.WRITE, StandardOpenOption.APPEND))) {
                    ByteBuffer wrap = ByteBuffer.wrap(fileMsg.getFileByte());
//                wrap.flip();
                    fileChannel.write(wrap, fileMsg.getPosition());
                    fileMsg.setPosition(fileMsg.getPosition() + fileMsg.getPreSize());
                    fileMsg.setFileByte(null);
                    fileMsg.setUploadSignal(UploadSignal.CONTINUE);
                }
            }
        } else {
            File files = new File(Constant.fileReceivePath, fileMsg.getAliasName());
            if (files.exists()) {
                try (FileChannel fileChannel = (FileChannel.open(files.toPath(),
                        StandardOpenOption.READ))) {
                    fileMsg.setPosition(fileChannel.size());
                    fileMsg.setFileByte(null);
                    fileMsg.setUploadSignal(UploadSignal.CONTINUE);
                }
            }
        }
    }
}
