package com.lsxy.app.area.cti.commander;

import java.util.Map;
import java.util.HashMap;

public enum ConnectStatus {
	CONNECTION_LOST(0), CONNECTION_NEW(1), CONNECTION_LIVE(2);

	private int value;
	public static Map<Integer, ConnectStatus> map = new HashMap<Integer, ConnectStatus>();

	private ConnectStatus(int value) {
		this.value = value;
	}

	static {
		for (ConnectStatus obj : ConnectStatus.values()) {
			map.put(obj.value, obj);
		}
	}

	public static ConnectStatus valueOf(int value) {
		return map.get(value);
	}

	public int getValue() {
		return this.value;
	}
}
