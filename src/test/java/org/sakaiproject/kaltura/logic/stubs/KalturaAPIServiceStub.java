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
package org.sakaiproject.kaltura.logic.stubs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.sf.ehcache.Ehcache;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.kaltura.logic.KalturaAPIService;
import org.sakaiproject.kaltura.logic.MediaService;
import org.sakaiproject.kaltura.logic.User;
import org.sakaiproject.kaltura.model.MediaCollection;
import org.sakaiproject.kaltura.model.MediaItem;

import com.kaltura.client.enums.KalturaEntryType;
import com.kaltura.client.enums.KalturaMediaType;
import com.kaltura.client.enums.KalturaPlaylistType;
import com.kaltura.client.types.KalturaCategory;
import com.kaltura.client.types.KalturaCategoryEntry;
import com.kaltura.client.types.KalturaMediaEntry;
import com.kaltura.client.types.KalturaMetadata;
import com.kaltura.client.types.KalturaPlaylist;

public class KalturaAPIServiceStub extends KalturaAPIService {

    public ExternalLogicStub external;

    public KalturaAPIServiceStub(ExternalLogicStub external, Ehcache entriesCache, Ehcache categoriesCache) {
        this.external = external;
        setExternal(external);
        setEntriesCache(entriesCache);
        setCategoriesCache(categoriesCache);
        init();
    }

    public void init() {
        super.setOffline(true);
        super.init();

        createMockItems();
        createMockCategories();
        createMockCollections();
    }

    /**
     * current user, access level user in LOCATION_ID1
     */
    public final static String USER_ID = "user-11111111";
    public final static String USER_NAME = "user";
    public final static String USER_DISPLAY = "Aaron Zeckoski";
    /**
     * access level user in LOCATION1_ID
     */
    public final static String ACCESS_USER_ID = "access-2222222";
    public final static String ACCESS_USER_NAME = "access";
    public final static String ACCESS_USER_DISPLAY = "Regular User";

    public final static String ACCESS_USER_ID_W = "access-write";
    public final static String ACCESS_USER_NAME_W = "accessw";
    public final static String ACCESS_USER_DISPLAY_W = "Regular User Write";

    public final static String ACCESS_USER_ID_E = "access-editor";
    public final static String ACCESS_USER_NAME_E = "accesse";
    public final static String ACCESS_USER_DISPLAY_E = "Regular User Editor";

    /**
     * maintain level user in LOCATION1_ID
     */
    public final static String MAINT_USER_ID = "maint-33333333";
    public final static String MAINT_USER_NAME = "maint";
    public final static String MAINT_USER_DISPLAY = "Maint User";

    public final static String MAINT_USER_ID_A = "maint-Admin";
    public final static String MAINT_USER_NAME_A = "maintb";
    public final static String MAINT_USER_DISPLAY_A = "Maint User Admin";

    public final static String MAINT_USER_ID_M = "maint-Manager";
    public final static String MAINT_USER_NAME_M = "maintb";
    public final static String MAINT_USER_DISPLAY_M = "Maint User Manage";

    /**
     * super admin user
     */
    public final static String ADMIN_USER_ID = "admin";
    public final static String ADMIN_USER_NAME = "admin";
    public final static String ADMIN_USER_DISPLAY = "Administrator";
    /**
     * Invalid user (also can be used to simulate the anonymous user)
     */
    public final static String INVALID_USER_ID = "invalid-UUUUUU";

    /**
     * current location
     */
    public final static String LOCATION1_ID = "/site/ref-1111111";
    public final static String LOCATION1_TITLE = "Location 1 title";
    public final static String LOCATION2_ID = "/site/ref-22222222";
    public final static String LOCATION2_TITLE = "Location 2 title";
    public final static String INVALID_LOCATION_ID = "invalid-LLLLLLLL";

    // testing data objects here

    // categories
    public KalturaCategory categoryRoot;
    public KalturaCategory categoryLoc1;
    public KalturaCategory categoryLoc2;

    // playlists
    public final static String PLAYLIST1_ID = "pl_123456";

    // Sharing const
    private final boolean SHARED = true;
    private final boolean NOT_SHARED = false;

    // library items
    public MediaItem miL1 = new MediaItem(LOCATION1_ID, "kid1", MAINT_USER_NAME, true, NOT_SHARED, false); // private
    public MediaItem miL2 = new MediaItem(LOCATION1_ID, "kid2", MAINT_USER_NAME, false, SHARED, true); // public
    public MediaItem miL3 = new MediaItem(LOCATION1_ID, "kid3", ADMIN_USER_NAME, false, SHARED, true); // public
    public MediaItem miL4 = new MediaItem(LOCATION1_ID, "kid4", MAINT_USER_NAME, false, SHARED, false); // public
    public MediaItem miL5 = new MediaItem(LOCATION1_ID, "kid5", MAINT_USER_NAME, true, SHARED, false); // private
    public MediaItem miL6 = new MediaItem(LOCATION1_ID, "kid6", ACCESS_USER_NAME_W, false, NOT_SHARED, true); // public
    public MediaItem miL7 = new MediaItem(LOCATION1_ID, "kid7", ACCESS_USER_NAME_W, true, NOT_SHARED, false); // private
    public MediaItem miL8 = new MediaItem(LOCATION1_ID, "kid8", ACCESS_USER_NAME_E, true, NOT_SHARED, false); // private

