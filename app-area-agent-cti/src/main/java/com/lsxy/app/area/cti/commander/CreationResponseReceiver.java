package com.lsxy.app.area.cti.commander;

/**
 * Created by liuxy on 16-7-13.
 */
public abstract class CreationResponseReceiver extends ResponseReceiver {
    protected abstract void onReceive(String id);
}
