package com.lsxy.app.area.cti;

import java.util.concurrent.ScheduledFuture;

/**
 * RPC 结果监听器
 * <p>
 * 在收到正常结果时，回调 {@link #onResult}。
 * 在收到错误结果时，回调 {@link #onError}。
 * 如果等待超时间，回调 {@link #onTimeout}。
 * <p>
 * Created by liuxy on 16-7-12.
 */
public abstract class RpcResultListener {
    private String id;
    private ScheduledFuture future;

    /**
     * 结果等待超时值 MILLISECONDS
     */
    protected int timeout = 15000;

    /**
     * 回调： RPC 返回值
     *
     * @param result 返回值
     */
    protected abstract void onResult(Object result);

    /**
     * 回调：RPC 错误
     *
     * @param error 错误信息
     */
    protected abstract void onError(RpcError error);

    /**
     * 回调：RPC 等待结果超时
     */
    protected abstract void onTimeout();

    ScheduledFuture getFuture() {
        return future;
    }

    void setFuture(ScheduledFuture future) {
        this.future = future;
    }

    /**
     * @return 结果等待超时值 MILLISECONDS
     */
    public int getTimeout() {
        return this.timeout;
    }

    void setId(String val) {
        this.id = val;
    }

    /**
     * @return RPC 的 ID
     */
    public String getId() {
        return this.id;
    }


}