    public MediaItem miNO_A = new MediaItem(null, "kidA", ADMIN_USER_NAME, false, SHARED, false); // for adding item tests
    public MediaItem miNO_B = new MediaItem(null, "kidB", ADMIN_USER_NAME, false, SHARED, false); // for adding item tests
    // NOTE: the test data used to have duplicate kaltura ids in it, this is no longer the case

    // collections
    /** sharing: PRIVATE, owner: MAINT */
    public MediaCollection mcPr = new MediaCollection("mcPr", LOCATION1_ID, MAINT_USER_NAME, "collection Pr", false, MediaCollection.SHARING_PRIVATE);
    /** sharing: ADMIN, owner: MAINT */
    public MediaCollection mcAd = new MediaCollection("mcAd", LOCATION1_ID, MAINT_USER_NAME, "collection Ad", false, MediaCollection.SHARING_ADMIN);
    /** sharing: SHARED, owner: MAINT */
    public MediaCollection mcSh = new MediaCollection("mcSh", LOCATION1_ID, MAINT_USER_NAME, "collection Sh", false, MediaCollection.SHARING_SHARED);
    /** sharing: PUBLIC, owner: MAINT */
    public MediaCollection mcPu = new MediaCollection("mcPu", LOCATION1_ID, MAINT_USER_NAME, "collection Pu", false, MediaCollection.SHARING_PUBLIC);

    public MediaItem miPr_L1 = new MediaItem(mcPr, miL1); // private
    public MediaItem miPr_L2 = new MediaItem(mcPr, miL2); // public
    public MediaItem miPr_L5 = new MediaItem(mcPr, miL5); // private

    public MediaItem miAd_L1 = new MediaItem(mcAd, miL1); // private
    public MediaItem miAd_L2 = new MediaItem(mcAd, miL2); // public
    public MediaItem miAd_L3 = new MediaItem(mcAd, miL3); // public
    public MediaItem miAd_L5 = new MediaItem(mcAd, miL5); // private
    public MediaItem miAd_L6 = new MediaItem(mcAd, miL6); // public

    public MediaItem miSh_L1 = new MediaItem(mcSh, miL1); // private
    public MediaItem miSh_L2 = new MediaItem(mcSh, miL2); // public

    public MediaItem miPu_L1 = new MediaItem(mcPu, miL1); // private
    public MediaItem miPu_L2 = new MediaItem(mcPu, miL2); // public

    /** sharing: SHARED, owner: MAINT, hidden */
    public MediaCollection mcShH = new MediaCollection("mcShH", LOCATION1_ID, MAINT_USER_NAME, "collection Sh Hi", true, MediaCollection.SHARING_ADMIN);
    /** sharing: PRIVATE, owner: MAINT, hidden */
    public MediaCollection mcPrH = new MediaCollection("mcPrH", LOCATION1_ID, MAINT_USER_NAME, "collection Pr Hi", true, MediaCollection.SHARING_PRIVATE);

    public MediaItem mcShH_L1 = new MediaItem(mcShH, miL1); // private
    public MediaItem mcShH_L2 = new MediaItem(mcShH, miL2); // public

    public MediaItem mcPrH_L1 = new MediaItem(mcPrH, miL1); // private
    public MediaItem mcPrH_L2 = new MediaItem(mcPrH, miL2); // public

    /** sharing: PRIVATE, owner: MAINT, EMPTY */
    public MediaCollection mcPrE = new MediaCollection("mcPrE", LOCATION1_ID, MAINT_USER_NAME, "collection Pr E", false, MediaCollection.SHARING_PRIVATE);
    /** sharing: ADMIN, owner: MAINT, EMPTY */
    public MediaCollection mcAdE = new MediaCollection("mcAdE", LOCATION1_ID, MAINT_USER_NAME, "collection Ad E", false, MediaCollection.SHARING_ADMIN);
    /** sharing: SHARED, owner: MAINT, EMPTY */
    public MediaCollection mcShE = new MediaCollection("mcShE", LOCATION1_ID, MAINT_USER_NAME, "collection Sh E", false, MediaCollection.SHARING_SHARED);
    /** sharing: PUBLIC, owner: MAINT, EMPTY */
    public MediaCollection mcPuE = new MediaCollection("mcPuE", LOCATION1_ID, MAINT_USER_NAME, "collection Pu E", false, MediaCollection.SHARING_PUBLIC);

    /** sharing: PRIVATE, owner: MAINT, private only */
    public MediaCollection mcPrNoPub = new MediaCollection("mcPrNoPub", LOCATION1_ID, MAINT_USER_NAME, "collection Pr NoPub", false, MediaCollection.SHARING_PRIVATE);
    /** sharing: ADMIN, owner: MAINT, private only */
    public MediaCollection mcAdNoPub = new MediaCollection("mcAdNoPub", LOCATION1_ID, MAINT_USER_NAME, "collection Ad NoPub", false, MediaCollection.SHARING_ADMIN);
    /** sharing: SHARED, owner: MAINT, private only */
    public MediaCollection mcShNoPub = new MediaCollection("mcShNoPub", LOCATION1_ID, MAINT_USER_NAME, "collection Sh NoPub", false, MediaCollection.SHARING_SHARED);
    /** sharing: PUBLIC, owner: MAINT, private only */
    public MediaCollection mcPuNoPub = new MediaCollection("mcPuNoPub", LOCATION1_ID, MAINT_USER_NAME, "collection Pu NoPub", false, MediaCollection.SHARING_PUBLIC);

