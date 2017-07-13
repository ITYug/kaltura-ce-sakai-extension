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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class FckKalturaSelectorController extends AbstractController {

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        // create our model object
        Map<String,Object> model = new HashMap<String,Object>();

        // NOTE: we cannot get the current location from here -AZ
        //String locationId = external.getCurrentLocationId();
        String userId = external.getCurrentUserId();
        model.put("currentUserId", userId);
        model.put("isSuperUser", external.isUserAdmin(userId));

        /* Cannot show the special uploader here because we cannot get the current location
        String widgetPlayerUploadSpecialId = "";
        String widgetPlayerUploadSpecialKS = "";
        if (service.isKalturaUploadSpecial(userId, locationId)) {
            widgetPlayerUploadSpecialId = service.getKalturaWidgetId(Widget.UPLOADER_SPECIAL);
            widgetPlayerUploadSpecialKS = service.getKalturaClient(MediaService.KS_PERM_LIST).getSessionId();
        }
        model.put("uploadSpecialId", widgetPlayerUploadSpecialId);
        model.put("uploadSpecialKS", widgetPlayerUploadSpecialKS);
         */

        return new ModelAndView("fckEditorSelector", model);
    }

    private ExternalLogic external;
    public void setExternal(ExternalLogic external) {
        this.external = external;
    }
}
