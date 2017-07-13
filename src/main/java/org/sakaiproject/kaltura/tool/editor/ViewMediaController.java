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
package org.sakaiproject.kaltura.tool.editor;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.kaltura.logic.ExternalLogic;
import org.sakaiproject.kaltura.logic.MediaService;
import org.sakaiproject.kaltura.model.MediaItem;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class ViewMediaController extends AbstractController {

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        // create our model object
        Map<String,Object> model = new HashMap<String,Object>();

        // check permissions to view
        if (mediaService.canViewItems()) {
            model.put("allowed", true);

            String userId = external.getCurrentUserId();
            model.put("currentUserId", userId);
            String locationId = external.getCurrentLocationId(); // this is probably not going to get us a real location
            model.put("currentLocationId", locationId);

            String kalturaEntryId = request.getParameter("entryId");
            String currentUsername = external.getCurrentUserName();
            MediaItem mi = new MediaItem(locationId, kalturaEntryId, currentUsername, true, false, false);
            mi.indicateUserControl(currentUsername, false, false, false); // this stops it from doing a permissions check by location
            mediaService.populateItem(mi); // this will run populateMediaItemData() already
            //mediaService.populateMediaItemData(mi);
            model.put("entryId", kalturaEntryId);
            model.put("html5PlayerJS", mi.getPlayerJSURL()); // null if html5 player not enabled

            String type = request.getParameter("entryType");
            if (type == null || "".equals(type)) {
                type = MediaItem.TYPE_VIDEO;
            }
            model.put("entryType", type);

            mi.setType(type);
            model.put("item", mi);

            // add the Kaltura partner ID to the model
            model.put("kalturaPartnerId", mediaService.getKalturaPartnerId());
            model.put("kalturaEndpoint", mediaService.getKalturaEndpoint());

            // add the current user's username to the model
            model.put("currentUserName", currentUsername);
        } else {
            // NOT allowed to view this item
            model.put("allowed", false);
        }

        return new ModelAndView("viewMedia", model);
    }

    private MediaService mediaService;
    public void setMediaService(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    private ExternalLogic external;
    public void setExternal(ExternalLogic external) {
        this.external = external;
    }

}
