package com.lsxy.app.area.cti.commander;

import java.util.Map;

/**
 * Created by tanbr on 2016/7/20.
 */
public interface RpcEventListener {
    /**
     *
     * @param request 该事件所对应的RPC请求
     */
    void onEvent(RpcRequest request);
}
