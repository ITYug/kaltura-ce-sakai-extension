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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.kaltura.logic.MediaService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * DeleteCollectionController handles permanently deleting a user
 * collection.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class DeleteCollectionController extends AbstractController {

    /*
     * (non-Javadoc)
     * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        // remove the indicated collection from the Kaltura server
        String collectionId = request.getParameter("collectionId");
        mediaService.removeCollection(collectionId);

        // add a message about the deletion to the messages list
        List<String> messageCodes = new ArrayList<String>();
        messageCodes.add("editCollectionDetails.removed");
        request.setAttribute("infos", messageCodes);

        // send the user back to the main collection list page
        request.getRequestDispatcher("listCollections.htm").forward(request, response);
        return null;

    }

    private MediaService mediaService;
    public void setMediaService(MediaService service) {
        this.mediaService = service;
    }

}
