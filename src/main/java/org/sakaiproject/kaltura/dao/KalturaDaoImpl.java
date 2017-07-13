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
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.sakaiproject.genericdao.hibernate.HibernateGeneralGenericDao;
import org.sakaiproject.kaltura.logic.MediaService.Filter;
import org.sakaiproject.kaltura.logic.MediaService.SaveResults;

/**
 * Implementations of any specialized DAO methods from the specialized DAO that allows the developer
 * to extend the functionality of the generic dao package
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 * @deprecated do not use this class for anything other than migration
 */
public class KalturaDaoImpl extends HibernateGeneralGenericDao implements KalturaDao {

    private static Log log = LogFactory.getLog(KalturaDaoImpl.class);

    public void init() {
        log.debug("init");
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.kaltura.dao.KalturaDao#getLibraryItems(java.lang.String, java.lang.String, org.sakaiproject.kaltura.dao.KalturaDao.Filter, java.lang.String[], java.lang.String, int, int)
     */
    @SuppressWarnings("unchecked")
    public List<MediaItemDB> getLibraryItems(String userId, String locationId, Filter filter,
            String[] keids, String sort, int start, int max) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        String locationQuery = "";
        if (locationId != null) {
            params.put("locationId", locationId);
            locationQuery += " AND locationId = :locationId";
        }
        String extraQuery;
        if (Filter.ALL.equals(filter)) {
            extraQuery = "";
        } else if (Filter.PUBLIC.equals(filter)) {
            extraQuery = " AND hidden = false";
        } else if (Filter.SHARED.equals(filter)) {
            extraQuery = " AND ( (hidden = false AND shared = true) OR ownerId = :userId)";
            params.put("userId", userId);
        } else if (Filter.MINE.equals(filter)) {
            extraQuery = " AND (hidden = false OR ownerId = :userId)";
            params.put("userId", userId);
        /* same as the default so removing this for now, add it back in if the default changes -AZ
        } else if (Filter.OWNED.equals(filter)) {
            extraQuery = " AND ownerId = :userId";
            params.put("userId", userId);
        */
        } else {
            // safe default (only my items)
            extraQuery = " AND ownerId = :userId";
            params.put("userId", userId);
        }
        String orderQuery = " order by dateCreated desc";
        if (sort != null) {
            orderQuery = " order by "+sort;
        }
        String hql = "from MediaItemDB WHERE collection.id IS NULL" + locationQuery + extraQuery + orderQuery;
        
        List<MediaItemDB> l = (List<MediaItemDB>) executeHqlQuery(hql, params, start, max);
        log.info(l.size()+" library items ("+filter+") retrieved for user ("+userId+") in location ("+locationId+") by hql: "+hql);
        return l;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.kaltura.dao.KalturaDao#commit()
     */
    public void commit() {
        this.getSession().flush();
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.kaltura.dao.KalturaDao#saveInLocation(java.lang.String, java.util.Collection, java.util.Collection)
     */
    public SaveResults saveInLocation(String locationId, Collection<MediaItemDB> library, Collection<MediaCollectionDB> collections) {
        throw new UnsupportedOperationException("This DAO method is no longer supported");
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.kaltura.dao.KalturaDao#migrate(java.lang.Class, java.util.Set)
     */
    public <T> int migrate(Class<T> type, Set<Long> ids) {
        int changed = 0;
        if (ids != null && !ids.isEmpty()) {
            String idsCSV = StringUtils.join(ids, ",");
            String hql = "update "+type.getName()+" mi set mi.migrated = true WHERE mi.migrated = false and mi.id in ("+idsCSV+")";
            Query query = getSession().createQuery(hql);
            changed = query.executeUpdate();
        }
        
        return changed;
    }

    public int migratePartialCollections() {
        // SQL: SELECT kc.*, count(ki.id) FROM kaltura_coll kc JOIN kaltura_item ki ON ki.COLLECTION_FK = kc.id AND ki.migrated <> 1 WHERE kc.migrated = 1 GROUP BY kc.id;
        // Update SQL: UPDATE kaltura_coll kc JOIN kaltura_item ki ON ki.COLLECTION_FK = kc.id AND ki.migrated <> 1 SET kc.migrated=0 WHERE kc.migrated = 1;
        // stupid hibernate cannot handle join in update.... bug has been open for... years
        // SQL: UPDATE kaltura_coll kc SET kc.migrated=0 WHERE kc.migrated = 1 and kc.id IN (select ki.COLLECTION_FK FROM kaltura_item ki WHERE ki.COLLECTION_FK = kc.id AND ki.migrated <> 1);
        //String hql = "UPDATE MediaCollection kc JOIN MediaItem ki ON ki.collection.id = kc.id AND ki.migrated != 1 SET kc.migrated = 0 WHERE kc.migrated = 1";
        String hql = "UPDATE MediaCollectionDB kc SET kc.migrated = 0 WHERE kc.migrated = 1 and kc.id in (select ki.collection.id from MediaItemDB ki WHERE ki.collection.id = kc.id AND ki.migrated != 1)";
        Query query = getSession().createQuery(hql);
        return query.executeUpdate();
    }

}
