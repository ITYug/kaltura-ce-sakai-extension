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
package org.sakaiproject.kaltura.tool;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.kaltura.logic.ExternalLogic;
import org.sakaiproject.kaltura.logic.KalturaAPIService;
import org.sakaiproject.kaltura.logic.MediaService;
import org.sakaiproject.kaltura.logic.User;
import org.sakaiproject.kaltura.logic.KalturaAPIService.Widget;
import org.sakaiproject.kaltura.model.MediaCollection;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.kaltura.client.types.KalturaCategory;

/**
 * ViewCollectionController provides a detailed view of an
 * individual Kaltura collection 
 * (which could be a set of MyMedia, an actual collection, or the site library).
 * 
 * @author Aaron Zeckoski, azeckoski@unicon.net
 * @version $Revision$
 */
public class ViewCollectionController extends AbstractController {

    final protected Log log = LogFactory.getLog(getClass());

    /*
     * (non-Javadoc)
     * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        Map<String,Object> model = new HashMap<String,Object>();

        String locationId = external.getCurrentLocationId();
        model.put("currentLocationId", locationId);
        KalturaCategory siteCat = kalturaAPIService.getSiteCategory(locationId);
        if (siteCat != null) {
            model.put("siteCategoryId", siteCat.id);
        }
        String userId = external.getCurrentUserId();
        model.put("currentUserId", userId);
        boolean admin = external.canAdministrateKalturaPermissions();
        model.put("canAdministrateKalturaPermissions", admin);
        model.put("isKalturaAdmin", mediaService.isKalturaAdmin(userId, locationId));
        model.put("isKalturaManager", mediaService.isKalturaManager(userId, locationId));
        model.put("isKalturaWrite", mediaService.isKalturaWrite(userId, locationId));
        model.put("isKalturaRead", mediaService.isKalturaRead(userId, locationId));
        model.put("isSiteLibraryVisible", mediaService.isSiteLibraryVisible(userId, locationId));
        model.put("showDeleteItemWarnings", mediaService.getShowKalturaDeleteItemWarnings());
        model.put("showMyMediaTab", mediaService.isKalturaMyMediaVisible(userId, locationId));
        model.put("clippingEnabled", mediaService.isKalturaClippingEnabled());

        // Retrieve the media collection, populate its items and add it to our data model
        String collectionId = request.getParameter("collectionId");
        String mediaContainerType; // indicates the current item container rendering: "mymedia", "library", or "collection"
        String mediaContainerId; // contains the id of the container (the library siteId or collection id for example)
        boolean isMyMedia = "myMedia".equals(request.getParameter("view"));
        boolean isLibrary = false;
        boolean isCollectionView = false;
        if (StringUtils.isBlank(collectionId)) {
            if (isMyMedia) {
                model.put("currentCollectionId", "myMedia");
                mediaContainerType = "mymedia";
                User u = external.getCurrentUser();
                mediaContainerId = (u == null ? userId : u.getUsername());
                if (log.isDebugEnabled()) log.debug("View My Media for user: "+mediaContainerId);
            } else {
                String locId = developerHelperService.getLocationIdFromRef(locationId);
                model.put("currentCollectionId", locId);
                mediaContainerType = "library";
                mediaContainerId = locationId;
                if (log.isDebugEnabled()) log.debug("View Site Library in location: "+mediaContainerId);
                isLibrary = true;
            }
        } else {
            if (log.isDebugEnabled()) log.debug("View collection: "+collectionId);
            MediaCollection collection = mediaService.getCollection(collectionId, 0, 0); // only the collection (no items)
            if (collection == null) {
                // TODO: error
                return new ModelAndView("viewCollection", model);
            }
            model.put("collection", collection);
            model.put("currentCollectionId", collectionId);
            isCollectionView = !StringUtils.substringAfterLast(locationId, "/site/").equals(collectionId);
            mediaContainerType = "collection";
            mediaContainerId = collectionId;
        }

        // Retrieve or auto-populate a media item from this collection
        String mediaId = request.getParameter("mid");
        model.put("currentItemId", StringUtils.isNotBlank(mediaId) ? mediaId : "0");

        // should return null if html5 player not enabled
        model.put("html5PlayerJS", mediaService.findKalturaPlayerJSURL(kalturaAPIService.getKalturaWidgetId(Widget.PLAYER_VIDEO)));

        model.put("isMyMedia", isMyMedia);
        model.put("isLibrary", isLibrary);
        model.put("isCollection", isCollectionView);
        model.put("isViewCollection", isCollectionView);
        model.put("mediaContainerType", mediaContainerType);
        model.put("mediaContainerId", mediaContainerId);
        return new ModelAndView("viewCollection", model);
    }

    private MediaService mediaService;
    public void setMediaService(MediaService service) {
        this.mediaService = service;
    }

    private KalturaAPIService kalturaAPIService;
    public void setKalturaAPIService(KalturaAPIService kalturaAPIService) {
        this.kalturaAPIService = kalturaAPIService;
    }

    private ExternalLogic external;
    public void setExternal(ExternalLogic external) {
        this.external = external;
    }

    private DeveloperHelperService developerHelperService;
    public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
        this.developerHelperService = developerHelperService;
    }

}
