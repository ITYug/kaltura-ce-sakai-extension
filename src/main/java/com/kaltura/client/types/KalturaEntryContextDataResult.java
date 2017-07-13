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

public class KalturaEntryContextDataResult extends KalturaObjectBase {
    public boolean isSiteRestricted;
    public boolean isCountryRestricted;
    public boolean isSessionRestricted;
    public boolean isIpAddressRestricted;
    public boolean isUserAgentRestricted;
    public int previewLength = Integer.MIN_VALUE;
    public boolean isScheduledNow;
    public boolean isAdmin;
	/**  http/rtmp/hdnetwork     */
    public String streamerType;
	/**  http/https, rtmp/rtmpe     */
    public String mediaProtocol;
    public String storageProfilesXML;
	/**  Array of messages as received from the access control rules that invalidated     */
    public ArrayList<KalturaString> accessControlMessages;
	/**  Array of actions as received from the access control rules that invalidated     */
    public ArrayList<KalturaAccessControlAction> accessControlActions;

    public KalturaEntryContextDataResult() {
    }

    public KalturaEntryContextDataResult(Element node) throws KalturaApiException {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node aNode = childNodes.item(i);
            String nodeName = aNode.getNodeName();
            String txt = aNode.getTextContent();
            if (nodeName.equals("isSiteRestricted")) {
                this.isSiteRestricted = ParseUtils.parseBool(txt);
                continue;
            } else if (nodeName.equals("isCountryRestricted")) {
                this.isCountryRestricted = ParseUtils.parseBool(txt);
                continue;
            } else if (nodeName.equals("isSessionRestricted")) {
                this.isSessionRestricted = ParseUtils.parseBool(txt);
                continue;
            } else if (nodeName.equals("isIpAddressRestricted")) {
                this.isIpAddressRestricted = ParseUtils.parseBool(txt);
                continue;
            } else if (nodeName.equals("isUserAgentRestricted")) {
                this.isUserAgentRestricted = ParseUtils.parseBool(txt);
                continue;
            } else if (nodeName.equals("previewLength")) {
                this.previewLength = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("isScheduledNow")) {
                this.isScheduledNow = ParseUtils.parseBool(txt);
                continue;
            } else if (nodeName.equals("isAdmin")) {
                this.isAdmin = ParseUtils.parseBool(txt);
                continue;
            } else if (nodeName.equals("streamerType")) {
                this.streamerType = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("mediaProtocol")) {
                this.mediaProtocol = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("storageProfilesXML")) {
                this.storageProfilesXML = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("accessControlMessages")) {
                this.accessControlMessages = ParseUtils.parseArray(KalturaString.class, aNode);
                continue;
            } else if (nodeName.equals("accessControlActions")) {
                this.accessControlActions = ParseUtils.parseArray(KalturaAccessControlAction.class, aNode);
                continue;
            } 
        }
    }

    public KalturaParams toParams() {
        KalturaParams kparams = super.toParams();
        kparams.add("objectType", "KalturaEntryContextDataResult");
        kparams.add("isSiteRestricted", this.isSiteRestricted);
        kparams.add("isCountryRestricted", this.isCountryRestricted);
        kparams.add("isSessionRestricted", this.isSessionRestricted);
        kparams.add("isIpAddressRestricted", this.isIpAddressRestricted);
        kparams.add("isUserAgentRestricted", this.isUserAgentRestricted);
        kparams.add("previewLength", this.previewLength);
        kparams.add("isScheduledNow", this.isScheduledNow);
        kparams.add("isAdmin", this.isAdmin);
        kparams.add("streamerType", this.streamerType);
        kparams.add("mediaProtocol", this.mediaProtocol);
        kparams.add("storageProfilesXML", this.storageProfilesXML);
        kparams.add("accessControlMessages", this.accessControlMessages);
        kparams.add("accessControlActions", this.accessControlActions);
        return kparams;
    }

}

