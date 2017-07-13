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

public class KalturaAccessControlScope extends KalturaObjectBase {
	/**  URL to be used to test domain conditions.     */
    public String referrer;
	/**  IP to be used to test geographic location conditions.     */
    public String ip;
	/**  Kaltura session to be used to test session and user conditions.     */
    public String ks;
	/**  Browser or client application to be used to test agent conditions.     */
    public String userAgent;
	/**  Unix timestamp (In seconds) to be used to test entry scheduling, keep null to
	  use now.     */
    public int time = Integer.MIN_VALUE;
	/**  Indicates what contexts should be tested. No contexts means any context.     */
    public ArrayList<KalturaAccessControlContextTypeHolder> contexts;
	/**  Array of hashes to pass to the access control profile scope     */
    public ArrayList<KalturaKeyValue> hashes;

    public KalturaAccessControlScope() {
    }

    public KalturaAccessControlScope(Element node) throws KalturaApiException {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node aNode = childNodes.item(i);
            String nodeName = aNode.getNodeName();
            String txt = aNode.getTextContent();
            if (nodeName.equals("referrer")) {
                this.referrer = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("ip")) {
                this.ip = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("ks")) {
                this.ks = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("userAgent")) {
                this.userAgent = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("time")) {
                this.time = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("contexts")) {
                this.contexts = ParseUtils.parseArray(KalturaAccessControlContextTypeHolder.class, aNode);
                continue;
            } else if (nodeName.equals("hashes")) {
                this.hashes = ParseUtils.parseArray(KalturaKeyValue.class, aNode);
                continue;
            } 
        }
    }

    public KalturaParams toParams() {
        KalturaParams kparams = super.toParams();
        kparams.add("objectType", "KalturaAccessControlScope");
        kparams.add("referrer", this.referrer);
        kparams.add("ip", this.ip);
        kparams.add("ks", this.ks);
        kparams.add("userAgent", this.userAgent);
        kparams.add("time", this.time);
        kparams.add("contexts", this.contexts);
        kparams.add("hashes", this.hashes);
        return kparams;
    }

}

