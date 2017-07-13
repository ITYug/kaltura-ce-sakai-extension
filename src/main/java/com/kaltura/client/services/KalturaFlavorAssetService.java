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
import java.util.List;

/**
 * This class was generated using generate.php
 * against an XML schema provided by Kaltura.
 * @date Wed, 23 Jan 13 11:47:33 -0500
 * 
 * MANUAL CHANGES TO THIS CLASS WILL BE OVERWRITTEN.
 */

/**  Retrieve information and invoke actions on Flavor Asset    */
public class KalturaFlavorAssetService extends KalturaServiceBase {
    public KalturaFlavorAssetService(KalturaClient client) {
        this.kalturaClient = client;
    }

	/**  Add flavor asset        */
    public KalturaFlavorAsset add(String entryId, KalturaFlavorAsset flavorAsset) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        kparams.add("flavorAsset", flavorAsset);
        this.kalturaClient.queueServiceCall("flavorasset", "add", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaFlavorAsset.class, resultXmlElement);
    }

	/**  Update flavor asset        */
    public KalturaFlavorAsset update(String id, KalturaFlavorAsset flavorAsset) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        kparams.add("flavorAsset", flavorAsset);
        this.kalturaClient.queueServiceCall("flavorasset", "update", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaFlavorAsset.class, resultXmlElement);
    }

	/**  Update content of flavor asset        */
    public KalturaFlavorAsset setContent(String id, KalturaContentResource contentResource) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        kparams.add("contentResource", contentResource);
        this.kalturaClient.queueServiceCall("flavorasset", "setContent", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaFlavorAsset.class, resultXmlElement);
    }

	/**  Get Flavor Asset by ID     */
    public KalturaFlavorAsset get(String id) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        this.kalturaClient.queueServiceCall("flavorasset", "get", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaFlavorAsset.class, resultXmlElement);
    }

	/**  Get Flavor Assets for Entry     */
    public List<KalturaFlavorAsset> getByEntryId(String entryId) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        this.kalturaClient.queueServiceCall("flavorasset", "getByEntryId", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseArray(KalturaFlavorAsset.class, resultXmlElement);
    }

    public KalturaFlavorAssetListResponse list() throws KalturaApiException {
        return this.list(null);
    }

    public KalturaFlavorAssetListResponse list(KalturaAssetFilter filter) throws KalturaApiException {
        return this.list(filter, null);
    }

	/**  List Flavor Assets by filter and pager     */
    public KalturaFlavorAssetListResponse list(KalturaAssetFilter filter, KalturaFilterPager pager) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("filter", filter);
        kparams.add("pager", pager);
        this.kalturaClient.queueServiceCall("flavorasset", "list", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaFlavorAssetListResponse.class, resultXmlElement);
    }

	/**  Get web playable Flavor Assets for Entry     */
    public List<KalturaFlavorAsset> getWebPlayableByEntryId(String entryId) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        this.kalturaClient.queueServiceCall("flavorasset", "getWebPlayableByEntryId", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseArray(KalturaFlavorAsset.class, resultXmlElement);
    }

    public void convert(String entryId, int flavorParamsId) throws KalturaApiException {
        this.convert(entryId, flavorParamsId, 0);
    }

	/**  Add and convert new Flavor Asset for Entry with specific Flavor Params     */
    public void convert(String entryId, int flavorParamsId, int priority) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        kparams.add("flavorParamsId", flavorParamsId);
        kparams.add("priority", priority);
        this.kalturaClient.queueServiceCall("flavorasset", "convert", kparams);
        if (this.kalturaClient.isMultiRequest())
            return;
        this.kalturaClient.doQueue();
    }

	/**  Reconvert Flavor Asset by ID     */
    public void reconvert(String id) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        this.kalturaClient.queueServiceCall("flavorasset", "reconvert", kparams);
        if (this.kalturaClient.isMultiRequest())
            return;
        this.kalturaClient.doQueue();
    }

	/**  Delete Flavor Asset by ID     */
    public void delete(String id) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        this.kalturaClient.queueServiceCall("flavorasset", "delete", kparams);
        if (this.kalturaClient.isMultiRequest())
            return;
        this.kalturaClient.doQueue();
    }

    public String getUrl(String id) throws KalturaApiException {
        return this.getUrl(id, Integer.MIN_VALUE);
    }

	/**  Get download URL for the asset     */
    public String getUrl(String id, int storageId) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        kparams.add("storageId", storageId);
        this.kalturaClient.queueServiceCall("flavorasset", "getUrl", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        String resultText = resultXmlElement.getTextContent();
        return ParseUtils.parseString(resultText);
    }

	/**  Get remote storage existing paths for the asset     */
    public KalturaRemotePathListResponse getRemotePaths(String id) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        this.kalturaClient.queueServiceCall("flavorasset", "getRemotePaths", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaRemotePathListResponse.class, resultXmlElement);
    }

    public String getDownloadUrl(String id) throws KalturaApiException {
        return this.getDownloadUrl(id, false);
    }

	/**  Get download URL for the Flavor Asset     */
    public String getDownloadUrl(String id, boolean useCdn) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        kparams.add("useCdn", useCdn);
        this.kalturaClient.queueServiceCall("flavorasset", "getDownloadUrl", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        String resultText = resultXmlElement.getTextContent();
        return ParseUtils.parseString(resultText);
    }

	/**  Get Flavor Asset with the relevant Flavor Params (Flavor Params can exist
	  without Flavor Asset & vice versa)     */
    public List<KalturaFlavorAssetWithParams> getFlavorAssetsWithParams(String entryId) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("entryId", entryId);
        this.kalturaClient.queueServiceCall("flavorasset", "getFlavorAssetsWithParams", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseArray(KalturaFlavorAssetWithParams.class, resultXmlElement);
    }

    public KalturaFlavorAsset export(String assetId, int storageProfileId) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("assetId", assetId);
        kparams.add("storageProfileId", storageProfileId);
        this.kalturaClient.queueServiceCall("flavorasset", "export", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaFlavorAsset.class, resultXmlElement);
    }
}
