package com.lsxy.app.area.cti.commander;

import java.util.UUID;
import java.util.Map;
import java.io.CharArrayWriter;
import java.io.Writer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Client {
    Client(Byte unitId, Byte id, Byte type, String ip, short port, int queueCapacity) {
        this.unitId = unitId;
        this.id = id;
        this.type = type;
        this.ip = ip;
        this.port = port;
        int cpuCount = Runtime.getRuntime().availableProcessors();
        this.dataExecutor = new ThreadPoolExecutor(1, cpuCount, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(queueCapacity, true));
        this.dataExecutor.prestartAllCoreThreads();
        this.logger = LoggerFactory.getLogger(String.format("%s<%d:%d>", Client.class, unitId, id));
    }

    private Logger logger;
    private Byte unitId;
    private Byte id;
    private Byte type;
    private String ip;
    private short port;
    private ClientCallback callback;
    ThreadPoolExecutor dataExecutor;

    public Byte getUnitId() {
        return unitId;
    }

    public Byte getId() {
        return id;
    }

    public Byte getType() {
        return type;
    }

    public String getIp() {
        return ip;
    }

    public short getPort() {
        return port;
    }

    public String deliverCreation(Byte dstUnitId, Byte dstClientId, String resourceName, Map<String, Object> params, ResponseReceiver receiver) throws Exception {
        this.logger.debug(
                ">>> deliverCreation(dstUnitId={}, dstClientId={}, resourceName={}, params={}, receiver={})",
                dstUnitId, dstClientId, resourceName, params, receiver
        );
        // resourceName = IPSC 项目ID.流程ID
        String[] nameParts = resourceName.split(".", 2);
        String projectId = nameParts[0];
        String flowId = nameParts[1];
        // 调用流程， IPSC 流程中照这个 ID 进行 RPC 返回
        String rpcId = UUID.randomUUID().toString();
        // 构建 JSON 数据结构格式： [[unit_id, client_id], rpc_id, params]
        Object[] obj = new Object[3];
        Integer[] item0 = new Integer[2];
        item0[0] = (int) this.unitId;
        item0[1] = (int) this.id;
        obj[0] = item0;
        obj[1] = rpcId;
        obj[2] = params;
        // 序列化！
        ObjectMapper mapper = new ObjectMapper();
        Writer w = new CharArrayWriter();
        mapper.writeValue(w, obj);
        w.close();
        // 接收器进入等待队列
        receiver.setId(rpcId);
        Commander.setOutgoingRpcReceiver(receiver);
        // 调用 JNI 发送：启动 IPSC 流程
        try {
            int fiId = com.lsxy.app.area.cti.busnetcli.Client.launchFlow(
                    this.id, dstUnitId, dstClientId, projectId, flowId, 1, 0, w.toString()
            );
            if (fiId < 0)
                throw new RuntimeException(String.format("com.lsxy.app.area.cti.busnetcli.Client.launchFlow() returns %d", fiId));
        } catch (Exception exc) {
            // 出错了，撤销接收器于等待队列
            Commander.delOutgoingRpcReceiver(receiver);
            throw exc;
        }
        //返回 RPC ID
        this.logger.debug(
                "<<< deliverCreation(dstUnitId={}, dstClientId={}, resourceName={}, params={}, receiver={}) -> {}",
                dstUnitId, dstClientId, resourceName, params, receiver, rpcId
        );
        return rpcId;
    }
}
