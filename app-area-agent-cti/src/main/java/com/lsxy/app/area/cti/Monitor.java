package com.lsxy.app.area.cti;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

/**
 * CTI BUS 负载数据监听器
 * <p>
 * Created by tanbr on 2016/8/15.
 * <p>
 *
 * @see <a href="http://cf.liushuixingyun.com/pages/viewpage.action?pageId=1803231">YEP 8 -- 区域代理配置数据项</a>
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
        executor = new ThreadPoolExecutor(1, 1, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(100));
        executor.prestartAllCoreThreads();
    }

    private ThreadPoolExecutor executor;

    void onData(String data) {
        executor.execute(() -> {
            this.logger.debug("onData(data={}", data);
        });
    }
}
