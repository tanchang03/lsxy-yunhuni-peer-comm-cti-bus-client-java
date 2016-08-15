package com.lsxy.app.area.cti;

/**
 * RPC 回复。CTI服务使用这个格式响应java应用服务的RPC调用
 * <p>
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

    /**
     * @return 对应的 RPC ID
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return RPC 返回值。如果出错，返回值必须为 null
     */
    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    /**
     * @return RPC 错误信息。如果该属性不是null，表明RPC执行出错。
     */
    public RpcError getError() {
        return error;
    }

    public void setError(RpcError error) {
        this.error = error;
    }
}
