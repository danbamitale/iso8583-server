package com.titp.server.utils;

public enum ISOResponseCode {
    SUCCESS("00"), ERROR("06");

    private final String code;

    ISOResponseCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
