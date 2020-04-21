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
import java.util.concurrent.*;

public class MsgInBound extends SimpleChannelInboundHandler<FileMsg> {
    private static final ExecutorService executor = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(100000), new ThreadPoolExecutor.AbortPolicy());

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
                writeFile(ctx, fileMsg, UploadSignal.CONTINUE);
                break;
            case FINISH:
                writeFile(ctx, fileMsg, UploadSignal.CONFIRM);
                break;
            case CONTINUE:
                writeFile(ctx, fileMsg, UploadSignal.CONTINUE);
                break;
            default:
                break;
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.pipeline().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    private void writeFile(ChannelHandlerContext ctx, FileMsg fileMsg, UploadSignal signal) throws IOException {
        try {
            executor.submit(() -> {
                try {
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
                                fileMsg.setUploadSignal(signal);
                                ctx.writeAndFlush(fileMsg);
                            }
                        } else {
                            files.createNewFile();
                            fileMsg.setPosition(0L);
                            fileMsg.setFileByte(null);
                            fileMsg.setUploadSignal(signal);
                            ctx.writeAndFlush(fileMsg);
                        }
                    } else {
                        File files = new File(Constant.fileReceivePath, fileMsg.getAliasName());
                        if (files.exists()) {
                            try (FileChannel fileChannel = (FileChannel.open(files.toPath(),
                                    StandardOpenOption.READ))) {
                                fileMsg.setPosition(fileChannel.size());
                                fileMsg.setFileByte(null);
                                fileMsg.setUploadSignal(signal);
                                ctx.writeAndFlush(fileMsg);
                            }
                        } else {
                            files.createNewFile();
                            fileMsg.setPosition(0L);
                            fileMsg.setFileByte(null);
                            fileMsg.setUploadSignal(signal);
                            ctx.writeAndFlush(fileMsg);
                        }
                    }
                } catch (Exception e) {
                    fileMsg.setUploadSignal(UploadSignal.STOP);
                }
            });
        } catch (Exception e) {
            fileMsg.setUploadSignal(UploadSignal.STOP);
        }
    }
}
