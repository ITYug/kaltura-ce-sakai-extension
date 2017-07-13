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
import com.kaltura.client.KalturaFiles;
import java.io.File;
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

/**  Bulk upload service is used to upload & manage bulk uploads using CSV files. 
  This service manages only entry bulk uploads.    */
public class KalturaBulkUploadService extends KalturaServiceBase {
    public KalturaBulkUploadService(KalturaClient client) {
        this.kalturaClient = client;
    }

	/**  Add new bulk upload batch job   Conversion profile id can be specified in the
	  API or in the CSV file, the one in the CSV file will be stronger.   If no
	  conversion profile was specified, partner's default will be used     */
    public KalturaBulkUpload add(int conversionProfileId, File csvFileData) throws KalturaApiException {
        return this.add(conversionProfileId, csvFileData, null);
    }

	/**  Add new bulk upload batch job   Conversion profile id can be specified in the
	  API or in the CSV file, the one in the CSV file will be stronger.   If no
	  conversion profile was specified, partner's default will be used     */
    public KalturaBulkUpload add(int conversionProfileId, File csvFileData, String bulkUploadType) throws KalturaApiException {
        return this.add(conversionProfileId, csvFileData, bulkUploadType, null);
    }

	/**  Add new bulk upload batch job   Conversion profile id can be specified in the
	  API or in the CSV file, the one in the CSV file will be stronger.   If no
	  conversion profile was specified, partner's default will be used     */
    public KalturaBulkUpload add(int conversionProfileId, File csvFileData, String bulkUploadType, String uploadedBy) throws KalturaApiException {
        return this.add(conversionProfileId, csvFileData, bulkUploadType, uploadedBy, null);
    }

	/**  Add new bulk upload batch job   Conversion profile id can be specified in the
	  API or in the CSV file, the one in the CSV file will be stronger.   If no
	  conversion profile was specified, partner's default will be used     */
    public KalturaBulkUpload add(int conversionProfileId, File csvFileData, String bulkUploadType, String uploadedBy, String fileName) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("conversionProfileId", conversionProfileId);
        KalturaFiles kfiles = new KalturaFiles();
        kfiles.put("csvFileData", new KalturaFile(csvFileData));
        kparams.add("bulkUploadType", bulkUploadType);
        kparams.add("uploadedBy", uploadedBy);
        kparams.add("fileName", fileName);
        this.kalturaClient.queueServiceCall("bulkupload", "add", kparams, kfiles);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaBulkUpload.class, resultXmlElement);
    }

	/**  Get bulk upload batch job by id     */
    public KalturaBulkUpload get(int id) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        this.kalturaClient.queueServiceCall("bulkupload", "get", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaBulkUpload.class, resultXmlElement);
    }

    public KalturaBulkUploadListResponse list() throws KalturaApiException {
        return this.list(null);
    }

	/**  List bulk upload batch jobs     */
    public KalturaBulkUploadListResponse list(KalturaFilterPager pager) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("pager", pager);
        this.kalturaClient.queueServiceCall("bulkupload", "list", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaBulkUploadListResponse.class, resultXmlElement);
    }

	/**  serve action returan the original file.     */
    public String serve(int id) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        this.kalturaClient.queueServiceCall("bulkupload", "serve", kparams);
        return this.kalturaClient.serve();
    }

	/**  serveLog action returan the original file.     */
    public String serveLog(int id) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        this.kalturaClient.queueServiceCall("bulkupload", "serveLog", kparams);
        return this.kalturaClient.serve();
    }

	/**  Aborts the bulk upload and all its child jobs     */
    public KalturaBulkUpload abort(int id) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        this.kalturaClient.queueServiceCall("bulkupload", "abort", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaBulkUpload.class, resultXmlElement);
    }
}
