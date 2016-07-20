package com.lsxy.app.area.cti.commander;

/**
 * Created by liuxy on 16-7-13.
 */
public class RpcError {
    private int code;
    private String message = null;
    private Object data = null;

    @Override
    public String toString() {
        return String.format("<%s code=%d, message=%s, data=%s>", RpcError.class, getCode(), getMessage(), getData());
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
