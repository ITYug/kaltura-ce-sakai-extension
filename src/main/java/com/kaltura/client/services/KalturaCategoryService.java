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
import com.kaltura.client.KalturaFile;
import com.kaltura.client.KalturaServiceBase;
import com.kaltura.client.types.*;
import org.w3c.dom.Element;
import com.kaltura.client.utils.ParseUtils;
import com.kaltura.client.KalturaParams;
import com.kaltura.client.KalturaApiException;
import com.kaltura.client.enums.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import com.kaltura.client.KalturaFiles;
import com.kaltura.client.KalturaFile;

/**
 * This class was generated using generate.php
 * against an XML schema provided by Kaltura.
 * @date Wed, 23 Jan 13 11:47:33 -0500
 * 
 * MANUAL CHANGES TO THIS CLASS WILL BE OVERWRITTEN.
 */

/**  Add & Manage Categories    */
public class KalturaCategoryService extends KalturaServiceBase {
    public KalturaCategoryService(KalturaClient client) {
        this.kalturaClient = client;
    }

	/**  Add new Category     */
    public KalturaCategory add(KalturaCategory category) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("category", category);
        this.kalturaClient.queueServiceCall("category", "add", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaCategory.class, resultXmlElement);
    }

	/**  Get Category by id     */
    public KalturaCategory get(int id) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        this.kalturaClient.queueServiceCall("category", "get", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaCategory.class, resultXmlElement);
    }

	/**  Update Category     */
    public KalturaCategory update(int id, KalturaCategory category) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        kparams.add("category", category);
        this.kalturaClient.queueServiceCall("category", "update", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaCategory.class, resultXmlElement);
    }

    public void delete(int id) throws KalturaApiException {
        this.delete(id, KalturaNullableBoolean.get(1));
    }

	/**  Delete a Category     */
    public void delete(int id, KalturaNullableBoolean moveEntriesToParentCategory) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        kparams.add("moveEntriesToParentCategory", moveEntriesToParentCategory);
        this.kalturaClient.queueServiceCall("category", "delete", kparams);
        if (this.kalturaClient.isMultiRequest())
            return;
        this.kalturaClient.doQueue();
    }

    public KalturaCategoryListResponse list() throws KalturaApiException {
        return this.list(null);
    }

    public KalturaCategoryListResponse list(KalturaCategoryFilter filter) throws KalturaApiException {
        return this.list(filter, null);
    }

	/**  List all categories     */
    public KalturaCategoryListResponse list(KalturaCategoryFilter filter, KalturaFilterPager pager) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("filter", filter);
        kparams.add("pager", pager);
        this.kalturaClient.queueServiceCall("category", "list", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaCategoryListResponse.class, resultXmlElement);
    }

    public int index(int id) throws KalturaApiException {
        return this.index(id, true);
    }

	/**  Index Category by id     */
    public int index(int id, boolean shouldUpdate) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        kparams.add("shouldUpdate", shouldUpdate);
        this.kalturaClient.queueServiceCall("category", "index", kparams);
        if (this.kalturaClient.isMultiRequest())
            return 0;
        Element resultXmlElement = this.kalturaClient.doQueue();
        String resultText = resultXmlElement.getTextContent();
        return ParseUtils.parseInt(resultText);
    }

	/**  Move categories that belong to the same parent category to a target categroy -
	  enabled only for ks with disable entitlement     */
    public KalturaCategoryListResponse move(String categoryIds, int targetCategoryParentId) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("categoryIds", categoryIds);
        kparams.add("targetCategoryParentId", targetCategoryParentId);
        this.kalturaClient.queueServiceCall("category", "move", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaCategoryListResponse.class, resultXmlElement);
    }

	/**  Unlock categories     */
    public void unlockCategories() throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        this.kalturaClient.queueServiceCall("category", "unlockCategories", kparams);
        if (this.kalturaClient.isMultiRequest())
            return;
        this.kalturaClient.doQueue();
    }

    public KalturaBulkUpload addFromBulkUpload(File fileData) throws KalturaApiException {
        return this.addFromBulkUpload(fileData, null);
    }

    public KalturaBulkUpload addFromBulkUpload(File fileData, KalturaBulkUploadJobData bulkUploadData) throws KalturaApiException {
        return this.addFromBulkUpload(fileData, bulkUploadData, null);
    }

    public KalturaBulkUpload addFromBulkUpload(File fileData, KalturaBulkUploadJobData bulkUploadData, KalturaBulkUploadCategoryData bulkUploadCategoryData) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        KalturaFiles kfiles = new KalturaFiles();
        kfiles.put("fileData", new KalturaFile(fileData));
        kparams.add("bulkUploadData", bulkUploadData);
        kparams.add("bulkUploadCategoryData", bulkUploadCategoryData);
        this.kalturaClient.queueServiceCall("category", "addFromBulkUpload", kparams, kfiles);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaBulkUpload.class, resultXmlElement);
    }
}
