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
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.kaltura.aspectj.ProfilerControl;
import org.sakaiproject.kaltura.logic.ExternalLogic;
import org.sakaiproject.kaltura.logic.MediaService;
import org.sakaiproject.kaltura.model.MediaCollection;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * ListCollectionsController is currently the main view of the 
 * Kaltura tool.  This controller provides the list of 
 * existing collections for the currently authenticated user. 
 * 
 * @author Aaron Zeckoski, azeckoski@unicon.net
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class ListCollectionsController extends AbstractController {

    final protected Log log = LogFactory.getLog(getClass());

    /**
     * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        Map<String,Object> model = new HashMap<String,Object>();

        String userId = external.getCurrentUserId();
        model.put("currentUsername", userId);

        boolean enabled = mediaService.isKalturaEnabled();
        model.put("isKalturaEnabled", enabled);
        model.put("isKalturaConfigured", mediaService.isKalturaConfigured());
        model.put("isProfilingEnabled", ProfilerControl.isProfilingEnabled());

        boolean initialized = mediaService.isKalturaInitialized();
        model.put("isInitialized", initialized);
        if (enabled && initialized) {
            String locationId = external.getCurrentLocationId();
            boolean migrating = mediaService.isMigrationRunning(locationId);
            model.put("isKalturaMigrating", migrating);
            if (migrating) {
                // migration in progress for this course (percent, time remaining)
                long[] stats = mediaService.getMigrationStats();
                long totalToMigrate = stats[0] + stats[2] + stats[4];
                long leftToMigrate = stats[1] + stats[3] + stats[5];
                long migratedCount = totalToMigrate - leftToMigrate;
                int minsToCompletion = Math.round((leftToMigrate * 3)/60);
                if (minsToCompletion <= 0) {
                    minsToCompletion = 1;
                }
                int percentComplete = totalToMigrate > 0 ? Math.round(((float)migratedCount / totalToMigrate)*100.0f) : 100;
                String msg = percentComplete+","+minsToCompletion;
                model.put("migrationMessageNumbers", msg);
            } else {
                boolean admin = mediaService.isKalturaAdmin(userId, locationId);
                model.put("canAdministrateKalturaPermissions", external.canAdministrateKalturaPermissions());
                model.put("isKalturaAdmin", admin);
                model.put("isKalturaManager", mediaService.isKalturaManager(userId, locationId));
                model.put("isKalturaEditor", mediaService.isKalturaEditor(userId, locationId));
                model.put("isKalturaWrite", mediaService.isKalturaWrite(userId, locationId));
                model.put("isKalturaRead", mediaService.isKalturaRead(userId, locationId));
                model.put("isSiteLibraryVisible", mediaService.isSiteLibraryVisible(userId, locationId));
                model.put("showMyMediaTab", mediaService.isKalturaMyMediaVisible(userId, locationId));

                // get the list of collections from the Kaltura service
                Boolean hiddenFilter = admin ? null : false;
                List<MediaCollection> mediaCollections = mediaService.getCollections(locationId, hiddenFilter, -1, null, 0, 0);
                boolean viewableCollections = false;
                for (MediaCollection mediaCollection : mediaCollections) {
                    if (mediaCollection.isViewable()) {
                        viewableCollections = true;
                        break;
                    }
                }
                model.put("collections", mediaCollections);
                model.put("viewableCollections", viewableCollections);
                model.put("toolIntroText", mediaService.getToolIntroInstructions());
                model.put("emptyCollText", mediaService.getEmptyCollectionsInstructions());
            }
        }

        return new ModelAndView("listCollections", model);
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
