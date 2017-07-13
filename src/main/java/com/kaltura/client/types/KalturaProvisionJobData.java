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
import com.kaltura.client.utils.ParseUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class was generated using generate.php
 * against an XML schema provided by Kaltura.
 * @date Wed, 23 Jan 13 11:47:33 -0500
 * 
 * MANUAL CHANGES TO THIS CLASS WILL BE OVERWRITTEN.
 */

public class KalturaProvisionJobData extends KalturaJobData {
    public String streamID;
    public String backupStreamID;
    public String rtmp;
    public String encoderIP;
    public String backupEncoderIP;
    public String encoderPassword;
    public String encoderUsername;
    public int endDate = Integer.MIN_VALUE;
    public String returnVal;
    public int mediaType = Integer.MIN_VALUE;
    public String primaryBroadcastingUrl;
    public String secondaryBroadcastingUrl;
    public String streamName;

    public KalturaProvisionJobData() {
    }

    public KalturaProvisionJobData(Element node) throws KalturaApiException {
        super(node);
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node aNode = childNodes.item(i);
            String nodeName = aNode.getNodeName();
            String txt = aNode.getTextContent();
            if (nodeName.equals("streamID")) {
                this.streamID = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("backupStreamID")) {
                this.backupStreamID = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("rtmp")) {
                this.rtmp = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("encoderIP")) {
                this.encoderIP = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("backupEncoderIP")) {
                this.backupEncoderIP = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("encoderPassword")) {
                this.encoderPassword = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("encoderUsername")) {
                this.encoderUsername = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("endDate")) {
                this.endDate = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("returnVal")) {
                this.returnVal = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("mediaType")) {
                this.mediaType = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("primaryBroadcastingUrl")) {
                this.primaryBroadcastingUrl = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("secondaryBroadcastingUrl")) {
                this.secondaryBroadcastingUrl = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("streamName")) {
                this.streamName = ParseUtils.parseString(txt);
                continue;
            } 
        }
    }

    public KalturaParams toParams() {
        KalturaParams kparams = super.toParams();
        kparams.add("objectType", "KalturaProvisionJobData");
        kparams.add("streamID", this.streamID);
        kparams.add("backupStreamID", this.backupStreamID);
        kparams.add("rtmp", this.rtmp);
        kparams.add("encoderIP", this.encoderIP);
        kparams.add("backupEncoderIP", this.backupEncoderIP);
        kparams.add("encoderPassword", this.encoderPassword);
        kparams.add("encoderUsername", this.encoderUsername);
        kparams.add("endDate", this.endDate);
        kparams.add("returnVal", this.returnVal);
        kparams.add("mediaType", this.mediaType);
        kparams.add("primaryBroadcastingUrl", this.primaryBroadcastingUrl);
        kparams.add("secondaryBroadcastingUrl", this.secondaryBroadcastingUrl);
        kparams.add("streamName", this.streamName);
        return kparams;
    }

}

