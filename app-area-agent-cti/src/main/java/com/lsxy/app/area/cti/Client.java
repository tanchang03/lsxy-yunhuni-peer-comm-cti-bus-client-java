package com.lsxy.app.area.cti;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CTI BUS 客户端
 * <p>
 * 客户端是从属于 {@link Unit} 的
 * <p>
 * 使用 {@link Unit#createCommander} 创建客户端，<strong>不要</strong>使用构造函数。
 * <p>
 * BUS Client {@code type} 一律是 {@code 10}
 */
public class Client {
    /**
     * @param unitId        所属的本地Unit节点的ID
     * @param id            客户端ID
     * @param type          客户端 type
     * @param ip            要连接的 CTI BUS 服务器 IP
     * @param port          要连接的 CTI BUS 服务器端口
     * @throws InterruptedException 启动期间程序被中断
     */
    Client(byte unitId, byte id, byte type, String ip, short port) throws InterruptedException {
        this.logger = LoggerFactory.getLogger(Client.class);
        this.unitId = unitId;
        this.connectingUnitId = -1;
        this.connected = false;
        this.id = id;
        this.type = type;
        this.ip = ip;
        this.port = port;
        int errCode = com.lsxy.app.area.cti.busnetcli.Client.createConnect(
                this.id, this.type, this.ip, this.port, "", (short) 0xff, "", "", ""
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
    byte connectingUnitId;
    boolean connected;
    private byte id;
    private byte type;
    private String ip;
    private short port;

    /**
     * @return 该客户端所述的本地 BUS UNIT 的 ID，即 {@link Unit#getLocalUnitId} 属性
     */
    public byte getUnitId() {
        return unitId;
    }

    /**
     * @return 是否连接到了 CTI BUS 服务器。
     */
    public boolean getConnected() {
        return connected;
    }

    /**
     * @return 该客户端所连接的CTI服务器的 BUS UNIT ID
     * <br>
     * <code>-1</code> 表示未曾连接到服务器。
     */
    public byte getConnectingUnitId() {
        return connectingUnitId;
    }

    /**
     * @return 该客户端所在其接的CTI服务器的BUS客户端ID(不同的 Unit 下, Client ID 可以重复)
     */
    public byte getId() {
        return id;
    }

    /**
     * @return 该客户端所在其接的CTI服务器的BUS客户端类型标志
     */
    public byte getType() {
        return type;
    }

    /**
     * @return 该客户端所在其接的CTI服务器的IP地址
     */
    public String getIp() {
        return ip;
    }

    /**
     * @return 该客户端所在其接的CTI服务器的端口
     */
    public short getPort() {
        return port;
    }

}
