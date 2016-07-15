package com.lsxy.app.area.cti.commander;

import java.util.concurrent.ScheduledFuture;

/**
 * RPC 结果接收器。
 * 在收到正常结果时，回调他的 receive 方法.
 * 在收到错误结果时，回调他的 error 方法.
 * 如果等待超时间，回调他的 timeout 方法.
 * Created by liuxy on 16-7-12.
 */
public abstract class ResponseReceiver {
    private String id;
    private ScheduledFuture future;
    protected int timeout = 15000;

    protected abstract void onReceive(Object result);

    protected abstract void onError(ResponseError error);

    protected abstract void onTimeout();

    ScheduledFuture getFuture() {
        return future;
    }

    void setFuture(ScheduledFuture future) {
        this.future = future;
    }

    /**
     * 结果等待超时值 MILLISECONDS
     */
    public int getTimeout() {
        return this.timeout;
    }

    void setId(String val) {
        this.id = val;
    }

    public String getId() {
        return this.id;
    }


}
