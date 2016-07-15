package com.lsxy.app.area.cti;

import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lsxy.app.area.cti.commander.Client;
import com.lsxy.app.area.cti.commander.Commander;
import com.lsxy.app.area.cti.commander.ResponseReceiver;
import com.lsxy.app.area.cti.commander.CreationResponseReceiver;
import com.lsxy.app.area.cti.commander.ResponseError;

/**
 * Created by liuxy on 16-7-13.
 */
public class MainClass {
    static final Logger logger = LoggerFactory.getLogger(MainClass.class);

    public static void main(String[] args) throws Exception {
        logger.debug("initiate...");
        Commander.initiate(20);
        logger.debug("initiate OK!");
        Client client = Commander.createClient((byte) 0, (byte) 20, "192.168.2.100");
        logger.debug("client: {}", client);
        //Pause for 1 seconds
        logger.debug("sleep...");
        Thread.sleep(1000);
        logger.debug("sleep end.");
        logger.debug("deliverCreation...");
        Map<String, Object> params = new HashMap<String, Object>();
        client.deliverCreation((byte) 1, (byte) 0, "sys.call", params, new ResponseReceiver() {
            @Override
            protected void onReceive(Object result) {
                logger.debug("onReceive(result={})", result);
                String resId = (String) result;
                logger.debug("onReceive: ResourceID={}", result);
            }

            @Override
            protected void onError(ResponseError error) {
                logger.error("onError:{}", error);

            }

            @Override
            protected void onTimeout() {
                logger.error("onTimeout");
            }
        });

        String inputStr = null;
        Scanner scanner = new Scanner(System.in);
        System.out.printf("started! Input \"quit\" or \"q\" to quit.\n");
        while (true) {
            inputStr = scanner.nextLine();
            if (inputStr.toLowerCase().startsWith("q"))
                break;
        }
    }
}
