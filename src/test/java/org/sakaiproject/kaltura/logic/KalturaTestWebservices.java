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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import org.sakaiproject.kaltura.logic.stubs.ExternalLogicStub;
import org.sakaiproject.kaltura.model.MediaItem;
import org.springframework.test.AbstractTransactionalSpringContextTests;

import com.kaltura.client.KalturaApiException;
import com.kaltura.client.KalturaClient;
import com.kaltura.client.types.KalturaFilterPager;
import com.kaltura.client.types.KalturaMediaEntry;
import com.kaltura.client.types.KalturaMediaEntryFilter;
import com.kaltura.client.types.KalturaMediaListResponse;


/**
 * Testing the kaltura APIs (this is only executed manually and not during maven builds)
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class KalturaTestWebservices extends AbstractTransactionalSpringContextTests {

    protected KalturaAPIService service;
    protected ExternalLogicStub external;

    private FakeDataPreload tdp;

    // all items should be tagged with WSTEST to make them easy to find
    private static String KEID1 = "0_ammi4uyn"; // video, AZ
    private static String KEID2 = "0_hblbtdcd"; // video, AZ
    private static String KEID3 = "0_yfgrlthp"; // image
    private static String KEID4 = "0_du86z2fd"; // image
    private static String KEID5 = "0_mwlsjn1i"; // video
    private static String KEID6 = "0_3bfjhugt"; // image
    private static String KEID7 = "0_ptj1bsno"; // audio

    // run this before each test starts and as part of the transaction
    protected void onSetUpInTransaction() {

        // make a cache
        CacheManager cacheManager = CacheManager.create();
        if (! cacheManager.cacheExists("ehcache.sakai.kaltura.entries")) {
            cacheManager.addCache("ehcache.sakai.kaltura.entries");
        }
        Ehcache entriesCache = cacheManager.getCache("ehcache.sakai.kaltura.entries");

        // create and setup the object to be tested
        external = new ExternalLogicStub();
        service = new KalturaAPIService();
        service.setExternal(external);
        service.setEntriesCache(entriesCache);
        service.setOffline(true); // for testing we do not try to fetch real data

        // run the init
        external.currentUserId = FakeDataPreload.ADMIN_USER_ID; // DEFAULT ADMIN
        // UNICON settings for testing
        external.config.put("kaltura.partnerid", 166762);
        external.config.put("kaltura.adminsecret", "26d08a0ba54c911492bbc7599028295f");
        external.config.put("kaltura.secret", "6e4755b613a38b19e4cfb5d7405ed170");

        service.testWSInit(); // SPECIAL INIT

        service.getKalturaClient();
    }

    @Override
    protected void onTearDownInTransaction() throws Exception {
        service.clearKalturaClient();
    }

    public void testKMEAll() {
        KalturaClient client = service.getKalturaClient();
        KalturaMediaEntryFilter filt = new KalturaMediaEntryFilter();
        KalturaFilterPager pager = new KalturaFilterPager();
        pager.pageSize = 100;
        try {
            KalturaMediaListResponse list =
                client.getMediaService().list(filt, pager);
            assertTrue(list.objects.size() > 36);
        } catch (KalturaApiException e) {
            fail(e.toString());
        }
    }

    public void testKMEIdsTypes() {
        List<String> keids = new ArrayList<String>();
        keids.add(KEID5);
        keids.add(KEID6);
        keids.add(KEID7);
        KalturaClient client = service.getKalturaClient();
        KalturaMediaEntryFilter filt = new KalturaMediaEntryFilter();
        filt.idIn = KEID5+","+KEID6+","+KEID7;
        MediaItem video = null;
        MediaItem image = null;
        MediaItem audio = null;
        try {
            KalturaMediaListResponse list =
                client.getMediaService().list(filt);
            assertEquals(3, list.objects.size());
            for (KalturaMediaEntry kme : list.objects) {
                if (! keids.contains(kme.id)) {
                    fail("keid ("+kme.id+") not in the allowed set ("+list+")");
                }
                if (KEID5.equals(kme.id)) {
                    video = new MediaItem(FakeDataPreload.LOCATION1_ID, kme.id, FakeDataPreload.ADMIN_USER_ID, true, false, false);
                    video.setKalturaItem(kme);
                } else if (KEID6.equals(kme.id)) {
                    image = new MediaItem(FakeDataPreload.LOCATION1_ID, kme.id, FakeDataPreload.ADMIN_USER_ID, true, false, false);
                    image.setKalturaItem(kme);
                } else if (KEID7.equals(kme.id)) {
                    audio = new MediaItem(FakeDataPreload.LOCATION1_ID, kme.id, FakeDataPreload.ADMIN_USER_ID, true, false, false);
                    audio.setKalturaItem(kme);
                }
            }
        } catch (KalturaApiException e) {
            fail(e.toString());
        }
        assertNotNull(video);
        assertNotNull(image);
        assertNotNull(audio);
        assertEquals(KEID5, video.getKalturaId());
        assertEquals(KEID6, image.getKalturaId());
        assertEquals(KEID7, audio.getKalturaId());
        assertEquals("video", video.findType());
        assertEquals("image", image.findType());
        assertEquals("audio", audio.findType());
    }

    public void testKMEIdsIn() {
        List<String> keids = new ArrayList<String>();
        keids.add(KEID1);
        keids.add(KEID2);
        keids.add(KEID3);
        keids.add(KEID4);
        keids.add(KEID5);        
        KalturaClient client = service.getKalturaClient();
        KalturaMediaEntryFilter filt = new KalturaMediaEntryFilter();
        filt.idIn = KEID1+","+KEID2+","+KEID3+","+KEID4+","+KEID5;
        try {
            KalturaMediaListResponse list =
                client.getMediaService().list(filt);
            assertEquals(5, list.objects.size());
            for (KalturaMediaEntry kme : list.objects) {
                if (! keids.contains(kme.id)) {
                    fail("keid ("+kme.id+") not in the allowed set ("+list+")");
                }
            }
        } catch (KalturaApiException e) {
            fail(e.toString());
        }
    }

    public void testKMEAllPartner() {
        KalturaClient client = service.getKalturaClient();
        KalturaMediaEntryFilter filt = new KalturaMediaEntryFilter();
        filt.partnerIdEqual = service.getKalturaConfig().getPartnerId();
        KalturaFilterPager pager = new KalturaFilterPager();
        pager.pageSize = 100;
        try {
            KalturaMediaListResponse list =
                client.getMediaService().list(filt, pager);
            assertTrue(list.objects.size() > 36);
        } catch (KalturaApiException e) {
            fail(e.toString());
        }
    }

    public void testKMEPartnerIdsIn() {
        List<String> keids = new ArrayList<String>();
        keids.add(KEID1);
        keids.add(KEID2);
        keids.add(KEID3);
        keids.add(KEID4);
        keids.add(KEID5);        
        KalturaClient client = service.getKalturaClient();
        KalturaMediaEntryFilter filt = new KalturaMediaEntryFilter();
        filt.partnerIdEqual = service.getKalturaConfig().getPartnerId();
        filt.idIn = KEID1+","+KEID2+","+KEID3+","+KEID4+","+KEID5;
        try {
            KalturaMediaListResponse list =
                client.getMediaService().list(filt);
            assertEquals(5, list.objects.size());
            for (KalturaMediaEntry kme : list.objects) {
                if (! keids.contains(kme.id)) {
                    fail("keid ("+kme.id+") not in the allowed set ("+list+")");
                }
            }
        } catch (KalturaApiException e) {
            fail(e.toString());
        }
    }

    public void testKMEAllPartnerUser() {
        KalturaClient client = service.getKalturaClient();
        KalturaMediaEntryFilter filt = new KalturaMediaEntryFilter();
        filt.partnerIdEqual = service.getKalturaConfig().getPartnerId();
        filt.userIdEqual = FakeDataPreload.ADMIN_USER_ID;
        KalturaFilterPager pager = new KalturaFilterPager();
        pager.pageSize = 100;
        try {
            KalturaMediaListResponse list =
                client.getMediaService().list(filt, pager);
            assertTrue(list.objects.size() > 25);
        } catch (KalturaApiException e) {
            fail(e.toString());
        }
    }

    public void testKMEPagerOnly() {
        // TEST pager ONLY
        KalturaClient client = service.getKalturaClient();
        // had to comment out? KalturaMediaListResponse list = client.getMediaService().list();
        KalturaMediaEntryFilter filt = new KalturaMediaEntryFilter();
        KalturaFilterPager pager = new KalturaFilterPager();
        // filt.idIn = "0_21g8hviq,0_b1nsskt0";
        pager.pageSize = 35;
        pager.pageIndex=0;
        try {
            KalturaMediaListResponse list =
                client.getMediaService().list(filt, pager);
            assertEquals(35, list.objects.size());
        } catch (KalturaApiException e) {
            fail(e.toString());
        }
    }

    public void testKMEPagerPlusIdsIn() {
        // TEST pager PLUS filter
        List<String> keids = new ArrayList<String>();
        keids.add(KEID1);
        keids.add(KEID2);
        keids.add(KEID3);
        keids.add(KEID4);
        keids.add(KEID5);        
        KalturaClient client = service.getKalturaClient();
        // had to comment out? KalturaMediaListResponse list = client.getMediaService().list();
        KalturaMediaEntryFilter filt = new KalturaMediaEntryFilter();
        KalturaFilterPager pager = new KalturaFilterPager();
        filt.idIn = KEID1+","+KEID2+","+KEID3+","+KEID4+","+KEID5;
        pager.pageSize = 3;
        pager.pageIndex=0;
        try {
            KalturaMediaListResponse list =
                client.getMediaService().list(filt, pager);
            assertEquals(3, list.objects.size());
            for (KalturaMediaEntry kme : list.objects) {
                if (! keids.contains(kme.id)) {
                    fail("keid ("+kme.id+") not in the allowed set ("+list+")");
                }
            }
        } catch (KalturaApiException e) {
            fail(e.toString());
        }
    }

    // SKIPPING TEST
    public void skiptestKMEPagerIdsUser() {
        KalturaClient client = service.getKalturaClient();
        // had to comment out? KalturaMediaListResponse list = client.getMediaService().list();
        KalturaMediaEntryFilter filt = new KalturaMediaEntryFilter();
        KalturaFilterPager pager = new KalturaFilterPager();
        filt.userIdEqual = FakeDataPreload.ADMIN_USER_ID;
        filt.idIn = KEID1+","+KEID2+","+KEID3+","+KEID4+","+KEID5;
        pager.pageSize = 3;
        pager.pageIndex=0;
        try {
            KalturaMediaListResponse list =
                client.getMediaService().list(filt, pager);
            assertEquals(3, list.objects.size());
        } catch (KalturaApiException e) {
            fail(e.toString());
        }
    }

    // SKIPPING TEST
    public void skiptestKMEPagerIdsPartnerUser() {
        KalturaClient client = service.getKalturaClient();
        // had to comment out? KalturaMediaListResponse list = client.getMediaService().list();
        KalturaMediaEntryFilter filt = new KalturaMediaEntryFilter();
        KalturaFilterPager pager = new KalturaFilterPager();
        filt.partnerIdEqual = service.getKalturaConfig().getPartnerId();
        filt.userIdEqual = FakeDataPreload.ADMIN_USER_ID;
        filt.idIn = KEID1+","+KEID2+","+KEID3+","+KEID4+","+KEID5;
        pager.pageSize = 3;
        pager.pageIndex=0;
        try {
            KalturaMediaListResponse list =
                client.getMediaService().list(filt, pager);
            assertEquals(3, list.objects.size());
        } catch (KalturaApiException e) {
            fail(e.toString());
        }
    }

    // SKIPPING TEST
    public void skiptestKMEPagerIdsPartnerUserName() {
        KalturaClient client = service.getKalturaClient();
        // had to comment out? KalturaMediaListResponse list = client.getMediaService().list();
        KalturaMediaEntryFilter filt = new KalturaMediaEntryFilter();
        KalturaFilterPager pager = new KalturaFilterPager();
        filt.partnerIdEqual = service.getKalturaConfig().getPartnerId();
        filt.userIdEqual = FakeDataPreload.ADMIN_USER_ID;
        filt.idIn = KEID1+","+KEID2+","+KEID3+","+KEID4+","+KEID5;
        filt.nameLike = "";
        pager.pageSize = 3;
        pager.pageIndex=0;
        try {
            KalturaMediaListResponse list =
                client.getMediaService().list(filt, pager);
            assertEquals(3, list.objects.size());
        } catch (KalturaApiException e) {
            fail(e.toString());
        }

        //filt.nameLike = "AZ";
        filt.searchTextMatchOr = "AZ";
        try {
            KalturaMediaListResponse list =
                client.getMediaService().list(filt, pager);
            assertEquals(2, list.objects.size());
        } catch (KalturaApiException e) {
            fail(e.toString());
        }
    }

    // SKIPPING TEST
    public void skiptestKMEPopulate() {
        List<KalturaMediaEntry> kmes = null;
        List<String> ids = new ArrayList<String>();

        kmes = service.getKalturaItems(null, null, 0, 0);
        assertNotNull(kmes);
        assertTrue(kmes.size() > 25);

        kmes = service.getKalturaItems(null, new String[] {KEID1, KEID2, KEID3}, 0, 0);
        assertNotNull(kmes);
        assertEquals(3, kmes.size());
        // values come back in a different order than sent unfortunately
        ids.add(kmes.get(0).id);
        ids.add(kmes.get(1).id);
        ids.add(kmes.get(2).id);
        assertTrue( ids.contains(KEID1) );
        assertTrue( ids.contains(KEID2) );
        assertTrue( ids.contains(KEID3) );
        assertFalse( ids.contains(KEID4) );
        assertFalse( ids.contains(KEID5) );

        kmes = service.getKalturaItems(null, new String[] {KEID1, KEID2, KEID3}, 0, 2);
        assertNotNull(kmes);
        assertEquals(2, kmes.size());
    }

    public void testPopulateTypes() {
        List<String> keids = new ArrayList<String>();
        keids.add(KEID5);
        keids.add(KEID6);
        keids.add(KEID7);

        MediaItem video = new MediaItem(FakeDataPreload.LOCATION1_ID, KEID5, FakeDataPreload.ADMIN_USER_ID, true, false, false);
        MediaItem image = new MediaItem(FakeDataPreload.LOCATION1_ID, KEID6, FakeDataPreload.ADMIN_USER_ID, true, false, false);
        MediaItem audio = new MediaItem(FakeDataPreload.LOCATION1_ID, KEID7, FakeDataPreload.ADMIN_USER_ID, true, false, false);

        service.populateMediaItemKalturaData(video);
        service.populateMediaItemKalturaData(audio);
        service.populateMediaItemKalturaData(image);

        assertEquals(KEID5, video.getKalturaId());
        assertEquals(KEID6, image.getKalturaId());
        assertEquals(KEID7, audio.getKalturaId());
        assertEquals("video", video.findType());
        assertEquals("image", image.findType());
        assertEquals("audio", audio.findType());
        assertEquals("video", video.getMediaType());
        assertEquals("image", image.getMediaType());
        assertEquals("audio", audio.getMediaType());
    }

    public void testGetKME() {
        String keid = null;
        KalturaMediaEntry kme = null;

        keid = KEID1;
        kme = service.getKalturaItem(keid);
        assertNotNull(kme);
        assertEquals(kme.id, keid);

        keid = KEID2;
        kme = service.getKalturaItem(keid);
        assertNotNull(kme);
        assertEquals(kme.id, keid);

        kme = service.getKalturaItem(keid);
        assertNotNull(kme);
        assertEquals(kme.id, keid);

        kme = service.getKalturaItem(keid);
        assertNotNull(kme);
        assertEquals(kme.id, keid);
    }

    public void testKMEUpdate() {
        String name = null;
        String keid = null;
        KalturaMediaEntry entry = null;
        KalturaMediaEntry kme = null;

        name = "AZ new test name: "+new Random().nextInt(1000);
        keid = KEID1;
        entry = new KalturaMediaEntry();
        entry.id = keid;
        entry.name = name;
        kme = service.updateKalturaItem(entry);
        assertNotNull(kme);
        assertEquals(kme.id, keid);
        assertEquals(kme.name, name);

        kme = service.getKalturaItem(keid);
        assertNotNull(kme);
        assertEquals(kme.id, keid);
        assertEquals(kme.name, name);

        name = "AZ another test name: "+new Random().nextInt(1000);
        keid = KEID2;
        entry = new KalturaMediaEntry();
        entry.id = keid;
        entry.name = name;
        kme = service.updateKalturaItem(entry);
        assertNotNull(kme);
        assertEquals(kme.id, keid);
        assertEquals(kme.name, name);

        kme = service.getKalturaItem(keid);
        assertNotNull(kme);
        assertEquals(kme.id, keid);
        assertEquals(kme.name, name);
    }

}