    public MediaItem mcPrNoPub_L1 = new MediaItem(mcPrNoPub, miL1); // private
    public MediaItem mcAdNoPub_L1 = new MediaItem(mcAdNoPub, miL1); // private
    public MediaItem mcShNoPub_L1 = new MediaItem(mcShNoPub, miL1); // private
    public MediaItem mcPuNoPub_L1 = new MediaItem(mcPuNoPub, miL1); // private

    public List<KalturaCategory> kalturaCategories;
    /**
     * Map of kaltura category ids -> media items in the category
     */
    public Map<Integer, List<MediaItem>> kalturaCategoryItems;
    private void createMockCategories() {
        categoryRoot = makeSampleKC(80001, "CatRoot", -1);
        categoryRoot.membersCount = 2;
        categoryLoc1 = makeSampleKC(81000, LOCATION1_ID, categoryRoot.id);
        categoryLoc2 = makeSampleKC(82000, LOCATION2_ID, categoryRoot.id);

        kalturaCategoryItems = new HashMap<Integer, List<MediaItem>>(3);
        kalturaCategoryItems.put(categoryRoot.id, new ArrayList<MediaItem>(0));
        ArrayList<MediaItem> catItems = new ArrayList<MediaItem>(8);
        catItems.add(miL1);
        catItems.add(miL2);
        catItems.add(miL3);
        catItems.add(miL4);
        catItems.add(miL5);
        catItems.add(miL6);
        catItems.add(miL7);
        catItems.add(miL8);
        categoryLoc1.directEntriesCount = catItems.size();
        categoryLoc1.entriesCount = catItems.size();
        kalturaCategoryItems.put(categoryLoc1.id, catItems);
        kalturaCategoryItems.put(categoryLoc2.id, new ArrayList<MediaItem>(0));

        kalturaCategories = new ArrayList<KalturaCategory>();
        kalturaCategories.add(categoryRoot);
        kalturaCategories.add(categoryLoc1);
        kalturaCategories.add(categoryLoc2);
    }

    private KalturaCategory makeSampleKC(int id, String name, int parentId) {
        KalturaCategory kc = new KalturaCategory();
        kc.createdAt = (int) (System.currentTimeMillis() / 1000);
        kc.id = id;
        kc.name = external.extractSiteId(name);
        kc.description = "Description: "+id;
        kc.parentId = parentId;
        kc.fullName = "sakai>"+name;
        kc.depth = (parentId > 0 ? 2 : 1);
        kc.tags = "aaron,test,tags";
        kc.directEntriesCount = 0;
        kc.directSubCategoriesCount = 0;
        kc.membersCount = 0;
        kc.entriesCount = 0;
        return kc;
    }

    public List<MediaCollection> mockCollections;
    private void createMockCollections() {
        mockCollections = new ArrayList<MediaCollection>();
        ArrayList<MediaItem> items;

        // collection items
        items = new ArrayList<MediaItem>(3);
        items.add(miPr_L1);
        items.add(miPr_L2);
        items.add(miPr_L5);
        mcPr.setItems(items);
        miPr_L1.setCollection(mcPr);
        miPr_L2.setCollection(mcPr);
        miPr_L5.setCollection(mcPr);
        items = new ArrayList<MediaItem>(5);
        items.add(miAd_L1);
        items.add(miAd_L2);
        items.add(miAd_L3);
        items.add(miAd_L5);
        items.add(miAd_L6);
        mcAd.setItems(items);
        miAd_L1.setCollection(mcAd);
        miAd_L2.setCollection(mcAd);
        miAd_L3.setCollection(mcAd);
        miAd_L5.setCollection(mcAd);
        miAd_L6.setCollection(mcAd);
        items = new ArrayList<MediaItem>(2);
        items.add(miSh_L1);
        items.add(miSh_L2);
        mcSh.setItems(items);
        miSh_L1.setCollection(mcSh);
        miSh_L2.setCollection(mcSh);
        items = new ArrayList<MediaItem>(2);
        items.add(miPu_L1);
        items.add(miPu_L2);
        mcPu.setItems(items);
        miPu_L1.setCollection(mcPu);
        miPu_L2.setCollection(mcPu);

        mockCollections.add(mcPr);
        mockCollections.add(mcAd);
        mockCollections.add(mcSh);
        mockCollections.add(mcPu);

        // collection items
        items = new ArrayList<MediaItem>(2);
        items.add(mcShH_L1);
        items.add(mcShH_L2);
        mcShH.setItems(items);
        mcShH_L1.setCollection(mcShH);
        mcShH_L2.setCollection(mcShH);
        items = new ArrayList<MediaItem>(2);
        items.add(mcPrH_L1);
        items.add(mcPrH_L2);
        mcPrH.setItems(items);
        mcPrH_L1.setCollection(mcPrH);
        mcPrH_L2.setCollection(mcPrH);

        mockCollections.add(mcShH);
        mockCollections.add(mcPrH);
        mockCollections.add(mcPrE);
        mockCollections.add(mcAdE);
        mockCollections.add(mcShE);
        mockCollections.add(mcPuE);

        // collection items
        items = new ArrayList<MediaItem>(1);
        items.add(mcPrNoPub_L1);
        mcPrNoPub.setItems(items);
        mcPrNoPub_L1.setCollection(mcPrNoPub);
        items = new ArrayList<MediaItem>(1);
        items.add(mcAdNoPub_L1);
        mcAdNoPub.setItems(items);
        mcAdNoPub_L1.setCollection(mcAdNoPub);
        items = new ArrayList<MediaItem>(1);
        items.add(mcShNoPub_L1);
        mcShNoPub.setItems(items);
        mcShNoPub_L1.setCollection(mcShNoPub);
        items = new ArrayList<MediaItem>(1);
        items.add(mcPuNoPub_L1);
        mcPuNoPub.setItems(items);
        mcPuNoPub_L1.setCollection(mcPuNoPub);

        mockCollections.add(mcPrNoPub);
        mockCollections.add(mcAdNoPub);
        mockCollections.add(mcShNoPub);
        mockCollections.add(mcPuNoPub);
    }

