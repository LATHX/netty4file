package com.file.modal;

import com.file.global.UploadSignal;

import java.io.Serializable;
import java.util.Arrays;

public class FileMsg implements Serializable {
    private String fileName;
    private String aliasName;
    private String filePath;
    private byte[] fileByte;
    private Long position;
    private UploadSignal uploadSignal;
    private Long preSize;
    private Long fileSize;
    private Long uploadDate;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public Long getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Long uploadDate) {
        this.uploadDate = uploadDate;
    }

    public Long getPreSize() {
        return preSize;
    }

    public void setPreSize(Long preSize) {
        this.preSize = preSize;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public UploadSignal getUploadSignal() {
        return uploadSignal;
    }

    public void setUploadSignal(UploadSignal uploadSignal) {
        this.uploadSignal = uploadSignal;
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
                ", aliasName='" + aliasName + '\'' +
                ", position=" + position +
                ", uploadSignal=" + uploadSignal +
                ", preSize=" + preSize +
                ", fileSize=" + fileSize +
                ", uploadDate=" + uploadDate +
                '}';
    }
}
