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
package com.kaltura.client.types;

import org.w3c.dom.Element;
import com.kaltura.client.KalturaParams;
import com.kaltura.client.KalturaApiException;
import com.kaltura.client.KalturaObjectBase;
import com.kaltura.client.enums.KalturaStorageProfileStatus;
import com.kaltura.client.enums.KalturaStorageProfileProtocol;
import com.kaltura.client.enums.KalturaStorageProfileDeliveryStatus;
import com.kaltura.client.enums.KalturaStorageProfileReadyBehavior;
import java.util.ArrayList;
import com.kaltura.client.utils.ParseUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class was generated using generate.php
 * against an XML schema provided by Kaltura.
 * @date Wed, 23 Jan 13 11:47:32 -0500
 * 
 * MANUAL CHANGES TO THIS CLASS WILL BE OVERWRITTEN.
 */

public class KalturaStorageProfile extends KalturaObjectBase {
    public int id = Integer.MIN_VALUE;
    public int createdAt = Integer.MIN_VALUE;
    public int updatedAt = Integer.MIN_VALUE;
    public int partnerId = Integer.MIN_VALUE;
    public String name;
    public String systemName;
    public String desciption;
    public KalturaStorageProfileStatus status;
    public KalturaStorageProfileProtocol protocol;
    public String storageUrl;
    public String storageBaseDir;
    public String storageUsername;
    public String storagePassword;
    public boolean storageFtpPassiveMode;
    public String deliveryHttpBaseUrl;
    public String deliveryRmpBaseUrl;
    public String deliveryIisBaseUrl;
    public int minFileSize = Integer.MIN_VALUE;
    public int maxFileSize = Integer.MIN_VALUE;
    public String flavorParamsIds;
    public int maxConcurrentConnections = Integer.MIN_VALUE;
    public String pathManagerClass;
    public ArrayList<KalturaKeyValue> pathManagerParams;
    public String urlManagerClass;
    public ArrayList<KalturaKeyValue> urlManagerParams;
	/**  No need to create enum for temp field     */
    public int trigger = Integer.MIN_VALUE;
	/**  Delivery Priority     */
    public int deliveryPriority = Integer.MIN_VALUE;
    public KalturaStorageProfileDeliveryStatus deliveryStatus;
    public String rtmpPrefix;
    public KalturaStorageProfileReadyBehavior readyBehavior;
	/**  Flag sugnifying that the storage exported content should be deleted when soure
	  entry is deleted     */
    public int allowAutoDelete = Integer.MIN_VALUE;

    public KalturaStorageProfile() {
    }

    public KalturaStorageProfile(Element node) throws KalturaApiException {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node aNode = childNodes.item(i);
            String nodeName = aNode.getNodeName();
            String txt = aNode.getTextContent();
            if (nodeName.equals("id")) {
                this.id = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("createdAt")) {
                this.createdAt = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("updatedAt")) {
                this.updatedAt = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("partnerId")) {
                this.partnerId = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("name")) {
                this.name = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("systemName")) {
                this.systemName = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("desciption")) {
                this.desciption = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("status")) {
                this.status = KalturaStorageProfileStatus.get(ParseUtils.parseInt(txt));
                continue;
            } else if (nodeName.equals("protocol")) {
                this.protocol = KalturaStorageProfileProtocol.get(ParseUtils.parseInt(txt));
                continue;
            } else if (nodeName.equals("storageUrl")) {
                this.storageUrl = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("storageBaseDir")) {
                this.storageBaseDir = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("storageUsername")) {
                this.storageUsername = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("storagePassword")) {
                this.storagePassword = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("storageFtpPassiveMode")) {
                this.storageFtpPassiveMode = ParseUtils.parseBool(txt);
                continue;
            } else if (nodeName.equals("deliveryHttpBaseUrl")) {
                this.deliveryHttpBaseUrl = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("deliveryRmpBaseUrl")) {
                this.deliveryRmpBaseUrl = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("deliveryIisBaseUrl")) {
                this.deliveryIisBaseUrl = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("minFileSize")) {
                this.minFileSize = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("maxFileSize")) {
                this.maxFileSize = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("flavorParamsIds")) {
                this.flavorParamsIds = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("maxConcurrentConnections")) {
                this.maxConcurrentConnections = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("pathManagerClass")) {
                this.pathManagerClass = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("pathManagerParams")) {
                this.pathManagerParams = ParseUtils.parseArray(KalturaKeyValue.class, aNode);
                continue;
            } else if (nodeName.equals("urlManagerClass")) {
                this.urlManagerClass = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("urlManagerParams")) {
                this.urlManagerParams = ParseUtils.parseArray(KalturaKeyValue.class, aNode);
                continue;
            } else if (nodeName.equals("trigger")) {
                this.trigger = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("deliveryPriority")) {
                this.deliveryPriority = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("deliveryStatus")) {
                this.deliveryStatus = KalturaStorageProfileDeliveryStatus.get(ParseUtils.parseInt(txt));
                continue;
            } else if (nodeName.equals("rtmpPrefix")) {
                this.rtmpPrefix = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("readyBehavior")) {
                this.readyBehavior = KalturaStorageProfileReadyBehavior.get(ParseUtils.parseInt(txt));
                continue;
            } else if (nodeName.equals("allowAutoDelete")) {
                this.allowAutoDelete = ParseUtils.parseInt(txt);
                continue;
            } 
        }
    }

    public KalturaParams toParams() {
        KalturaParams kparams = super.toParams();
        kparams.add("objectType", "KalturaStorageProfile");
        kparams.add("name", this.name);
        kparams.add("systemName", this.systemName);
        kparams.add("desciption", this.desciption);
        kparams.add("status", this.status);
        kparams.add("protocol", this.protocol);
        kparams.add("storageUrl", this.storageUrl);
        kparams.add("storageBaseDir", this.storageBaseDir);
        kparams.add("storageUsername", this.storageUsername);
        kparams.add("storagePassword", this.storagePassword);
        kparams.add("storageFtpPassiveMode", this.storageFtpPassiveMode);
        kparams.add("deliveryHttpBaseUrl", this.deliveryHttpBaseUrl);
        kparams.add("deliveryRmpBaseUrl", this.deliveryRmpBaseUrl);
        kparams.add("deliveryIisBaseUrl", this.deliveryIisBaseUrl);
        kparams.add("minFileSize", this.minFileSize);
        kparams.add("maxFileSize", this.maxFileSize);
        kparams.add("flavorParamsIds", this.flavorParamsIds);
        kparams.add("maxConcurrentConnections", this.maxConcurrentConnections);
        kparams.add("pathManagerClass", this.pathManagerClass);
        kparams.add("pathManagerParams", this.pathManagerParams);
        kparams.add("urlManagerClass", this.urlManagerClass);
        kparams.add("urlManagerParams", this.urlManagerParams);
        kparams.add("trigger", this.trigger);
        kparams.add("deliveryPriority", this.deliveryPriority);
        kparams.add("deliveryStatus", this.deliveryStatus);
        kparams.add("rtmpPrefix", this.rtmpPrefix);
        kparams.add("readyBehavior", this.readyBehavior);
        kparams.add("allowAutoDelete", this.allowAutoDelete);
        return kparams;
    }

}

