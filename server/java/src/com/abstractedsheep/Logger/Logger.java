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

package com.abstractedsheep.Logger;

import com.abstractedsheep.config.STSProperties;

import java.io.IOException;
import java.util.logging.FileHandler;

/**
 * Created by IntelliJ IDEA.
 * User: ujonnalagadda
 * Date: 7/22/11
 * Time: 4:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class Logger extends java.util.logging.Logger {
    protected Logger(String s, String s1) {
        super(s, s1);
    }

    public static Logger getConfiguredLogger(Class c) {
        Logger log = (Logger) getLogger(c.getCanonicalName());
        String path = STSProperties.LOG_PATH + "/sts.log";

        try {
            log.addHandler(new FileHandler(path));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return log;
    }
}
