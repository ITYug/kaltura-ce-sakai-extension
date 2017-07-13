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
package org.sakaiproject.kaltura.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.kaltura.model.MediaCollection;

/**
 * This checks and preloads any data that is needed for this app
 * @author Sakai App Builder -AZ
 * @deprecated do not use this class for anything other than migration
 */
public class PreloadDataImpl {

	private static Log log = LogFactory.getLog(PreloadDataImpl.class);

	private KalturaDao dao;
	public void setDao(KalturaDao dao) {
		this.dao = dao;
	}

	public void init() {
		preloadItems();
	}

	/**
	 * Preload some items into the database
	 */
	public void preloadItems() {
	    long count = dao.countAll(MediaCollection.class);
	    if (log.isDebugEnabled()) {
	        log.debug("Check for existing collections: "+count);
	    }
/*
		// check if there are any items present, load some if not
		if(dao.findAll(SampleItem.class).isEmpty()){

			// use the dao to preload some data here
			dao.save( new SampleItem("Preload Title", 
					"Preload Owner", "Preload Site", Boolean.TRUE, new Date()) );

			log.info("Preloaded " + dao.countAll(SampleItem.class) + " items");
		}
*/
	}

}
