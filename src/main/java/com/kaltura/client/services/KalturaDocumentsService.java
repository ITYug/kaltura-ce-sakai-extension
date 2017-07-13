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
import java.util.ArrayList;
import java.io.File;
import com.kaltura.client.KalturaFiles;

/**
 * This class was generated using generate.php
 * against an XML schema provided by Kaltura.
 * @date Wed, 23 Jan 13 11:47:33 -0500
 * 
 * MANUAL CHANGES TO THIS CLASS WILL BE OVERWRITTEN.
 */

/**  Document service lets you upload and manage document files    */
public class KalturaDocumentsService extends KalturaServiceBase {
    public KalturaDocumentsService(KalturaClient client) {
        this.kalturaClient = client;
    }

	/**  Add new document entry after the specific document file was uploaded and the
	  upload token id exists     */
    public KalturaDocumentEntry addFromUploadedFile(KalturaDocumentEntry documentEntry, String uploadTokenId) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("documentEntry", documentEntry);
        kparams.add("uploadTokenId", uploadTokenId);
        this.kalturaClient.queueServiceCall("document_documents", "addFromUploadedFile", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaDocumentEntry.class, resultXmlElement);
    }

    public KalturaDocumentEntry addFromEntry(String sourceEntryId) throws KalturaApiException {
        return this.addFromEntry(sourceEntryId, null);
    }

    public KalturaDocumentEntry addFromEntry(String sourceEntryId, KalturaDocumentEntry documentEntry) throws KalturaApiException {
        return this.addFromEntry(sourceEntryId, documentEntry, Integer.MIN_VALUE);
    }

	/**  Copy entry into new entry     */
    public KalturaDocumentEntry addFromEntry(String sourceEntryId, KalturaDocumentEntry documentEntry, int sourceFlavorParamsId) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("sourceEntryId", sourceEntryId);
        kparams.add("documentEntry", documentEntry);
        kparams.add("sourceFlavorParamsId", sourceFlavorParamsId);
        this.kalturaClient.queueServiceCall("document_documents", "addFromEntry", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaDocumentEntry.class, resultXmlElement);
    }

    public KalturaDocumentEntry addFromFlavorAsset(String sourceFlavorAssetId) throws KalturaApiException {
        return this.addFromFlavorAsset(sourceFlavorAssetId, null);
    }

	/**  Copy flavor asset into new entry     */
    public KalturaDocumentEntry addFromFlavorAsset(String sourceFlavorAssetId, KalturaDocumentEntry documentEntry) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("sourceFlavorAssetId", sourceFlavorAssetId);
        kparams.add("documentEntry", documentEntry);
        this.kalturaClient.queueServiceCall("document_documents", "addFromFlavorAsset", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaDocumentEntry.class, resultXmlElement);
    }

    public int convert(String entryId) throws KalturaApiException {
        return this.convert(entryId, Integer.MIN_VALUE);
    }

    public int convert(String entryId, int conversionProfileId) throws KalturaApiException {
        return this.convert(entryId, conversionProfileId, null);
    }

	/**  Convert entry     */
    public int convert(String entryId, int conversionProfileId, ArrayList<KalturaConversionAttribute> dynamicConversionAttributes) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        kparams.add("conversionProfileId", conversionProfileId);
        kparams.add("dynamicConversionAttributes", dynamicConversionAttributes);
        this.kalturaClient.queueServiceCall("document_documents", "convert", kparams);
        if (this.kalturaClient.isMultiRequest())
            return 0;
        Element resultXmlElement = this.kalturaClient.doQueue();
        String resultText = resultXmlElement.getTextContent();
        return ParseUtils.parseInt(resultText);
    }

    public KalturaDocumentEntry get(String entryId) throws KalturaApiException {
        return this.get(entryId, -1);
    }

	/**  Get document entry by ID.     */
    public KalturaDocumentEntry get(String entryId, int version) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        kparams.add("version", version);
        this.kalturaClient.queueServiceCall("document_documents", "get", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaDocumentEntry.class, resultXmlElement);
    }

	/**  Update document entry. Only the properties that were set will be updated.     */
    public KalturaDocumentEntry update(String entryId, KalturaDocumentEntry documentEntry) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        kparams.add("documentEntry", documentEntry);
        this.kalturaClient.queueServiceCall("document_documents", "update", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaDocumentEntry.class, resultXmlElement);
    }

	/**  Delete a document entry.     */
    public void delete(String entryId) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        this.kalturaClient.queueServiceCall("document_documents", "delete", kparams);
        if (this.kalturaClient.isMultiRequest())
            return;
        this.kalturaClient.doQueue();
    }

    public KalturaDocumentListResponse list() throws KalturaApiException {
        return this.list(null);
    }

    public KalturaDocumentListResponse list(KalturaDocumentEntryFilter filter) throws KalturaApiException {
        return this.list(filter, null);
    }

	/**  List document entries by filter with paging support.     */
    public KalturaDocumentListResponse list(KalturaDocumentEntryFilter filter, KalturaFilterPager pager) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("filter", filter);
        kparams.add("pager", pager);
        this.kalturaClient.queueServiceCall("document_documents", "list", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaDocumentListResponse.class, resultXmlElement);
    }

	/**  Upload a document file to Kaltura, then the file can be used to create a
	  document entry.      */
    public String upload(File fileData) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        KalturaFiles kfiles = new KalturaFiles();
        kfiles.put("fileData", new KalturaFile(fileData));
        this.kalturaClient.queueServiceCall("document_documents", "upload", kparams, kfiles);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        String resultText = resultXmlElement.getTextContent();
        return ParseUtils.parseString(resultText);
    }

	/**  This will queue a batch job for converting the document file to swf   Returns
	  the URL where the new swf will be available      */
    public String convertPptToSwf(String entryId) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        this.kalturaClient.queueServiceCall("document_documents", "convertPptToSwf", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        String resultText = resultXmlElement.getTextContent();
        return ParseUtils.parseString(resultText);
    }

    public String serve(String entryId) throws KalturaApiException {
        return this.serve(entryId, null);
    }

    public String serve(String entryId, String flavorAssetId) throws KalturaApiException {
        return this.serve(entryId, flavorAssetId, false);
    }

	/**  Serves the file content     */
    public String serve(String entryId, String flavorAssetId, boolean forceProxy) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        kparams.add("flavorAssetId", flavorAssetId);
        kparams.add("forceProxy", forceProxy);
        this.kalturaClient.queueServiceCall("document_documents", "serve", kparams);
        return this.kalturaClient.serve();
    }

    public String serveByFlavorParamsId(String entryId) throws KalturaApiException {
        return this.serveByFlavorParamsId(entryId, null);
    }

    public String serveByFlavorParamsId(String entryId, String flavorParamsId) throws KalturaApiException {
        return this.serveByFlavorParamsId(entryId, flavorParamsId, false);
    }

	/**  Serves the file content     */
    public String serveByFlavorParamsId(String entryId, String flavorParamsId, boolean forceProxy) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        kparams.add("flavorParamsId", flavorParamsId);
        kparams.add("forceProxy", forceProxy);
        this.kalturaClient.queueServiceCall("document_documents", "serveByFlavorParamsId", kparams);
        return this.kalturaClient.serve();
    }
}
