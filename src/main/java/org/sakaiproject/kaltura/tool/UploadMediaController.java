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
import org.sakaiproject.kaltura.logic.KalturaAPIService;
import org.sakaiproject.kaltura.logic.KalturaAPIService.Widget;
import org.sakaiproject.kaltura.logic.MediaService;
import org.sakaiproject.kaltura.logic.ExternalLogic;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * 
 * @author Aaron Zeckoski, azeckoski@unicon.net
 * @version $Revision$
 */
@Controller
@RequestMapping("/uploadMedia.htm")

public class UploadMediaController {

    @RequestMapping(method=RequestMethod.GET) 
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String,Object> model = new HashMap<String,Object>();
        String userId = external.getCurrentUserId();
        model.put("currentUserId", userId);
        String locationId = external.getCurrentLocationId();
        model.put("currentLocationId", locationId);

        String widgetPlayerUploadSpecialId = "";
        String widgetPlayerUploadSpecialKS = "";
        if (service.isKalturaUploadSpecial(userId, locationId)) {
            widgetPlayerUploadSpecialId = kalturaAPIService.getKalturaWidgetId(Widget.UPLOADER_SPECIAL);
            widgetPlayerUploadSpecialKS = kalturaAPIService.getKalturaClient(KalturaAPIService.KS_PERM_LIST).getSessionId();
        }
        model.put("uploadSpecialId", widgetPlayerUploadSpecialId);
        model.put("uploadSpecialKS", widgetPlayerUploadSpecialKS);
        model.put("toolId", external.getCurrentToolId());
        model.put("isSiteLibraryVisible", service.isSiteLibraryVisible(userId, locationId));

        String collectionId = request.getParameter("collectionId");
        if (!StringUtils.substringAfterLast(locationId, "/site/").equals(collectionId)) {
            model.put("collectionId", collectionId);
        }

        return new ModelAndView("uploadMedia", model);
    }

    private ExternalLogic external;
    public void setExternal(ExternalLogic external) {
        this.external = external;
    }

    private KalturaAPIService kalturaAPIService;
    public void setKalturaAPIService(KalturaAPIService kalturaAPIService) {
        this.kalturaAPIService = kalturaAPIService;
    }

    private MediaService service;
    public void setService(MediaService service) {
        this.service = service;
    }

}
