/**
 * 区域 CTI 通信 BUS 的客户端
 *
 * @auth liuxy@yunhuni.com
 * @date 2016-07-01
 */
package com.lsxy.app.area.cti.busnetcli;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * BUS 客户端
 */
public class Client {

    /**
     * 初始化
     *
     * @param unitId 客户端ID。unitId >= 16
     * @return 返回码
     */
    public native static int initiateLibrary(byte unitId);

    /**
     * 库的释放
     */
    public native static void releaseLibrary();

    /**
     * 创建连接
     *
     * @param localClientId   本地clientid, >= 0 and <= 255
     * @param localClientType 本地clienttype
     * @param masterIp        目标主IP地址
     * @param masterPort      目标主端口
     * @param slaverIp        目标从IP地址。没有从地址的，填写0，或者""
     * @param slaverPort      目标从端口。没有从端口的，填写0xFFFF
     * @param authorUserName  验证用户名
     * @param authorPwd       验证密码
     * @param addInfo         附加信息
     * @return 返回码
     */
    public native static int createConnect(byte localClientId, int localClientType, String masterIp, short masterPort,
                                           String slaverIp, short slaverPort, String authorUserName, String authorPwd, String addInfo);

    /**
     * 远程调用流程
     *
     * @param localClientId 本地clientid
     * @param serverUnitId  目标unitid
     * @param ipscIndex     目标clientid
     * @param projectId     目标流程的ProjectID
     * @param flowId        目标流程的FlowID
     * @param mode          调用模式：0 有流程返回、1 无流程返回
     * @param timeout       有流程返回时的等待超时值。单位ms
     * @param valueList     整型、浮点型、字符串及其组合的 object, array
     *                      JSON数组（子流程开始节点的传人参数自动变换为输入参数列表数据。）（对应的字符串内容最大长度不超过16K字节）
     * @return > 0 invoke_id，调用ID，用于流程结果返回匹配用途。< 0 表示错误。
     */
    public native static int launchFlow(byte localClientId, int serverUnitId, int ipscIndex, String projectId,
                                        String flowId, int mode, int timeout, String valueList);

    /**
     * 发送通知消息
     *
     * @param localClientId 本地clientid
     * @param serverUnitId  目标unitid ipscindex
     * @param ipscIndex     目标clientid projectid
     * @param projectId     目标流程的ProjectID
     * @param title         通知的标示
     * @param mode          调用模式。目前无意义，一律使用0
     * @param expires       消息有效期。单位ms
     * @param param         消息数据
     * @return 返回值：> 0 invoke_id，调用ID。< 0 表示错误。
     */
    public native static int sendNotification(byte localClientId, int serverUnitId, int ipscIndex, String projectId,
                                              String title, int mode, int expires, String param);

    /**
     * 发送数据
     *
     * @param localClientId 本地clientid
     * @param cmd           cmd：命令
     * @param cmdType       命令类型，值为 2
     * @param dstUnitId     The destination unitid
     * @param dstClientId   The destination clientid
     * @param dstClientType The destination clienttype
     * @param data          The data
     * @return 0 表示成功、 < 0 表示错误。
     */
    public native static int sendData(byte localClientId, byte cmd, byte cmdType, int dstUnitId, int dstClientId,
                                      int dstClientType, byte[] data);

    static {
        System.loadLibrary("busnetcli-jni");
    }

    static private Callbacks callbacks = null;

    public static void setCallbacks(Callbacks value) {
        callbacks = value;
    }

    protected static void callbackGlobalConnect(byte unitId, byte clientId, byte clientType, byte status,
                                                String addInfo) {
        if (callbacks != null) {
            callbacks.globalConnect(unitId, clientId, clientType, status, addInfo);
        }
    }

    protected static void callbackConnect(byte localClientId, int accessPointUnitId, int ack) {
        if (callbacks != null) {
            callbacks.connect(localClientId, accessPointUnitId, ack);
        }
    }

    protected static void callbackDisconnect(byte localClientId) {
        if (callbacks != null) {
            callbacks.disconnect(localClientId);
        }
    }

    protected static void callbackData(byte cmd, byte cmdType,
                                       byte srcUnitId, byte srcClientId, byte srcClientType,
                                       byte dstUnitId, byte dstClientId, byte dstClientType,
                                       byte[] data) {
        if (callbacks != null) {
            Head head = new Head(cmd, cmdType, srcUnitId, srcClientId, srcClientType, dstUnitId, dstClientId, dstClientType);
            callbacks.data(head, data);
        }
    }

    protected static void callbackLog(byte[] bytesMsg, boolean isErr) throws UnsupportedEncodingException {
        if (callbacks != null) {
            String msg = new String(bytesMsg, "US-ASCII");
            callbacks.log(msg, isErr);
        }
    }

}
