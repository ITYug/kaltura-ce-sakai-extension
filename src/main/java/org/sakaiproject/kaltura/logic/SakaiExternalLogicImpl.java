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

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.NoSuchMessageException;

/**
 * This is the implementation for logic which is external to our app logic
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class SakaiExternalLogicImpl implements ExternalLogic, MessageSourceAware {

    protected final Log log = LogFactory.getLog(getClass());

    private AuthzGroupService authzGroupService;
    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
        this.authzGroupService = authzGroupService;
    }

    private DeveloperHelperService developerHelperService;
    public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
        this.developerHelperService = developerHelperService;
    }

    private FunctionManager functionManager;
    public void setFunctionManager(FunctionManager functionManager) {
        this.functionManager = functionManager;
    }

    private ToolManager toolManager;
    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }

    private SecurityService securityService;
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    private ServerConfigurationService serverConfigurationService;
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    private SessionManager sessionManager;
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    private SiteService siteService;
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private UserDirectoryService userDirectoryService;
    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    private MessageSource messageSource;
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }


    /**
     * Place any code that should run when this class is initialized by spring here
     */
    public void init() {
        log.debug("init");
        // register Sakai permissions for this tool if needed
        List<String> kalturaPerms = functionManager.getRegisteredFunctions(KALTURA_PREFIX);
        registerPermission(PERM_ADMIN, kalturaPerms);
        registerPermission(PERM_MANAGER, kalturaPerms);
        registerPermission(PERM_EDITOR, kalturaPerms);
        registerPermission(PERM_WRITE, kalturaPerms);
        registerPermission(PERM_READ, kalturaPerms);
        registerPermission(PERM_UPLOAD_SPECIAL, kalturaPerms);
        registerPermission(PERM_SHOW_SITE_LIBRARY, kalturaPerms);
        registerPermission(PERM_SHOW_MY_MEDIA, kalturaPerms);
    }

    private void registerPermission(String perm, List<String> kalturaPerms) {
        if (!kalturaPerms.contains(perm)) {
            functionManager.registerFunction(perm);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.kaltura.logic.ExternalLogic#getCurrentToolId()
     */
    public String getCurrentToolId() {

        /*
         * Attempt to get the current tool from the tool manager.  The current
         * tool is tracked via a ThreadLocal and is not accessible via the
         * EntityBroker.
         */
        Placement placement = toolManager.getCurrentPlacement();

        if (placement != null) {
            return placement.getId();
        } else {
            return null;
        }

    }

    public String getCurrentLocationId() {
        String location = null;
        try {
            String context = toolManager.getCurrentPlacement().getContext();
            location = context;
            Site s = siteService.getSite( context );
            location = s.getReference(); // get the entity reference to the site
        } catch (Exception e) {
            // sakai failed to get us a location so we can assume we are not inside the portal
            return NO_LOCATION;
        }
        if (location == null) {
            location = NO_LOCATION;
        }
        return location;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.kaltura.logic.ISessionService#getCurrentUserDisplayName()
     */
    public String getCurrentUserDisplayName() {
        return getUserDisplayName(getCurrentUserId());
    }

    public String getLocationTitle(String locationId) {
        String title = null;
        try {
            Site site = siteService.getSite(locationId);
            title = site.getTitle();
        } catch (IdUnusedException e) {
            log.warn("Cannot get the info about locationId: " + locationId);
            title = "----------";
        }
        return title;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.kaltura.logic.ExternalLogic#getCurrentUserId()
     */
    public String getCurrentUserId() {
        return sessionManager.getCurrentSessionUserId();
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.kaltura.logic.ExternalLogic#getCurrentUserName()
     */
    public String getCurrentUserName() {
        org.sakaiproject.kaltura.logic.User u = this.getUser(this.getCurrentUserId());
        return (u == null ? null : u.getUsername());
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.kaltura.logic.ExternalLogic#getCurrentUser()
     */
    public org.sakaiproject.kaltura.logic.User getCurrentUser() {
        return this.getUser(this.getCurrentUserId());
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.kaltura.logic.ISessionService#getCurrentLocale()
     */
    public Locale getCurrentLocale() {
        return new ResourceLoader().getLocale();
    }

    protected String getUserDisplayName(String userId) {
        String name = null;
        try {
            name = userDirectoryService.getUser(userId).getDisplayName();
        } catch (UserNotDefinedException e) {
            log.warn("Cannot get user displayname for id: " + userId);
            name = "--------";
        }
        return name;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.kaltura.logic.ExternalLogic#getUser(java.lang.String)
     */
    public org.sakaiproject.kaltura.logic.User getUser(String userId) {
        org.sakaiproject.kaltura.logic.User user = null;
        if (userId != null) {
            User u = null;
            try {
                u = userDirectoryService.getUserByEid(userId);
            } catch (UserNotDefinedException e) {
                try {
                    u = userDirectoryService.getUser(userId);
                } catch (UserNotDefinedException e1) {
                    log.warn("Cannot get user for id: " + userId);
                }
            }
            if (u != null) {
                user = new org.sakaiproject.kaltura.logic.User(u.getId(),
                        u.getEid(), u.getDisplayName(), u.getSortName(), u.getEmail());
                user.fname = u.getFirstName();
                user.lname = u.getLastName();
            }
        }
        return user;
    }

    ThreadLocal<SecurityAdvisor> securityAdvisorTL = new ThreadLocal<SecurityAdvisor>();

    /* (non-Javadoc)
     * @see org.sakaiproject.kaltura.logic.ExternalLogic#currentAllowAdminAccess()
     */
    public void currentAllowAdminAccess() {
        if (securityAdvisorTL.get() == null) {
            // only set the advisor if there is not already one
            SecurityAdvisor securityAdvisor = new SecurityAdvisor() {
                public SecurityAdvice isAllowed(String userId, String function, String reference) {
                    if (StringUtils.startsWith(function, KALTURA_PREFIX)) {
                        return SecurityAdvice.ALLOWED;
                    }
                    return SecurityAdvice.PASS;
                }
            };
            securityAdvisorTL.set(securityAdvisor);
        }
        securityService.pushAdvisor( securityAdvisorTL.get() );
        if (StringUtils.isBlank(getCurrentUserId())) {
            // force a current user to the admin if there is not one right now
            String adminUserRef = developerHelperService.getUserRefFromUserId(getAdminUserId());
            developerHelperService.setCurrentUser(adminUserRef);
        }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.kaltura.logic.ExternalLogic#currentRestoreNormalAccess()
     */
    public void currentRestoreNormalAccess() {
        SecurityAdvisor sa = securityAdvisorTL.get();
        if (sa != null) {
            // only pop the advisor when it is found (in case this is called without calling currentAllowAdminAccess)
            securityService.popAdvisor( securityAdvisorTL.get() );
        }
        securityAdvisorTL.remove();
    }

    public boolean isUserAdmin(String userId) {
        return securityService.isSuperUser(userId);
    }

    public String getAdminUserId() {
        return "admin";
    }

    public boolean isUserAllowedInLocation(String userId, String permission, String locationId) {
        // need to convert the location into a ref
        if (locationId != null && ! locationId.startsWith("/")) {
            locationId = "/site/"+locationId; // assume site then
        }
        if ( securityService.unlock(userId, permission, locationId) ) {
            return true;
        }
        return false;
    }

    public boolean checkPerms(String userId, String locationId, String[] perms) {
        // NOTE: we needed to put this here to allow permissions checks from the kaltura API service
        if (perms == null || perms.length <= 0) {
            throw new IllegalArgumentException("perms ("+ArrayUtils.toString(perms)+") must be set");
        }
        if (userId == null || "".equals(userId)) {
            userId = getCurrentUserId();
            if (userId == null || "".equals(userId)) {
                throw new IllegalArgumentException("userId ("+userId+") must be set");
            }
        }
        if (locationId == null || "".equals(locationId)) {
            locationId = getCurrentLocationId();
            if (locationId == null || "".equals(locationId)) {
                throw new IllegalArgumentException("location ("+locationId+") must be set");
            }
        }
        boolean allowed = false;
        if ( isUserAdmin(userId) ) {
            // the system super user can do anything
            allowed = true;
        } else {
            for (int i = 0; i < perms.length; i++) {
                if (perms[i] != null && isUserAllowedInLocation(userId, perms[i], locationId)) {
                    allowed = true;
                    break;
                }
            }
        }
        return allowed;
    }

    /**
     * @return a string identifying the current server
     */
    public String getServerInfo() {
        return serverConfigurationService.getServerName() + "("+serverConfigurationService.getServerId()+")" + "[" + serverConfigurationService.getServerUrl() + "]";
    }

    public String getServerId() {
        return serverConfigurationService.getServerId();
    }

    /**
     * String type: gets the printable name of this server
     */
    protected static final String SETTING_SERVER_NAME = "server.name";

    /**
     * String type: gets the unique id of this server (safe for clustering if used)
     */
    protected static final String SETTING_SERVER_ID = "server.cluster.id";

    /**
     * String type: gets the URL to this server
     */
    protected static final String SETTING_SERVER_URL = "server.main.URL";

    /**
     * String type: gets the URL to the portal on this server (or just returns the server URL if no
     * portal in use)
     */
    protected static final String SETTING_PORTAL_URL = "server.portal.URL";

    /**
     * Boolean type: if true then there will be data preloads and DDL creation, if false then data
     * preloads are disabled (and will cause exceptions if preload data is missing)
     */
    protected static final String SETTING_AUTO_DDL = "auto.ddl";

    /**
     * Retrieves settings from the configuration service (sakai.properties)
     * 
     * @param settingName the name of the setting to retrieve, Should be a string name: e.g. auto.ddl,
     *            mystuff.config, etc. OR one of the SETTING constants (e.g {@link #SETTING_AUTO_DDL})
     * @param defaultValue a specified default value to return if this setting cannot be found, <b>NOTE:</b>
     *            You can set the default value to null but you must specify the class type in parens
     * @return the value of the configuration setting OR the default value if none can be found
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfigurationSetting(String settingName, T defaultValue) {
        T returnValue = defaultValue;
        if (SETTING_SERVER_NAME.equals(settingName)) {
            returnValue = (T) serverConfigurationService.getServerName();
        } else if (SETTING_SERVER_URL.equals(settingName)) {
            returnValue = (T) serverConfigurationService.getServerUrl();
        } else if (SETTING_PORTAL_URL.equals(settingName)) {
            returnValue = (T) serverConfigurationService.getPortalUrl();
        } else if (SETTING_SERVER_ID.equals(settingName)) {
            returnValue = (T) serverConfigurationService.getServerIdInstance();
        } else {
            if (defaultValue == null) {
                returnValue = (T) serverConfigurationService.getString(settingName);
                if ("".equals(returnValue)) {
                    returnValue = null;
                }
            } else {
                if (defaultValue instanceof Number) {
                    int num = ((Number) defaultValue).intValue();
                    int value = serverConfigurationService.getInt(settingName, num);
                    returnValue = (T) Integer.valueOf(value);
                } else if (defaultValue instanceof Boolean) {
                    boolean bool = ((Boolean) defaultValue).booleanValue();
                    boolean value = serverConfigurationService.getBoolean(settingName, bool);
                    returnValue = (T) Boolean.valueOf(value);
                } else if (defaultValue instanceof String) {
                    returnValue = (T) serverConfigurationService.getString(settingName,
                            (String) defaultValue);
                }
            }
        }
        return returnValue;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.kaltura.logic.ExternalLogic#getI18nMessage(java.lang.String, java.lang.Object[])
     */
    public String getI18nMessage(String key, Object[] args) {
        String msg = "";
        if (StringUtils.isBlank(key)) {
            log.warn("Missing i18n key, returning warning message");
            msg = "{ERROR: MISSING KEY}";
        } else {
            try {
                msg = messageSource.getMessage(key, args, null);
            } catch (NoSuchMessageException e) {
                log.warn("Invalid i18n key ("+key+") could not be found, returning warning message");
                msg = "{ERROR: INVALID KEY: "+key+"}";
            }
        }
        return msg;
    }

    /**
     * @return true if the user has the ability to administrate permissions on the site
     */
    public boolean canAdministrateKalturaPermissions() {
        String userId = getCurrentUserId();
        String locationId = getCurrentLocationId();
        return isUserAdmin(userId) || authzGroupService.allowUpdate(locationId);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.kaltura.logic.ExternalLogic#extractSiteId(java.lang.String)
     */
    public String extractSiteId(String locationId) {
        String siteId = locationId;
        if (locationId != null) {
            siteId = locationId.replaceFirst("/site/", "").trim();
        }
        return siteId;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.kaltura.logic.ExternalLogic#fixLocationId(java.lang.String)
     */
    public String fixLocationId(String locationId) {
        String locId = locationId;
        if (locationId != null) {
            if (!locationId.startsWith("/")) {
                locId = "/site/"+locId;
            }
        }
        return locId;
    }

}
