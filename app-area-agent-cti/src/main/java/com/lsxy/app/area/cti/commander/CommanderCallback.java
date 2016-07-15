package com.lsxy.app.area.cti.commander;

interface CommanderCallback {
	public void globalConnect(Byte unitId, Byte clientId, Byte clientType, ConnectStatus status, String addInfo);

	public void loggingText(String msg, Boolean isErr);
}
