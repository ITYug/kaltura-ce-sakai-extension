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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.kaltura.model.MediaCollection;
import org.sakaiproject.kaltura.model.MediaItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Sakai entity which represents a kaltura collection
 * 
 * @author Aaron Zeckoski (azeckoski @ vt.edu)
 */
public class CollectionEntity implements Entity, Serializable {

    private static Log log = LogFactory.getLog(CollectionEntity.class);

    public static final String XML_COLLECTION_KEY = "collection";

    MediaCollection mc;

    public MediaCollection getMediaCollection() {
        return mc;
    }

    public CollectionEntity() {
        // DO NOT USE
    }

    public CollectionEntity(MediaCollection entity) {
        if (log.isDebugEnabled()) log.debug("kaltura CE.construct(entity="+entity+")");
        mc = entity;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entity.api.Entity#getId()
     */
    public String getId() {
        String id = mc.getIdStr();
        if (log.isDebugEnabled()) log.debug("kaltura CE.getId(): "+id);
        return id;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entity.api.Entity#getUrl()
     */
    public String getUrl() {
        String url = ServerConfigurationService.getAccessUrl() + "/kaltura/collection/" + mc.getIdStr();
        if (log.isDebugEnabled()) log.debug("kaltura CE.getUrl(): "+url);
        return url;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entity.api.Entity#getReference()
     */
    public String getReference() {
        String ref = Entity.SEPARATOR + "kaltura" + Entity.SEPARATOR + XML_COLLECTION_KEY + Entity.SEPARATOR + mc.getIdStr();
        if (log.isDebugEnabled()) log.debug("kaltura CE.getReference(): "+ref);
        return ref;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entity.api.Entity#getUrl(java.lang.String)
     */
    public String getUrl(String rootProperty) {
        return getUrl();
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entity.api.Entity#getReference(java.lang.String)
     */
    public String getReference(String rootProperty) {
        return getReference();
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entity.api.Entity#getProperties()
     */
    public ResourceProperties getProperties() {
        if (log.isDebugEnabled()) log.debug("kaltura CE.getProperties()");
        return null;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entity.api.Entity#toXml(org.w3c.dom.Document, java.util.Stack)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Element toXml(Document doc, Stack stack) {
        if (log.isDebugEnabled()) log.debug("kaltura CE.toXml(doc="+doc+", stack="+stack+")");
        Element coll = doc.createElement(XML_COLLECTION_KEY);
        if (stack.isEmpty()) {
            doc.appendChild(coll);
            if (log.isDebugEnabled()) log.debug("kaltura CE.toXml(): current(root)="+doc.getNodeName());
        } else {
            Element current = (Element) stack.peek();
            current.appendChild(coll);
            if (log.isDebugEnabled()) log.debug("kaltura CE.toXml(): current="+current.getNodeName());
        }
        stack.push(coll);

        // add collection attributes
        coll.setAttribute("id", mc.getIdStr());
        coll.setAttribute("locationId", mc.getLocationId());
        coll.setAttribute("ownerId", mc.getOwnerId());
        coll.setAttribute("title", mc.getTitle());
        coll.setAttribute("description", mc.getDescription());
        coll.setAttribute("dateCreated", new DateTime(mc.getDateCreated()).toString());
        coll.setAttribute("dateModified", new DateTime(mc.getDateModified()).toString());
        coll.setAttribute("hidden", mc.isHidden() ? "true" : "false"); // convert to string
        coll.setAttribute("sharing", mc.getSharing());
        if (mc.getItems() != null && !mc.getItems().isEmpty()) {
            if (log.isDebugEnabled()) log.debug("kaltura CE.toXml(): items="+mc.getItems().size());
            /* add items as children 
             * <collection ...>
             *   <item .... />
             *   <item .... />
             * </collection>
             */
            for (MediaItem mediaItem : mc.getItems()) {
                Element item = mediaItem.makeXML(doc);
                coll.appendChild(item);
            }
        } else {
            // no items in this collection
        }
        stack.pop();
        if (log.isDebugEnabled()) log.debug("kaltura CE.toXml(): coll="+coll.getNodeName()+", attribs="+coll.getAttributes());
        return coll;
    }

    /**
     * Create a collection entity from a chunk of XML and params
     * 
     * @param locationId the location for the collection
     * @param ownerId the ownerId (will override the value from the XML if set)
     * @param element the XML Dom element
     */
    public CollectionEntity(String locationId, String ownerId, Element element) {
        if (log.isDebugEnabled()) log.debug("kaltura CE.construct(loc="+locationId+", owner="+ownerId+", element="+element+")");
        this.mc = new MediaCollection(null, locationId, ownerId, "", false, null); // id, location, owner, title
        // ignore the id for now
        if (ownerId == null || "".equals(ownerId)) {
            if (!"".equals(element.getAttribute("ownerId"))) {
                ownerId = element.getAttribute("ownerId");
                this.mc.setOwnerId(ownerId);
            }
        }
        if (!"".equals(element.getAttribute("title"))) {
            this.mc.setTitle(element.getAttribute("title"));
        }
        if (!"".equals(element.getAttribute("description"))) {
            this.mc.setDescription(element.getAttribute("description"));
        }
        if (!"".equals(element.getAttribute("hidden"))) {
            this.mc.setHidden(Boolean.parseBoolean(element.getAttribute("hidden")));
        }
        if (!"".equals(element.getAttribute("sharing"))) {
            this.mc.setSharing(element.getAttribute("sharing"));
        }
        // check for the children items
        if (element.hasChildNodes()) {
            NodeList nodes = element.getChildNodes();
            if (log.isDebugEnabled()) log.debug("kaltura CE.construct: found items="+nodes.getLength());
            List<MediaItem> mediaItems = new ArrayList<MediaItem>(nodes.getLength());
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (MediaItem.XML_ITEM_KEY.equals(node.getNodeName()) && Node.ELEMENT_NODE == node.getNodeType()) {
                    MediaItem mi = new MediaItem(this.mc, ownerId, (Element) node);
                    if (log.isDebugEnabled()) log.debug("kaltura CE.construct: made node into mediaItem="+mi);
                    mediaItems.add(mi);
                } else {
                    log.warn("kaltura CE.construct: skipped node="+node);
                }
            }
            this.mc.setItems(mediaItems);
            if (log.isDebugEnabled()) log.debug("kaltura CE.construct: mediaItems added to collection="+mediaItems.size());
        }
        if (log.isDebugEnabled()) log.debug("kaltura CE.construct: coll="+this.mc);
    }

}
