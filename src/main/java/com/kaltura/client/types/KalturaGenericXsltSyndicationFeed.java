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
import java.util.ArrayList;
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

public class KalturaGenericXsltSyndicationFeed extends KalturaGenericSyndicationFeed {
    public String xslt;
	/**  This parameter determines which custom metadata fields of type related-entry
	  should be   expanded to contain the kaltura MRSS feed of the related entry.
	  Related-entry fields not   included in this list will contain only the related
	  entry id.   This property contains a list xPaths in the Kaltura MRSS.     */
    public ArrayList<KalturaString> itemXpathsToExtend;

    public KalturaGenericXsltSyndicationFeed() {
    }

    public KalturaGenericXsltSyndicationFeed(Element node) throws KalturaApiException {
        super(node);
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node aNode = childNodes.item(i);
            String nodeName = aNode.getNodeName();
            String txt = aNode.getTextContent();
            if (nodeName.equals("xslt")) {
                this.xslt = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("itemXpathsToExtend")) {
                this.itemXpathsToExtend = ParseUtils.parseArray(KalturaString.class, aNode);
                continue;
            } 
        }
    }

    public KalturaParams toParams() {
        KalturaParams kparams = super.toParams();
        kparams.add("objectType", "KalturaGenericXsltSyndicationFeed");
        kparams.add("xslt", this.xslt);
        kparams.add("itemXpathsToExtend", this.itemXpathsToExtend);
        return kparams;
    }

}

