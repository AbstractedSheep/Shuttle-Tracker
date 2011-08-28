package com.abstractedsheep.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;

public class DBProperties {
    public static final String user = new String((String) loadFromPoperties(
            "user", "usr"));
    public static String password = new String(
            (String) loadFromPoperties("password", ""));
    public static String shuttleTable = new String(
            (String) loadFromPoperties("shuttleTable", "shuttles"));
    public static String stopTable = new String(
            (String) loadFromPoperties("stopTable", "stops"));
    public static String routeTable = new String(
            (String) loadFromPoperties("routesTable", "routes"));
    public static long UPDATETIMEOUT = new Long((Long) loadFromPoperties(
            "timeout", 1000L * 45L));

    private static Object loadFromPoperties(String propertyName,
                                            Object defaultValue) {
        Properties props = new Properties();
        try {
            File f = new File("conf/db.properties");
            InputStream st = new FileInputStream(f);
            Object val = defaultValue;
            props.load(st);
            System.out.println(propertyName.toString() + " " + props.get(propertyName));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {

        }
        return defaultValue;
    }

    public void reloadMembers(String propsFilePath) {
        File f = new File(propsFilePath);
        for (Field fi : getClass().getDeclaredFields()) {
            System.out.println(fi.getName());
            try {
                System.out.println(fi.get(null));
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        (new DBProperties()).reloadMembers("");
    }
}