    /**
     * MediaItem object list
     */
    public List<MediaItem> mockItems;
    private void createMockItems() {
        mockItems = new ArrayList<MediaItem>();
        // only the library items (since the other items have the same IDs as the library ones)
        mockItems.add(miL1);
        mockItems.add(miL2);
        mockItems.add(miL3);
        mockItems.add(miL4);
        mockItems.add(miL5);
        mockItems.add(miL6);
        mockItems.add(miL7);
        mockItems.add(miL8);
        // items NOT in a library yet
        mockItems.add(miNO_A);
        mockItems.add(miNO_B);
    }

    /**
     * Generate fake but realistic KME from MediaItem
     * @param m MediaItem object
     * @return sample KME
     */
    public KalturaMediaEntry makeSampleKME(MediaItem mi) {
        KalturaMediaEntry kme = new KalturaMediaEntry();
        kme.createdAt = (int) (System.currentTimeMillis() / 1000);
        kme.description = "this is a sample item, it is item number "+mi.getIdStr();
        kme.id = mi.getKalturaId();
        kme.name = "sample "+mi.getIdStr();
        kme.partnerId = 12345678;
        kme.tags = "aaron,test,tags";
        kme.thumbnailUrl = "images/sample_thumbnail.png";
        kme.type = KalturaEntryType.AUTOMATIC;
        kme.userId = mi.getOwnerId();
        kme.creatorId = mi.getCreatorUserId();
        kme.version = 1;
        // media only
        kme.dataUrl = "images/sample_thumbnail.png";
        kme.duration = 100;
        kme.height = 300;
        kme.mediaDate = (int) (System.currentTimeMillis() / 1000);
        kme.mediaType = KalturaMediaType.VIDEO;
        kme.width = 400;
        MediaCollection c = mi.getCollection();
        if (c != null) {
            KalturaCategory cat = getKalturaCategory(external.extractSiteId(c.getLocationId()), categoryRoot.id);
            kme.categoriesIds = cat.id+"";
        }
        return kme;
    }

    /**
     * Generate fake but realistic playlist from MediaCollection
     * @param m MediaCollection object
     * @return sample KalturaPlaylist object
     */
    public KalturaPlaylist makeSamplePlaylist(MediaCollection mc) {
        KalturaPlaylist playlist = new KalturaPlaylist();
        playlist.id = mc.getIdStr();
        playlist.name = mc.getTitle();
        playlist.creatorId = mc.getOwnerId();
        playlist.createdAt = (int) (System.currentTimeMillis() / 1000);
        playlist.description = "this is a sample playlist, it is id "+mc.getIdStr();
        KalturaCategory category = getKalturaCategory(external.extractSiteId(mc.getLocationId()), getRootCategory().id);
        if (category != null) {
            playlist.categoriesIds = category.id+"";
        }
        ArrayList<String> mIds = new ArrayList<String>();
        if (mc.getItems() != null) {
            for (MediaItem mi : mc.getItems()) {
                mIds.add(mi.getIdStr());
            }
        }
        playlist.playlistContent = StringUtils.join(mIds, ",");
        return playlist;
    }


    // Mock KalturaAPIService methods

    // CATEGORIES

    @Override
    protected KalturaCategory getKalturaCategory(String categoryName, int parentId) {
        categoryName = makeSafeCategoryName(categoryName);
        KalturaCategory kc = null;
        if (parentId <= 0) {
            kc = this.categoryRoot;
        } else {
            for (KalturaCategory category : this.kalturaCategories) {
                if (StringUtils.equals(categoryName, category.name) && category.parentId == parentId) {
                    kc = category;
                    break;
                }
            }
        }
        return kc;
    }

    @Override
    protected KalturaCategory getKalturaCategoryById(String categoryId) {
        int cid = Integer.parseInt(categoryId);
        KalturaCategory kc = null;
        for (KalturaCategory category : this.kalturaCategories) {
            if (cid == category.id) {
                kc = category;
                break;
            }
        }
        return kc;
    }

