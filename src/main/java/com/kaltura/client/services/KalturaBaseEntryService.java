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
import com.kaltura.client.enums.KalturaEntryType;
import com.kaltura.client.types.*;
import org.w3c.dom.Element;
import com.kaltura.client.utils.ParseUtils;
import com.kaltura.client.KalturaParams;
import com.kaltura.client.KalturaApiException;
import java.util.List;
import com.kaltura.client.utils.XmlUtils;
import com.kaltura.client.KalturaFiles;
import java.io.File;

/**
 * This class was generated using generate.php
 * against an XML schema provided by Kaltura.
 * @date Tue, 31 Jul 12 11:52:02 -0400
 * 
 * MANUAL CHANGES TO THIS CLASS WILL BE OVERWRITTEN.
 */

/**  Base Entry Service    */
public class KalturaBaseEntryService extends KalturaServiceBase {
    public KalturaBaseEntryService(KalturaClient client) {
        this.kalturaClient = client;
    }

    public KalturaBaseEntry add(KalturaBaseEntry entry) throws KalturaApiException {
        return this.add(entry, null);
    }

	/**  Generic add entry, should be used when the uploaded entry type is not known.    
	     */
    public KalturaBaseEntry add(KalturaBaseEntry entry, KalturaEntryType type) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entry", entry);
        kparams.add("type", type);
        this.kalturaClient.queueServiceCall("baseentry", "add", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaBaseEntry.class, resultXmlElement);
    }

	/**  Attach content resource to entry in status NO_MEDIA        */
    public KalturaBaseEntry addContent(String entryId, KalturaResource resource) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        kparams.add("resource", resource);
        this.kalturaClient.queueServiceCall("baseentry", "addContent", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaBaseEntry.class, resultXmlElement);
    }

    public KalturaBaseEntry addFromUploadedFile(KalturaBaseEntry entry, String uploadTokenId) throws KalturaApiException {
        return this.addFromUploadedFile(entry, uploadTokenId, null);
    }

	/**  Generic add entry using an uploaded file, should be used when the uploaded entry
	  type is not known.        */
    public KalturaBaseEntry addFromUploadedFile(KalturaBaseEntry entry, String uploadTokenId, KalturaEntryType type) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entry", entry);
        kparams.add("uploadTokenId", uploadTokenId);
        kparams.add("type", type);
        this.kalturaClient.queueServiceCall("baseentry", "addFromUploadedFile", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaBaseEntry.class, resultXmlElement);
    }

    public KalturaBaseEntry get(String entryId) throws KalturaApiException {
        return this.get(entryId, -1);
    }

	/**  Get base entry by ID.     */
    public KalturaBaseEntry get(String entryId, int version) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        kparams.add("version", version);
        this.kalturaClient.queueServiceCall("baseentry", "get", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaBaseEntry.class, resultXmlElement);
    }

	/**  Get remote storage existing paths for the asset.        */
    public KalturaRemotePathListResponse getRemotePaths(String entryId) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        this.kalturaClient.queueServiceCall("baseentry", "getRemotePaths", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaRemotePathListResponse.class, resultXmlElement);
    }

	/**  Update base entry. Only the properties that were set will be updated.     */
    public KalturaBaseEntry update(String entryId, KalturaBaseEntry baseEntry) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        kparams.add("baseEntry", baseEntry);
        this.kalturaClient.queueServiceCall("baseentry", "update", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaBaseEntry.class, resultXmlElement);
    }

    public KalturaBaseEntry updateContent(String entryId, KalturaResource resource) throws KalturaApiException {
        return this.updateContent(entryId, resource, Integer.MIN_VALUE);
    }

	/**  Update the content resource associated with the entry.     */
    public KalturaBaseEntry updateContent(String entryId, KalturaResource resource, int conversionProfileId) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        kparams.add("resource", resource);
        kparams.add("conversionProfileId", conversionProfileId);
        this.kalturaClient.queueServiceCall("baseentry", "updateContent", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaBaseEntry.class, resultXmlElement);
    }

	/**  Get an array of KalturaBaseEntry objects by a comma-separated list of ids.     */
    public List<KalturaBaseEntry> getByIds(String entryIds) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryIds", entryIds);
        this.kalturaClient.queueServiceCall("baseentry", "getByIds", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseArray(KalturaBaseEntry.class, resultXmlElement);
    }

	/**  Delete an entry.     */
    public void delete(String entryId) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        this.kalturaClient.queueServiceCall("baseentry", "delete", kparams);
        if (this.kalturaClient.isMultiRequest())
            return;
        this.kalturaClient.doQueue();
    }

    public KalturaBaseEntryListResponse list() throws KalturaApiException {
        return this.list(null);
    }

    public KalturaBaseEntryListResponse list(KalturaBaseEntryFilter filter) throws KalturaApiException {
        return this.list(filter, null);
    }

	/**  List base entries by filter with paging support.     */
    public KalturaBaseEntryListResponse list(KalturaBaseEntryFilter filter, KalturaFilterPager pager) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("filter", filter);
        kparams.add("pager", pager);
        this.kalturaClient.queueServiceCall("baseentry", "list", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaBaseEntryListResponse.class, resultXmlElement);
    }

    public void listByReferenceId(String refId) throws KalturaApiException {
        this.listByReferenceId(refId, null);
    }

	/**  List base entries by filter according to reference id     */
    public void listByReferenceId(String refId, KalturaFilterPager pager) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("refId", refId);
        kparams.add("pager", pager);
        this.kalturaClient.queueServiceCall("baseentry", "listByReferenceId", kparams);
        if (this.kalturaClient.isMultiRequest())
            return;
        this.kalturaClient.doQueue();
    }

    public int count() throws KalturaApiException {
        return this.count(null);
    }

	/**  Count base entries by filter.     */
    public int count(KalturaBaseEntryFilter filter) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("filter", filter);
        this.kalturaClient.queueServiceCall("baseentry", "count", kparams);
        if (this.kalturaClient.isMultiRequest())
            return 0;
        Element resultXmlElement = this.kalturaClient.doQueue();
        String resultText = resultXmlElement.getTextContent();
        return ParseUtils.parseInt(resultText);
    }

	/**  Upload a file to Kaltura, that can be used to create an entry.      */
    public String upload(File fileData) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        KalturaFiles kfiles = new KalturaFiles();
        kfiles.put("fileData", new KalturaFile(fileData));
        this.kalturaClient.queueServiceCall("baseentry", "upload", kparams, kfiles);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        String resultText = resultXmlElement.getTextContent();
        return ParseUtils.parseString(resultText);
    }

	/**  Update entry thumbnail using a raw jpeg file.     */
    public KalturaBaseEntry updateThumbnailJpeg(String entryId, File fileData) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        KalturaFiles kfiles = new KalturaFiles();
        kfiles.put("fileData", new KalturaFile(fileData));
        this.kalturaClient.queueServiceCall("baseentry", "updateThumbnailJpeg", kparams, kfiles);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaBaseEntry.class, resultXmlElement);
    }

	/**  Update entry thumbnail using url.     */
    public KalturaBaseEntry updateThumbnailFromUrl(String entryId, String url) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        kparams.add("url", url);
        this.kalturaClient.queueServiceCall("baseentry", "updateThumbnailFromUrl", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaBaseEntry.class, resultXmlElement);
    }

	/**  Update entry thumbnail from a different entry by a specified time offset (in
	  seconds).     */
    public KalturaBaseEntry updateThumbnailFromSourceEntry(String entryId, String sourceEntryId, int timeOffset) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        kparams.add("sourceEntryId", sourceEntryId);
        kparams.add("timeOffset", timeOffset);
        this.kalturaClient.queueServiceCall("baseentry", "updateThumbnailFromSourceEntry", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaBaseEntry.class, resultXmlElement);
    }

	/**  Flag inappropriate entry for moderation.     */
    public void flag(KalturaModerationFlag moderationFlag) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("moderationFlag", moderationFlag);
        this.kalturaClient.queueServiceCall("baseentry", "flag", kparams);
        if (this.kalturaClient.isMultiRequest())
            return;
        this.kalturaClient.doQueue();
    }

	/**  Reject the entry and mark the pending flags (if any) as moderated (this will
	  make the entry non-playable).     */
    public void reject(String entryId) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        this.kalturaClient.queueServiceCall("baseentry", "reject", kparams);
        if (this.kalturaClient.isMultiRequest())
            return;
        this.kalturaClient.doQueue();
    }

	/**  Approve the entry and mark the pending flags (if any) as moderated (this will
	  make the entry playable).      */
    public void approve(String entryId) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        this.kalturaClient.queueServiceCall("baseentry", "approve", kparams);
        if (this.kalturaClient.isMultiRequest())
            return;
        this.kalturaClient.doQueue();
    }

    public KalturaModerationFlagListResponse listFlags(String entryId) throws KalturaApiException {
        return this.listFlags(entryId, null);
    }

	/**  List all pending flags for the entry.     */
    public KalturaModerationFlagListResponse listFlags(String entryId, KalturaFilterPager pager) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        kparams.add("pager", pager);
        this.kalturaClient.queueServiceCall("baseentry", "listFlags", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaModerationFlagListResponse.class, resultXmlElement);
    }

	/**  Anonymously rank an entry, no validation is done on duplicate rankings.     */
    public void anonymousRank(String entryId, int rank) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        kparams.add("rank", rank);
        this.kalturaClient.queueServiceCall("baseentry", "anonymousRank", kparams);
        if (this.kalturaClient.isMultiRequest())
            return;
        this.kalturaClient.doQueue();
    }

	/**  This action delivers entry-related data, based on the user's context: access
	  control, restriction, playback format and storage information.     */
    public KalturaEntryContextDataResult getContextData(String entryId, KalturaEntryContextDataParams contextDataParams) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        kparams.add("contextDataParams", contextDataParams);
        this.kalturaClient.queueServiceCall("baseentry", "getContextData", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaEntryContextDataResult.class, resultXmlElement);
    }

    public KalturaBaseEntry export(String entryId, int storageProfileId) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        kparams.add("storageProfileId", storageProfileId);
        this.kalturaClient.queueServiceCall("baseentry", "export", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaBaseEntry.class, resultXmlElement);
    }

    public int index(String id) throws KalturaApiException {
        return this.index(id, true);
    }

	/**  Index an entry by id.     */
    public int index(String id, boolean shouldUpdate) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        kparams.add("shouldUpdate", shouldUpdate);
        this.kalturaClient.queueServiceCall("baseentry", "index", kparams);
        if (this.kalturaClient.isMultiRequest())
            return 0;
        Element resultXmlElement = this.kalturaClient.doQueue();
        String resultText = resultXmlElement.getTextContent();
        return ParseUtils.parseInt(resultText);
    }
}
