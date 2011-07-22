package com.abstractedsheep.config;

/**
 * Created by IntelliJ IDEA.
 * User: ujonnalagadda
 * Date: Jul 21, 2011
 * Time: 6:32:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class STSProperties extends AbstractProperties{
    public static final Property LOG_PATH = new Property("logPath", "../logs");

    public static final Property DB_PATH = new Property("dbPath", "./db.properties");
}
