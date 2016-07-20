package com.lsxy.app.area.cti.commander;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 命令处理器
 * <p>
 * 命令处理器实际上是对 CTI BUS JNI 客户端的一次在封装，以适应云呼你项目中关于CTI服务调用的规定。
 * <p>
 * 一个进程只用使用一个 {@link Commander}
 */
public class Commander {
    /**
     * 初始化 JNI 库
     * <p>
     * 在使用 {@link Commander} 的其它功能之前，必须使用 {@link Commander#initiate} 进行初始化。
     * 该方法只能执行一次。
     *
     * @param localUnitId 该进程在CTI BUS 中的单元ID(Unit Id)
     */
    public static void initiate(byte localUnitId) {
        logger.info(">>> initiate(localUnitId={})", localUnitId);
        unitId = localUnitId;
        int errCode = com.lsxy.app.area.cti.busnetcli.Client.initiateLibrary(unitId);
        if (errCode != 0) {
            throw new RuntimeException(
                    String.format(
                            "com.lsxy.app.area.cti.busnetcli.Client.initiateLibrary(localUnitId=%d) returns %d",
                            localUnitId, errCode
                    )
            );
        }
        com.lsxy.app.area.cti.busnetcli.Client.setCallbacks(new LibCallbackHandler());
        logger.info("<<< initiate()");
    }

    /**
     * 释放JNI库
     */
    public static void release() {
        logger.warn(">>> release()");
        com.lsxy.app.area.cti.busnetcli.Client.releaseLibrary();
        logger.warn("<<< release()");
    }

    private static Byte unitId;

    /**
     * @return 该命令处理器的 CTI BUS 单元ID (Unit Id)
     */
    public static Byte getUnitId() {
        return unitId;
    }


