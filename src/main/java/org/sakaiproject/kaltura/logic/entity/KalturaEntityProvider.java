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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestInterceptor;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestStorable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.kaltura.logic.ExternalLogic;
import org.sakaiproject.kaltura.logic.KalturaAPIService;
import org.sakaiproject.kaltura.logic.KalturaAPIService.Widget;
import org.sakaiproject.kaltura.logic.MediaService;
import org.sakaiproject.kaltura.logic.MediaService.Filter;
import org.sakaiproject.kaltura.logic.User;
import org.sakaiproject.kaltura.model.MediaCollection;
import org.sakaiproject.kaltura.model.MediaItem;
import org.sakaiproject.kaltura.model.MyMediaCollectionItem;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;

import com.kaltura.client.KalturaClient;
import com.kaltura.client.enums.KalturaMediaType;
import com.kaltura.client.types.KalturaMediaEntry;

/**
 * This provides and defines the various REST endpoints
 *  
 * NOTE: locationId is the site reference (/site/12345....)
 * 
 * @author Aaron Zeckoski (azeckoski @ vt.edu)
 */
public class KalturaEntityProvider extends AbstractEntityProvider implements RESTful, RequestStorable, RequestInterceptor {

    //private static Log log = LogFactory.getLog(KalturaEntityProvider.class);

    public static String PREFIX = "kaltura";
    public String getEntityPrefix() {
        return PREFIX;
    }

    // INTERCEPTORS

    public void before(EntityView view, HttpServletRequest req, HttpServletResponse res) {
        // clear out any existing Kaltura session in this thread
        kalturaAPIService.clearKalturaClient();
    }
    public void after(EntityView view, HttpServletRequest req, HttpServletResponse res) {
        // This space intentionally left blank
    }

    // ACTIONS

    @EntityCustomAction(action = "version", viewKey = EntityView.VIEW_SHOW)
    public String getVersion() {
        // GET /kaltura/version
        return MediaService.APP_VERSION;
    }

    /**
     * Return Kaltura session and configuration information required by Kaltura javascript widgets.
     */
    @EntityCustomAction(action="config", viewKey=EntityView.VIEW_LIST)
    public Object getKalturaSessionInfo(EntityView view, Map<String, Object> params) {
        // GET /kaltura/config
        Map<String, Object> info = new HashMap<String, Object>();

        info.put("appVersion", MediaService.APP_VERSION);

        // get a new Kaltura client and add the session ID to the map
        KalturaClient client = kalturaAPIService.getKalturaClient(null);
        info.put("kalturaSessionId", client.getSessionId());

        // add the username of the current user
        User u = external.getCurrentUser();
        info.put("username", u != null ? u.getUsername() : "");
        info.put("userDisplayName", u != null ? u.getName() : "");

        // add in the kaltura widget ids
        info.put("widgetPlayerVideoId", kalturaAPIService.getKalturaWidgetId(Widget.PLAYER_VIDEO));
        info.put("widgetPlayerCaptureId", kalturaAPIService.getKalturaWidgetId(Widget.PLAYER_EDIT));
        info.put("widgetPlayerImageId", kalturaAPIService.getKalturaWidgetId(Widget.PLAYER_IMAGE));
        info.put("widgetPlayerAudioId", kalturaAPIService.getKalturaWidgetId(Widget.PLAYER_AUDIO));
        info.put("widgetUploaderId", kalturaAPIService.getKalturaWidgetId(Widget.UPLOADER));
        info.put("widgetEditorId", kalturaAPIService.getKalturaWidgetId(Widget.EDITOR));
        info.put("widgetClipperId", kalturaAPIService.getKalturaWidgetId(Widget.CLIPPER));

        // add in the kaltura player sizes
        info.put("widgetPlayerVideoWidth", kalturaAPIService.getKalturaWidgetWidth(Widget.PLAYER_VIDEO));
        info.put("widgetPlayerVideoHeight", kalturaAPIService.getKalturaWidgetHeight(Widget.PLAYER_VIDEO));
        info.put("widgetPlayerAudioWidth", kalturaAPIService.getKalturaWidgetWidth(Widget.PLAYER_AUDIO));
        info.put("widgetPlayerAudioHeight", kalturaAPIService.getKalturaWidgetHeight(Widget.PLAYER_AUDIO));
        info.put("widgetPlayerImageWidth", kalturaAPIService.getKalturaWidgetWidth(Widget.PLAYER_IMAGE));
        info.put("widgetPlayerImageHeight", kalturaAPIService.getKalturaWidgetHeight(Widget.PLAYER_IMAGE));

        /*
         * Add Kaltura information required by javascript widgets to a new configuration parameter
         * map. We do this instead of adding the configuration object itself to the object map to
         * prevent exposing values like the admin secret to end users.
         */
        Map<String, String> configMap = new HashMap<String, String>();
        configMap.put("partnerId", service.getKalturaPartnerId());
        configMap.put("endpoint", service.getKalturaEndpoint());
        configMap.put("timeout", service.getKalturaTimeout());
        // adding in the kaltura widgets
        info.put("kalturaConfiguration", configMap);

        return info;
    }

