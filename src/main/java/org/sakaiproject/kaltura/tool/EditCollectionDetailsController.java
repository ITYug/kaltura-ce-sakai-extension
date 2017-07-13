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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.kaltura.logic.ExternalLogic;
import org.sakaiproject.kaltura.logic.MediaService;
import org.sakaiproject.kaltura.model.MediaCollection;
import org.sakaiproject.kaltura.mvc.command.CollectionForm;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * EditCollectionDetailsController handles creating new Kaltura
 * collections and editing the main title/description details of
 * existing collections.
 * 
 * @author Aaron Zeckoski, azeckoski@unicon.net
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Controller
@RequestMapping("/editCollectionDetails.htm")
public class EditCollectionDetailsController {

    final protected Log log = LogFactory.getLog(getClass());

    /**
     * Default constructor
     */
   /* public EditCollectionDetailsController() {
        this.setCommandClass(CollectionForm.class);
    }*/

    /*
     * (non-Javadoc)
     * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
     */
    @RequestMapping(method=RequestMethod.POST) 
    protected ModelAndView onSubmit(@ModelAttribute("priceIncrease")CollectionForm form, 
            BindingResult result)
                    throws Exception {

        //CollectionForm form = (CollectionForm) command;

        MediaCollection collection;

        if (StringUtils.isBlank(form.getId())) {
            // if this is a new collection, create a new collection on the Kaltura server
            String locationId = external.getCurrentLocationId(); 
            if (log.isDebugEnabled()) log.debug("Submit add collection in location: "+locationId);
            collection = mediaService.addCollection(form.getTitle(), locationId);
        } else {
            // otherwise, retrieve the current collection from Kaltura
            if (log.isDebugEnabled()) log.debug("Submit update collection: "+form.getId());
            collection = mediaService.getCollection(form.getId(), 0, 0);
        }

        // update the collection with the form information
        collection.setTitle(form.getTitle());
        collection.setDescription(form.getDescription());
        collection.setHidden(form.isHidden());
        collection.setSharing(form.getSharing());
        collection.setOwnerId(external.getCurrentUser().getUsername());

        // persist the collection update
        mediaService.updateCollection(collection);

        // add an appropriate message indicating collection has been edited successfully
        List<String> messageCodes = new ArrayList<String>();
        messageCodes.add("editCollectionDetails.saved");
        //request.setAttribute("infos", messageCodes);

        // send the user back to the main collections listing
        String forwardUrl = "viewCollection.htm?collectionId=" + collection.getId();
        //request.getRequestDispatcher(forwardUrl).forward(request, response);
        ModelAndView modelAndView = new ModelAndView("forward:"+forwardUrl);
        		
        return modelAndView;

    }

    /*
     * (non-Javadoc)
     * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
     */
    @RequestMapping(method=RequestMethod.GET) 
    protected Object formBackingObject(HttpServletRequest request)
            throws Exception {

        CollectionForm form;
        Map<String,Object> model = new HashMap<String,Object>();

        String collectionId = request.getParameter("collectionId");
        if ( StringUtils.isBlank(collectionId) ) {
            model.put("isAdding", true);
        } else {
            model.put("isAdding", false);
        }

         //make sharing options available to the page
		List<String> sharingOptions = new ArrayList<String>();
		sharingOptions.add(MediaCollection.SHARING_ADMIN);
		sharingOptions.add(MediaCollection.SHARING_PRIVATE);
		sharingOptions.add(MediaCollection.SHARING_PUBLIC);
		sharingOptions.add(MediaCollection.SHARING_SHARED);
		model.put("sharingOptions", sharingOptions); 
        if (!StringUtils.isBlank(collectionId)) {
            MediaCollection collection = mediaService.getCollection(collectionId, 0, 0);
            form = new CollectionForm(collection);
        } else {
            form = new CollectionForm();
        }

        return form;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors)
     */
    /*@SuppressWarnings("rawtypes")
    @Override
    protected Map referenceData(HttpServletRequest request, Object command,
            Errors errors) throws Exception {

        Map<String,Object> model = new HashMap<String,Object>();

        String collectionId = request.getParameter("collectionId");
        if ( StringUtils.isBlank(collectionId) ) {
            model.put("isAdding", true);
        } else {
            model.put("isAdding", false);
        }

         make sharing options available to the page
		List<String> sharingOptions = new ArrayList<String>();
		sharingOptions.add(MediaCollection.SHARING_ADMIN);
		sharingOptions.add(MediaCollection.SHARING_PRIVATE);
		sharingOptions.add(MediaCollection.SHARING_PUBLIC);
		sharingOptions.add(MediaCollection.SHARING_SHARED);
		model.put("sharingOptions", sharingOptions); 

        return model;
    }*/

    /*
     * (non-Javadoc)
     * @see org.springframework.web.servlet.mvc.CancellableFormController#onCancel(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object)
     */
    /*@Override
    protected ModelAndView onCancel(HttpServletRequest request,
            HttpServletResponse response, Object command) throws Exception {

        CollectionForm form = (CollectionForm) command;

        
         *  If this would have been a new collection, send the user to the main
         *  collection listing page.  Otherwise, send the user back to the view page
         *  for the currently-in-edit collection. 
         

        if (StringUtils.isBlank(form.getId())) {
            request.getRequestDispatcher("listCollections.htm").forward(request, response);
        } else {
            request.getRequestDispatcher("viewCollection.htm?collectionId=".concat(form.getId())).forward(request, response);
        }

        return null;
    }*/


    private MediaService mediaService;
    public void setMediaService(MediaService service) {
        this.mediaService = service;
    }

    private ExternalLogic external;
    public void setExternal(ExternalLogic external) {
        this.external = external;
    }

}
