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
import com.kaltura.client.KalturaFiles;
import com.kaltura.client.enums.*;

/**
 * This class was generated using generate.php
 * against an XML schema provided by Kaltura.
 * @date Wed, 23 Jan 13 11:47:33 -0500
 * 
 * MANUAL CHANGES TO THIS CLASS WILL BE OVERWRITTEN.
 */

/**  Generic Distribution Provider Actions service    */
public class KalturaGenericDistributionProviderActionService extends KalturaServiceBase {
    public KalturaGenericDistributionProviderActionService(KalturaClient client) {
        this.kalturaClient = client;
    }

	/**  Add new Generic Distribution Provider Action     */
    public KalturaGenericDistributionProviderAction add(KalturaGenericDistributionProviderAction genericDistributionProviderAction) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("genericDistributionProviderAction", genericDistributionProviderAction);
        this.kalturaClient.queueServiceCall("contentdistribution_genericdistributionprovideraction", "add", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaGenericDistributionProviderAction.class, resultXmlElement);
    }

	/**  Add MRSS transform file to generic distribution provider action     */
    public KalturaGenericDistributionProviderAction addMrssTransform(int id, String xslData) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        kparams.add("xslData", xslData);
        this.kalturaClient.queueServiceCall("contentdistribution_genericdistributionprovideraction", "addMrssTransform", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaGenericDistributionProviderAction.class, resultXmlElement);
    }

	/**  Add MRSS transform file to generic distribution provider action     */
    public KalturaGenericDistributionProviderAction addMrssTransformFromFile(int id, File xslFile) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        KalturaFiles kfiles = new KalturaFiles();
        kfiles.put("xslFile", new KalturaFile(xslFile));
        this.kalturaClient.queueServiceCall("contentdistribution_genericdistributionprovideraction", "addMrssTransformFromFile", kparams, kfiles);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaGenericDistributionProviderAction.class, resultXmlElement);
    }

	/**  Add MRSS validate file to generic distribution provider action     */
    public KalturaGenericDistributionProviderAction addMrssValidate(int id, String xsdData) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        kparams.add("xsdData", xsdData);
        this.kalturaClient.queueServiceCall("contentdistribution_genericdistributionprovideraction", "addMrssValidate", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaGenericDistributionProviderAction.class, resultXmlElement);
    }

	/**  Add MRSS validate file to generic distribution provider action     */
    public KalturaGenericDistributionProviderAction addMrssValidateFromFile(int id, File xsdFile) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        KalturaFiles kfiles = new KalturaFiles();
        kfiles.put("xsdFile", new KalturaFile(xsdFile));
        this.kalturaClient.queueServiceCall("contentdistribution_genericdistributionprovideraction", "addMrssValidateFromFile", kparams, kfiles);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaGenericDistributionProviderAction.class, resultXmlElement);
    }

	/**  Add results transform file to generic distribution provider action     */
    public KalturaGenericDistributionProviderAction addResultsTransform(int id, String transformData) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        kparams.add("transformData", transformData);
        this.kalturaClient.queueServiceCall("contentdistribution_genericdistributionprovideraction", "addResultsTransform", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaGenericDistributionProviderAction.class, resultXmlElement);
    }

	/**  Add MRSS transform file to generic distribution provider action     */
    public KalturaGenericDistributionProviderAction addResultsTransformFromFile(int id, File transformFile) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        KalturaFiles kfiles = new KalturaFiles();
        kfiles.put("transformFile", new KalturaFile(transformFile));
        this.kalturaClient.queueServiceCall("contentdistribution_genericdistributionprovideraction", "addResultsTransformFromFile", kparams, kfiles);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaGenericDistributionProviderAction.class, resultXmlElement);
    }

	/**  Get Generic Distribution Provider Action by id     */
    public KalturaGenericDistributionProviderAction get(int id) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        this.kalturaClient.queueServiceCall("contentdistribution_genericdistributionprovideraction", "get", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaGenericDistributionProviderAction.class, resultXmlElement);
    }

	/**  Get Generic Distribution Provider Action by provider id     */
    public KalturaGenericDistributionProviderAction getByProviderId(int genericDistributionProviderId, KalturaDistributionAction actionType) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("genericDistributionProviderId", genericDistributionProviderId);
        kparams.add("actionType", actionType);
        this.kalturaClient.queueServiceCall("contentdistribution_genericdistributionprovideraction", "getByProviderId", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaGenericDistributionProviderAction.class, resultXmlElement);
    }

	/**  Update Generic Distribution Provider Action by provider id     */
    public KalturaGenericDistributionProviderAction updateByProviderId(int genericDistributionProviderId, KalturaDistributionAction actionType, KalturaGenericDistributionProviderAction genericDistributionProviderAction) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("genericDistributionProviderId", genericDistributionProviderId);
        kparams.add("actionType", actionType);
        kparams.add("genericDistributionProviderAction", genericDistributionProviderAction);
        this.kalturaClient.queueServiceCall("contentdistribution_genericdistributionprovideraction", "updateByProviderId", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaGenericDistributionProviderAction.class, resultXmlElement);
    }

	/**  Update Generic Distribution Provider Action by id     */
    public KalturaGenericDistributionProviderAction update(int id, KalturaGenericDistributionProviderAction genericDistributionProviderAction) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        kparams.add("genericDistributionProviderAction", genericDistributionProviderAction);
        this.kalturaClient.queueServiceCall("contentdistribution_genericdistributionprovideraction", "update", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaGenericDistributionProviderAction.class, resultXmlElement);
    }

	/**  Delete Generic Distribution Provider Action by id     */
    public void delete(int id) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        this.kalturaClient.queueServiceCall("contentdistribution_genericdistributionprovideraction", "delete", kparams);
        if (this.kalturaClient.isMultiRequest())
            return;
        this.kalturaClient.doQueue();
    }

	/**  Delete Generic Distribution Provider Action by provider id     */
    public void deleteByProviderId(int genericDistributionProviderId, KalturaDistributionAction actionType) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("genericDistributionProviderId", genericDistributionProviderId);
        kparams.add("actionType", actionType);
        this.kalturaClient.queueServiceCall("contentdistribution_genericdistributionprovideraction", "deleteByProviderId", kparams);
        if (this.kalturaClient.isMultiRequest())
            return;
        this.kalturaClient.doQueue();
    }

    public KalturaGenericDistributionProviderActionListResponse list() throws KalturaApiException {
        return this.list(null);
    }

    public KalturaGenericDistributionProviderActionListResponse list(KalturaGenericDistributionProviderActionFilter filter) throws KalturaApiException {
        return this.list(filter, null);
    }

	/**  List all distribution providers     */
    public KalturaGenericDistributionProviderActionListResponse list(KalturaGenericDistributionProviderActionFilter filter, KalturaFilterPager pager) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("filter", filter);
        kparams.add("pager", pager);
        this.kalturaClient.queueServiceCall("contentdistribution_genericdistributionprovideraction", "list", kparams);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaGenericDistributionProviderActionListResponse.class, resultXmlElement);
    }
}