    @Override
    protected KalturaCategory addKalturaCategory(String categoryName, int parentId) {
        KalturaCategory kc;
        if (parentId <= 0) {
            kc = this.categoryRoot;
        } else {
            kc = makeSampleKC(new Random().nextInt(1000)+83000, categoryName, parentId);
            this.kalturaCategories.add(kc);
        }
        return kc;
    }

    @Override
    protected List<KalturaCategoryEntry> addKalturaCategoryEntries(int categoryId, String categoryName, Map<String, Map<String, String>> metadata,
            String... entryIds) {
        List<KalturaCategoryEntry> kalturaCategoryEntries = new ArrayList<KalturaCategoryEntry>();
        if (entryIds != null && entryIds.length > 0) {
            // eliminate the duplicates
            HashSet<String> entryIdsSet = new HashSet<String>(entryIds.length);
            for (String keid : entryIds) {
                entryIdsSet.add(keid);
            }
            // get the current list of items in the category and remove them from the set of entries to add
            KalturaCategory category = getKalturaCategoryById(categoryId+"");
            for (MediaItem mi : mockItems) {
                if (StringUtils.equals(external.extractSiteId(mi.getLocationId()), category.name)) {
                    entryIdsSet.remove(mi.getIdStr());
                }
            }
            String locationId = external.fixLocationId(categoryName);
            for (String keid : entryIdsSet) {
                KalturaCategoryEntry kce = new KalturaCategoryEntry();
                kce.categoryId = categoryId;
                kce.entryId = keid;
                kalturaCategoryEntries.add(kce);
                KalturaMediaEntry kme = getKalturaItem(keid);
                // add metadata for the entry
                if (kme != null) {
                    MediaItem mediaItem = new MediaItem(locationId, kme, null);
                    Map<String, String> entryMetadata = null;
                    if (MapUtils.isNotEmpty(metadata)) {
                        entryMetadata = metadata.get(keid);
                    }
                    // update the entry's metadata, if necessary
                    MediaService.updateEntryMetadataIfNeeded(mediaItem, METADATA_PERMISSIONS_CONTAINER_TYPE_SITE, categoryName, locationId, entryMetadata, this, external);
                }
                // now we need to add these to the storage (both the category items AND the mock items)
                MediaItem mediaItem = null;
                for (MediaItem mi : mockItems) {
                    if (StringUtils.equals(mi.getIdStr(), keid)) {
                        mediaItem = new MediaItem(null, mi); // make copy
                        break;
                    }
                }
                if (mediaItem != null) {
                    mediaItem.setLocationId(locationId);
                    mediaItem.setOwnerId(external.getCurrentUserName());
                    kalturaCategoryItems.get(categoryId).add(mediaItem);
                    mockItems.add(mediaItem);
                }
            }
        }
        return kalturaCategoryEntries;
    }

