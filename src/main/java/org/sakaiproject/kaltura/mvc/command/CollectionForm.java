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
package org.sakaiproject.kaltura.mvc.command;

import org.sakaiproject.kaltura.model.MediaCollection;

/**
 * CollectionForm represents an editing form for a Kaltura collection's 
 * basic information.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class CollectionForm {

    private String id;
    private String title;
    private String description;
    private String sharing;
    private boolean hidden;

    /**
     * Default constructor
     */
    public CollectionForm() { }

    /**
     * Construct a CollectionForm from an existing MediaCollection.
     * 
     * @param mediaCollection
     */
    public CollectionForm(MediaCollection mediaCollection) {
        this.id = mediaCollection.getIdStr();
        this.title = mediaCollection.getTitle();
        this.description = mediaCollection.getDescription();
        this.hidden = mediaCollection.isHidden();
        this.sharing = mediaCollection.getSharing();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getSharing() {
        return sharing;
    }

    public void setSharing(String sharing) {
        this.sharing = sharing;
    }

}
