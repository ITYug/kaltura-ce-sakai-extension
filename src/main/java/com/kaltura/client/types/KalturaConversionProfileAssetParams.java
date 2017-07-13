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
import com.kaltura.client.enums.KalturaFlavorReadyBehaviorType;
import com.kaltura.client.enums.KalturaAssetParamsOrigin;
import com.kaltura.client.enums.KalturaNullableBoolean;
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

public class KalturaConversionProfileAssetParams extends KalturaObjectBase {
	/**  The id of the conversion profile     */
    public int conversionProfileId = Integer.MIN_VALUE;
	/**  The id of the asset params     */
    public int assetParamsId = Integer.MIN_VALUE;
	/**  The ingestion origin of the asset params     */
    public KalturaFlavorReadyBehaviorType readyBehavior;
	/**  The ingestion origin of the asset params     */
    public KalturaAssetParamsOrigin origin;
	/**  Asset params system name     */
    public String systemName;
	/**  Starts conversion even if the decision layer reduced the configuration to comply
	  with the source     */
    public KalturaNullableBoolean forceNoneComplied;

    public KalturaConversionProfileAssetParams() {
    }

    public KalturaConversionProfileAssetParams(Element node) throws KalturaApiException {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node aNode = childNodes.item(i);
            String nodeName = aNode.getNodeName();
            String txt = aNode.getTextContent();
            if (nodeName.equals("conversionProfileId")) {
                this.conversionProfileId = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("assetParamsId")) {
                this.assetParamsId = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("readyBehavior")) {
                this.readyBehavior = KalturaFlavorReadyBehaviorType.get(ParseUtils.parseInt(txt));
                continue;
            } else if (nodeName.equals("origin")) {
                this.origin = KalturaAssetParamsOrigin.get(ParseUtils.parseInt(txt));
                continue;
            } else if (nodeName.equals("systemName")) {
                this.systemName = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("forceNoneComplied")) {
                this.forceNoneComplied = KalturaNullableBoolean.get(ParseUtils.parseInt(txt));
                continue;
            } 
        }
    }

    public KalturaParams toParams() {
        KalturaParams kparams = super.toParams();
        kparams.add("objectType", "KalturaConversionProfileAssetParams");
        kparams.add("readyBehavior", this.readyBehavior);
        kparams.add("origin", this.origin);
        kparams.add("systemName", this.systemName);
        kparams.add("forceNoneComplied", this.forceNoneComplied);
        return kparams;
    }

}

