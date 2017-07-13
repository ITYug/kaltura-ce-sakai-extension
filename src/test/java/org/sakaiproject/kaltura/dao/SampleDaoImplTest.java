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

import java.util.List;

import org.sakaiproject.kaltura.dao.KalturaDao;
import org.sakaiproject.kaltura.logic.FakeDataPreload;
import org.sakaiproject.kaltura.logic.MediaService.Filter;
import org.springframework.test.AbstractTransactionalSpringContextTests;

/**
 * Testing for the specialized DAO methods (do not test the Generic Dao methods)
 * @author Sakai App Builder -AZ
 * @deprecated tests no longer valid
 */
public class SampleDaoImplTest extends AbstractTransactionalSpringContextTests {

    protected KalturaDao dao;
    private FakeDataPreload tdp;

    private MediaCollectionDB item;

    private final static String ITEM_TITLE = "New Title";
    private final static String ITEM_OWNER = "11111111";
    private final static String ITEM_SITE = "22222222";
    private final static Boolean ITEM_HIDDEN = false;


    protected String[] getConfigLocations() {
        // point to the needed spring config files, must be on the classpath
        // (add component/src/webapp/WEB-INF to the build path in Eclipse),
        // they also need to be referenced in the project.xml file
        return new String[] {"hibernate-test.xml", "spring-hibernate.xml"};
    }

    // run this before each test starts
    protected void onSetUpBeforeTransaction() throws Exception {
        // create test objects
        item = new MediaCollectionDB(ITEM_SITE, ITEM_OWNER, ITEM_TITLE, ITEM_HIDDEN, null);
    }

    // run this before each test starts and as part of the transaction
    protected void onSetUpInTransaction() {
        // load the spring created dao class bean from the Spring Application Context
        dao = (KalturaDao) applicationContext.getBean("org.sakaiproject.kaltura.dao.KalturaDao");
        if (dao == null) {
            throw new NullPointerException("DAO could not be retrieved from spring context");
        }

        // load up the test data preloader from spring
        tdp = (FakeDataPreload) applicationContext.getBean("org.sakaiproject.kaltura.logic.test.FakeDataPreload");
        if (tdp == null) {
            throw new NullPointerException("FakeDataPreload could not be retrieved from spring context");
        }

        // preload data if desired
        dao.save(item);
    }


    /**
     * ADD unit tests below here, use testMethod as the name of the unit test,
     * Note that if a method is overloaded you should include the arguments in the
     * test name like so: testMethodClassInt (for method(Class, int);
     */

    public void testGetLibraryItems() {
        List<MediaItemDB> items = null;

        items = dao.getLibraryItems(FakeDataPreload.MAINT_USER_ID, FakeDataPreload.LOCATION1_ID, Filter.ALL, null, null, 0, 0);
        assertNotNull(items);
        assertEquals(8, items.size());

        items = dao.getLibraryItems(FakeDataPreload.MAINT_USER_ID, FakeDataPreload.LOCATION1_ID, Filter.ALL, null, null, 0, 3);
        assertNotNull(items);
        assertEquals(3, items.size());

        items = dao.getLibraryItems(FakeDataPreload.MAINT_USER_ID, FakeDataPreload.LOCATION1_ID, Filter.ALL, null, null, 2, 2);
        assertNotNull(items);
        assertEquals(2, items.size());

        items = dao.getLibraryItems(FakeDataPreload.MAINT_USER_ID, FakeDataPreload.LOCATION1_ID, Filter.PUBLIC, null, null, 0, 0);
        assertNotNull(items);
        assertEquals(4, items.size());

        items = dao.getLibraryItems(FakeDataPreload.MAINT_USER_ID, FakeDataPreload.LOCATION1_ID, Filter.SHARED, null, null, 0, 0);
        assertNotNull(items);
        assertEquals(4, items.size());

        items = dao.getLibraryItems(FakeDataPreload.MAINT_USER_ID, FakeDataPreload.LOCATION1_ID, Filter.MINE, null, null, 0, 0);
        assertNotNull(items);
        assertEquals(6, items.size());

        items = dao.getLibraryItems(FakeDataPreload.MAINT_USER_ID, FakeDataPreload.LOCATION1_ID, Filter.OWNED, null, null, 0, 0);
        assertNotNull(items);
        assertEquals(4, items.size());
    }


    // THESE ARE SAMPLE UNIT TESTS WHICH SHOULD BE REMOVED LATER -AZ
    /**
     * TODO - Remove this sample unit test
     * Test method for {@link org.sakaiproject.kaltura.dao.impl.GenericHibernateDao#save(java.lang.Object)}.
     */
    public void testSave() {
        assertEquals(15, dao.countAll(MediaCollectionDB.class));
        MediaCollectionDB item1 = new MediaCollectionDB(ITEM_SITE, ITEM_OWNER, "new 1", ITEM_HIDDEN, null);
        dao.save(item1);
        Long itemId = item1.getId();
        assertNotNull(itemId);
        assertEquals(16, dao.countAll(MediaCollectionDB.class));
    }

    /**
     * TODO - Remove this sample unit test
     * Test method for {@link org.sakaiproject.kaltura.dao.impl.GenericHibernateDao#delete(java.lang.Object)}.
     */
    public void testDelete() {
        assertEquals(dao.countAll(MediaCollectionDB.class), 15);
        dao.delete(item);
        assertEquals(dao.countAll(MediaCollectionDB.class), 14);
    }

    /**
     * Add anything that supports the unit tests below here
     */
}
