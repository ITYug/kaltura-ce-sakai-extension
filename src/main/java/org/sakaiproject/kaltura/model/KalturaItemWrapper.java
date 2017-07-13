/**
 * Copyright 2010 Unicon (R) Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.kaltura.model;

import java.util.Date;

import com.kaltura.client.enums.KalturaMediaType;
import com.kaltura.client.types.KalturaMediaEntry;

public class KalturaItemWrapper {

    private KalturaMediaEntry entry;

    public KalturaItemWrapper(KalturaMediaEntry entry) {
        this.entry = entry;
    }

    public String getName() {
        return entry.name;
    }

    public String getDesc() {
        return entry.description;
    }

    public float getDuration() {
        return entry.duration;
    }

    public Date getDate() {
        return new Date(entry.createdAt* (1000l));
    }

    public int getWidth() {
        return entry.width;
    }

    public int getHeight() {
        return entry.height;
    }

    public String getType() {
        String type = "video";
        KalturaMediaType mediaType = entry.mediaType;
        type = mediaType.toString().toLowerCase();
        return type;
    }

    public String getThumbnail() {
        return entry.thumbnailUrl;
    }

    public String getKalturaId() {
        return entry.id;
    }


}
