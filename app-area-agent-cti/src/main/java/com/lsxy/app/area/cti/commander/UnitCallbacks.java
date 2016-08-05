package com.lsxy.app.area.cti.commander;

/**
 * 单元级别事件回调接口
 *
 * @author tanbr
 * @implNote 这个接口类中的回调方法在执行期间会阻塞底层共享库的IO，应<strong>尽快</strong>从方法返回！
 */
public interface UnitCallbacks {
    /**
     * 客户端连接成功
     *
     * @param client 连接状态发生变化的客户端
     */
    void connectSucceed(Client client);

    /**
     * 客户端连接失败
     *
     * @param client    连接状态发生变化的客户端
     * @param errorCode 连接失败原因（参考该项目 C Library 的头文件吧）
     */
    void connectFailed(Client client, int errorCode);

    /**
     * 客户端连接丢失
     *
     * @param client 连接状态发生变化的客户端
     */
    void connectLost(Client client);

    /**
     * 全局（整个 CTI BUS 上的连接状态变化事件）
     *
     * @param unitId     产生连接状态变化的BUS节点的Unit ID
     * @param clientId   产生连接状态变化的BUS节点的Client ID。是node中心节点连接时，client id 值为 -1
     * @param clientType 产生连接状态变化的BUS节点的Client Type<br/>
     *                   类型定义：
     *                   <ul>
     *                   <li><code>0</code>: NULL</li>
     *                   <li><code>1</code>: BUS 服务</li>
     *                   <li><code>2</code>: IPSC（CTI服务进程）服务</li>
     *                   <li><cde>3</cde>: IPSC 监控服务</li>
     *                   </ul>
     * @param status     产生连接状态变化的BUS节点的连接状态 <br/>
     *                   状态值：
     *                   <ul>
     *                   <li><code>0</code>: 断开连接</li>
     *                   <li><code>1</code>: 新建连接</li>
     *                   <li><code>2</code>: 已有的连接</li>
     *                   </ul>
     * @param addInfo    产生连接状态变化的BUS节点的附加信息
     */
    void globalConnectStateChanged(byte unitId, byte clientId, byte clientType, byte status, String addInfo);
}
