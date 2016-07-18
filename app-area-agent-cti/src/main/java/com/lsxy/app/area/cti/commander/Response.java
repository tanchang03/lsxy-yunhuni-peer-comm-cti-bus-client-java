package com.lsxy.app.area.cti.commander;

/**
 * Created by liuxy on 16-7-13.
 */
public class Response {
    private String id;
    private Object result = null;
    private ResponseError error = null;

    @Override
    public String toString() {
        if (this.getError() == null)
            return String.format("<%s id=%s, error=%s>", Response.class, this.getId(), this.getError());
        else
            return String.format("<%s id=%s, result=%s>", Response.class, this.getId(), this.getResult());
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public ResponseError getError() {
        return error;
    }

    public void setError(ResponseError error) {
        this.error = error;
    }
}
