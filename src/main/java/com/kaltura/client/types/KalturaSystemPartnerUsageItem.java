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
import com.kaltura.client.enums.KalturaPartnerStatus;
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

public class KalturaSystemPartnerUsageItem extends KalturaObjectBase {
	/**  Partner ID     */
    public int partnerId = Integer.MIN_VALUE;
	/**  Partner name     */
    public String partnerName;
	/**  Partner status     */
    public KalturaPartnerStatus partnerStatus;
	/**  Partner package     */
    public int partnerPackage = Integer.MIN_VALUE;
	/**  Partner creation date (Unix timestamp)     */
    public int partnerCreatedAt = Integer.MIN_VALUE;
	/**  Number of player loads in the specific date range     */
    public int views = Integer.MIN_VALUE;
	/**  Number of plays in the specific date range     */
    public int plays = Integer.MIN_VALUE;
	/**  Number of new entries created during specific date range     */
    public int entriesCount = Integer.MIN_VALUE;
	/**  Total number of entries     */
    public int totalEntriesCount = Integer.MIN_VALUE;
	/**  Number of new video entries created during specific date range     */
    public int videoEntriesCount = Integer.MIN_VALUE;
	/**  Number of new image entries created during specific date range     */
    public int imageEntriesCount = Integer.MIN_VALUE;
	/**  Number of new audio entries created during specific date range     */
    public int audioEntriesCount = Integer.MIN_VALUE;
	/**  Number of new mix entries created during specific date range     */
    public int mixEntriesCount = Integer.MIN_VALUE;
	/**  The total bandwidth usage during the given date range (in MB)     */
    public float bandwidth = Float.MIN_VALUE;
	/**  The total storage consumption (in MB)     */
    public float totalStorage = Float.MIN_VALUE;
	/**  The change in storage consumption (new uploads) during the given date range (in
	  MB)     */
    public float storage = Float.MIN_VALUE;
	/**  The peak amount of storage consumption during the given date range for the
	  specific publisher     */
    public float peakStorage = Float.MIN_VALUE;
	/**  The average amount of storage consumption during the given date range for the
	  specific publisher     */
    public float avgStorage = Float.MIN_VALUE;
	/**  The combined amount of bandwidth and storage consumed during the given date
	  range for the specific publisher     */
    public float combinedBandwidthStorage = Float.MIN_VALUE;
	/**  Amount of deleted storage in MB     */
    public float deletedStorage = Float.MIN_VALUE;

    public KalturaSystemPartnerUsageItem() {
    }

    public KalturaSystemPartnerUsageItem(Element node) throws KalturaApiException {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node aNode = childNodes.item(i);
            String nodeName = aNode.getNodeName();
            String txt = aNode.getTextContent();
            if (nodeName.equals("partnerId")) {
                this.partnerId = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("partnerName")) {
                this.partnerName = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("partnerStatus")) {
                this.partnerStatus = KalturaPartnerStatus.get(ParseUtils.parseInt(txt));
                continue;
            } else if (nodeName.equals("partnerPackage")) {
                this.partnerPackage = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("partnerCreatedAt")) {
                this.partnerCreatedAt = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("views")) {
                this.views = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("plays")) {
                this.plays = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("entriesCount")) {
                this.entriesCount = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("totalEntriesCount")) {
                this.totalEntriesCount = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("videoEntriesCount")) {
                this.videoEntriesCount = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("imageEntriesCount")) {
                this.imageEntriesCount = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("audioEntriesCount")) {
                this.audioEntriesCount = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("mixEntriesCount")) {
                this.mixEntriesCount = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("bandwidth")) {
                this.bandwidth = ParseUtils.parseFloat(txt);
                continue;
            } else if (nodeName.equals("totalStorage")) {
                this.totalStorage = ParseUtils.parseFloat(txt);
                continue;
            } else if (nodeName.equals("storage")) {
                this.storage = ParseUtils.parseFloat(txt);
                continue;
            } else if (nodeName.equals("peakStorage")) {
                this.peakStorage = ParseUtils.parseFloat(txt);
                continue;
            } else if (nodeName.equals("avgStorage")) {
                this.avgStorage = ParseUtils.parseFloat(txt);
                continue;
            } else if (nodeName.equals("combinedBandwidthStorage")) {
                this.combinedBandwidthStorage = ParseUtils.parseFloat(txt);
                continue;
            } else if (nodeName.equals("deletedStorage")) {
                this.deletedStorage = ParseUtils.parseFloat(txt);
                continue;
            } 
        }
    }

    public KalturaParams toParams() {
        KalturaParams kparams = super.toParams();
        kparams.add("objectType", "KalturaSystemPartnerUsageItem");
        kparams.add("partnerId", this.partnerId);
        kparams.add("partnerName", this.partnerName);
        kparams.add("partnerStatus", this.partnerStatus);
        kparams.add("partnerPackage", this.partnerPackage);
        kparams.add("partnerCreatedAt", this.partnerCreatedAt);
        kparams.add("views", this.views);
        kparams.add("plays", this.plays);
        kparams.add("entriesCount", this.entriesCount);
        kparams.add("totalEntriesCount", this.totalEntriesCount);
        kparams.add("videoEntriesCount", this.videoEntriesCount);
        kparams.add("imageEntriesCount", this.imageEntriesCount);
        kparams.add("audioEntriesCount", this.audioEntriesCount);
        kparams.add("mixEntriesCount", this.mixEntriesCount);
        kparams.add("bandwidth", this.bandwidth);
        kparams.add("totalStorage", this.totalStorage);
        kparams.add("storage", this.storage);
        kparams.add("peakStorage", this.peakStorage);
        kparams.add("avgStorage", this.avgStorage);
        kparams.add("combinedBandwidthStorage", this.combinedBandwidthStorage);
        kparams.add("deletedStorage", this.deletedStorage);
        return kparams;
    }

}