    private static final Logger logger = LoggerFactory.getLogger(Commander.class);
    static final Map<Byte, Client> clients = new ConcurrentHashMap<>();
    private static final Map<String, RpcResultListener> rpcResultListenerMap = new ConcurrentHashMap<>();
    private static final ScheduledThreadPoolExecutor rpcResultListenerTimer = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());

    static {
        rpcResultListenerTimer.setRemoveOnCancelPolicy(true);
    }

    static void pushRpcResultListener(final RpcResultListener rpcResultListener) {
        logger.debug(">>> pushRpcResultListener(id={})", rpcResultListener.getId());
        ScheduledFuture fut = rpcResultListenerTimer.schedule(() -> {
            logger.debug("OutgoingRpcReceiver(id={}) Timeout", rpcResultListener.getId());
            try {
                rpcResultListenerMap.remove(rpcResultListener.getId());
                rpcResultListener.onTimeout();
            } catch (Exception e) {
                logger.error(String.format("rpcResultListenerTimer schedule error(id=%s)", rpcResultListener.getId()), e);
                throw e;
            }
        }, rpcResultListener.getTimeout(), TimeUnit.MILLISECONDS);
        rpcResultListener.setFuture(fut);
        rpcResultListenerMap.put(rpcResultListener.getId(), rpcResultListener);
        logger.debug("<<< pushRpcResultListener()");
    }

    static RpcResultListener popRpcResultListener(String rpcId) {
        RpcResultListener receiver = rpcResultListenerMap.remove(rpcId);
        if (receiver == null) return null;
        receiver.getFuture().cancel(false);
        return receiver;
    }

    static RpcResultListener popRpcResultListener(RpcResultListener rpcResultListener) {
        return popRpcResultListener(rpcResultListener.getId());
    }

    static void rpcResponded(RpcResponse response) {
        logger.debug(">>> rpcResponded(response={})", response);
        RpcResultListener receiver = popRpcResultListener(response.getId());
        if (receiver == null) {
            logger.warn("rpcResponded(response={}) cannot be found in rpcResultListenerMap.", response);
            return;
        }
        if (response.getResult() != null) {
            receiver.onResult(response.getResult());
        } else {
            receiver.onError(response.getError());
        }
        logger.debug("<<< rpcResponded()");
    }

    /**
     * 建立一个bus客户端链接
     *
     * @param localClientId     本地clientid, >= 0 and <= 255
     * @param localClientType   本地clienttype 要大于 8
     * @param ip                BUS服务器IP地址
     * @param port              BUS服务器端口
     * @param eventListener     该客户端的事件监听器
     * @param corePoolSize      该客户端内部 ThreadPoolExecutor 的 corePoolSize
     * @param maximumPoolSize   该客户端内部 ThreadPoolExecutor 的 maximumPoolSize
     * @param poolKeepAliveTime 该客户端内部 ThreadPoolExecutor 的 keepAliveTime
     * @param poolKeepAliveUnit 该客户端内部 ThreadPoolExecutor 的 keepAliveUnit
     * @param poolCapacity      该客户端内部 ThreadPoolExecutor ArrayBlockingQueue 的 capacity
     * @return 新建的客户端对象
     * @throws InterruptedException 程序结束?
     */
    public static Client createClient(byte localClientId, byte localClientType, String ip, short port, RpcEventListener eventListener,
                                      int corePoolSize, int maximumPoolSize, long poolKeepAliveTime, TimeUnit poolKeepAliveUnit, int poolCapacity) throws InterruptedException {
        logger.info(
                ">>> createClient(localClientId={}, localClientType={}, ip={}, port={}, corePoolSize={}, maximumPoolSize={}, poolKeepAliveTime={}, poolKeepAliveUnit={}, poolCapacity={})",
                localClientId, localClientType, ip, port, corePoolSize, maximumPoolSize, poolKeepAliveTime, poolKeepAliveUnit, poolCapacity
        );
        Client client = new Client(unitId, localClientId, localClientType, ip, port, eventListener, corePoolSize, maximumPoolSize, poolKeepAliveTime, poolKeepAliveUnit, poolCapacity);
        clients.put(localClientId, client);
        logger.info("<<< createClient() -> {}", client);
        return client;
    }

    /**
     * 建立一个bus客户端链接
     * <p>
     * 新建的 {@link Client} 对象的线程池执行器的 corePoolSize是1，maximumPoolSize是处理器核心数，keepAliveTime是1分钟，capacity是处理器核心数乘以1000。
     *
     * @param localClientId   本地clientid, >= 0 and <= 255
     * @param localClientType 本地clienttype 要大于 8
     * @param ip              BUS服务器IP地址
     * @param port            BUS服务器端口
     * @param eventListener   该客户端的事件监听器
     * @return 新建的客户端对象
     * @throws InterruptedException 程序结束?
     */
    public static Client createClient(byte localClientId, byte localClientType, String ip, short port, RpcEventListener eventListener) throws InterruptedException {
        int processors = Runtime.getRuntime().availableProcessors();
        return createClient(
                localClientId, localClientType, ip, port, eventListener,
                1, processors, 1, TimeUnit.SECONDS, processors * 1000
        );
    }

    /**
     * 建立一个bus客户端链接
     * <p>
     * 新建的 {@link Client} 对象的线程池执行器的 corePoolSize是1，maximumPoolSize是处理器核心数，keepAliveTime是1分钟，capacity是处理器核心数乘以1000.
     * 其客户端BUS类型是10。
     *
     * @param localClientId 本地clientid, >= 0 and <= 255
     * @param ip            BUS服务器IP地址
     * @param port          BUS服务器端口
     * @param eventListener 该客户端的事件监听器
     * @return 新建的客户端对象
     * @throws InterruptedException 程序结束?
     */
    public static Client createClient(byte localClientId, String ip, short port, RpcEventListener eventListener) throws InterruptedException {
        return createClient(localClientId, (byte) 10, ip, port, eventListener);
    }

    /**
     * 建立一个bus客户端链接
     * <p>
     * 新建的 {@link Client} 对象的线程池执行器的 corePoolSize是1，maximumPoolSize是处理器核心数，keepAliveTime是1分钟，capacity是处理器核心数乘以1000.
     * 其客户端BUS类型是10。连接的端口是 8088。
     *
     * @param localClientId 本地clientid, >= 0 and <= 255
     * @param ip            BUS服务器IP地址
     * @param eventListener 该客户端的事件监听器
     * @return 新建的客户端对象
     * @throws InterruptedException 程序结束?
     */
    public static Client createClient(byte localClientId, String ip, RpcEventListener eventListener) throws InterruptedException {
        return createClient(localClientId, ip, (short) 8088, eventListener);
    }
}