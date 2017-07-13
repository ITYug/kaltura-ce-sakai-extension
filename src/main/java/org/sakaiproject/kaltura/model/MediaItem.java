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
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.azeckoski.reflectutils.annotations.ReflectIgnoreClassFields;
import org.joda.time.DateTime;
import org.sakaiproject.kaltura.logic.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.kaltura.client.enums.KalturaMediaType;
import com.kaltura.client.types.KalturaMediaEntry;

/**
 * This is a kaltura media item, it represents a kaltura item in a collection
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@ReflectIgnoreClassFields(value = { "collection", "kalturaItem", "locationId", "type" })
public class MediaItem implements Serializable {

    public static final String METADATA_OWNER = "Owner";
    public static final String METADATA_REMIXABLE = "Remixable";
    public static final String METADATA_REUSABLE = "Reusable";
    public static final String METADATA_HIDDEN = "Hidden";
    public static final String XML_ITEM_KEY = "item";

    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_AUDIO = "audio";
    public static final String TYPE_VIDEO = "video";

    // storing value from sakai.properties
    public static boolean KALTURA_CLIPPING_DEFAULT_ALLOWED = false;

    private static final long serialVersionUID = 1L;

    private String id; // internal id (should match the kaltura entry id)
    private String locationId; // Sakai site or group reference
    private transient MediaCollection mediaCollection; // the containing collection (null if this is a library item)
    private String kalturaId; // kaltura entry id
    /** Sakai username (not id but eid) - this is the user who added the item to the container (collection, library, mymedia) */
    private String ownerId;
    /** Sakai username (not id but eid) - this is the user who uploaded the item to kaltura server (assumed to be ownerId if not set) */
    private String creatorId;
    /** indicates the position (ordering) in the collection, 1 is the first item */
    private Integer position;
    private Date dateCreated;
    private Date dateModified;
    private String type = null;
    private boolean hidden = true; // only visible to editors if is true, hidden by default (private)
    private boolean shared = false; // only shared use if this is true (reusable)
    private boolean remixable = false; // only remixable/clippable if this is true

    // non-persistent
    private String downloadKS; // Kaltura Session ID

    private String clipperId = "";
    private String clipperURL = "";

    // NOTE: transient fields ignored for output
    transient int kalturaPartnerId = 1111111;
    transient boolean media = false;
    transient boolean mix = false;
    /**
     * This is set to the id of the media item this one was duplicated from
     */
    transient String originalId;

    transient String kalturaCDN = "http://cdn.kaltura.com";
    transient KalturaMediaEntry kalturaItem;

    // truncation settings
    transient int shortMaxName = 16;
    transient int shortMaxDesc = 50;
    transient int shortSpace = 3;

    transient String clipperFlashVars = "";


    // PERM - for permissions wrangling in the models
    /**
     * Indicates if the current user can edit the item meta data (title, desc, etc.)
     */
    boolean edit = false;

    /**
     * Indicates if the current user can edit this item
     */
    public boolean isEdit() {
        return edit;
    }

    /**
     * Indicates if the current user has control over this item (can remove or rearrange it)
     */
    boolean control = false;

    /**
     * Indicates if the current user has control over this item (can remove or rearrange it)
     */
    public boolean isControl() {
        return control;
    }

    /**
     * Indicates the user is allowed to modify the hidden/shared/remix settings
     */
    boolean manage = false;

    /**
     * Indicates the user is allowed to modify the hidden/shared/remix settings
     */
    public boolean isManage() {
        return manage;
    }

    /**
     * @deprecated us isManage()
     */
    public boolean isAdminControl() {
        return manage;
    }

    /**
     * Indicate the control level a user has over a given item
     * 
     * @param username the user eid (username)
     * @param edit indicate if a user has edit meta data rights
     * @param control indicate if a user has control to remove/move this item (owned or granted)
     * @param manage indicate if the user can change (manage) the hidden/shared/remix settings
     */
    public void indicateUserControl(String username, boolean edit, boolean control, boolean manage) {
        this.currentUsername = username;
        this.edit = edit;
        if (this.isCreated()) {
            this.edit = true;
        }
        this.control = control;
        this.manage = manage;
        if (this.isOwned()) {
            this.control = true;
            this.manage = true;
        }
    }

    String currentUsername = null;

    /**
     * @return username the current user eid (username) who is operating on this item (may not be the owner or creator)
     */
    public String getCurrentUsername() {
        return currentUsername;
    }

    /**
     * @return true if the current user is the owner of this item OR false otherwise
     */
    public boolean isOwned() {
        boolean owned = false;
        if (this.ownerId != null && this.currentUsername != null) {
            owned = (this.ownerId.equals(this.currentUsername));
        }
        return owned;
    }

    /**
     * @return true if the current user is the creator of this item OR false otherwise
     */
    public boolean isCreated() {
        boolean created = false;
        if (this.getCreatorUserId() != null && this.currentUsername != null) {
            created = (this.getCreatorUserId().equals(this.currentUsername));
        }
        return created;
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
     * NOTE: requires item to be populated with permissions data
     * @return true if this item can be remixed/clipped OR false otherwise
     */
    public boolean isCanRemix() {
        return ((this.remixable || this.isCreated()) && (this.control || this.edit));
    }

    // PERM - done

    // SPECIAL Kaltura data

    // player data
    String playerId = "";
    String playerURL = "";
    String entryPlayerURL = "";
    String userPlayerURL = "";
    String playerJSURL = null; // default NULL
    int playerWidth = 480;
    int playerHeight = 360;

    public void setPlayerInfo(String playerId, String playerURL, String userPlayerURL, String playerJSURL, int playerWidth, int playerHeight, String entryPlayerURL) {
        if (StringUtils.isBlank(playerId)) {
            throw new IllegalArgumentException("playerId must be set");
        }
        this.playerId = playerId;
        if (StringUtils.isBlank(playerURL)) {
            throw new IllegalArgumentException("playerURL must be set");
        }
        this.playerURL = playerURL;
        if (userPlayerURL != null) {
            this.userPlayerURL = userPlayerURL;
        } else {
            this.userPlayerURL = playerURL;
        }
        if (entryPlayerURL != null) {
            this.entryPlayerURL = entryPlayerURL;
        } else {
            this.entryPlayerURL = playerURL;
        }
        if (playerJSURL != null) {
            this.playerJSURL = playerJSURL;
        }
        if (playerWidth > 0) {
            this.playerWidth = playerWidth;
        }
        if (playerHeight > 0) {
            this.playerHeight = playerHeight;
        }
    }

    public void setClippingInfo(String clipperId, String clipperURL, String clipperFlashVars) {
        this.clipperId = clipperId;
        this.clipperURL = clipperURL;
        this.clipperFlashVars = clipperFlashVars;
    }

    public void setKalturaPartnerId(int kalturaPartnerId) {
        this.kalturaPartnerId = kalturaPartnerId;
    }

    public void setKalturaCDN(String kalturaCDN) {
        this.kalturaCDN = kalturaCDN;
    }

    public KalturaMediaEntry getKalturaItem() {
        return kalturaItem;
    }

    public void setKalturaItem(KalturaMediaEntry kalturaItem) {
        this.kalturaItem = kalturaItem;
        if (kalturaItem != null) {
            if (this.id == null) {
                this.id = kalturaItem.id;
            }
            this.kalturaId = kalturaItem.id;
            this.creatorId = kalturaItem.creatorId;
            if (this.ownerId == null) {
                this.ownerId = kalturaItem.creatorId;
            }
            this.dateCreated = new Date(kalturaItem.createdAt * 1000l);
            this.dateModified = new Date(kalturaItem.updatedAt * 1000l);
        }
        if (this.type == null) {
            this.type = findType();
        }
    }

    public boolean isPopulated() {
        return kalturaItem != null;
    }

    public String getLocation() {
        if (this.locationId == null && this.mediaCollection != null) {
            return this.mediaCollection.getLocationId();
        }
        return this.locationId;
    }

    /**
     * @return the collection id if it is set
     */
    public String getCollectionId() {
        if (this.mediaCollection != null) {
            return this.mediaCollection.getId();
        }
        return null;
    }

    public String getIdStr() {
        if (this.id != null) {
            return this.id.toString();
        }
        return null;
    }

    public String getName() {
        String name = this.kalturaId;
        if (kalturaItem != null && kalturaItem.name != null) {
            name = kalturaItem.name;
        }
        return name;
    }

    public String getShortName() {
        return MediaItem.truncateText(getName(), shortMaxName, shortSpace);
    }

    public String getDesc() {
        String desc = "";
        if (kalturaItem != null && kalturaItem.description != null) {
            desc = kalturaItem.description;
        }
        return desc;
    }

    public String getShortDesc() {
        return MediaItem.truncateText(getDesc(), shortMaxDesc, shortSpace);
    }

    public float getDuration() {
        float duration = 0;
        if (kalturaItem != null) {
            duration = kalturaItem.duration;
            if (duration < 0) {
                duration = 0;
            }
        }
        return duration;
    }

    public Date getDate() {
        Date d = new Date();
        if (kalturaItem != null && kalturaItem.createdAt > 0) {
            long time = (long) kalturaItem.createdAt * 1000l;
            d = new Date(time);
        }
        return d;
    }

    public int getWidth() {
        int width = 400;
        if (kalturaItem != null) {
            width = kalturaItem.width;
            if (width < 0) {
                width = 0;
            }
        }
        return width;
    }

    public int getHeight() {
        int height = 300;
        if (kalturaItem != null) {
            height = kalturaItem.height;
            if (height < 0) {
                height = 0;
            }
        }
        return height;
    }

    public String findType() {
        String type = TYPE_VIDEO;
        if (kalturaItem != null) {
            KalturaMediaEntry kme = (KalturaMediaEntry) kalturaItem;
            if (KalturaMediaType.AUDIO.equals(kme.mediaType)) {
                type = TYPE_AUDIO;
            } else if (KalturaMediaType.IMAGE.equals(kme.mediaType)) {
                type = TYPE_IMAGE;
            }
        } else {
            if (this.type != null) {
                type = this.type;
            }
        }
        return type;
    }

    public String getMediaType() {
        return findType();
    }

    public String getThumbnail() {
        String url = this.kalturaCDN + "/p/" + this.kalturaPartnerId
                + "/thumbnail/width/120/height/90/entry_id/" + this.kalturaId;
        if (kalturaItem != null && kalturaItem.thumbnailUrl != null) {
            url = kalturaItem.thumbnailUrl;
        }
        return url;
    }

    /**
     * @return the URL which can be used to download the media content
     */
    public String getDownloadURL() {
        String url = "";
        if (!isOwned() && !isManage()) {
            // only managers and owners can see the download urls
        } else {
            /*
             * Instructions from Kaltura Download Button: currently the URL (which I guess you take from
             * the 'get' response) is something like:
             * http://cdnbakmi.kaltura.com/p/166762/sp/16676200/raw/entry_id/1_nkkzp8z4/version/0
             * concatenating '/file_name/1_nkkzp8z4' so the URL is actually adding the 'file_name'
             * parameter to the request which simply adds 'content-disposition' header to the response.
             * the 'content-disposition' is what forces the browser to open the 'open/save as' dialog.
             * after the concatenation the URL would look like:
             * http://cdnbakmi.kaltura.com/p/166762
             * /sp/16676200/raw/entry_id/1_nkkzp8z4/version/0/file_name/1_nkkzp8z4
             */
            // suffix forces the mime-type from kaltura server to be correct for download, it also forces a download KS to be included if available
            String suffix = "/file_name/" + this.kalturaId + (this.downloadKS != null ? "/ks/" + this.downloadKS : "");
            if (kalturaItem != null && kalturaItem.thumbnailUrl != null) {
                url = kalturaItem.downloadUrl + suffix;
            } else {
                url = this.kalturaCDN + "/p/" + this.kalturaPartnerId + "/raw/entry_id/" + this.kalturaId + suffix;
            }
        }
        return url;
    }

    public String[] getTags() {
        String[] tags = new String[0];
        if (kalturaItem != null && kalturaItem.tags != null) {
            tags = StringUtils.splitByWholeSeparator(kalturaItem.tags, ", ");
        }
        return tags;
    }

    User author;
    public User getAuthor() {
        return author;
    }
    public void indicateAuthor(User author) {
        this.author = author;
    }


    /**
     * Allows inputing a metadata map to update the fields of this item based on it
     * 
     * @param metadata a map of string -> string (as returned from {@link KalturaAPIService} metadata methods)
     *      NOTE: empty or null map will cause no changes to the current fields
     */
    public void updateFromMetadataMap(Map<String, String> metadata) {
        if (metadata != null) {
            this.hidden = StringUtils.equals(metadata.get(METADATA_HIDDEN), "H");
            this.shared = StringUtils.equals(metadata.get(METADATA_REUSABLE), "S");
            this.remixable = StringUtils.equals(metadata.get(METADATA_REMIXABLE), "R");
            this.ownerId = StringUtils.isNotEmpty(metadata.get(METADATA_OWNER)) ? metadata.get(METADATA_OWNER) : this.creatorId;
        }
    }

    /**
     * Get the relevant fields out of this item as map which is compatible with the format used for the Kaltura metadata methods
     * 
     * @return a map of the fields which are relevant to the Kaltura metadata handling methods in {@link KalturaAPIService}
     */
    public Map<String, String> extractMetaDataMap() {
        // WARNING: if you change this, you have to also change the code in MediaService.createEntryMetadataMap()
        Map<String, String> metadata = new LinkedHashMap<String, String>();
        metadata.put(METADATA_HIDDEN, this.hidden ? "H" : "h");
        metadata.put(METADATA_REUSABLE, this.shared ? "S" : "s");
        metadata.put(METADATA_REMIXABLE, this.remixable ? "R" : "r");
        metadata.put(METADATA_OWNER, this.ownerId);
        return metadata;
    }

    /**
     * Generate a metadata map from incoming media item data
     * @param hidden
     * @param reusable
     * @param remixable
     * @param ownerId the username of the owner
     * @return a map of the fields which are relevant to the Kaltura metadata handling methods in {@link KalturaAPIService}
     */
    public static Map<String, String> makeMetaDataMap(Boolean hidden, Boolean reusable, Boolean remixable, String ownerId) {
        MediaItem mi = new MediaItem(hidden, reusable, remixable, ownerId);
        return mi.extractMetaDataMap();
    }


    /**
     * Default constructor
     */
    protected MediaItem() {
        this.manage = false;
        this.control = false;
        this.dateCreated = new Date();
        this.dateModified = this.dateCreated;
        this.position = 0;
    }

    public MediaItem(String kalturaId, String locationId, MediaCollection collection, Map<String, String> metadata) {
        this();
        if (collection == null) {
            throw new IllegalArgumentException("collection cannot be null");
        }
        if (kalturaId == null) {
            throw new IllegalArgumentException("kalturaId cannot be null");
        }
        this.id = kalturaId;
        this.kalturaId = kalturaId;
        if (locationId != null) {
            this.locationId = locationId;
        } else {
            this.locationId = collection.getLocationId();
        }
        this.mediaCollection = collection;
        updateFromMetadataMap(metadata);
    }

    public MediaItem(KalturaMediaEntry entry, String locationId, MediaCollection collection, Map<String, String> metadata) {
        this();
        if (entry == null) {
            throw new IllegalArgumentException("entry cannot be null");
        }
        if (collection == null) {
            throw new IllegalArgumentException("collection cannot be null");
        }
        this.mediaCollection = collection;
        if (locationId != null) {
            this.locationId = locationId;
        } else {
            this.locationId = collection.getLocationId();
        }
        setKalturaItem(entry);
        updateFromMetadataMap(metadata);
    }

    public MediaItem(String locationId, KalturaMediaEntry entry, Map<String, String> metadata) {
        this();
        if (locationId == null) {
            throw new IllegalArgumentException("locationId cannot be null");
        }
        if (entry == null) {
            throw new IllegalArgumentException("entry cannot be null");
        }
        this.locationId = locationId;
        setKalturaItem(entry);
        updateFromMetadataMap(metadata);
    }

    public MediaItem(MediaCollection collection, KalturaMediaEntry entry, Map<String, String> metadata) {
        this();
        if (collection == null) {
            throw new IllegalArgumentException("collection cannot be null");
        }
        if (entry == null) {
            throw new IllegalArgumentException("entry cannot be null");
        }
        this.mediaCollection = collection;
        this.locationId = collection.getLocationId();
        setKalturaItem(entry);
        updateFromMetadataMap(metadata);
    }

    /**
     * for TESTING ONLY
     * 
     * @param locationId
     * @param kalturaId
     * @param ownerId
     * @param hidden
     * @param shared
     * @param remixable
     */
    public MediaItem(String locationId, String kalturaId, String ownerId, boolean hidden, boolean shared, boolean remixable) {
        this();
        // NOTE: location can be null for "my media" type items
        if (kalturaId == null) {
            throw new IllegalArgumentException("kalturaId cannot be null");
        }
        if (ownerId == null) {
            throw new IllegalArgumentException("ownerId cannot be null");
        }
        this.mediaCollection = null;
        this.id = kalturaId;
        this.kalturaId = kalturaId;
        this.locationId = locationId;
        this.ownerId = ownerId;
        this.creatorId = ownerId;
        this.hidden = hidden;
        this.shared = shared;
        this.remixable = remixable;
    }

    /**
     * for TESTING ONLY
     * 
     * @param mc
     * @param mi
     */
    public MediaItem(MediaCollection mc, MediaItem mi) {
        this();
        if (mi == null) {
            throw new IllegalArgumentException("mi cannot be null");
        }
        this.mediaCollection = mc;
        this.id = mi.getIdStr();
        this.locationId = mi.getLocationId();
        this.kalturaId = mi.getKalturaId();
        this.ownerId = mi.getOwnerId();
        this.creatorId = mi.getCreatorId();
        this.hidden = mi.isHidden();
        this.shared = mi.isShared();
        this.remixable = mi.isRemixable();
        this.position = mi.position; // copy true value
    }

    /**
     * WARNING: DO NOT USE THIS CONSTRUCTOR for anything other than generating XML
     * 
     * @param hidden
     * @param reusable
     * @param remixable
     * @param ownerId the username of the owner
     */
    private MediaItem(Boolean hidden, Boolean reusable, Boolean remixable, String ownerId) {
        this();
        Map<String, String> metadata = new LinkedHashMap<String, String>();
        if (hidden != null) {
            metadata.put(METADATA_HIDDEN, hidden ? "H" : "h");
        }
        if (reusable != null) {
            metadata.put(METADATA_REUSABLE, reusable ? "S" : "s");
        }
        if (remixable != null) {
            metadata.put(METADATA_REMIXABLE, remixable ? "R" : "r");
        }
        if (ownerId != null) {
            metadata.put(METADATA_OWNER, ownerId);
        }
        updateFromMetadataMap(metadata);
    }

    /**
     * Create a media item from a chunk of XML data
     * 
     * @param collection the collection this item should be part of
     * @param ownerId the ownerId (will override the value from the XML if set)
     * @param element the XML Dom element
     */
    public MediaItem(MediaCollection collection, String ownerId, Element element) {
        this();
        if (collection != null) {
            this.mediaCollection = collection;
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
        Element item = doc.createElement(XML_ITEM_KEY);
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
                + ":ip=" + (hidden ? "H":"h") + (shared ? "S":"s") + (remixable ? "R":"r")
                + ":prm=" + (control ? "C":"c") + (edit ? "E":"e") + (manage ? "M":"m") + (isOwned() ? "O":"o")
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
        MediaItem other = (MediaItem) obj;
        if (id == null || other.id == null)  {
            return false;
        } else {
            return id.equals(other.id); // use id only if set
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

    public String getKalturaId() {
        return kalturaId;
    }

    public void setKalturaId(String kalturaId) {
        this.kalturaId = kalturaId;
    }

    public MediaCollection getCollection() {
        return this.mediaCollection;
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

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerURL() {
        return playerURL;
    }

    public String getUserPlayerURL() {
        return userPlayerURL;
    }

    public boolean isUseHtml5Player() {
        return this.playerJSURL != null;
    }

    public String getPlayerJSURL() {
        return playerJSURL;
    }

    public int getPlayerWidth() {
        return playerWidth;
    }

    public int getPlayerHeight() {
        return playerHeight;
    }

    public void setDownloadKS(String downloadKS) {
        this.downloadKS = downloadKS;
    }

    public String getOriginalId() {
        return originalId;
    }

    public String getClipperId() {
        return clipperId;
    }

    public String getClipperURL() {
        return clipperURL;
    }

    public String getClipperFlashVars() {
        return clipperFlashVars;
    }

    public void setCollection(MediaCollection mc) {
        this.mediaCollection = mc;
    }


    public static class MediaItemComparator implements Comparator<MediaItem>, Serializable {
        static private final long serialVersionUID = 31L;
        public int compare(MediaItem o1, MediaItem o2) {
            int comparison = o1.getPosition().compareTo(o2.getPosition());
            if (comparison == 0 && o1.getId() != null && o2.getId() != null) {
                comparison = o1.getId().compareTo(o2.getId());
            }
            return comparison;
        }
    }

    public static class MediaItemNameComparator implements Comparator<MediaItem>, Serializable {
        static private final long serialVersionUID = 31L;
        public int compare(MediaItem o1, MediaItem o2) {
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

    public static class ItemDateComparator implements Comparator<MediaItem>, Serializable {
        static private final long serialVersionUID = 31L;
        public int compare(MediaItem o1, MediaItem o2) {
            if (o1.dateCreated == null) return 1;
            if (o2.dateCreated == null) return -1;
            return (int) (o1.dateCreated.getTime() - o2.dateCreated.getTime());
        }
    }

}