    @EntityCustomAction(action="perms", viewKey=EntityView.VIEW_LIST)
    public Object getKalturaUserPerms(EntityView view, Search search) {
        // GET /kaltura/perms/site/{siteId}
        // GET /kaltura/perms/tool/{tooldId}
        Map<String, Object> info = new HashMap<String, Object>();

        String userId = external.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("cannot get perms for a user when there is not a current user session");
        }
        if (search != null) {
            // get the user from the URL
            Restriction userR = search.getRestrictionByProperties(new String[] {"user","userId"});
            if (userR != null && ! "".equals(userR.getStringValue())) {
                userId = userR.getStringValue();
            }
        }

        String locationId = developerHelperService.getCurrentLocationReference();
        String locType = view.getPathSegment(2);
        String locId = view.getPathSegment(3);
        if (locType != null && locId != null) {
            // get the location from the URL
            if ("tool".equals(locType)) {
                locationId = getLocationIdFromToolId(locId);
            } else {
                locationId = "/"+locType+"/"+locId;
            }
        } else {
            if (locationId == null) {
                throw new IllegalArgumentException("expects a URL like: GET /kaltura/perms/site/{siteId} OR GET /kaltura/perms/tool/{toolId}");
            }
        }
        if (locationId == null) {
            throw new IllegalArgumentException("cannot check permissions for a user without a location being set");
        }

        info.put("userId", userId);
        info.put("locationId", locationId);
        info.put("superuser", external.isUserAdmin(userId));
        info.put("admin", service.isKalturaAdmin(userId, locationId));
        info.put("editor", service.isKalturaEditor(userId, locationId));
        info.put("manager", service.isKalturaManager(userId, locationId));
        info.put("write", service.isKalturaWrite(userId, locationId));
        info.put("read", service.isKalturaRead(userId, locationId));
        boolean uploadSpecial = service.isKalturaUploadSpecial(userId, locationId);
        info.put("uploadSpecial", uploadSpecial);
        if (uploadSpecial) {
            String widgetPlayerUploadSpecialId = kalturaAPIService.getKalturaWidgetId(Widget.UPLOADER_SPECIAL);
            String widgetPlayerUploadSpecialKS = kalturaAPIService.getKalturaClient(KalturaAPIService.KS_PERM_LIST).getSessionId();
            info.put("uploadSpecialId", widgetPlayerUploadSpecialId);
            info.put("uploadSpecialKS", widgetPlayerUploadSpecialKS);
        }

