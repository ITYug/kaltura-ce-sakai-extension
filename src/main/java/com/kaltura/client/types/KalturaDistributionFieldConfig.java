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
import com.kaltura.client.enums.KalturaDistributionFieldRequiredStatus;
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

public class KalturaDistributionFieldConfig extends KalturaObjectBase {
	/**  A value taken from a connector field enum which associates the current
	  configuration to that connector field      Field enum class should be returned
	  by the provider's getFieldEnumClass function.        */
    public String fieldName;
	/**  A string that will be shown to the user as the field name in error messages
	  related to the current field        */
    public String userFriendlyFieldName;
	/**  An XSLT string that extracts the right value from the Kaltura entry MRSS XML.   
	    The value of the current connector field will be the one that is returned from
	  transforming the Kaltura entry MRSS XML using this XSLT string.        */
    public String entryMrssXslt;
	/**  Is the field required to have a value for submission ?        */
    public KalturaDistributionFieldRequiredStatus isRequired;
	/**  Trigger distribution update when this field changes or not ?        */
    public boolean updateOnChange;
	/**  Entry column or metadata xpath that should trigger an update        */
    public ArrayList<KalturaString> updateParams;
	/**  Is this field config is the default for the distribution provider?        */
    public boolean isDefault;

    public KalturaDistributionFieldConfig() {
    }

    public KalturaDistributionFieldConfig(Element node) throws KalturaApiException {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node aNode = childNodes.item(i);
            String nodeName = aNode.getNodeName();
            String txt = aNode.getTextContent();
            if (nodeName.equals("fieldName")) {
                this.fieldName = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("userFriendlyFieldName")) {
                this.userFriendlyFieldName = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("entryMrssXslt")) {
                this.entryMrssXslt = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("isRequired")) {
                this.isRequired = KalturaDistributionFieldRequiredStatus.get(ParseUtils.parseInt(txt));
                continue;
            } else if (nodeName.equals("updateOnChange")) {
                this.updateOnChange = ParseUtils.parseBool(txt);
                continue;
            } else if (nodeName.equals("updateParams")) {
                this.updateParams = ParseUtils.parseArray(KalturaString.class, aNode);
                continue;
            } else if (nodeName.equals("isDefault")) {
                this.isDefault = ParseUtils.parseBool(txt);
                continue;
            } 
        }
    }

    public KalturaParams toParams() {
        KalturaParams kparams = super.toParams();
        kparams.add("objectType", "KalturaDistributionFieldConfig");
        kparams.add("fieldName", this.fieldName);
        kparams.add("userFriendlyFieldName", this.userFriendlyFieldName);
        kparams.add("entryMrssXslt", this.entryMrssXslt);
        kparams.add("isRequired", this.isRequired);
        kparams.add("updateOnChange", this.updateOnChange);
        kparams.add("updateParams", this.updateParams);
        return kparams;
    }

}

