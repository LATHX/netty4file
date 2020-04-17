package com.file.global;

public enum UploadSignal {
    CREATE(0, "create"),
    GENERAL(1, "general"),
    UPLOAD(2, "upload"),
    CONTINUE(3, "continue"),
    RESEND(4, "resend"),
    FINISH(5, "finish"),
    CONFIRM(6, "confirm"),
    STOP(7, "stop");
    private Integer code;
    private String msg;

    UploadSignal(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "UploadSignal{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
