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
package org.sakaiproject.kaltura.logic;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.genericdao.api.search.Order;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.kaltura.aspectj.ProfilerControl;
import org.sakaiproject.kaltura.aspectj.ProfilerControl.NoProfile;
import org.sakaiproject.kaltura.model.MediaCollection;
import org.sakaiproject.kaltura.model.MediaItem;
import org.sakaiproject.kaltura.model.MyMediaCollectionItem;
import org.sakaiproject.kaltura.model.MyMediaItem;

import com.kaltura.client.KalturaConfiguration;
import com.kaltura.client.enums.KalturaEntryStatus;
import com.kaltura.client.enums.KalturaMediaType;
import com.kaltura.client.types.KalturaBaseEntry;
import com.kaltura.client.types.KalturaCategory;
import com.kaltura.client.types.KalturaMediaEntry;
import com.kaltura.client.types.KalturaPlaylist;

/**
 * This is the implementation of the business logic for the kaltura app
 * 
 * @author azeckoski@unicon.net - Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class MediaService {
    // START replacement - DO NOT change this code block! -AZ
    // NOTE: this will be replaced at runtime from versions.properties
    public static String APP_VERSION = "20130509";
    public static String APP_BUILD_DATE = "20130509";
    public static String APP_SVN_REVISION = "123912";
    public static String APP_SVN_DATE = "?";
    // END replacement

    private static Log log = LogFactory.getLog(MediaService.class);

    private static final String METADATA_OWNER = MediaItem.METADATA_OWNER;
    private static final String METADATA_HIDDEN = MediaItem.METADATA_HIDDEN;
    private static final String METADATA_REUSABLE = MediaItem.METADATA_REUSABLE;
    private static final String METADATA_REMIXABLE = MediaItem.METADATA_REMIXABLE;

    boolean forceMediaNameOrdering = true; // set to false once the ordering UI is complete
    boolean showDeleteItemWarnings = false;
    boolean siteLibraryRequiresPrivs = false;
    boolean siteArchiveSupport = false;
    boolean migrationDisabled = false;


    String[] ALL_PERMS = new String[] {
            ExternalLogic.PERM_READ, //
            ExternalLogic.PERM_WRITE, //
            ExternalLogic.PERM_ADMIN, //
            ExternalLogic.PERM_MANAGER, //
            ExternalLogic.PERM_EDITOR, //
            ExternalLogic.PERM_SHOW_SITE_LIBRARY, //
            ExternalLogic.PERM_UPLOAD_SPECIAL, //
            ExternalLogic.PERM_SHOW_MY_MEDIA
    };

    /*
     * The kaltura configurable strings
     * Maybe these should just be strings in the i18n file?
     */
    String kalturaToolIntroInstructions = null;
    String kalturaEmptyCollectionsInstructions = null;

    private KalturaAPIService kalturaAPIService;
    private ExternalLogic external;
    private Ehcache entriesCache;
    @SuppressWarnings("deprecation")
    private org.sakaiproject.kaltura.dao.KalturaDao dao;
    private KalturaConfiguration kalturaConfig;

    /* Sample kaltura sakai.properties settings
   kaltura.enabled=true
   kaltura.partnerid=111111
   kaltura.adminsecret=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   kaltura.secret=YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY
   kaltura.endpoint=http://www.kaltura.com
     */

    @NoProfile
    public void init() {
        log.info("INIT");
        setAdminInternal(true);

        try {
            // load the versions file and relevant data
            try {
                Properties prop = new Properties();
                InputStream versionsInputStream = this.getClass().getClassLoader().getResourceAsStream("versions.properties");
                prop.load(versionsInputStream);
                versionsInputStream.close();
                APP_BUILD_DATE = prop.getProperty("build.timestamp");
                APP_VERSION = prop.getProperty("svn.timestamp");
                APP_SVN_REVISION = prop.getProperty("svn.revision");
                APP_SVN_DATE = prop.getProperty("svn.date");
                log.info("Versions data loaded: "+APP_VERSION+" - SVN: r"+APP_SVN_REVISION+" "+APP_SVN_DATE);
            } catch (IOException e) {
                // no versions could be loaded so log the warning
                log.warn("Unable to load versions.properties: "+e, e);
            }

            // supports the ability to customize the messages appearing to users when first using the tool
            this.kalturaToolIntroInstructions = external.getConfigurationSetting("kaltura.tool.introduction.instructions", null);
            this.kalturaEmptyCollectionsInstructions = external.getConfigurationSetting("kaltura.empty.collections.instructions", null);

            this.showDeleteItemWarnings = external.getConfigurationSetting("kaltura.showDeleteItemWarnings", this.showDeleteItemWarnings);
            this.siteLibraryRequiresPrivs = external.getConfigurationSetting("kaltura.siteLibraryRequiresPrivs", this.siteLibraryRequiresPrivs);
            this.siteArchiveSupport = external.getConfigurationSetting("kaltura.archive.support.enabled", this.siteArchiveSupport);
            if (this.siteArchiveSupport) {
                log.info("Kaltura archive support enabled");
            } else {
                log.info("Kaltura archive support disabled, use 'kaltura.archive.support.enabled = true' to enable");
            }

            if (!kalturaAPIService.isOfflineMode()) {
                this.migrationDisabled = external.getConfigurationSetting("kaltura.migration.disabled", this.migrationDisabled);
                if (this.migrationDisabled) {
                    log.warn("Kaltura ALERT\n" +
                        "  *******************************************************\n" +
                        "  ***** WARNING: Kaltura Migration is DISABLED *****\n" +
                        "  * (by the kaltura.migration.disabled config flag)\n" +
                        "  * This should NOT be disabled in PRODUCTION\n" +
                        "  * since it will mean existing user collections and\n" +
                        "  * item permissions will no longer be accessible.\n" +
                        "  * If you plan to not migrate older database data,\n" +
                        "  * you should remove all the content from the\n" +
                        "  * kaltura tables (KALTURA_COLL and KALTURA_ITEM).\n" +
                        "  *******************************************************\n"
                    );
                } else {
                    // execute the DB migration thread
                    startMigrationThread();
                }
            }

            // flush the cache
            entriesCache.flush();
        } finally {
            setAdminInternal(false);
        }

        ProfilerControl.enableProfiler();
    }

    public void destroy() {
        if (adminInternal != null) {
            adminInternal.remove();
        }
    }

    @NoProfile
    public void setForceMediaNameOrdering(boolean forceMediaNameOrdering) {
        this.forceMediaNameOrdering = forceMediaNameOrdering;
    }

    public String getKalturaCDN() {
        return kalturaAPIService.getKalturaCDN();
    }

    @NoProfile
    public boolean getShowKalturaDeleteItemWarnings() {
        return this.showDeleteItemWarnings;
    }

    /**
     * getSiteLibraryRequiresPrivs() returns whether the site tab is displayed for all
     * users (default) or if the user has to have privileges.
     * @return false if all users can see the Site table, true if the user must have
     * privileges
     */
    @NoProfile
    public boolean getSiteLibraryRequiresPrivs() {
        return this.siteLibraryRequiresPrivs;
    }


    // Kaltura service passthrough methods

    /**
     * Passthrough to kalturaAPIService
     */
    public void testWSInit() {
        this.kalturaAPIService.testWSInit();
    }

    @NoProfile
    public boolean isKalturaEnabled() {
        return kalturaAPIService.isKalturaEnabled();
    }

    @NoProfile
    public boolean isKalturaInitialized() {
        if (!kalturaAPIService.isKalturaInitialized()) {
            kalturaAPIService.init();
        }
        return kalturaAPIService.isKalturaInitialized();
    }

    @NoProfile
    public boolean isKalturaConfigured() {
        return kalturaAPIService.isKalturaConfigured();
    }

    /**
     * @return true if the kaltura clipping is enabled
     */
    @NoProfile
    public boolean isKalturaClippingEnabled() {
        return this.kalturaAPIService.isKalturaClippingEnabled();
    }

    /**
     * Finds the correct JS player to use
     * @param playerId [OPTIONAL] the numeric id of the kaltura player
     * @return the HTML5 JS player for a given player
     */
    @NoProfile
    public String findKalturaPlayerJSURL(String playerId) {
        return this.kalturaAPIService.findKalturaPlayerJSURL(playerId);
    }

    @NoProfile
    public void setOffline(boolean offlineMode) {
        this.kalturaAPIService.setOffline(offlineMode);
    }

    @NoProfile
    public boolean isOfflineMode() {
        return this.kalturaAPIService.isOfflineMode();
    }

    // SERVICES

    @NoProfile
    public void setKalturaAPIService(KalturaAPIService kalturaAPIService) {
        this.kalturaAPIService = kalturaAPIService;
        this.kalturaConfig = kalturaAPIService.getKalturaConfig();
    }

    @SuppressWarnings("deprecation")
    @NoProfile
    public void setDao(org.sakaiproject.kaltura.dao.KalturaDao dao) {
        this.dao = dao;
    }

    @NoProfile
    public void setExternal(ExternalLogic external) {
        this.external = external;
    }

    @NoProfile
    public void setEntriesCache(Ehcache entriesCache) {
        this.entriesCache = entriesCache;
    }

    /**
     * @return the configured intro instructions OR null if none are configured
     */
    @NoProfile
    public String getToolIntroInstructions() {
        return kalturaToolIntroInstructions;
    }

    /**
     * @return the configured empty collection instructions or null if none are configured
     */
    @NoProfile
    public String getEmptyCollectionsInstructions() {
        return kalturaEmptyCollectionsInstructions;
    }

    /**
     * Special threadlocal that allows us to run internal processes as an admin user even though no admin user
     * is currently available (basically allows direct access to the ADMIN KS even without a current sakai user).
     * Should be set TEMPORARILY and then reset back to false when no longer needed (typically in the same method that enabled it)
     */
    private static final ThreadLocal<Boolean> adminInternal = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };
    @NoProfile
    static boolean checkAdminInternal() {
        return adminInternal.get() == null ? false : adminInternal.get().booleanValue();
    }
    @NoProfile
    static void setAdminInternal(boolean enabled) {
        adminInternal.set(enabled);
    }


    // MAIN METHODS


    // POPULATE MEDIA

    /**
     * Populate the media item with data from kaltura
     * @param item a media item
     */
    public void populateItem(MediaItem item) {
        if (log.isDebugEnabled()) log.debug("populateItem(item="+item+")");
        if (item != null) {
            ArrayList<MediaItem> items = new ArrayList<MediaItem>();
            items.add(item);
            populateItems(items);
        }
    }

    /**
     * Get kaltura data for a set of media items and add it to the items
     * CACHE AWARE
     * 
     * @param mediaItems a set of media items
     */
    public void populateItems(List<MediaItem> items) {
        if (items != null && ! items.isEmpty()) {
            if (log.isDebugEnabled()) log.debug("populateItems(items="+items.size()+")");
            if (kalturaAPIService.isOfflineMode()) {
                log.warn("OFFLINE MODE");
                for (MediaItem mediaItem : items) {
                    KalturaMediaEntry entry = this.kalturaAPIService.makeSampleKME(mediaItem.getId());
                    entry.id = mediaItem.getKalturaId();
                    mediaItem.setKalturaItem(entry);
                }
            } else {
                // See if this stuff is in the cache already
                List<String> keids = makeKalturaIds(items);
                for (Iterator<String> iterator = keids.iterator(); iterator.hasNext();) {
                    String keid = iterator.next();
                    Element el = entriesCache.get(keid);
                    if (el != null) {
                        KalturaMediaEntry entry = (KalturaMediaEntry) el.getObjectValue();
                        if (entry != null) {
                            // skip cached items which are in a ready status (so refetch items which are not ready)
                            if (entry.status != KalturaEntryStatus.READY) {
                                // not ready so we leave it in the list
                                log.warn("Skipped over cached item with status: "+entry.status);
                                continue;
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("Retrieved "+entry.id+" from cache: "+entriesCache.getSize());
                            }
                            iterator.remove(); // take this one out of the list then
                            for (MediaItem mediaItem : items) {
                                if (mediaItem.getKalturaId().equals(entry.id)) {
                                    mediaItem.setKalturaItem(entry);
                                    // need to re-populate the media item data now that we know the type
                                    populateMediaItemData(mediaItem);
                                }
                            }
                        }
                    }
                }
                if (! keids.isEmpty()) {
                    // get kaltura items if not in the cache already
                    List<KalturaMediaEntry> kalturaItems = this.kalturaAPIService.getKalturaItems(null, keids.toArray(new String[keids.size()]), 0, 0);
                    // populate the items
                    for (KalturaMediaEntry kbe : kalturaItems) {
                        for (MediaItem mediaItem : items) {
                            if (mediaItem.getKalturaId().equals(kbe.id)) {
                                mediaItem.setKalturaItem(kbe);
                                // need to re-populate the media item data now that we know the type
                                populateMediaItemData(mediaItem);
                            }
                        }
                    }
                    // SPECIAL logging
                    if (log.isInfoEnabled()) {
                        List<String> kmeids = new ArrayList<String>();
                        for (KalturaBaseEntry kme : kalturaItems) {
                            kmeids.add(kme.id);
                        }
                        log.info("Populate KMEs found "+kalturaItems.size()+" items ("+kmeids+") of "+keids.size()+" from the list of "+items.size()+" ids ("+keids+")");
                    }
                }
            }
        }
    }

    /**
     * This will fill in data in the media item which is not available in the model,
     * the collection should ideally be set, checks permissions and fails if not allowed
     * 
     * This will NOT fetch the item data from the DB OR fetch kaltura data from the server
     * 
     * @param item the item to populate
     * @throws SecurityException if not allowed
     */
    public void populateMediaItemData(MediaItem item) {
        if (StringUtils.isNotBlank(item.getOwnerId()) && item.getAuthor() == null) {
            item.indicateAuthor( external.getUser(item.getOwnerId()) );
        }
        // no need to check if the current user id is set because check was already done
        if (item.getCurrentUsername() == null) {
            checkPermControlMI(item); // populate security data
        }
        this.kalturaAPIService.populateMediaItemKalturaData(item);
    }


     // MY MEDIA

    /**
     * Get the list of media item (from the kaltura server) for a given user,
     * optionally limit the number of items returned and filter by a search string
     * 
     * @param username [OPTIONAL] not the Sakai user id, the Sakai username which is the kaltura userId, 
     *      DEFAULT if null: current user username
     * @param search [OPTIONAL] a search filter to filter the items by,
     *      DEFAULT if null: return all items
     * @param start 0 for all, or >0 start with that item
     * @param max 0 for all, or >0 to only return that many
     * @return the List of kaltura entries
     * @throws IllegalArgumentException if the username is null and no current user exists
     */
    public List<MediaItem> getMyMedia(String username, String search, int start, int max) {
        if (username == null) {
            username = external.getCurrentUserName();
        }
        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("No username specified, cannot get media items");
        }
        if (start < 0) {
            start = 0;
        }
        if (max < 0) {
            max = 0;
        }
        if (log.isDebugEnabled()) log.debug("getMyMedia(username="+username+", search="+search+", start="+start+", max="+max+")");
        List<MediaItem> items = new ArrayList<MediaItem>();
        List<KalturaMediaEntry> kalturaEntries = this.kalturaAPIService.getKalturaItemsForUser(username, search, start, max);
        if (!kalturaEntries.isEmpty()) {
            for (KalturaMediaEntry kme : kalturaEntries) {
                // make up fake media items to send back (these do not exist in the DB anywhere)
                MyMediaItem item = makeMyMediaItem(username, kme);
                items.add(item);
            }
        }
        if (log.isDebugEnabled()) log.debug("getMyMedia : COMPLETE");
        return items;
    }

    /**
     * Construct a MyMedia item correctly based on the KME and user who is the creator
     * @param username username of the creator of this entry
     * @param userId the internal user ID of the creator of this entry
     * @param kme kaltura entry
     * @return MyMediaItem
     */
    private MyMediaItem makeMyMediaItem(String username, KalturaMediaEntry kme) {
        MyMediaItem item = new MyMediaItem(kme.id, username);
        item.setKalturaItem(kme); // avoids re-fetching the items later (in populateItems)
        boolean remixable = kalturaAPIService.kalturaClippingEnabled && kme.mediaType == KalturaMediaType.VIDEO;
        item.setRemixable(remixable);
        populateMediaItemData(item);
        return item;
    }

    /**
     * Get the collections the user might want to add a piece of media to in a site
     * 
     * @param kalturaId a kaltura item id
     * @param locationId a sakai location id
     * @return the list of collections (including the site library) that the user can add media to,
     *      includes indications if the item already exists in the collection
     */
    public List<MyMediaCollectionItem> getCollectionsForMyMedia(String kalturaId, String locationId) {
        if (log.isDebugEnabled()) log.debug("getCollectionsForMyMedia(kalturaId="+kalturaId+", locationId="+locationId+"");
        ArrayList<MyMediaCollectionItem> result = new ArrayList<MyMediaCollectionItem>();
        MyMediaCollectionItem mmci = new MyMediaCollectionItem();
        // Add the site library first (assuming it is visible)
        if (isSiteLibraryVisible(external.getCurrentUserId(), locationId)) {
            String siteLibraryTitle = external.getI18nMessage("app.site.library", null);
            mmci.setName(siteLibraryTitle);
            mmci.setUserHasWriteAccess( isKalturaWrite(external.getCurrentUserId(), locationId) );
            mmci.setId(locationId); // we have to use the actual full id here
            List<MediaItem> libraryItems = getLibrary(locationId, null, null, null, 0, 0);
            mmci.setContainsMediaItem( containsMediaItem(libraryItems, kalturaId) );
            result.add(mmci);
        }
        // Add the collections
        boolean admin = external.canAdministrateKalturaPermissions();
        List<MediaCollection> mediaCollections = getCollections(locationId, admin ? null : false, 0, null, 0, 0);
        for (MediaCollection mediaCollection : mediaCollections) {
            mmci = new MyMediaCollectionItem();
            mmci.setName(mediaCollection.getTitle());
            mmci.setId(mediaCollection.getIdStr());
            mmci.setShortName(mediaCollection.getShortTitle());
            mmci.setUserHasWriteAccess(mediaCollection.isAddItems());
            // check if this kalturaId is part of the playlist
            KalturaPlaylist kp = mediaCollection.getKalturaPlaylist();
            mmci.setContainsMediaItem( kp.playlistContent == null ? false : kp.playlistContent.contains(kalturaId) );
            // OLD way - required fetching all items - mmci.setContainsMediaItem( containsMediaItem(mediaCollection.getItems(), kalturaId) );
            result.add(mmci);
        }
        return result;
    }

    /**
     * Utility method
     * @return true if the supplied list contains a media item with the matching kalturaId
     */
    @NoProfile
    private boolean containsMediaItem(final List<MediaItem> mediaItems, final String kalturaIdToMatch) {
        for (MediaItem item : mediaItems) {
            if (item.getKalturaId().equals(kalturaIdToMatch)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fetch a My Media item for a given KME and User
     * @param kalturaId a kaltura item id
     * @param userId a Sakai internal user id (not the username)
     * @return a "MyMedia" MediaItem based on the supplied kaltura id
     * @throws SecurityException if this KME was not created by the user in question
     * @throws IllegalArgumentException if the user or kaltura ids are invalid
     */
    public MediaItem getMyMediaByKalturaId(String kalturaId, String userId) {
        KalturaMediaEntry kme = this.kalturaAPIService.getKalturaItem(kalturaId);
        if (kme == null) {
            throw new IllegalArgumentException("Cannot find the item with kaltura id: "+kalturaId);
        }
        User u = external.getUser(userId);
        if (u == null) {
            throw new IllegalArgumentException("Cannot find the user with id: "+userId);
        }
        String username = u.getUsername();
        MyMediaItem item = makeMyMediaItem(username, kme);
        if (!external.isUserAdmin(userId) && !StringUtils.equals(username, kme.creatorId)) {
            throw new SecurityException("Attempt to get My Media item for user ("+username+") not allowed since item creator ("+kme.creatorId+") does not match");
        }
        return item;
    }



    // COLLECTIONS

    public enum Filter {ALL, PUBLIC, SHARED, MINE, OWNED};

    /**
     * Get the library of media items for a given location
     * 
     * @param locationId the unique id of a location
     * @param filter [OPTIONAL] whether to fetch all items, public items, shared items, my items (public + owner), or my owned items only, DEFAULTs to just my items (MINE)
     * @param search [OPTIONAL] a search filter, will be matched against the items and used to limit the results returned
     * @param sort [OPTIONAL] the field to sort on (title, date (created) ), default is by date created reverse
     * @param start paging control, item to start with, 0 for first
     * @param max paging control, max items to return, 0 for all
     * @return the list of media items
     * @throws IllegalArgumentException if the params are invalid
     * @throws SecurityException if the user does not have permissions
     */
    public List<MediaItem> getLibrary(String locationId, Filter filter, String search, String sort, int start, int max) {
        if (locationId == null) {
            locationId = external.getCurrentLocationId();
        }
        if (locationId == null || ExternalLogic.NO_LOCATION.equals(locationId)) {
            throw new IllegalArgumentException("No location specified, cannot get library");
        }
        if (start < 0) {
            start = 0;
        }
        if (max < 0) {
            max = 0;
        }
        if (log.isDebugEnabled()) log.debug("getLibrary(locationId="+locationId+", filter="+filter+", search="+search+", sort="+sort+", start="+start+", max="+max+")");
        String currentUserId = external.getCurrentUserId();
        // fix up the permissions
        if ( external.isUserAdmin(currentUserId) 
                || external.checkPerms(currentUserId, locationId, new String[] {ExternalLogic.PERM_MANAGER, ExternalLogic.PERM_EDITOR}) ) {
            filter = Filter.ALL; // managers, editors and super admins can ALWAYS see all the items
        } else {
            // fix permissions for normal users
            if (filter == null) {
                if ( external.checkPerms(currentUserId, locationId, new String[] {ExternalLogic.PERM_READ, ExternalLogic.PERM_ADMIN}) ) {
                    filter = Filter.MINE;
                }
            }
        }
        // default to owned only
        if (filter == null) {
            filter = Filter.OWNED;
        }
        ArrayList<MediaItem> items = new ArrayList<MediaItem>();
        KalturaCategory libraryCategory = kalturaAPIService.getSiteCategory(locationId);
        List<KalturaMediaEntry> entries = kalturaAPIService.getKalturaItemsForCategory(libraryCategory.id, start, max);
        if (!entries.isEmpty()) {
            // extract the entry ids into an array
            String[] entryIds = new String[entries.size()];
            for (int i = 0; i < entryIds.length; i++) {
                entryIds[i] = entries.get(i).id;
            }
            Map<String, Map<String, String>> entriesMetadata = kalturaAPIService.getMetadataForEntry(libraryCategory.name, entryIds);
            // convert to media items
            for (KalturaMediaEntry entry : entries) {
                Map<String, String> metadata = entriesMetadata.get(entry.id);
                MediaItem item = new MediaItem(locationId, entry, metadata);
                // fill in the permissions
                item.indicateUserControl(null, false, false, false); // RESET, will be set correctly later
                populateMediaItemData(item);
                // double check viewing permissions on items
                if (canViewMI(item, null)) {
                    items.add(item);
                }
            }
            items = applyFilter(items, filter);
            // sort the media items - TODO allow sort by other fields?
            Collections.sort(items, new MediaItem.ItemDateComparator());
        }
        return items;
    }

    /**
     * Apply the supplied filter to the list. null filter implies no filtering.
     * 
     * @param items
     * @param filter
     * @param currentUserId
     * @return
     */
    private ArrayList<MediaItem> applyFilter(ArrayList<MediaItem> items, Filter filter) {
        ArrayList<MediaItem> result = new ArrayList<MediaItem>();
        String username = external.getCurrentUserName();
        switch (filter) {
        case OWNED:
            for (MediaItem mi : items) {
                if (username.equals(mi.getOwnerId())) {
                    result.add(mi);
                }
            }
            break;
        case PUBLIC:
            for (MediaItem mi : items) {
                if (username.equals(mi.getOwnerId()) || !mi.isHidden()) {
                    result.add(mi);
                }
            }
            break;
        case SHARED:
            for (MediaItem mi : items) {
                if (username.equals(mi.getOwnerId()) || mi.isShared()) {
                    result.add(mi);
                }
            }
            break;
        // MINE = has PERM_READ or PERM_ADMIN
        // the items were already checked for this in canViewMI(), so if this is the filter, we let the whole list through.
        case MINE:
        case ALL:
        default:
            result = items;
            break;
        }
        return result;
    }

    /**
     * Gets the list of all collections for the current user in a given location,
     * can optionally include items and add a search filter or limit the number of collections shown
     * 
     * @param locationId the location to get the collections for
     * @param hidden whether to fetch hidden collections, null = all, true = hidden only, false = visible only
     * @param includeItems number of items to fetch for each collection (0 means none, <0 for all)
     * @param query the search filter for collection title 
     * @param start paging control, item to start with
     * @param max paging control, max items to return
     * @return the list of collections
     * @throws IllegalArgumentException if the params are invalid
     * @throws SecurityException if the user does not have permissions
     */
    public List<MediaCollection> getCollections(String locationId, Boolean hidden, int includeItems, String query, int start, int max) {
        if (locationId == null) {
            locationId = external.getCurrentLocationId();
        }
        if (StringUtils.isEmpty(locationId) || ExternalLogic.NO_LOCATION.equals(locationId)) {
            throw new IllegalArgumentException("No location available ("+locationId+"), cannot retrieve collections");
        }
        if (log.isDebugEnabled()) log.debug("getCollections(locationId="+locationId+", hidden="+hidden+", includeItems="+includeItems+", query="+query+", start="+start+", max="+max+")");
        String currentUserId = external.getCurrentUserId();
        String currentUserEid = external.getCurrentUserName();
        if (start < 0) {
            start = 0;
        }
        if (max < 0) {
            max = 0;
        }
        // TODO handle this case? - start
        // TODO handle this case? - max
        // only respect the hidden setting if the user can't see all items
        if (! external.checkPerms(currentUserId, locationId, new String[] {ExternalLogic.PERM_ADMIN, ExternalLogic.PERM_MANAGER})) {
            hidden = false;
        }
        if (StringUtils.isNotBlank(query)) {
            // TODO handle this case?
        }
        List<MediaCollection> collections;
        /* Request Optimization
         * NOTE: category:list is OK
         * 
         * ORIGINAL:
         * [category:list, category:list, 
         * playlist:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list, 
         * playlist:get, media:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list,  metadata_metadata:list, metadata_metadata:list, metadata_metadata:list,
         * playlist:get, media:list, metadata_metadata:list, metadata_metadata:list, 
         * playlist:get, 
         * playlist:get, media:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list,  
         * playlist:get, media:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list, 
         * playlist:get, media:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list,  metadata_metadata:list, 
         * playlist:get, media:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list, metadata_metadata:list, 
         * playlist:get, media:list, metadata_metadata:list,  metadata_metadata:list, metadata_metadata:list, metadata_metadata:list,
         * ]
         * 
         * NEW: (first time includes: [category:list, category:list]
         * [playlist:list, metadata_metadata:list, media:list, metadata_metadata:list]
         */
        KalturaCategory siteCategory = kalturaAPIService.getSiteCategory(locationId);
        List<KalturaPlaylist> collPlaylists = kalturaAPIService.getPlaylistsInCategoryIds(siteCategory.id+"");
        String[] playlistIds = new String[collPlaylists.size()];
        int i = 0;
        for (KalturaPlaylist playlist : collPlaylists) {
            playlistIds[i++] = playlist.id;
        }
        collections = new ArrayList<MediaCollection>(collPlaylists.size());
        Map<String, Map<String, String>> playlistMetadata = kalturaAPIService.getPlaylistMetadataFields(playlistIds);
        for (KalturaPlaylist playlist : collPlaylists) {
            Map<String, String> metadata = playlistMetadata.get(playlist.id);
            MediaCollection mc = new MediaCollection(playlist, locationId, metadata);
            collections.add(mc);
        }
        // this block populates the collection lists with actual items from these collections
        if (! collections.isEmpty()) {
            // order the collections by title
            Collections.sort(collections, new MediaCollection.CollectionTitleComparator());
            // perm check and filter the collections to only those visible
            boolean viewAllMC = external.checkPerms(currentUserId, locationId, 
                    new String[] {ExternalLogic.PERM_ADMIN, ExternalLogic.PERM_MANAGER, ExternalLogic.PERM_EDITOR});
            for (Iterator<MediaCollection> cit = collections.iterator(); cit.hasNext();) {
                MediaCollection mc = cit.next();
                // handling the "hidden" param inputs
                if (hidden == null) {
                    // NOTE: special case - hidden=null means include all collections
                } else if (Boolean.TRUE.equals(hidden) && !mc.isHidden()) {
                    cit.remove();
                    continue; // SHORT CIRCUIT - no need to do perms check
                } else if (Boolean.FALSE.equals(hidden) && mc.isHidden()) {
                    cit.remove();
                    continue; // SHORT CIRCUIT - no need to do perms check
                }
                boolean adminMC = false;
                try {
                    checkPermControlMC(mc); // populate the perms
                    adminMC = true;
                } catch (Exception e) {
                    mc.indicateUserControl(currentUserEid, false);
                }
                // SKE-63 SKE-64 SKE-65 collection visibility
                if (!mc.isViewable() && viewAllMC) {
                    mc.setViewable(true);
                }
                // handling the hidden collection permissions check
                if ( mc.isHidden() && !adminMC && !mc.getOwnerId().equals(currentUserEid)) {
                    // remove hidden collection if the current user cannot view it
                    cit.remove();
                }
            }

            // this will fill in the items if any collections are left
            if (includeItems != 0) {
                // get all the items for all collections we found so we can check permissions

                // NEW WAY - OPTIMIZED - fetch all items and all metadata in one big chunk
                HashMap<String,List<KalturaMediaEntry>> collIdToEntriesMap = new HashMap<String, List<KalturaMediaEntry>>();
                // get all the entry ids
                String[] keids = new String[0];
                for (MediaCollection mc : collections) {
                    KalturaPlaylist playlist = mc.getKalturaPlaylist();
                    if (playlist.playlistContent != null) {
                        String[] playlistEntries = StringUtils.splitByWholeSeparator(playlist.playlistContent, ",");
                        if (playlistEntries.length > 0) {
                            keids = (String[]) ArrayUtils.addAll(keids, playlistEntries);
                        }
                    }
                }
                // fetch all the entries by id
                List<KalturaMediaEntry> entries = kalturaAPIService.getKalturaItems(null, keids, 0, -1);
                LinkedHashMap<String,KalturaMediaEntry> entryIdToEntryMap = new LinkedHashMap<String, KalturaMediaEntry>(collections.size());
                // map the entries to each collection they are from
                for (KalturaMediaEntry kme : entries) {
                    entryIdToEntryMap.put(kme.id, kme);
                }
                for (MediaCollection mc : collections) {
                    KalturaPlaylist playlist = mc.getKalturaPlaylist();
                    ArrayList<KalturaMediaEntry> collEntries;
                    if (playlist.playlistContent != null) {
                        String[] playlistEntries = StringUtils.splitByWholeSeparator(playlist.playlistContent, ",");
                        if (playlistEntries.length > 0) {
                            collEntries = new ArrayList<KalturaMediaEntry>(playlistEntries.length);
                            // create the entry to collection mapping
                            for (String entryId : playlistEntries) {
                                KalturaMediaEntry kme = entryIdToEntryMap.get(entryId);
                                if (kme != null) {
                                    collEntries.add(kme);
                                }
                            }
                        } else {
                            collEntries = new ArrayList<KalturaMediaEntry>(0);
                        }
                    } else {
                        collEntries = new ArrayList<KalturaMediaEntry>(0);
                    }
                    collIdToEntriesMap.put(mc.getId(), collEntries);
                }
                List<MediaItem> items = new ArrayList<MediaItem>();
                // put playlist ids into a set
                HashSet<String> playlistIdsSet = new HashSet<String>(playlistIds.length);
                for (int j = 0; j < playlistIds.length; j++) {
                    playlistIdsSet.add(playlistIds[j]);
                }
                // fetch all metadata in a single call
                Map<String, Map<String, Map<String, String>>> playlistEntriesMetadata = kalturaAPIService.getMetadataForContainersEntries(playlistIdsSet, keids);
                // generate the default set of metadata permissions for when they do not exist
                Map<String, String> defaultMetadata = kalturaAPIService.decodeMetadataPermissions(null, false);
                for (MediaCollection mc : collections) {
                    KalturaPlaylist playlist = mc.getKalturaPlaylist();
                    List<KalturaMediaEntry> collEntries = collIdToEntriesMap.get(mc.getId());
                    mc.setItems( new ArrayList<MediaItem>(collEntries.size()) );
                    if (!collEntries.isEmpty()) {
                        String[] collKeids = new String[collEntries.size()];
                        int j = 0;
                        for (KalturaMediaEntry kme : collEntries) {
                            collKeids[j++] = kme.id;
                        }
                        // populate all entries metadata for this playlist
                        Map<String, Map<String, String>> entriesMetadata = playlistEntriesMetadata.get(playlist.id); //kalturaAPIService.getMetadataForEntry(playlist.id, collKeids);
                        for (KalturaMediaEntry kme : collEntries) {
                            Map<String, String> metadata;
                            if (entriesMetadata.containsKey(kme.id)) {
                                metadata = entriesMetadata.get(kme.id);
                            } else {
                                metadata = defaultMetadata;
                            }
                            items.add( new MediaItem(kme, null, mc, metadata) );
                        }
                    }
                }

                /* OLD WAY - fetch items for collection and metadata for items
                List<MediaItem> items = new ArrayList<MediaItem>();
                for (MediaCollection mc : collections) {
                    //KalturaPlaylist playlist = kalturaAPIService.getPlaylistByPlaylistId(mc.getIdStr());
                    KalturaPlaylist playlist = mc.getKalturaPlaylist();
                    mc.setItems( new ArrayList<MediaItem>() );
                    if (playlist.playlistContent != null) {
                        String[] keids = StringUtils.splitByWholeSeparator(playlist.playlistContent, ",");
                        // fetch all entries and metadata for this playlist
                        List<KalturaMediaEntry> entries = kalturaAPIService.getKalturaItems(null, keids, 0, -1);
                        Map<String, Map<String, String>> entriesMetadata = kalturaAPIService.getMetadataForEntry(playlist.id, keids);
                        for (KalturaMediaEntry kme : entries) {
                            Map<String, String> metadata = entriesMetadata.get(kme.id);
                            items.add( new MediaItem(kme, null, mc, metadata) );
                        }
                    }
                }
                */
                // build the hash of items in collections
                HashMap<String, List<MediaItem>> m = new HashMap<String, List<MediaItem>>();
                for (MediaItem item : items) {
                    // exclude or populate this item
                    MediaCollection mc = item.getCollection();
                    String cid = mc.getId();
                    if (mc != null) {
                        item.setCollection(mc);
                        item.indicateUserControl(null, false, false, false); // RESET, will be set correctly later
                        populateMediaItemData(item);
                        if (! canViewMI(item, mc)) {
                            if (item.isHidden()) {
                                // this item is not visible so indicate there are hidden items and skip it
                                mc.setHiddenItems(true);
                            }
                            continue; // SKIP
                        }
                    }
                    if (! m.containsKey(cid)) {
                        m.put(cid, new ArrayList<MediaItem>());
                    }
                    m.get(cid).add(item);
                }
                ArrayList<MediaItem> displayItems = new ArrayList<MediaItem>();
                for (MediaCollection collection : collections) {
                    List<MediaItem> l = m.get(collection.getId());
                    if (l != null) {
                        if (includeItems > 0) {
                            // trim the list size
                            int maxList = includeItems;
                            if (maxList > l.size()) {
                                maxList = l.size();
                            }
                            l = l.subList(0, maxList);
                        }
                        collection.setItems(l);
                        displayItems.addAll(collection.getItems());
                        populateCollectionOwner(collection);
                    }
                }
                // finally we need to populate the kme data into the items
                populateItems(displayItems);
                /* SKE-63 SKE-64 SKE-65 collection visibility
                 * Last chance to be able to view the collection,
                 * if it is not set to viewable yet it means the user doesn't have special permissions so 
                 * they can only view it if there is a visible item in it
                 */
                for (MediaCollection collection : collections) {
                    // non-admin collection visibility is limited to collections with public items in them
                    if (collection.isViewable()) {
                        if (!collection.isVisibleItems() 
                                && !viewAllMC 
                                && !(collection.isControl() || collection.isAddItems()) ) {
                            collection.setViewable(false);
                        }
                    }
                }
            }
        }
        if (log.isDebugEnabled()) log.debug("getCollections : COMPLETE");
        return collections;
    }

    /**
     * Populates the collection owner data
     * @param collection a collection
     */
    private void populateCollectionOwner(MediaCollection collection) {
        if (collection != null && collection.getOwnerId() != null) {
            User cOwner = external.getUser(collection.getOwnerId());
            if (cOwner != null) {
                collection.indicateOwner(cOwner);
            }
        }
    }

    /**
     * Get a collection by its unique identifier,
     * also gets the items in the collection as desired
     * 
     * @param collectionId the unique id for the given collection (typically the playlist id)
     * @param start if <= 0 then start with first item, if a positive number then starts with that item (returns none if greater than the number of items)
     * @param max if 0 then only the collection is returned, if -1 then all items, if a positive number then return is limited to that many items
     * @return the collection OR null if none can be found
     * @throws IllegalArgumentException if the params are invalid
     * @throws SecurityException if the user does not have permissions
     */
    public MediaCollection getCollection(String collectionId, int start, int max) {
        if (collectionId == null) {
            throw new IllegalArgumentException("collectionId must not be null");
        }
        if (start < 0) {
            start = 0;
        }
        if (max < 0) {
            max = -1;
        }
        if (log.isDebugEnabled()) {
            log.debug("getCollection(playlistId=" + collectionId + ", start=" + start + ", max=" + max + ")");
        }
        MediaCollection mc = findMediaCollectionById(collectionId, true);
        if (mc != null) {
            String currentUserEid = external.getCurrentUserName();
            boolean adminMC = false;
            try {
                checkPermControlMC(mc); // populate the perms
                adminMC = true;
                mc.setViewable(true);
            } catch (Exception e) {
                mc.indicateUserControl(currentUserEid, false);
            }
            if (mc.isHidden() && !adminMC && !mc.getOwnerId().equals(currentUserEid)) {
                throw new SecurityException("user ("+currentUserEid+") cannot view collection ("+collectionId+")");
            }
            populateCollectionOwner(mc);
            if (max == 0) {
                mc.setItems(null);
            } else {
                List<MediaItem> items = new ArrayList<MediaItem>();
                KalturaPlaylist playlist = mc.getKalturaPlaylist();
                if (playlist.playlistContent != null) {
                    String[] kalturaIds = StringUtils.split(playlist.playlistContent, ",");
                    // fetch all entries and metadata for this playlist
                    List<KalturaMediaEntry> entries = kalturaAPIService.getKalturaItems(null, kalturaIds, 0, -1);
                    Map<String, Map<String, String>> entriesMetadata = kalturaAPIService.getMetadataForEntry(playlist.id, kalturaIds);
                    for (KalturaMediaEntry kme : entries) {
                        Map<String, String> metadata = entriesMetadata.get(kme.id);
                        MediaItem mi = new MediaItem(mc, kme, metadata);
                        items.add(mi);
                    }
                }
                mc.setItems(items); // this will set the hidden items flag on the collection
                // populate mi data and remove hidden items
                for (Iterator<MediaItem> iterator = items.iterator(); iterator.hasNext();) {
                    MediaItem item = iterator.next();
                    item.setCollection(mc);
                    item.indicateUserControl(null, false, false, false); // RESET, will be set correctly later
                    populateMediaItemData(item); // do this after the collection is trimmed?
                    if (! canViewMI(item, mc) ) {
                        iterator.remove();
                    }
                }

                // HANDLE PAGING
                if (max > 0 || start > 0) {
                    // limit the total number returned
                    int startList = 0;
                    int maxList = max;
                    if (start > 0) {
                        startList = start;
                        maxList += startList;
                    } else {
                        startList = 0;
                    }
                    // adjust the maxlist based on startlist
                    if (startList > items.size()) {
                        // return no items
                        startList = 0;
                        maxList = 0;
                    } else {
                        // limit the returned items by start and/or max
                        if (maxList < 0 || maxList > items.size()) {
                            maxList = items.size();
                        }
                    }
                    items = items.subList(startList, maxList);
                }

                populateItems(items); // populate kaltura meta data
                mc.setItems(items); // set the items with the correct set
            }
        }
        return mc;
    }

    /**
     * finds a MediaCollection object by its id
     * NOTE: replaces the previous DAO method
     * 
     * @param collectionId the id of the MediaCollection object (normally the playlist id)
     * @param includeMetadata if true then include the metadata for this collection
     * @return the MediaCollection object OR null if collection is not found
     * @throws IllegalArgumentException if the input data is invalid
     */
    private MediaCollection findMediaCollectionById(String collectionId, boolean includeMetadata) {
        if (collectionId == null) {
            throw new IllegalArgumentException("collectionId must not be null");
        }
        MediaCollection mediaCollection = null;
        KalturaPlaylist playlist = kalturaAPIService.getPlaylistByPlaylistId(collectionId);
        if (playlist != null && playlist.categoriesIds != null) {
            String locationId = findLocationFromPlaylist(playlist);
            if (locationId != null) {
                Map<String, String> metaData = null;
                if (includeMetadata) {
                    metaData = kalturaAPIService.getPlaylistMetadataFields(collectionId).get(collectionId);
                }
                mediaCollection = new MediaCollection(playlist, locationId, metaData);
            }
        }
        return mediaCollection;
    }

    /**
     * finds the locationId from a KalturaPlaylist
     * 
     * @param playlist the kaltura playlist
     * @return the locationId (e.g. /site/xxxxx-xx-xxx-xxxx)
     */
    private String findLocationFromPlaylist(KalturaPlaylist playlist) {
        if (playlist == null) {
            throw new IllegalArgumentException("playlist must not be null");
        }
        String locationId = null;
        if (playlist != null && playlist.categoriesIds != null) {
            KalturaCategory category = kalturaAPIService.getKalturaCategoryById(playlist.categoriesIds);
            if (category != null) {
                locationId = external.fixLocationId(category.name);
            }
        }
        return locationId;
    }


    /**
     * Add a new collection to the location (site) indicated
     * 
     * @param title the title for this collection
     * @param locationId the unique id of the location to place this collection in
     * @return the newly created media collection
     * @throws IllegalArgumentException  if the params are invalid
     * @throws SecurityException if the user does not have permissions
     */
    public MediaCollection addCollection(String title, String locationId) {
        if (locationId == null) {
            locationId = external.getCurrentLocationId();
        }
        if (StringUtils.isBlank(locationId)) {
            throw new IllegalArgumentException("location must be set");
        }
        String ownerId = checkPermOrException(ExternalLogic.PERM_ADMIN, locationId);
        if (StringUtils.isBlank(ownerId)) {
            throw new IllegalArgumentException("ownerId must be set");
        }
        if (StringUtils.isBlank(title)) {
            throw new IllegalArgumentException("title must be set");
        }
        if (log.isDebugEnabled()) log.debug("addCollection(title=" + title + ", locationId=" + locationId + ")");
        title = StringUtils.abbreviate(title, 250); // max length 255
        MediaCollection mc = new MediaCollection(title, null, "1", null);
        Map<String, String> metadata = mc.extractMetadataMap();
        KalturaCategory siteCat = kalturaAPIService.getSiteCategory(locationId);
        KalturaPlaylist kp = kalturaAPIService.getOrAddKalturaPlaylist(siteCat.id, title, metadata);
        mc = new MediaCollection(kp, locationId, metadata);
        mc.setItems( new ArrayList<MediaItem>(0) );
        log.info("User ("+external.getCurrentUserId()+") added collection ("+mc.getId()+", "+mc.getTitle()+") to location ("+mc.getLocationId()+"): " + mc);
        return mc;
    }

    /**
     * Allows title and hidden to be set for a collection,
     * note that the collection MUST exist and you must have permissions
     * 
     * @param collection the collection to save
     * @return the updated collection
     */
    public MediaCollection updateCollection(MediaCollection collection) {
        if (collection == null) {
            throw new IllegalArgumentException("collection must not be null");
        }
        if (collection.getId() == null) {
            throw new IllegalArgumentException("collection ("+collection+") must be a persistent object");
        }
        MediaCollection mc = findMediaCollectionById(collection.getId(), true);
        if (log.isDebugEnabled()) log.debug("updateCollection(collection="+collection+")");
        // owner can always edit - check after fetching since we don't trust the data we are given
        checkPermControlMC(mc);
        // update playlist metadata
        KalturaPlaylist playlist = kalturaAPIService.getPlaylistByPlaylistId(collection.getIdStr());
        if (playlist == null) {
            throw new RuntimeException("playlist cannot be null");
        }
        playlist.description = collection.getDescription();
        playlist.name = collection.getTitle();
        kalturaAPIService.saveUpdatedKalturaPlaylist(playlist);
        kalturaAPIService.updatePlaylistMetadata(collection.getId(), collection.extractMetadataMap());
        log.info("User ("+external.getCurrentUserId()+") updated collection ("+mc.getId()+", "+mc.getTitle()+") in location ("+mc.getLocationId()+"): " + mc);
        return mc;
    }

    /**
     * Removes a collection and all accompanying items completely
     * 
     * @param collectionId this is the unique id for the collection
     * @return true if the collection was found and removed OR false otherwise
     * @throws IllegalArgumentException if the params are invalid
     * @throws SecurityException is the user is not allowed to remove this collection
     */
    public boolean removeCollection(String collectionId) {
        if (collectionId == null) {
            throw new IllegalArgumentException("collectionId must not be null");
        }
        if (log.isDebugEnabled()) log.debug("removeCollection(collectionId="+collectionId+")");
        MediaCollection mc = findMediaCollectionById(collectionId, true);
        if (mc != null) {
            checkPermControlMC(mc);
            kalturaAPIService.deleteKalturaPlaylist(collectionId);
            log.info("User ("+external.getCurrentUserId()+") removed collection ("+mc.getId()+", "+mc.getTitle()+") from location ("+mc.getLocationId()+")");
            return true;
        }
        return false;
    }



    // MEDIA ITEMS

    /**
     * Retrieve a media item for a collection (only if it exists in the collection)
     * 
     * @param kalturaEntryId the kaltura entry id of this media item
     * @param collectionId
     * @param populate
     * @return a populated media item OR null if none is found with the given id in the given collection
     * @throws IllegalArgumentException if the params are invalid
     * @throws SecurityException if the user does not have permissions
     */
    public MediaItem getCollectionMediaItem(String kalturaEntryId, String collectionId, boolean populate) {
        if (StringUtils.isEmpty(collectionId)) {
            throw new IllegalArgumentException("collectionId must not be null");
        }
        if (log.isDebugEnabled()) {
            log.debug("getCollectionMediaItem(keid=" + kalturaEntryId + ", cid=" + collectionId + ", populate=" + populate + ")");
        }
        KalturaPlaylist kp = kalturaAPIService.getPlaylistByPlaylistId(collectionId);
        if (kp == null) {
            throw new RuntimeException("kaltura playlist doesn't exist");
        }
        String locationId = findLocationFromPlaylist(kp);
        Map<String, String> plMetadata = kalturaAPIService.getPlaylistMetadataFields(kp.id).get(collectionId);
        MediaCollection mc = new MediaCollection(kp, locationId, plMetadata);
        return getCollectionMediaItem(kalturaEntryId, mc, populate);
    }

    /**
     * Retrieve a media item for a playlist (only if it exists in the playlist)
     * 
     * @param kalturaEntryId the kaltura entry id of this media item
     * @param collection the collction (with playlist) that contains the entry
     * @param populate if true then populate the item with permissions and content
     * @return a populated media item OR null if none is found with the given id in the given collection
     * @throws IllegalArgumentException if the params are invalid
     * @throws SecurityException if the user does not have permissions
     */
    public MediaItem getCollectionMediaItem(String kalturaEntryId, MediaCollection collection, boolean populate) {
        if (StringUtils.isEmpty(kalturaEntryId)) {
            throw new IllegalArgumentException("kalturaEntryId must not be null");
        }
        if (collection == null) {
            throw new IllegalArgumentException("collection must not be null");
        }
        KalturaPlaylist playlist = collection.getKalturaPlaylist();
        if (playlist == null) {
            throw new IllegalArgumentException("playlist must not be null");
        }
        // verify the entry is a real one
        KalturaMediaEntry kme = kalturaAPIService.getKalturaItem(kalturaEntryId);
        if (kme == null) {
            throw new RuntimeException("kaltura media entry doesn't exist");
        }
        String collectionId = playlist.id;
        boolean playlistContainsItem = StringUtils.contains(playlist.playlistContent, kalturaEntryId);
        MediaItem mi = null;
        if (playlistContainsItem) {
            /* collection is input now
            String locationId = findLocationFromPlaylist(playlist);
            Map<String, String> plMetadata = kalturaAPIService.getPlaylistMetadataFields(playlist.id).get(collectionId);
            MediaCollection mc = new MediaCollection(playlist, locationId, plMetadata);
            */
            Map<String, String> kmeMetadata = kalturaAPIService.getMetadataForEntry(playlist.id, kalturaEntryId).get(kalturaEntryId);
            mi = new MediaItem(collection, kme, kmeMetadata);
            populateMediaItemData(mi); // fill in the permissions
            if (! canViewMI(mi, collection) ) {
                throw new SecurityException("user (" + external.getCurrentUserId() + ") cannot view item (" + kalturaEntryId + ") in collection ("+collectionId+")");
            }
            if (populate) {
                populateItem(mi);
            }
        }
        return mi;
    }

    /**
     * Get a media item for the library based on the given kaltura entry id and location
     * 
     * @param kalturaEntryId the kaltura entry id of this media item
     * @param locationId the locationId of the site (containing this library)
     * @param populate if true then add kaltura meta data, if false only the item is returned
     * @return a populated media item OR null if none is found with the given id
     * @throws IllegalArgumentException if the params are invalid
     * @throws SecurityException if the user does not have permissions
     */
    public MediaItem getLibraryMediaItem(String kalturaEntryId, String locationId, boolean populate) {
        if (StringUtils.isEmpty(kalturaEntryId)) {
            throw new IllegalArgumentException("kalturaEntryId must not be null");
        }
        if (StringUtils.isEmpty(locationId)) {
            throw new IllegalArgumentException("locationId must not be null");
        }
        if (log.isDebugEnabled()) {
            log.debug("getLibraryMediaItem(keid=" + kalturaEntryId + ", loc=" + locationId + ", populate=" + populate + ")");
        }
        KalturaCategory kc = kalturaAPIService.getSiteCategory(locationId);
        if (kc == null) {
            throw new RuntimeException("category cannot be null");
        }
        List<KalturaMediaEntry> entries = kalturaAPIService.getKalturaItemsForCategory(kc.id, 0, -1);
        boolean categoryContainsItem = false;
        for (KalturaMediaEntry e : entries) {
            if (StringUtils.equals(kalturaEntryId,e.id)) {
                categoryContainsItem = true;
                break;
            }
        }
        MediaItem mi = null;
        if (categoryContainsItem) {
            KalturaMediaEntry kme = kalturaAPIService.getKalturaItem(kalturaEntryId);
            if (kme != null) {
                Map<String, String> metadata = kalturaAPIService.getMetadataForEntry(kc.id+"", kme.id).get(kme.id);
                mi = new MediaItem(locationId, kme, metadata);
                populateMediaItemData(mi); // fill in the permissions
                if (! canViewMI(mi, null) ) { // NOTE the permissions check here - this is only going to check library level perms
                    throw new SecurityException("user (" + external.getCurrentUserId() + ") cannot view item (" + kalturaEntryId + ") in library ("+locationId+")");
                }
                if (populate) {
                    populateItem(mi);
                }
            }
        }
        return mi;
    }

    /**
     * Adds an item to a collection (and also to the site library if not already part of it)
     * 
     * @param collectionId the collection id (should be playlist id)
     * @param locationId the location (i.e. the site library id - e.g. /site/3674fc9f-8d5c-4ede-86f0-2f7432edb0db)
     * @param kalturaId the id for the kaltura item (not the media id) (e.g. 0_5soq7x2r)
     * @param position the position to add the item,
     * <= 0 indicates to put the item at the end,
     * any other number will place the item at that location in the collection
     * @return the newly added item
     * @throws SecurityException if the user is not allowed to add to this collection
     * @throws IllegalArgumentException if the params are blank or invalid and do not exist
     * @throws IllegalStateException if the item is already in the collection
     */
    public MediaItem addKalturaItemToCollection(String collectionId, String locationId, String kalturaId, int position) {
        if (StringUtils.isBlank(kalturaId)) {
            throw new IllegalArgumentException("kalturaId must be set");
        }
        if (StringUtils.isBlank(collectionId) ) {
            throw new IllegalArgumentException("collectionId must be set");
        }
        if (log.isDebugEnabled()) log.debug("addKalturaItemToCollection(collectionId="+collectionId+", locationId="+locationId+", kalturaId="+kalturaId+", position="+position+")");
        MediaCollection mc = getCollection(collectionId, 0, -1); // include all items
        if (mc == null) {
            throw new IllegalArgumentException("MediaCollection (id="+collectionId+") could not be found");
        }
        locationId = mc.getLocationId();
        if (StringUtils.isBlank(locationId)) {
            throw new IllegalArgumentException("locationId is not set while trying to add kaltura item ("+kalturaId+") to collection ("+collectionId+")");
        }
        // Try to add this to the library (won't do anything if it already exists in the library)
        String mediaId;
        try {
            mediaId = addKalturaItemToLibrary(locationId, kalturaId);
        } catch (Exception e) {
            // The user might not have permission to add the item to the site library
            log.warn("Kaltura user ("+external.getCurrentUserName()+") cannot add item ("+kalturaId+") to library ("+locationId+") while adding it to the collection ("+collectionId+")");
            mediaId = kalturaId;
        }

        // add the item to the collection - handles security checks

        // do perm checks and get the collection
        if (! mc.isAddItems()) { // check the add items flag
            // also, admin can always add items, otherwise fail
            checkPermsOrException(
                    new String[] {ExternalLogic.PERM_ADMIN, ExternalLogic.PERM_MANAGER}, 
                    mc.getLocationId());
        }
        MediaItem item = mc.getItemById(mediaId); // exists already if not null
        if (item != null) {
            // TODO do we really want to throw exception if this is already in the collection
            throw new IllegalStateException("item ("+mediaId+") already exists in the collection");
        } else {
            KalturaMediaEntry entry = kalturaAPIService.getKalturaItem(mediaId);
            item = new MediaItem(mc, entry, null);
            // force the creator of the new item to the current user id (maintain the owner id)
            item.setCreatorId(entry.creatorId);
            String currentUsername = external.getCurrentUserName();
            if (currentUsername != null) {
                item.setOwnerId(currentUsername);
            } else {
                item.setOwnerId(item.getCreatorId());
            }
            // update the entry's metadata, if necessary
            updateEntryMetadataIfNeeded(item, KalturaAPIService.METADATA_PERMISSIONS_CONTAINER_TYPE_PLAYLIST, collectionId, locationId, null, kalturaAPIService, external);
            // assume the mediaId is the kaltura entry id and the collectionId is the playlist id
            KalturaPlaylist playlist = kalturaAPIService.updatePlaylistEntryOrdering(mc.getId(), mediaId, position);
            int playlistLength = StringUtils.split(playlist.playlistContent, ',').length;
            if (position <= 0 || position > playlistLength) {
                position = playlistLength - 1;
            }
            item.setPosition(position);
            log.info("User ("+external.getCurrentUserId()+") added media item ("+mediaId+") to collection: " + mc + " at position: " + position);
        }
        if (log.isDebugEnabled()) log.debug("addKalturaItemToCollection : COMPLETED");
        return item;
    }

    /**
     * updates a media collection's items set entirely 
     * (the current set will be entirely replaced by the new input set assuming permissions allow it),
     * this will also add the items to the related site library if needed
     * 
     * @param collectionId the collection id (should be playlist id)
     * @param locationId the location (i.e. the site library id - e.g. /site/3674fc9f-8d5c-4ede-86f0-2f7432edb0db)
     * @param kalturaIds the complete set in order of the ids of items for this collection
     * @return the complete list of current items for the updated collection 
     * (may differ from ids that were passed in at kalturaIds because some items may not be allowed or may not exist)
     */
    public List<MediaItem> updateCollectionMediaItems(String collectionId, String[] kalturaIds) {
        // added for https://jira.sakaiproject.org/browse/SKE-174
        if (kalturaIds == null) {
            throw new IllegalArgumentException("kalturaIds must be set");
        }
        if (StringUtils.isBlank(collectionId) ) {
            throw new IllegalArgumentException("collectionId must be set");
        }
        if (log.isDebugEnabled()) log.debug("updateCollectionMediaItems(collectionId="+collectionId+", kalturaIds="+ArrayUtils.toString(kalturaIds)+")");
        MediaCollection mc = getCollection(collectionId, 0, -1); // include all items
        if (mc == null) {
            throw new IllegalArgumentException("MediaCollection (id="+collectionId+") could not be found");
        }
        String locationId = mc.getLocationId();
        if (StringUtils.isBlank(locationId)) {
            throw new IllegalArgumentException("locationId is not set while trying to update items in collection ("+collectionId+"), items: "+ArrayUtils.toString(kalturaIds));
        }

        // add the items to the library first (should do nothing if they are already in there)
        try {
            addKalturaItemToLibrary(locationId, kalturaIds);
        } catch (Exception e) {
            // The user might not have permission to add the item to the site library
            log.warn("Kaltura user ("+external.getCurrentUserName()+") cannot add items ("+ArrayUtils.toString(kalturaIds)+") to library ("+locationId+") while adding it to the collection ("+collectionId+")");
        }

        // add or remove the items to the collection - handles security checks

        // create a set which includes all the added ids and one with all the removed ids for security checks
        HashSet<String> kalturaIdsSet = new HashSet<String>(Arrays.asList(kalturaIds));
        HashSet<String> collItemIdsSet = new HashSet<String>(mc.getKalturaIds());
        HashSet<String> adds = new HashSet<String>(Arrays.asList(kalturaIds));
        HashSet<String> removes = new HashSet<String>(mc.getKalturaIds());
        adds.removeAll(collItemIdsSet);
        removes.removeAll(kalturaIdsSet);

        List<MediaItem> currentItems;
        if (adds.isEmpty() && removes.isEmpty()) {
            // nothing to do here
            currentItems = mc.getItems();
        } else {
            // we have at least one add or remove

            HashMap<String, MediaItem> newItems = new HashMap<String, MediaItem>(kalturaIds.length);
            for (MediaItem mi : mc.getItems()) {
                newItems.put(mi.getKalturaId(), mi);
            }
            if (!removes.isEmpty()) {
                // do perm check on collection and items
                // these will already be available in the collection so just get them from there, no need to fetch them separate
                for (String kalturaId : removes) {
                    MediaItem mi = mc.getByKalturaId(kalturaId);
                    checkPermControlMI(mi); // EXCEPTION if it fails
                    newItems.remove(kalturaId);
                    // remove entry's permission metadata for this playlist (assume collectionId is playlist id) - TODO operate on all removes at once?
                    kalturaAPIService.updateEntryMetadata(kalturaId, KalturaAPIService.METADATA_PERMISSIONS_CONTAINER_TYPE_PLAYLIST, collectionId, null, true);
                    if (log.isDebugEnabled()) log.debug("updateCollectionMediaItems removing item ("+kalturaId+") from collection ("+mc.getId()+")");
                }
            }

            if (!adds.isEmpty()) {
                // do perm checks on the collection
                if (! mc.isAddItems()) { // check the add items flag
                    // also, admin can always add items, otherwise fail
                    checkPermsOrException(
                            new String[] {ExternalLogic.PERM_ADMIN, ExternalLogic.PERM_MANAGER}, 
                            mc.getLocationId());
                }
                // fetch the items
                List<KalturaMediaEntry> entries = kalturaAPIService.getKalturaItems(null, adds.toArray(new String[adds.size()]), 0, -1);
                for (KalturaMediaEntry entry : entries) {
                    MediaItem item = new MediaItem(mc, entry, null);
                    // force the creator of the new item to the current user id (maintain the owner id)
                    item.setCreatorId(entry.creatorId);
                    String currentUsername = external.getCurrentUserName();
                    if (currentUsername != null) {
                        item.setOwnerId(currentUsername);
                    } else {
                        item.setOwnerId(item.getCreatorId());
                    }
                    item.setCollection(mc);
                    // update the entry's metadata, if necessary - TODO should this operate on the entire set at once?
                    updateEntryMetadataIfNeeded(item, KalturaAPIService.METADATA_PERMISSIONS_CONTAINER_TYPE_PLAYLIST, collectionId, locationId, null, kalturaAPIService, external);
                    populateMediaItemData(item);
                    newItems.put(item.getKalturaId(), item);
                }
            }

            // now we update the entire set
            kalturaAPIService.updateKalturaPlaylistEntries(mc.getKalturaPlaylist(), Arrays.asList(kalturaIds), true, null);
            currentItems = new ArrayList<MediaItem>(kalturaIds.length);
            for (int i = 0; i < kalturaIds.length; i++) {
                String kalturaId = kalturaIds[i];
                MediaItem item = newItems.get(kalturaId);
                if (item != null) {
                    currentItems.add(item);
                }
            }
        }
        return currentItems;
    }

    /**
     * removes an item from a collection
     * 
     * @param collectionId the collection id (should be playlist id)
     * @param kalturaId the id of the kaltura item
     * @return true if item is removed, otherwise false
     * @throws IllegalArgumentException if parameters are not set
     * @throws RuntimeException if playlist is null
     * @throws SecurityException is the user is not allowed
     */
    public boolean removeKalturaItemFromCollection(String collectionId, String kalturaId) {
        if (StringUtils.isBlank(collectionId)) {
            throw new IllegalArgumentException("collectionId must be set");
        }
        if (StringUtils.isBlank(kalturaId)) {
            throw new IllegalArgumentException("kalturaId must be set");
        }
        if (log.isDebugEnabled()) log.debug("removeKalturaItemFromCollection(collectionId="+collectionId+", kalturaId="+kalturaId+")");
        MediaCollection mc = getCollection(collectionId, 0, 0);
        if (mc == null || mc.getKalturaPlaylist() == null) {
            throw new RuntimeException("collection ("+collectionId+") and playlist cannot be null");
        }
        MediaItem mi = getCollectionMediaItem(kalturaId, mc, true);
        //checkPermControlMC(mc);
        checkPermControlMI(mi);
        KalturaPlaylist kp = mc.getKalturaPlaylist();
        boolean removed = kalturaAPIService.removeItemFromKalturaPlaylist(kp, kalturaId);
        if (log.isDebugEnabled()) log.debug("removeKalturaItemFromCollection : COMPLETED");
        return removed;
    }

    /**
     * Add a kaltura item (or items) to the site library,
     * does not add the item to a collection
     * 
     * @param locationId the id of the location of the library
     * @param kalturaIds the kaltura item to add to it (or multiple to add more than one)
     * @return the ID of the item that was added (first ID if more than one is added)
     * @throws IllegalArgumentException if the input data is invalid
     * @throws SecurityException is the user is not allowed
     */
    public String addKalturaItemToLibrary(String locationId, String... kalturaIds) {
        if (StringUtils.isBlank(locationId)) {
            throw new IllegalArgumentException("locationId must be set");
        }
        if (kalturaIds == null || kalturaIds.length <= 0) {
            throw new IllegalArgumentException("kalturaIds must be set");
        }
        if (log.isDebugEnabled()) log.debug("addKalturaItemToLibrary(locationId="+locationId+", kalturaId="+ArrayUtils.toString(kalturaIds)+")");

        // check the user has write permission in the site/location
        checkPermsOrException(new String[] {ExternalLogic.PERM_WRITE}, locationId);

        // add the item
        String ownerId = external.getCurrentUserName();
        String siteId = external.extractSiteId(locationId);
        KalturaCategory siteCategory = kalturaAPIService.getSiteCategory(siteId);
        kalturaAPIService.addKalturaCategoryEntries(siteCategory.id, siteCategory.name, null, kalturaIds);
        for (String kalturaId : kalturaIds) {
            entriesCache.remove(kalturaId); // the cached item may be wrong so just clear it now so it will be reloaded later
        }
        log.info("User ("+ownerId+") added kaltura item ("+ArrayUtils.toString(kalturaIds)+") to site library ("+siteId+")");
        if (log.isDebugEnabled()) log.debug("addKalturaItemToLibrary : COMPLETED");
        return kalturaIds[0];
    }

    /**
     * remove a kaltura entry from a library
     * 
     * @param locationId the library's location id
     * @param kalturaId the kaltura entry's id
     * @return true if item is removed, otherwise false
     * @throws IllegalArgumentException if parameters are invalid
     * @throws SecurityException if user is not allowed
     */
    public boolean removeKalturaItemFromLibrary(String locationId, String kalturaId) {
        if (StringUtils.isBlank(locationId)) {
            throw new IllegalArgumentException("locationId must be set");
        }
        if (StringUtils.isBlank(kalturaId)) {
            throw new IllegalArgumentException("kalturaIds must be set");
        }
        if (log.isDebugEnabled()) log.debug("removeKalturaItemFromLibrary(locationId="+locationId+", kalturaId="+kalturaId+")");
        int removed = 0;
        locationId = external.fixLocationId(locationId);
        MediaItem mi = getLibraryMediaItem(kalturaId, locationId, false);
        if (mi != null) {
            // only remove the item if it actually exists
            checkCanControlMI(mi); // SecurityException if not allowed
            String siteId = external.extractSiteId(locationId);
            KalturaCategory siteCategory = kalturaAPIService.getSiteCategory(siteId);
            removed = kalturaAPIService.removeKalturaCategoryEntries(siteCategory.id, kalturaId);
        }
        log.debug("removeKalturaItemFromLibrary : COMPLETED");
        return removed > 0;
    }

    /**
     * Update an item (will not update the position (ordering) for the item),
     * this mostly is only for updating the permissions setting on an item,
     * NOTE: will only update perms if the user has manage permissions
     * 
     * @param item item to update (must have the MediaCollection populated if part of a collection)
     * @return the updated item
     * @throws IllegalArgumentException if the arguments are invalid or the container/item cannot be found
     * @throws SecurityException if the user cannot manage or control the item
     */
    public MediaItem updateMediaItem(MediaItem item) {
        if (item == null) {
            throw new IllegalArgumentException("item must not be null");
        }
        if (item.getKalturaId() == null) {
            throw new IllegalArgumentException("item ("+item+") must be a persistent object");
        }
        if (StringUtils.trimToNull(item.getLocationId()) == null && item.getCollection() == null) {
            throw new IllegalArgumentException("item ("+item+") must have the location ("+item.getLocationId()+") OR collection set");
        }
        String containerType = KalturaAPIService.METADATA_PERMISSIONS_CONTAINER_TYPE_SITE;
        String containerId = external.extractSiteId(item.getLocationId());
        if (item.getCollection() != null) {
            containerId = item.getCollection().getId();
            containerType = KalturaAPIService.METADATA_PERMISSIONS_CONTAINER_TYPE_PLAYLIST;
        }
        if (log.isDebugEnabled()) log.debug("updateMediaItem(item="+item+")");
        KalturaMediaEntry kalturaMediaEntry = kalturaAPIService.getKalturaItem(item.getKalturaId());
        if (kalturaMediaEntry == null) {
            throw new IllegalArgumentException("item id (" + item.getKalturaId() + ") not found");
        }
        Map<String, String> metadata = kalturaAPIService.getMetadataForEntry(containerId, kalturaMediaEntry.id).get(kalturaMediaEntry.id);
        MediaItem mi = new MediaItem(item.getLocationId(), kalturaMediaEntry, metadata);
        checkCanControlMI(mi); // check perms and verify user allowed to control or manage
        if (! mi.isManage() && ! mi.isControl()) {
            throw new SecurityException("User "+mi.getCurrentUsername()+" not allowed to manage or control item: " + mi);
        }
        // only manage perm allowed to change the item perms
        if (mi.isManage()) {
            boolean remixable = false;
            if (mi.getMediaType().equals(MediaItem.TYPE_VIDEO)) {
                remixable = item.isRemixable();
            }
            metadata = MediaItem.makeMetaDataMap(item.isHidden(), item.isShared(), remixable, item.getOwnerId());
        }
        mi = new MediaItem(item.getLocationId(), kalturaMediaEntry, metadata);
        // update metadata fields
        kalturaAPIService.updateEntryMetadata(item.getKalturaId(), containerType, containerId, metadata, false);
        mi.setDateModified(new Date());
        log.info("User ("+external.getCurrentUserId()+") updated (manage:"+mi.isManage()+") media item ("+mi.getIdStr()+", "+mi.getName()+"): " + mi);
        populateMediaItemData(mi);
        if (log.isDebugEnabled()) log.debug("updateMediaItem : COMPLPETED");
        return mi;
    }


    // METADATA

    /**
     * Updates an entry's metadata, only if metadata has changed from the defaults or current (previous) value.
     * Also handles all the default metadata processing (including the rules related to admin/managers)
     * 
     * This method reduces the number of calls back to the Kaltura server. 
     * We must place this here as a static method, as it needs to be called from both mediaService 
     * and kalturaApiService. This way the logic is consolidated here.
     * 
     * @param mediaItem MediaItem object for entry
     * @param containerType the type of container holding the entry (category or playlist)
     * @param containerId the id of the container which holds the entry
     * @param locationId the location id (i.e. /site/xxxxxxxxx)
     * @param metadata [OPTIONAL] new metadata
     * @param kalturaAPIService the KalturaAPIService instance
     * @param externalLogic the ExternalLogic instance
     * @return true if the metadata was updated, false if not
     */
    public static boolean updateEntryMetadataIfNeeded(MediaItem mediaItem, String containerType, 
            String containerId, String locationId, Map<String, String> metadata, 
            KalturaAPIService kalturaAPIService, ExternalLogic externalLogic) {
        if (mediaItem == null) {
            throw new IllegalArgumentException("mediaItem must be set");
        }
        if (kalturaAPIService == null) {
            throw new IllegalArgumentException("kalturaAPIService must be set");
        }
        if (externalLogic == null) {
            throw new IllegalArgumentException("externalLogic must be set");
        }
        if (StringUtils.isEmpty(locationId)) {
            locationId = externalLogic.getCurrentLocationId();
            if (StringUtils.isEmpty(locationId)) {
                throw new IllegalArgumentException("locationId must be set (could not get from current context either)");
            }
        }
        Map<String, String> newMetadata = (metadata == null) ? kalturaAPIService.buildEntryMetadataPermissionsMap(null) : metadata;
        boolean hasChanged = false;
        String currentUserId = externalLogic.getCurrentUserId(); // this might not be set so check and warn if not
        if (StringUtils.isBlank(currentUserId)) {
            log.warn("No current user available for updateEntryMetadataIfNeeded: containerId="+containerId+", locationId="+locationId);
        }
        boolean isUserKalturaAdminOrManager = externalLogic.checkPerms(currentUserId, externalLogic.fixLocationId(locationId), 
                new String[] {ExternalLogic.PERM_ADMIN, ExternalLogic.PERM_MANAGER}); // check current user is Kaltura admin/manager (SKE-163)
        if (isUserKalturaAdminOrManager) {
            // if current user is Kaltura admin or manager, set media to public
            newMetadata.put(METADATA_HIDDEN, KalturaAPIService.DEFAULT_METADATA_HIDDEN_ADMIN);
        }
        // if clipping is enabled, must set permissions as media is not remixable by default
        boolean remixable = kalturaAPIService.isKalturaClippingEnabled() && MediaItem.KALTURA_CLIPPING_DEFAULT_ALLOWED;
        if (remixable) {
            newMetadata.put(METADATA_REMIXABLE, "R");
        }
        // set permissions if the owner of the item is different
        if (!StringUtils.equals(mediaItem.getCreatorId(), mediaItem.getOwnerId())) {
            hasChanged = true;
        }
        // compare item's existing metadata to new metadata
        Map<String, String> itemMetadata = mediaItem.extractMetaDataMap(); // get the item's existing metadata
        for (String permission : itemMetadata.keySet()) {
            String value = itemMetadata.get(permission);
            // checking for permissions equality, disregard the owner for this check as it is added to the permissions string later
            if(!StringUtils.equals(METADATA_OWNER, permission) && !StringUtils.equals(value, newMetadata.get(permission))) {
                hasChanged = true;
                break;
            }
        }
        // only update entry's metadata if there are changes
        boolean updated = false;
        if (hasChanged) {
            kalturaAPIService.updateEntryMetadata(mediaItem.getIdStr(), containerType, containerId, newMetadata, false);
            updated = true;
        }
        return updated;
    }

    // UTILS

    private List<String> makeKalturaIds(List<MediaItem> items) {
        ArrayList<String> kids = new ArrayList<String>();
        if (items != null) {
            for (MediaItem mi : items) {
                kids.add(mi.getKalturaId());
            }
        }
        return kids;
    }


    // PERM checks

    public boolean isKalturaAdmin(String userId, String locationId) {
        return external.checkPerms(userId, locationId, new String[] {ExternalLogic.PERM_ADMIN});
    }

    public boolean isKalturaManager(String userId, String locationId) {
        return external.checkPerms(userId, locationId, new String[] {ExternalLogic.PERM_MANAGER});
    }

    public boolean isKalturaMyMediaVisible(String userId, String locationId) {
        return external.checkPerms(userId, locationId, new String[] { ExternalLogic.PERM_SHOW_MY_MEDIA, ExternalLogic.PERM_WRITE });
    }

    public boolean isKalturaEditor(String userId, String locationId) {
        return external.checkPerms(userId, locationId, new String[] {ExternalLogic.PERM_EDITOR});
    }

    public boolean isKalturaWrite(String userId, String locationId) {
        return external.checkPerms(userId, locationId, new String[] {ExternalLogic.PERM_WRITE});
    }

    public boolean isKalturaRead(String userId, String locationId) {
        return external.checkPerms(userId, locationId, new String[] {ExternalLogic.PERM_READ});
    }

    public boolean isKalturaUploadSpecial(String userId, String locationId) {
        return external.checkPerms(userId, locationId, new String[] {ExternalLogic.PERM_UPLOAD_SPECIAL});
    }

    /**
     * isSiteLibraryVisible() returns true if the user can view the Site Library tab or false if not.
     * <p>Users can see the site library by default if the property kaltura.siteLibraryRequiresPrivs is not
     * set or is false.  If the property is set to true, the user requires the priv ExternalLogic.PERM_SHOW_SITE_LIBRARY
     * @param userId user permission being checked
     * @param locationId site being checked
     * @return true if the user can see the Site Library, false if not.
     */
    public boolean isSiteLibraryVisible(String userId, String locationId) {
        boolean hasPrivs = external.checkPerms(userId, locationId, new String[] {ExternalLogic.PERM_SHOW_SITE_LIBRARY});
        boolean visible = !this.siteLibraryRequiresPrivs || hasPrivs;
        if (log.isDebugEnabled()) log.debug("isSiteLibraryVisible(userId="+userId+", locationId="+locationId+"): visible="+visible);
        return visible;
    }

    /**
     * Check if the current user can view items or not
     */
    public boolean canViewItems() {
        boolean allowed = false;
        String currentUserId = external.getCurrentUserId();
        if (currentUserId != null) {
            allowed = true;
        }
        return allowed;
    }

    /**
     * Check if current user can control a given collection,
     * this will also handle the add items perm check update as well
     * 
     * @param mc the collection
     * @throws SecurityException if the user is not allowed
     */
    private void checkPermControlMC(MediaCollection mc) {
        String currentUserId = external.getCurrentUserId();
        String currentUsername = external.getCurrentUserName();
        mc.indicateUserControl(currentUsername, false); // clear to false
        mc.setAddItems(false);
        if (mc.getOwnerId().equals(currentUsername)) {
            // owner can control and add items
            mc.indicateUserControl(currentUsername, true);
            mc.setAddItems(true);
        } else if (external.isUserAdmin(currentUserId)) {
            // super admin can control and add items
            mc.indicateUserControl(currentUsername, true);
            mc.setAddItems(true);
        } else {
            String[] locationPerms = new String[] { 
                    ExternalLogic.PERM_READ, 
                    ExternalLogic.PERM_WRITE, 
                    ExternalLogic.PERM_ADMIN, 
                    ExternalLogic.PERM_MANAGER, 
                    ExternalLogic.PERM_EDITOR 
            };
            if (MediaCollection.SHARING_PUBLIC.equals(mc.getSharing()) 
                    || MediaCollection.SHARING_SHARED.equals(mc.getSharing()) ) {
                // check if user is a member of the collection course
                boolean allowed = external.checkPerms(currentUserId, mc.getLocationId(), locationPerms);
                if (allowed) {
                    // anyone can add, if they have access to the course
                    mc.setAddItems(true);
                } else if (!mc.isHidden()) {
                    mc.setViewable(true);
                }
            } else if (MediaCollection.SHARING_ADMIN.equals(mc.getSharing())) {
                // this is only here to update the viewable flag
                boolean allowed = external.checkPerms(currentUserId, mc.getLocationId(), new String[] { ExternalLogic.PERM_READ });
                if (allowed && !mc.isHidden()) {
                    mc.setViewable(true);
                }
            } else if (MediaCollection.SHARING_PRIVATE.equals(mc.getSharing())) {
                mc.setViewable(false);
                // check if user was owner OR super admin above so automatic exception
                throw new SecurityException("only the owner of a private collection may modify or remove it");
            }
            boolean locationAdmin = external.checkPerms(currentUserId, mc.getLocationId(), new String[] { ExternalLogic.PERM_ADMIN });
            if (!locationAdmin) {
                throw new SecurityException("user ("+currentUserId+") is not allowed to control collection ("+mc.getId()+") in location ("+mc.getLocationId()+")");
            }
            // at this point, user must be an admin (collection must be any type EXCEPT private)
            mc.indicateUserControl(currentUsername, true);
            mc.setAddItems(true);
            mc.setViewable(true);
        }
    }

    /**
     * Check if this item can be viewed or not (item should be populated)
     * 
     * @param mi the media item to check
     * @param mc the collection which contains the media item (OR null if this is only in a site library or my media)
     * @return true if it can be viewed OR false otherwise
     */
    public boolean canViewMI(MediaItem mi, MediaCollection mc) {
        if (mi == null) {
            throw new IllegalArgumentException("mi must not be null");
        }
        boolean allowed = false;
        String currentUserEid = external.getCurrentUserName();
        String currentUserId = external.getCurrentUserId();
        if (mi.getOwnerId().equals(currentUserEid)) {
            allowed = true;
        } else if (external.isUserAdmin(currentUserId)) {
            allowed = true;
        } else if (external.checkPerms(currentUserId, mi.getLocationId(), 
                new String[] {ExternalLogic.PERM_MANAGER, ExternalLogic.PERM_EDITOR})) {
            // manager/editor can see all regardless of hidden setting
            allowed = true;
        } else {
            // normal view check
            allowed = external.checkPerms(currentUserId, mi.getLocationId(), 
                    new String[] {ExternalLogic.PERM_READ, ExternalLogic.PERM_ADMIN});
            if (allowed) {
                // allowed to view items in general
                populateMediaItemData(mi); // populates the media perms
                mi.setCollection(mc);
                if (mi.isHidden()) {
                    // check for hidden items viewing
                    if (mc == null) {
                        // site library
                        if (! mi.isControl()) {
                            allowed = false;
                        }
                    } else {
                        // make sure this item is allowed to be viewed given the collection it is part of
                        if (! mc.isControl() && ! mi.isControl()) {
                            allowed = false;
                        }
                    }
                }
            }
        }
        return allowed;
    }

    /**
     * Check if current user can control a given media item,
     * populates the security info for the given media item
     * 
     * @param mi the media item
     * @return true if user allowed to control OR false otherwise
     */
    private boolean checkPermControlMI(MediaItem mi) {
        if (mi == null) {
            throw new IllegalArgumentException("media item must not be null for perm check");
        }
        boolean control = false; // user can move/remove item
        boolean edit = false; // user can edit meta-data
        boolean manage = false; // user can edit item permissions

        String currentUserId = external.getCurrentUserId();
        String currentUserEid = external.getCurrentUserName();
        boolean superAdmin = external.isUserAdmin(currentUserId);
        boolean editor = external.checkPerms(currentUserId, mi.getLocationId(), new String[] {ExternalLogic.PERM_EDITOR});
        if (superAdmin || editor || mi.getCreatorUserId().equals(currentUserEid) ) {
            // super-admin, editor and creator can always edit
            edit = true;
        }

        boolean manager = external.checkPerms(currentUserId, mi.getLocationId(), new String[] {ExternalLogic.PERM_MANAGER});
        if (superAdmin || manager) {
            // super-admin and manager can always manage
            manage = true;
        }

        if (superAdmin || mi.getOwnerId().equals(currentUserEid)) {
            // super-admin and owner can always control
            control = true;
        }
        if ((manage == false || control == false) && mi.getCollection() != null) {
            // only do these checks if the user does not already have the permissions
            // collection - use the collection from the mi if it is set
            MediaCollection mc = mi.getCollection();
            boolean admin = external.checkPerms(currentUserId, mc.getLocationId(), new String[] {ExternalLogic.PERM_ADMIN});
            if (mc.getOwnerId().equals(currentUserEid)) {
                // owner can always control items in their collection
                control = true;
                if (admin) {
                    // OVERRIDE manager setting for collections where admin is an owner
                    manage = true;
                }
            } else if (MediaCollection.SHARING_PUBLIC.equals(mc.getSharing())) {
                // anyone allowed to control items in a public collection
                control = true;
            } else if (MediaCollection.SHARING_SHARED.equals(mc.getSharing())) {
                // only the creator allowed to control items in a shared collection (handled above)
                if (mi.getCreatorUserId().equals(currentUserEid)) {
                    control = true;
                }
            } else if (MediaCollection.SHARING_ADMIN.equals(mc.getSharing())) {
                // any admin allowed to control items in an admin collection
                if (admin) {
                    control = true;
                }
            } else if (MediaCollection.SHARING_PRIVATE.equals(mc.getSharing()) ) {
                // only collection owner can control the items in a private collection (handled above)
                if (mc.getOwnerId().equals(currentUserEid)) {
                    control = true;
                }
            }
        } else {
            // site library (non-owner of item)
            if ( manager ) {
                // manager can always control items in the site library
                control = true;
            }
        }
        mi.indicateUserControl(currentUserEid, edit, control, manage);
        return control;
    }

    /**
     * Check if current user can control (remove/reorder) a given media item,
     * populates the security info for the given media item
     * 
     * @param mi the media item
     * @param mc the collection which contains the media item (OR null if this is only in a site library or my media)
     * @throws SecurityException if the user cannot edit this mi
     */
    private void checkCanControlMI(MediaItem mi) {
        boolean control = checkPermControlMI(mi);
        if (! control) {
            throw new SecurityException("user ("+mi.getCurrentUsername()+") is not allowed to control item ("+mi+")");
        }
    }

    /**
     * Checks the permission for the current user and returns the current user id
     * @param perm the permission to check
     * @param locationId the location id (null for current location)
     * @return the current user ID
     * @throws SecurityException if the user is not allowed
     * @throws IllegalArgumentException if perm or location are null
     */
    private String checkPermOrException(String perm, String locationId) {
        return checkPermsOrException(new String[] {perm}, locationId);
    }

    /**
     * Checks the permissions for the current user and returns the current user id
     * @param perms the permissions to check
     * @param locationId the location id (null for current location)
     * @return the current user ID
     * @throws SecurityException if the user is not allowed
     * @throws IllegalArgumentException if perm or location are null
     */
    private String checkPermsOrException(String[] perms, String locationId) {
        String userId = external.getCurrentUserId();
        if (locationId == null || "".equals(locationId)) {
            locationId = external.getCurrentLocationId();
        }
        if (! external.checkPerms(userId, locationId, perms) ) {
            throw new SecurityException("user ("+userId+") is not allowed for ("+ArrayUtils.toString(perms)+") in location ("+locationId+")");
        }
        return userId;
    }


    // system data migration

    static int migrationStartDelaySecs = 60;
    static Thread migrationThread = null;
    static String markerKalturaId = "-=XmigrationRunnerMarkerX=-";
    static Boolean migrationRunning = null;
    /**
     * Run the migration process in a thread
     * Will not allow 2 migration processes to run at once on the same server
     */
    protected void startMigrationThread() {
        if (log.isDebugEnabled()) log.debug("startMigrationThread()");
        if (this.migrationDisabled) {
            log.info("Migration disabled by kaltura.migration.disabled config setting, CANNOT start the migration thread!");
        } else {
            if (migrationThread != null && migrationThread.isAlive()) {
                log.info("DB migration thread ("+migrationThread.getName()+" - "+migrationThread.getId()+") is already running and cannot be started again");
            } else {
                migrationThread = new Thread() {
                    @Override
                    public void run() {
                        setAdminInternal(true);
                        try {
                            migrateDatabaseToKalturaServer(false);
                        } catch (Exception e) {
                            log.fatal("FATAL exception in migration thread: "+e, e);
                            throw new RuntimeException("FATAL exception in migration thread: "+e, e);
                        } finally {
                            setAdminInternal(false);
                        }
                    }
                };
                migrationThread.setDaemon(true); // allow interupt
                migrationStartDelaySecs = migrationStartDelaySecs + new Random().nextInt(30); // random delay
                Executors.newSingleThreadScheduledExecutor().schedule(migrationThread, migrationStartDelaySecs, TimeUnit.SECONDS);
                log.info("DB migration (server: "+external.getServerInfo()+") thread ("+migrationThread.getName()+" - "+migrationThread.getId()+") starting (after a "+migrationStartDelaySecs+" second delay)");
            }
        }
    }

    /**
     * @return boolean true if the migration is currently running, false otherwise
     */
    @SuppressWarnings("deprecation")
    public boolean isMigrationRunning(String locationId) {
        boolean running = false;
        if (this.migrationDisabled) {
            // migration cannot run while it is disabled
        } else if (migrationRunning == null) {
            org.sakaiproject.kaltura.dao.MediaItemDB markerItem = dao.findOneBySearch(org.sakaiproject.kaltura.dao.MediaItemDB.class, 
                    new Search( new Restriction("kalturaId", markerKalturaId) )
            );
            if (markerItem != null) {
                // migration running
                running = true;
            }
        } else if (migrationRunning) {
            running = migrationRunning;
        }
        if (running && StringUtils.isNotEmpty(locationId)) {
            // check if the migration is complete for this location by counting all the items for this location
            long allLocationCollections = dao.countBySearch(org.sakaiproject.kaltura.dao.MediaCollectionDB.class, 
                    new Search( new Restriction("locationId", locationId) ) );
            long allLocationItems = dao.countBySearch(org.sakaiproject.kaltura.dao.MediaItemDB.class, 
                    new Search( new Restriction("locationId", locationId) ) );
            long toMigrate = allLocationCollections + allLocationItems;
            if (toMigrate <= 0l) {
                // nothing to migrate
                log.info("Kaltura migration: migration complete for "+locationId+": nothing to migrate");
                running = false;
            } else {
                // there are items to migrate so see if we have already migrated them all
                long unmigratedLocationCollections = dao.countBySearch(org.sakaiproject.kaltura.dao.MediaCollectionDB.class, 
                        new Search(
                                new Restriction[] {
                                        new Restriction("locationId", locationId),
                                        new Restriction("migrated", Boolean.TRUE, Restriction.NOT_EQUALS),
                                }
                            )
                        );
                long unmigratedLocationItems = dao.countBySearch(org.sakaiproject.kaltura.dao.MediaItemDB.class, 
                        new Search(
                                new Restriction[] {
                                        new Restriction("locationId", locationId),
                                        new Restriction("migrated", Boolean.TRUE, Restriction.NOT_EQUALS),
                                }
                            )
                        );
                long unmigrated = unmigratedLocationCollections + unmigratedLocationItems;
                if (unmigrated == 0l) {
                    log.info("Kaltura migration: migration complete for "+locationId+": "+toMigrate+" objects migrated");
                    running = false;
                } else {
                    log.info("Kaltura migration: migration not yet complete for "+locationId+": "+toMigrate+" objects to migrate, "+unmigrated+" objects remaining");
                }
            }
        }
        return running;
    }

    /**
     * @return an array of stats (longs) about the currently running migration:
     *  [libraryItems, unmigratedLibraryItems, collections, unmigratedCollections, collectionItems, unmigratedCollectionItems]
     */
    @SuppressWarnings("deprecation")
    public long[] getMigrationStats() {
        long libraryItems = dao.countBySearch(org.sakaiproject.kaltura.dao.MediaItemDB.class, 
                new Search( new Restriction("collection.id", "", Restriction.NULL) )
                );
        long collections = dao.countAll(org.sakaiproject.kaltura.dao.MediaCollectionDB.class);
        long collectionItems = dao.countBySearch(org.sakaiproject.kaltura.dao.MediaItemDB.class, 
                new Search( new Restriction("collection.id", "", Restriction.NOT_NULL) )
                );
        long unmigratedLibraryItems = dao.countBySearch(org.sakaiproject.kaltura.dao.MediaItemDB.class, 
                new Search(
                        new Restriction[] {
                                new Restriction("collection.id", "", Restriction.NULL),
                                new Restriction("migrated", Boolean.TRUE, Restriction.NOT_EQUALS),
                        }
                    )
                );
        long unmigratedCollections = dao.countBySearch(org.sakaiproject.kaltura.dao.MediaCollectionDB.class, 
                new Search( new Restriction("migrated", Boolean.TRUE, Restriction.NOT_EQUALS) )
                );
        long unmigratedCollectionItems = dao.countBySearch(org.sakaiproject.kaltura.dao.MediaItemDB.class, 
                new Search(
                        new Restriction[] {
                                new Restriction("collection.id", "", Restriction.NOT_NULL),
                                new Restriction("migrated", Boolean.TRUE, Restriction.NOT_EQUALS),
                        }
                    )
                );
        return new long[] { libraryItems, unmigratedLibraryItems, collections, unmigratedCollections, collectionItems, unmigratedCollectionItems };
    }

    /**
     * This is the main migration method
     * It will attempt to move all the data from the database into the Kaltura server category/playlist and metadata
     * 
     * NOTE: this will only allow one migration process to run at a time
     * 
     * @param removeExistingDataAfterVerify
     */
    @SuppressWarnings("deprecation")
    protected void migrateDatabaseToKalturaServer(boolean removeExistingDataAfterVerify) {
        if (log.isDebugEnabled()) log.debug("migrateDatabaseToKalturaServer(removeExistingDataAfterVerify="+removeExistingDataAfterVerify+")");

        String serverId = external.getServerInfo(); // ownerId
        if (this.migrationDisabled) {
            log.info("Migration disabled by kaltura.migration.disabled config setting, CANNOT run the on this server ("+serverId+")!");
            return; // SHORT CIRCUIT
        }

        // need to ensure the migration can only run on a single server
        org.sakaiproject.kaltura.dao.MediaItemDB markerItem = dao.findOneBySearch(org.sakaiproject.kaltura.dao.MediaItemDB.class, 
                new Search( new Restriction("kalturaId", markerKalturaId) )
        );
        if (markerItem != null) {
            migrationRunning = true;
            // check if it is our server that holds the lock, if so we can proceed, if not we have to die
            if (serverId.equals(markerItem.getOwnerId())) {
                log.info("Kaltura migration: found existing lock for this server ("+serverId+"), continuing with migration");
            } else {
                log.warn("Kaltura migration: found existing lock for server ("+markerItem.getOwnerId()+"), cannot run the migration on this server ("+serverId+"), if you know the migration is not running you can delete the KALTURA_ITEM lock marker (with kalturaId="+markerItem.getKalturaId()+") and restart the server to run the migration again");
                return; // SHORT CIRCUIT
            }
        }
        if (markerItem == null) {
            // create a new marker lock in the DB to stop other servers from trying to run this also
            markerItem = new org.sakaiproject.kaltura.dao.MediaItemDB(serverId, markerKalturaId, serverId);
            markerItem.setMigrated(true);
            dao.save(markerItem);
            migrationRunning = true;
            log.info("Kaltura migration: reserved a migration lock for server ("+serverId+"): "+markerItem);
        }

        int skippedBecauseDeletedItems = 0;
        log.info("Kaltura migration: starting sakai kaltura content migration from database to kaltura server");

        // force archiving process to run as admin user
        external.currentAllowAdminAccess();

        // check the DB for site library items not yet migrated
        List<org.sakaiproject.kaltura.dao.MediaItemDB> libraryItems;
        // check the DB for collections not yet migrated
        List<org.sakaiproject.kaltura.dao.MediaCollectionDB> collections;
        try {
            libraryItems = dao.findBySearch(org.sakaiproject.kaltura.dao.MediaItemDB.class, 
                    new Search(
                            new Restriction[] {
                                    new Restriction("collection.id", "", Restriction.NULL),
                                    new Restriction("migrated", Boolean.TRUE, Restriction.NOT_EQUALS),
                            },
                            new Order("locationId", true)
                            )
                    );
            log.info("Kaltura migration: found "+libraryItems.size()+" unmigrated library items");
            if (!libraryItems.isEmpty()) {
                // get complete list of site library location ids from the items
                HashMap<String, List<org.sakaiproject.kaltura.dao.MediaItemDB>> libraryItemsByLocation = new HashMap<String, List<org.sakaiproject.kaltura.dao.MediaItemDB>>();
                for (org.sakaiproject.kaltura.dao.MediaItemDB mediaItem : libraryItems) {
                    String locId = mediaItem.getLocationId();
                    if (!libraryItemsByLocation.containsKey(locId)) {
                        libraryItemsByLocation.put(locId, new ArrayList<org.sakaiproject.kaltura.dao.MediaItemDB>());
                    }
                    // convert the owner id from the sakai internal id to the eid
                    User user = external.getUser(mediaItem.getOwnerId());
                    if (user != null) {
                        mediaItem.setOwnerId(user.username);
                    } else {
                        // if no user is found, set owner id to admin
                        mediaItem.setOwnerId(KalturaAPIService.DEFAULT_OWNER_ID);
                    }
                    libraryItemsByLocation.get(locId).add(mediaItem);
                }

                // loop through the locations and create all categories (need to also check if they are already created)
                for (Entry<String, List<org.sakaiproject.kaltura.dao.MediaItemDB>> entry : libraryItemsByLocation.entrySet()) {
                    String locationId = entry.getKey();
                    List<org.sakaiproject.kaltura.dao.MediaItemDB> items = entry.getValue();
                    try {
                        //getSiteCategory(locationId);
                        // create the library playlists and add the items into them
                        KalturaCategory site = this.kalturaAPIService.getSiteCategory(locationId); // get and create site category
                        HashSet<Long> itemIds = new HashSet<Long>();
                        ArrayList<String> entryIds = new ArrayList<String>();
                        Map<String, Map<String, String>> fields = new HashMap<String, Map<String, String>>();
                        for (org.sakaiproject.kaltura.dao.MediaItemDB mediaItem : items) {
                            String keid = mediaItem.getKalturaId();
                            itemIds.add(mediaItem.getId());
                            KalturaMediaEntry kme = this.kalturaAPIService.getKalturaItem(keid);
                            if (kme == null) {
                                skippedBecauseDeletedItems++;
                                log.warn("Kaltura migration: Skipped kaltura entry ("+keid+") and library item ("+mediaItem+") since the item cannot be found on the Kaltura server");
                                continue; // SHORT CIRCUIT
                            }
                            entryIds.add(keid);
                            // update metadata fields
                            fields.put(mediaItem.getKalturaId(), createEntryMetadataMap(mediaItem));
                        }

                        // add the items to the site library
                        this.kalturaAPIService.addKalturaCategoryEntries(site.id, site.name, fields, entryIds.toArray(new String[entryIds.size()]));

                        if (removeExistingDataAfterVerify) {
                            dao.deleteSet(new HashSet<org.sakaiproject.kaltura.dao.MediaItemDB>(items));
                        } else {
                            // mark each item as migrated
                            dao.migrate(org.sakaiproject.kaltura.dao.MediaItemDB.class, itemIds);
                        }
                    } catch (Exception e) {
                        log.error("Kaltura migration: Unable to migrate site library for location: "+locationId+" "+e, e);
                    }
                }
            }

            // check the DB for collections which were partly migrated
            int partialCollections = dao.migratePartialCollections();
            log.info("Kaltura migration: found "+partialCollections+" partially migrated collections");

            collections = dao.findBySearch(org.sakaiproject.kaltura.dao.MediaCollectionDB.class, 
                    new Search(
                            new Restriction("migrated", Boolean.TRUE, Restriction.NOT_EQUALS),
                            new Order("id", true)
                            )
                    );
            log.info("Kaltura migration: found "+collections.size()+" unmigrated collections");

            // loop through unmigrated collections
            for (org.sakaiproject.kaltura.dao.MediaCollectionDB mediaCollection : collections) {
                try {
                    // migrate single collection
                    KalturaCategory site = this.kalturaAPIService.getSiteCategory(mediaCollection.getLocationId());
                    // add collection meta data
                    Map<String, String> fields = null;
                    if (!mediaCollection.isMigrated()) {
                        // convert the owner id from the sakai internal id to the eid
                        User user = external.getUser(mediaCollection.getOwnerId());
                        if (user != null) {
                            mediaCollection.setOwnerId(user.username);
                        } else {
                            // if no user is found, set owner id to admin
                            mediaCollection.setOwnerId(KalturaAPIService.DEFAULT_OWNER_ID);
                        }
                        fields = createPlaylistMetadataMap(mediaCollection);
                    }
                    KalturaPlaylist collPlaylist = this.kalturaAPIService.getOrAddKalturaPlaylist(site.id, mediaCollection.getTitle(), fields);
                    if (!mediaCollection.isMigrated()) {
                        // need to update the collection description or it will be lost
                        collPlaylist.description = mediaCollection.getDescription();
                        collPlaylist = this.kalturaAPIService.saveUpdatedKalturaPlaylist(collPlaylist);
                    }

                    // migrate all items in the collection (only fetch items not already migrated)
                    List<org.sakaiproject.kaltura.dao.MediaItemDB> collectionItems = dao.findBySearch(org.sakaiproject.kaltura.dao.MediaItemDB.class, 
                            new Search(
                                    new Restriction[] {
                                            new Restriction("collection.id", mediaCollection.getId()),
                                            new Restriction("migrated", Boolean.TRUE, Restriction.NOT_EQUALS),
                                    },
                                    new Order("id", true)
                                    )
                            );
                    log.info("Kaltura migration: found "+collectionItems.size()+" unmigrated items in collection ("+mediaCollection.getIdStr()+"): "+mediaCollection.getTitle());

                    HashSet<Long> itemIds = new HashSet<Long>();
                    ArrayList<String> entryIds = new ArrayList<String>();
                    Map<String, Map<String, String>> entryCollectionFields = new HashMap<String, Map<String, String>>();
                    for (org.sakaiproject.kaltura.dao.MediaItemDB mediaItem : collectionItems) {
                        String keid = mediaItem.getKalturaId();
                        itemIds.add(mediaItem.getId());
                        KalturaMediaEntry kme = this.kalturaAPIService.getKalturaItem(keid);
                        if (kme == null) {
                            skippedBecauseDeletedItems++;
                            log.warn("Kaltura migration: Skipped kaltura entry ("+keid+") and collection item ("+mediaItem+") since the item cannot be found on the Kaltura server");
                            continue; // SHORT CIRCUIT
                        }
                        entryIds.add(mediaItem.getKalturaId());
                        // convert the owner id from the sakai internal id to the eid
                        User user = external.getUser(mediaItem.getOwnerId());
                        if (user != null) {
                            mediaItem.setOwnerId(user.username);
                        } else {
                            // if no user is found, set owner id to admin
                            mediaItem.setOwnerId(KalturaAPIService.DEFAULT_OWNER_ID);
                        }
                        // update metadata fields
                        entryCollectionFields.put(mediaItem.getKalturaId(), createEntryMetadataMap(mediaItem));
                    }

                    try {
                        // add the items metadata to the entries (only if it was not already entered)

                        // add the items into the playlist
                        this.kalturaAPIService.updateKalturaPlaylistEntries(collPlaylist, entryIds, true, entryCollectionFields);

                        if (removeExistingDataAfterVerify) {
                            dao.deleteSet(new HashSet<org.sakaiproject.kaltura.dao.MediaItemDB>(collectionItems));
                        } else {
                            // mark each item as migrated
                            dao.migrate(org.sakaiproject.kaltura.dao.MediaItemDB.class, itemIds);
                        }
                    } catch (Exception e) {
                        log.error("Kaltura migration: Unable to complete migration of "+entryIds.size()+" items into playlist ("+collPlaylist.id+") for collection: "+mediaCollection+" "+e, e);
                    }

                    if (removeExistingDataAfterVerify) {
                        dao.delete(mediaCollection);
                    } else {
                        // mark collection as migrated
                        HashSet<Long> cid = new HashSet<Long>(1);
                        cid.add(mediaCollection.getId());
                        dao.migrate(org.sakaiproject.kaltura.dao.MediaCollectionDB.class, cid);
                    }
                } catch (Exception e) {
                    log.error("Kaltura migration: Unable to migrate collection: "+mediaCollection.getIdStr()+" "+e, e);
                }
            }

            // cleanup the marker item as needed
            if (markerItem != null) {
                dao.delete(markerItem);
            }
            migrationRunning = false;
        } finally {
            external.currentRestoreNormalAccess();
        }

        log.info("Kaltura migration: migrateDatabaseToKalturaServer() completed on server ("+serverId+"): library-items: "+libraryItems.size()+", collections: "+collections.size()+", skipped "+skippedBecauseDeletedItems+" missing items");
    }


    // course data migration

    /**
     * Support for SKE-38
     * Added support for SKE-88
     * Migrates all kaltura data from one location to the other 
     * 
     * @param fromLocationId location id of the origin of the kaltura data
     * @param toLocationId location id of the destimation for the kaltura data
     * @param clearExistingDataFirst if this is true then existing data in the destination should be deleted
     */
    public void migrateLocationData(String fromLocationId, String toLocationId, boolean clearExistingDataFirst) {
        if (log.isDebugEnabled()) log.debug("migrateLocationData(from="+fromLocationId+", to="+toLocationId+", clear="+clearExistingDataFirst+")");

        if (fromLocationId == null || ExternalLogic.NO_LOCATION.equals(fromLocationId)) {
            throw new IllegalArgumentException("No fromLocationId specified, cannot migrate data");
        }
        if (toLocationId == null || ExternalLogic.NO_LOCATION.equals(toLocationId)) {
            throw new IllegalArgumentException("No toLocationId specified, cannot migrate data");
        }

        if (!this.siteArchiveSupport) {
            log.info("kaltura tool site duplication support is not enabled so no content will be copied from site ("+fromLocationId+") to site ("+toLocationId+"), use 'kaltura.archive.support.enabled = true' to enable");
            return; // SHORT CIRCUIT
        }
        String currentUserId = external.getCurrentUserId();
        if (currentUserId != null) {
            // check permissions for this user (if there is one, otherwise assume some kind of admin job)
            if (!canArchive(toLocationId, currentUserId)) { // NOTE: should we also check permissions in the origin site?
                log.warn("user ("+currentUserId+") not allowed to duplicate/migrate in ("+toLocationId+"), no data will be duplicated");
                return; // SHORT CIRCUIT
            }
        } else {
            // no current user so instead we will force the archiving process to run with admin access
            external.currentAllowAdminAccess();
        }

        String sourceSiteId;
        String destinationSiteId;
        // process the site library items
        List<KalturaMediaEntry> sourceLibraryItems;
        // process the collections (playlists)
        List<KalturaPlaylist> sourcePlaylists;
        List<MediaCollection> collections;
        List<MediaItem> items;
        try {
            KalturaCategory rootCategory = kalturaAPIService.getRootCategory();
            sourceSiteId = external.extractSiteId(fromLocationId);
            KalturaCategory sourceCategory = kalturaAPIService.getOrAddKalturaCategory(sourceSiteId, rootCategory.id);
            destinationSiteId = external.extractSiteId(toLocationId);
            KalturaCategory destinationCategory = kalturaAPIService.getOrAddKalturaCategory(destinationSiteId, rootCategory.id);
            
            // delete existing destination site library and collections, if specified
            if (clearExistingDataFirst) {
                // handle the clear flag by removing all the collections and items
                log.info("kaltura site duplication clear data requested for site ("+destinationSiteId+"), all kaltura data will be removed from the site");
                // get items in destination site library
                List<KalturaMediaEntry> destinationLibraryItems = kalturaAPIService.getKalturaItemsForCategory(destinationCategory.id, 0, -1);
                if (log.isDebugEnabled()) log.debug("kaltura migrateLocationData: found "+destinationLibraryItems.size()+" libary items in "+destinationSiteId+" to clear");
                // get all playlists
                List<KalturaPlaylist> playlists = kalturaAPIService.getPlaylistsInCategoryIds(destinationCategory.id+"");
                // if playlists exist, delete them
                for (KalturaPlaylist playlist : playlists) {
                    kalturaAPIService.deleteKalturaPlaylist(playlist.id);
                }
                // delete items from the library
                String[] kmesToRemove = new String[destinationLibraryItems.size()];
                for (int i = 0; i < destinationLibraryItems.size(); i++) {
                    kmesToRemove[i] = destinationLibraryItems.get(i).id;
                }
                kalturaAPIService.removeKalturaCategoryEntries(destinationCategory.id, kmesToRemove);
                // output clearing process messages
                if (!destinationLibraryItems.isEmpty() || !playlists.isEmpty()) {
                    log.info("kaltura site duplication: cleared "+kmesToRemove.length+" items and "+playlists.size()+" collections from site ("+destinationSiteId+") before duplicating from site ("+fromLocationId+")");
                } else {
                    log.info("kaltura site duplication: no content to clear from site ("+destinationSiteId+") before duplicating from site ("+sourceSiteId+")");
                }
            }

            sourceLibraryItems = kalturaAPIService.getKalturaItemsForCategory(sourceCategory.id, 0, 0);
            if (log.isDebugEnabled()) log.debug("kaltura migrate: found "+sourceLibraryItems.size()+" library items in "+sourceSiteId);
            String[] entryIdsToAdd = new String[sourceLibraryItems.size()];
            Map<String, Map<String, String>> destinationCategoryMetadata = new LinkedHashMap<String, Map<String, String>>();
            String[] sourceEntryIds = new String[sourceLibraryItems.size()];
            for (int i = 0; i < sourceLibraryItems.size(); i++) {
                KalturaMediaEntry entry = sourceLibraryItems.get(i);
                sourceEntryIds[i] = entry.id;
            }
            Map<String, Map<String, String>> siteEntriesMetadata = kalturaAPIService.getMetadataForEntry(sourceSiteId, sourceEntryIds);
            // process each source library item
            for (int i = 0; i < sourceLibraryItems.size(); i++) {
                KalturaMediaEntry entry = sourceLibraryItems.get(i);
                // add kalturaID to list
                entryIdsToAdd[i] = entry.id;
                // get the existing metadata for entry in old site library
                Map<String, String> entryMetadata = siteEntriesMetadata.get(entry.id);
                destinationCategoryMetadata.put(entry.id, entryMetadata);
            }
            // add the items to the destination site library
            kalturaAPIService.addKalturaCategoryEntries(destinationCategory.id, destinationSiteId, destinationCategoryMetadata, entryIdsToAdd);
            if (log.isDebugEnabled()) log.debug("kaltura migrate: created "+sourceLibraryItems.size()+" items for library in "+destinationSiteId);

            sourcePlaylists = kalturaAPIService.getPlaylistsInCategoryIds(sourceCategory.id+"");
            collections = new ArrayList<MediaCollection>(sourcePlaylists.size());
            items = new ArrayList<MediaItem>();
            // build the list of MediaCollection objects
            for (KalturaPlaylist sourcePlaylist : sourcePlaylists) {
                Map<String, String> sourcePlaylistMetadata = kalturaAPIService.getPlaylistMetadataFields(sourcePlaylist.id).get(sourcePlaylist.id);
                // set owner id on destination playlist to current user's eid
                String username = external.getCurrentUserName();
                if (username == null) {
                    username = KalturaAPIService.DEFAULT_OWNER_ID; // for cases with threaded processes
                }
                sourcePlaylistMetadata.put(METADATA_OWNER, username);
                // get or add destination playlist
                KalturaPlaylist destinationPlaylist = kalturaAPIService.getOrAddKalturaPlaylist(destinationCategory.id, sourcePlaylist.name, sourcePlaylistMetadata);
                destinationPlaylist.description = sourcePlaylist.description;
                MediaCollection mc = new MediaCollection(destinationPlaylist, toLocationId, sourcePlaylistMetadata);
                collections.add(mc);
                if (StringUtils.trimToNull(sourcePlaylist.playlistContent) != null) {
                    // get all of the entries in source playlist
                    String[] entryIds = StringUtils.splitByWholeSeparator(sourcePlaylist.playlistContent, ",");
                    List<String> entriesIds = new ArrayList<String>(entryIds.length);
                    Map<String, Map<String, String>> entriesMetadata = kalturaAPIService.getMetadataForEntry(sourcePlaylist.id+"", entryIds);
                    Map<String, Map<String, String>> destinationPlaylistMetadata = new LinkedHashMap<String, Map<String, String>>();
                    // build the list of MediaItem objects
                    for (String entryId : entryIds) {
                        entriesIds.add(entryId);
                        // get the metadata for the entry
                        Map<String, String> entryMetadata = entriesMetadata.get(entryId);
                        destinationPlaylistMetadata.put(entryId, entryMetadata);
                        MediaItem mi = new MediaItem(entryId, toLocationId, mc, entryMetadata);
                        items.add(mi);
                    }
                }
                if (log.isDebugEnabled()) log.debug("kaltura migrate: created "+sourcePlaylists.size()+" collections for site: "+destinationSiteId);
            }
        } finally {
            external.currentRestoreNormalAccess();
        }

        // output processing messages
        if (!sourceLibraryItems.isEmpty() || !sourcePlaylists.isEmpty()) {
            SaveResults results = saveInLocation(destinationSiteId, items, collections);
            log.info("kaltura site duplicate: added "+results.collectionsCount+" collections and "+results.itemsCount+" library items to site "+destinationSiteId);
        } else {
            log.info("kaltura site duplicate: no collections or items migrate from "+sourceSiteId+" to "+destinationSiteId);
        }
    }

    /**
     * @return true if site archive support is enabled
     */
    @NoProfile
    public boolean isSiteArchiveSupport() {
        return siteArchiveSupport;
    }

    /**
     * Check if the current user is allowed to do a course archive, merge, or duplicate of kaltura data
     * Super admins and kaltura admin or manager is allowed to archive data
     * 
     * @param locationId [OPTIONAL] if null then the current location is used instead
     * @param userId [OPTIONAL] if null then the current user is used instead
     * @return true if the user is allowed to archive, false otherwise
     */
    public boolean canArchive(String locationId, String userId) {
        boolean allowed = false;
        if (log.isDebugEnabled()) log.debug("canArchive(locationId="+locationId+", userId="+userId+"), siteArchiveSupport="+siteArchiveSupport);
        if (siteArchiveSupport) {
            String currentUserId = userId;
            if (userId == null) {
                currentUserId = external.getCurrentUserId();
            }
            if (log.isDebugEnabled()) log.debug("isUserAllowedArchive: userId="+currentUserId);
            if (currentUserId != null) {
                if ( external.isUserAdmin(currentUserId) ) {
                    if (log.isDebugEnabled()) log.debug("isUserAllowedArchive: user ("+currentUserId+") is admin");
                    allowed = true;
                } else {
                    if (locationId == null) {
                        locationId = external.getCurrentLocationId();
                    }
                    if (log.isDebugEnabled()) log.debug("isUserAllowedArchive: checking if user ("+currentUserId+") is admin/manager in location ("+locationId+")");
                    if (locationId != null && !ExternalLogic.NO_LOCATION.equals(locationId)) {
                        if (external.checkPerms(currentUserId, locationId, new String[] {ExternalLogic.PERM_MANAGER, ExternalLogic.PERM_ADMIN}) ) {
                            if (log.isDebugEnabled()) log.debug("isUserAllowedArchive: user ("+currentUserId+") allowed in location ("+locationId+")");
                            allowed = true;
                        }
                    }
                }
            }
        }
        if (log.isDebugEnabled()) log.debug("isUserAllowedArchive(loc="+locationId+", usr="+userId+") = "+allowed);
        return allowed;
    }

    /**
     * Saves or updates a large set of items in a location in a single transaction
     * 
     * @param locationId [OPTIONAL] if null or empty then the location in the items/collections are used, 
     *     if set then this overrides any location in the items or collections
     * @param library the items to save in the library for the location
     * @param collections the collections (and items within) to save in the location (this will not remove items)
     * @return the results of the save operation
     * @throws IllegalArgumentException if the locationId is not set AND the item/collection location ids are also not set
     */
    public SaveResults saveInLocation(String locationId, Collection<MediaItem> library, Collection<MediaCollection> collections) {
        if (log.isDebugEnabled()) {
            log.debug("saveInLocation(locationId=" + locationId + ", library, collections)");
        }
        // set SaveResults data
        SaveResults saveResults = new SaveResults();
        saveResults.locationId = locationId;
        saveResults.collectionsCount = collections.size();
        saveResults.itemsCount = library.size();
        saveResults.libraryItems = (List<MediaItem>) library;
        saveResults.collections = (List<MediaCollection>) collections;
        // save each collection as a playlist with entries
        for (MediaCollection mc : collections) {
            List<String> entryIds = new ArrayList<String>();
            Map<String, Map<String, String>> metadata = new LinkedHashMap<String, Map<String, String>>();
            for (MediaItem mi : library) {
                // only add items that are members of the collection
                if (StringUtils.equals(mc.getIdStr(), mi.getCollectionId())) {
                    String entryId = mi.getKalturaId();
                    entryIds.add(entryId);
                    metadata.put(entryId, mi.extractMetaDataMap());
                }
            }
            // update each playlist with the entries and metadata
            kalturaAPIService.saveUpdatedKalturaPlaylist(mc.getKalturaPlaylist());
            kalturaAPIService.updateKalturaPlaylistEntries(mc.getKalturaPlaylist(), entryIds, false, metadata);
        }
        return saveResults;
    }

    public static class SaveResults {
        /**
         * location the items were saved in
         */
        public String locationId = "";
        /**
         * total number of items added to the location (library and collection items)
         */
        public int itemsCount = 0;
        /**
         * total number of collections added to the location
         */
        public int collectionsCount = 0;
        public List<MediaItem> libraryItems = new ArrayList<MediaItem>(0);
        public List<MediaCollection> collections = new ArrayList<MediaCollection>(0);
    }

    @NoProfile
    public String getKalturaPartnerId() {
        return Integer.toString(kalturaConfig.getPartnerId());
    }

    @NoProfile
    public String getKalturaEndpoint() {
        return kalturaConfig.getEndpoint();
    }

    @NoProfile
    public String getKalturaTimeout() {
        return Integer.toString(kalturaConfig.getTimeout());
    }

    /**
     * creates metadata map for a MediaItemDB object
     * 
     * @param mediaItem media object
     * @return HashMap of metadata for object
     * @deprecated this should not be used except for the DB to kaltura server migration
     * 
     * WARNING: when this is deleted - make sure you cleanup the WARNING comment in MediaItem.extractMetadataMap()
     */
    private Map<String, String> createEntryMetadataMap(org.sakaiproject.kaltura.dao.MediaItemDB mediaItem) {
        Map<String, String> metadata = new LinkedHashMap<String, String>();
        metadata.put(METADATA_HIDDEN, mediaItem.isHidden() ? "H" : "h");
        metadata.put(METADATA_REUSABLE, mediaItem.isShared() ? "S" : "s");
        metadata.put(METADATA_REMIXABLE, mediaItem.isRemixable() ? "R" : "r");
        metadata.put(METADATA_OWNER, mediaItem.getOwnerId());
        return metadata;
    }

    /**
     * creates metadata map for a MediaCollectionDB object
     * 
     * @param mediaCollection media object
     * @return HashMap of metadata for object
     * @deprecated this should not be used except for the DB to kaltura server migration
     * 
     * WARNING: when this is deleted - make sure you cleanup the WARNING comment in MediaCollection.extractMetadataMap()
     */
    private Map<String, String> createPlaylistMetadataMap(org.sakaiproject.kaltura.dao.MediaCollectionDB mediaCollection) {
        Map<String, String> metadata = new LinkedHashMap<String, String>();
        metadata.put("Title", mediaCollection.getTitle());
        metadata.put("Type", mediaCollection.getSharing());
        metadata.put(METADATA_HIDDEN, mediaCollection.isHidden() ? "1" : "0");
        metadata.put(METADATA_OWNER, mediaCollection.getOwnerId());
        return metadata;
    }
}
