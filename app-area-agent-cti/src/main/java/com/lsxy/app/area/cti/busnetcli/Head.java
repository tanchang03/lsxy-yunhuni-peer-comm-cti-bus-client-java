package com.lsxy.app.area.cti.busnetcli;

/**
 * 消息的头，包含发送者，接收者的 BUS 地址信息
 *
 * @author 雪彦
 */
public class Head {

    public Head(byte flag, byte cmd, byte cmdType, byte srcUnitId,
                byte srcClientId, byte srcClientType, byte dstUnitId,
                byte dstClientId, byte dstClientType) {
        this.flag = flag;
        this.cmd = cmd;
        this.cmdType = cmdType;
        this.srcUnitId = srcUnitId;
        this.srcClientId = srcClientId;
        this.srcClientType = srcClientType;
        this.dstUnitId = dstUnitId;
        this.dstClientId = dstClientId;
        this.dstClientType = dstClientType;
    }

    private final byte flag;
    private final byte cmd;
    private final byte cmdType;
    private final byte srcUnitId;
    private final byte srcClientId;
    private final byte srcClientType;
    private final byte dstUnitId;
    private final byte dstClientId;
    private final byte dstClientType;

    public byte getFlag() {
        return flag;
    }

    public byte getCmd() {
        return cmd;
    }

    public byte getCmdType() {
        return cmdType;
    }

    public byte getSrcUnitId() {
        return srcUnitId;
    }

    public byte getSrcClientId() {
        return srcClientId;
    }

    public byte getSrcClientType() {
        return srcClientType;
    }

    public byte getDstUnitId() {
        return dstUnitId;
    }

    public byte getDstClientId() {
        return dstClientId;
    }

    public byte getDstClientType() {
        return dstClientType;
    }

}
