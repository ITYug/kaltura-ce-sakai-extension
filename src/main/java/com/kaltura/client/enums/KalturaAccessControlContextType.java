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
public enum KalturaAccessControlContextType implements KalturaEnumAsString {
    PLAY ("1"),
    DOWNLOAD ("2"),
    THUMBNAIL ("3"),
    METADATA ("4");

    public String hashCode;

    KalturaAccessControlContextType(String hashCode) {
        this.hashCode = hashCode;
    }

    public String getHashCode() {
        return this.hashCode;
    }

    public static KalturaAccessControlContextType get(String hashCode) {
        if (hashCode.equals("1"))
        {
           return PLAY;
        }
        else 
        if (hashCode.equals("2"))
        {
           return DOWNLOAD;
        }
        else 
        if (hashCode.equals("3"))
        {
           return THUMBNAIL;
        }
        else 
        if (hashCode.equals("4"))
        {
           return METADATA;
        }
        else 
        {
           return PLAY;
        }
    }
}
