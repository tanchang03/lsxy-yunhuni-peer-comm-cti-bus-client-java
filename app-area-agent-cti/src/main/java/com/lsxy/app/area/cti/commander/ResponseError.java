package com.lsxy.app.area.cti.commander;

/**
 * Created by liuxy on 16-7-13.
 */
public class ResponseError {
    private int code;
    private String message = null;
    private Object data = null;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return String.format("<%s code=%d, message=%s, data=%s>", ResponseError.class, code, message, data);
    }
}
