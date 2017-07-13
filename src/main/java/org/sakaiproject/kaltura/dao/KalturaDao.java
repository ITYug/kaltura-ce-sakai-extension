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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.sakaiproject.genericdao.api.GeneralGenericDao;
import org.sakaiproject.kaltura.logic.MediaService.Filter;
import org.sakaiproject.kaltura.logic.MediaService.SaveResults;

/**
 * This is a specialized DAO that allows the developer to extend
 * the functionality of the generic dao package
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 * @deprecated do not use this class for anything other than migration
 */
public interface KalturaDao extends GeneralGenericDao {

    public List<MediaItemDB> getLibraryItems(String userId, String locationId, Filter filter, String[] keids, String sort, int start, int max);

    public void commit();

    /**
     * Mark content (MediaItemDB or MediaCollectionDB) as migrated
     * @param type MediaItemDB or MediaCollectionDB
     * @param ids set of unique ids
     * @return the number of items marked as migrated
     */
    public <T> int migrate(Class<T> type, Set<Long> ids);

    /**
     * Switches collections over to unmigrated status if they contain unmigrated items
     * @return the number of collections marked as unmigrated
     */
    public int migratePartialCollections();

    /**
     * Saves or updates a large set of items in a location in a single transaction,
     * the original items (from hibernate) can and should be passed into this method,
     * it will properly handle reassigning the location and blanking out the id numbers
     * 
     * @param locationId [OPTIONAL] if null or empty then the location in the items/collections are used, 
     *     if set then this overrides any location in the items or collections
     * @param library the items to save in the library for the location
     * @param collections the collections (and items within) to save in the location (this will not remove items)
     * @return the results of the save operation
     * @throws IllegalArgumentException if the locationId is not set AND the item/collection location ids are also not set
     */
    public SaveResults saveInLocation(String locationId, Collection<MediaItemDB> library, Collection<MediaCollectionDB> collections);

}
