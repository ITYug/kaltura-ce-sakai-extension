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
import com.kaltura.client.enums.KalturaCaptionType;
import com.kaltura.client.enums.KalturaCaptionAssetStatus;
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

public abstract class KalturaCaptionAssetBaseFilter extends KalturaAssetFilter {
    public int captionParamsIdEqual = Integer.MIN_VALUE;
    public String captionParamsIdIn;
    public KalturaCaptionType formatEqual;
    public String formatIn;
    public KalturaCaptionAssetStatus statusEqual;
    public String statusIn;
    public String statusNotIn;

    public KalturaCaptionAssetBaseFilter() {
    }

    public KalturaCaptionAssetBaseFilter(Element node) throws KalturaApiException {
        super(node);
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node aNode = childNodes.item(i);
            String nodeName = aNode.getNodeName();
            String txt = aNode.getTextContent();
            if (nodeName.equals("captionParamsIdEqual")) {
                this.captionParamsIdEqual = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("captionParamsIdIn")) {
                this.captionParamsIdIn = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("formatEqual")) {
                this.formatEqual = KalturaCaptionType.get(ParseUtils.parseString(txt));
                continue;
            } else if (nodeName.equals("formatIn")) {
                this.formatIn = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("statusEqual")) {
                this.statusEqual = KalturaCaptionAssetStatus.get(ParseUtils.parseInt(txt));
                continue;
            } else if (nodeName.equals("statusIn")) {
                this.statusIn = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("statusNotIn")) {
                this.statusNotIn = ParseUtils.parseString(txt);
                continue;
            } 
        }
    }

    public KalturaParams toParams() {
        KalturaParams kparams = super.toParams();
        kparams.add("objectType", "KalturaCaptionAssetBaseFilter");
        kparams.add("captionParamsIdEqual", this.captionParamsIdEqual);
        kparams.add("captionParamsIdIn", this.captionParamsIdIn);
        kparams.add("formatEqual", this.formatEqual);
        kparams.add("formatIn", this.formatIn);
        kparams.add("statusEqual", this.statusEqual);
        kparams.add("statusIn", this.statusIn);
        kparams.add("statusNotIn", this.statusNotIn);
        return kparams;
    }

}

