// ===================================================================================================
//                           _  __     _ _
//                          | |/ /__ _| | |_ _  _ _ _ __ _
//                          | ' </ _` | |  _| || | '_/ _` |
//                          |_|\_\__,_|_|\__|\_,_|_| \__,_|
//
// This file is part of the Kaltura Collaborative Media Suite which allows users
// to do with audio, video, and animation what Wiki platfroms allow them to do with
// text.
//
// Copyright (C) 2006-2011  Kaltura Inc.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// @ignore
// ===================================================================================================

package com.kaltura.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Logging class to allow commons logging
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
public class KalturaLoggerCommons extends KalturaLogger {
    Log log;

    public static KalturaLogger getLogger(String name) {
        return new KalturaLoggerCommons(name);
    }

    protected KalturaLoggerCommons(String name) {
        log = LogFactory.getLog(name);
    }

    public boolean isEnabled() {
        return true;
    }

    // printing methods:
    public void trace(Object message) {
        log.trace(message);
    }

    public void debug(Object message) {
        log.debug(message);
    }

    public void info(Object message) {
        log.info(message);
    }

    public void warn(Object message) {
        log.warn(message);
    }

    public void error(Object message) {
        log.error(message);
    }

    public void fatal(Object message) {
        log.fatal(message);
    }

    public void trace(Object message, Throwable t) {
        log.trace(message, t);
    }

    public void debug(Object message, Throwable t) {
        log.debug(message, t);
    }

    public void info(Object message, Throwable t) {
        log.info(message, t);
    }

    public void warn(Object message, Throwable t) {
        log.warn(message, t);
    }

    public void error(Object message, Throwable t) {
        log.error(message, t);
    }

    public void fatal(Object message, Throwable t) {
        log.fatal(message, t);
    }
}
