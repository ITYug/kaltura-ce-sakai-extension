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
package com.kaltura.client.services;

import com.kaltura.client.KalturaClient;
import com.kaltura.client.KalturaServiceBase;
import com.kaltura.client.types.*;
import org.w3c.dom.Element;
import com.kaltura.client.utils.ParseUtils;
import com.kaltura.client.KalturaParams;
import com.kaltura.client.KalturaApiException;

/**
 * This class was generated using generate.php
 * against an XML schema provided by Kaltura.
 * @date Wed, 23 Jan 13 11:47:33 -0500
 * 
 * MANUAL CHANGES TO THIS CLASS WILL BE OVERWRITTEN.
 */

/**  Manage access control profiles    */
public class KalturaAccessControlProfileService extends KalturaServiceBase {
    public KalturaAccessControlProfileService(KalturaClient client) {
        this.kalturaClient = client;
    }

	/**  Add new access control profile     */
    public KalturaAccessControlProfile add(KalturaAccessControlProfile accessControlProfile) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("accessControlProfile", accessControlProfile);
        this.kalturaClient.queueServiceCall("accesscontrolprofile", "add", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaAccessControlProfile.class, resultXmlElement);
    }

	/**  Get access control profile by id     */
    public KalturaAccessControlProfile get(int id) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        this.kalturaClient.queueServiceCall("accesscontrolprofile", "get", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaAccessControlProfile.class, resultXmlElement);
    }

	/**  Update access control profile by id     */
    public KalturaAccessControlProfile update(int id, KalturaAccessControlProfile accessControlProfile) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        kparams.add("accessControlProfile", accessControlProfile);
        this.kalturaClient.queueServiceCall("accesscontrolprofile", "update", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaAccessControlProfile.class, resultXmlElement);
    }

	/**  Delete access control profile by id     */
    public void delete(int id) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        this.kalturaClient.queueServiceCall("accesscontrolprofile", "delete", kparams);
        if (this.kalturaClient.isMultiRequest())
            return;
        this.kalturaClient.doQueue();
    }

    public KalturaAccessControlProfileListResponse list() throws KalturaApiException {
        return this.list(null);
    }

    public KalturaAccessControlProfileListResponse list(KalturaAccessControlProfileFilter filter) throws KalturaApiException {
        return this.list(filter, null);
    }

	/**  List access control profiles by filter and pager     */
    public KalturaAccessControlProfileListResponse list(KalturaAccessControlProfileFilter filter, KalturaFilterPager pager) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("filter", filter);
        kparams.add("pager", pager);
        this.kalturaClient.queueServiceCall("accesscontrolprofile", "list", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaAccessControlProfileListResponse.class, resultXmlElement);
    }
}
