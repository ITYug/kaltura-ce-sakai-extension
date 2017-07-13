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
package org.sakaiproject.kaltura.logic.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.kaltura.logic.ExternalLogic;
import org.sakaiproject.kaltura.logic.KalturaAPIService;
import org.sakaiproject.kaltura.logic.MediaService;
import org.sakaiproject.kaltura.logic.MediaService.Filter;
import org.sakaiproject.kaltura.logic.MediaService.SaveResults;
import org.sakaiproject.kaltura.model.MediaCollection;
import org.sakaiproject.kaltura.model.MediaItem;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Handles the integration with the old sakai entity system
 * 
 * @author Aaron Zeckoski (azeckoski @ vt.edu)
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class KalturaEntityProducer implements EntityProducer, EntityTransferrer, ApplicationContextAware {

    private static Log log = LogFactory.getLog(KalturaEntityProducer.class);
    /**
     * The XML root of the archive MUST match the actual name of the entity producer service
     * for this content type. It will be fetched using: (EntityProducer) ComponentManager.get({xml_root_element});
     * NOTE: xml_root_element will actually be the element inside the archive element
     */
    public static final String XML_ROOT = KalturaEntityProducer.class.getName(); //"kaltura";
    public static final String XML_LIBRARY_ROOT = "library";
    public static final String XML_COLLECTIONS_ROOT = "collections";

    public static final String KALTURA = "kaltura";
    public static final String REFERENCE_ROOT = Entity.SEPARATOR + KALTURA;
    public static final String REFERENCE_SUB_TYPE = REFERENCE_ROOT + Entity.SEPARATOR + "collection";

    private EntityManager entityManager;
    public void setEntityManager(EntityManager em) {
        entityManager = em;
    }

    private MediaService service;
    public void setService(MediaService mediaService) {
        this.service = mediaService;
    }

    private ExternalLogic external;
    public void setExternal(ExternalLogic external) {
        this.external = external;
    }

    ApplicationContext applicationContext;
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void init() {
        if (log.isDebugEnabled()) log.debug("kaltura EP.init()");
        try {
            entityManager.registerEntityProducer(this, REFERENCE_ROOT);
            log.info("Registered kaltura entity producer as: "+ REFERENCE_ROOT);

            // AZ - now we need to do some serious spring gymnastics to get our service into the main Sakai AC
            // get the main sakai AC (it will be the parent of our AC)
            ApplicationContext sakaiAC = applicationContext.getParent();
            if (sakaiAC != null && sakaiAC instanceof ConfigurableApplicationContext) {
                // only ConfigurableApplicationContext - or higher - can register singletons
                Object currentKEP = ComponentManager.get(KalturaEntityProducer.class.getName());
                // check if something is already registered
                if (currentKEP != null) {
                    log.info("Found existing "+KalturaEntityProducer.class.getName()+" in the ComponentManager: "+currentKEP);
                    // attempt to unregister the existing bean (otherwise the register call will fail)
                    try {
                        // only DefaultListableBeanFactory - or higher - can unregister singletons
                        DefaultListableBeanFactory dlbf = (DefaultListableBeanFactory) sakaiAC.getAutowireCapableBeanFactory();
                        dlbf.destroySingleton(KalturaEntityProducer.class.getName());
                        log.info("Removed existing "+KalturaEntityProducer.class.getName()+" from the ComponentManager");
                    } catch (Exception e) {
                        log.warn("FAILED attempted removal of kaltura bean: "+e);
                    }
                }
                // register this EP with the sakai AC
                ((ConfigurableApplicationContext)sakaiAC).getBeanFactory().registerSingleton(KalturaEntityProducer.class.getName(), this);
            }
            // now verify if we are good to go
            if (ComponentManager.get(KalturaEntityProducer.class.getName()) != null) {
                log.info("Found "+KalturaEntityProducer.class.getName()+" in the ComponentManager");
            } else {
                log.warn("FAILED to insert and lookup "+KalturaEntityProducer.class.getName()+" in the Sakai ComponentManager, archive imports for kaltura will not work");
            }
        } catch (Exception ex) {
            log.warn("kaltura EP.init(): "+ex, ex);
        }
    }

    /*
     * EntityProducer Methods
     */

    /* (non-Javadoc)
     * @see org.sakaiproject.entity.api.EntityProducer#getLabel()
     */
    public String getLabel() {
        return KALTURA; // this will define the filename used during archive (among other things)
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entity.api.EntityProducer#willArchiveMerge()
     */
    public boolean willArchiveMerge() {
        /*
         * we only do the merge IF all of this is true:
         * 1) archive enabled
         * 2) user has permission
         */
        boolean merge = false;
        String currentUserId = external.getCurrentUserId();
        String locationId = external.getCurrentLocationId();
        if (!service.isSiteArchiveSupport()) {
            log.info("kaltura tool site archive support is not enabled so no content will be archived from site ("+locationId+"), use 'kaltura.archive.support.enabled = true' to enable");
        } else {
            merge = true; // assume admin merge if no location or user
            if (currentUserId != null && !ExternalLogic.NO_LOCATION.equals(locationId)) {
                // check permissions for this user (if there is one, otherwise assume some kind of admin job)
                if (service.canArchive(locationId, currentUserId)) {
                    log.info("kaltura tool site willArchiveMerge: user ("+currentUserId+") can merge in ("+locationId+")");
                } else {
                    log.info("kaltura tool site willArchiveMerge: user ("+currentUserId+") cannot  merge in ("+locationId+")");
                    merge = false;
                }
            }
        }
        // NOTE: we could also check for items if we wanted and the location is available
        return merge;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entity.api.EntityProducer#archive(java.lang.String, org.w3c.dom.Document, java.util.Stack, java.lang.String, java.util.List)
     */
    public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments) {
        if (log.isDebugEnabled()) log.debug("kaltura EP.archive(siteId="+siteId+", doc="+doc+", stack="+stack+", archivePath="+archivePath+", attachments="+attachments+")");
        // archiving all kaltura data in a site
        String locationId = "/site/"+siteId;

        String result = "kaltura: no archive created for "+locationId+"\n";
        if (!service.isSiteArchiveSupport()) {
            log.info("kaltura tool site archive support is not enabled so no content will be archived from site ("+locationId+"), use 'kaltura.archive.support.enabled = true' to enable");
            result = "no kaltura data archived for location "+ locationId + ", archiving disabled for this data\n";
        } else {
            String currentUserId = external.getCurrentUserId();
            boolean allowed = true;
            if (currentUserId != null) {
                // check permissions for this user (if there is one, otherwise assume some kind of admin job)
                if (!service.canArchive(locationId, currentUserId)) {
                    allowed = false;
                    String msg = "user ("+currentUserId+") not allowed to archive ("+locationId+")";
                    log.warn(msg);
                    result = "kaltura tool site archive: "+msg+"\n";
                } else {
                    log.info("kaltura tool site archive: user ("+currentUserId+") can archive ("+locationId+")");
                }
            }

            if (allowed) {
                int libraryCount = 0;
                int collectionsCount = 0;
                int itemsCount = 0;
                int libraryErrors = 0;
                int collectionErrors = 0;
                /*
                    <kaltura locationId="origin">
                      <library locationId="origin" count="1">
                        <item .... />
                      </library>
                      <collections count="1">
                        <collection ... >
                          <item .... />
                        </collection>
                      </collection>
                    </kaltura> 
                 */
                // start with an element with our own name
                Element rootElement = doc.createElement(XML_ROOT);
                ((Element) stack.peek()).appendChild(rootElement);
                stack.push(rootElement);
                rootElement.setAttribute("locationId", locationId);

                Element libraryElement = doc.createElement(XML_LIBRARY_ROOT);
                libraryElement.setAttribute("locationId", locationId);
                rootElement.appendChild(libraryElement);
                // get all library items
                List<MediaItem> libraryItems = service.getLibrary(locationId, Filter.ALL, null, "id", 0, 0);
                libraryElement.setAttribute("count", libraryItems.size()+"");
                if (!libraryItems.isEmpty()) {
                    for (MediaItem mediaItem : libraryItems) {
                        try {
                            Element itemElement = mediaItem.makeXML(doc);
                            libraryElement.appendChild(itemElement);
                            libraryCount++;
                        } catch (Exception e) {
                            log.warn("Failure while archiving library item: "+mediaItem+" :: "+e, e);
                            libraryErrors++;
                        }
                    }
                }

                Element collectionsElement = doc.createElement(XML_COLLECTIONS_ROOT);
                rootElement.appendChild(collectionsElement);

                // get all the collections and items for this site
                List<MediaCollection> collections = service.getCollections(locationId, null, -1, null, 0, 0);
                collectionsElement.setAttribute("count", collections.size()+"");
                for (MediaCollection mediaCollection : collections) {
                    try {
                        // archive this collection
                        CollectionEntity collection = new CollectionEntity(mediaCollection);
                        Element collectionElement = collection.toXml(doc, stack);
                        collectionsElement.appendChild(collectionElement);
                        collectionsCount++;
                        itemsCount += ( mediaCollection.getItems() == null ? 0 : mediaCollection.getItems().size() );
                    } catch (Exception e) {
                        log.warn("Failure while archiving collection: "+mediaCollection+" :: "+e, e);
                        collectionErrors++;
                    }
                }
                stack.pop();
                String msg = "archiving kaltura content ("+libraryCount+" library items ["+libraryErrors+" errors], "+collectionsCount+" collections ["+collectionErrors+" errors] with "+itemsCount+" items) in site: " + siteId;
                log.info(msg);
                result = msg + ".\n";
            }
        }

        if (log.isDebugEnabled()) log.debug("archive: "+result);
        return result;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entity.api.EntityProducer#merge(java.lang.String, org.w3c.dom.Element, java.lang.String, java.lang.String, java.util.Map, java.util.Map, java.util.Set)
     */
    public String merge(String siteId, Element root, String archivePath, String fromSiteId,
            Map attachmentNames, Map userIdTrans, Set userListAllowImport) {
        if (log.isDebugEnabled()) log.debug("kaltura EP.merge(siteId="+siteId+", fromSiteId="+fromSiteId+", archivePath="+archivePath+", userIdTrans="+userIdTrans+", userListAllowImport="+userListAllowImport+")");
        StringBuilder results = new StringBuilder();
        String locationId = "/site/"+siteId;

        if (!service.isSiteArchiveSupport()) {
            log.info("kaltura tool site archive support is not enabled so no content will be merged from archive for site ("+locationId+"), use 'kaltura.archive.support.enabled = true' to enable");
            results.append("no kaltura data merged from archive for location "+ locationId + ", archiving support disabled\n");
        } else {
            // this method is executed when an archive is imported again
            String defaultOwnerId = KalturaAPIService.DEFAULT_OWNER_ID; // the super admin
            String currentUserId = external.getCurrentUserId();
            boolean allowed = true;
            if (currentUserId != null) {
                // check permissions for this user (if there is one, otherwise assume some kind of admin job)
                if (service.canArchive(locationId, currentUserId)) {
                    defaultOwnerId = currentUserId;
                } else {
                    allowed = false;
                    String msg = "user ("+currentUserId+") not allowed to do merge in ("+locationId+")";
                    log.warn(msg);
                    results.append("kaltura tool site merge: "+msg+"\n");
                }
            }

            if (allowed) {
                log.info("kaltura EP.merge: archive ("+archivePath+") from site ("+fromSiteId+") to site ("+siteId+"): xml="+root);
                results.append("Kaltura merging library items and collections from site "+fromSiteId+" to site "+siteId+": \n");

                Element kalturaElement = root;
                if (!XML_ROOT.equals(root.getNodeName()) || root.getNodeType() != Node.ELEMENT_NODE) {
                    NodeList kalturaElements = root.getElementsByTagName(XML_ROOT);
                    if (kalturaElements != null && kalturaElements.getLength() > 0) {
                        kalturaElement = (Element) kalturaElements.item(0);
                    } else {
                        log.warn("Unable to find the kaltura root node by name ("+XML_ROOT+"), will attempt anyway with this as the root: "+root);
                        results.append(" (XML "+XML_ROOT+" node could not be located): ");
                    }
                }

                /*
                    <kaltura locationId="origin">
                      <library locationId="origin" count="1">
                        <item .... />
                      </library>
                      <collections count="1">
                        <collection ... >
                          <item .... />
                        </collection>
                      </collection>
                    </kaltura> 
                 */
                try {
                    // first we get the library items
                    HashSet<MediaItem> libraryItems = new HashSet<MediaItem>();
                    NodeList libraryElements = kalturaElement.getElementsByTagName(XML_LIBRARY_ROOT);
                    if (libraryElements != null && libraryElements.getLength() > 0) {
                        if (log.isDebugEnabled()) log.debug("kaltura EP.merge: found "+libraryElements.getLength()+" libraryElements");
                        String ownerId = defaultOwnerId;
                        // importing library items - we only care about the first one if there are multiples
                        Element libraryElement = (Element) libraryElements.item(0);
                        NodeList libraryItemNodes = libraryElement.getChildNodes();
                        if (libraryItemNodes != null && libraryItemNodes.getLength() > 0) {
                            if (log.isDebugEnabled()) log.debug("kaltura EP.merge: found "+libraryItemNodes.getLength()+" libraryItemNodes");
                            results.append("Library items ("+libraryItemNodes.getLength()+"): ");
                            for (int j = 0; j < libraryItemNodes.getLength(); j++) {
                                Node itemNode = libraryItemNodes.item(j);
                                if (itemNode.getNodeType() != Node.ELEMENT_NODE) continue;
                                MediaItem mi = new MediaItem(null, ownerId, (Element) itemNode);
                                mi.setLocationId(locationId);
                                if (log.isDebugEnabled()) log.debug("kaltura EP.merge: made library item from xml: "+mi);
                                if (j > 0) {
                                    results.append(", \n  ");
                                }
                                results.append(mi);
                                libraryItems.add(mi);
                            }
                            results.append("\n :library: \n");
                        }
                    }
                    if (log.isDebugEnabled()) log.debug("kaltura EP.merge: found "+libraryItems.size()+" library items: "+libraryItems);
                    // next we get the collections and items in them
                    HashSet<MediaCollection> collections = new HashSet<MediaCollection>();
                    NodeList collectionsElements = kalturaElement.getElementsByTagName(XML_COLLECTIONS_ROOT);
                    if (collectionsElements != null && collectionsElements.getLength() > 0) {
                        // importing collections - we only care about the first one if there are multiples
                        Element collectionsElement = (Element) collectionsElements.item(0);
                        NodeList collectionNodes = collectionsElement.getChildNodes();
                        if (collectionNodes != null && collectionNodes.getLength() > 0) {
                            results.append("\nCollections ("+collectionNodes.getLength()+"): ");
                            for (int j = 0; j < collectionNodes.getLength(); j++) {
                                Node collectionNode = collectionNodes.item(j);
                                if (collectionNode.getNodeType() != Node.ELEMENT_NODE) continue;
                                CollectionEntity collectionEntity = new CollectionEntity(locationId, defaultOwnerId, (Element) collectionNode);
                                MediaCollection mc = collectionEntity.getMediaCollection();
                                if (log.isDebugEnabled()) log.debug("kaltura EP.merge: made collection from xml: "+mc);
                                if (j > 0) {
                                    results.append(", \n  ");
                                }
                                results.append(mc);
                                collections.add(mc);
                            }
                            results.append("\n :collections: \n");
                        }
                    }
                    if (log.isDebugEnabled()) log.debug("kaltura EP.merge: found "+collections.size()+" collections: "+collections);

                    // now we need to save all the items
                    SaveResults sr = service.saveInLocation(locationId, libraryItems, collections);
                    if (log.isDebugEnabled()) log.debug("kaltura EP.merge: saved library and collections in location ("+locationId+"): "+sr);
                    results.append("Saved "+sr.itemsCount+" library items and "+sr.collectionsCount+" collections in location: "+sr.locationId);
                } catch (Exception e) {
                    // any exception is caught so we can properly log it and put info in the results
                    log.error("Failure while merging archive data into "+locationId+": "+e, e);
                    results.append("Exception (merge failure): "+e);
                }
            }
        }

        String result = results.toString();
        log.info("kaltura EP.merge result: "+result);
        return result;
    }

    public String[] myToolIds() {
        return new String[] {"sakai.kaltura"};
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entity.api.EntityTransferrer#transferCopyEntities(java.lang.String, java.lang.String, java.util.List)
     */
    public void transferCopyEntities(String fromContext, String toContext, List ids) {
        if (log.isDebugEnabled()) log.debug("kaltura EP.transferCopyEntities(fromContext="+fromContext+", toContext="+toContext+", ids="+ids+")");
        transferCopyEntities(fromContext, toContext, ids, false);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entity.api.EntityTransferrer#transferCopyEntities(java.lang.String, java.lang.String, java.util.List, boolean)
     */
    public void transferCopyEntities(String fromContext, String toContext, List ids, boolean cleanup) {
        if (log.isDebugEnabled()) log.debug("kaltura EP.transferCopyEntities(fromContext="+fromContext+", toContext="+toContext+", ids="+ids+", cleanup="+cleanup+")");
        /* called when duplicating a site: 
         * fromContext = site id of the site we are copying from
         * toContext = site id of the site we are copying to
         * ids = could be a set of ids to copy, however, this would require custom changes to site-manage so it will be null or empty
         * cleanup = if true, coming from importToolIntoSiteMigrate() - this indicates that existing data should be removed
         *         if false, coming from importToolIntoSite() - this indicates that existing data should not be removed
         *         and instead the data should be merged
         */
        service.migrateLocationData("/site/"+fromContext, "/site/"+toContext, cleanup);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entity.api.EntityProducer#parseEntityReference(java.lang.String, org.sakaiproject.entity.api.Reference)
     */
    public boolean parseEntityReference(String reference, Reference ref) {
        if (reference.startsWith(REFERENCE_ROOT)) {
            if (log.isDebugEnabled()) log.debug("kaltura EP.parseEntityReference(reference="+reference+", ref="+ref+")");
            String[] parts = StringUtils.split(reference, Entity.SEPARATOR);

            String subType = REFERENCE_SUB_TYPE;
            String context = null;
            String id = null;
            String container = "";
            if (parts.length > 1) {
                String type = parts[1];
                if ("collection".equals(type) && parts.length > 2) {
                    // assume /kaltura/collection/collectionid
                    id = parts[2];
                    MediaCollection mc = service.getCollection(id, 0, 0);
                    if (mc != null) {
                        context = mc.getLocationId();
                    }
                    ref.set("kaltura", subType, id, container, context);
                    if (log.isDebugEnabled()) log.debug("kaltura EP.parseEntityReference: TRUE context="+context+", ref="+ref);
                    return true;
                }
                // handle other types?
            }
            if (log.isDebugEnabled()) log.debug("kaltura EP.parseEntityReference: FALSE");
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entity.api.EntityProducer#getEntity(org.sakaiproject.entity.api.Reference)
     */
    public Entity getEntity(Reference ref) {
        if (log.isDebugEnabled()) log.debug("kaltura EP.getEntity(ref="+ref+")");
        Entity rv = null;
        if (REFERENCE_SUB_TYPE.equals(ref.getSubType())) {
            MediaCollection mc = service.getCollection(ref.getId(), 0, 0);
            rv = new CollectionEntity(mc);
        } else {
            // handle other types?
        }
        if (log.isDebugEnabled()) log.debug("kaltura EP.getEntity: "+rv);
        return rv;
    }

    public String getEntityUrl(Reference ref) {
        if (log.isDebugEnabled()) log.debug("kaltura EP.getEntityUrl(ref="+ref+")");
        // not needed
        return null;
    }

    public String getEntityDescription(Reference ref) {
        if (log.isDebugEnabled()) log.debug("kaltura EP.getEntityDescription(ref="+ref+")");
        // not needed
        return null;
    }

    public ResourceProperties getEntityResourceProperties(Reference ref) {
        if (log.isDebugEnabled()) log.debug("kaltura EP.getEntityResourceProperties(ref="+ref+")");
        // not needed
        return null;
    }

    public Collection getEntityAuthzGroups(Reference ref, String arg1) {
        if (log.isDebugEnabled()) log.debug("kaltura EP.getEntityAuthzGroups(ref="+ref+", arg1="+arg1+")");
        // not needed
        return null;
    }

    public HttpAccess getHttpAccess() {
        if (log.isDebugEnabled()) log.debug("kaltura EP.getHttpAccess()");
        // not needed
        return null;
    }

}
