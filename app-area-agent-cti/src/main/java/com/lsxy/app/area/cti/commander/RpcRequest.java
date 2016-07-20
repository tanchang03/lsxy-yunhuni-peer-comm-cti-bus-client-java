package com.lsxy.app.area.cti.commander;

import java.util.Map;

/**
 * Created by liuxy on 16-7-13.
 */
public class RpcRequest {
    @Override
    public String toString() {
        return String.format("<%s id=%s, method=%s, params=%s>", RpcResponse.class, this.id, this.method, this.params);
    }

    private String id = null;
    private String method;
    private Map<String, Object> params = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
