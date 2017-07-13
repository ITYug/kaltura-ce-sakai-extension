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

public class KalturaGenericDistributionProfile extends KalturaDistributionProfile {
    public int genericProviderId = Integer.MIN_VALUE;
    public KalturaGenericDistributionProfileAction submitAction;
    public KalturaGenericDistributionProfileAction updateAction;
    public KalturaGenericDistributionProfileAction deleteAction;
    public KalturaGenericDistributionProfileAction fetchReportAction;
    public String updateRequiredEntryFields;
    public String updateRequiredMetadataXPaths;

    public KalturaGenericDistributionProfile() {
    }

    public KalturaGenericDistributionProfile(Element node) throws KalturaApiException {
        super(node);
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node aNode = childNodes.item(i);
            String nodeName = aNode.getNodeName();
            String txt = aNode.getTextContent();
            if (nodeName.equals("genericProviderId")) {
                this.genericProviderId = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("submitAction")) {
                this.submitAction = ParseUtils.parseObject(KalturaGenericDistributionProfileAction.class, aNode);
                continue;
            } else if (nodeName.equals("updateAction")) {
                this.updateAction = ParseUtils.parseObject(KalturaGenericDistributionProfileAction.class, aNode);
                continue;
            } else if (nodeName.equals("deleteAction")) {
                this.deleteAction = ParseUtils.parseObject(KalturaGenericDistributionProfileAction.class, aNode);
                continue;
            } else if (nodeName.equals("fetchReportAction")) {
                this.fetchReportAction = ParseUtils.parseObject(KalturaGenericDistributionProfileAction.class, aNode);
                continue;
            } else if (nodeName.equals("updateRequiredEntryFields")) {
                this.updateRequiredEntryFields = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("updateRequiredMetadataXPaths")) {
                this.updateRequiredMetadataXPaths = ParseUtils.parseString(txt);
                continue;
            } 
        }
    }

    public KalturaParams toParams() {
        KalturaParams kparams = super.toParams();
        kparams.add("objectType", "KalturaGenericDistributionProfile");
        kparams.add("genericProviderId", this.genericProviderId);
        kparams.add("submitAction", this.submitAction);
        kparams.add("updateAction", this.updateAction);
        kparams.add("deleteAction", this.deleteAction);
        kparams.add("fetchReportAction", this.fetchReportAction);
        kparams.add("updateRequiredEntryFields", this.updateRequiredEntryFields);
        kparams.add("updateRequiredMetadataXPaths", this.updateRequiredMetadataXPaths);
        return kparams;
    }

}

