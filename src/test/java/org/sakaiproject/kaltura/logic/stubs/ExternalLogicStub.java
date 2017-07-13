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
package org.sakaiproject.kaltura.logic.stubs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.kaltura.logic.ExternalLogic;
import org.sakaiproject.kaltura.logic.SakaiExternalLogicImpl;
import org.sakaiproject.kaltura.logic.User;

/**
 * Stub class for the external logic impl (for testing)
 * @author azeckoski
 */
public class ExternalLogicStub extends SakaiExternalLogicImpl implements ExternalLogic {

    /**
     * represents the current user userId, can be changed to simulate multiple users 
     */
    public String currentUserId;
    /**
     * represents the current location, can be changed to simulate multiple locations 
     */
    public String currentLocationId;

    /**
     * Config options
     */
    public Map<String, Object> config = new ConcurrentHashMap<String, Object>();

    /**
     * Reset the current user and location to defaults
     */
    public void setDefaults() {
        currentUserId = KalturaAPIServiceStub.USER_ID;
        currentLocationId = KalturaAPIServiceStub.LOCATION1_ID;
    }

    public ExternalLogicStub() {
        setDefaults();
    }

    public String getCurrentToolId() {
        return null;
    }


    /* (non-Javadoc)
     * @see org.sakaiproject.kaltura.logic.ExternalLogic#getCurrentLocationId()
     */
    public String getCurrentLocationId() {
        return currentLocationId;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.kaltura.logic.ExternalLogic#getLocationTitle(java.lang.String)
     */
    public String getLocationTitle(String locationId) {
        if (locationId.equals(KalturaAPIServiceStub.LOCATION1_ID)) {
            return KalturaAPIServiceStub.LOCATION1_TITLE;
        } else if (locationId.equals(KalturaAPIServiceStub.LOCATION2_ID)) {
            return KalturaAPIServiceStub.LOCATION2_TITLE;
        }
        return "--------";
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.kaltura.logic.ExternalLogic#getCurrentUserId()
     */
    public String getCurrentUserId() {
        return currentUserId;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.kaltura.logic.ExternalLogic#getUserDisplayName(java.lang.String)
     */
    public String getUserDisplayName(String userId) {
        if (userId.equals(KalturaAPIServiceStub.USER_ID)) {
            return KalturaAPIServiceStub.USER_DISPLAY;
        } else if (userId.equals(KalturaAPIServiceStub.ACCESS_USER_ID)) {
            return KalturaAPIServiceStub.ACCESS_USER_DISPLAY;
        } else if (userId.equals(KalturaAPIServiceStub.ACCESS_USER_ID_W)) {
            return KalturaAPIServiceStub.ACCESS_USER_DISPLAY_W;
        } else if (userId.equals(KalturaAPIServiceStub.ACCESS_USER_ID_E)) {
            return KalturaAPIServiceStub.ACCESS_USER_DISPLAY_E;
        } else if (userId.equals(KalturaAPIServiceStub.MAINT_USER_ID)) {
            return KalturaAPIServiceStub.MAINT_USER_DISPLAY;
        } else if (userId.equals(KalturaAPIServiceStub.MAINT_USER_ID_A)) {
            return KalturaAPIServiceStub.MAINT_USER_DISPLAY_A;
        } else if (userId.equals(KalturaAPIServiceStub.MAINT_USER_ID_M)) {
            return KalturaAPIServiceStub.MAINT_USER_DISPLAY_M;
        } else if (userId.equals(KalturaAPIServiceStub.ADMIN_USER_ID)) {
            return KalturaAPIServiceStub.ADMIN_USER_DISPLAY;
        }
        return "----------";
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.kaltura.logic.ExternalLogic#isUserAdmin(java.lang.String)
     */
    public boolean isUserAdmin(String userId) {
        if (userId.equals(KalturaAPIServiceStub.ADMIN_USER_ID)) {
            return true;
        }
        return false;
    }

    String tempUser = null;
    @Override
    public void currentAllowAdminAccess() {
        if (tempUser == null) {
            tempUser = currentUserId;
            currentUserId = KalturaAPIServiceStub.ADMIN_USER_ID;
        }
    }

    @Override
    public void currentRestoreNormalAccess() {
        if (tempUser != null) {
            currentUserId = tempUser;
            tempUser = null;
        }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.kaltura.logic.ExternalLogic#isUserAllowedInLocation(java.lang.String, java.lang.String, java.lang.String)
     */
    public boolean isUserAllowedInLocation(String userId, String permission, String locationId) {
        if (userId.equals(KalturaAPIServiceStub.USER_ID)) {
            if (locationId.equals(KalturaAPIServiceStub.LOCATION1_ID)) {
                return false;
            }
        } else if (userId.equals(KalturaAPIServiceStub.ACCESS_USER_ID)) {
            if (locationId.equals(KalturaAPIServiceStub.LOCATION1_ID)) {
                if (permission.equals(PERM_READ)) {
                    return true;
                }
            }
        } else if (userId.equals(KalturaAPIServiceStub.ACCESS_USER_ID_W)) {
            if (locationId.equals(KalturaAPIServiceStub.LOCATION1_ID)) {
                if (permission.equals(PERM_WRITE) ) {
                    return true;
                }
            }
        } else if (userId.equals(KalturaAPIServiceStub.ACCESS_USER_ID_E)) {
            if (locationId.equals(KalturaAPIServiceStub.LOCATION1_ID)) {
                if (permission.equals(PERM_EDITOR) ) {
                    return true;
                }
            }
        } else if (userId.equals(KalturaAPIServiceStub.MAINT_USER_ID)) {
            if (locationId.equals(KalturaAPIServiceStub.LOCATION1_ID)) {
                if (permission.equals(PERM_ADMIN) 
                        || permission.equals(PERM_MANAGER)
                        || permission.equals(PERM_EDITOR)
                        || permission.equals(PERM_WRITE)
                        || permission.equals(PERM_READ)) {
                    return true;
                }
            }
        } else if (userId.equals(KalturaAPIServiceStub.MAINT_USER_ID_A)) {
            if (locationId.equals(KalturaAPIServiceStub.LOCATION1_ID)) {
                if (permission.equals(PERM_ADMIN)) {
                    return true;
                }
            }
        } else if (userId.equals(KalturaAPIServiceStub.MAINT_USER_ID_M)) {
            if (locationId.equals(KalturaAPIServiceStub.LOCATION1_ID)) {
                if (permission.equals(PERM_MANAGER)) {
                    return true;
                }
            }
        } else if (userId.equals(KalturaAPIServiceStub.ADMIN_USER_ID)) {
            // admin can do anything in any context
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public <T> T getConfigurationSetting(String settingName, T defaultValue) {
        T result = defaultValue;
        if (config.containsKey(settingName)) {
            result = (T) config.get(settingName);
        }
        return result;
    }

    public User getCurrentUser() {
        return getUser(currentUserId);
    }

    public String getCurrentUserDisplayName() {
        return getUser(currentUserId).getName();
    }

    public String getCurrentUserName() {
        return getUser(currentUserId).getUsername();
    }

    public String getLocation(HttpServletRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

    public User getUser(String userId) {
        User user = null;
        if (userId.equals(KalturaAPIServiceStub.USER_ID)) {
            user = new User(userId, KalturaAPIServiceStub.USER_NAME, KalturaAPIServiceStub.USER_DISPLAY);
        } else if (userId.equals(KalturaAPIServiceStub.ACCESS_USER_ID)) {
            user = new User(userId, KalturaAPIServiceStub.ACCESS_USER_NAME, KalturaAPIServiceStub.ACCESS_USER_DISPLAY);
        } else if (userId.equals(KalturaAPIServiceStub.ACCESS_USER_ID_W)) {
            user = new User(userId, KalturaAPIServiceStub.ACCESS_USER_NAME_W, KalturaAPIServiceStub.ACCESS_USER_DISPLAY_W);
        } else if (userId.equals(KalturaAPIServiceStub.ACCESS_USER_ID_E)) {
            user = new User(userId, KalturaAPIServiceStub.ACCESS_USER_NAME_E, KalturaAPIServiceStub.ACCESS_USER_DISPLAY_E);
        } else if (userId.equals(KalturaAPIServiceStub.MAINT_USER_ID)) {
            user = new User(userId, KalturaAPIServiceStub.MAINT_USER_NAME, KalturaAPIServiceStub.MAINT_USER_DISPLAY);
        } else if (userId.equals(KalturaAPIServiceStub.MAINT_USER_ID_A)) {
            user = new User(userId, KalturaAPIServiceStub.MAINT_USER_NAME_A, KalturaAPIServiceStub.MAINT_USER_DISPLAY_A);
        } else if (userId.equals(KalturaAPIServiceStub.MAINT_USER_ID_M)) {
            user = new User(userId, KalturaAPIServiceStub.MAINT_USER_NAME_M, KalturaAPIServiceStub.MAINT_USER_DISPLAY_M);
        } else if (userId.equals(KalturaAPIServiceStub.ADMIN_USER_ID)) {
            user = new User(userId, KalturaAPIServiceStub.ADMIN_USER_NAME, KalturaAPIServiceStub.ADMIN_USER_DISPLAY);
        }
        return user;
    }

    public String getI18nMessage(String key, Object[] args) {
        return "TEST: "+key;
    }

    public boolean canAdministrateKalturaPermissions() {
        return false;
    }

    public String getServerInfo() {
        return "MOCK SERVER INFO";
    }

    public String getServerId() {
        return "MOCK SERVER ID";
    }

}
