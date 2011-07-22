/*
 * Copyright 2011
 *
 *   This file is part of Mobile Shuttle Tracker.
 *
 *   Mobile Shuttle Tracker is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Mobile Shuttle Tracker is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Mobile Shuttle Tracker.  If not, see <http://www.gnu.org/licenses/>.
 */

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
