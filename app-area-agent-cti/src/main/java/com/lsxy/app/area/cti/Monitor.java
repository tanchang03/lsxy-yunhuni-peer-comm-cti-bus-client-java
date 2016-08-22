package com.lsxy.app.area.cti;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
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
        serverInfoMap = new ConcurrentHashMap<>();
    }

    ThreadPoolExecutor executor;
    private ConcurrentHashMap<String, ServerInfo> serverInfoMap;

    private Map<String, String> parseKeyValStr(String s) {
        logger.debug("s={}", s);
        String[] ss = s.split("(,|;|\\|)");
        logger.debug("ss={}, len(ss)={}", s, ss.length);
        Map<String, String> result = new HashMap<>(ss.length);
        for (String i : ss) {
            String[] kv = i.split("=", 2);
            String key = null;
            String value = null;
            if (kv.length > 0)
                key = kv[0];
            if (kv.length > 1)
                value = kv[1];
            if (key != null)
                result.put(key, value);
        }
        return result;
    }

    void process(String s) {
        Integer flag = null;
        String[] parts = s.split(":", 2);
        if ("svr".equals(parts[0].toLowerCase())) {
            flag = 0;
        } else if ("svrres".equals(parts[1].toLowerCase())) {
            flag = 1;
        }
        if (flag == null)
            return;
        Map<String, String> kvs = parseKeyValStr(parts[1]);
        String id = kvs.remove("id");
        ServerInfo si = serverInfoMap.get(id);
        if (si == null) {
            si = new ServerInfo();
            serverInfoMap.put(id, si);
        }
        if (flag == 0) {
            si.name = kvs.get("name");
            if (kvs.get("type") == null)
                si.type = null;
            else
                si.type = Integer.parseInt(kvs.get("type"));
            si.machineName = kvs.get("machinename");
            si.os = kvs.get("os");
            if (kvs.get("mode") == null)
                si.mode = null;
            else
                si.mode = Integer.parseInt(kvs.get("mode").trim());
            si.prj = kvs.get("prj");
            if (kvs.get("pi") == null)
                si.pi = null;
            else
                si.pi = Long.parseLong(kvs.get("pi").trim());
            si.ipscVersion = kvs.get("ipsc_version");
            if (kvs.get("startup_time") == null)
                si.startupTime = null;
            else
                si.startupTime = LocalDateTime.parse(kvs.get("startup_time").trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (kvs.get("dog_status") == null) si.dogStatus = null;
            else si.dogStatus = Integer.parseInt(kvs.get("dog_status").trim());
            if (kvs.get("loadlevel") == null)
                si.loadlevel = null;
            else
                si.loadlevel = Integer.parseInt(kvs.get("loadlevel").trim());
        } else {
            ServerInfo _si = si;
            kvs.forEach((k, v) -> _si.loads.put(k, Integer.parseInt(v)));
        }

        logger.debug("%s", getServerInfoMap().get(id));
    }

    public Map<String, ServerInfo> getServerInfoMap() {
        return new HashMap<>(serverInfoMap);
    }

}
