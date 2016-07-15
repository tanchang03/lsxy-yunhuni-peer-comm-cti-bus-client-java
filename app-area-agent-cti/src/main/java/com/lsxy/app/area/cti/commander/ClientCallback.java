package com.lsxy.app.area.cti.commander;

import com.lsxy.app.area.cti.busnetcli.Head;

public interface ClientCallback {
	public void connectSucceed(Integer accessUnitId);

	public void connectFailed(Integer errorCode);

	public void disconnected();

	public void dataReceived(Head head, Byte[] data);

}
