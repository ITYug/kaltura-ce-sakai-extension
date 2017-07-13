package org.sakaiproject.kaltura.model;
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
public class MyMediaCollectionItem {
    private boolean containsMediaItem;
    private String id;
    private String name;
    private String shortName;
    private boolean userHasWriteAccess;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public boolean isContainsMediaItem() {
        return containsMediaItem;
    }

    public boolean isUserHasWriteAccess() {
        return userHasWriteAccess;
    }

    public void setContainsMediaItem(boolean containsMediaItem) {
        this.containsMediaItem = containsMediaItem;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public void setUserHasWriteAccess(boolean userHasWriteAccess) {
        this.userHasWriteAccess = userHasWriteAccess;
    }
}
