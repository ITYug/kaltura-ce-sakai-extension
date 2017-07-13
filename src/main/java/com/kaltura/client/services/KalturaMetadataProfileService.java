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

import com.kaltura.client.KalturaFile;
import com.kaltura.client.KalturaClient;
import com.kaltura.client.KalturaServiceBase;
import com.kaltura.client.types.*;
import org.w3c.dom.Element;
import com.kaltura.client.utils.ParseUtils;
import com.kaltura.client.KalturaParams;
import com.kaltura.client.KalturaApiException;
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

/**  Metadata Profile service    */
public class KalturaMetadataProfileService extends KalturaServiceBase {
    public KalturaMetadataProfileService(KalturaClient client) {
        this.kalturaClient = client;
    }

    public KalturaMetadataProfile add(KalturaMetadataProfile metadataProfile, String xsdData) throws KalturaApiException {
        return this.add(metadataProfile, xsdData, null);
    }

	/**  Allows you to add a metadata profile object and metadata profile content
	  associated with Kaltura object type     */
    public KalturaMetadataProfile add(KalturaMetadataProfile metadataProfile, String xsdData, String viewsData) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("metadataProfile", metadataProfile);
        kparams.add("xsdData", xsdData);
        kparams.add("viewsData", viewsData);
        this.kalturaClient.queueServiceCall("metadata_metadataprofile", "add", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaMetadataProfile.class, resultXmlElement);
    }

	/**  Allows you to add a metadata profile object and metadata profile file associated
	  with Kaltura object type     */
    public KalturaMetadataProfile addFromFile(KalturaMetadataProfile metadataProfile, File xsdFile) throws KalturaApiException {
        return this.addFromFile(metadataProfile, xsdFile, null);
    }

	/**  Allows you to add a metadata profile object and metadata profile file associated
	  with Kaltura object type     */
    public KalturaMetadataProfile addFromFile(KalturaMetadataProfile metadataProfile, File xsdFile, File viewsFile) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("metadataProfile", metadataProfile);
        KalturaFiles kfiles = new KalturaFiles();
        kfiles.put("xsdFile", new KalturaFile(xsdFile));
        kfiles.put("viewsFile", new KalturaFile(viewsFile));
        this.kalturaClient.queueServiceCall("metadata_metadataprofile", "addFromFile", kparams, kfiles);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaMetadataProfile.class, resultXmlElement);
    }

	/**  Retrieve a metadata profile object by id     */
    public KalturaMetadataProfile get(int id) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        this.kalturaClient.queueServiceCall("metadata_metadataprofile", "get", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaMetadataProfile.class, resultXmlElement);
    }

    public KalturaMetadataProfile update(int id, KalturaMetadataProfile metadataProfile) throws KalturaApiException {
        return this.update(id, metadataProfile, null);
    }

    public KalturaMetadataProfile update(int id, KalturaMetadataProfile metadataProfile, String xsdData) throws KalturaApiException {
        return this.update(id, metadataProfile, xsdData, null);
    }

	/**  Update an existing metadata object     */
    public KalturaMetadataProfile update(int id, KalturaMetadataProfile metadataProfile, String xsdData, String viewsData) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        kparams.add("metadataProfile", metadataProfile);
        kparams.add("xsdData", xsdData);
        kparams.add("viewsData", viewsData);
        this.kalturaClient.queueServiceCall("metadata_metadataprofile", "update", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaMetadataProfile.class, resultXmlElement);
    }

    public KalturaMetadataProfileListResponse list() throws KalturaApiException {
        return this.list(null);
    }

    public KalturaMetadataProfileListResponse list(KalturaMetadataProfileFilter filter) throws KalturaApiException {
        return this.list(filter, null);
    }

	/**  List metadata profile objects by filter and pager     */
    public KalturaMetadataProfileListResponse list(KalturaMetadataProfileFilter filter, KalturaFilterPager pager) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("filter", filter);
        kparams.add("pager", pager);
        this.kalturaClient.queueServiceCall("metadata_metadataprofile", "list", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaMetadataProfileListResponse.class, resultXmlElement);
    }

	/**  List metadata profile fields by metadata profile id     */
    public KalturaMetadataProfileFieldListResponse listFields(int metadataProfileId) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("metadataProfileId", metadataProfileId);
        this.kalturaClient.queueServiceCall("metadata_metadataprofile", "listFields", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaMetadataProfileFieldListResponse.class, resultXmlElement);
    }

	/**  Delete an existing metadata profile     */
    public void delete(int id) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        this.kalturaClient.queueServiceCall("metadata_metadataprofile", "delete", kparams);
        if (this.kalturaClient.isMultiRequest())
            return;
        this.kalturaClient.doQueue();
    }

	/**  Update an existing metadata object definition file     */
    public KalturaMetadataProfile revert(int id, int toVersion) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        kparams.add("toVersion", toVersion);
        this.kalturaClient.queueServiceCall("metadata_metadataprofile", "revert", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaMetadataProfile.class, resultXmlElement);
    }

	/**  Update an existing metadata object definition file     */
    public KalturaMetadataProfile updateDefinitionFromFile(int id, File xsdFile) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        KalturaFiles kfiles = new KalturaFiles();
        kfiles.put("xsdFile", new KalturaFile(xsdFile));
        this.kalturaClient.queueServiceCall("metadata_metadataprofile", "updateDefinitionFromFile", kparams, kfiles);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaMetadataProfile.class, resultXmlElement);
    }

	/**  Update an existing metadata object views file     */
    public KalturaMetadataProfile updateViewsFromFile(int id, File viewsFile) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        KalturaFiles kfiles = new KalturaFiles();
        kfiles.put("viewsFile", new KalturaFile(viewsFile));
        this.kalturaClient.queueServiceCall("metadata_metadataprofile", "updateViewsFromFile", kparams, kfiles);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaMetadataProfile.class, resultXmlElement);
    }

	/**  Update an existing metadata object xslt file     */
    public KalturaMetadataProfile updateTransformationFromFile(int id, File xsltFile) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        KalturaFiles kfiles = new KalturaFiles();
        kfiles.put("xsltFile", new KalturaFile(xsltFile));
        this.kalturaClient.queueServiceCall("metadata_metadataprofile", "updateTransformationFromFile", kparams, kfiles);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaMetadataProfile.class, resultXmlElement);
    }

	/**  Serves metadata profile XSD file     */
    public String serve(int id) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        this.kalturaClient.queueServiceCall("metadata_metadataprofile", "serve", kparams);
        return this.kalturaClient.serve();
    }

	/**  Serves metadata profile view file     */
    public String serveView(int id) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        this.kalturaClient.queueServiceCall("metadata_metadataprofile", "serveView", kparams);
        return this.kalturaClient.serve();
    }
}
