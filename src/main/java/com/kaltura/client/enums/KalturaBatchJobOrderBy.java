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
public enum KalturaBatchJobOrderBy implements KalturaEnumAsString {
    CHECK_AGAIN_TIMEOUT_ASC ("+checkAgainTimeout"),
    CREATED_AT_ASC ("+createdAt"),
    ESTIMATED_EFFORT_ASC ("+estimatedEffort"),
    EXECUTION_ATTEMPTS_ASC ("+executionAttempts"),
    FINISH_TIME_ASC ("+finishTime"),
    LOCK_EXPIRATION_ASC ("+lockExpiration"),
    LOCK_VERSION_ASC ("+lockVersion"),
    PRIORITY_ASC ("+priority"),
    QUEUE_TIME_ASC ("+queueTime"),
    STATUS_ASC ("+status"),
    UPDATED_AT_ASC ("+updatedAt"),
    CHECK_AGAIN_TIMEOUT_DESC ("-checkAgainTimeout"),
    CREATED_AT_DESC ("-createdAt"),
    ESTIMATED_EFFORT_DESC ("-estimatedEffort"),
    EXECUTION_ATTEMPTS_DESC ("-executionAttempts"),
    FINISH_TIME_DESC ("-finishTime"),
    LOCK_EXPIRATION_DESC ("-lockExpiration"),
    LOCK_VERSION_DESC ("-lockVersion"),
    PRIORITY_DESC ("-priority"),
    QUEUE_TIME_DESC ("-queueTime"),
    STATUS_DESC ("-status"),
    UPDATED_AT_DESC ("-updatedAt");

    public String hashCode;

    KalturaBatchJobOrderBy(String hashCode) {
        this.hashCode = hashCode;
    }

    public String getHashCode() {
        return this.hashCode;
    }

    public static KalturaBatchJobOrderBy get(String hashCode) {
        if (hashCode.equals("+checkAgainTimeout"))
        {
           return CHECK_AGAIN_TIMEOUT_ASC;
        }
        else 
        if (hashCode.equals("+createdAt"))
        {
           return CREATED_AT_ASC;
        }
        else 
        if (hashCode.equals("+estimatedEffort"))
        {
           return ESTIMATED_EFFORT_ASC;
        }
        else 
        if (hashCode.equals("+executionAttempts"))
        {
           return EXECUTION_ATTEMPTS_ASC;
        }
        else 
        if (hashCode.equals("+finishTime"))
        {
           return FINISH_TIME_ASC;
        }
        else 
        if (hashCode.equals("+lockExpiration"))
        {
           return LOCK_EXPIRATION_ASC;
        }
        else 
        if (hashCode.equals("+lockVersion"))
        {
           return LOCK_VERSION_ASC;
        }
        else 
        if (hashCode.equals("+priority"))
        {
           return PRIORITY_ASC;
        }
        else 
        if (hashCode.equals("+queueTime"))
        {
           return QUEUE_TIME_ASC;
        }
        else 
        if (hashCode.equals("+status"))
        {
           return STATUS_ASC;
        }
        else 
        if (hashCode.equals("+updatedAt"))
        {
           return UPDATED_AT_ASC;
        }
        else 
        if (hashCode.equals("-checkAgainTimeout"))
        {
           return CHECK_AGAIN_TIMEOUT_DESC;
        }
        else 
        if (hashCode.equals("-createdAt"))
        {
           return CREATED_AT_DESC;
        }
        else 
        if (hashCode.equals("-estimatedEffort"))
        {
           return ESTIMATED_EFFORT_DESC;
        }
        else 
        if (hashCode.equals("-executionAttempts"))
        {
           return EXECUTION_ATTEMPTS_DESC;
        }
        else 
        if (hashCode.equals("-finishTime"))
        {
           return FINISH_TIME_DESC;
        }
        else 
        if (hashCode.equals("-lockExpiration"))
        {
           return LOCK_EXPIRATION_DESC;
        }
        else 
        if (hashCode.equals("-lockVersion"))
        {
           return LOCK_VERSION_DESC;
        }
        else 
        if (hashCode.equals("-priority"))
        {
           return PRIORITY_DESC;
        }
        else 
        if (hashCode.equals("-queueTime"))
        {
           return QUEUE_TIME_DESC;
        }
        else 
        if (hashCode.equals("-status"))
        {
           return STATUS_DESC;
        }
        else 
        if (hashCode.equals("-updatedAt"))
        {
           return UPDATED_AT_DESC;
        }
        else 
        {
           return CHECK_AGAIN_TIMEOUT_ASC;
        }
    }
}
