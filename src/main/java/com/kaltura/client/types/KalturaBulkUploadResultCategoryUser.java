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

public class KalturaBulkUploadResultCategoryUser extends KalturaBulkUploadResult {
    public int categoryId = Integer.MIN_VALUE;
    public String categoryReferenceId;
    public String userId;
    public int permissionLevel = Integer.MIN_VALUE;
    public int updateMethod = Integer.MIN_VALUE;
    public int requiredObjectStatus = Integer.MIN_VALUE;

    public KalturaBulkUploadResultCategoryUser() {
    }

    public KalturaBulkUploadResultCategoryUser(Element node) throws KalturaApiException {
        super(node);
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node aNode = childNodes.item(i);
            String nodeName = aNode.getNodeName();
            String txt = aNode.getTextContent();
            if (nodeName.equals("categoryId")) {
                this.categoryId = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("categoryReferenceId")) {
                this.categoryReferenceId = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("userId")) {
                this.userId = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("permissionLevel")) {
                this.permissionLevel = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("updateMethod")) {
                this.updateMethod = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("requiredObjectStatus")) {
                this.requiredObjectStatus = ParseUtils.parseInt(txt);
                continue;
            } 
        }
    }

    public KalturaParams toParams() {
        KalturaParams kparams = super.toParams();
        kparams.add("objectType", "KalturaBulkUploadResultCategoryUser");
        kparams.add("categoryId", this.categoryId);
        kparams.add("categoryReferenceId", this.categoryReferenceId);
        kparams.add("userId", this.userId);
        kparams.add("permissionLevel", this.permissionLevel);
        kparams.add("updateMethod", this.updateMethod);
        kparams.add("requiredObjectStatus", this.requiredObjectStatus);
        return kparams;
    }

}

