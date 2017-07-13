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
import com.kaltura.client.enums.KalturaUiConfObjType;
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

/**  Info about uiconf type    */
public class KalturaUiConfTypeInfo extends KalturaObjectBase {
	/**  UiConf Type     */
    public KalturaUiConfObjType type;
	/**  Available versions        */
    public ArrayList<KalturaString> versions;
	/**  The direcotry this type is saved at        */
    public String directory;
	/**  Filename for this UiConf type        */
    public String filename;

    public KalturaUiConfTypeInfo() {
    }

    public KalturaUiConfTypeInfo(Element node) throws KalturaApiException {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node aNode = childNodes.item(i);
            String nodeName = aNode.getNodeName();
            String txt = aNode.getTextContent();
            if (nodeName.equals("type")) {
                this.type = KalturaUiConfObjType.get(ParseUtils.parseInt(txt));
                continue;
            } else if (nodeName.equals("versions")) {
                this.versions = ParseUtils.parseArray(KalturaString.class, aNode);
                continue;
            } else if (nodeName.equals("directory")) {
                this.directory = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("filename")) {
                this.filename = ParseUtils.parseString(txt);
                continue;
            } 
        }
    }

    public KalturaParams toParams() {
        KalturaParams kparams = super.toParams();
        kparams.add("objectType", "KalturaUiConfTypeInfo");
        kparams.add("type", this.type);
        kparams.add("versions", this.versions);
        kparams.add("directory", this.directory);
        kparams.add("filename", this.filename);
        return kparams;
    }

}

