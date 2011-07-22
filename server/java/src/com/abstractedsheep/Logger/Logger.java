package com.abstractedsheep.Logger;

import com.abstractedsheep.config.STSProperties;

import java.io.IOException;
import java.util.logging.FileHandler;

/**
 * Created by IntelliJ IDEA.
 * User: ujonnalagadda
 * Date: Jul 21, 2011
 * Time: 10:36:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class Logger extends java.util.logging.Logger{
    private java.util.logging.Logger log;

    protected Logger(String s, String s1) {
        super(s, s1);
    }

    public static Logger getConfiguredLogger(Class c) {
        String name = c.getCanonicalName();
        String pathToLogFile = STSProperties.LOG_PATH + "/sts.log";
        Logger log = (Logger) Logger.getLogger(name);

        try {
            log.addHandler(new FileHandler(pathToLogFile));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return log;
    }
}
