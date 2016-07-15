package com.lsxy.app.area.cti.commander;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Commander {
    /**
     * 初始化
     *
     * @param localUnitId BUS单元ID
     */
    public static int initiate(Byte localUnitId, CommanderCallback commanderCallback) {
        logger.debug(">>> initiate(localUnitId={})", localUnitId);
        Integer errCode = com.lsxy.app.area.cti.busnetcli.Client.initiateLibrary(localUnitId);
        if (errCode != 0) {
            throw new RuntimeException(
                    String.format(
                            "com.lsxy.app.area.cti.busnetcli.Client.initiateLibrary(localUnitId=%d) returns %d",
                            localUnitId, errCode
                    )
            );
        }
        unitId = localUnitId;
        callback = commanderCallback;
        com.lsxy.app.area.cti.busnetcli.Client.setCallbacks(new LibCallbackHandler());
        logger.debug("<<< initiate -> {}", errCode);
        return errCode;
    }

    private static Byte unitId;

    public static Byte getUnitId() {
        return unitId;
    }


    private static final Logger logger = LoggerFactory.getLogger(Commander.class);
    static CommanderCallback callback;
    static final Map<Byte, Client> clients = new ConcurrentHashMap<Byte, Client>();
    private static final Map<String, ResponseReceiver> outgoingRpcMap = new ConcurrentHashMap<String, ResponseReceiver>();
    private static final ScheduledThreadPoolExecutor outgoingRpcTimer = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());

    static {
        outgoingRpcTimer.setRemoveOnCancelPolicy(true);
    }

    static void setOutgoingRpcReceiver(final ResponseReceiver receiver) {
        logger.debug(">>> setOutgoingRpcReceiver(id={})", receiver.getId());
        ScheduledFuture fut = outgoingRpcTimer.schedule(() -> {
            logger.debug("OutgoingRpcReceiver(id={}) Timeout", receiver.getId());
            try {
                outgoingRpcMap.remove(receiver.getId());
                receiver.onTimeout();
            } catch (Exception exc) {
                logger.error(String.format("outgoingRpcTimer schedule error(id=%s)", receiver.getId()), exc);
                throw exc;
            }
        }, receiver.getTimeout(), TimeUnit.MILLISECONDS);
        receiver.setFuture(fut);
        outgoingRpcMap.put(receiver.getId(), receiver);
        logger.debug("<<< setOutgoingRpcReceiver(id={})", receiver.getId());
    }

    static ResponseReceiver delOutgoingRpcReceiver(String rpcId) {
        ResponseReceiver receiver = outgoingRpcMap.remove(rpcId);
        if (receiver == null) return null;
        receiver.getFuture().cancel(false);
        return receiver;
    }

    static ResponseReceiver delOutgoingRpcReceiver(ResponseReceiver receiver) {
        return delOutgoingRpcReceiver(receiver.getId());
    }

    static void outgoingRpcDone(Response response) {
        ResponseReceiver receiver = delOutgoingRpcReceiver(response.getId());
        if (response.getResult() != null) {
            receiver.onReceive(response.getResult());
        } else {
            receiver.onError(response.getError());
        }
    }

    /**
     * 建立一个bus客户端链接
     *
     * @param localClientId   本地clientid, >= 0 and <= 255
     * @param localClientType 本地clienttype
     * @param ip              BUS服务器IP地址
     * @param port            BUS服务器端口
     * @return 新建的客户端对象
     */
    public static Client createClient(Byte localClientId, Byte localClientType, String ip, short port, int queueCapacity) {
        logger.debug(
                ">>> createClient(localClientId={}, localClientType={}, ip={}, port={}, queueCapacity={})",
                localClientId, localClientType, ip, port, queueCapacity
        );
        Integer errCode = com.lsxy.app.area.cti.busnetcli.Client.createConnect(localClientId, localClientType, ip, port, "", (short) 0xff, "", "", "");
        if (errCode != 0) {
            throw new RuntimeException(
                    String.format("com.lsxy.app.area.cti.busnetcli.Client.createConnect returns %d", errCode));
        }
        Client client = new Client(unitId, localClientId, localClientType, ip, port, queueCapacity);
        clients.put(localClientId, client);
        logger.debug("<<< createClient -> {}", client);
        return client;
    }

    /**
     * 建立一个bus客户端链接
     *
     * @param localClientId   本地clientid, >= 0 and <= 255
     * @param localClientType 本地clienttype
     * @param ip              BUS服务器IP地址
     * @return 新建的客户端对象
     */
    public static Client createClient(Byte localClientId, Byte localClientType, String ip) {
        return createClient(localClientId, localClientType, ip, (short) 8088, 1024 * 4);
    }
}