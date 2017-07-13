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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.azeckoski.reflectutils.annotations.ReflectIgnoreClassFields;
import org.sakaiproject.kaltura.logic.User;

import com.kaltura.client.types.KalturaPlaylist;

/**
 * This is a kaltura media collection, it represents a set of kaltura items,
 * this will be represented by a playlist in the kaltura server
 *
 * Special viewing permissions KAL-40:
 * Show the collection to admin/manage users all the time. 
 * Show the "no items" message to admin/manage users when there are no items in the collection. 
 * Show the "no public items" message to admin/manage users when all the items in the collection are private. 
 * Show a collection to read users only when the collection has at least one public item
 * Show to all users when collection is shared OR public
 *  
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@ReflectIgnoreClassFields(value = { "kalturaPlaylist" })
public class MediaCollection implements Serializable {

    public static String SHARING_PUBLIC = "public";
    public static String SHARING_SHARED = "shared";
    public static String SHARING_ADMIN = "admin";
    public static String SHARING_PRIVATE = "private";

    private static final long serialVersionUID = 1L;

    private String id; // The unique id for this collection (should match the playlist id)
    private String locationId; // Sakai site or group id
    private String ownerId; // Sakai userId
    private String title; // Display name for this collection
    private String description; // Description for this collection
    private Date dateCreated;
    private Date dateModified;
    private boolean hidden = false; // only visible to editors if true
    private String sharing = SHARING_ADMIN; // can be edited by any admin in course

    // non-persistent
    /**
     * Indicates this collection has at least one hidden item
     */
    private boolean hiddenItems = false;
    /**
     * Indicates this collection has at least one visible item
     */
    private boolean visibleItems = false;

    private List<MediaItem> items;
    /**
     * This is the item to use as the display item for the collection (null if there is not one),
     * will be public if a public item available
     */
    private MediaItem displayItem;


    // truncation settings
    transient int shortMaxName = 30;
    transient int shortMaxDesc = 80;
    transient int shortSpace = 6;

    // should be populated when the collection is fetched and constructed
    transient KalturaPlaylist kalturaPlaylist;
    public KalturaPlaylist getKalturaPlaylist() {
        return kalturaPlaylist;
    }
    public void setKalturaPlaylist(KalturaPlaylist kalturaPlaylist) {
        this.kalturaPlaylist = kalturaPlaylist;
    }

    public List<MediaItem> getItems() {
        return items;
    }

    public void setItems(List<MediaItem> items) {
        this.items = items;
        if (this.items != null) {
            // set the visible items flags for the current user
            for (MediaItem mediaItem : items) {
                if (mediaItem.isHidden()) {
                    if (!this.hiddenItems) {
                        this.hiddenItems = true;
                    }
                } else {
                    if (!this.visibleItems) {
                        this.visibleItems = true;
                    }
                    if (this.displayItem == null) {
                        this.displayItem = mediaItem;
                    }
                }
            }
            /* cannot set this or it is visible when user not in course
            if (this.isVisibleItems()) {
                this.setViewable(true);
            }*/
            // ensure the display item is populated
            if (this.displayItem == null && ! this.items.isEmpty()) {
                this.displayItem = this.items.get(0);
            }
        } else {
            this.items = null;
            this.displayItem = null;
            this.visibleItems = false;
            this.hiddenItems = false;
            this.viewable = false;
        }
    }

    /**
     * @return the media item that is appropriate as a representation of this collection
     * (most recently added public item - or private if none found)
     */
    public MediaItem getDisplayItem() {
        return displayItem;
    }

    // have to use Unpopulated because "empty" will not work in JSTL
    public boolean isUnpopulated() {
        boolean empty = true;
        if (this.items != null) {
            empty = this.items.isEmpty();
        }
        return empty;
    }

    /**
     * @return a list of all the kaltura ids for the items in this collection
     */
    public List<String> getKalturaIds() {
        ArrayList<String> kids = new ArrayList<String>();
        if (this.items != null) {
            for (MediaItem mi : this.items) {
                kids.add(mi.getKalturaId());
            }
        }
        return kids;
    }

    public MediaItem getItemById(String mid) {
        MediaItem item = null;
        if (this.items != null && mid != null) {
            for (MediaItem mi : this.items) {
                if (mi.getId().equals(mid)) {
                    item = mi;
                    break;
                }
            }
        }
        return item;
    }

    public MediaItem getByKalturaId(String kalturaId) {
        MediaItem item = null;
        if (this.items != null && kalturaId != null) {
            for (MediaItem mi : this.items) {
                if (mi.getKalturaId().equals(kalturaId)) {
                    item = mi;
                    break;
                }
            }
        }
        return item;
    }

    public String getIdStr() {
        if (this.id != null) {
            return this.id.toString();
        }
        return null;
    }

    public String getShortTitle() {
        return MediaItem.truncateText(getTitle(), shortMaxName, shortSpace);
    }

    public String getShortDescription() {
        return MediaItem.truncateText(getDescription(), shortMaxDesc, shortSpace);
    }

    /**
     * Allows inputing a metadata map to update the fields of this collection based on it
     * 
     * @param metadata a map of string -> string (as returned from {@link KalturaAPIService} metadata methods)
     *      NOTE: empty or null map will cause no changes to the current fields
     */
    public void updateFromMetadataMap(Map<String, String> metadata) {
        if (metadata != null) {
            this.hidden = StringUtils.equals(metadata.get("Hidden"), "1");
            if (metadata.containsKey("Type")){
                this.sharing = metadata.get("Type");
            }
            if (metadata.containsKey("Owner")) {
                this.ownerId = metadata.get("Owner");
            }
            if (metadata.containsKey("Title")) {
                this.title = metadata.get("Title");
            } else {
                this.title = kalturaPlaylist.name;
            }
        }
    }

    /**
     * Get the relevant fields out of this collection as map which is compatible with the format used for the Kaltura metadata methods
     * 
     * @return a map of the fields which are relevant to the Kaltura metadata handling methods in {@link KalturaAPIService}
     */
    public Map<String, String> extractMetadataMap() {
        // WARNING: if you change this, you have to also change the code in MediaService.createPlaylistMetadataMap()
        Map<String, String> metadata = new LinkedHashMap<String, String>();
        metadata.put("Title", this.title);
        metadata.put("Type", this.sharing);
        metadata.put("Hidden", this.hidden ? "1" : "0");
        metadata.put("Owner", this.ownerId);
        return metadata;
    }

    transient User owner;
    protected User getOwner() {
        return owner;
    }
    public void indicateOwner(User owner) {
        this.owner = owner;
    }
    public String getOwnerName() {
        String name = this.ownerId;
        if (owner != null) {
            name = owner.name;
        }
        return name;
    }
    public String getOwnerEmail() {
        String email = "";
        if (owner != null) {
            email = owner.email;
        }
        return email;
    }

    // PERM - for permissions wrangling in the models
    boolean control = false; // can the current user edit/remove the collection
    /**
     * Indicates if the current user has control over this collection
     */
    public boolean isControl() {
        return control;
    }

    String currentUsername = null;
    public String getCurrentUsername() {
        return currentUsername;
    }

    boolean addItems = false;
    /**
     * Indicates if the current user has the ability to add items to this collection
     */
    public boolean isAddItems() {
        return addItems;
    }
    /**
     * @param add true if the current user can add items to this collection
     */
    public void setAddItems(boolean add) {
        this.addItems = add;
        if (this.addItems) {
            this.viewable = true;
        }
    }

    boolean viewable = false;
    public boolean isViewable() {
        return viewable;
    }
    public void setViewable(boolean viewable) {
        this.viewable = viewable;
    }

    /**
     * 
     * @param username the current user eid (not the internal id)
     * @param control true if the current user can control this collection
     */
    public void indicateUserControl(String username, boolean control) {
        this.currentUsername = username;
        this.control = control;
        if (this.control) {
            this.viewable = true;
        }
    }
    // PERM - done

    /**
     * KalturaPlaylist constructor for Kaltura server-based collections
     * 
     * @param playlist the KalturaPlaylist object
     * @param locationId the location for this collection (e.g. /site/xxxx-xxx-xxx-x-xxxx)
     * @param metadata [OPTIONAL] the playlist's metadata HashMap
     */
    public MediaCollection(KalturaPlaylist playlist, String locationId, Map<String, String> metadata) {
        if (playlist == null) {
            throw new IllegalArgumentException("playlist must not be null");
        }
        if (locationId == null) {
            throw new IllegalArgumentException("locationId must not be null");
        }
        this.kalturaPlaylist = playlist;
        this.id = playlist.id;
        this.description = playlist.description;
        this.locationId = locationId;
        updateFromMetadataMap(metadata);
    }

    /**
     * for permissions setting
     * @param title title of the collection
     * @param type type of collection
     * @param hidden visibility of the collection
     * @param owner owner id of the collection
     */
    public MediaCollection(String title, String type, String hidden, String owner) {
        Map<String, String> metadata = new LinkedHashMap<String, String>();
        if (title != null) {
            metadata.put("Title", title);
        }
        if (type != null) {
            metadata.put("Type", type);
        }
        if (hidden != null) {
            metadata.put("Hidden", hidden);
        }
        if (owner != null) {
            metadata.put("Owner", owner);
        }
        updateFromMetadataMap(metadata);
    }

    /**
     * TESTING and PERMISSIONS ONLY
     * don't use this one in general
     */
    public MediaCollection(String id, String locationId, String ownerId, String title, boolean hidden, String sharing) {
        this.id = id;
        this.locationId = locationId;
        this.ownerId = ownerId;
        this.title = title;
        this.dateCreated = new Date();
        this.dateModified = this.dateCreated;
        this.hidden = hidden;
        if (sharing != null) {
            setSharing(sharing);
        }
    }

    @Override
    public String toString() {
        return "{MC:" + id 
                + ":s=" + sharing 
                + ":l=" + locationId 
                + ":t=" + title 
                + ":h=" + hidden  
                + ":ic=" + (items != null ? items.size() : "-1") 
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
        MediaCollection other = (MediaCollection) obj;
        if (id == null || other.id == null)  {
            return false;
        } else {
            return id.equals(other.id);
        }
    }

    /**
     * Getters and Setters
     */

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public boolean isHiddenItems() {
        return hiddenItems;
    }

    public void setHiddenItems(boolean hiddenItems) {
        this.hiddenItems = hiddenItems;
    }

    public boolean isVisibleItems() {
        return visibleItems;
    }

    public void setVisibleItems(boolean visibleItems) {
        this.visibleItems = visibleItems;
    }


    public static class CollectionTitleComparator implements Comparator<MediaCollection> {
        public int compare(MediaCollection mc0, MediaCollection mc1) {
            if (mc0.title == null) return 1;
            if (mc1.title == null) return -1;
            return mc0.title.compareTo(mc1.title);
        }
    }

}
