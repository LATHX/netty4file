package com.file.client.bound;

import com.alibaba.fastjson.JSON;
import com.file.global.Constant;
import com.file.global.UploadSignal;
import com.file.modal.FileMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;

public class MsgSendFileBound extends SimpleChannelInboundHandler<FileMsg> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileMsg fileMsg) throws Exception {
        File file = new File(Constant.fileSendPath, fileMsg.getFileName());
        if (file.exists()) {
            switch (fileMsg.getUploadSignal()) {
                case GENERAL:
                    File tmpFile = new File(file.getParent(), fileMsg.getFileName() + Constant.TMP_SUFFIX);
                    createTmpFile(tmpFile, fileMsg);
                    uploadFile(fileMsg, ctx, file);
                    break;
                case CONTINUE:
                    uploadFile(fileMsg, ctx, file);
                    break;
                case CONFIRM:
                    System.out.println(System.currentTimeMillis() - fileMsg.getUploadDate());
                    File tmpFileConfirm = new File(file.getParent(), fileMsg.getFileName() + Constant.TMP_SUFFIX);
                    if(tmpFileConfirm.exists()){
                        tmpFileConfirm.delete();
                    }
                    ctx.close();
                    break;
                case STOP:
                    fileMsg.setFileByte(null);
                    fileMsg.setUploadSignal(UploadSignal.FINISH);
                    ctx.writeAndFlush(fileMsg);
                    break;
                default:
                    break;
            }
        }
    }

    private void uploadFile(FileMsg fileMsg, ChannelHandlerContext ctx, File file) throws IOException {
        try (FileChannel fileChannel = (FileChannel.open(file.toPath(),
                StandardOpenOption.READ))) {
            fileMsg.setUploadSignal(UploadSignal.UPLOAD);
            if ((fileMsg.getPosition() + fileMsg.getPreSize()) >= fileChannel.size()) {
                fileMsg.setPreSize(fileChannel.size() - fileMsg.getPosition());
                fileMsg.setUploadSignal(UploadSignal.FINISH);
            }
            MappedByteBuffer map = fileChannel.map(FileChannel.MapMode.READ_ONLY, fileMsg.getPosition(), fileMsg.getPreSize()).load();
            map.asReadOnlyBuffer().flip();
            byte[] arr = new byte[map.asReadOnlyBuffer().remaining()];
            map.asReadOnlyBuffer().get(arr);
            fileMsg.setFileByte(arr);
            ctx.pipeline().writeAndFlush(fileMsg);
            map.clear();
        }
    }

    private boolean createTmpFile(File tmpFile, FileMsg fileMsg) {
        try (FileChannel fileChannel = (FileChannel.open(tmpFile.toPath(),
                StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {
            byte[] bytes = JSON.toJSONString(fileMsg).getBytes(StandardCharsets.UTF_8);
            ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
            byteBuffer.put(bytes);
            byteBuffer.flip();
            fileChannel.write(byteBuffer);
            ReferenceCountUtil.release(byteBuffer);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
