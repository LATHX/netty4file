package com.file.server.second.bound;

import com.file.global.Constant;
import com.file.global.UploadSignal;
import com.file.modal.FileMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

public class MsgInfoBound extends ChannelInboundHandlerAdapter {
    private FileChannel fileChannel;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FileMsg) {
            FileMsg fileMsg = (FileMsg) msg;
            switch (fileMsg.getUploadSignal()) {
                case CREATE:
                    String endPoint = "";
                    if (fileMsg.getFileName().lastIndexOf(".") != -1) {
                        endPoint = fileMsg.getFileName().substring(fileMsg.getFileName().lastIndexOf("."));
                    }
                    fileMsg.setAliasName(UUID.randomUUID().toString().replaceAll("-", "") + endPoint);
                    File createFile = new File(Constant.fileReceivePath, fileMsg.getAliasName());
                    boolean newFile = createFile.createNewFile();
                    if (newFile) {
                        fileMsg.setUploadSignal(UploadSignal.GENERAL);
                        this.fileChannel = (FileChannel.open(createFile.toPath(),
                                StandardOpenOption.WRITE, StandardOpenOption.APPEND));
                    } else {
                        fileMsg.setUploadSignal(UploadSignal.STOP);
                    }
                    ctx.writeAndFlush(fileMsg);
                    ctx.pipeline().addFirst("receiveFile", new ReceiveFileBound(fileChannel, fileMsg));
                    break;
                case CONTINUE:
                    if (fileChannel == null) {
                        File files = new File(Constant.fileReceivePath, fileMsg.getAliasName());
                        if (files.exists()) {
                            this.fileChannel = (FileChannel.open(files.toPath(),
                                    StandardOpenOption.WRITE, StandardOpenOption.APPEND));
                            fileMsg.setUploadSignal(UploadSignal.CONTINUE);
                            fileMsg.setPosition(fileChannel.size());
                            fileMsg.setFileByte(null);
                            ctx.pipeline().addFirst("receiveFile", new ReceiveFileBound(fileChannel, fileMsg));
                            ctx.writeAndFlush(fileMsg);
                        } else {
                            files.createNewFile();
                            fileMsg.setPosition(0L);
                            fileMsg.setFileByte(null);
                            fileMsg.setUploadSignal(UploadSignal.CONTINUE);
                            ctx.pipeline().addFirst("receiveFile", new ReceiveFileBound(fileChannel, fileMsg));
                            ctx.writeAndFlush(fileMsg);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
