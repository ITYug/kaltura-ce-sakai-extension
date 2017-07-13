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

import java.lang.reflect.Field;

import org.sakaiproject.kaltura.dao.MediaCollectionDB;
import org.sakaiproject.kaltura.dao.MediaItemDB;
import org.sakaiproject.kaltura.model.MediaCollection;
import org.sakaiproject.genericdao.api.GenericDao;

/**
 * Contains test data for preloading and test constants
 * 
 * @author Sakai App Builder -AZ
 * @deprecated tests no longer valid
 */
public class FakeDataPreload {

    /**
     * current user, access level user in LOCATION_ID1
     */
    public final static String USER_ID = "user-11111111";
    public final static String USER_DISPLAY = "Aaron Zeckoski";
    /**
     * access level user in LOCATION1_ID
     */
    public final static String ACCESS_USER_ID = "access-2222222";
    public final static String ACCESS_USER_DISPLAY = "Regular User";

    public final static String ACCESS_USER_ID_W = "access-write";
    public final static String ACCESS_USER_DISPLAY_W = "Regular User Write";

    public final static String ACCESS_USER_ID_E = "access-editor";
    public final static String ACCESS_USER_DISPLAY_E = "Regular User Editor";

    /**
     * maintain level user in LOCATION1_ID
     */
    public final static String MAINT_USER_ID = "maint-33333333";
    public final static String MAINT_USER_DISPLAY = "Maint User";

    public final static String MAINT_USER_ID_A = "maint-Admin";
    public final static String MAINT_USER_DISPLAY_A = "Maint User Admin";

    public final static String MAINT_USER_ID_M = "maint-Manager";
    public final static String MAINT_USER_DISPLAY_M = "Maint User Manage";

    /**
     * super admin user
     */
    public final static String ADMIN_USER_ID = "admin";
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

    // library items
    public MediaItemDB miL1 = new MediaItemDB(LOCATION1_ID, "kid1", MAINT_USER_ID); // private
    public MediaItemDB miL2 = new MediaItemDB(LOCATION1_ID, "kid2", MAINT_USER_ID, false, false); // public
    public MediaItemDB miL3 = new MediaItemDB(LOCATION1_ID, "kid3", ADMIN_USER_ID, false, false); // public
    public MediaItemDB miL4 = new MediaItemDB(LOCATION1_ID, "kid4", MAINT_USER_ID, false, true); // public
    public MediaItemDB miL5 = new MediaItemDB(LOCATION1_ID, "kid5", MAINT_USER_ID, true, true); // private
    public MediaItemDB miL6 = new MediaItemDB(LOCATION1_ID, "kid5", ACCESS_USER_ID_W, false, false); // public
    public MediaItemDB miL7 = new MediaItemDB(LOCATION1_ID, "kid5", ACCESS_USER_ID_W); // private
    public MediaItemDB miL8 = new MediaItemDB(LOCATION1_ID, "kid3", ACCESS_USER_ID_E, true, false); // private

    // collections
    /** sharing: PRIVATE, owner: MAINT */
    public MediaCollectionDB mcPr = new MediaCollectionDB(LOCATION1_ID, MAINT_USER_ID, "collection Pr", false, MediaCollection.SHARING_PRIVATE);
    /** sharing: ADMIN, owner: MAINT */
    public MediaCollectionDB mcAd = new MediaCollectionDB(LOCATION1_ID, MAINT_USER_ID, "collection Ad", false, MediaCollection.SHARING_ADMIN);
    /** sharing: SHARED, owner: MAINT */
    public MediaCollectionDB mcSh = new MediaCollectionDB(LOCATION1_ID, MAINT_USER_ID, "collection Sh", false, MediaCollection.SHARING_SHARED);
    /** sharing: PUBLIC, owner: MAINT */
    public MediaCollectionDB mcPu = new MediaCollectionDB(LOCATION1_ID, MAINT_USER_ID, "collection Pu", false, MediaCollection.SHARING_PUBLIC);

    // collection items
    public MediaItemDB miPr_L1 = new MediaItemDB(mcPr, miL1); // private
    public MediaItemDB miPr_L2 = new MediaItemDB(mcPr, miL2); // public
    public MediaItemDB miPr_L5 = new MediaItemDB(mcPr, miL5); // private

    public MediaItemDB miAd_L1 = new MediaItemDB(mcAd, miL1); // private
    public MediaItemDB miAd_L2 = new MediaItemDB(mcAd, miL2); // public
    public MediaItemDB miAd_L3 = new MediaItemDB(mcAd, miL3); // public
    public MediaItemDB miAd_L1B = new MediaItemDB(mcAd, miL1); // private
    public MediaItemDB miAd_L2B = new MediaItemDB(mcAd, miL2); // public

    public MediaItemDB miSh_L1 = new MediaItemDB(mcSh, miL1); // private
    public MediaItemDB miSh_L2 = new MediaItemDB(mcSh, miL2); // public

    public MediaItemDB miPu_L1 = new MediaItemDB(mcPu, miL1); // private
    public MediaItemDB miPu_L2 = new MediaItemDB(mcPu, miL2); // public

