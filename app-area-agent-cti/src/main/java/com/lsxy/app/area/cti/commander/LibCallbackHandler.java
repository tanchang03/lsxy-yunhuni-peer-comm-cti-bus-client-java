package com.lsxy.app.area.cti.commander;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.lsxy.app.area.cti.busnetcli.Head;

class LibCallbackHandler implements com.lsxy.app.area.cti.busnetcli.Callbacks {

    private final Logger logger = LoggerFactory.getLogger(LibCallbackHandler.class);
    private final Logger jniLogger = LoggerFactory.getLogger("busnetcli");

    public void globalConnect(byte unitId, byte clientId, byte clientType, byte status, String addInfo) {
        logger.info("Bus global connection envent: unitId={}, clientId={}, clientType={}, addInfo={}, status={}", unitId, clientId, clientType, addInfo, status);
//        if (Commander.callback == null)
//            return;
//        Commander.callback.globalConnect(unitId, clientId, clientType, ConnectStatus.valueOf((int) status), addInfo);
    }

    public void connect(byte localClientId, int accessPointUnitId, int errorCode) {
        if (errorCode == 0) {
            // 客户端链接成功
            logger.info("[{}:{}] connection succeed. AccessPointUnitId={}", Commander.getUnitId(), localClientId, accessPointUnitId);
        } else {
            // 客户端链接失败
            logger.error("[{}:{}] connection failed. ErrorCode={}", Commander.getUnitId(), localClientId, errorCode);
        }
    }

    public void disconnect(byte localClientId) {
        logger.error("[{}:{}] connection lost", Commander.getUnitId(), localClientId);
    }

    public void data(Head head, byte[] bytes) {
        logger.trace("data: head={}. bytes={}", head, bytes);
        Client client = Commander.clients.get(head.getDstClientId());
        if (client == null) {
            logger.warn("data: Can not find the client whose id is {}", head.getDstClientId());
            return;
        }
        client.dataExecutor.execute(() -> {
            ObjectMapper mapper = new ObjectMapper();
            Response resp = null;
            try {
                resp = mapper.readValue(bytes, Response.class);
            } catch (IOException e) {
                logger.error("RPC response JSON error", e);
                return;
            }
            Commander.outgoingRpcDone(resp);
        });
    }

    public void log(String msg, Boolean isErr) {
        if (isErr) {
            jniLogger.error(msg);
        } else {
            jniLogger.info(msg);
        }
    }

}