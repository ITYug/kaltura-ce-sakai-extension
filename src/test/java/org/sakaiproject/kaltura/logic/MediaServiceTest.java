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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import org.apache.commons.lang.ArrayUtils;
import org.sakaiproject.kaltura.logic.MediaService.Filter;
import org.sakaiproject.kaltura.logic.stubs.ExternalLogicStub;
import org.sakaiproject.kaltura.logic.stubs.KalturaAPIServiceStub;
import org.sakaiproject.kaltura.model.MediaCollection;
import org.sakaiproject.kaltura.model.MediaItem;


/**
 * Testing the service
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class MediaServiceTest extends TestCase {

    protected MediaService service;
    protected ExternalLogicStub external;
    private KalturaAPIServiceStub kalturaAPIService;

    @Override
    protected void setUp() throws Exception {
        // THIS IS EXECUTED BEFORE EACH TEST

        // make the caches
        CacheManager cacheManager = CacheManager.create();
        if (! cacheManager.cacheExists("ehcache.sakai.kaltura.entries")) {
            cacheManager.addCache("ehcache.sakai.kaltura.entries");
        }
        Ehcache entriesCache = cacheManager.getCache("ehcache.sakai.kaltura.entries");

        if (! cacheManager.cacheExists("ehcache.sakai.kaltura.cats")) {
            cacheManager.addCache("ehcache.sakai.kaltura.cats");
        }
        Ehcache categoriesCache = cacheManager.getCache("ehcache.sakai.kaltura.cats");

        // create and setup the object to be tested
        external = new ExternalLogicStub();

        kalturaAPIService = new KalturaAPIServiceStub(external, entriesCache, categoriesCache);
        kalturaAPIService.setKalturaClippingEnabled(true); // for testing

        service = new MediaService();
        service.setExternal(external);
        service.setKalturaAPIService(kalturaAPIService);
        service.setEntriesCache(entriesCache);
        service.setForceMediaNameOrdering(false);

        // run the init
        service.init();
    }

    public void testRetrieval() {
        external.currentUserId = KalturaAPIServiceStub.MAINT_USER_ID;

        MediaCollection mc = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(mc);
        assertEquals(kalturaAPIService.mcAd.getId(), mc.getId());
        assertNotNull(mc.getItems());
        assertEquals(5, mc.getItems().size());

        MediaItem mi = kalturaAPIService.getMediaItemById(kalturaAPIService.miAd_L1.getIdStr());
        assertNotNull(mi);
        assertEquals(kalturaAPIService.miAd_L1.getId(), mi.getId());

        List<MediaCollection> colls = service.getCollections(KalturaAPIServiceStub.LOCATION1_ID, null, 0, null, 0, 0);
        assertNotNull(colls);
        assertEquals(14, colls.size());

        colls = service.getCollections(KalturaAPIServiceStub.LOCATION1_ID, true, 0, null, 0, 0);
        assertNotNull(colls);
        assertEquals(2, colls.size());

        colls = service.getCollections(KalturaAPIServiceStub.LOCATION1_ID, false, 0, null, 0, 0);
        assertNotNull(colls);
        assertEquals(12, colls.size());

        // test perms
        external.currentUserId = KalturaAPIServiceStub.ACCESS_USER_ID;

        colls = service.getCollections(KalturaAPIServiceStub.LOCATION1_ID, null, 0, null, 0, 0);
        assertNotNull(colls);
        assertEquals(12, colls.size());

        try {
            service.getCollection(kalturaAPIService.mcShH.getIdStr(), 0, -1);
            fail("should have died");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        try {
            service.getCollection(kalturaAPIService.mcPrH.getIdStr(), 0, -1);
            fail("should have died");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }
    }

    public void testSharedFilter() {
        List<MediaItem> library;
        List<MediaItem> items;

        // SUPER ADMIN should get everything
        external.currentUserId = KalturaAPIServiceStub.ADMIN_USER_ID;

        library = service.getLibrary(KalturaAPIServiceStub.LOCATION1_ID, Filter.SHARED, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(8, library.size());
        items = library;
        assertTrue(items.contains(kalturaAPIService.miL1));
        assertTrue(items.contains(kalturaAPIService.miL2));
        assertTrue(items.contains(kalturaAPIService.miL3));
        assertTrue(items.contains(kalturaAPIService.miL4));
        assertTrue(items.contains(kalturaAPIService.miL5));
        assertTrue(items.contains(kalturaAPIService.miL6));
        assertTrue(items.contains(kalturaAPIService.miL7));
        assertTrue(items.contains(kalturaAPIService.miL8));

        // TEACHER
        external.currentUserId = KalturaAPIServiceStub.MAINT_USER_ID;

        library = service.getLibrary(KalturaAPIServiceStub.LOCATION1_ID, Filter.SHARED, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(8, library.size());
        items = library;
        assertNotNull(items);
        assertTrue(items.contains(kalturaAPIService.miL1)); // hidden
        assertTrue(items.contains(kalturaAPIService.miL2));
        assertTrue(items.contains(kalturaAPIService.miL3)); // SUPER
        assertTrue(items.contains(kalturaAPIService.miL4));
        assertTrue(items.contains(kalturaAPIService.miL5)); // hidden
        assertTrue(items.contains(kalturaAPIService.miL6)); // AW
        assertTrue(items.contains(kalturaAPIService.miL7)); // AW, hidden
        assertTrue(items.contains(kalturaAPIService.miL8)); // AE, hidden

        // STUDENT
        external.currentUserId = KalturaAPIServiceStub.ACCESS_USER_ID;

        library = service.getLibrary(KalturaAPIServiceStub.LOCATION1_ID, Filter.SHARED, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(3, library.size());
        items = library;
        assertNotNull(items);
        assertFalse(items.contains(kalturaAPIService.miL1)); // hidden
        assertTrue(items.contains(kalturaAPIService.miL2));
        assertTrue(items.contains(kalturaAPIService.miL3));
        assertTrue(items.contains(kalturaAPIService.miL4));
        assertFalse(items.contains(kalturaAPIService.miL5)); // hidden
        assertFalse(items.contains(kalturaAPIService.miL6)); // NOT SHARED
        assertFalse(items.contains(kalturaAPIService.miL7)); // hidden
    }

    public void testWrites() {
        boolean result;
        MediaCollection coll;
        List<MediaItem> items;

        external.currentUserId = KalturaAPIServiceStub.MAINT_USER_ID;

        MediaCollection myMC = service.addCollection("my collection", null);
        assertNotNull(myMC);
        assertEquals(0, myMC.getItems().size());
        assertEquals(true, myMC.isHidden());

        String myMI1 = service.addKalturaItemToCollection(myMC.getIdStr(), myMC.getLocationId(), kalturaAPIService.miL1.getIdStr(), -1).getId();
        assertNotNull(myMI1);
        String myMI2 = service.addKalturaItemToCollection(myMC.getIdStr(), myMC.getLocationId(), kalturaAPIService.miL2.getIdStr(), -1).getId(); 
        assertNotNull(myMI2);
        String myMI3 = service.addKalturaItemToCollection(myMC.getIdStr(), myMC.getLocationId(), kalturaAPIService.miL3.getIdStr(), 1).getId();
        assertNotNull(myMI3);
        try {
            service.addKalturaItemToCollection(myMC.getIdStr(), myMC.getLocationId(), "111111", -1);
            fail("should have died");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        myMC.setHidden(false);
        myMC = service.updateCollection(myMC);

        coll = service.getCollection(myMC.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertEquals(coll.getId(), myMC.getId());
        assertEquals(false, myMC.isHidden());
        assertEquals(3, coll.getItems().size());
        assertEquals(3, coll.getKalturaIds().size());
        items = coll.getItems();
        assertNotNull(items);
        // NOTE: not checking order, because we did not mock the order handling
        // assertEquals(items.get(0), myMI3);
        // assertEquals(items.get(1), myMI1);
        // assertEquals(items.get(2), myMI2);

        // WARNING: no tests for collection reordering

        result = service.removeKalturaItemFromCollection(coll.getId(), myMI3);
        assertTrue(result);

        coll = service.getCollection(myMC.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertEquals(2, coll.getItems().size());
        items = coll.getItems();
        assertNotNull(items);
        // NOTE: not checking order, because we did not mock the order handling
        // assertEquals(items.get(0), myMI1);
        // assertEquals(items.get(1), myMI2);

        result = service.removeCollection(myMC.getIdStr());
        assertTrue(result);

        coll = service.getCollection(myMC.getIdStr(), 0, -1);
        assertNull(coll);
    }

    public void testBasePermissions() {
        MediaCollection coll;
        List<MediaItem> library;
        List<MediaItem> items;

        // SUPER ADMIN
        external.currentUserId = KalturaAPIServiceStub.ADMIN_USER_ID;

        library = service.getLibrary(KalturaAPIServiceStub.LOCATION1_ID, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(8, library.size());
        items = library;
        assertNotNull(items);
        assertTrue(items.contains(kalturaAPIService.miL1));
        assertTrue(items.contains(kalturaAPIService.miL2));
        assertTrue(items.contains(kalturaAPIService.miL3));
        assertTrue(items.contains(kalturaAPIService.miL4));
        assertTrue(items.contains(kalturaAPIService.miL5));
        assertTrue(items.contains(kalturaAPIService.miL6));
        assertTrue(items.contains(kalturaAPIService.miL7));
        assertTrue(items.contains(kalturaAPIService.miL8));

        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertEquals(5, coll.getItems().size());
        items = coll.getItems();
        assertNotNull(items);
        assertTrue(items.contains(kalturaAPIService.miAd_L1));
        assertTrue(items.contains(kalturaAPIService.miAd_L2));
        assertTrue(items.contains(kalturaAPIService.miAd_L3));

        // TEACHER
        external.currentUserId = KalturaAPIServiceStub.MAINT_USER_ID;

        library = service.getLibrary(KalturaAPIServiceStub.LOCATION1_ID, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(8, library.size());
        items = library;
        assertNotNull(items);
        assertTrue(items.contains(kalturaAPIService.miL1)); // hidden
        assertTrue(items.contains(kalturaAPIService.miL2));
        assertTrue(items.contains(kalturaAPIService.miL3)); // SUPER
        assertTrue(items.contains(kalturaAPIService.miL4));
        assertTrue(items.contains(kalturaAPIService.miL5)); // hidden
        assertTrue(items.contains(kalturaAPIService.miL6)); // AW
        assertTrue(items.contains(kalturaAPIService.miL7)); // AW, hidden
        assertTrue(items.contains(kalturaAPIService.miL8)); // AE, hidden

        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertEquals(5, coll.getItems().size());
        items = coll.getItems();
        assertNotNull(items);
        assertTrue(items.contains(kalturaAPIService.miAd_L1));
        assertTrue(items.contains(kalturaAPIService.miAd_L2));
        assertTrue(items.contains(kalturaAPIService.miAd_L3));

        /* OK to be commented out -AZ
        assertTrue(items.contains(kaltura.mi11));
        assertTrue(items.contains(kaltura.mi12));
        assertTrue(items.contains(kaltura.mi13));
        assertTrue(items.contains(kaltura.mi21));
        assertTrue(items.contains(kaltura.mi31));
        assertTrue(items.contains(kaltura.mi32));
        assertTrue(items.contains(kaltura.mi35));
        assertTrue(items.contains(kaltura.mi41));
        assertTrue(items.contains(kaltura.mi42));
        assertTrue(items.contains(kaltura.mi45));
        assertTrue(items.contains(kaltura.mi51));
        assertTrue(items.contains(kaltura.mi52));
        assertTrue(items.contains(kaltura.mi55));
         */

        // STUDENT
        external.currentUserId = KalturaAPIServiceStub.ACCESS_USER_ID;

        library = service.getLibrary(KalturaAPIServiceStub.LOCATION1_ID, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(4, library.size());
        items = library;
        assertNotNull(items);
        assertFalse(items.contains(kalturaAPIService.miL1)); // hidden
        assertTrue(items.contains(kalturaAPIService.miL2));
        assertTrue(items.contains(kalturaAPIService.miL3));
        assertTrue(items.contains(kalturaAPIService.miL4));
        assertFalse(items.contains(kalturaAPIService.miL5)); // hidden
        assertTrue(items.contains(kalturaAPIService.miL6));
        assertFalse(items.contains(kalturaAPIService.miL7)); // hidden

        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertEquals(3, coll.getItems().size());
        items = coll.getItems();
        assertNotNull(items);
        assertFalse(items.contains(kalturaAPIService.miAd_L1)); // hidden
        assertTrue(items.contains(kalturaAPIService.miAd_L2));
        assertTrue(items.contains(kalturaAPIService.miAd_L3));

        // STUDENT
        external.currentUserId = KalturaAPIServiceStub.ACCESS_USER_ID_W;

        library = service.getLibrary(KalturaAPIServiceStub.LOCATION1_ID, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(2, library.size());
        items = library;
        assertNotNull(items);
        assertFalse(items.contains(kalturaAPIService.miL1)); // hidden
        assertFalse(items.contains(kalturaAPIService.miL2));
        assertFalse(items.contains(kalturaAPIService.miL3));
        assertFalse(items.contains(kalturaAPIService.miL4));
        assertFalse(items.contains(kalturaAPIService.miL5)); // hidden
        assertTrue(items.contains(kalturaAPIService.miL6));
        assertTrue(items.contains(kalturaAPIService.miL7));

        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertEquals(1, coll.getItems().size());
        items = coll.getItems();
        assertNotNull(items);

        // test creator is able to clip
        MediaItem item = kalturaAPIService.miL7;
        external.currentUserId = KalturaAPIServiceStub.ACCESS_USER_NAME_W;
        item.indicateUserControl(external.currentUserId, true, true, false);
        String currentCreator = item.getCreatorUserId();
        
        // user is the creator
        assertNotNull(item);
        assertFalse(item.isRemixable());
        assertTrue(item.isCanRemix());
        
        // user is not the creator
        item.setCreatorId(KalturaAPIServiceStub.ACCESS_USER_NAME_E);
        assertNotNull(item);
        assertFalse(item.isRemixable());
        assertFalse(item.isCanRemix());
        
        // reset creator
        item.setCreatorId(currentCreator);

    }

    public void testOverallPermsSuperAdmin() {
        List<MediaCollection> colls;
        List<MediaItem> library;
        List<MediaItem> items;
        MediaItem item;
        String itemId;
        MediaCollection coll;
        String locationId = KalturaAPIServiceStub.LOCATION1_ID;
        String kalturaId = "kidA";
        String coll_title = "";
        Date d = null;

        external.currentUserId = KalturaAPIServiceStub.ADMIN_USER_ID; // SUPER ADMIN
        //external.currentUserId = FakeDataPreload.MAINT_USER_ID; // FULL ADMIN
        //external.currentUserId = FakeDataPreload.MAINT_USER_ID_A; // ADMIN ONLY
        //external.currentUserId = FakeDataPreload.MAINT_USER_ID_M; // MANAGER ONLY
        //external.currentUserId = FakeDataPreload.ACCESS_USER_ID_W; // WRITE
        //external.currentUserId = FakeDataPreload.ACCESS_USER_ID; // READ
        //external.currentUserId = FakeDataPreload.USER_ID; // NOTHING

        // view collections / hidden
        colls = service.getCollections(locationId, null, -1, null, 0, 0);
        assertNotNull(colls);
        assertEquals(14, colls.size()); // 2 hidden of 14

        // collection viewing
        Map<String, MediaCollection> mcs = new HashMap<String, MediaCollection>();
        for (MediaCollection mc : colls) {
            mcs.put(mc.getIdStr(), mc);
        }
        assertTrue(mcs.get(kalturaAPIService.mcPr.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcAd.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcSh.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPu.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPrH.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcShH.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPrE.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcAdE.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcShE.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPuE.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPrNoPub.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcAdNoPub.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcShNoPub.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPuNoPub.getIdStr()).isViewable());

        // edit coll
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, 0);
        assertNotNull(coll);
        assertNull(coll.getItems());
        coll_title = "test"+System.currentTimeMillis();
        assertNotSame(coll_title, coll.getTitle());
        coll.setTitle(coll_title);
        service.updateCollection(coll);
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, 0);
        assertEquals(coll_title, coll.getTitle());

        // view coll items
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertEquals(5, coll.getItems().size());
        items = coll.getItems();
        assertNotNull(items);
        assertTrue(items.contains(kalturaAPIService.miAd_L1)); // hidden
        assertTrue(items.contains(kalturaAPIService.miAd_L2));
        assertTrue(items.contains(kalturaAPIService.miAd_L3));

        // add / remove to collection
        item = service.addKalturaItemToCollection(coll.getIdStr(), coll.getLocationId(), kalturaAPIService.miL7.getIdStr(), 0);
        itemId = item.getId();
        item = service.getCollectionMediaItem(itemId, coll.getIdStr(), true);
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertEquals(6, coll.getItems().size());
        items = coll.getItems();
        assertTrue(items.contains(item));
        service.removeKalturaItemFromCollection(coll.getIdStr(), itemId);
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertEquals(5, coll.getItems().size());
        items = coll.getItems();
        assertFalse(items.contains(item));

        // view / hidden
        library = service.getLibrary(locationId, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(8, library.size());
        items = library;
        assertNotNull(items);
        assertTrue(items.contains(kalturaAPIService.miL1)); // hidden
        assertTrue(items.contains(kalturaAPIService.miL2));
        assertTrue(items.contains(kalturaAPIService.miL3)); // SUPER
        assertTrue(items.contains(kalturaAPIService.miL4));
        assertTrue(items.contains(kalturaAPIService.miL5)); // hidden
        assertTrue(items.contains(kalturaAPIService.miL6)); // AW
        assertTrue(items.contains(kalturaAPIService.miL7)); // AW, hidden
        assertTrue(items.contains(kalturaAPIService.miL8)); // AE, hidden

        // edit item
        item = kalturaAPIService.getMediaItemById(kalturaAPIService.miL3.getIdStr());
        assertNotNull(item);
        item.setDateModified(new Date(System.currentTimeMillis() - 60000));
        d = item.getDateModified();
        MediaItem mi = service.updateMediaItem(item);
        assertNotSame(d, mi.getDateModified());

        // edit collection item
        item = kalturaAPIService.getMediaItemById(kalturaAPIService.miAd_L1.getIdStr());
        assertNotNull(item);
        item.setDateModified(new Date(System.currentTimeMillis() - 60000));
        d = item.getDateModified();
        MediaItem mci = service.updateMediaItem(item);
        assertNotSame(d, mci.getDateModified());

        // edit item perms
        item = kalturaAPIService.getMediaItemById(kalturaAPIService.miL3.getIdStr());
        assertNotNull(item);
        d = item.getDateModified();
        assertFalse(item.isHidden());
        assertTrue(item.isShared());
        item.setHidden(true);
        item.setShared(false);
        MediaItem mip = service.updateMediaItem(item);
        assertTrue(mip.isHidden());
        assertFalse(mip.isShared());
        assertNotSame(d, mip.getDateModified());

        // upload
        itemId = service.addKalturaItemToLibrary(locationId, kalturaId);
        assertNotNull(itemId);
        MediaItem newItem = kalturaAPIService.getMediaItemById(itemId);
        assertNotNull(newItem);
        library = service.getLibrary(locationId, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(9, library.size());
        assertTrue(library.contains(newItem));

        // test remixing/clipping
        String currentCreator = kalturaAPIService.miL3.getCreatorUserId();
        kalturaAPIService.miL3.setCreatorId(external.getCurrentUserName());
        String myKalturaId = kalturaAPIService.miL3.getId();
        kalturaId = kalturaAPIService.miL2.getId();

        item = service.getMyMediaByKalturaId(myKalturaId, external.currentUserId);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        item = service.getLibraryMediaItem(kalturaId, locationId, true);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        // using miPu_L2 as the origin item
        item = service.getCollectionMediaItem(kalturaId, kalturaAPIService.mcPu.getId(), true);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        // force the remix flag to off
        kalturaAPIService.miL2.setRemixable(false);
        kalturaAPIService.miPu_L2.setRemixable(false);

        item = service.getMyMediaByKalturaId(myKalturaId, external.currentUserId);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        item = service.getLibraryMediaItem(kalturaId, locationId, true);
        assertNotNull(item);
        assertFalse(item.isRemixable());
        assertFalse(item.isCanRemix());

        item = service.getCollectionMediaItem(kalturaId, kalturaAPIService.mcPu.getId(), true);
        assertNotNull(item);
        assertFalse(item.isRemixable());
        assertFalse(item.isCanRemix());

        // reset it back to what it was
        kalturaAPIService.miL2.setRemixable(true);
        kalturaAPIService.miPu_L2.setRemixable(true);
        kalturaAPIService.miL3.setCreatorId(currentCreator);

    }

    public void testOverallPermsFullAdmin() {
        List<MediaCollection> colls;
        List<MediaItem> library;
        List<MediaItem> items;
        MediaItem item;
        String itemId;
        MediaCollection coll;
        String locationId = KalturaAPIServiceStub.LOCATION1_ID;
        String kalturaId = "kidA";
        String coll_title = "";
        Date d = null;

        external.currentUserId = KalturaAPIServiceStub.MAINT_USER_ID; // FULL ADMIN

        // view collections / hidden
        colls = service.getCollections(locationId, null, -1, null, 0, 0);
        assertNotNull(colls);
        assertEquals(14, colls.size()); // 2 hidden of 14

        // collection viewing
        Map<String, MediaCollection> mcs = new HashMap<String, MediaCollection>();
        for (MediaCollection mc : colls) {
            mcs.put(mc.getIdStr(), mc);
        }
        assertTrue(mcs.get(kalturaAPIService.mcPr.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcAd.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcSh.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPu.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPrH.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcShH.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPrE.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcAdE.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcShE.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPuE.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPrNoPub.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcAdNoPub.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcShNoPub.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPuNoPub.getIdStr()).isViewable());

        // edit coll
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, 0);
        assertNotNull(coll);
        assertNull(coll.getItems());
        coll_title = "test"+System.currentTimeMillis();
        assertNotSame(coll_title, coll.getTitle());
        coll.setTitle(coll_title);
        service.updateCollection(coll);
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, 0);
        assertEquals(coll_title, coll.getTitle());

        // view coll items
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertEquals(5, coll.getItems().size());
        items = coll.getItems();
        assertNotNull(items);
        assertTrue(items.contains(kalturaAPIService.miAd_L1)); // hidden
        assertTrue(items.contains(kalturaAPIService.miAd_L2));
        assertTrue(items.contains(kalturaAPIService.miAd_L3));

        // add / remove to collection
        kalturaAPIService.miNO_A.setHidden(true); // set item to hidden
        item = service.addKalturaItemToCollection(coll.getIdStr(), coll.getLocationId(), kalturaAPIService.miNO_A.getIdStr(), 0);
        itemId = item.getId();
        item = kalturaAPIService.getMediaItemById(itemId);
        assertFalse(item.isHidden());
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertEquals(6, coll.getItems().size());
        items = coll.getItems();
        assertTrue(items.contains(item));
        service.removeKalturaItemFromCollection(coll.getIdStr(), item.getIdStr());
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertEquals(5, coll.getItems().size());
        items = coll.getItems();
        assertFalse(items.contains(item));
        kalturaAPIService.miNO_A.setHidden(false); // reset item to public

        // view / hidden
        library = service.getLibrary(locationId, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(9, library.size());
        items = library;
        assertNotNull(items);
        assertTrue(items.contains(kalturaAPIService.miL1)); // hidden
        assertTrue(items.contains(kalturaAPIService.miL2));
        assertTrue(items.contains(kalturaAPIService.miL3)); // SUPER
        assertTrue(items.contains(kalturaAPIService.miL4));
        assertTrue(items.contains(kalturaAPIService.miL5)); // hidden
        assertTrue(items.contains(kalturaAPIService.miL6)); // AW
        assertTrue(items.contains(kalturaAPIService.miL7)); // AW, hidden
        assertTrue(items.contains(kalturaAPIService.miL8)); // AE, hidden

        // edit item
        item = kalturaAPIService.getMediaItemById(kalturaAPIService.miL3.getIdStr());
        assertNotNull(item);
        d = item.getDateModified();
        MediaItem mi = service.updateMediaItem(item);
        assertNotSame(d, mi.getDateModified());

        // edit collection item
        item = kalturaAPIService.getMediaItemById(kalturaAPIService.miAd_L1.getIdStr());
        assertNotNull(item);
        d = item.getDateModified();
        mi = service.updateMediaItem(item);
        assertNotSame(d, mi.getDateModified());

        // edit item perms
        item = kalturaAPIService.getMediaItemById(kalturaAPIService.miL3.getIdStr());
        assertNotNull(item);
        d = item.getDateModified();
        assertFalse(item.isHidden());
        assertTrue(item.isShared());
        item.setHidden(true);
        item.setShared(false);
        mi = service.updateMediaItem(item);
        assertTrue(mi.isHidden());
        assertFalse(mi.isShared());
        assertNotSame(d, mi.getDateModified());

        // upload
        kalturaAPIService.miNO_A.setHidden(true); // set item to hidden
        itemId = service.addKalturaItemToLibrary(locationId, kalturaId);
        assertNotNull(itemId);
        item = kalturaAPIService.getMediaItemById(itemId);
        assertFalse(item.isHidden());
        library = service.getLibrary(locationId, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(9, library.size());
        assertTrue(library.contains(item));
        kalturaAPIService.miNO_A.setHidden(false); // reset item to public

        // remove
        service.removeKalturaItemFromLibrary(locationId, item.getIdStr());
        library = service.getLibrary(locationId, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(8, library.size());
        assertFalse(library.contains(item));

        // test remixing/clipping
        String currentCreator = kalturaAPIService.miL3.getCreatorUserId();
        kalturaAPIService.miL3.setCreatorId(external.getCurrentUserName());
        String myKalturaId = kalturaAPIService.miL3.getId();
        kalturaId = kalturaAPIService.miL2.getId();

        item = service.getMyMediaByKalturaId(myKalturaId, external.currentUserId);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        item = service.getLibraryMediaItem(kalturaId, locationId, true);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        // using miPu_L2 as the origin item
        item = service.getCollectionMediaItem(kalturaId, kalturaAPIService.mcPu.getId(), true);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        // force the remix flag to off
        kalturaAPIService.miL2.setRemixable(false);
        kalturaAPIService.miPu_L2.setRemixable(false);

        item = service.getMyMediaByKalturaId(myKalturaId, external.currentUserId);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        item = service.getLibraryMediaItem(kalturaId, locationId, true);
        assertNotNull(item);
        assertFalse(item.isRemixable());
        assertTrue(item.isCanRemix());

        item = service.getCollectionMediaItem(kalturaId, kalturaAPIService.mcPu.getId(), true);
        assertNotNull(item);
        assertFalse(item.isRemixable());
        assertTrue(item.isCanRemix());

        // reset it back to what it was
        kalturaAPIService.miL2.setRemixable(true);
        kalturaAPIService.miPu_L2.setRemixable(true);
        kalturaAPIService.miL3.setCreatorId(currentCreator);

    }

    public void testOverallPermsAdmin() {
        List<MediaCollection> colls;
        List<MediaItem> library;
        List<MediaItem> items;
        MediaItem item;
        String itemId;
        MediaCollection coll;
        String locationId = KalturaAPIServiceStub.LOCATION1_ID;
        String kalturaId = "kidA";
        String coll_title = "";
        Date d = null;

        external.currentUserId = KalturaAPIServiceStub.MAINT_USER_ID_A; // ADMIN ONLY

        // view collections / hidden
        colls = service.getCollections(locationId, null, -1, null, 0, 0);
        assertNotNull(colls);
        assertEquals(13, colls.size()); // 2 hidden of 14

        // collection viewing
        Map<String, MediaCollection> mcs = new HashMap<String, MediaCollection>();
        for (MediaCollection mc : colls) {
            mcs.put(mc.getIdStr(), mc);
        }
        assertTrue(mcs.get(kalturaAPIService.mcPr.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcAd.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcSh.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPu.getIdStr()).isViewable());
        //assertTrue(mcs.get(kalturaAPIService.mcPrH.getIdStr()).isViewable());
        //assertTrue(mcs.get(kalturaAPIService.mcShH.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcShH.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPrE.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcAdE.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcShE.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPuE.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPrNoPub.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcAdNoPub.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcShNoPub.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPuNoPub.getIdStr()).isViewable());

        // edit coll
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, 0);
        assertNotNull(coll);
        assertNull(coll.getItems());
        coll_title = "test"+System.currentTimeMillis();
        assertNotSame(coll_title, coll.getTitle());
        coll.setTitle(coll_title);
        service.updateCollection(coll);
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, 0);
        assertEquals(coll_title, coll.getTitle());

        // view coll items
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertEquals(5, coll.getItems().size());
        items = coll.getItems();
        assertNotNull(items);
        assertTrue(items.contains(kalturaAPIService.miAd_L1)); // hidden
        assertTrue(items.contains(kalturaAPIService.miAd_L2));
        assertTrue(items.contains(kalturaAPIService.miAd_L3));

        // add / remove to collection
        item = service.addKalturaItemToCollection(coll.getIdStr(), coll.getLocationId(), kalturaAPIService.miNO_A.getIdStr(), 0);
        itemId = item.getId();
        item = kalturaAPIService.getMediaItemById(itemId);
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertEquals(6, coll.getItems().size());
        items = coll.getItems();
        assertTrue(items.contains(item));
        service.removeKalturaItemFromCollection(coll.getIdStr(), item.getIdStr());
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertEquals(5, coll.getItems().size());
        items = coll.getItems();
        assertFalse(items.contains(item));

        // view / hidden
        library = service.getLibrary(locationId, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(4, library.size());
        items = library;
        assertNotNull(items);
        assertFalse(items.contains(kalturaAPIService.miL1)); // hidden
        assertTrue(items.contains(kalturaAPIService.miL2));
        assertTrue(items.contains(kalturaAPIService.miL3)); // SUPER
        assertTrue(items.contains(kalturaAPIService.miL4));
        assertFalse(items.contains(kalturaAPIService.miL5)); // hidden
        assertTrue(items.contains(kalturaAPIService.miL6)); // AW
        assertFalse(items.contains(kalturaAPIService.miL7)); // AW, hidden
        assertFalse(items.contains(kalturaAPIService.miL8)); // AE, hidden

        // edit item
        item = kalturaAPIService.getMediaItemById(kalturaAPIService.miL3.getIdStr());
        assertNotNull(item);
        d = item.getDateModified();
        try {
            item = service.updateMediaItem(item);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }
        item = kalturaAPIService.getMediaItemById(kalturaAPIService.miL3.getIdStr());
        assertSame(d, item.getDateModified());

        // edit collection item
        item = kalturaAPIService.getMediaItemById(kalturaAPIService.miAd_L1.getIdStr());
        assertNotNull(item);
        item.setDateModified(new Date(System.currentTimeMillis() - 60000));
        d = item.getDateModified();
        try {
            item = service.updateMediaItem(item);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }
        assertSame(d, item.getDateModified());

        // edit item perms
        item = kalturaAPIService.getMediaItemById(kalturaAPIService.miL3.getIdStr());
        assertNotNull(item);
        d = item.getDateModified();
        assertFalse(item.isHidden());
        assertTrue(item.isShared());
        item.setHidden(true);
        item.setShared(false);
        try {
            item = service.updateMediaItem(item);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }
        assertSame(d, item.getDateModified());

        // upload
        try {
            service.addKalturaItemToLibrary(locationId, kalturaId);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }
        library = service.getLibrary(locationId, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(3, library.size());

        // cannot remove item since they could not add the item
        //service.removeKalturaItemFromLibrary(locationId, item.getIdStr());

        try {
            service.removeKalturaItemFromLibrary(locationId, kalturaAPIService.miL1.getIdStr());
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        // test remixing/clipping
        String currentCreator = kalturaAPIService.miL3.getCreatorUserId();
        kalturaAPIService.miL3.setCreatorId(external.getCurrentUserName());
        String myKalturaId = kalturaAPIService.miL3.getId();
        kalturaId = kalturaAPIService.miL2.getId();

        item = service.getMyMediaByKalturaId(myKalturaId, external.currentUserId);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        item = service.getLibraryMediaItem(kalturaId, locationId, true);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertFalse(item.isCanRemix());

        // using miPu_L2 as the origin item
        item = service.getCollectionMediaItem(kalturaId, kalturaAPIService.mcPu.getId(), true);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        // force the remix flag to off
        kalturaAPIService.miL2.setRemixable(false);
        kalturaAPIService.miPu_L2.setRemixable(false);

        item = service.getMyMediaByKalturaId(myKalturaId, external.currentUserId);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        item = service.getLibraryMediaItem(kalturaId, locationId, true);
        assertNotNull(item);
        assertFalse(item.isRemixable());
        assertFalse(item.isCanRemix());

        item = service.getCollectionMediaItem(kalturaId, kalturaAPIService.mcPu.getId(), true);
        assertNotNull(item);
        assertFalse(item.isRemixable());
        assertFalse(item.isCanRemix());

        // reset it back to what it was
        kalturaAPIService.miL2.setRemixable(true);
        kalturaAPIService.miPu_L2.setRemixable(true);
        kalturaAPIService.miL3.setCreatorId(currentCreator);

    }

    public void testOverallPermsManager() {
        List<MediaCollection> colls;
        List<MediaItem> library;
        List<MediaItem> items;
        MediaItem item;
        String itemId;
        MediaCollection coll;
        String locationId = KalturaAPIServiceStub.LOCATION1_ID;
        String kalturaId = "kidA";
        String coll_title = "";
        Date d = null;

        external.currentUserId = KalturaAPIServiceStub.MAINT_USER_ID_M; // MANAGER ONLY

        // view collections / hidden
        colls = service.getCollections(locationId, null, -1, null, 0, 0);
        assertNotNull(colls);
        assertEquals(12, colls.size()); // 2 hidden of 14

        // collection viewing
        Map<String, MediaCollection> mcs = new HashMap<String, MediaCollection>();
        for (MediaCollection mc : colls) {
            mcs.put(mc.getIdStr(), mc);
        }
        assertTrue(mcs.get(kalturaAPIService.mcPr.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcAd.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcSh.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPu.getIdStr()).isViewable());
        //assertTrue(mcs.get(kalturaAPIService.mcPrH.getIdStr()).isViewable());
        //assertTrue(mcs.get(kalturaAPIService.mcShH.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPrE.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcAdE.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcShE.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPuE.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPrNoPub.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcAdNoPub.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcShNoPub.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPuNoPub.getIdStr()).isViewable());

        // edit coll
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, 0);
        assertNotNull(coll);
        assertNull(coll.getItems());
        coll_title = "test"+System.currentTimeMillis();
        assertNotSame(coll_title, coll.getTitle());
        coll.setTitle(coll_title);
        try {
            service.updateCollection(coll);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        // view coll items
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertEquals(5, coll.getItems().size());
        items = coll.getItems();
        assertNotNull(items);
        assertTrue(items.contains(kalturaAPIService.miAd_L1)); // hidden
        assertTrue(items.contains(kalturaAPIService.miAd_L2));
        assertTrue(items.contains(kalturaAPIService.miAd_L3));

        // add / remove to collection
        item = service.addKalturaItemToCollection(coll.getIdStr(), coll.getLocationId(), kalturaAPIService.miNO_A.getIdStr(), 0);
        itemId = item.getId();
        item = kalturaAPIService.getMediaItemById(itemId);
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertEquals(6, coll.getItems().size());
        items = coll.getItems();
        assertTrue(items.contains(item));
        service.removeKalturaItemFromCollection(coll.getIdStr(), item.getIdStr());
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertEquals(5, coll.getItems().size());
        items = coll.getItems();
        assertFalse(items.contains(item));

        // view / hidden
        library = service.getLibrary(locationId, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(8, library.size());
        items = library;
        assertNotNull(items);
        assertTrue(items.contains(kalturaAPIService.miL1)); // hidden
        assertTrue(items.contains(kalturaAPIService.miL2));
        assertTrue(items.contains(kalturaAPIService.miL3)); // SUPER
        assertTrue(items.contains(kalturaAPIService.miL4));
        assertTrue(items.contains(kalturaAPIService.miL5)); // hidden
        assertTrue(items.contains(kalturaAPIService.miL6)); // AW
        assertTrue(items.contains(kalturaAPIService.miL7)); // AW, hidden
        assertTrue(items.contains(kalturaAPIService.miL8)); // AE, hidden

        // edit item
        item = kalturaAPIService.getMediaItemById(kalturaAPIService.miL3.getIdStr());
        assertNotNull(item);
        d = item.getDateModified();
        item = service.updateMediaItem(item);
        assertNotSame(d, item.getDateModified());

        // edit collection item
        item = kalturaAPIService.getMediaItemById(kalturaAPIService.miAd_L1.getIdStr());
        assertNotNull(item);
        d = item.getDateModified();
        item = service.updateMediaItem(item);
        assertNotSame(d, item.getDateModified());

        // edit item perms
        item = kalturaAPIService.getMediaItemById(kalturaAPIService.miL3.getIdStr());
        assertNotNull(item);
        item.setDateModified(new Date(System.currentTimeMillis() - 60000));
        d = item.getDateModified();
        assertFalse(item.isHidden());
        assertTrue(item.isShared());
        item.setHidden(false);
        item.setShared(true);
        item = service.updateMediaItem(item);
        assertFalse(item.isHidden());
        assertTrue(item.isShared());
        assertNotSame(d, item.getDateModified());

        // upload
        try {
            service.addKalturaItemToLibrary(locationId, kalturaId);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        library = service.getLibrary(locationId, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(8, library.size());

        // remove
        service.removeKalturaItemFromLibrary(locationId, item.getIdStr());
        library = service.getLibrary(locationId, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(7, library.size());

        service.removeKalturaItemFromLibrary(locationId, kalturaAPIService.miL1.getIdStr());

        // test remixing/clipping
        String currentCreator = kalturaAPIService.miL3.getCreatorUserId();
        kalturaAPIService.miL3.setCreatorId(external.getCurrentUserName());
        String myKalturaId = kalturaAPIService.miL3.getId();
        kalturaId = kalturaAPIService.miL2.getId();

        item = service.getMyMediaByKalturaId(myKalturaId, external.currentUserId);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        item = service.getLibraryMediaItem(kalturaId, locationId, true);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        // using miPu_L2 as the origin item
        item = service.getCollectionMediaItem(kalturaId, kalturaAPIService.mcPu.getId(), true);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        // force the remix flag to off
        kalturaAPIService.miL2.setRemixable(false);
        kalturaAPIService.miPu_L2.setRemixable(false);

        item = service.getMyMediaByKalturaId(myKalturaId, external.currentUserId);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        item = service.getLibraryMediaItem(kalturaId, locationId, true);
        assertNotNull(item);
        assertFalse(item.isRemixable());
        assertFalse(item.isCanRemix());

        item = service.getCollectionMediaItem(kalturaId, kalturaAPIService.mcPu.getId(), true);
        assertNotNull(item);
        assertFalse(item.isRemixable());
        assertFalse(item.isCanRemix());

        // reset it back to what it was
        kalturaAPIService.miL2.setRemixable(true);
        kalturaAPIService.miPu_L2.setRemixable(true);
        kalturaAPIService.miL3.setCreatorId(currentCreator);

    }

    public void testOverallPermsWrite() {
        List<MediaCollection> colls;
        List<MediaItem> library;
        List<MediaItem> items;
        MediaItem item;
        String itemId;
        MediaCollection coll;
        String locationId = KalturaAPIServiceStub.LOCATION1_ID;
        String coll_title = "";
        String collId = "";
        Date d = null;
        assertNull(d);

        external.currentUserId = KalturaAPIServiceStub.ACCESS_USER_ID_W; // WRITE

        // view collections / hidden
        colls = service.getCollections(locationId, null, -1, null, 0, 0);
        assertNotNull(colls);
        assertEquals(12, colls.size()); // 2 hidden of 14

        // collection viewing
        Map<String, MediaCollection> mcs = new HashMap<String, MediaCollection>();
        for (MediaCollection mc : colls) {
            mcs.put(mc.getIdStr(), mc);
        }
        assertFalse(mcs.get(kalturaAPIService.mcPr.getIdStr()).isViewable());
        assertFalse(mcs.get(kalturaAPIService.mcAd.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcSh.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPu.getIdStr()).isViewable());
        //assertTrue(mcs.get(kalturaAPIService.mcPrH.getIdStr()).isViewable());
        //assertTrue(mcs.get(kalturaAPIService.mcShH.getIdStr()).isViewable());
        assertFalse(mcs.get(kalturaAPIService.mcPrE.getIdStr()).isViewable());
        assertFalse(mcs.get(kalturaAPIService.mcAdE.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcShE.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPuE.getIdStr()).isViewable());
        assertFalse(mcs.get(kalturaAPIService.mcPrNoPub.getIdStr()).isViewable());
        assertFalse(mcs.get(kalturaAPIService.mcAdNoPub.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcShNoPub.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPuNoPub.getIdStr()).isViewable());

        // edit coll
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, 0);
        assertNotNull(coll);
        assertNull(coll.getItems());
        coll_title = "test"+System.currentTimeMillis();
        assertNotSame(coll_title, coll.getTitle());
        coll.setTitle(coll_title);
        try {
            service.updateCollection(coll);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        // view coll items
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertEquals(1, coll.getItems().size());
        items = coll.getItems();
        assertNotNull(items);
        assertFalse(items.contains(kalturaAPIService.miAd_L1)); // hidden
        assertFalse(items.contains(kalturaAPIService.miAd_L2));
        assertFalse(items.contains(kalturaAPIService.miAd_L3));

        // add / remove to collection
        try {
            item = service.addKalturaItemToCollection(coll.getIdStr(), coll.getLocationId(), kalturaAPIService.miL7.getIdStr(), 0);
            itemId = item.getId();
            item = kalturaAPIService.getMediaItemById(itemId);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }
        try {
            service.removeKalturaItemFromCollection(coll.getIdStr(), kalturaAPIService.miL1.getIdStr());
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        kalturaAPIService.miL6.setHidden(true); // set item to hidden
        collId = kalturaAPIService.mcPu.getIdStr();
        coll = service.getCollection(collId, 0, -1); // public
        assertNotNull(coll);
        assertEquals(0, coll.getItems().size());
        item = service.addKalturaItemToCollection(coll.getIdStr(), coll.getLocationId(), kalturaAPIService.miL6.getIdStr(), 0);
        itemId = item.getId();
        item = kalturaAPIService.getMediaItemById(itemId);
        assertTrue(item.isHidden());
        coll = service.getCollection(collId, 0, -1);
        assertEquals(1, coll.getItems().size());
        service.removeKalturaItemFromCollection(coll.getIdStr(), item.getIdStr());
        coll = service.getCollection(collId, 0, -1);
        assertEquals(0, coll.getItems().size());

        collId = kalturaAPIService.mcSh.getIdStr();
        coll = service.getCollection(collId, 0, -1); // shared
        assertNotNull(coll);
        assertEquals(0, coll.getItems().size());
        item = service.addKalturaItemToCollection(coll.getIdStr(), coll.getLocationId(), kalturaAPIService.miL6.getIdStr(), 0);
        itemId = item.getId();
        item = kalturaAPIService.getMediaItemById(itemId);
        assertTrue(item.isHidden());
        coll = service.getCollection(collId, 0, -1);
        assertEquals(1, coll.getItems().size());
        service.removeKalturaItemFromCollection(coll.getIdStr(), item.getIdStr());
        coll = service.getCollection(collId, 0, -1);
        assertEquals(0, coll.getItems().size());
        kalturaAPIService.miL6.setHidden(false); // reset item to public

        // view / hidden
        library = service.getLibrary(locationId, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(2, library.size());
        items = library;
        assertNotNull(items);
        assertFalse(items.contains(kalturaAPIService.miL1)); // hidden
        assertFalse(items.contains(kalturaAPIService.miL2));
        assertFalse(items.contains(kalturaAPIService.miL3)); // SUPER
        assertFalse(items.contains(kalturaAPIService.miL4));
        assertFalse(items.contains(kalturaAPIService.miL5)); // hidden
        assertTrue(items.contains(kalturaAPIService.miL6)); // AW
        assertTrue(items.contains(kalturaAPIService.miL7)); // AW, hidden
        assertFalse(items.contains(kalturaAPIService.miL8)); // AE, hidden

        // edit item
        item = kalturaAPIService.miL3;
        assertNotNull(item);
        d = item.getDateModified();
        try {
            service.updateMediaItem(item);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        // edit collection item
        item = kalturaAPIService.miAd_L1;
        assertNotNull(item);
        d = item.getDateModified();
        try {
            service.updateMediaItem(item);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        // edit item perms
        item = kalturaAPIService.miL3;
        assertNotNull(item);
        d = item.getDateModified();
        assertFalse(item.isHidden());
        assertTrue(item.isShared());
        item.setHidden(true);
        item.setShared(false);
        try {
            service.updateMediaItem(item);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        // upload
        kalturaAPIService.miNO_B.setHidden(true); // set item to hidden
        itemId = service.addKalturaItemToLibrary(locationId, "kidB");
        assertNotNull(itemId);
        item = kalturaAPIService.getMediaItemById(itemId);
        assertTrue(item.isHidden());
        kalturaAPIService.miNO_B.setHidden(false); // reset item to public
        library = service.getLibrary(locationId, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(3, library.size());
        assertTrue(library.contains(item));

        // remove
        service.removeKalturaItemFromLibrary(locationId, item.getIdStr());
        library = service.getLibrary(locationId, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(2, library.size());

        try {
            service.removeKalturaItemFromLibrary(locationId, kalturaAPIService.miL1.getIdStr());
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        // test remixing/clipping
        String currentCreator = kalturaAPIService.miL3.getCreatorUserId();
        kalturaAPIService.miL3.setCreatorId(external.getCurrentUserName());
        String myKalturaId = kalturaAPIService.miL3.getId();
        String kalturaId = kalturaAPIService.miL6.getId();

        item = service.getMyMediaByKalturaId(myKalturaId, external.currentUserId);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        item = service.getLibraryMediaItem(kalturaId, locationId, true);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        // using miAd_L6 as the origin item
        item = service.getCollectionMediaItem(kalturaId, kalturaAPIService.mcAd.getId(), true);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        // force the remix flag to off
        kalturaAPIService.miL6.setRemixable(false);
        kalturaAPIService.miAd_L6.setRemixable(false);

        item = service.getMyMediaByKalturaId(myKalturaId, external.currentUserId);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        item = service.getLibraryMediaItem(kalturaId, locationId, true);
        assertNotNull(item);
        assertFalse(item.isRemixable());
        assertTrue(item.isCanRemix());

        // using miAd_L6 as the origin item
        item = service.getCollectionMediaItem(kalturaId, kalturaAPIService.mcAd.getId(), true);
        assertNotNull(item);
        assertFalse(item.isRemixable());
        assertTrue(item.isCanRemix());

        // reset it back to what it was
        kalturaAPIService.miL6.setRemixable(true);
        kalturaAPIService.miAd_L6.setRemixable(true);
        kalturaAPIService.miL3.setCreatorId(currentCreator);

    }

    public void testOverallPermsEditor() {
        List<MediaCollection> colls;
        List<MediaItem> library;
        List<MediaItem> items;
        MediaItem item;
        String itemId;
        MediaCollection coll;
        String locationId = KalturaAPIServiceStub.LOCATION1_ID;
        String kalturaId = "kidA";
        String coll_title = "";
        String collId = "";
        Date d = null;
        assertNull(d);

        external.currentUserId = KalturaAPIServiceStub.ACCESS_USER_ID_E; // EDITOR

        // view collections / hidden
        colls = service.getCollections(locationId, null, -1, null, 0, 0);
        assertNotNull(colls);
        assertEquals(12, colls.size()); // 2 hidden of 14

        // collection viewing
        Map<String, MediaCollection> mcs = new HashMap<String, MediaCollection>();
        for (MediaCollection mc : colls) {
            mcs.put(mc.getIdStr(), mc);
        }
        assertTrue(mcs.get(kalturaAPIService.mcPr.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcAd.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcSh.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPu.getIdStr()).isViewable());
        //assertTrue(mcs.get(kalturaAPIService.mcPrH.getIdStr()).isViewable());
        //assertTrue(mcs.get(kalturaAPIService.mcShH.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPrE.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcAdE.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcShE.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPuE.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPrNoPub.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcAdNoPub.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcShNoPub.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPuNoPub.getIdStr()).isViewable());

        // edit coll
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, 0);
        assertNotNull(coll);
        assertNull(coll.getItems());
        coll_title = "test"+System.currentTimeMillis();
        assertNotSame(coll_title, coll.getTitle());
        coll.setTitle(coll_title);
        try {
            service.updateCollection(coll);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        // view coll items
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertEquals(5, coll.getItems().size());
        items = coll.getItems();
        assertNotNull(items);
        assertTrue(items.contains(kalturaAPIService.miAd_L1)); // hidden
        assertTrue(items.contains(kalturaAPIService.miAd_L2));
        assertTrue(items.contains(kalturaAPIService.miAd_L3));

        // add / remove to collection
        kalturaAPIService.miL3.setHidden(true); // set item to hidden
        collId = kalturaAPIService.mcPu.getIdStr();
        coll = service.getCollection(collId, 0, -1); // public
        assertNotNull(coll);
        assertEquals(2, coll.getItems().size());
        item = service.addKalturaItemToCollection(coll.getIdStr(), null, kalturaAPIService.miL3.getIdStr(), 0);
        itemId = item.getId();
        item = kalturaAPIService.getMediaItemById(itemId);
        assertTrue(item.isHidden());
        coll = service.getCollection(collId, 0, -1);
        assertEquals(3, coll.getItems().size());
        service.removeKalturaItemFromCollection(coll.getIdStr(), item.getIdStr());

        coll = service.getCollection(collId, 0, -1);
        assertEquals(2, coll.getItems().size());

        collId = kalturaAPIService.mcSh.getIdStr();
        coll = service.getCollection(collId, 0, -1); // shared
        assertNotNull(coll);
        assertEquals(2, coll.getItems().size());
        item = service.addKalturaItemToCollection(coll.getIdStr(), coll.getLocationId(), kalturaAPIService.miL3.getIdStr(), 0);
        itemId = item.getId();
        item = kalturaAPIService.getMediaItemById(itemId);
        assertTrue(item.isHidden());
        coll = service.getCollection(collId, 0, -1);
        assertEquals(3, coll.getItems().size());
        service.removeKalturaItemFromCollection(coll.getIdStr(), item.getIdStr());
        coll = service.getCollection(collId, 0, -1);
        assertEquals(2, coll.getItems().size());
        kalturaAPIService.miL3.setHidden(false); // reset item to public

        // view / hidden
        library = service.getLibrary(locationId, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(8, library.size());
        items = library;
        assertNotNull(items);
        assertTrue(items.contains(kalturaAPIService.miL1)); // hidden
        assertTrue(items.contains(kalturaAPIService.miL2));
        assertTrue(items.contains(kalturaAPIService.miL3)); // SUPER
        assertTrue(items.contains(kalturaAPIService.miL4));
        assertTrue(items.contains(kalturaAPIService.miL5)); // hidden
        assertTrue(items.contains(kalturaAPIService.miL6)); // AW
        assertTrue(items.contains(kalturaAPIService.miL7)); // AW, hidden
        assertTrue(items.contains(kalturaAPIService.miL8)); // AE, hidden

        // edit item
        item = kalturaAPIService.miL3;
        assertNotNull(item);
        d = item.getDateModified();
        try {
            service.updateMediaItem(item);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        // edit collection item
        item = kalturaAPIService.miAd_L1;
        assertNotNull(item);
        d = item.getDateModified();
        try {
            service.updateMediaItem(item);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        // edit item perms
        item = kalturaAPIService.miL3;
        assertNotNull(item);
        d = item.getDateModified();
        assertFalse(item.isHidden());
        assertTrue(item.isShared());
        item.setHidden(true);
        item.setShared(false);
        try {
            service.updateMediaItem(item);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        // upload
        try {
            service.addKalturaItemToLibrary(locationId, kalturaId);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }
        library = service.getLibrary(locationId, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(8, library.size());

        // remove
        service.removeKalturaItemFromLibrary(locationId, kalturaAPIService.miL8.getIdStr());
        library = service.getLibrary(locationId, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(7, library.size());

        try {
            service.removeKalturaItemFromLibrary(locationId, kalturaAPIService.miL1.getIdStr());
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        // test remixing/clipping
        String currentCreator = kalturaAPIService.miL3.getCreatorUserId();
        kalturaAPIService.miL3.setCreatorId(external.getCurrentUserName());
        String myKalturaId = kalturaAPIService.miL3.getId();
        kalturaId = kalturaAPIService.miL2.getId();

        item = service.getMyMediaByKalturaId(myKalturaId, external.currentUserId);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        item = service.getLibraryMediaItem(kalturaId, locationId, true);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        // using miPu_L2 as the origin item
        item = service.getCollectionMediaItem(kalturaId, kalturaAPIService.mcPu.getId(), true);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        // force the remix flag to off
        kalturaAPIService.miL2.setRemixable(false);
        kalturaAPIService.miPu_L2.setRemixable(false);

        item = service.getMyMediaByKalturaId(myKalturaId, external.currentUserId);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        item = service.getLibraryMediaItem(kalturaId, locationId, true);
        assertNotNull(item);
        assertFalse(item.isRemixable());
        assertFalse(item.isCanRemix());

        item = service.getCollectionMediaItem(kalturaId, kalturaAPIService.mcPu.getId(), true);
        assertNotNull(item);
        assertFalse(item.isRemixable());
        assertFalse(item.isCanRemix());

        // reset it back to what it was
        kalturaAPIService.miL2.setRemixable(true);
        kalturaAPIService.miPu_L2.setRemixable(true);
        kalturaAPIService.miL3.setCreatorId(currentCreator);

    }

    public void testOverallPermsRead() {
        List<MediaCollection> colls;
        List<MediaItem> library;
        List<MediaItem> items;
        MediaItem item;
        String itemId;
        MediaCollection coll;
        String locationId = KalturaAPIServiceStub.LOCATION1_ID;
        String kalturaId = "kidA";
        String coll_title = "";
        String collId = "";
        Date d = null;
        assertNull(d);

        external.currentUserId = KalturaAPIServiceStub.ACCESS_USER_ID; // READ

        // view collections / hidden
        colls = service.getCollections(locationId, null, -1, null, 0, 0);
        assertNotNull(colls);
        assertEquals(12, colls.size()); // 2 hidden of 14

        // collection viewing
        Map<String, MediaCollection> mcs = new HashMap<String, MediaCollection>();
        for (MediaCollection mc : colls) {
            mcs.put(mc.getIdStr(), mc);
        }
        assertFalse(mcs.get(kalturaAPIService.mcPr.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcAd.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcSh.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPu.getIdStr()).isViewable());
        //assertTrue(mcs.get(kalturaAPIService.mcPrH.getIdStr()).isViewable());
        //assertTrue(mcs.get(kalturaAPIService.mcShH.getIdStr()).isViewable());
        assertFalse(mcs.get(kalturaAPIService.mcPrE.getIdStr()).isViewable());
        assertFalse(mcs.get(kalturaAPIService.mcAdE.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcShE.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPuE.getIdStr()).isViewable());
        assertFalse(mcs.get(kalturaAPIService.mcPrNoPub.getIdStr()).isViewable());
        assertFalse(mcs.get(kalturaAPIService.mcAdNoPub.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcShNoPub.getIdStr()).isViewable());
        assertTrue(mcs.get(kalturaAPIService.mcPuNoPub.getIdStr()).isViewable());

        // edit coll
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, 0);
        assertNotNull(coll);
        assertNull(coll.getItems());
        coll_title = "test"+System.currentTimeMillis();
        assertNotSame(coll_title, coll.getTitle());
        coll.setTitle(coll_title);
        try {
            service.updateCollection(coll);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        // view coll items
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertEquals(3, coll.getItems().size());
        items = coll.getItems();
        assertNotNull(items);
        assertFalse(items.contains(kalturaAPIService.miAd_L1)); // hidden
        assertTrue(items.contains(kalturaAPIService.miAd_L2));
        assertTrue(items.contains(kalturaAPIService.miAd_L3));

        // add / remove to collection
        try {
            service.addKalturaItemToCollection(coll.getIdStr(), coll.getLocationId(), kalturaAPIService.miL7.getIdStr(), 0);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }
        try {
            service.removeKalturaItemFromCollection(coll.getIdStr(), kalturaAPIService.miL1.getIdStr());
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        kalturaAPIService.miL3.setHidden(true); // set item to hidden
        collId = kalturaAPIService.mcPu.getIdStr();
        coll = service.getCollection(collId, 0, -1); // public
        assertNotNull(coll);
        assertEquals(2, coll.getItems().size());
        item = service.addKalturaItemToCollection(coll.getIdStr(), coll.getLocationId(), kalturaAPIService.miL3.getIdStr(), 0);
        itemId = item.getId();
        item = kalturaAPIService.getMediaItemById(itemId);
        assertTrue(item.isHidden());
        coll = service.getCollection(collId, 0, -1);
        assertEquals(3, coll.getItems().size());
        service.removeKalturaItemFromCollection(coll.getIdStr(), item.getIdStr());
        coll = service.getCollection(collId, 0, -1);
        assertEquals(2, coll.getItems().size());
        kalturaAPIService.miL3.setHidden(false); // reset item to public

        collId = kalturaAPIService.mcSh.getIdStr();
        coll = service.getCollection(collId, 0, -1); // shared
        assertNotNull(coll);
        assertEquals(1, coll.getItems().size());
        item = service.addKalturaItemToCollection(coll.getIdStr(), coll.getLocationId(), kalturaAPIService.miL3.getIdStr(), 0);
        itemId = item.getId();
        item = kalturaAPIService.getMediaItemById(itemId);
        coll = service.getCollection(collId, 0, -1);
        assertEquals(2, coll.getItems().size());
        service.removeKalturaItemFromCollection(coll.getIdStr(), item.getIdStr());
        coll = service.getCollection(collId, 0, -1);
        assertEquals(1, coll.getItems().size());

        // view / hidden
        library = service.getLibrary(locationId, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(4, library.size());
        items = library;
        assertNotNull(items);
        assertFalse(items.contains(kalturaAPIService.miL1)); // hidden
        assertTrue(items.contains(kalturaAPIService.miL2));
        assertTrue(items.contains(kalturaAPIService.miL3)); // SUPER
        assertTrue(items.contains(kalturaAPIService.miL4));
        assertFalse(items.contains(kalturaAPIService.miL5)); // hidden
        assertTrue(items.contains(kalturaAPIService.miL6)); // AW
        assertFalse(items.contains(kalturaAPIService.miL7)); // AW, hidden
        assertFalse(items.contains(kalturaAPIService.miL8)); // AE, hidden

        // edit item
        item = kalturaAPIService.miL3;
        assertNotNull(item);
        d = item.getDateModified();
        try {
            service.updateMediaItem(item);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        // edit collection item
        item = kalturaAPIService.miAd_L1;
        assertNotNull(item);
        d = item.getDateModified();
        try {
            service.updateMediaItem(item);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        // edit item perms
        item = kalturaAPIService.getMediaItemById(kalturaAPIService.miL3.getIdStr());
        assertNotNull(item);
        d = item.getDateModified();
        assertFalse(item.isHidden());
        assertTrue(item.isShared());
        try {
            service.updateMediaItem(item);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        // upload
        try {
            service.addKalturaItemToLibrary(locationId, kalturaId);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }
        library = service.getLibrary(locationId, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(4, library.size());

        // remove
        try {
            service.removeKalturaItemFromLibrary(locationId, kalturaAPIService.miL1.getIdStr());
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }
        library = service.getLibrary(locationId, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(4, library.size());

        // test remixing/clipping
        String currentCreator = kalturaAPIService.miL3.getCreatorUserId();
        kalturaAPIService.miL3.setCreatorId(external.getCurrentUserName());
        String myKalturaId = kalturaAPIService.miL3.getId();
        kalturaId = kalturaAPIService.miL2.getId();

        item = service.getMyMediaByKalturaId(myKalturaId, external.currentUserId);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        item = service.getLibraryMediaItem(kalturaId, locationId, true);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertFalse(item.isCanRemix());

        // using miPu_L2 as the origin item
        item = service.getCollectionMediaItem(kalturaId, kalturaAPIService.mcPu.getId(), true);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        // force the remix flag to off
        kalturaAPIService.miL2.setRemixable(false);
        kalturaAPIService.miPu_L2.setRemixable(false);

        item = service.getMyMediaByKalturaId(myKalturaId, external.currentUserId);
        assertNotNull(item);
        assertTrue(item.isRemixable());
        assertTrue(item.isCanRemix());

        item = service.getLibraryMediaItem(kalturaId, locationId, true);
        assertNotNull(item);
        assertFalse(item.isRemixable());
        assertFalse(item.isCanRemix());

        item = service.getCollectionMediaItem(kalturaId, kalturaAPIService.mcPu.getId(), true);
        assertNotNull(item);
        assertFalse(item.isRemixable());
        assertFalse(item.isCanRemix());

        // reset it back to what it was
        kalturaAPIService.miL2.setRemixable(true);
        kalturaAPIService.miPu_L2.setRemixable(true);
        kalturaAPIService.miL3.setCreatorId(currentCreator);

    }

    public void testOverallPermsNone() {
        List<MediaCollection> colls;
        List<MediaItem> library;
        List<MediaItem> items;
        MediaItem item;
        //String itemId;
        MediaCollection coll;
        String locationId = KalturaAPIServiceStub.LOCATION1_ID;
        String kalturaId = "kidA";
        String coll_title = "";
        Date d = null;
        assertNull(d);

        external.currentUserId = KalturaAPIServiceStub.USER_ID; // NO PERMS

        // view collections / hidden
        colls = service.getCollections(locationId, null, -1, null, 0, 0);
        assertNotNull(colls);
        assertEquals(12, colls.size()); // 2 hidden of 14

        // collection viewing
        Map<String, MediaCollection> mcs = new HashMap<String, MediaCollection>();
        for (MediaCollection mc : colls) {
            mcs.put(mc.getIdStr(), mc);
        }
        assertFalse(mcs.get(kalturaAPIService.mcPr.getIdStr()).isViewable());
        assertFalse(mcs.get(kalturaAPIService.mcAd.getIdStr()).isViewable());
        assertFalse(mcs.get(kalturaAPIService.mcSh.getIdStr()).isViewable());
        assertFalse(mcs.get(kalturaAPIService.mcPu.getIdStr()).isViewable());
        //assertTrue(mcs.get(kalturaAPIService.mcPrH.getIdStr()).isViewable());
        //assertTrue(mcs.get(kalturaAPIService.mcShH.getIdStr()).isViewable());
        assertFalse(mcs.get(kalturaAPIService.mcPrE.getIdStr()).isViewable());
        assertFalse(mcs.get(kalturaAPIService.mcAdE.getIdStr()).isViewable());
        assertFalse(mcs.get(kalturaAPIService.mcShE.getIdStr()).isViewable());
        assertFalse(mcs.get(kalturaAPIService.mcPuE.getIdStr()).isViewable());
        assertFalse(mcs.get(kalturaAPIService.mcPrNoPub.getIdStr()).isViewable());
        assertFalse(mcs.get(kalturaAPIService.mcAdNoPub.getIdStr()).isViewable());
        assertFalse(mcs.get(kalturaAPIService.mcShNoPub.getIdStr()).isViewable());
        assertFalse(mcs.get(kalturaAPIService.mcPuNoPub.getIdStr()).isViewable());

        // edit coll
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, 0);
        assertNotNull(coll);
        assertNull(coll.getItems());
        coll_title = "test"+System.currentTimeMillis();
        assertNotSame(coll_title, coll.getTitle());
        coll.setTitle(coll_title);
        try {
            service.updateCollection(coll);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        // view coll items
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertEquals(0, coll.getItems().size());
        items = coll.getItems();
        assertNotNull(items);
        assertFalse(items.contains(kalturaAPIService.miAd_L1)); // hidden
        assertFalse(items.contains(kalturaAPIService.miAd_L2));
        assertFalse(items.contains(kalturaAPIService.miAd_L3));

        // add / remove to collection
        try {
            service.addKalturaItemToCollection(coll.getIdStr(), coll.getLocationId(), kalturaAPIService.miL7.getIdStr(), 0);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }
        try {
            service.removeKalturaItemFromCollection(coll.getIdStr(), kalturaAPIService.miL1.getIdStr());
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        // view / hidden
        library = service.getLibrary(locationId, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(0, library.size());
        items = library;
        assertNotNull(items);
        assertFalse(items.contains(kalturaAPIService.miL1)); // hidden
        assertFalse(items.contains(kalturaAPIService.miL2));
        assertFalse(items.contains(kalturaAPIService.miL3)); // SUPER
        assertFalse(items.contains(kalturaAPIService.miL4));
        assertFalse(items.contains(kalturaAPIService.miL5)); // hidden
        assertFalse(items.contains(kalturaAPIService.miL6)); // AW
        assertFalse(items.contains(kalturaAPIService.miL7)); // AW, hidden

        // edit item
        item = kalturaAPIService.miL3;
        assertNotNull(item);
        d = item.getDateModified();
        try {
            service.updateMediaItem(item);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        // edit collection item
        item = kalturaAPIService.miAd_L1;
        assertNotNull(item);
        d = item.getDateModified();
        try {
            service.updateMediaItem(item);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        // edit item perms
        item = kalturaAPIService.miL3;
        assertNotNull(item);
        d = item.getDateModified();
        assertFalse(item.isHidden());
        assertTrue(item.isShared());
        try {
            service.updateMediaItem(item);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        // upload
        try {
            service.addKalturaItemToLibrary(locationId, kalturaId);
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }
        library = service.getLibrary(locationId, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(0, library.size());

        // remove
        try {
            service.removeKalturaItemFromLibrary(locationId, item.getIdStr());
            fail("should have failed");
        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }

        library = service.getLibrary(locationId, null, null, null, 0, -1);
        assertNotNull(library);
        assertEquals(0, library.size());

        // test remixing/clipping
        // NOTE: this user can never remix, so no testing needed

    }


    public void testCollUploadAddItemsPermissions() {
        MediaCollection coll;

        // SUPER ADMIN
        external.currentUserId = KalturaAPIServiceStub.ADMIN_USER_ID;

        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertTrue(coll.isAddItems());
        coll = service.getCollection(kalturaAPIService.mcPr.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertTrue(coll.isAddItems());
        coll = service.getCollection(kalturaAPIService.mcPu.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertTrue(coll.isAddItems());
        coll = service.getCollection(kalturaAPIService.mcSh.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertTrue(coll.isAddItems());

        // TEACHER
        external.currentUserId = KalturaAPIServiceStub.MAINT_USER_ID;

        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertTrue(coll.isAddItems());
        coll = service.getCollection(kalturaAPIService.mcPr.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertTrue(coll.isAddItems());
        coll = service.getCollection(kalturaAPIService.mcPu.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertTrue(coll.isAddItems());
        coll = service.getCollection(kalturaAPIService.mcSh.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertTrue(coll.isAddItems());

        // TEACHER (not owner)
        external.currentUserId = KalturaAPIServiceStub.MAINT_USER_ID_A;

        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertTrue(coll.isAddItems());
        coll = service.getCollection(kalturaAPIService.mcPr.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertFalse(coll.isAddItems());
        coll = service.getCollection(kalturaAPIService.mcPu.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertTrue(coll.isAddItems());
        coll = service.getCollection(kalturaAPIService.mcSh.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertTrue(coll.isAddItems());

        // STUDENT
        external.currentUserId = KalturaAPIServiceStub.ACCESS_USER_ID;

        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertFalse(coll.isAddItems());
        coll = service.getCollection(kalturaAPIService.mcPr.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertFalse(coll.isAddItems());
        coll = service.getCollection(kalturaAPIService.mcPu.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertTrue(coll.isAddItems());
        coll = service.getCollection(kalturaAPIService.mcSh.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertTrue(coll.isAddItems());

        // STUDENT (not in course)
        external.currentUserId = KalturaAPIServiceStub.USER_ID;

        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertFalse(coll.isAddItems());
        coll = service.getCollection(kalturaAPIService.mcPr.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertFalse(coll.isAddItems());
        coll = service.getCollection(kalturaAPIService.mcPu.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertFalse(coll.isAddItems());
        coll = service.getCollection(kalturaAPIService.mcSh.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertFalse(coll.isAddItems());

    }

    public void testMetadataEncoding() {
        String encoded = null;
        Map<String, String> decoded = null;
        // DECODING
        encoded = "site:<siteId>::{<ownerId>}h,S,r";
        decoded = kalturaAPIService.decodeMetadataPermissions(encoded, false);
        assertNotNull(decoded);
        assertEquals("site", decoded.get("containerType"));
        assertEquals("<siteId>", decoded.get("containerId"));
        assertEquals("<ownerId>", decoded.get("Owner"));
        assertEquals("h", decoded.get("Hidden"));
        assertEquals("S", decoded.get("Reusable"));
        assertEquals("r", decoded.get("Remixable"));

        encoded = "playlist:<playlistId>::{<ownerId>}h,s,r";
        decoded = kalturaAPIService.decodeMetadataPermissions(encoded, false);
        assertNotNull(decoded);
        assertEquals("playlist", decoded.get("containerType"));
        assertEquals("<playlistId>", decoded.get("containerId"));
        assertEquals("<ownerId>", decoded.get("Owner"));
        assertEquals("h", decoded.get("Hidden"));
        assertEquals("s", decoded.get("Reusable"));
        assertEquals("r", decoded.get("Remixable"));

        encoded = "site:3674fc9f-8d5c-4ede-86f0-2f7432edb0db::{azeckoski@unicon.net}h,s,r";
        decoded = kalturaAPIService.decodeMetadataPermissions(encoded, false);
        assertNotNull(decoded);
        assertEquals("site", decoded.get("containerType"));
        assertEquals("3674fc9f-8d5c-4ede-86f0-2f7432edb0db", decoded.get("containerId"));
        assertEquals("azeckoski@unicon.net", decoded.get("Owner"));
        assertEquals("h", decoded.get("Hidden"));
        assertEquals("s", decoded.get("Reusable"));
        assertEquals("r", decoded.get("Remixable"));

        encoded = "site:crazy:{site}::(1234)::{crazy:{user}::(abcd)}h,s,r";
        decoded = kalturaAPIService.decodeMetadataPermissions(encoded, false);
        assertNotNull(decoded);
        assertEquals("site", decoded.get("containerType"));
        assertEquals("crazy:{site}::(1234)", decoded.get("containerId"));
        assertEquals("crazy:{user}::(abcd)", decoded.get("Owner"));
        assertEquals("h", decoded.get("Hidden"));
        assertEquals("s", decoded.get("Reusable"));
        assertEquals("r", decoded.get("Remixable"));

        // missing site id
        try {
            encoded = "site::{<ownerId>}h,S,r";
            decoded = kalturaAPIService.decodeMetadataPermissions(encoded, false);
            fail("should have died");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        // missing owner
        try {
            encoded = "playlist:<playlistId>::{h,s,r";
            decoded = kalturaAPIService.decodeMetadataPermissions(encoded, false);
            fail("should have died");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        // missing permissions
        try {
            encoded = "site:<siteId>::{<ownerId>}";
            decoded = kalturaAPIService.decodeMetadataPermissions(encoded, false);
            fail("should have died");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        // empty
        encoded = "";
        decoded = kalturaAPIService.decodeMetadataPermissions(encoded, false);
        assertNotNull(decoded); // defaults

        try {
            encoded = "";
            decoded = kalturaAPIService.decodeMetadataPermissions(encoded, true);
            fail("should have died");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        // ENCODING
        Map<String, String> permissions = new HashMap<String, String>();
        permissions.put("Hidden", "h");
        permissions.put("Reusable", "S");
        permissions.put("Remixable", "r");

        encoded = kalturaAPIService.encodeMetadataPermissions("site", "AZsite1", "azeckoski", permissions);
        assertNotNull(encoded);
        assertEquals("site:AZsite1::{azeckoski}h,S,r", encoded);

        encoded = kalturaAPIService.encodeMetadataPermissions("playlist", "87g{123g8}73()21_8::g71:3287g132", "az{fds}12(dfsdf)::3iu32", permissions);
        assertNotNull(encoded);
        assertEquals("playlist:87g{123g8}73()21_8::g71:3287g132::{az{fds}12(dfsdf)::3iu32}h,S,r", encoded);

        encoded = kalturaAPIService.encodeMetadataPermissions("site", "123456", "azeckoski", null);
        assertNotNull(encoded);
        assertEquals("site:123456::{azeckoski}H,s,r", encoded);

        // check for bad container
        try {
            encoded = kalturaAPIService.encodeMetadataPermissions("XXXXXXinvalid", "123", "asd", permissions);
            fail("should have died");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        // check for bad container id
        try {
            encoded = kalturaAPIService.encodeMetadataPermissions("site", "", "asd", permissions);
            fail("should have died");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

    }


    public void testUpdateCollectionMediaItems() {
        MediaCollection coll;
        List<MediaItem> items;
        String[] kalturaIds;

        // SUPER ADMIN
        external.currentUserId = KalturaAPIServiceStub.ADMIN_USER_ID;

        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertTrue(coll.isAddItems());
        items = coll.getItems();
        assertEquals(5, items.size());
        kalturaIds = coll.getKalturaIds().toArray(new String[items.size()]);
        service.updateCollectionMediaItems(coll.getId(), kalturaIds);
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertEquals(5, coll.getItems().size()); // no change in size

        // test adding one
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertTrue(coll.isAddItems());
        items = coll.getItems();
        assertEquals(5, items.size());
        kalturaIds = coll.getKalturaIds().toArray(new String[items.size()]);
        kalturaIds = (String[]) ArrayUtils.add(kalturaIds, "kid7");
        service.updateCollectionMediaItems(coll.getId(), kalturaIds);
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertEquals(6, coll.getItems().size()); // +1 size
        assertTrue(coll.getKalturaIds().contains("kid7"));

        // test removing one
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertTrue(coll.isAddItems());
        items = coll.getItems();
        assertEquals(6, items.size());
        kalturaIds = coll.getKalturaIds().toArray(new String[items.size()]);
        kalturaIds = (String[]) ArrayUtils.removeElement(kalturaIds, "kid7");
        service.updateCollectionMediaItems(coll.getId(), kalturaIds);
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertEquals(5, coll.getItems().size()); // -1 size
        assertFalse(coll.getKalturaIds().contains("kid7"));

        // testing adding and removing
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertNotNull(coll);
        assertTrue(coll.isAddItems());
        items = coll.getItems();
        assertEquals(5, items.size());
        kalturaIds = coll.getKalturaIds().toArray(new String[items.size()]);
        kalturaIds = (String[]) ArrayUtils.removeElement(kalturaIds, "kid5");
        kalturaIds = (String[]) ArrayUtils.add(kalturaIds, "kidA");
        kalturaIds = (String[]) ArrayUtils.add(kalturaIds, "kidB");
        service.updateCollectionMediaItems(coll.getId(), kalturaIds);
        coll = service.getCollection(kalturaAPIService.mcAd.getIdStr(), 0, -1);
        assertEquals(6, coll.getItems().size()); // -1 +2 size
        assertFalse(coll.getKalturaIds().contains("kid5"));
        assertTrue(coll.getKalturaIds().contains("kidA"));
        assertTrue(coll.getKalturaIds().contains("kidB"));
    }


    // currently not running this test
    public void disabledTestLongFloatCalcs() {
        int percent = 0;
        percent = Math.round(((float)100l / 100l)*100.0f);
        assertEquals(100, percent);
        percent = Math.round(((float)50l / 100l)*100.0f);
        assertEquals(50, percent);
        percent = Math.round(((float)0l / 100l)*100.0f);
        assertEquals(0, percent);
        percent = Math.round(((float)134l / 432l)*100.0f);
        assertEquals(31, percent);
    }

}
