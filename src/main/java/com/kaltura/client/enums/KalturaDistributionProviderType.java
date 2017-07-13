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
public enum KalturaDistributionProviderType implements KalturaEnumAsString {
    ATT_UVERSE ("attUverseDistribution.ATT_UVERSE"),
    CROSS_KALTURA ("crossKalturaDistribution.CROSS_KALTURA"),
    HULU ("huluDistribution.HULU"),
    IDETIC ("ideticDistribution.IDETIC"),
    UVERSE ("uverseDistribution.UVERSE"),
    YAHOO ("yahooDistribution.YAHOO"),
    YOUTUBE_API ("youtubeApiDistribution.YOUTUBE_API"),
    GENERIC ("1"),
    SYNDICATION ("2");

    public String hashCode;

    KalturaDistributionProviderType(String hashCode) {
        this.hashCode = hashCode;
    }

    public String getHashCode() {
        return this.hashCode;
    }

    public static KalturaDistributionProviderType get(String hashCode) {
        if (hashCode.equals("attUverseDistribution.ATT_UVERSE"))
        {
           return ATT_UVERSE;
        }
        else 
        if (hashCode.equals("crossKalturaDistribution.CROSS_KALTURA"))
        {
           return CROSS_KALTURA;
        }
        else 
        if (hashCode.equals("huluDistribution.HULU"))
        {
           return HULU;
        }
        else 
        if (hashCode.equals("ideticDistribution.IDETIC"))
        {
           return IDETIC;
        }
        else 
        if (hashCode.equals("uverseDistribution.UVERSE"))
        {
           return UVERSE;
        }
        else 
        if (hashCode.equals("yahooDistribution.YAHOO"))
        {
           return YAHOO;
        }
        else 
        if (hashCode.equals("youtubeApiDistribution.YOUTUBE_API"))
        {
           return YOUTUBE_API;
        }
        else 
        if (hashCode.equals("1"))
        {
           return GENERIC;
        }
        else 
        if (hashCode.equals("2"))
        {
           return SYNDICATION;
        }
        else 
        {
           return ATT_UVERSE;
        }
    }
}
