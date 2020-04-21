package com.file.client.bound;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.file.global.Constant;
import com.file.global.UploadSignal;
import com.file.modal.FileMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;

public class MsgSendFileBound extends ChannelInboundHandlerAdapter {
    private String filename = "123.pdf";
    private FileChannel fileChannel;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        File file = new File(Constant.fileSendPath, filename);
        File tmpFile = new File(Constant.fileSendPath, filename + Constant.TMP_SUFFIX);
        if (file.exists()) {
            FileMsg fileMsg = new FileMsg();
            fileMsg.setFileName(filename);
            fileMsg.setFilePath(file.getParent());
            fileMsg.setPosition(0L);
            fileMsg.setPreSize(50960L);
            fileMsg.setFileSize(new File(Constant.fileSendPath, filename).length());
            fileMsg.setUploadDate(System.currentTimeMillis());
            fileMsg.setUploadSignal(UploadSignal.CREATE);
            if (tmpFile.exists()) {
                String aliasName = readTmpFile(tmpFile);
                if (aliasName == null) {
                    ctx.close();
                }
                fileMsg.setUploadSignal(UploadSignal.CONTINUE);
                fileMsg.setAliasName(aliasName);
            }
            this.fileChannel = (FileChannel.open(file.toPath(),
                    StandardOpenOption.READ));
            ctx.writeAndFlush(fileMsg);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FileMsg) {
            FileMsg fileMsg = (FileMsg) msg;
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
                        if (tmpFileConfirm.exists()) {
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
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.fileChannel.close();
    }

    private void uploadFile(FileMsg fileMsg, ChannelHandlerContext ctx, File file) throws IOException {
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

    private boolean createTmpFile(File tmpFile, FileMsg fileMsg) {
        try (FileChannel createFileChannel = (FileChannel.open(tmpFile.toPath(),
                StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {
            byte[] bytes = JSON.toJSONString(fileMsg).getBytes(StandardCharsets.UTF_8);
            ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
            byteBuffer.put(bytes);
            byteBuffer.flip();
            createFileChannel.write(byteBuffer);
            ReferenceCountUtil.release(byteBuffer);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String readTmpFile(File tmpFile) {
        try (FileChannel tempFileChannel = (FileChannel.open(tmpFile.toPath(),
                StandardOpenOption.READ))) {
            MappedByteBuffer map = tempFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, tempFileChannel.size()).load();
            map.asReadOnlyBuffer().flip();
            byte[] array = new byte[map.asReadOnlyBuffer().remaining()];
            map.asReadOnlyBuffer().get(array);
            String content = new String(array, StandardCharsets.UTF_8);
            JSONObject jsonObject = JSON.parseObject(content);
            return jsonObject.getString("aliasName");
        } catch (IOException e) {
            tmpFile.delete();
            return null;
        }
    }
}