        return new ActionReturn(info);
    }

    @EntityCustomAction(action="library", viewKey=EntityView.VIEW_LIST)
    public Object getLibrary(EntityView view, Search search) {
        // GET /kaltura/library/site/{siteId}
        // GET /kaltura/library/tool/{toolId}
        int start = 0;
        int limit = 0;
        String locationId = null;
        Filter filter = null;
        String searchFilter = null;

        String locType = view.getPathSegment(2);
        String locId = view.getPathSegment(3);
        if (locType == null || locId == null) {
            throw new IllegalArgumentException("expects a URL like: GET /kaltura/library/site/{siteId} OR GET /kaltura/library/tool/{toolId}");
        }
        if ("tool".equals(locType)) {
            locationId = getLocationIdFromToolId(locId);
        } else {
            locationId = "/"+locType+"/"+locId;
        }

        if (search != null) {
            Restriction sharedR = search.getRestrictionByProperties(new String[] {"shared"});
            if (sharedR != null && sharedR.getBooleanValue()) {
                filter = Filter.SHARED;
            }
            Restriction filterR = search.getRestrictionByProperty("search");
            if (filterR != null) {
                searchFilter = filterR.getStringValue();
            }
            start = (int) search.getStart();
            limit = (int) search.getLimit();
        }
        if (locationId == null) {
            locationId = developerHelperService.getCurrentLocationReference();
        }
        List<MediaItem> mediaItems = "isMyMedia".equals(locId) ? service.getMyMedia(null, null, start, limit) : 
                service.getLibrary(locationId, filter, searchFilter, null, start, limit);
        return new ActionReturn(mediaItems);
    }

    @EntityCustomAction(action="libraryadd", viewKey=EntityView.VIEW_NEW)
    public Object addToLibrary(EntityView view, Search search, Map<String, Object> data) {
        // POST /kaltura/libraryadd/{keid}/site/{siteId}
        // POST /kaltura/libraryadd/{keid}/tool/{toolId}
        String locationId = null;
        String locType = view.getPathSegment(3);
        String locId = view.getPathSegment(4);

        if ("tool".equals(locType)) {
            locationId = getLocationIdFromToolId(locId);
        } else {
            locationId = "/"+locType+"/"+locId;
        }
        if (locationId == null) {
            locationId = developerHelperService.getCurrentLocationReference();
        }
        // SKE-222 check for user's workspace site lib and correct the location id
        if (StringUtils.startsWith(locationId, "/site/~")) {
            locationId = "/site/~" + external.getCurrentUserId();
        }
        if (data == null) {
            data = new HashMap<String, Object>();
        }
        String[] eids = null;

        // retrieve ids from the data structure
        Object eidsObj = data.get("eids[]");
        String mediaItemId = "";
        if (eidsObj == null) { // pull from command line
            eids = new String[1];
            eids[0] = view.getPathSegment(2);
        } else if (eidsObj instanceof String) { // only one eid
            eids = new String[1];
            eids[0] = (String) eidsObj;
        } else if (eidsObj instanceof String[]) { // multiple eids
            eids = (String[]) eidsObj;
        } else { // something is wrong if we get here
            throw new IllegalArgumentException("expects a URL like: POST /kaltura/libraryadd/{keid}/site/{locationId} OR POST /kaltura/libraryadd/{keid}/tool/{toolId}");
        }
        String collectionId = data.containsKey("collectionId") ? data.get("collectionId").toString() : null;
        for (String kalturaId : eids) {
            if (kalturaId == null || locType == null || locId == null) {
                throw new IllegalArgumentException("expects a URL like: POST /kaltura/libraryadd/{keid}/site/{locationId} OR POST /kaltura/libraryadd/{keid}/tool/{toolId}");
            }
            mediaItemId = service.addKalturaItemToLibrary(locationId, kalturaId);
            // add the item to the collection as well IF the collectionId is set
            if (StringUtils.isNotEmpty(collectionId) && !StringUtils.substringAfterLast(locationId, "/site/").equals(collectionId)) {
                service.addKalturaItemToCollection(collectionId, locationId, mediaItemId, 0);
            }
        }
        ActionReturn ar = new ActionReturn(mediaItemId);
        ar.setResponseCode(HttpServletResponse.SC_CREATED);
        return ar;
    }

    @EntityCustomAction(action="media", viewKey="")
    public ActionReturn saveMediaItem(EntityView view, Search search, Map<String, Object> data) {
        // POST /kaltura/media/{mediaId}/my
        // POST /kaltura/media/{mediaId}/site/{siteId}
        // POST /kaltura/media/{mediaId}/coll/{cid}
        String mediaId = view.getPathSegment(2);
        if ( StringUtils.isBlank(mediaId) ) {
            throw new IllegalArgumentException("expects a URL like: /kaltura/media/{mediaId}/...");
        }
        // POST or PUT only
        if (!EntityView.Method.POST.name().equals(view.getMethod()) && !EntityView.Method.PUT.name().equals(view.getMethod())) {
            throw new IllegalArgumentException("Method must be POST or PUT");
        }
        String containerType = view.getPathSegment(3);
        if ( StringUtils.isBlank(containerType) ) {
            throw new IllegalArgumentException("expects a URL like: /kaltura/media/{mediaId}/my OR /kaltura/media/{mediaId}/site/{siteId} OR /kaltura/media/{mediaId}/coll/{cid}");
        }
        String locationId = null;
        String collectionId = null;
        if ("my".equals(containerType)) {
            // Kaltura id (indicated by "KID=") should only happen when the items are from My Media
            String kalturaId = mediaId.startsWith("KID=") ? mediaId.substring(4) : null;
            // translate the media id if needed (this should no longer be required)
            mediaId = (kalturaId != null ? kalturaId : mediaId);
        } else if ("site".equals(containerType)) {
            locationId = view.getPathSegment(4);
            if ( StringUtils.isBlank(locationId) ) {
                throw new IllegalArgumentException("blank siteId, expects a URL like: /kaltura/media/{mediaId}/site/{siteId}");
            }
            locationId = "/site/"+locationId;
        } else if ("coll".equals(containerType)) {
            collectionId = view.getPathSegment(4);
            if ( StringUtils.isBlank(collectionId) ) {
                throw new IllegalArgumentException("blank collection id (a.k.a. cid), expects a URL like: /kaltura/media/{mediaId}/coll/{cid}");
            }
        } else {
            throw new IllegalArgumentException("invalid type in url ("+containerType+"), expects a URL like: /kaltura/media/{mediaId}/my OR /kaltura/media/{mediaId}/site/{siteId} OR /kaltura/media/{mediaId}/coll/{cid}");
        }

        MediaItem item;
        if (collectionId != null) {
            item = service.getCollectionMediaItem(mediaId, collectionId, false); // UNPOPULATED
            if (item == null) {
                throw new EntityNotFoundException("No media item in collection ("+collectionId+") with id: " + mediaId, view.getEntityURL());
            }
            // MediaCollection and location will be populated in the item
        } else if (locationId != null) {
            item = service.getLibraryMediaItem(mediaId, locationId, false);
            if (item == null) {
                throw new EntityNotFoundException("No media item in library ("+locationId+") with id: " + mediaId, view.getEntityURL());
            }
            // location will be populated in the item
        } else {
            item = service.getMyMediaByKalturaId(mediaId, external.getCurrentUserId());
            if (item == null) {
                throw new EntityNotFoundException("No media item with id: " + mediaId, view.getEntityURL());
            }
        }
        if (! item.isManage() && ! item.isControl() && ! item.isEdit()) {
            throw new SecurityException("User ("+external.getCurrentUserId()+") cannot edit/control/manage item ("+item.getIdStr()+"): " + item);
        }
        developerHelperService.copyBean(data, item, 0, null, true);
        if (item.isManage()) {
            // update the item perms IF user has manage perms (OR EXCEPTION)
            item = service.updateMediaItem(item);
        }
        if (item.isEdit()) {
            // update the KME fields if allowed and they are found (name, desc, tags)
            String name = (String) data.get("name");
            String desc = (String) data.get("desc");
            String tags = (String) data.get("tags");
            if (name != null || desc != null || tags != null) {
                KalturaMediaEntry kme = kalturaAPIService.getKalturaItem(item.getKalturaId());
                if (kme != null) {
                    if (name != null) {
                        kme.name = name;
                    }
                    if (desc != null) {
                        kme.description = desc;
                    }
                    if (tags != null) {
                        kme.tags = tags;
                    }
                    kalturaAPIService.updateKalturaItem(kme); // no perm check
                } else {
                    throw new IllegalStateException("Invalid KME id ("+item.getKalturaId()+") in this item ("+item.getIdStr()+")");
                }
            }
        }
        service.populateItem(item);
        ActionReturn ar = new ActionReturn(item);
        // single media item are ALWAYS populated
        return ar;
    }

    // NOTE: removed /kaltura/initclip/{mediaId} because it did not seem to be used

    /**
     * Allows use to get the various kaltura media entries for a user directly and to manage them as well
     */
    @EntityCustomAction(action="kme", viewKey="")
    public Object manageKalturaMediaEntries(EntityView view, Search search, Map<String, Object> data) {
        // GET /kaltura/kme
        // GET /kaltura/kme/keid
        // PUT /kaltura/kme/keid
        // DEL /kaltura/kme/keid
        Object result = null;
        String keid = view.getPathSegment(2);
        if ( StringUtils.isBlank(keid) ) {
            if (EntityView.Method.GET.name().equals(view.getMethod())) {
                // get all kaltura items
                String query = "";
                String qstr = (String) search.getRestrictionValueByProperties(new String[] {"search"});
                if (qstr != null) {
                    query = qstr;
                }
                String[] keids = null;
                String keidsStr = (String) search.getRestrictionValueByProperties(new String[] {"keids"});
                if (keidsStr != null) {
                    keids = StringUtils.split(keidsStr, ", "); // WARNING: will split on space OR comma
                }
                result = kalturaAPIService.getKalturaItems(query, keids, (int) search.getStart(), (int) search.getLimit());
            } else {
                throw new EntityException("Only GET is supported for retrieving the list of KMEs", view.getEntityURL(), HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } else {
            // get the kaltura entry
            KalturaMediaEntry kme = kalturaAPIService.getKalturaItem(keid);
            if (kme == null) {
                throw new EntityNotFoundException("No kme with the given id: "+keid, view.getEntityURL());
            }
            if (EntityView.Method.GET.name().equals(view.getMethod())) {
                // return a single kme
                result = kme;
            } else if (EntityView.Method.DELETE.name().equals(view.getMethod())) {
                // remove this kme
                result = kalturaAPIService.removeKalturaItem(keid);
            } else {
                // update the kme
                developerHelperService.copyBean(data, kme, 0, null, true);
                result = kalturaAPIService.updateKalturaItem(kme);
            }
        }
        return result;
    }

    @EntityCustomAction(action="item", viewKey=EntityView.VIEW_SHOW)
    public MediaItem findCollectionItem(EntityView view, Search search) {
        // GET /kaltura/cid/media/mid
        String cid = view.getEntityReference().getId();
        String mediaId = view.getPathSegment(3);
        if (cid == null || mediaId == null) {
            throw new IllegalArgumentException("expects a URL like: GET /kaltura/{cid}/item/{mediaId}");
        }
        MediaCollection mc = service.getCollection(cid, 0, 0);
        if (mc == null) {
            throw new IllegalArgumentException("Invalid collection id: "+cid);
        }
        MediaItem item = mc.getItemById(mediaId);
        if (item == null) {
            throw new IllegalArgumentException("This media item (" + mediaId + ") is not in the parent collection id: " + cid);
        }
        // single media item are ALWAYS populated
        return item;
    }

    @EntityCustomAction(action="add", viewKey=EntityView.VIEW_EDIT)
    public MediaItem addCollectionItem(EntityView view) {
        // POST /kaltura/{cid}/add/{kid}[/{pos}]
        String cid = view.getEntityReference().getId();
        String kalturaId = view.getPathSegment(3);
        int position = 0;
        String posStr = view.getPathSegment(4);
        if (posStr != null) {
            position = Integer.parseInt(posStr);
        }
        if (cid == null || kalturaId == null) {
            throw new IllegalArgumentException("expects a URL like: PUT /kaltura/{cid}/add/{kid}[/{pos}]");
        }
        MediaItem item = service.addKalturaItemToCollection(cid, null, kalturaId, position);
        // The ajax call expects to get back the fully populated item
        //MediaItem item = service.getCollectionMediaItem(mediaId, cid, true);
        return item;
    }

    @EntityCustomAction(action="replace", viewKey=EntityView.VIEW_EDIT)
    public ActionReturn replaceCollectionItems(EntityView view, Map<String, Object> data) {
        // POST /kaltura/{cid}/replace (list of item ids in params are required)
        // added for https://jira.sakaiproject.org/browse/SKE-174
        String cid = view.getEntityReference().getId();
        if (cid == null) {
            throw new IllegalArgumentException("expects a URL like: PUT /kaltura/{cid}/replace WITH an 'ids[]' param containing ordered list of item ids for the collection");
        }
        Object ids;
        if (!data.containsKey("ids[]") || data.get("ids[]") == null) {
            if (data.containsKey("ids")) {
                ids = data.get("ids"); // should be a single string
            } else {
                throw new IllegalArgumentException("replace action requires 'ids[]' param containing an ordered list of item ids for the collection");
            }
        } else {
           ids = data.get("ids[]");
        }
        String[] kalturaIds;
        if (ids instanceof String[]) {
            kalturaIds = (String[]) ids;
        } else if (StringUtils.isNotBlank((String)ids)) {
            // single item needs to convert back into an array
            kalturaIds = new String[] { (String)ids };
        } else {
            kalturaIds = new String[0]; // empty means remove all the items
        }
        List<MediaItem> items = service.updateCollectionMediaItems(cid, kalturaIds);
        return new ActionReturn(items);
    }

    // NOTE: removed /kaltura/cid/save/mid because it did not seem to be used

    @EntityCustomAction(action="order", viewKey=EntityView.VIEW_EDIT)
    public MediaItem reorderItem(EntityView view) {
        // POST /kaltura/{cid}/order/{kid}/{pos}
        String cid = view.getEntityReference().getId();
        String kalturaId = view.getPathSegment(3);
        String posStr = view.getPathSegment(4);
        if (cid == null || kalturaId == null || posStr == null) {
            throw new IllegalArgumentException("expects a URL like: POST /kaltura/{cid}/order/{kid}/{pos}");
        }
        int position = Integer.parseInt(posStr);
        MediaItem item = service.addKalturaItemToCollection(cid, null, kalturaId, position);
        // NOTE: the media item is not populated
        //MediaItem item = service.getCollectionMediaItem(mediaId, cid, false);
        return item;
    }

    @EntityCustomAction(action="remove", viewKey="")
    public boolean removeItem(EntityView view) {
        // POST /kaltura/remove/{mid}/site/{cid}
        // POST /kaltura/remove/{mid}/coll/{cid}
        String mediaId = view.getPathSegment(2);
        if (mediaId == null) {
            throw new IllegalArgumentException("expects a URL like: POST /kaltura/remove/{mid}/site/{cid}");
        }
        // POST only
        if (!EntityView.Method.POST.name().equals(view.getMethod())) {
            throw new IllegalArgumentException("Method must be POST");
        }
        String containerType = view.getPathSegment(3);
        if ( StringUtils.isBlank(containerType) ) {
            throw new IllegalArgumentException("expects a URL like: /kaltura/remove/{mediaId}/site/{siteId} OR /kaltura/remove/{mediaId}/coll/{cid}");
        }
        String locationId = null;
        String collectionId = null;
        if ("site".equals(containerType)) {
            locationId = view.getPathSegment(4);
            if ( StringUtils.isBlank(locationId) ) {
                throw new IllegalArgumentException("blank siteId, expects a URL like: /kaltura/remove/{mediaId}/site/{siteId}");
            }
            locationId = "/site/"+locationId;
        } else if ("coll".equals(containerType)) {
            collectionId = view.getPathSegment(4);
            if ( StringUtils.isBlank(collectionId) ) {
                throw new IllegalArgumentException("blank collection id (a.k.a. cid), expects a URL like: /kaltura/remove/{mediaId}/coll/{cid}");
            }
        } else {
            throw new IllegalArgumentException("invalid type in url ("+containerType+"), expects a URL like: /kaltura/remove/{mediaId}/site/{siteId} OR /kaltura/remove/{mediaId}/coll/{cid}");
        }
        boolean success = false;
        if (collectionId != null) {
            success = service.removeKalturaItemFromCollection(collectionId, mediaId);
        } else if (locationId != null) {
            success = service.removeKalturaItemFromLibrary(locationId, mediaId);
        }
        return success;
    }


    // MY MEDIA

    /**
     * @return The list of MyMediaCollectionItem (represented collection including the Site Library) 
     *      which the user has access to in the given Sakai site
     */
    @EntityCustomAction(action="mymediatargets", viewKey="")
    public ActionReturn getTargetCollectionsForMyMedia(EntityView view, Map<String, Object> data) {
        // GET /kaltura/mymediatargets/{kalturaId}/site/{siteId}
        // GET /kaltura/mymediatargets/{kalturaId}/tool/{toolId}
        if (view.getPathSegments().length < 5) {
            throw new IllegalArgumentException("not enough path segments ("+view.getOriginalEntityUrl()+"), expects a URL like: GET /kaltura/mymediatargets/{kalturaId}/site/{siteId} OR /kaltura/mymediatargets/{kalturaId}tool/{toolId}");
        }
        // NOTE: by using show - we know the first key will always be set
        String kalturaId = view.getPathSegment(2); // will equal the {kalturaId} value
        if (StringUtils.isEmpty(kalturaId)) {
            throw new IllegalArgumentException("kalturaId is blank ("+view.getOriginalEntityUrl()+"), expects a URL like: GET /kaltura/mymediatargets/{kalturaId}/site/{siteId} OR /kaltura/mymediatargets/{kalturaId}tool/{toolId}");
        }
        String locationId = developerHelperService.getCurrentLocationReference();
        String locType = view.getPathSegment(3);
        String locId = view.getPathSegment(4);
        if (locType != null && locId != null) {
            // get the location from the URL
            if ("tool".equals(locType)) {
                locationId = getLocationIdFromToolId(locId);
            } else {
                locationId = "/"+locType+"/"+locId;
            }
        }
        if (StringUtils.isEmpty(locationId)) {
            throw new IllegalArgumentException("locationId is blank ("+view.getOriginalEntityUrl()+"), expects a URL like: GET /kaltura/mymediatargets/{kalturaId}/site/{siteId} OR /kaltura/mymediatargets/{kalturaId}tool/{toolId}");
        }

        List<MyMediaCollectionItem> result = service.getCollectionsForMyMedia(kalturaId, locationId);
        return new ActionReturn(result); // needed to avoid this trying to encode inappropriate data
    }

    @EntityCustomAction(action="mymediaadd", viewKey="")
    public String addToCollectionFromMyMedia(EntityView view, Map<String, Object> data) {
        // POST /kaltura/mymediaadd/{kalturaId}/site/{siteId}
        // POST /kaltura/mymediaadd/{kalturaId}/collection/{collectionId}
        if (view.getPathSegments().length < 5) {
            throw new IllegalArgumentException("not enough path segments ("+view.getOriginalEntityUrl()+"), expects a URL like: POST /kaltura/mymediaadd/{kalturaId}/site/{siteId} OR /kaltura/mymediaadd/{kalturaId}/collection/{collectionId}");
        }
        // POST only
        if (!EntityView.Method.POST.name().equals(view.getMethod()) && !EntityView.Method.PUT.name().equals(view.getMethod())) {
            throw new IllegalArgumentException("Method must be POST");
        }
        // NOTE: by using show - we know the first key will always be set
        String kalturaId = view.getPathSegment(2); // will equal the {kalturaId} value
        if (StringUtils.isEmpty(kalturaId)) {
            throw new IllegalArgumentException("kalturaId is blank ("+view.getOriginalEntityUrl()+"), expects a URL like: POST /kaltura/mymediaadd/{kalturaId}/site/{siteId} OR /kaltura/mymediaadd/{kalturaId}/collection/{collectionId}");
        }
        String collectionId = null;
        String locationId = developerHelperService.getCurrentLocationReference();
        String locType = view.getPathSegment(3);
        String locId = view.getPathSegment(4);
        String mediaId = null;
        if (locType != null && locId != null) {
            // get the collection or location from the URL
            if ("collection".equals(locType)) {
                collectionId = locId;
                MediaItem mi = service.addKalturaItemToCollection(collectionId, locationId, kalturaId, -1);
                mediaId = mi.getId();
            } else {
                locationId = "/"+locType+"/"+locId;
                mediaId = service.addKalturaItemToLibrary(locationId, kalturaId);
            }
        }
        if (StringUtils.isEmpty(locationId) && StringUtils.isEmpty(collectionId)) {
            throw new IllegalArgumentException("collectionId and locationId are both blank ("+view.getOriginalEntityUrl()+"), expects a URL like: POST /kaltura/mymediaadd/{kalturaId}/site/{siteId} OR /kaltura/mymediaadd/{kalturaId}/collection/{collectionId}");
        }
        return mediaId;
    }


    // CLIPPING

    /**
     * Save a new clip from an existing clip.
     * expects a URL like: PUT /kaltura/{entryId}/saveClip/{start}/{end}
     * 
     * @param view
     * @param data
     * @return media item which contains the new clip
     */
    @EntityCustomAction(action = "saveClip", viewKey = EntityView.VIEW_EDIT)
    public MediaItem saveClip(EntityView view, Map<String, Object> data) {
        // PUT /kaltura/{entryId}/saveClip/{start}/{end}
        if (view.getEntityReference().getId() == null) {
            throw new IllegalArgumentException("entryId required - expects a URL like: PUT /kaltura/{entryId}/saveClip/{start}/{end}");
        }
        // POST only
        if (!EntityView.Method.POST.name().equals(view.getMethod()) && !EntityView.Method.PUT.name().equals(view.getMethod())) {
            throw new IllegalArgumentException("Method must be POST");
        }
        KalturaMediaEntry newEntry = createClip(view, data);
        String locationId = data.get("locationId").toString();
        String username = external.getCurrentUserName();
        MediaItem mi = new MediaItem(locationId, newEntry.id, username, true, false, false);
        mi.setKalturaItem(newEntry);
        if (data.get("saveToSiteLibrary") != null && "true".equalsIgnoreCase(data.get("saveToSiteLibrary").toString()))
            service.addKalturaItemToLibrary(locationId, mi.getKalturaId());
        return mi;
    }

    /**
     * Save a new clip and then replace the parent clip in the identified collection with the new clip.
     * expects a URL like: PUT /kaltura/{entryId}/replaceClip/{start}/{end}/{cid}/{position}
     * 
     * @param view
     * @param data
     * @return media item of the newly created clip
     */
    @EntityCustomAction(action = "replaceClip", viewKey = EntityView.VIEW_EDIT)
    public MediaItem createClipAndSwapWithOriginal(EntityView view, Map<String, Object> data) {
        // PUT /kaltura/{entryId}/replaceClip/{start}/{end}/{cid}/{position}
        String entryId = view.getEntityReference().getId();
        if (entryId == null) {
            throw new IllegalArgumentException(
                    "entryId required - expects a URL like: PUT /kaltura/{entryId}/replaceClip/{start}/{end}/{cid}/{position}");
        }
        // POST only
        if (!EntityView.Method.POST.name().equals(view.getMethod()) && !EntityView.Method.PUT.name().equals(view.getMethod())) {
            throw new IllegalArgumentException("Method must be POST");
        }
        String containerId = view.getPathSegment(5);
        if (containerId == null) {
            throw new IllegalArgumentException(
                    "collectionId required - expects a URL like: PUT /kaltura/{entryId}/replaceClip/{start}/{end}/{cid}/{position}");
        }
        if (view.getPathSegment(6) == null) {
            throw new IllegalArgumentException(
                    "position required - expects a URL like: PUT /kaltura/{entryId}/replaceClip/{start}/{end}/{cid}/{position}");
        }
        int position = Integer.parseInt(view.getPathSegment(6));
        // In case the user checked the "save to site library" box. We want to clear this so that save clip doesn't add it to the library.
        data.put("saveToSiteLibrary", null);
        MediaItem mi = saveClip(view, data);

        String locationId = (String) data.get("locationId");
        if (StringUtils.contains(locationId, containerId)) {
            // swap in the site library
            service.removeKalturaItemFromLibrary(locationId, entryId);
            service.addKalturaItemToLibrary(locationId, mi.getKalturaId());
        } else {
            // Swap in the collection
            MediaItem itemToRemoveFromCollection = service.getCollectionMediaItem(entryId, containerId, false);
            service.addKalturaItemToCollection(containerId, locationId, mi.getKalturaId(), position);
            service.removeKalturaItemFromCollection(containerId, itemToRemoveFromCollection.getIdStr());
        }
        return mi;
    }

    private KalturaMediaEntry createClip(EntityView view, Map<String, Object> data) {
        String entryId = view.getEntityReference().getId();
        String start = view.getPathSegment(3);
        String end = view.getPathSegment(4);
        if (start == null) {
            throw new IllegalArgumentException("start must be set in order to save");
        }
        int startTime = Integer.valueOf(start);
        if (end == null) {
            throw new IllegalArgumentException("end must be set in order to save");
        }
        int endTime = Integer.valueOf(end);
        int clipDuration = endTime - startTime;
        if (clipDuration <= 0) {
            throw new IllegalArgumentException("clipDuration (" + clipDuration + ") must be > 0 in order to save");
        }

        KalturaMediaEntry kalturaEntry = new KalturaMediaEntry();
        kalturaEntry.id = entryId;
        if (data.get("name") != null) {
            kalturaEntry.name = data.get("name").toString();
        }
        if (data.get("desc") != null) {
            kalturaEntry.description = data.get("desc").toString();
        }
        KalturaMediaType mediaType = KalturaMediaType.VIDEO;
        if (data.get("mediaType") != null) {
            // should only be possible to be video or audio (right?)
            int mediaTypeParam = Integer.valueOf(data.get("mediaType").toString());
            if (KalturaMediaType.AUDIO.equals(mediaTypeParam)) {
                mediaType = KalturaMediaType.AUDIO;
            }
        }
        kalturaEntry.mediaType = mediaType;
        return kalturaAPIService.createClip(kalturaEntry, startTime, clipDuration);
    }

    // STANDARD METHODS

    public Object getEntity(EntityReference ref) {
        if (ref.getId() == null) {
            // SPECIAL case for entity identification (should be empty)
            return new MediaCollection(null, null, null, null, false, null);
        }
        MediaCollection entity = service.getCollection(ref.getId(), 0, -1);
        // populate the kaltura items as requested
        Boolean populateKaltura = reqStore.getStoredValueAsType(Boolean.class, "populate");
        if (populateKaltura != null && populateKaltura.booleanValue()) {
            service.populateItems(entity.getItems());
        }
        if (entity != null) {
            return entity;
        }
        throw new IllegalArgumentException("Invalid id:" + ref.getId());
    }

    public List<?> getEntities(EntityReference ref, Search search) {
        String locationId = null;
        int start = 0;
        int limit = 0;
        int includeItems = 0;
        String filter = null;
        if (search != null) {
            String locId = (String) search.getRestrictionValueByProperties(new String[] {"locationId","context"});
            if (locId != null) {
                locationId = "/site/"+locId;
            } else {
                String toolId = (String) search.getRestrictionValueByProperties(new String[] {"toolId"});
                locationId = getLocationIdFromToolId(toolId);
            }
            start = (int) search.getStart();
            limit = (int) search.getLimit();
            filter = (String) search.getRestrictionValueByProperties(new String[] {"filter","search"});
            // allow including items
            Restriction r = search.getRestrictionByProperties(new String[] {"items"});
            if (r != null) {
                Integer i = Integer.getInteger(r.getStringValue());
                if (i != null) {
                    includeItems = i.intValue();
                }
            }
        }
        if (locationId == null) {
            locationId = developerHelperService.getCurrentLocationReference();
        }
        Boolean hiddenFilter = false;
        if (service.isKalturaAdmin(developerHelperService.getCurrentUserId(), locationId) 
                || service.isKalturaManager(developerHelperService.getCurrentUserId(), locationId)) {
            hiddenFilter = null;
        }
        List<MediaCollection> mediaCollections = service.getCollections(locationId, hiddenFilter, includeItems, filter, start, limit);
        return mediaCollections;
    }

    public String createEntity(EntityReference ref, Object entity) {
        return createEntity(ref, entity, null);
    }

    public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        MediaCollection item = (MediaCollection) entity;
        item = service.addCollection(item.getTitle(), developerHelperService.getCurrentLocationId());
        return item.getId().toString();
    }


    public Object getSampleEntity() {
        // SPECIAL case for entity identification (should be empty)
        return new MediaCollection(null, null, null, null, false, null);
    }

    public void updateEntity(EntityReference ref, Object entity) {
        updateEntity(ref, entity, null);
    }

    public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        MediaCollection item = (MediaCollection) entity;
        MediaCollection current = service.getCollection(ref.getId(), 0, 0);
        if (current == null) {
            throw new IllegalArgumentException("Could not locate entity to update");
        }
        service.updateCollection(item);
    }

    public void deleteEntity(EntityReference ref) {
        deleteEntity(ref, null);
    }

    public void deleteEntity(EntityReference ref, Map<String, Object> params) {
        boolean result = service.removeCollection(ref.getId());
        if (! result) {
            throw new IllegalArgumentException("Could not locate entity to remove");
        }
    }

    RequestStorage reqStore;
    public void setRequestStorage(RequestStorage requestStorage) {
        this.reqStore = requestStorage;
    }

    public String[] getHandledOutputFormats() {
        return new String[] {Formats.XML, Formats.JSON};
    }

    public String[] getHandledInputFormats() {
        return new String[] {Formats.HTML, Formats.XML, Formats.JSON};
    }


    /**
     * This is a special method which allows the lookup of the locationId using a toolId,
     * Many Bothans died to bring us this information... i.e. it was really hard to figure this out
     * 
     * @param toolId the tool id
     * @return the location id of this tool
     */
    public String getLocationIdFromToolId(String toolId) {
        if (toolId == null) {
            throw new IllegalArgumentException("tooldId cannot be null");
        }
        ToolConfiguration tc = siteService.findTool(toolId);
        if (tc == null) {
            throw new IllegalArgumentException("No tool config found in any site with id: " + toolId);
        }
        String locationId = "/site/"+tc.getSiteId();
        return locationId;
    }

    private MediaService service;
    public void setService(MediaService logic) {
        this.service = logic;
    }

    private KalturaAPIService kalturaAPIService;
    public void setKalturaAPIService(KalturaAPIService kalturaAPIService) {
        this.kalturaAPIService = kalturaAPIService;
    }

    private ExternalLogic external;
    public void setExternal(ExternalLogic external) {
        this.external = external;
    }

    private SiteService siteService;
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

}
