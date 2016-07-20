package com.lsxy.app.area.cti.commander;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Client {
    Client(byte unitId, byte id, byte type, String ip, short port, RpcEventListener eventListener,
           int corePoolSize, int maximumPoolSize, long poolKeepAliveTime, TimeUnit poolKeepAliveUnit, int poolCapacity) throws InterruptedException {
        this.logger = LoggerFactory.getLogger(String.format("%s(%d,%d)", Client.class.toString(), unitId, id));
        this.unitId = unitId;
        this.id = id;
        this.type = type;
        this.ip = ip;
        this.port = port;
        this.eventListener = eventListener;
        this.dataExecutor = new ThreadPoolExecutor(
                corePoolSize, maximumPoolSize,
                poolKeepAliveTime, poolKeepAliveUnit,
                new ArrayBlockingQueue<>(poolCapacity, true)
        );
        this.dataExecutor.prestartAllCoreThreads();
        int errCode = com.lsxy.app.area.cti.busnetcli.Client.createConnect(
                id, type, ip, port, "", (short) 0xff, "", "", ""
        );
        if (errCode != 0) {
            throw new RuntimeException(
                    String.format("com.lsxy.app.area.cti.busnetcli.Client.createConnect returns %d", errCode)
            );
        }
        Thread.sleep(1000); //Pause for 1 seconds
    }

    Logger logger;
    private byte unitId;
    private byte id;
    private byte type;
    private String ip;
    private short port;
    RpcEventListener eventListener;
    ThreadPoolExecutor dataExecutor;

    public byte getUnitId() {
        return unitId;
    }

    public byte getId() {
        return id;
    }

    public byte getType() {
        return type;
    }

    public String getIp() {
        return ip;
    }

    public short getPort() {
        return port;
    }

    /**
     * 在指定的CTI服务(IPSC)节点上新建一个 CTI 资源
     *
     * @param dstUnitId         目标 IPSC 的 BUS Unit ID
     * @param dstIpscIndex      目标 IPSC 的 进程编号
     * @param name              要新建的资源。目前支持的资源有 呼叫 - sys.call, 会议 - sys.conf
     * @param params            新建资源的参数
     * @param rpcResultListener 调用返回结果监听器
     * @return 此次调用的 RPC ID
     * @throws IOException
     */
    public String createResource(int dstUnitId, int dstIpscIndex,
                                 String name, Map<String, Object> params,
                                 RpcResultListener rpcResultListener) throws IOException {
        this.logger.debug(
                ">>> createResource(dstUnitId={}, dstIpscIndex={}, name={}, params={}, rpcResultListener={})",
                dstUnitId, dstIpscIndex, name, params, rpcResultListener
        );
        // name = IPSC 项目ID.流程ID
        String[] nameParts = name.split(Pattern.quote("."), 2);
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
        if (rpcResultListener != null) {
            rpcResultListener.setId(rpcId);
            Commander.pushRpcResultListener(rpcResultListener);
        }
        // 调用 JNI：启动 IPSC 流程
        this.logger.debug(
                "createResource: >>> launchFlow(id={}, dstUnitId={}, dstIpscIndex={}, projectId={}, flowId={}, params={})",
                this.id, dstUnitId, dstIpscIndex, projectId, flowId, w.toString()
        );
        int fiId = com.lsxy.app.area.cti.busnetcli.Client.launchFlow(
                this.id, dstUnitId, dstIpscIndex, projectId, flowId, 1, 0, w.toString()
        );
        this.logger.debug("createResource: <<< launchFlow() -> {}", fiId);
        if (fiId < 0) {
            // 出错了，撤销接收器于等待队列
            if (rpcResultListener != null)
                Commander.popRpcResultListener(rpcResultListener);
            throw new RuntimeException(String.format("com.lsxy.app.area.cti.busnetcli.Client.launchFlow() returns %d", fiId));
        }
        //返回 RPC ID
        this.logger.debug("<<< createResource() -> {}", rpcId);
        return rpcId;
    }

    /**
     * 操作指定的CTI服务(IPSC)节点上的 CTI 资源
     *
     * @param dstUnitId         目标 IPSC 的 BUS Unit ID
     * @param dstIpscIndex      目标 IPSC 的 进程编号
     * @param id                要操作的资源的ID
     * @param method            操作方法名
     * @param params            操作方法的参数
     * @param rpcResultListener 调用返回结果监听器
     * @return 此次调用的 RPC ID
     * @throws IOException
     */
    public String operateResource(int dstUnitId, int dstIpscIndex,
                                  String id, String method, Map<String, Object> params,
                                  RpcResultListener rpcResultListener) throws IOException {
        this.logger.debug(
                ">>> operateResource(dstUnitId={}, dstIpscIndex={}, id={}, method={}, params={}, rpcResultListener={})",
                dstUnitId, dstIpscIndex, id, method, params, rpcResultListener
        );
        // name = IPSC 项目ID.流程ID
        String[] nameParts = method.split(Pattern.quote("."), 2);
        String projectId = nameParts[0];
        // 调用流程， IPSC 流程中照这个 ID 进行 RPC 返回
        String rpcId = UUID.randomUUID().toString();
        // 构建 JSON 数据结构格式： [[unit_id, client_id], rpc_id, method, params]
        Object[] obj = new Object[4];
        Integer[] item0 = new Integer[2];
        item0[0] = (int) this.unitId;
        item0[1] = (int) this.id;
        obj[0] = item0;
        obj[1] = rpcId;
        obj[2] = method;
        obj[3] = params;
        // 序列化！
        ObjectMapper mapper = new ObjectMapper();
        Writer w = new CharArrayWriter();
        mapper.writeValue(w, obj);
        w.close();
        // 接收器进入等待队列
        if (rpcResultListener != null) {
            rpcResultListener.setId(rpcId);
            Commander.pushRpcResultListener(rpcResultListener);
        }
        // 调用 JNI：向 IPSC 流程发送订阅通知
        this.logger.debug(
                "operateResource: >>> sendNotification(id={}, dstUnitId={}, dstIpscIndex={}, projectId={}, titleId={}, params={})",
                this.id, dstUnitId, dstIpscIndex, projectId, id, w.toString()
        );
        int ivkId = com.lsxy.app.area.cti.busnetcli.Client.sendNotification(
                this.id, dstUnitId, dstIpscIndex, projectId, id, 0, 15 * 1000, w.toString()
        );
        this.logger.debug("operateResource: <<< sendNotification() -> {}", ivkId);
        if (ivkId < 0) {
            // 出错了，撤销接收器于等待队列
            Commander.popRpcResultListener(rpcResultListener);
            throw new RuntimeException(String.format("com.lsxy.app.area.cti.busnetcli.Client.sendNotification() returns %d", ivkId));
        }
        //返回 RPC ID
        this.logger.debug("<<< operateResource() -> {}", rpcId);
        return rpcId;
    }

}
