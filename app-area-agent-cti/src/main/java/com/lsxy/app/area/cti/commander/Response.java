package com.lsxy.app.area.cti.commander;

/**
 * Created by liuxy on 16-7-13.
 */
public class Response {
    private String id;
    private Object result = null;
    private ResponseError error = null;

    public String getId() {
        return id;
    }

    public Object getResult() {
        return result;
    }

    public ResponseError getError() {
        return error;
    }
}
