package com.abstractedsheep.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: ujonnalagadda
 * Date: Jul 21, 2011
 * Time: 9:59:01 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractProperties {
    protected static final String STS_CONFIG_PREFIX = "sts";
    protected static final String DB_CONFIG_PREFIX = "db";

    public static void loadDBProperties(String path) throws IOException {
        Properties p = new Properties(System.getProperties());
        p.load(new FileInputStream(path));
        System.setProperties(p);
    }

    protected static class Property {
        private String name;
        private String defaultValue;

        public Property (String name, String val) {
            this.name = name;
            this.defaultValue = val;
        }

        public String toString() {
            String str = System.getProperty(name);
            return (str == null) ? defaultValue : str;
        }
    }
}
