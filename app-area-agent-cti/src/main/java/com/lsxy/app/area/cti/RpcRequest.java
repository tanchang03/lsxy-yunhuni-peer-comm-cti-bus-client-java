package com.lsxy.app.area.cti;

import java.util.Map;

/**
 * RPC 请求。JAVA应用服务发送的RPC请求，CTI服务发送的RPC事件通知，使用这个类型
 * <p>
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

    /**
     * @return RPC请求ID。RPC事件的 id 参数总是 null
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return 方法名/事件名
     */
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * @return 参数
     */
    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
