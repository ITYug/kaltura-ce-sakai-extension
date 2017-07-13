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
import java.util.Date;

import org.sakaiproject.kaltura.model.MediaCollection;

/**
 * This is a kaltura media collection, it represents a set of kaltura items in the database
 *  
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 * @deprecated do not use this class for anything other than migration
 */
public class MediaCollectionDB implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id; // internal id
    private String locationId; // Sakai site or group id
    private String ownerId; // Sakai userId
    private String title; // Display name for this collection
    private String description; // Description for this collection
    private Date dateCreated;
    private Date dateModified;
    private boolean hidden = false; // only visible to editors if true
    private String sharing = MediaCollection.SHARING_ADMIN; // can be edited by any admin in course
    /*
     * special flag to indicate if this item has been migrated to the kaltura server or not
     */
    private Boolean migrated = false; 

    // truncation settings
    transient int shortMaxName = 30;
    transient int shortMaxDesc = 80;
    transient int shortSpace = 6;

    public String getIdStr() {
        if (this.id != null) {
            return this.id.toString();
        }
        return null;
    }

    public String getShortTitle() {
        return MediaItemDB.truncateText(getTitle(), shortMaxName, shortSpace);
    }

    public String getShortDescription() {
        return MediaItemDB.truncateText(getDescription(), shortMaxDesc, shortSpace);
    }

    /**
     * Default constructor
     */
    public MediaCollectionDB() {
    }

    public MediaCollectionDB(String locationId, String ownerId, String title) {
        this.locationId = locationId;
        this.ownerId = ownerId;
        this.title = title;
        this.hidden = true; // defaults to hidden
        this.dateCreated = new Date();
        this.dateModified = this.dateCreated;
    }

    public MediaCollectionDB(String locationId, String ownerId, String title, boolean hidden, String sharing) {
        this(locationId, ownerId, title);
        this.hidden = hidden;
        if (sharing != null) {
            setSharing(sharing);
        }
    }

    public MediaCollectionDB(String locationId, MediaCollectionDB original) {
        this(locationId, original.ownerId, original.title);
        this.description = original.description;
        this.hidden = original.hidden;
        if (original.sharing != null) {
            setSharing(original.sharing);
        }
    }

    @Override
    public String toString() {
        return "{MC:" + id 
                + ":s=" + sharing 
                + ":l=" + locationId 
                + ":h=" + hidden  
                + ":o=" + ownerId
                + "}";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        if (id != null) {
            result = prime * result + ((id == null) ? 0 : id.hashCode());
        } else {
            result = prime * result + ((locationId == null) ? 0 : locationId.hashCode());
            result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
            result = prime * result + ((sharing == null) ? 0 : sharing.hashCode());
            result = prime * result + ((title == null) ? 0 : title.hashCode());
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
        MediaCollectionDB other = (MediaCollectionDB) obj;
        if (id == null || other.id == null)  {
            return false;
        } else {
            return id.equals(other.id);
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

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getSharing() {
        return sharing;
    }

    public void setSharing(String sharing) {
        this.sharing = sharing;
    }

    public boolean isMigrated() {
        return (migrated == null ? false : migrated);
    }

    public void setMigrated(Boolean migrated) {
        this.migrated = migrated == null ? false : migrated;
    }

}
