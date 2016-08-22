package com.lsxy.app.area.cti;

import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CTI服务器(IPSC实例)负载信息
 * <p>
 * Created by tanbr on 2016/8/22.
 */
public class ServerInfo {
    String id;              //ipsc的id号
    String name;
    Integer type;
    String machineName;
    String os;
    Integer mode;
    String prj;
    Long pi;
    String ipscVersion;
    LocalDateTime startupTime;
    Integer dogStatus;
    Integer loadlevel;          //系统负载水平。0-100，数字越大，表示负载率越高。
    Map<String, Integer> loads = new ConcurrentHashMap<>(); // KEY-VALUE 负载信息表

    @Override
    public String toString() {
        return String.format("<%s id=%s, name=%s, type=%d, machineName=%s, os=%s, mode=%d, prj=%s, pi=%s, ipscVersion=%s, startupTime=%s, dogStatus=%d, loadlevel=%d, loads=%s>",
                ServerInfo.class, id, name, type, machineName, os, mode, prj, pi, ipscVersion, startupTime, dogStatus, loadlevel, loads);
    }

    /**
     * ipsc的id号
     */
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getType() {
        return type;
    }

    public String getMachineName() {
        return machineName;
    }

    public String getOs() {
        return os;
    }

    public Integer getMode() {
        return mode;
    }

    public String getPrj() {
        return prj;
    }

    public Long getPi() {
        return pi;
    }

    public String getIpscVersion() {
        return ipscVersion;
    }

    public LocalDateTime getStartupTime() {
        return startupTime;
    }

    public Integer getDogStatus() {
        return dogStatus;
    }

    /**
     * 系统负载水平。0-100，数字越大，表示负载率越高。
     */
    public Integer getLoadLevel() {
        return loadlevel;
    }

    /**
     * 负载指数 {@code Map<String, Integer>} Key-Value 对.
     * <p>
     * 负载名称是 Key，值是 Value.
     * <p>
     * 负载 Key 有：
     * <ul>
     * <li>{@code callin.count} 累计呼入总数</li>
     * <li>{@code callout.count} 累计呼出总数</li>
     * <li>{@code callin.num} 累计呼入总数</li>
     * <li>{@code callout.num} 当前呼出数</li>
     * <li>{@code ch.total.num} 通道资源总数</li>
     * <li>{@code sip.in.total.num} SIP呼入通道资源数</li>
     * <li>{@code sip.out.total.num} SIP呼出通道资源数</li>
     * <li>{@code sip.callin.num} 当前SIP呼入数</li>
     * <li>{@code sip.callout.num} 当前SIP呼出数</li>
     * <li>{@code dsp.used.num} 当前DSP资源使用数</li>
     * </ul>
     */
    public Map<String, Integer> getLoads() {
        return new HashMap<>(loads);
    }

}
