package com.lsxy.app.area.cti.commander;

/**
 * RPC 事件监听器
 * <p>
 * Created by tanbr on 2016/7/20.
 */
public interface RpcEventListener {
    /**
     * 回调：收到CTI的事件通知
     *
     * @param source  发送该事件的IPSC服务实例
     * @param request 该事件所对应的RPC请求
     */
    void onEvent(BusAddress source, RpcRequest request);
}
