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
package org.sakaiproject.kaltura.logic;

/**
 * ExternalService provides an interface for retrieving information
 * about the current user, location, and permissions from the application
 * container.
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public interface ExternalLogic {

    /**
     * Prefix for kaltura permissions
     */
    public static final String KALTURA_PREFIX = "kaltura.";

    /**
     * No location string (blank means nothing)
     */
    public final static String NO_LOCATION = "";

    /**
     * Kaltura admin/approver permission for a location
     * 
     * Allows user to edit collections and items in them (as collection permissions allow)
     */
    public final static String PERM_ADMIN = KALTURA_PREFIX+"admin";
    /**
     * Kaltura manager/approver permission for a location
     * 
     * Allows the user to edit the permissions on any items (as collection permissions allow)
     */
    public final static String PERM_MANAGER = KALTURA_PREFIX+"manager";
    /**
     * Kaltura editor permission for a location
     * 
     * Allows user to edit the meta data for any items
     */
    public final static String PERM_EDITOR = KALTURA_PREFIX+"editor";
    /**
     * Kaltura write/uploader permission for a location
     * 
     * Allows user to upload content and update the meta data on it (owned items only)
     */
    public final static String PERM_WRITE = KALTURA_PREFIX+"write";
    /**
     * Kaltura read/consumer permission for a location
     * 
     * Allows the user to view any content in a location which is not hidden
     */
    public final static String PERM_READ = KALTURA_PREFIX+"read";
    /**
     * Kaltura upload permission for a location
     * 
     * Allows the user to access custom special uploader (instead of the default one)
     */
    public final static String PERM_UPLOAD_SPECIAL = KALTURA_PREFIX+"uploadSpecial";

    /**
     * Kaltura display permission for a user.
     * 
     * Allows a user to access/see the My Media tab, which shows all the uploaded media for the user across sites.
     */
    public final static String PERM_SHOW_MY_MEDIA = KALTURA_PREFIX+"showMyMedia";

    /**
     * Kaltura upload permission for a location
     * 
     * Allows the user to see the Site library, only if kaltura is configured
     * to require this permission. 
     */
    public final static String PERM_SHOW_SITE_LIBRARY = KALTURA_PREFIX+"showSiteLibrary";

    /**
     * Return the ID of the current Sakai tool, or <code>null</code> if none
     * can be determined. 
     * 
     * @return tool id
     */
    public String getCurrentToolId();

    /**
     * Return a string representing the current location.
     * 
     * @return the reference for the current location (e.g. /site/siteId)
     */
    public String getCurrentLocationId();

    /**
     * Get the title associated with the current location.
     * 
     * @param locationId
     * @return the title of the current location
     */
    public String getLocationTitle(String locationId);

    /**
     * Get system unique id for the currently authenticated user.
     * 
     * @return the internal user id (not the username or display name)
     * @see #getCurrentUserName()
     * @see #getCurrentUser()
     */
    public String getCurrentUserId();

    /**
     * Get system username for the currently authenticated user.
     * 
     * @return the username (not the user id or display name)
     * @see #getCurrentUserId()
     * @see #getCurrentUser()
     */
    public String getCurrentUserName();

    /**
     * Get the current user's display name.
     * 
     * @return the display name for a given user
     * @see #getCurrentUser()
     */
    public String getCurrentUserDisplayName();

    /**
     * Return a User object representing the currently authenticated user.
     * 
     * @return the current user OR null if none
     * @see #getCurrentUserId()
     * @see #getCurrentUserName()
     * @see #getCurrentUserDisplayName()
     */
    public org.sakaiproject.kaltura.logic.User getCurrentUser();

    /**
     * Return a User object representing the user with the 
     * specified system id.
     * 
     * @param userId
     * @return the user object OR null if none can be found with this id
     */
    public org.sakaiproject.kaltura.logic.User getUser(String userId);

    /**
     * Determine whether the currently logged in user is an adminstrator.
     * 
     * @param userId
     * @return true if the user is an admin OR false otherwise
     */
    public boolean isUserAdmin(String userId);

    /**
     * @return a super user admin user id (for archive and migration processing)
     */
    public String getAdminUserId();

    /**
     * Allow the current thread to have admin kaltura access temporarily
     * WARNING: Do NOT call this without also calling {@link #currentRestoreNormalAccess()} after it
     * NOTE: calling this multiple time in a row will have no effect
     */
    public void currentAllowAdminAccess();

    /**
     * Restore the current thread to normal user (maybe no user) access
     * WARNING: Only makes sense if you called {@link #currentAllowAdminAccess()} first
     * NOTE: calling this multiple time in a row will have no effect
     */
    public void currentRestoreNormalAccess();

    /**
     * Determine if a specified user has a particular permission in a
     * particular location.
     * 
     * @param userId
     * @param permission
     * @param locationId
     * @return true if the user has the permission OR false otherwise
     */
    public boolean isUserAllowedInLocation(String userId,
            String permission, String locationId);

    /**
     * Determine if a specified user has one of a set of permissions in a
     * particular location (also checks for super admin and fills in
     * known defaults if params are empty).
     * 
     * @param userId [OPTIONAL] userId or current user if not set
     * @param locationId [OPTIONAL] location (site) id or current site if not set
     * @param perms array of permissions to check
     * @return true if the user has the permission OR false otherwise
     */
    public boolean checkPerms(String userId, String locationId, String[] perms);

    /**
     * @return a string identifying the current server
     */
    public String getServerInfo();

    /**
     * @return a string with the unique id of the current server
     */
    public String getServerId();

    /**
     * Retrieves settings from the configuration service (sakai.properties)
     * 
     * @param settingName the name of the setting to retrieve, Should be a string name: e.g. auto.ddl,
     *            mystuff.config, etc. OR one of the SETTING constants (e.g
     *            {@link #SETTING_AUTO_DDL})
     * @param defaultValue a specified default value to return if this setting cannot be found, <b>NOTE:</b>
     *            You can set the default value to null but you must specify the class type in parens
     * @return the value of the configuration setting OR the default value if none can be found
     */
    public <T> T getConfigurationSetting(String settingName, T defaultValue);

    /**
     * Translation method to convert keys (and arguments) into translated strings
     * @param key the i18n key (e.g. app.title)
     * @param args [OPTIONAL] replacement arguments (null if none are used in the string)
     * @return the translated string (will not be null)
     */
    public String getI18nMessage(String key, Object[] args);

    /**
     * @return true if the user has the ability to administrate permissions on the site
     */
    public boolean canAdministrateKalturaPermissions();

    /**
     * Extract the safe siteId (subset of the locationId) if possible
     * @param locationId unique Sakai realms locationId (e.g. /site/xxxxxxx)
     * @return the siteId as extracted from the location or the input string if it cannot be extracted
     */
    public String extractSiteId(String locationId);

    /**
     * Ensure that locationId is properly formatted
     * @param locationId a siteId or locationId (e.g. /site/xxxxxxx)
     * @return the locationId (e.g. /site/xxxxxxx)
     */
    public String fixLocationId(String locationId);

}