package com.lsxy.app.area.cti.commander;

/**
 * Created by liuxy on 16-7-13.
 */
public class RpcResponse {
    private String id;
    private Object result = null;
    private RpcError error = null;

    @Override
    public String toString() {
        if (this.getError() == null)
            return String.format("<%s id=%s, error=%s>", RpcResponse.class, this.id, this.error);
        else
            return String.format("<%s id=%s, result=%s>", RpcResponse.class, this.id, this.result);
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public RpcError getError() {
        return error;
    }

    public void setError(RpcError error) {
        this.error = error;
    }
}
