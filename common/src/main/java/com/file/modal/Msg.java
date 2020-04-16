package com.file.modal;

import io.netty.buffer.ByteBuf;

import java.io.Serializable;
import java.nio.ByteBuffer;

public class Msg implements Serializable {
    private String fileName;
    private byte[] fileByte;
    private Long position;
    private Boolean isFinish;

    public Boolean getFinish() {
        return isFinish;
    }

    public void setFinish(Boolean finish) {
        isFinish = finish;
    }

    public Long getPosition() {
        return position;
    }

    public void setPosition(Long position) {
        this.position = position;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getFileByte() {
        return fileByte;
    }

    public void setFileByte(byte[] fileByte) {
        this.fileByte = fileByte;
    }

    @Override
    public String toString() {
        return "Msg{" +
                "fileName='" + fileName + '\'' +
                ", fileByte=" + fileByte +
                ", position=" + position +
                ", isFinish=" + isFinish +
                '}';
    }
}
