package com.file.client.bound;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.file.global.Constant;
import com.file.global.UploadSignal;
import com.file.modal.FileMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;

public class MsgInitInBound extends ChannelInboundHandlerAdapter {
    private String filename = "CentOS-7-x86_64-DVD-1810.iso";

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        File file = new File(Constant.fileSendPath, filename);
        File tmpFile = new File(Constant.fileSendPath, filename + Constant.TMP_SUFFIX);
        if (file.exists()) {
            FileMsg fileMsg = new FileMsg();
            fileMsg.setFileName(filename);
            fileMsg.setFilePath(file.getParent());
            fileMsg.setPosition(0L);
            fileMsg.setPreSize(40960L);
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
            ctx.writeAndFlush(fileMsg);
        }
    }




    private String readTmpFile(File tmpFile) {
        try (FileChannel fileChannel = (FileChannel.open(tmpFile.toPath(),
                StandardOpenOption.READ))) {
            MappedByteBuffer map = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size()).load();
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
