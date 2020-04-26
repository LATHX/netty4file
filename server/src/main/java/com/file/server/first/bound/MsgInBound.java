package com.file.server.first.bound;

import com.file.global.Constant;
import com.file.global.UploadSignal;
import com.file.modal.FileMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.concurrent.*;

public class MsgInBound extends ChannelInboundHandlerAdapter {
    private static final ExecutorService executor = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(100000), new ThreadPoolExecutor.AbortPolicy());
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
                    break;
                case UPLOAD:
                    writeFile(ctx, fileMsg, UploadSignal.CONTINUE);
                    break;
                case FINISH:
                    writeFile(ctx, fileMsg, UploadSignal.CONFIRM);
                    break;
                case CONTINUE:
                    if (fileChannel == null) {
                        File files = new File(Constant.fileReceivePath, fileMsg.getAliasName());
                        if (files.exists()) {
                            this.fileChannel = (FileChannel.open(files.toPath(),
                                    StandardOpenOption.WRITE, StandardOpenOption.APPEND));
                        } else {
                            files.createNewFile();
                            fileMsg.setPosition(0L);
                            fileMsg.setFileByte(null);
                            fileMsg.setUploadSignal(UploadSignal.CONTINUE);
                            ctx.writeAndFlush(fileMsg);
                            break;
                        }
                    }
                    writeFile(ctx, fileMsg, UploadSignal.CONTINUE);
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.fileChannel.close();
        ctx.pipeline().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        this.fileChannel.close();
        super.exceptionCaught(ctx, cause);
    }

    private void writeFile(ChannelHandlerContext ctx, FileMsg fileMsg, UploadSignal signal) throws IOException {
        try {
            executor.submit(() -> {
                try {
                    if (fileMsg.getFileByte() != null) {
                        ByteBuffer wrap = ByteBuffer.wrap(fileMsg.getFileByte());
                        fileChannel.write(wrap, fileMsg.getPosition());
                        fileChannel.force(true);
                        fileMsg.setPosition(fileMsg.getPosition() + fileMsg.getPreSize());
                        fileMsg.setFileByte(null);
                        fileMsg.setUploadSignal(signal);
                        ReferenceCountUtil.release(wrap);
                        ctx.writeAndFlush(fileMsg);
                    } else {
                        fileMsg.setPosition(fileChannel.size());
                        fileMsg.setFileByte(null);
                        fileMsg.setUploadSignal(signal);
                        ctx.writeAndFlush(fileMsg);

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
