package com.lsxy.app.area.cti;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;

/**
 * CTI BUS 命令处理器
 * <p>
 * Created by tanbr on 2016/8/15.
 */
public class Commander extends Client {
    /**
     * @param unitId        所属的本地Unit节点的ID
     * @param id            客户端ID
     * @param ip            要连接的 CTI BUS 服务器 IP
     * @param port          要连接的 CTI BUS 服务器端口
     * @param eventListener RPC事件监听器
     * @param executor      RPC事件和回复处理的执行器
     * @throws InterruptedException 启动期间程序被中断
     */
    Commander(byte unitId, byte id, String ip, short port, RpcEventListener eventListener, ThreadPoolExecutor executor) throws InterruptedException {
        super(unitId, id, (byte) 10, ip, port);
        this.logger = LoggerFactory.getLogger(Commander.class);
        this.eventListener = eventListener;
        this.executor = executor;
        this.executor.prestartAllCoreThreads();
    }

    RpcEventListener eventListener;
    ThreadPoolExecutor executor;

    /**
     * 在指定的CTI服务(IPSC)节点上新建一个 CTI 资源
     *
     * @param target            目标 IPSC 的 BUS 地址
     * @param name              要新建的资源的名称。
     *                          <br>
     *                          目前支持的资源有:
     *                          <ul>
     *                          <li>呼叫 - {@code "sys.call"}</li>
     *                          <li>会议 - {@code "sys.conf"}</li>
     *                          </ul>
     * @param params            新建资源的参数
     * @param rpcResultListener 调用返回结果监听器. {@code null} 则无结果回调。
     * @return 此次调用的 RPC ID
     * @throws IOException JSON解码错误
     */
    public String createResource(BusAddress target,
                                 String name, Map<String, Object> params,
                                 RpcResultListener rpcResultListener) throws IOException {
        this.logger.debug(
                ">>> createResource(target={}, name={}, params={}, rpcResultListener={})",
                target, name, params, rpcResultListener
        );
        int dstUnitId = target.getUnitId();
        int dstIpscIndex = target.getClientId();
        // name = IPSC 项目ID.流程ID
        String[] nameParts = name.split(Pattern.quote("."), 2);
        String projectId = nameParts[0];
        String flowId = nameParts[1];
        // 调用流程， IPSC 流程中照这个 ID 进行 RPC 返回
        String rpcId = UUID.randomUUID().toString();
        // 构建 JSON 数据结构格式： [[unit_id, client_id], rpc_id, params]
        Object[] obj = new Object[3];
        Integer[] item0 = new Integer[2];
        item0[0] = (int) this.getUnitId();
        item0[1] = (int) this.getId();
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
            Unit.pushRpcResultListener(rpcResultListener);
        }
        // 调用 JNI：启动 IPSC 流程
        this.logger.debug(
                "createResource: >>> launchFlow(id={}, dstUnitId={}, dstIpscIndex={}, projectId={}, flowId={}, params={})",
                this.getId(), dstUnitId, dstIpscIndex, projectId, flowId, w.toString()
        );
        int fiId = com.lsxy.app.area.cti.busnetcli.Client.launchFlow(
                this.getId(), dstUnitId, dstIpscIndex, projectId, flowId, 1, 0, w.toString()
        );
        this.logger.debug("createResource: <<< launchFlow() -> {}", fiId);
        if (fiId < 0) {
            // 出错了，撤销接收器于等待队列
            if (rpcResultListener != null)
                Unit.popRpcResultListener(rpcResultListener);
            throw new RuntimeException(String.format("com.lsxy.app.area.cti.busnetcli.Client.launchFlow() returns %d", fiId));
        }
        //返回 RPC ID
        this.logger.debug("<<< createResource() -> {}", rpcId);
        return rpcId;
    }

    /**
     * 操作指定的CTI服务(IPSC)节点上的 CTI 资源
     *
     * @param target            目标 IPSC 的 BUS 地址
     * @param id                要操作的资源的ID
     * @param method            操作方法名
     * @param params            操作方法的参数
     * @param rpcResultListener 调用返回结果监听器. {@code null} 则无结果回调。
     * @return 此次调用的 RPC ID
     * @throws IOException JSON解码错误
     */
    public String operateResource(BusAddress target,
                                  String id, String method, Map<String, Object> params,
                                  RpcResultListener rpcResultListener) throws IOException {
        this.logger.debug(
                ">>> operateResource(target={}, id={}, method={}, params={}, rpcResultListener={})",
                target, id, method, params, rpcResultListener
        );
        int dstUnitId = target.getUnitId();
        int dstIpscIndex = target.getClientId();
        // name = IPSC 项目ID.流程ID.方法名
        String[] nameParts = method.split(Pattern.quote("."), 3);
        String projectId = nameParts[0];
        // 调用流程， IPSC 流程中照这个 ID 进行 RPC 返回
        String rpcId = UUID.randomUUID().toString();
        // 构建 JSON 数据结构格式： [[unit_id, client_id], rpc_id, method, params]
        Object[] obj = new Object[4];
        Integer[] item0 = new Integer[2];
        item0[0] = (int) this.getUnitId();
        item0[1] = (int) this.getId();
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
            Unit.pushRpcResultListener(rpcResultListener);
        }
        // 调用 JNI：向 IPSC 流程发送订阅通知
        this.logger.debug(
                "operateResource: >>> sendNotification(id={}, dstUnitId={}, dstIpscIndex={}, projectId={}, titleId={}, params={})",
                this.getId(), dstUnitId, dstIpscIndex, projectId, id, w.toString()
        );
        int ivkId = com.lsxy.app.area.cti.busnetcli.Client.sendNotification(
                this.getId(), dstUnitId, dstIpscIndex, projectId, id, 0, 15 * 1000, w.toString()
        );
        this.logger.debug("operateResource: <<< sendNotification() -> {}", ivkId);
        if (ivkId < 0) {
            // 出错了，撤销接收器于等待队列
            if (rpcResultListener != null)
                Unit.popRpcResultListener(rpcResultListener);
            throw new RuntimeException(String.format("com.lsxy.app.area.cti.busnetcli.Client.sendNotification() returns %d", ivkId));
        }
        //返回 RPC ID
        this.logger.debug("<<< operateResource() -> {}", rpcId);
        return rpcId;
    }

}
