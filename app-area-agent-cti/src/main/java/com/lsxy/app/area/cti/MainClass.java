package com.lsxy.app.area.cti;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A very simple sample program shows how to use the package.
 * <p>
 * Created by liuxy on 16-7-13.
 */
public class MainClass {
    private static final Logger logger = LoggerFactory.getLogger(MainClass.class);

    public static void main(String[] args) throws Exception {
        logger.debug("initiate...");
        Unit.initiate((byte) 24);
        logger.debug("initiate OK!");

        Monitor monitor = Unit.createMonitor(10, '127.0.0.1');

        String inputStr;
        Scanner scanner = new Scanner(System.in);
        System.out.printf("started! Input \"quit\" or \"q\" to quit.\n");
        while (true) {
            inputStr = scanner.nextLine();
            if (inputStr.toLowerCase().startsWith("q"))
                break;
        }
    }
}