    @Override
    public int removeKalturaCategoryEntries(int categoryId, String... entryIds) {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Invalid category Id ("+categoryId+"), must be > 0");
        }
        int removed = 0;
        // get media items for category
        List<MediaItem> mediaItems = kalturaCategoryItems.get(categoryId);
        for (String entryId : entryIds) {
            // check each item, if found in list, remove it
            for (Iterator<MediaItem> mii = mediaItems.iterator();mii.hasNext();) {
                MediaItem mi = mii.next();
                if (StringUtils.equals(mi.getIdStr(), entryId)) {
                    mii.remove();
                    removed++;
                    break;
                }
            }
        }
        return removed;
    }

    @Override
    protected List<KalturaMediaEntry> getKalturaItemsForCategory(int categoryId, int start, int max) {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Invalid category Id ("+categoryId+"), must be > 0");
        }
        List<KalturaMediaEntry> kalturaMediaEntries = new ArrayList<KalturaMediaEntry>(0);
        // get media items for category
        List<MediaItem> mediaItems = kalturaCategoryItems.get(categoryId);
        // make each media item a kaltura media entry and add it to list 
        for (MediaItem mi : mediaItems) {
            KalturaMediaEntry kme = makeSampleKME(mi);
            kalturaMediaEntries.add(kme);
        }
        return kalturaMediaEntries;
    }

    // PLAYLISTS

    @Override
    protected KalturaPlaylist updateKalturaPlaylistEntries(KalturaPlaylist playlist, List<String> entryIds, boolean replace,
            Map<String, Map<String, String>> permissions) {
        if (playlist == null) {
            throw new IllegalArgumentException("playlist cannot be null");
        }
        // convert entryIds to comma separated string
        String entries = StringUtils.join(entryIds, ",");
        // check if playlist entries are the same
        boolean changed = !entries.equals(playlist.playlistContent);
        if (changed) {
            //if not replacing current entries, get current playlist entries to add
            HashSet<String> removedEntries = new HashSet<String>();
            if (!replace && playlist.playlistContent != null) {
                List<String> existingEntries = Arrays.asList(StringUtils.split(playlist.playlistContent, ','));
                removedEntries.addAll(existingEntries);
                // add new entryIds to the end
                existingEntries.addAll(entryIds);
                // converting to set removes duplicates
                Set<String> entrySet = new LinkedHashSet<String>(existingEntries);
                entries = StringUtils.join(entrySet, ',');
            }
            removedEntries.removeAll(entryIds);
            playlist.playlistContent = entries;
            // create list of media items to add to collection
            List<MediaItem> mediaItems = new ArrayList<MediaItem>();
            for (String entry : StringUtils.split(entries, ',')) {
                for (MediaItem mi : mockItems) {
                    if (StringUtils.equals(mi.getIdStr(), entry)) {
                        mediaItems.add(mi);
                        break;
                    }
                }
            }
            // get the collection
            for (Iterator<MediaCollection> mci = mockCollections.iterator();mci.hasNext();) {
                MediaCollection mediaCollection = mci.next();
                if (StringUtils.equals(mediaCollection.getIdStr(), playlist.id)) {
                    // add the items to the collection
                    mediaCollection.setItems(mediaItems);
                    break;
                }
            }
            // update the entries' metadata objects
            if (permissions != null) { // if no permissions to change, don't update
                for (String entryId : entryIds) {
                    updateEntryMetadata(entryId, METADATA_PERMISSIONS_CONTAINER_TYPE_PLAYLIST, playlist.id, permissions.get(entryId), false);
                }
            }
        }
        return playlist;
    }

    @Override
    protected boolean removeItemFromKalturaPlaylist(KalturaPlaylist playlist, String kalturaId) {
        if (playlist == null) {
            throw new IllegalArgumentException("playlist is not set");
        }
        if (StringUtils.isEmpty(kalturaId)) {
            throw new IllegalArgumentException("kaltura entry id is not set");
        }
        boolean removed = false;
        if (playlist.playlistContent != null) {
            // must create a mutable ArrayList in order to remove item here
            List<String> entries = new ArrayList<String>(Arrays.asList(StringUtils.splitByWholeSeparator(playlist.playlistContent, ",")));
            boolean changed = entries.remove(kalturaId);
            if (changed) {
                MediaCollection mc = null;
                if (entries.isEmpty()) {
                    entries.add(KalturaAPIService.DEFAULT_PLAYLIST_EMPTY);
                } else {
                    KalturaPlaylist newPlaylist = new KalturaPlaylist();
                    newPlaylist.playlistContent = StringUtils.join(entries, ",");
                    // get the collection
                    for (Iterator<MediaCollection> mci = mockCollections.iterator();mci.hasNext();) {
                        MediaCollection mediaCollection = mci.next();
                        if (StringUtils.equals(mediaCollection.getIdStr(), playlist.id)) {
                            mc = mediaCollection;
                            break;
                        }
                    }
                }
                // add the items to the collection
                if (mc != null) {
                    // create list of media items to add to collection
                    List<MediaItem> mediaItems = new ArrayList<MediaItem>();
                    for (String entry : entries) {
                        for (MediaItem mi : mockItems) {
                            if (StringUtils.equals(mi.getIdStr(), entry)) {
                                mediaItems.add(mi);
                                break;
                            }
                        }
                    }
                    mc.setItems(mediaItems);
                }
                removed = true;
            }
        }
        return removed;
    }

    @Override
    protected KalturaPlaylist addKalturaPlaylist(int categoryId, String name) {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("cannot add playlist to invalid categoryId");
        }
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("playlist name cannot be empty");
        }
        KalturaPlaylist kalturaPlaylist = new KalturaPlaylist();
        kalturaPlaylist.id = PLAYLIST1_ID;
        kalturaPlaylist.categoriesIds = categoryId+"";
        kalturaPlaylist.name = name;
        kalturaPlaylist.playlistType = KalturaPlaylistType.STATIC_LIST;
        User u = external.getCurrentUser();
        kalturaPlaylist.creatorId = (u != null ? u.getUsername() : ADMIN_USER_NAME);
        KalturaCategory category = getKalturaCategoryById(categoryId+"");
        MediaCollection mc = new MediaCollection(kalturaPlaylist, external.fixLocationId(category.name), null);
        mc.setOwnerId(kalturaPlaylist.creatorId);
        mockCollections.add(mc);
        return kalturaPlaylist;
    }

    @Override
    protected KalturaPlaylist getKalturaPlaylist(int categoryId, String playlistName) {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("cannot get playlist for invalid categoryId");
        }
        if (StringUtils.isEmpty(playlistName)) {
            throw new IllegalArgumentException("playlist name cannot be empty");
        }
        KalturaPlaylist playlist = null;
        KalturaCategory category = getKalturaCategoryById(categoryId+"");
        for (MediaCollection mc : mockCollections) {
            // if collection is in category and the name matches
            if (StringUtils.equals(external.extractSiteId(mc.getLocationId()), category.name) && StringUtils.equals(mc.getTitle(), playlistName)) {
                // create the playlist
                playlist = makeSamplePlaylist(mc);
                break;
            }
        }
        return playlist;
    }

    @Override
    public KalturaPlaylist getPlaylistByPlaylistId(String playlistId) {
        if (StringUtils.isEmpty(playlistId)) {
            throw new IllegalArgumentException("playlist id must be set");
        }
        KalturaPlaylist playlist = null;
        for (MediaCollection mc : mockCollections) {
            if (StringUtils.equals(mc.getId(), playlistId)) {
                playlist = makeSamplePlaylist(mc);
                break;
            }
        }
        return playlist;
    }

    @Override
    protected boolean deleteKalturaPlaylist(String playlistId) {
        if (StringUtils.isEmpty(playlistId)) {
            throw new IllegalArgumentException("playlist id must be set");
        }
        // find collection with matching id and remove it
        for (Iterator<MediaCollection> mci = mockCollections.iterator();mci.hasNext();) {
            MediaCollection mc = mci.next();
            if (StringUtils.equals(mc.getIdStr(), playlistId)) {
                mci.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    protected List<KalturaPlaylist> getPlaylistsInCategoryIds(String... categoryIds) {
        if (categoryIds == null || categoryIds.length <= 0) {
            throw new IllegalArgumentException("category ids must be set and contain at least one category id");
        }
        List<KalturaPlaylist> kalturaPlaylists = new ArrayList<KalturaPlaylist>(0);
        for (String categoryId : categoryIds) {
            KalturaCategory category = getKalturaCategoryById(categoryId);
            for (MediaCollection mc : mockCollections) {
                if (StringUtils.equals(external.extractSiteId(mc.getLocationId()), category.name)) {
                    KalturaPlaylist playlist = makeSamplePlaylist(mc);
                    kalturaPlaylists.add(playlist);
                }
            }
        }
        return kalturaPlaylists;
    }

    @Override
    protected KalturaPlaylist saveUpdatedKalturaPlaylist(KalturaPlaylist modifiedPlaylist) {
        if (modifiedPlaylist == null) {
            throw new IllegalArgumentException("modified playlist must be set");
        }
        String playlistId = modifiedPlaylist.id;
        if (StringUtils.isEmpty(playlistId)) {
            throw new IllegalArgumentException("playlist id list must be set");
        }
        KalturaPlaylist playlist = getPlaylistByPlaylistId(playlistId);
        // if playlist doesn't exist, create a new one
        if (playlist == null) {
            playlist = new KalturaPlaylist();
        } else { // if playlist exists, update it
            // only allow certain playlist data to be modified
            // description
            if (!StringUtils.equals(playlist.description, modifiedPlaylist.description)) {
                playlist.description = modifiedPlaylist.description;
            }
            // name
            if (!StringUtils.equals(playlist.name, modifiedPlaylist.name)) {
                playlist.name = modifiedPlaylist.name;
            }
            for (Iterator<MediaCollection> mci = mockCollections.iterator(); mci.hasNext();) {
                MediaCollection mc = mci.next();
                if (StringUtils.equals(mc.getIdStr(), playlistId)) {
                    mc.setDescription(modifiedPlaylist.description);
                    mc.setTitle(modifiedPlaylist.name);
                    break;
                }
            }
        }
        return playlist;
    }

    @Override
    protected KalturaMetadata updatePlaylistMetadata(String playlistId, Map<String, String> newMetadata) {
        if (StringUtils.isEmpty(playlistId)) {
            throw new IllegalArgumentException("playlist id must be set");
        }
        if (newMetadata == null) {
            newMetadata = new LinkedHashMap<String, String>();
        }
        // get existing metadata for playlist
        Map<String, String> metadata = getPlaylistMetadataFields(playlistId).get(playlistId);
        // update metadata key => value pairs with new values
        metadata.putAll(newMetadata);
        String xml = createPlaylistXmlMetadataString(metadata);
        KalturaMetadata kalturaMetadata = new KalturaMetadata();
        kalturaMetadata.xml = xml;
        return kalturaMetadata;
    }

    @Override
    protected Map<String, Map<String, String>> getPlaylistMetadataFields(String... playlistIds) {
        HashMap<String, Map<String, String>> m;
        if (playlistIds == null) {
            throw new IllegalArgumentException("playlistIds must not be null or empty");
        } else if (playlistIds.length == 0) {
            m = new HashMap<String, Map<String, String>>(0);
        } else {
            m = new HashMap<String, Map<String, String>>(playlistIds.length);
            for (String playlistId : playlistIds) {
                Map<String, String> metadata = new LinkedHashMap<String, String>();
                for (MediaCollection mc : mockCollections) {
                    if (StringUtils.equals(mc.getIdStr(), playlistId)) {
                        metadata = mc.extractMetadataMap();
                        break;
                    }
                }
                m.put(playlistId, metadata);
            }
        }
        return m;
    }


    // ENTRIES

    @Override
    protected KalturaMetadata updateEntryMetadata(String entryId, String containerType, String containerId, Map<String, String> permissions,
            boolean remove) {
        if (StringUtils.isEmpty(entryId)) {
            throw new IllegalArgumentException("entry id must be set");
        }
        if (StringUtils.isEmpty(containerId)) {
            throw new IllegalArgumentException("container id must be set");
        }
        if (permissions == null) {
            permissions = new HashMap<String, String>(0);
        }
        KalturaMetadata kalturaMetadata = new KalturaMetadata();
        List<MediaItem> itemsList = mockItems;
        if (StringUtils.equals("playlist", containerType)) {
            for (MediaCollection mc : mockCollections) {
                if (StringUtils.equals(mc.getId(), containerId)) {
                    itemsList = mc.getItems();
                    break;
                }
            }
        }
        if (itemsList != null) {
            for (MediaItem mediaItem : itemsList) {
                if (StringUtils.equals(mediaItem.getIdStr(), entryId)) {
                    Map<String, String> metadata = mediaItem.extractMetaDataMap();
                    metadata.putAll(permissions);
                    mediaItem.updateFromMetadataMap(metadata);
                    String perms = encodeMetadataPermissions(containerType, containerId, mediaItem.getOwnerId(), metadata);
                    List<String> fields = Arrays.asList(perms);
                    String xmlData = createEntryXmlMetadataString(fields);
                    kalturaMetadata.xml = xmlData;
                    break;
                }
            }
        }
        return kalturaMetadata;
    }

    @Override
    protected Map<String, Map<String, Map<String, String>>> getMetadataForContainersEntries(Set<String> containerIds, String... entryIds) {
        if (containerIds == null || containerIds.isEmpty()) {
            throw new IllegalArgumentException("playlist ids must be set and not empty");
        }
        if (entryIds == null || entryIds.length == 0) {
            throw new IllegalArgumentException("entry ids must be set and not empty");
        }
        Map<String, Map<String, Map<String, String>>> metadata = new LinkedHashMap<String, Map<String, Map<String, String>>>(containerIds.size());
        for (String containerId : containerIds) {
            Map<String, Map<String, String>> entriesMetadata = getMetadataForEntry(containerId, entryIds);
            metadata.put(containerId, entriesMetadata);
        }
        return metadata;
    }

    @Override
    protected Map<String, Map<String, String>> getMetadataForEntry(String containerId, String... entryIds) {
        if (entryIds == null) {
            throw new IllegalArgumentException("entry id must be set");
        }
        if (StringUtils.isEmpty(containerId)) {
            throw new IllegalArgumentException("container id must be set");
        }
        Map<String, Map<String, String>> metadata = new LinkedHashMap<String, Map<String, String>>();
        List<MediaItem> itemsList = mockItems;
        if (!containerId.startsWith("ref-")) {
            // this is probably a collection so look through the collections
            for (MediaCollection mc : mockCollections) {
                if (StringUtils.equals(mc.getId(), containerId)) {
                    if (mc.getItems() != null) {
                        itemsList = mc.getItems();
                    }
                    break;
                }
            }
        }
        for (String entryId : entryIds) {
            Map<String, String> entryMetadata = null; //MediaItem.makeMetaDataMap(true, false, false, "UNKNOWN");
            for (MediaItem mi : itemsList) {
                if (StringUtils.equals(mi.getIdStr(), entryId)) {
                    entryMetadata = mi.extractMetaDataMap();
                }
            }
            metadata.put(entryId, entryMetadata);
        }
        return metadata;
    }

    @Override
    public List<KalturaMediaEntry> getKalturaItemsForUser(String username, String textFilter, int start, int max) {
        if (StringUtils.isEmpty(username)) {
            throw new IllegalArgumentException("username must be set");
        }
        List<KalturaMediaEntry> kalturaMediaEntries = new ArrayList<KalturaMediaEntry>();
        for (MediaItem mi : mockItems) {
            if (StringUtils.equals(mi.getOwnerId(), username)) {
                KalturaMediaEntry kme = makeSampleKME(mi);
                kalturaMediaEntries.add(kme);
            }
        }
        return kalturaMediaEntries;
    }

    @Override
    public List<KalturaMediaEntry> getKalturaItems(String textFilter, String[] keids, int start, int max) {
        if (textFilter != null) {
            throw new IllegalArgumentException("No support for textFilter param in MOCK");
        }
        if (keids == null) {
            throw new IllegalArgumentException("No support for null keids param in MOCK");
        }
        List<KalturaMediaEntry> kalturaMediaEntries = new ArrayList<KalturaMediaEntry>();
        if (keids.length > 0) {
            for (String keid : keids) {
                for (MediaItem mi : mockItems) {
                    if (StringUtils.equals(mi.getKalturaId(), keid)) {
                        KalturaMediaEntry kme = makeSampleKME(mi);
                        kalturaMediaEntries.add(kme);
                        break;
                    }
                }
            }
        }
        return kalturaMediaEntries;
    }

    @Override
    public KalturaMediaEntry getKalturaItem(String keid) {
        if (keid == null) {
            throw new IllegalArgumentException("keid must not be null");
        }
        KalturaMediaEntry kme = null;
        for (MediaItem mi : mockItems) {
            if (StringUtils.equals(mi.getKalturaId(), keid)) {
                kme = makeSampleKME(mi);
                break;
            }
        }
        return kme;
    }

    @Override
    public KalturaMediaEntry updateKalturaItem(KalturaMediaEntry kalturaEntry) {
        if (kalturaEntry == null) {
            throw new IllegalArgumentException("entry must not be null");
        }
        String keid = kalturaEntry.id;
        if (keid == null) {
            throw new IllegalArgumentException("entry keid must not be null");
        }
        KalturaMediaEntry kme = null;
        for (MediaItem mi : mockItems) {
            if (StringUtils.equals(mi.getKalturaId(), keid)) {
                kme = makeSampleKME(mi);
                kme.description = kalturaEntry.description;
                kme.name = kalturaEntry.name;
                kme.tags = kalturaEntry.tags;
                break;
            }
        }
        return kme;
    }

    public MediaItem getMediaItemById(String id) {
        if (StringUtils.isEmpty(id)) {
            throw new IllegalArgumentException("media id must not be blank");
        }
        MediaItem mediaItem = null;
        for (MediaItem mi : mockItems) {
            if (StringUtils.equals(mi.getIdStr(), id)) {
                mediaItem = mi;
            }
        }
        return mediaItem;
    }

}
