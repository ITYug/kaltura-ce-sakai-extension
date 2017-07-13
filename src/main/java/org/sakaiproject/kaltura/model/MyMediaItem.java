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
package org.sakaiproject.kaltura.model;

/**
 * For MyMediaItems, we shouldn't have a database id (MyMedia.id), so we want to project the kaltura id when possible.
 * 
 * @author chasegawa@unicon.net
 * @author azeckoski
 */
public class MyMediaItem extends MediaItem {
    private static final long serialVersionUID = 1L;

    /**
     * Create a new instance of MyMedia with the given locationId, kalturaId and ownerId
     * 
     * @param kalturaId KME id
     * @param ownerId username
     */
    public MyMediaItem(String kalturaId, String ownerId) {
        super(null, kalturaId, ownerId, false, false, true); // not hidden
        indicateUserControl(ownerId, true, false, false); // since we are the owner we can default these (this skips perms checks)
    }

    /**
     * @see org.sakaiproject.kaltura.model.MediaItem#getIdStr()
     */
    @Override
    public String getIdStr() {
        String id = super.getIdStr();
        if (null == id) {
            id = "KID=" + getKalturaId();
        }
        return id;
    }

    /**
     * Always false for this type of item (no permissions to manage on a MyMedia item).
     * 
     * @see org.sakaiproject.kaltura.model.MediaItem#isManage()
     */
    @Override
    public boolean isManage() {
        return false;
    }

    /**
     * MyMedia items are never private (since they are just shown to the owner)
     */
    @Override
    public boolean isHidden() {
        return false;
    }

    /**
     * MyMedia items can always be edited
     */
    @Override
    public boolean isEdit() {
        return true; // can always edit my media
    }

}
