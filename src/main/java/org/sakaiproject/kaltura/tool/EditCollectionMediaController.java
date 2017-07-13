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
import org.sakaiproject.kaltura.logic.ExternalLogic;
import org.sakaiproject.kaltura.logic.MediaService;
import org.sakaiproject.kaltura.model.MediaCollection;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * EditCollectionMediaController might in the future handle editing media
 * in an existing Kaltura collection.  Right now, adding and removing media
 * items is handled by two other controllers in this package, and this 
 * controller's submit method should not be called.
 * 
 * @author Aaron Zeckoski, azeckoski@unicon.net
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class EditCollectionMediaController extends AbstractController {

    /*
     * (non-Javadoc)
     * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Map<String,Object> model = new HashMap<String,Object>();

        /*
         * Retrieve the specified collection from the Kaltura service
         * Fully populate all contained media items and add the collection to the model.
         */

        String collectionId = request.getParameter("collectionId");
        MediaCollection collection = mediaService.getCollection(collectionId, 0, 0); // collection only
        // ensure this page cannot be loaded if the user does not have permission
        if (! collection.isControl() && ! collection.isAddItems()) {
            throw new SecurityException("User ("+external.getCurrentUserId()+") does not have permission to add items to this collection ("+collection+")");
        }
        //mediaService.populateItems(collection.getItems());

        // get the permissions
        String locationId = external.getCurrentLocationId();
        String userId = external.getCurrentUserId();

        model.put("collection", collection);
        // permissions
        model.put("showMyMediaTab", mediaService.isKalturaMyMediaVisible(userId, locationId));
        model.put("isSiteLibraryVisible", mediaService.isSiteLibraryVisible(userId, locationId));
        model.put("canAdministrateKalturaPermissions", external.canAdministrateKalturaPermissions());
        model.put("isKalturaAdmin", mediaService.isKalturaAdmin(userId, locationId));
        model.put("isKalturaManager", mediaService.isKalturaManager(userId, locationId));
        model.put("isKalturaWrite", mediaService.isKalturaWrite(userId, locationId));
        model.put("isKalturaRead", mediaService.isKalturaRead(userId, locationId));
        // get data needed for rendering
        model.put("toolId", external.getCurrentToolId());
        model.put("siteId", StringUtils.substringAfterLast(locationId, "/site/"));
        model.put("currentUserDisplay", external.getCurrentUserDisplayName());

        return new ModelAndView("editCollectionMedia", model);
    }

    private MediaService mediaService;
    public void setMediaService(MediaService service) {
        this.mediaService = service;
    }

    private ExternalLogic external;
    public void setExternal(ExternalLogic external) {
        this.external = external;
    }

}
