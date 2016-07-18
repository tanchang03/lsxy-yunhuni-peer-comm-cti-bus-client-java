package com.lsxy.app.area.cti.commander;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.lsxy.app.area.cti.busnetcli.Head;

class LibCallbackHandler implements com.lsxy.app.area.cti.busnetcli.Callbacks {

    private final Logger logger = LoggerFactory.getLogger(LibCallbackHandler.class);
    private final Logger jniLogger = LoggerFactory.getLogger("bus_net_cli");

    public void globalConnect(byte unitId, byte clientId, byte clientType, byte status, String addInfo) {
        logger.info("Bus global connection event: unitId={}, clientId={}, clientType={}, addInfo={}, status={}", unitId, clientId, clientType, addInfo, status);
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
        logger.debug(">>> data: head={}. data-length={}", head, bytes.length);
        try {
            int clientId = (int) head.getDstClientId();
            Client client = Commander.clients.get(clientId);
            if (client == null) {
                logger.warn("data: Can not find the client<id={}>", clientId);
                return;
            }
            client.dataExecutor.execute(() -> {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Response resp = null;
                    resp = mapper.readValue(bytes, Response.class);
                    Commander.outgoingRpcDone(resp);
                } catch (com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException e) {
                    logger.warn("还没有实现的 JSON RPC");
                }
                catch (IOException e) {
                    logger.error("RPC response JSON error", e);
                }
            });
        } catch (Exception e) {
            logger.error("data: error: head={}, data={}", head, bytes, e);
            throw e;
        }
        logger.debug("<<< data");
    }

    public void log(String msg, Boolean isErr) {
        if (isErr) {
            jniLogger.error(msg);
        } else {
            jniLogger.info(msg);
        }
    }

}