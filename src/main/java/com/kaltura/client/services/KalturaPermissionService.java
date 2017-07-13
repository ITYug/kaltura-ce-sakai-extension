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

/**  Permission service lets you create and manage user permissions    */
public class KalturaPermissionService extends KalturaServiceBase {
    public KalturaPermissionService(KalturaClient client) {
        this.kalturaClient = client;
    }

	/**  Adds a new permission object to the account.     */
    public KalturaPermission add(KalturaPermission permission) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("permission", permission);
        this.kalturaClient.queueServiceCall("permission", "add", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaPermission.class, resultXmlElement);
    }

	/**  Retrieves a permission object using its ID.     */
    public KalturaPermission get(String permissionName) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("permissionName", permissionName);
        this.kalturaClient.queueServiceCall("permission", "get", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaPermission.class, resultXmlElement);
    }

	/**  Updates an existing permission object.     */
    public KalturaPermission update(String permissionName, KalturaPermission permission) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("permissionName", permissionName);
        kparams.add("permission", permission);
        this.kalturaClient.queueServiceCall("permission", "update", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaPermission.class, resultXmlElement);
    }

	/**  Deletes an existing permission object.     */
    public KalturaPermission delete(String permissionName) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("permissionName", permissionName);
        this.kalturaClient.queueServiceCall("permission", "delete", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaPermission.class, resultXmlElement);
    }

    public KalturaPermissionListResponse list() throws KalturaApiException {
        return this.list(null);
    }

    public KalturaPermissionListResponse list(KalturaPermissionFilter filter) throws KalturaApiException {
        return this.list(filter, null);
    }

	/**  Lists permission objects that are associated with an account.   Blocked
	  permissions are listed unless you use a filter to exclude them.   Blocked
	  permissions are listed unless you use a filter to exclude them.     */
    public KalturaPermissionListResponse list(KalturaPermissionFilter filter, KalturaFilterPager pager) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("filter", filter);
        kparams.add("pager", pager);
        this.kalturaClient.queueServiceCall("permission", "list", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaPermissionListResponse.class, resultXmlElement);
    }

	/**  Retrieves a list of permissions that apply to the current KS.     */
    public String getCurrentPermissions() throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        this.kalturaClient.queueServiceCall("permission", "getCurrentPermissions", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        String resultText = resultXmlElement.getTextContent();
        return ParseUtils.parseString(resultText);
    }
}
