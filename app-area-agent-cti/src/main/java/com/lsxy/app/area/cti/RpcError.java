package com.lsxy.app.area.cti;

/**
 * RPC 错误。由 CTI 服务器返回
 * <p>
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

    /**
     * @return 错误编码
     */
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    /**
     * @return 错误描述信息
     */
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return 错误数据
     */
    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
