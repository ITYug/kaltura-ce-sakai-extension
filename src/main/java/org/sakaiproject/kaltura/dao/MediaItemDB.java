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

package org.sakaiproject.kaltura.dao;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

import org.azeckoski.reflectutils.annotations.ReflectIgnoreClassFields;
import org.joda.time.DateTime;
import org.sakaiproject.kaltura.model.MediaItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is a kaltura media item, it represents a kaltura item in a collection
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 * @deprecated do not use this class for anything other than migration
 */
@ReflectIgnoreClassFields(value = { "collection", "kalturaItem", "locationId", "type" })
public class MediaItemDB implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id; // internal id
    private String locationId; // Sakai site or group reference
    private MediaCollectionDB collection; // foreign key
    private String kalturaId; // kaltura identifier
    private String ownerId; // Sakai userId
    private String creatorId; // Sakai userId (assumed to be ownerId if not set)
    /** indicates the position (ordering) in the collection, 1 is the first item */
    private Integer position;
    private Date dateCreated;
    private Date dateModified;
    private String type = null;
    private boolean hidden = true; // only visible to editors if is true, hidden by default (private)
    private boolean shared = false; // only shared use if this is true (reusable)
    private boolean remixable = false; // only remixable/clippable if this is true
    /*
     * special flag to indicate if this item has been migrated to the kaltura server or not
     */
    private Boolean migrated = false; 

    // truncation settings
    transient int shortMaxName = 16;
    transient int shortMaxDesc = 50;
    transient int shortSpace = 3;

    /**
     * This is set to the id of the media item this one was duplicated from
     */
    transient Long originalId;

    public String getLocation() {
        // if (this.locationId == null && this.collection != null) {
        // return this.collection.getLocationId(); // lazy load most likely
        // }
        return this.locationId;
    }

    public String getIdStr() {
        if (this.id != null) {
            return this.id.toString();
        }
        return null;
    }

    public String getName() {
        String name = this.kalturaId;
        return name;
    }

    public String getShortName() {
        return MediaItemDB.truncateText(getName(), shortMaxName, shortSpace);
    }

    public String getDesc() {
        String desc = "";
        return desc;
    }

    public String getShortDesc() {
        return MediaItemDB.truncateText(getDesc(), shortMaxDesc, shortSpace);
    }

    /**
     * @return the creator user id for this media item
     */
    public String getCreatorUserId() {
        String creator = this.creatorId;
        if (creator == null) {
            creator = this.ownerId;
        }
        return creator;
    }

    /**
     * Default constructor
     */
    public MediaItemDB() {
        this.dateCreated = new Date();
        this.dateModified = this.dateCreated;
        this.position = 0;
    }

    public MediaItemDB(MediaCollectionDB collection, String kalturaId, String ownerId) {
        this();
        this.collection = collection;
        this.locationId = collection.getLocationId();
        this.kalturaId = kalturaId;
        this.ownerId = ownerId;
        this.creatorId = ownerId;
    }

    public MediaItemDB(String locationId, String kalturaId, String ownerId) {
        this();
        this.collection = null;
        this.locationId = locationId;
        this.kalturaId = kalturaId;
        this.ownerId = ownerId;
        this.creatorId = ownerId;
    }

    public MediaItemDB(String locationId, String kalturaId, String ownerId, boolean hidden,
            boolean shared) {
        this();
        this.collection = null;
        this.locationId = locationId;
        this.kalturaId = kalturaId;
        this.ownerId = ownerId;
        this.hidden = hidden;
        this.shared = shared;
        this.creatorId = ownerId;
    }

    /**
     * Duplication of collection item
     */
    public MediaItemDB(MediaCollectionDB collection, MediaItemDB original) {
        this();
        this.collection = collection;
        this.locationId = collection.getLocationId();
        this.kalturaId = original.kalturaId;
        this.ownerId = original.ownerId;
        this.creatorId = original.creatorId;
        this.hidden = original.hidden;
        this.shared = original.shared;
        this.remixable = original.remixable;
        this.type = original.type;
        this.originalId = original.id;
        if (original.dateCreated != null) {
            this.dateCreated = original.dateCreated;
        }
    }

    /**
     * Duplication of library item
     */
    public MediaItemDB(String locationId, MediaItemDB original) {
        this();
        this.collection = null;
        this.locationId = locationId;
        this.kalturaId = original.kalturaId;
        this.ownerId = original.ownerId;
        this.creatorId = original.creatorId;
        this.hidden = original.hidden;
        this.shared = original.shared;
        this.remixable = original.remixable;
        this.type = original.type;
        this.originalId = original.id;
        if (original.dateCreated != null) {
            this.dateCreated = original.dateCreated;
        }
    }

    /**
     * Create a media item from a chunk of XML data
     * 
     * @param collection the collection this item should be part of
     * @param ownerId the ownerId (will override the value from the XML if set)
     * @param element the XML Dom element
     */
    public MediaItemDB(MediaCollectionDB collection, String ownerId, Element element) {
        this();
        if (collection != null) {
            this.collection = collection;
            this.locationId = collection.getLocationId();
        }
        this.ownerId = ownerId;
        this.creatorId = ownerId;

        // now we process the XML data that was passed in
        // ignore the id for now
        if (ownerId == null || "".equals(ownerId)) {
            if (!"".equals(element.getAttribute("ownerId"))) {
                ownerId = element.getAttribute("ownerId");
                this.setOwnerId(ownerId);
            }
        }
        if (!"".equals(element.getAttribute("kalturaId"))) {
            this.setKalturaId(element.getAttribute("kalturaId"));
        }
        if (!"".equals(element.getAttribute("creatorId"))) {
            this.setCreatorId(element.getAttribute("creatorId"));
        }
        if (!"".equals(element.getAttribute("position"))) {
            try {
                int position = Integer.parseInt(element.getAttribute("position"));
                this.setPosition(position);
            } catch (NumberFormatException e) {
                // nothing really to do here but ignore it and default to 0
                this.setPosition(0);
            }
        }
        if (!"".equals(element.getAttribute("hidden"))) {
            this.setHidden(Boolean.parseBoolean(element.getAttribute("hidden")));
        }
        if (!"".equals(element.getAttribute("shared"))) {
            this.setShared(Boolean.parseBoolean(element.getAttribute("shared")));
        }
        if (!"".equals(element.getAttribute("remixable"))) {
            this.setRemixable(Boolean.parseBoolean(element.getAttribute("remixable")));
        }
    }

    /**
     * Create an XML node from this media item which represents the data in it
     * @param doc the related DOM document
     * @return the XML node
     */
    public Element makeXML(Document doc) {
        Element item = doc.createElement(MediaItem.XML_ITEM_KEY);
        item.setAttribute("id", this.getIdStr());
        item.setAttribute("kalturaId", this.getKalturaId());
        item.setAttribute("ownerId", this.getOwnerId());
        item.setAttribute("creatorId", this.getCreatorId());
        item.setAttribute("position", this.getPosition().toString()); // convert to string
        item.setAttribute("dateCreated", new DateTime(this.getDateCreated()).toString());
        item.setAttribute("dateModified", new DateTime(this.getDateModified()).toString());
        item.setAttribute("type", this.getType());
        item.setAttribute("hidden", this.isHidden() ? "true" : "false"); // convert to string
        item.setAttribute("shared", this.isShared() ? "true" : "false"); // convert to string
        item.setAttribute("remixable", this.isRemixable() ? "true" : "false"); // convert to string
        return item;
    }

    public static class MediaItemComparator implements Comparator<MediaItemDB>, Serializable {

        static private final long serialVersionUID = 31L;

        public int compare(MediaItemDB o1, MediaItemDB o2) {
            int comparison = o1.getPosition().compareTo(o2.getPosition());
            if (comparison == 0 && o1.getId() != null && o2.getId() != null) {
                comparison = o1.getId().compareTo(o2.getId());
            }
            return comparison;
        }
    }

    public static class MediaItemNameComparator implements Comparator<MediaItemDB>, Serializable {

        static private final long serialVersionUID = 31L;

        public int compare(MediaItemDB o1, MediaItemDB o2) {
            int comparison = 0;
            if (o1.getName() != null && o2.getName() != null) {
                comparison = o1.getName().compareTo(o2.getName());
            }
            if (comparison == 0 && o1.getId() != null && o2.getId() != null) {
                comparison = o1.getId().compareTo(o2.getId());
            }
            return comparison;
        }
    }

    private static String truncateSuffix = "...";

    public static String truncateText(String text, int maxChars, int reverseSpaceChars) {
        String truncated = text;
        if (maxChars <= 0) {
            maxChars = 50;
        }
        if (text != null && text.length() > maxChars) {
            boolean reverseFind = false;
            if (reverseSpaceChars > 0) {
                int pos = text.lastIndexOf(' ', maxChars);
                if (pos > (maxChars - reverseSpaceChars)) {
                    truncated = text.substring(0, pos);
                    reverseFind = true;
                }
            }
            if (!reverseFind) {
                truncated = text.substring(0, maxChars - 3);
            }
            truncated = truncated + truncateSuffix;
        }
        return truncated;
    }

    @Override
    public String toString() {
        return "{MI:" + id  
                + ":t=" + type 
                + ":c=" + (collection == null ? "SLib" : collection.getId()) 
                + ":ip=" + (hidden ? "H":"h") + (shared ? "S":"s") + (remixable ? "R":"r")
                + ":pos=" + position
                + ":o=" + ownerId
                + ":c=" + creatorId
                + "}";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        if (id != null) {
            result = prime * result + ((id == null) ? 0 : id.hashCode()); // use id only if set
        } else {
            result = prime * result + ((kalturaId == null) ? 0 : kalturaId.hashCode());
            result = prime * result + ((locationId == null) ? 0 : locationId.hashCode());
            result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
            result = prime * result + ((dateCreated == null) ? 0 : dateCreated.hashCode());
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MediaItemDB other = (MediaItemDB) obj;
        if (id == null || other.id == null)  {
            return false;
        } else {
            return id.equals(other.id); // use id only if set
        }
    }

    /**
     * Getters and Setters
     */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKalturaId() {
        return kalturaId;
    }

    public void setKalturaId(String kalturaId) {
        this.kalturaId = kalturaId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    public Integer getPosition() {
        if (position == null) {
            return 0;
        }
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public MediaCollectionDB getCollection() {
        return collection;
    }

    public void setCollection(MediaCollectionDB collection) {
        this.collection = collection;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public boolean isRemixable() {
        return remixable;
    }

    public void setRemixable(boolean remixable) {
        this.remixable = remixable;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public Long getOriginalId() {
        return originalId;
    }

    public boolean isMigrated() {
        return (migrated == null ? false : migrated);
    }

    public void setMigrated(Boolean migrated) {
        this.migrated = migrated == null ? false : migrated;
    }

}
