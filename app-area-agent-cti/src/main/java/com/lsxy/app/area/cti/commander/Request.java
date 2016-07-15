package com.lsxy.app.area.cti.commander;

import java.util.Map;

/**
 * Created by liuxy on 16-7-13.
 */
public class Request {
    Request(String id, String method, Map<String, Object> params) {
        this.id = id;
        this.method = method;
        this.params = params;
    }

    private String id = null;
    private String method;
    private Map<String, Object> params;

    public String getId() {
        return id;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
