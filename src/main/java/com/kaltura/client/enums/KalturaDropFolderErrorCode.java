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
package com.kaltura.client.enums;

/**
 * This class was generated using generate.php
 * against an XML schema provided by Kaltura.
 * @date Wed, 23 Jan 13 11:47:32 -0500
 * 
 * MANUAL CHANGES TO THIS CLASS WILL BE OVERWRITTEN.
 */
public enum KalturaDropFolderErrorCode implements KalturaEnumAsString {
    ERROR_CONNECT ("1"),
    ERROR_AUTENTICATE ("2"),
    ERROR_GET_PHISICAL_FILE_LIST ("3"),
    ERROR_GET_DB_FILE_LIST ("4"),
    DROP_FOLDER_APP_ERROR ("5"),
    CONTENT_MATCH_POLICY_UNDEFINED ("6");

    public String hashCode;

    KalturaDropFolderErrorCode(String hashCode) {
        this.hashCode = hashCode;
    }

    public String getHashCode() {
        return this.hashCode;
    }

    public static KalturaDropFolderErrorCode get(String hashCode) {
        if (hashCode.equals("1"))
        {
           return ERROR_CONNECT;
        }
        else 
        if (hashCode.equals("2"))
        {
           return ERROR_AUTENTICATE;
        }
        else 
        if (hashCode.equals("3"))
        {
           return ERROR_GET_PHISICAL_FILE_LIST;
        }
        else 
        if (hashCode.equals("4"))
        {
           return ERROR_GET_DB_FILE_LIST;
        }
        else 
        if (hashCode.equals("5"))
        {
           return DROP_FOLDER_APP_ERROR;
        }
        else 
        if (hashCode.equals("6"))
        {
           return CONTENT_MATCH_POLICY_UNDEFINED;
        }
        else 
        {
           return ERROR_CONNECT;
        }
    }
}
