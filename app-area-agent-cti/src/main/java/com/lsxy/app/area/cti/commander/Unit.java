package com.lsxy.app.area.cti.commander;

import java.util.Map;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CTI BUS 单元
 * <p>
 * 它实际上是对 CTI BUS JNI 客户端共享库的一次再封装，以适应云呼你项目中关于CTI服务调用的规定。
 * <p>
 * 一个进程只用使用一个 {@link Unit}
 */
public class Unit {
    /**
     * 初始化 JNI 库
     * <p>
     * 在使用 {@link Unit} 的其它功能之前，必须使用该静态方法进行初始化。
     * 该方法只能执行一次。
     *
     * @param localUnitId    该单元在 CTI BUS 中的单元ID(Unit Id)
     * @param callbacks      单元级别的事件回调函数
     * @param rpcResultTimer RPC返回超时计时器
     */
    public static void initiate(byte localUnitId, UnitCallbacks callbacks, ScheduledThreadPoolExecutor rpcResultTimer) {
        logger.info(">>> initiate(localUnitId={}, callbacks={})", localUnitId, callbacks);
        Unit.localUnitId = localUnitId;
        int errCode = com.lsxy.app.area.cti.busnetcli.Client.initiateLibrary(Unit.localUnitId);
        if (errCode != 0) {
            throw new RuntimeException(
                    String.format(
                            "com.lsxy.app.area.cti.busnetcli.Client.initiateLibrary(localUnitId=%d) returns %d",
                            localUnitId, errCode
                    )
            );
        }
        Unit.callbacks = callbacks;
        com.lsxy.app.area.cti.busnetcli.Client.setCallbacks(new LibCallbackHandler());
        if (rpcResultTimer == null) {
            Unit.rpcResultTimer = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
            Unit.rpcResultTimer.setRemoveOnCancelPolicy(true);
        } else {
            Unit.rpcResultTimer = rpcResultTimer;
        }
        logger.info("<<< initiate()");
    }

    /**
     * 初始化 JNI 库
     * <p>
     * 在使用 {@link Unit} 的其它功能之前，必须使用该静态方法进行初始化。
     * 该方法只能执行一次。
     *
     * @param localUnitId 该单元在 CTI BUS 中的单元ID(Unit Id)
     * @param callbacks   单元级别的事件回调函数
     */
    public static void initiate(byte localUnitId, UnitCallbacks callbacks) {
        initiate(localUnitId, callbacks, null);
    }

    /**
     * 初始化 JNI 库
     * <p>
     * 在使用 {@link Unit} 的其它功能之前，必须使用该静态方法进行初始化。
     * 该方法只能执行一次。
     *
     * @param localUnitId 该单元在 CTI BUS 中的单元ID(Unit Id)
     */
    public static void initiate(byte localUnitId) {
        initiate(localUnitId, null, null);
    }

    /**
     * 释放JNI库
     */
    public static void release() {
        logger.warn(">>> release()");
        com.lsxy.app.area.cti.busnetcli.Client.releaseLibrary();
        logger.warn("<<< release()");
    }

    private static Byte localUnitId;
    static UnitCallbacks callbacks;

    /**
     * @return 该命令处理器的 CTI BUS 单元ID (Unit Id)
     */
    public static Byte getLocalUnitId() {
        return localUnitId;
    }


    private static final Logger logger = LoggerFactory.getLogger(Unit.class);
    static final Map<Byte, Client> clients = new ConcurrentHashMap<>();
    private static final Map<String, RpcResultListener> rpcResultMap = new ConcurrentHashMap<>();
    private static ScheduledThreadPoolExecutor rpcResultTimer;

    static void pushRpcResultListener(final RpcResultListener rpcResultListener) {
        logger.debug(">>> pushRpcResultListener(id={})", rpcResultListener.getId());
        ScheduledFuture fut = rpcResultTimer.schedule(() -> {
            logger.debug("OutgoingRpcReceiver(id={}) Timeout", rpcResultListener.getId());
            try {
                rpcResultMap.remove(rpcResultListener.getId());
                rpcResultListener.onTimeout();
            } catch (Exception e) {
                logger.error(String.format("rpcResultTimer schedule error(id=%s)", rpcResultListener.getId()), e);
                throw e;
            }
        }, rpcResultListener.getTimeout(), TimeUnit.MILLISECONDS);
        rpcResultListener.setFuture(fut);
        rpcResultMap.put(rpcResultListener.getId(), rpcResultListener);
        logger.debug("<<< pushRpcResultListener()");
    }

    static RpcResultListener popRpcResultListener(String rpcId) {
        RpcResultListener receiver = rpcResultMap.remove(rpcId);
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
            logger.warn("rpcResponded(response={}) cannot be found in rpcResultMap.", response);
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
     * @param localClientId   本地clientid
     * @param localClientType 本地clienttype 要大于 8
     * @param ip              BUS服务器IP地址
     * @param port            BUS服务器端口
     * @param eventListener   该客户端的事件监听器
     * @param executor        该客户端内部的ThreadPoolExecutor，用于处理异步的消息返回
     * @return 新建的客户端对象
     * @throws InterruptedException 程序结束?
     */
    public static Client createClient(byte localClientId, byte localClientType, String ip, short port,
                                      RpcEventListener eventListener, ThreadPoolExecutor executor) throws InterruptedException {
        logger.info(
                ">>> createClient(localClientId={}, localClientType={}, ip={}, port={}, eventListener={}, executor={})",
                localClientId, localClientType, ip, port, eventListener, executor
        );
        if (executor == null) {
            int processors = Runtime.getRuntime().availableProcessors();
            executor = new ThreadPoolExecutor(
                    processors, processors * 5, 1, TimeUnit.MINUTES,
                    new ArrayBlockingQueue<>(processors * 1000, true)
            );
        }
        Client client = new Client(localUnitId, localClientId, localClientType, ip, port, eventListener, executor);
        clients.put(localClientId, client);
        logger.info("<<< createClient() -> {}", client);
        return client;
    }

    /**
     * 建立一个bus客户端链接
     *
     * @param localClientId   本地clientid
     * @param localClientType 本地clienttype 要大于 8
     * @param ip              BUS服务器IP地址
     * @param port            BUS服务器端口
     * @param eventListener   该客户端的事件监听器
     * @return 新建的客户端对象
     * @throws InterruptedException 程序结束?
     */
    public static Client createClient(byte localClientId, byte localClientType, String ip, short port, RpcEventListener eventListener) throws InterruptedException {
        return createClient(localClientId, localClientType, ip, port, eventListener, null);
    }

    /**
     * 建立一个bus客户端链接
     *
     * @param localClientId 本地clientid
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
     * 新建的 {@link Client} 对象的线程池执行器的
     * 新建的 {@link Client} 对象的线程池执行器的 corePoolSize是处理器核心数，
     * maximumPoolSize是处理器核心数乘以5，
     * keepAliveTime是1分钟，
     * capacity是处理器核心数乘以1000.
     * 其客户端BUS类型是10。连接的端口是 8088。
     *
     * @param localClientId 本地clientid
     * @param ip            BUS服务器IP地址
     * @param eventListener 该客户端的事件监听器
     * @return 新建的客户端对象
     * @throws InterruptedException 程序结束?
     */
    public static Client createClient(byte localClientId, String ip, RpcEventListener eventListener) throws InterruptedException {
        return createClient(localClientId, ip, (short) 8088, eventListener);
    }
}