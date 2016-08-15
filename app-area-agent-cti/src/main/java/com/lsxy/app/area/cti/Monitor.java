package com.lsxy.app.area.cti;

import org.slf4j.LoggerFactory;

/**
 * CTI BUS 负载数据监听器
 * <p>
 * Created by tanbr on 2016/8/15.
 */
public class Monitor extends Client {
    /**
     * @param unitId 所属的本地Unit节点的ID
     * @param id     客户端ID
     * @param ip     要连接的 CTI BUS 服务器 IP
     * @param port   要连接的 CTI BUS 服务器端口
     * @throws InterruptedException 启动期间程序被中断
     */
    Monitor(byte unitId, byte id, String ip, short port) throws InterruptedException {
        super(unitId, id, (byte) 3, ip, port);
        this.logger = LoggerFactory.getLogger(Monitor.class);
    }
}