    /** sharing: SHARED, owner: MAINT, hidden */
    public MediaCollectionDB mcShH = new MediaCollectionDB(LOCATION1_ID, MAINT_USER_ID, "collection Sh Hi", true, MediaCollection.SHARING_ADMIN);
    /** sharing: PRIVATE, owner: MAINT, hidden */
    public MediaCollectionDB mcPrH = new MediaCollectionDB(LOCATION1_ID, MAINT_USER_ID, "collection Pr Hi", true, MediaCollection.SHARING_PRIVATE);

    public MediaItemDB mcShH_L1 = new MediaItemDB(mcShH, miL1); // private
    public MediaItemDB mcShH_L2 = new MediaItemDB(mcShH, miL2); // public

    public MediaItemDB mcPrH_L1 = new MediaItemDB(mcPrH, miL1); // private
    public MediaItemDB mcPrH_L2 = new MediaItemDB(mcPrH, miL2); // public

    /** sharing: PRIVATE, owner: MAINT, EMPTY */
    public MediaCollectionDB mcPrE = new MediaCollectionDB(LOCATION1_ID, MAINT_USER_ID, "collection Pr E", false, MediaCollection.SHARING_PRIVATE);
    /** sharing: ADMIN, owner: MAINT, EMPTY */
    public MediaCollectionDB mcAdE = new MediaCollectionDB(LOCATION1_ID, MAINT_USER_ID, "collection Ad E", false, MediaCollection.SHARING_ADMIN);
    /** sharing: SHARED, owner: MAINT, EMPTY */
    public MediaCollectionDB mcShE = new MediaCollectionDB(LOCATION1_ID, MAINT_USER_ID, "collection Sh E", false, MediaCollection.SHARING_SHARED);
    /** sharing: PUBLIC, owner: MAINT, EMPTY */
    public MediaCollectionDB mcPuE = new MediaCollectionDB(LOCATION1_ID, MAINT_USER_ID, "collection Pu E", false, MediaCollection.SHARING_PUBLIC);

    /** sharing: PRIVATE, owner: MAINT, private only */
    public MediaCollectionDB mcPrNoPub = new MediaCollectionDB(LOCATION1_ID, MAINT_USER_ID, "collection Pr NoPub", false, MediaCollection.SHARING_PRIVATE);
    /** sharing: ADMIN, owner: MAINT, private only */
    public MediaCollectionDB mcAdNoPub = new MediaCollectionDB(LOCATION1_ID, MAINT_USER_ID, "collection Ad NoPub", false, MediaCollection.SHARING_ADMIN);
    /** sharing: SHARED, owner: MAINT, private only */
    public MediaCollectionDB mcShNoPub = new MediaCollectionDB(LOCATION1_ID, MAINT_USER_ID, "collection Sh NoPub", false, MediaCollection.SHARING_SHARED);
    /** sharing: PUBLIC, owner: MAINT, private only */
    public MediaCollectionDB mcPuNoPub = new MediaCollectionDB(LOCATION1_ID, MAINT_USER_ID, "collection Pu NoPub", false, MediaCollection.SHARING_PUBLIC);

    public MediaItemDB mcPrNoPub_L1 = new MediaItemDB(mcPrNoPub, miL1); // private
    public MediaItemDB mcAdNoPub_L1 = new MediaItemDB(mcAdNoPub, miL1); // private
    public MediaItemDB mcShNoPub_L1 = new MediaItemDB(mcShNoPub, miL1); // private
    public MediaItemDB mcPuNoPub_L1 = new MediaItemDB(mcPuNoPub, miL1); // private


    public GenericDao dao;

    public void setDao(GenericDao dao) {
        this.dao = dao;
    }

    public void init() {
        preloadTestData();
    }

    /**
     * Preload a bunch of test data into the database
     */
    public void preloadTestData() {
        /*
         * This iterates over the fields in FakeDataPreload and finds all the ones which are of the
         * type SampleItem, then it runs the dao save method on them: dao.findById(SampleItem.class,
         * item1.getId()); This does the same thing as writing a bunch of these: dao.save(item1);
         */
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(MediaCollectionDB.class)) {
                try {
                    dao.save((MediaCollectionDB) field.get(this));
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
            if (field.getType().equals(MediaItemDB.class)) {
                try {
                    dao.save((MediaItemDB) field.get(this));
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Reload the test data back into the current session so they can be tested correctly, if this
     * is not done then the preloaded data is in a separate session and equality tests will not work
     */
    public void reloadTestData() {
        /*
         * This iterates over the fields in FakeDataPreload and finds all the ones which are of the
         * type SampleItem, then it sets the field equal to the method:
         * dao.findById(SampleItem.class, item1.getId()); This does the same thing as writing a
         * bunch of these: item1 = (SampleItem) dao.findById(SampleItem.class, item1.getId());
         */
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(MediaCollectionDB.class)) {
                try {
                    field.set(this, dao.findById(MediaCollectionDB.class, ((MediaCollectionDB) field.get(this)).getId()));
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
            if (field.getType().equals(MediaItemDB.class)) {
                try {
                    field.set(this, dao.findById(MediaItemDB.class, ((MediaItemDB) field.get(this)).getId()));
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
    }

}
