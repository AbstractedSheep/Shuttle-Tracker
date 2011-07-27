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

/**
 * Created by IntelliJ IDEA.
 * User: ujonnalagadda
 * Date: Jul 21, 2011
 * Time: 6:32:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class STSProperties extends AbstractProperties {
    public static final Property LOG_PATH = new Property("logPath", "../logs");

    public static final Property DB_PATH = new Property("dbPath",
            "/Users/ujonnalagadda/Shuttle-Tracker/server/java/conf/db.properties");
}
