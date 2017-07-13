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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.kaltura.aspectj.ProfilerControl;
import org.sakaiproject.kaltura.aspectj.ProfilerControl.NoProfile;
import org.sakaiproject.kaltura.model.MediaCollection;
import org.sakaiproject.kaltura.model.MediaItem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.kaltura.client.KalturaApiException;
import com.kaltura.client.KalturaClient;
import com.kaltura.client.KalturaConfiguration;
import com.kaltura.client.KalturaFile;
import com.kaltura.client.enums.KalturaCategoryOrderBy;
import com.kaltura.client.enums.KalturaEntryStatus;
import com.kaltura.client.enums.KalturaEntryType;
import com.kaltura.client.enums.KalturaMediaType;
import com.kaltura.client.enums.KalturaMetadataObjectType;
import com.kaltura.client.enums.KalturaMetadataProfileCreateMode;
import com.kaltura.client.enums.KalturaPermissionStatus;
import com.kaltura.client.enums.KalturaPlaylistType;
import com.kaltura.client.enums.KalturaSessionType;
import com.kaltura.client.services.KalturaCategoryEntryService;
import com.kaltura.client.services.KalturaCategoryService;
import com.kaltura.client.services.KalturaMediaService;
import com.kaltura.client.services.KalturaMetadataProfileService;
import com.kaltura.client.services.KalturaMetadataService;
import com.kaltura.client.services.KalturaPlaylistService;
import com.kaltura.client.types.KalturaCategory;
import com.kaltura.client.types.KalturaCategoryEntry;
import com.kaltura.client.types.KalturaCategoryEntryFilter;
import com.kaltura.client.types.KalturaCategoryEntryListResponse;
import com.kaltura.client.types.KalturaCategoryFilter;
import com.kaltura.client.types.KalturaCategoryListResponse;
import com.kaltura.client.types.KalturaClipAttributes;
import com.kaltura.client.types.KalturaEntryResource;
import com.kaltura.client.types.KalturaFilterPager;
import com.kaltura.client.types.KalturaMediaEntry;
import com.kaltura.client.types.KalturaMediaEntryFilter;
import com.kaltura.client.types.KalturaMediaListResponse;
import com.kaltura.client.types.KalturaMetadata;
import com.kaltura.client.types.KalturaMetadataFilter;
import com.kaltura.client.types.KalturaMetadataProfile;
import com.kaltura.client.types.KalturaMetadataProfileFilter;
import com.kaltura.client.types.KalturaOperationAttributes;
import com.kaltura.client.types.KalturaOperationResource;
import com.kaltura.client.types.KalturaPermission;
import com.kaltura.client.types.KalturaPlaylist;
import com.kaltura.client.types.KalturaPlaylistFilter;
import com.kaltura.client.types.KalturaPlaylistListResponse;

/**
 * This is the integration with the kaltura APIs and any code necessary
 * to make them easier to work with for our specific Sakai use cases
 * 
 * @author azeckoski@unicon.net - Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class KalturaAPIService {

    private static final String METADATA_PLUGIN_PERMISSION = "METADATA_PLUGIN_PERMISSION";
    private static final String KALTURA_FEATURE_CLIP_MEDIA = "FEATURE_CLIP_MEDIA";
    private static final String METADATA_CONTAINER_ID = "containerId";
    private static final String METADATA_CONTAINER_TYPE = "containerType";
    private static final String METADATA_OWNER = MediaItem.METADATA_OWNER;
    private static final String METADATA_HIDDEN = MediaItem.METADATA_HIDDEN;
    private static final String METADATA_REUSABLE = MediaItem.METADATA_REUSABLE;
    private static final String METADATA_REMIXABLE = MediaItem.METADATA_REMIXABLE;

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
    public static final String KS_PERM_DOWNLOAD = "ks_download";
    public static final String KS_PERM_EDIT = "ks_edit";
    public static final String KS_PERM_LIST = "ks_list";
    public static final String DEFAULT_PLAYLIST_EMPTY = "__null_string__";

    /** used as the default kaltura owner */
    public static final String DEFAULT_OWNER_ID = "admin";

    public static final int MAX_ENTRIES_PER_REQUEST = 500;
    public static final int MAX_ENTRIES_FROM_KALTURA = 10000;
    private static final String DEFAULT_PLAYER_ID = "7473521"; //"1522202";

    //default metadata field values
    private static final String DEFAULT_METADATA_TITLE = "";
    private static final String DEFAULT_METADATA_TYPE = KalturaMediaType.VIDEO.name();
    private static final String DEFAULT_METADATA_OWNER = DEFAULT_OWNER_ID;
    private static final String DEFAULT_METADATA_REUSABLE = "s";
    private static final String DEFAULT_METADATA_REMIXABLE = "r";
    private static final String DEFAULT_METADATA_HIDDEN = "H";
    protected static final String DEFAULT_METADATA_HIDDEN_ADMIN = "h";

    //default metadata profile names
    private static final String METADATA_PROFILE_NAME_PLAYLIST = "playlist";
    private static final String METADATA_PROFILE_NAME_ENTRY = "entry";

    //default metadata XSD file names
    private static final String METADATA_PROFILE_XSD_PLAYLIST = "PlaylistMetadata.xsd";
    private static final String METADATA_PROFILE_XSD_ENTRY = "EntryMetadata.xsd";

    // default metadata permission container types
    public static final String METADATA_PERMISSIONS_CONTAINER_TYPE_SITE = "site";
    public static final String METADATA_PERMISSIONS_CONTAINER_TYPE_PLAYLIST = "playlist";

    /**
     * SPECIAL permissions - gives all permissions for the user KS
     */
    static final String KS_PERM_ALL = "ks_all_perms";
    /**
     * SPECIAL permissions - uses the super admin KS
     * DO NOT USE this KS for anything where the user can see the KS
     */
    static final String KS_PERM_ADMIN = "ks_admin";

    private static Log log = LogFactory.getLog(KalturaAPIService.class);

    private volatile boolean kalturaInitialized = false;
    private volatile boolean kalturaInitInProgress = true; // start out doing init
    private volatile String kalturaDisabledReason = null;

    boolean kalturaEnabled = false;
    boolean kalturaConfigured = false;
    private boolean offlineMode = false;
    boolean kalturaClippingEnabled = false;
    boolean useXSLTMetadataUpdate = false;

    KalturaConfiguration kalturaConfig;
    String kalturaCDN = null;
    String kalturaRootCategory = ROOT_CATEGORY;
    /*
     * The kaltura widget ids from config
     */
    String kalturaPlayerIdImage = null;
    String kalturaPlayerIdAudio = null;
    String kalturaPlayerIdView = null;
    String kalturaPlayerIdEdit = null;
    String kalturaEditorId = null;
    String kalturaClipperId = null;
    String kalturaClipperPlayerId = null;
    String kalturaUploaderId = null;
    String kalturaUploaderSpecialId = null;

    String kalturaHtml5PlayerJS = null;

    int defaultWidgetWidth = 480;
    int defaultWidgetHeight = 360;

    int kalturaPlayerImageWidth = this.defaultWidgetWidth;
    int kalturaPlayerImageHeight = this.defaultWidgetHeight;
    int kalturaPlayerAudioWidth = this.defaultWidgetWidth;
    int kalturaPlayerAudioHeight = 30;
    int kalturaPlayerVideoWidth = this.defaultWidgetWidth;
    int kalturaPlayerVideoHeight = this.defaultWidgetHeight;

    boolean kalturaHtml5PlayerEnabled = false;

    private ExternalLogic external;
    private Ehcache entriesCache;
    private Ehcache categoriesCache;

    /* Sample kaltura sakai.properties settings
   kaltura.enabled=true
   kaltura.partnerid=111111
   kaltura.adminsecret=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   kaltura.secret=YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY
   kaltura.endpoint=http://www.kaltura.com
     */

    private KalturaMetadataProfile playlistMetadataProfile = null;
    private KalturaMetadataProfile entryMetadataProfile = null;
    private Map<String, Map<String, String>> initialMetadataFields = new HashMap<String, Map<String, String>>(2);

    public void init() {
        log.info("INIT");
        MediaService.setAdminInternal(true);
        try {

            // load up the config
            this.kalturaEnabled = external.getConfigurationSetting("kaltura.enabled", false);
            if (!this.kalturaEnabled) {
                this.kalturaDisabledReason = "Kaltura Sakai integration disabled by configuration. Use kaltura.enabled=true to turn on the tool.";
            }
            int kalturaPartnerId = external.getConfigurationSetting("kaltura.partnerid", 0);
            String kalturaSecret = external.getConfigurationSetting("kaltura.secret", "");
            String kalturaAdminSecret = external.getConfigurationSetting("kaltura.adminsecret", "");

            this.kalturaConfigured = (kalturaPartnerId > 0) && !StringUtils.isBlank(kalturaSecret) && !StringUtils.isBlank(kalturaAdminSecret);
            if (!kalturaConfigured) {
                this.kalturaDisabledReason = "Kaltura Sakai integration is not configured correctly! Please see the README file with the plugin for details.";
                log.error(this.kalturaDisabledReason);
                this.kalturaEnabled = false; // disable things
                this.offlineMode = true;
            }

            String kalturaEndpoint = external.getConfigurationSetting("kaltura.endpoint", "http://www.kaltura.com");
            this.kalturaCDN = external.getConfigurationSetting("kaltura.cdn", "http://cdn.kaltura.com");

            // See README.txt (not the user facing readme) for details -AZ
            boolean kalturaUTF8Testing = false; // LEAVE this false unless you know what you are doing -AZ
            if (kalturaUTF8Testing) {
                kalturaEndpoint = "http://lbd.kaltura.com";
            }

            // supports customizing the look and feel AND functionality of the kaltura widgets
            this.kalturaPlayerIdImage = external.getConfigurationSetting("kaltura.player.image", "7473501"); //"2162571");
            this.kalturaPlayerIdAudio = external.getConfigurationSetting("kaltura.player.audio", "7473511"); //"2158531");
            this.kalturaPlayerIdView = external.getConfigurationSetting("kaltura.player.view", DEFAULT_PLAYER_ID);
            this.kalturaPlayerIdEdit = external.getConfigurationSetting("kaltura.player.edit", null);
            if (this.kalturaPlayerIdEdit == null) {
                this.kalturaPlayerIdEdit = "7502731"; //"1522362";
            } else if ("".equals(this.kalturaPlayerIdEdit)) {
                this.kalturaPlayerIdEdit = this.kalturaPlayerIdView;
            }
            this.kalturaUploaderId = external.getConfigurationSetting("kaltura.uploader", "11601051"); //"5612211"); //"2162581");
            this.kalturaClipperId = external.getConfigurationSetting("kaltura.clipper", "8425401");
            this.kalturaClipperPlayerId = external.getConfigurationSetting("kaltura.clipper.player", "20461781"); // https://jira.sakaiproject.org/browse/SKE-98
            this.kalturaEditorId = external.getConfigurationSetting("kaltura.editor", "2733871"); //"2011401");//"47400");
            this.kalturaUploaderSpecialId = external.getConfigurationSetting("kaltura.uploader.special", null); // "6125721");
            /* DEFAULT set as change by Gonen (Kaltura) on 8-12 Mar 2012, previously confirmed by Nir (Kaltura) on 21 Sept 2010 @ 2300
             * NEW KCW UIconf (SKE-160) from Gonen on 20 Jan 2013 - 11601051
                kaltura.player.image  - 7473501
                kaltura.player.audio  - 7473511
                kaltura.player.view  - 7473521
                kaltura.player.edit  - 7502731
                kaltura.uploader - 11601051
                kaltura.clipper - 8425401
             */

            // allows for config of the sizes of the players
            this.kalturaPlayerImageWidth = external.getConfigurationSetting("kaltura.player.image.width", this.kalturaPlayerImageWidth);
            this.kalturaPlayerImageHeight = external.getConfigurationSetting("kaltura.player.image.height", this.kalturaPlayerImageHeight);
            this.kalturaPlayerAudioWidth = external.getConfigurationSetting("kaltura.player.audio.width", this.kalturaPlayerAudioWidth);
            this.kalturaPlayerAudioHeight = external.getConfigurationSetting("kaltura.player.audio.height", this.kalturaPlayerAudioHeight);
            this.kalturaPlayerVideoWidth = external.getConfigurationSetting("kaltura.player.view.width", this.kalturaPlayerVideoWidth);
            this.kalturaPlayerVideoHeight = external.getConfigurationSetting("kaltura.player.view.height", this.kalturaPlayerVideoHeight);
            this.kalturaClippingEnabled = external.getConfigurationSetting("kaltura.clipping.enabled", this.kalturaClippingEnabled);
            // set the default clipping (remix) permission for new media items
            MediaItem.KALTURA_CLIPPING_DEFAULT_ALLOWED = external.getConfigurationSetting("kaltura.clipping.default.allowed", false);

            // html5 config
            this.kalturaHtml5PlayerEnabled = external.getConfigurationSetting("kaltura.player.html5.enabled", this.kalturaHtml5PlayerEnabled);
            this.kalturaHtml5PlayerJS = external.getConfigurationSetting("kaltura.player.html5.js", this.kalturaHtml5PlayerJS);

            this.useXSLTMetadataUpdate = !external.getConfigurationSetting("kaltura.old.xml.metdata.update", false);
            if (!this.useXSLTMetadataUpdate) { log.info("Using the OLD XML metadata updates processor"); }

            this.kalturaConfig = new KalturaConfiguration();
            this.kalturaConfig.setPartnerId(kalturaPartnerId);
            this.kalturaConfig.setSecret(kalturaSecret);
            this.kalturaConfig.setAdminSecret(kalturaAdminSecret);
            this.kalturaConfig.setEndpoint(kalturaEndpoint);
            if (kalturaUTF8Testing) {
                this.kalturaConfig.setClientTag("Sakai-Testing-UTF8");
            } else {
                this.kalturaConfig.setClientTag(this.kalturaConfig.getClientTag() + " sakai " + MediaService.APP_VERSION);
            }

            // flush the cache
            entriesCache.flush();
            categoriesCache.flush();

            // category config
            this.kalturaRootCategory = makeSafeCategoryName(external.getConfigurationSetting("kaltura.rootCategory", this.kalturaRootCategory));
            initKalturaServer(true);

        } finally {
            MediaService.setAdminInternal(false);
        }
    }

    /**
     * @see #initKalturaServer(boolean)
     */
    private void initKalturaServer() {
        initKalturaServer(false);
    }

    /**
     * Attempt to init the kaltura service and the connection to the kaltura server
     * TODO need to figure out a way to run this before every method and to abort or allow them to continue if things are not or are initialized
     * 
     * @param forceInitWhenInProgress if true, this will run the init even if it says it is already in progress (ONLY use this from the spring init block)
     */
    private void initKalturaServer(boolean forceInitWhenInProgress) {
        if (this.kalturaInitialized) {
            if (log.isDebugEnabled()) log.debug("KalturaAPI INIT kaltura server connection is already initialized");
        } else {
            if (log.isDebugEnabled()) log.debug("KalturaAPI INIT kaltura server not yet initialized, doing init check");
            boolean initCheck = this.kalturaEnabled
                    && !this.offlineMode 
                    && ( !this.kalturaInitInProgress 
                            || (this.kalturaInitInProgress && forceInitWhenInProgress) );
            if (initCheck) {
                synchronized(KalturaAPIService.class) {
                    this.kalturaInitInProgress = true;
                    if (log.isDebugEnabled()) log.debug("KalturaAPI INIT kaltura server not yet initialized, passed init check, starting init...");
                    // try to initialize the server connection
                    try {
                        int kalturaPartnerId = this.kalturaConfig.getPartnerId();
                        // check for the metadata service feature
                        boolean metadataEnabled = false;
                        KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
                        try {
                            KalturaPermission perm = kc.getPermissionService().get(METADATA_PLUGIN_PERMISSION);
                            metadataEnabled = (perm.status == KalturaPermissionStatus.ACTIVE);
                            log.info("Kaltura metadata feature permission ("+METADATA_PLUGIN_PERMISSION+") set to ("+perm.status+"), this MUST be active/enabled");
                        } catch (Exception ex) {
                            // permission does not exist
                            metadataEnabled = false;
                            log.warn("Kaltura metadata feature permission ("+METADATA_PLUGIN_PERMISSION+") not found - kaltura tool cannot start (partner="+kalturaPartnerId+", ks="+kc.getSessionId()+"): "+ex);
                        }
                        if (!metadataEnabled) {
                            String message = "Kaltura metadata feature permission ("+METADATA_PLUGIN_PERMISSION+") is not enabled, it is required for the Kaltura Sakai integration. Please contact Kaltura support or enable this feature on your Kaltura server.";
                            log.error(message+" (partner="+kalturaPartnerId+", ks="+kc.getSessionId()+")");
                            throw new IllegalStateException(message);
                        }

                        // create the root category as needed
                        getRootCategory();

                        // get the playlist metadata profile and metadata fields
                        playlistMetadataProfile = getOrAddKalturaMetadataProfile(KalturaPlaylist.class, METADATA_PROFILE_XSD_PLAYLIST, METADATA_PROFILE_NAME_PLAYLIST, KalturaMetadataObjectType.ENTRY);
                        initialMetadataFields.put(METADATA_PROFILE_NAME_PLAYLIST, initializeMetadataFields(METADATA_PROFILE_NAME_PLAYLIST));
                        // get the entry metadata profile and metadata fields
                        entryMetadataProfile = getOrAddKalturaMetadataProfile(KalturaMediaEntry.class, METADATA_PROFILE_XSD_ENTRY, METADATA_PROFILE_NAME_ENTRY, KalturaMetadataObjectType.ENTRY);
                        initialMetadataFields.put(METADATA_PROFILE_NAME_ENTRY, initializeMetadataFields(METADATA_PROFILE_NAME_ENTRY));

                        // check if clipping allowed on this account
                        try {
                            KalturaPermission perm = kc.getPermissionService().get(KALTURA_FEATURE_CLIP_MEDIA);
                            this.kalturaClippingEnabled = (perm.status == KalturaPermissionStatus.ACTIVE);
                            log.info("Kaltura clipping feature permission ("+KALTURA_FEATURE_CLIP_MEDIA+") set to ("+perm.status+"), clipping enabled = "+this.kalturaClippingEnabled);
                        } catch (Exception ex) {
                            // permission does not exist
                            this.kalturaClippingEnabled = false;
                            MediaItem.KALTURA_CLIPPING_DEFAULT_ALLOWED = false; // force default to false
                            log.warn("Kaltura clipping feature permission ("+KALTURA_FEATURE_CLIP_MEDIA+") not found - disabling clipping (partner="+kalturaPartnerId+", ks="+kc.getSessionId()+"): "+ex);
                        }

                        // all is OK
                        this.kalturaInitialized = true;
                        this.kalturaDisabledReason = null;
                        log.info("KalturaAPI INIT completed successfully");
                    } catch (RuntimeException e) {
                        // trap the exception and flag everything as not working
                        this.kalturaDisabledReason = e.getMessage();
                        this.kalturaInitialized = false;
                        log.error("KalturaAPI INIT failed (SKE tool will not work until init succeeds): "+this.kalturaDisabledReason);
                    } finally {
                        this.kalturaInitInProgress = false;
                    }
                } // synchronized
            } else {
                if (this.offlineMode) {
                    // offline mode
                    this.kalturaEnabled = false;
                    log.warn("OFFLINE MODE enabled! No connection to the kaltura server will be used and only mock data will be generated while this is activated!");
                } else {
                    log.debug("KalturaAPI INIT skipped (check failed): enabled="+this.kalturaEnabled+", offline="+this.offlineMode
                            +", inProgress="+this.kalturaInitInProgress+", force="+forceInitWhenInProgress+" : check="+initCheck);
                }
            }
        }
    }

    public void destroy() {
    }


    public void testWSInit() {
        this.offlineMode = false;
        init();
        this.kalturaEnabled = true;
    }

    // Kaltura service methods

    @NoProfile
    public void setExternal(ExternalLogic external) {
        this.external = external;
    }
    @NoProfile
    public ExternalLogic getExternal() {
        return external;
    }

    @NoProfile
    public void setEntriesCache(Ehcache entriesCache) {
        this.entriesCache = entriesCache;
    }
    @NoProfile
    public Ehcache getEntriesCache() {
        return entriesCache;
    }

    @NoProfile
    public void setCategoriesCache(Ehcache categoriesCache) {
        this.categoriesCache = categoriesCache;
    }
    @NoProfile
    public Ehcache getCategoriesCache() {
        return categoriesCache;
    }


    @NoProfile
    public boolean isKalturaEnabled() {
        return this.kalturaEnabled;
    }
    @NoProfile
    public boolean isKalturaConfigured() {
        return kalturaConfigured;
    }
    /**
     * @return true if kaltura tool is successfully initialized
     */
    @NoProfile
    public boolean isKalturaInitialized() {
        return kalturaInitialized;
    }
    /**
     * @return the reason why kaltura is not initialized (falure message usually) or NULL if there is no reason
     */
    @NoProfile
    public String getKalturaDisabledReason() {
        return kalturaDisabledReason;
    }

    @NoProfile
    public KalturaConfiguration getKalturaConfig() {
        return kalturaConfig;
    }

    @NoProfile
    public void setOffline(boolean offlineMode) {
        this.offlineMode = offlineMode;
    }

    /**
     * This check determines if we are in offline mode due to a failure to initialize (always in offline mode if we are not) or
     * if we have manually been set to offline mode (for testing and such). 
     * @return true if offline
     */
    @NoProfile
    public boolean isOfflineMode() {
        // always true if not initialized
        return this.kalturaInitialized ? this.offlineMode : true;
    }

    @NoProfile
    public String getKalturaCDN() {
        return kalturaCDN;
    }

    @NoProfile
    public KalturaMetadataProfile getPlaylistMetadataProfile() {
        return playlistMetadataProfile;
    }

    @NoProfile
    public KalturaMetadataProfile getEntryMetadataProfile() {
        return entryMetadataProfile;
    }

    /**
     * method to get a kaltura client (default permissions only)
     * @return the current kaltura client for this thread
     */
    @NoProfile
    public KalturaClient getKalturaClient() {
        return getKalturaClient(null);
    }

    /**
     * getKalturaClient returns a KalturaClient with the appropriate Kaltura Session id (ks)
     * for the current user, site, and permission request.
     * @param permission - has three values: sview, download, and edit.  These permissions control
     * how the media player is configured.
     * @return a configured KalturaClient, which contains a valid session ID (ks), given the
     * permissions requested
     */
    public KalturaClient getKalturaClient(String permission) {
        //if (log.isDebugEnabled()) log.debug("getKalturaClient(permission="+permission+")");
        // defaults
        String userKey = "anonymous";
        KalturaSessionType sessionType = KalturaSessionType.USER;

        User currentUser = external.getCurrentUser();
        if (currentUser != null || MediaService.checkAdminInternal()) {
            //String userId = "0";
            if (currentUser != null) {
                userKey = currentUser.username;
                //userId = currentUser.userId;
            } else if (MediaService.checkAdminInternal()) {
                userKey = DEFAULT_OWNER_ID;
            }
            if (KS_PERM_ADMIN.equals(permission) || MediaService.checkAdminInternal()) {
                // if admin permission is specially requested then we make this KS an admin KS
                sessionType = KalturaSessionType.ADMIN;
                permission = "";
                /* NOTE: we will assume the perms have been properly checked before the KS is requested
            } else if (isKalturaAdmin(userId, external.getCurrentLocationId())) {
                // the kaltura admin needs all permissions at all times
                permission = KS_PERM_ALL;
                 */
            }
            // ensure the kaltura server connection has been initialized
            initKalturaServer();
        }
        KalturaClient kc = makeKalturaClient(userKey, sessionType, 0, permission);
        return kc;
    }

    /**
     * destroys the current kaltura client
     */
    @NoProfile
    public void clearKalturaClient() {
        // not using the TL anymore
        //kctl.remove();

        // for profiling
        ProfilerControl.initSMPs();
    }

    /**
     * NOTE: this method will generate a new kaltura client, 
     * make sure you store this into the {@link #kctl} threadlocal if you are generating it using this method
     */
    private KalturaClient makeKalturaClient(String userKey, KalturaSessionType sessionType, int timeoutSecs, String permission) {
        //if (log.isDebugEnabled()) log.debug("makeKalturaClient(userKey="+userKey+", sessionType="+sessionType+", timeoutSecs="+timeoutSecs+", permission="+permission+")");
        // client is not threadsafe
        if (timeoutSecs <= 0) {
            timeoutSecs = 86400; // set to 24 hours by request of kaltura   60; // default to 60 seconds
        }
        KalturaClient kalturaClient = new KalturaClient(this.kalturaConfig);

        // adminsecret is always used to create the encrypted ks
        String secret = this.kalturaConfig.getAdminSecret();

        String combinedPermission = "";
        if (! KalturaSessionType.ADMIN.equals(sessionType)) {
            // generate permissions string for non-admin KS.  Kaltura supports sending multiple permission in a single ks
            List<String> permissions = new ArrayList<String>();
            permissions.add("sview:*");
            if (KS_PERM_LIST.equals(permission) 
                    || KS_PERM_ALL.equals(permission)) {
                permissions.add("list:*");
            }
            if (KS_PERM_EDIT.equals(permission) 
                    || KS_PERM_ALL.equals(permission)) {
                permissions.add("edit:*");
            }
            if (KS_PERM_DOWNLOAD.equals(permission)
                    || KS_PERM_ALL.equals(permission)) {
                permissions.add("download:*");
            }
            combinedPermission = StringUtils.join(permissions.toArray(), ",");
        }

        try {
            String sessionId = kalturaClient.generateSession(secret, userKey, sessionType, this.kalturaConfig.getPartnerId(), timeoutSecs, combinedPermission);
            kalturaClient.setSessionId( StringUtils.trim(sessionId) );
            // log.debug("Created new kaltura client (oid="+kalturaClient.toString()+", tid="+Thread.currentThread().getId()+", ks="+kalturaClient.getSessionId()+")");
        } catch (Exception e) {
            log.error("Unable to establish a kaltura session ("+kalturaClient.toString()+", "+kalturaClient.getSessionId()+"):: " + e, e);
        }

        return kalturaClient;
    }


    // CLIPPING

    public KalturaMediaEntry createClip(KalturaMediaEntry mediaEntry, int startTime, int clipDuration) {
        if (mediaEntry == null) {
            throw new IllegalArgumentException("kalturaEntry is not set");
        }
        if (log.isDebugEnabled()) log.debug("createClip(entry="+mediaEntry.id+", startTime="+startTime+", clipDuration="+clipDuration+")");
        if (mediaEntry.id == null) {
            throw new IllegalArgumentException("kalturaEntry id is not set");
        }
        KalturaMediaEntry clip = null;
        KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
        try {
            // Create New Clip
            KalturaClipAttributes kca = new KalturaClipAttributes();
            kca.offset = startTime;
            kca.duration = clipDuration;

            KalturaOperationResource resource = new KalturaOperationResource();
            resource.operationAttributes = new ArrayList<KalturaOperationAttributes>(1);
            resource.operationAttributes.add(kca);
            KalturaEntryResource ker = new KalturaEntryResource();
            ker.entryId = mediaEntry.id;
            resource.resource = ker;

            boolean overwrite = false; // get this from the launch config - clipping or trimming - for now we assume clipping
            if ( overwrite ) {
                // Trim Entry
                /*
                try {
                    $resultEntry = $client->media->updateContent($entryId, $resource);
                } catch( Exception $e ) {
                    die('{"error": "' . $e->getMessage() . '"}');
                }
                 */
                throw new IllegalStateException("Overwrite not supported yet");
            } else {
                // clipping - Create new clip from media entry
                KalturaMediaService kms = kc.getMediaService();
                clip = kms.add(mediaEntry);
                clip = kms.addContent(clip.id, resource);
            }
        } catch (Exception e) {
            log.error("Failure clipping entry ("+mediaEntry.id+") to (start="+startTime+", duration="+clipDuration+"): ks="+kc.getSessionId()+"):: " + e, e);
        }
        return clip;
    }


    // CATEGORY handling

    // Convenience Category methods

    /**
     * Fetch or create the root category if it does not exist yet
     * @return the existing or newly created category
     */
    @NoProfile
    protected KalturaCategory getRootCategory() {
        return getOrAddKalturaCategory(this.kalturaRootCategory, 0);
    }

    /**
     * Fetch or create the site category (off the root) if it does not exist yet
     * @param locationId the Sakai Site id OR location ID (i.e. /site/274875a3-5ea7-4242-ac9d-a2964343e7a1)
     * @return the existing or newly created category
     */
    public KalturaCategory getSiteCategory(String locationId) {
        if (log.isDebugEnabled()) log.debug("getSiteCategory(locationId="+locationId+")");
        String siteCategoryName = external.extractSiteId(locationId);
        // get root category id, if non-existent, add root category
        KalturaCategory rootCat = getRootCategory();
        KalturaCategory siteCat = getOrAddKalturaCategory(siteCategoryName, rootCat.id);
        return siteCat;
    }

    private static String ROOT_CATEGORY = "sakai";

    /**
     * Create a new category
     * NOTE: this will not check to see if the category already exists so you need to check that before calling this
     * @param categoryName the name of the category (e.g. 'myCategory'), max length of 50 chars
     * @param parentId the id of the parent category (set to 0 to indicate this is a root level category)
     * @return the newly created category
     * @throws IllegalArgumentException if the inputs are bad
     * @throws RuntimeException if the category cannot be created or already exists
     */
    protected KalturaCategory addKalturaCategory(String categoryName, int parentId) {
        if (StringUtils.isEmpty(categoryName)) {
            throw new IllegalArgumentException("Invalid category name (blank), name MUST be set");
        } else {
            categoryName = makeSafeCategoryName(categoryName);
        }
        if (log.isDebugEnabled()) log.debug("addKalturaCategory(categoryName="+categoryName+", parentId="+parentId+")");
        if (parentId <= 0) {
            parentId = 0;
        }
        KalturaCategory category = null;
        KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
        try {
            // http://www.kaltura.com/api_v3/testmeDoc/index.php?service=category&action=add
            KalturaCategoryService categoryService = kc.getCategoryService();
            category = new KalturaCategory();
            category.name = categoryName;
            // no parent means this is a root category, otherwise we set the parent based on the new id
            if (parentId > 0) {
                category.parentId = parentId;
            }
            // category id should be populated on creation
            category = categoryService.add(category);
            // add to the cache
            addCategoryToCache(category);
            log.info("Created new kaltura category ("+categoryName+") ["+category.id+"] in parent ("+parentId+"): "+category.fullName);
        } catch (Exception e) {
            // NOTE: adding a category that already exists generates this error: com.kaltura.client.KalturaApiException: category ... already exists
            String msg = "Failure adding category ("+categoryName+") to ("+parentId+") (partner="+this.kalturaConfig.getPartnerId()+", ks="+kc.getSessionId()+"): " + e;
            log.error(msg);
            throw new RuntimeException(msg, e);
        }
        return category;
    }

    private static int CATEGORY_MAX_LENGTH = 50;
    /**
     * creates a category name that is <= 50 characters and is unique
     * 
     * @param categoryName the name of the category
     * @return the safe category name
     * @throws IllegalArgumentException if playlist name is invalid
     */
    @NoProfile
    protected String makeSafeCategoryName(String categoryName) {
        categoryName = StringUtils.trimToNull(categoryName);
        if (categoryName == null) {
            throw new IllegalArgumentException("categoryName cannot be null");
        }
        if (categoryName.length() > CATEGORY_MAX_LENGTH) {
            categoryName = StringUtils.abbreviate(categoryName, CATEGORY_MAX_LENGTH);
            categoryName = categoryName.substring(0, CATEGORY_MAX_LENGTH-3) + RandomStringUtils.randomAlphanumeric(3);
        }
        return categoryName;
    }

    /**
     * Lookup the kaltura category based on the name and parent id
     * 
     * @param categoryName the category name (max length of 50 chars)
     * @param parentId the id of the parent for this category OR <0 to indicate root category
     * @return the KalturaCategory OR null if it cannot be found
     */
    protected KalturaCategory getKalturaCategory(String categoryName, int parentId) {
        // NOTE: Kaltura category names are case insensitive
        if (StringUtils.isEmpty(categoryName)) {
            categoryName = this.kalturaRootCategory;
        } else {
            categoryName = makeSafeCategoryName(categoryName);
        }
        if (log.isDebugEnabled()) log.debug("getKalturaCategory(categoryName="+categoryName+", parentId="+parentId+")");
        KalturaCategory category;
        if (isOfflineMode()) {
            // generate a fake category
            category = new KalturaCategory();
            category.createdAt = (int) System.currentTimeMillis();
            category.description = "this is sample category: "+categoryName;
            category.id = new Random().nextInt();
            category.name = categoryName;
            category.fullName = categoryName; // not very realistic
            category.partnerId = this.kalturaConfig.getPartnerId();
            category.parentId = parentId;
        } else {
            category = getCategoryfromCache(categoryName, parentId);
            if (category == null) {
                KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
                try {
                    KalturaCategoryService categoryService = kc.getCategoryService();
                    KalturaCategoryFilter filter = new KalturaCategoryFilter();
                    filter.nameOrReferenceIdStartsWith = '"'+categoryName+'"'; // adding quotes improves performance - Gonen 24 Sept 2012
                    if (parentId > 0) {
                        filter.parentIdEqual = parentId;
                    }
                    filter.orderBy = KalturaCategoryOrderBy.FULL_NAME_ASC.name();
                    KalturaFilterPager pager = new KalturaFilterPager();
                    pager.pageSize = MAX_ENTRIES_PER_REQUEST; // max items returned is 30 without a pager
                    KalturaCategoryListResponse listResponse = categoryService.list(filter, pager);
                    if (listResponse.totalCount == 1) {
                        category = listResponse.objects.get(0); // just get the first item
                    } else if (listResponse.totalCount > 1) {
                        for (KalturaCategory kalCat : listResponse.objects) {
                            if (kalCat.name.equalsIgnoreCase(categoryName)) {
                                category = kalCat;
                            }
                        }
                        if (category == null) {
                            // use the first if none match
                            category = listResponse.objects.get(0);
                        }
                    }
                    // add this to the cache
                    addCategoryToCache(category);
                } catch (Exception e) {
                    log.error("Failure finding category ("+categoryName+") in parent ("+parentId+"): " + e, e);
                }
            }
        }
        return category;
    }

    /**
     * gets a KalturaCategory object by its id
     * 
     * @param categoryId the id of the category object
     * @return the KalturaCategory object associated with this id OR NULL if not found
     */
    protected KalturaCategory getKalturaCategoryById(String categoryId) {
        if (categoryId == null || !StringUtils.isNumeric(categoryId)) {
            throw new IllegalArgumentException("invalid category id");
        }
        if (log.isDebugEnabled()) log.debug("getKalturaCategoryById(categoryId="+categoryId+")");
        KalturaCategory category = getCategoryfromCache(categoryId);
        if (category == null) {
            try {
                KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
                KalturaCategoryService categoryService = kc.getCategoryService();
                category = categoryService.get(Integer.parseInt(categoryId));
                // add this to the cache for next time
                addCategoryToCache(category);
            } catch (Exception e) {
                log.warn("Failure to lookup category by id: "+categoryId);
                category = null;
            }
        }
        return category;
    }

    /**
     * adds entries to a specified category
     * 
     * @param categoryId the id of the category
     * @param categoryName the name of the category
     * @param metadata [OPTIONAL] the HashMap of metadata for the entries
     * @param entryIds the array of entry ids
     * @return an ArrayList of KalturaCategoryEntry objects
     * @throws IllegalArgumentException if parameters are invalid
     * @throws RuntimeException if error adding items to category
     */
    protected List<KalturaCategoryEntry> addKalturaCategoryEntries(int categoryId, String categoryName, Map<String, Map<String, String>> metadata, String... entryIds) {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Invalid category Id ("+categoryId+"), must be > 0");
        }
        if (log.isDebugEnabled()) log.debug("addKalturaCategoryEntries(categoryId="+categoryId+", categoryName="+categoryName+", entryIds="+ArrayUtils.toString(entryIds)+")");
        List<KalturaCategoryEntry> l = new ArrayList<KalturaCategoryEntry>();
        if (entryIds != null && entryIds.length > 0) {
            KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
            try {
                // eliminate the duplicates
                HashSet<String> entryIdsSet = new HashSet<String>(entryIds.length);
                for (String keid : entryIds) {
                    entryIdsSet.add(keid);
                }
                KalturaCategoryEntryService categoryEntryService = kc.getCategoryEntryService();
                // get the current list of items in the category and remove them from the set of entries to add
                KalturaCategoryEntryFilter filter = new KalturaCategoryEntryFilter();
                filter.categoryIdEqual = categoryId;
                filter.entryIdIn = StringUtils.join(entryIds, ",");
                KalturaFilterPager pager = new KalturaFilterPager();
                pager.pageSize = MAX_ENTRIES_PER_REQUEST; // max items returned is 30 without a pager
                KalturaCategoryEntryListResponse currentEntries = null;
                // Zendesk 5327 (see also: 4131) - Kaltura team suggests waiting a second and then retrying up to 10 times.
                for (int i = 0; i < 10; i++) {
                    boolean okToContinue = true;
                    try {
                        currentEntries = categoryEntryService.list(filter, pager);
                    } catch (KalturaApiException kae) {
                        okToContinue = false;
                        // Gonen indicated that the service might throw an exception if it isn't found and wanted to trap this.
                        // If this is the last past through the loop, we re-throw it since we are done here regardless.
                        if (10 == i) {
                            throw kae;
                        }
                    }
                    if (okToContinue) {
                        // No exception happened, so no need to keep trying.
                        break;
                    }
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException ignore) {
                        // We don't care - ignore
                    }
                }
                for (KalturaCategoryEntry e : currentEntries.objects) {
                    entryIdsSet.remove(e.entryId);
                }
                String locationId = external.fixLocationId(categoryName);
                for (String keid : entryIdsSet) {
                    KalturaCategoryEntry categoryEntry = new KalturaCategoryEntry();
                    categoryEntry.categoryId = categoryId;
                    categoryEntry.entryId = keid;
                    KalturaMediaEntry kme = getKalturaItem(keid);
                    KalturaCategoryEntry added = categoryEntryService.add(categoryEntry);
                    l.add(added);
                    // add metadata for the entry
                    if (kme != null) {
                        MediaItem mediaItem = new MediaItem(locationId, kme, null);
                        Map<String, String> entryMetadata = null;
                        if (MapUtils.isNotEmpty(metadata)) {
                            entryMetadata = metadata.get(keid);
                        }
                        // update the entry's metadata, if necessary
                        MediaService.updateEntryMetadataIfNeeded(mediaItem, METADATA_PERMISSIONS_CONTAINER_TYPE_SITE, categoryName, locationId, entryMetadata, this, external);
                    }
                }
                log.info("Added entries ("+StringUtils.join(entryIdsSet,",")+") to category ("+categoryId+")");
            } catch (Exception e) {
                String msg = "Failure adding items ("+StringUtils.join(entryIds,",")+") in category ("+categoryId+"): " + e;
                log.warn(msg, e);
                throw new RuntimeException(msg, e);
            }
        }
        return l;
    }

    /**
     * Remove entries from a kaltura category
     * 
     * @param categoryId the unique ID for a kaltura category
     * @param entryIds the kaltura entry ids to remove
     * @return the number of items removed by this operation
     * @throws IllegalArgumentException if parameters are invalid
     * @throws RuntimeException if error removing items
     */
    public int removeKalturaCategoryEntries(int categoryId, String... entryIds) {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Invalid category Id ("+categoryId+"), must be > 0");
        }
        if (log.isDebugEnabled()) log.debug("removeKalturaCategoryEntries(categoryId="+categoryId+", entryIds="+ArrayUtils.toString(entryIds)+")");
        int removed = 0;
        if (entryIds != null && entryIds.length > 0) {
            KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
            try {
                KalturaCategory category = getKalturaCategoryById(categoryId+"");
                KalturaCategoryEntryService categoryEntryService = kc.getCategoryEntryService();
                List<KalturaPlaylist> playlists = getPlaylistsInCategoryIds(categoryId+"");
                for (String keid : entryIds) {
                    categoryEntryService.delete(keid, categoryId);
                    // delete entry from all playlists in site
                    for (KalturaPlaylist playlist : playlists) {
                        removeItemFromKalturaPlaylist(playlist, keid);
                    }
                    // delete category metadata for entry
                    updateEntryMetadata(keid, METADATA_PERMISSIONS_CONTAINER_TYPE_SITE, category.name, null, true);
                    removed++;
                }
            } catch (Exception e) {
                String msg = "Failure removing items ("+StringUtils.join(entryIds,",")+") from category ("+categoryId+"): " + e;
                log.error(msg, e);
                throw new RuntimeException(msg, e);
            }
        }
        return removed;
    }

    /**
     * gets a list of all items in a category
     * @param categoryId the id of the category
     * @param start index of item to begin returning results from
     * @param max the maximum numbers of items
     * @return an ArrayList of KalturaMediaEntry objects
     */
    protected List<KalturaMediaEntry> getKalturaItemsForCategory(int categoryId, int start, int max) {
        if (start < 0) {
            start = 0;
        }
        if (max <= 0) {
            max = MAX_ENTRIES_FROM_KALTURA;
        }
        // TODO check if the max is greater than MAX_ENTRIES_PER_REQUEST
        if (log.isDebugEnabled()) log.debug("getKalturaItemsForCategory(categoryId="+categoryId+", start="+start+", max="+max+")");
        List<KalturaMediaEntry> l = new ArrayList<KalturaMediaEntry>(0);
        KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
        try {
            KalturaMediaEntryFilter filter = new KalturaMediaEntryFilter();
            filter.partnerIdEqual = this.kalturaConfig.getPartnerId();
            filter.statusIn = "0,1,2"; // KalturaEntryStatus.IMPORT+","+KalturaEntryStatus.PRECONVERT+","+KalturaEntryStatus.READY;
            // limit to only the entries for this user
            filter.categoriesIdsMatchOr = categoryId+"";
            l = fetchMediaEntriesFromKaltura(kc, filter, max, start);
            // TODO handle entries caching for this category?
        } catch (Exception e) {
            log.error("Failure finding items in category ("+categoryId+"): " + e, e);
        }
        return l;
    }

    /**
     * Will fetch a category OR create it if it cannot be found
     * (mostly useful for getting root or site categories)
     * 
     * @param categoryName the name of the category (e.g. 'myCategory')
     * @param parentId the id of the parent category (set to 0 to indicate this is a root level category)
     * @return the existing or newly created category
     */
    protected KalturaCategory getOrAddKalturaCategory(String categoryName, int parentId) {
        if (StringUtils.isEmpty(categoryName)) {
            throw new IllegalArgumentException("Cannot create category with null or blank name");
        }
        if (log.isDebugEnabled()) log.debug("getOrAddKalturaCategory(categoryName="+categoryName+", parentId="+parentId+")");
        KalturaCategory cat = getKalturaCategory(categoryName, parentId);
        // if non-existent, add root category
        if (cat == null) {
            // add the category since it does not exist
            cat = addKalturaCategory(categoryName, parentId);
        }
        return cat;
    }

    // Category CACHE handling
    private String addCategoryToCache(KalturaCategory cat) {
        String cacheKey = null;
        if (cat != null) {
            cacheKey = cat.parentId+":"+cat.name;
            categoriesCache.put( new Element("id:"+cat.id, cat) );
            categoriesCache.put( new Element(cacheKey, cat.id) );
        }
        return cacheKey;
    }

    private KalturaCategory getCategoryfromCache(String categoryName, int parentId) {
        KalturaCategory cat = null;
        String cacheKey = parentId+":"+categoryName;
        Element el = categoriesCache.get(cacheKey);
        if (el != null) {
            // get it from the cache first
            Integer categoryId = (Integer) el.getObjectValue();
            el = categoriesCache.get("id:"+categoryId);
            if (el != null) {
                cat = (KalturaCategory) el.getObjectValue();
            }
        }
        return cat;
    }

    private KalturaCategory getCategoryfromCache(String categoryId) {
        KalturaCategory cat = null;
        Element el = categoriesCache.get("id:"+categoryId);
        if (el != null) {
            cat = (KalturaCategory) el.getObjectValue();
        }
        return cat;
    }


    // KALTURA Playlist-specific methods (SKE-85)

    // NOTE: NO PUBLIC Playlist methods

    /**
     * Fetch or add a playlist to a category (as a child of the category)
     * 
     * @param parentId the category id to which this playlist is related
     * @return the newly created or found playlist
     * @throws IllegalArgumentException if the category for kalturaCategoryId does not already exist
     * @throws RuntimeException if the playlist does not exist and cannot be created
     */
    protected KalturaPlaylist getOrAddKalturaPlaylist(int categoryId, String playlistName, Map<String, String> fields) {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Invalid category Id ("+categoryId+"), must be > 0");
        }
        if (StringUtils.isEmpty(playlistName)) {
            throw new IllegalArgumentException("Invalid playlist name (blank), name MUST be set");
        } else {
            // NOTE: Kaltura category names are case insensitive for lookup purposes and must be < 60 chars
            playlistName = makeSafePlaylistName(playlistName);
        }
        if (log.isDebugEnabled()) log.debug("getOrAddKalturaPlaylist(categoryId="+categoryId+", playlistName="+playlistName+", fields="+fields+")");
        // TODO caching needed
        KalturaPlaylist playlist = getKalturaPlaylist(categoryId, playlistName);
        // if non-existent, add playlist
        if (playlist == null) {
            // add the playlist since it does not exist
            playlist = addKalturaPlaylist(categoryId, playlistName);
            // add metadata for playlist
            updatePlaylistMetadata(playlist.id, fields);
        }
        return playlist;
    }

    private static int PLAYLIST_MAX_LENGTH = 150;
    /**
     * creates a playlist name that is <= 150 characters and is unique
     * 
     * @param playlistName the name of the playlist
     * @return the safe playlist name
     * @throws IllegalArgumentException if playlist name is invalid
     */
    @NoProfile
    protected String makeSafePlaylistName(String playlistName) {
        playlistName = StringUtils.trimToNull(playlistName);
        if (playlistName == null) {
            throw new IllegalArgumentException("playlistName cannot be null");
        }
        if (playlistName.length() > PLAYLIST_MAX_LENGTH) {
            playlistName = StringUtils.abbreviate(playlistName, CATEGORY_MAX_LENGTH);
            playlistName = playlistName.substring(0, CATEGORY_MAX_LENGTH-3) + RandomStringUtils.randomAlphanumeric(3);
        }
        return playlistName;
    }

    /**
     * Update a playlist and append or replace the entries in a playlist with the new list of entries 
     * in the order provided
     * 
     * @param playlist the Kaltura playlist object to update the entries in
     * @param entryIds a list of entry ids, if this is empty and replacing then clear out the entries from the playlist
     * @param replace if true then replace the existing entries, otherwise append to the end of the existing entries
     * @param permissions the HashMap of the permissions for the entries to be updated
     * @return the updated KalturaPlaylist
     * @throws IllegalArgumentException if the playlist for kalturaPlaylistId does not already exist
     * @throws RuntimeException when playlist cannot be updated
     */
    protected KalturaPlaylist updateKalturaPlaylistEntries(KalturaPlaylist playlist, List<String> entryIds, boolean replace, Map<String, Map<String, String>> permissions) {
        if (playlist == null) {
            throw new IllegalArgumentException("playlist cannot be null");
        }
        if (log.isDebugEnabled()) log.debug("updateKalturaPlaylistEntries(playlist="+playlist.id+", entryIds="+entryIds+", replace="+replace+", permissions="+permissions+")");
        KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
        KalturaPlaylistService playlistService = kc.getPlaylistService();
        // convert entryIds to comma separated string
        String entries = StringUtils.join(entryIds, ",");
        // check if playlist entries are the same
        boolean changed = !entries.equals(playlist.playlistContent);
        if (changed) {
            //if not replacing current entries, get current playlist entries to add
            if (!replace && playlist.playlistContent != null) {
                List<String> existingEntries = Arrays.asList(StringUtils.splitByWholeSeparator(playlist.playlistContent, ","));
                // add new entryIds to the end
                existingEntries.addAll(entryIds);
                // converting to set removes duplicates
                Set<String> entrySet = new LinkedHashSet<String>(existingEntries);
                entries = StringUtils.join(entrySet, ",");
            }
            KalturaPlaylist newPlaylist = new KalturaPlaylist();
            newPlaylist.playlistContent = entries;
            try {
                playlist = playlistService.update(playlist.id, newPlaylist);
                //update the entries' metadata objects
                if (permissions != null) { // if no permissions to change, don't update
                    /* multirequest - this will send all the requests together in a batch (single API calls will return null),
                     * need to potentially limit this to only 20 or so calls
                     */
                    //kc.setMultiRequest(true); // cannot use this right now because of getOrAddEntryMetadata
                    for (String entryId : entryIds) {
                        updateEntryMetadata(entryId, METADATA_PERMISSIONS_CONTAINER_TYPE_PLAYLIST, playlist.id, permissions.get(entryId), false);
                    }
                    //kc.doMultiRequest(); // sets multi request back to false
                }
            } catch (Exception e) {
                throw new RuntimeException("Failure updating entries in playlist ("+playlist.id+"): " + e);
            }
        }
        // TODO caching and failure handling
        return playlist;
    }

    /**
     * removes a kaltura entry from a playlist
     * 
     * @param playlistId the id of the KalturaPlaylist object
     * @param kalturaId the id of the kaltura item
     * @return true if item is removed, otherwise false
     * @throws IllegalArgumentException if the playlist for kalturaPlaylistId does not already exist
     * @throws RuntimeException when playlist cannot be updated
     */
    protected boolean removeItemFromKalturaPlaylist(KalturaPlaylist playlist, String kalturaId) {
        if (playlist == null) {
            throw new IllegalArgumentException("playlist is not set");
        }
        if (StringUtils.isEmpty(kalturaId)) {
            throw new IllegalArgumentException("kaltura entry id is not set");
        }
        if (log.isDebugEnabled()) log.debug("removeItemFromKalturaPlaylist(playlist="+playlist.id+", kalturaId="+kalturaId+")");
        boolean removed = false;
        KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
        KalturaPlaylistService playlistService = kc.getPlaylistService();
        if (playlist.playlistContent != null) {
            // must create a mutable ArrayList in order to remove item here
            List<String> entries = new ArrayList<String>(Arrays.asList(StringUtils.splitByWholeSeparator(playlist.playlistContent, ",")));
            boolean changed = entries.remove(kalturaId);
            if (changed) {
                /* workaround is no longer working in kaltura
                if (entries.isEmpty()) {
                    entries.add(DEFAULT_PLAYLIST_EMPTY);
                }
                */
                KalturaPlaylist newPlaylist = new KalturaPlaylist();
                newPlaylist.playlistContent = StringUtils.join(entries, ",");
                try {
                    playlist = playlistService.update(playlist.id, newPlaylist);
                    // remove entry's permission metadata for this playlist 
                    updateEntryMetadata(kalturaId, METADATA_PERMISSIONS_CONTAINER_TYPE_PLAYLIST, playlist.id, null, true);
                    removed = true;
                } catch (Exception e) {
                    throw new RuntimeException("Failure updating entries in playlist ("+playlist.id+"): " + e);
                }
            }
        }
        return removed;
        // TODO caching and failure handling
    }

    /**
     * Create a new playlist
     * NOTE: this will not check to see if the playlist already exists so you need to check that before calling this
     * 
     * @param categoryId the id of the parent category (set to 0 to indicate this is a root level category)
     * @param name the name of the playlist (should have already been processed using {@link #makeSafePlaylistName(String)}
     * @return the newly created playlist
     * @throws IllegalArgumentException if parameters are invalid
     * @throws RuntimeException if the playlist cannot be created
     */
    protected KalturaPlaylist addKalturaPlaylist(int categoryId, String name) {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Cannot add playlist to invalid categoryId");
        }
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("playlist name cannot be empty");
        }
        if (log.isDebugEnabled()) log.debug("addKalturaPlaylist(categoryId="+categoryId+", name="+name+")");
        KalturaPlaylist playlist = null;
        KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
        try {
            KalturaPlaylistService playlistService = kc.getPlaylistService();
            playlist = new KalturaPlaylist();
            playlist.name = name; // playlist name should have already been processed
            playlist.categoriesIds = categoryId + "";
            playlist.playlistType = KalturaPlaylistType.STATIC_LIST;
            User u = external.getCurrentUser(); // Can be null
            playlist.creatorId = (u != null ? u.getUsername() : DEFAULT_OWNER_ID);
            playlist = playlistService.add(playlist);
            // create playlist's metadata
            updatePlaylistMetadata(playlist.id, initialMetadataFields.get(METADATA_PROFILE_NAME_PLAYLIST));
            log.info("Created new kaltura playlist ("+playlist.name+") ["+playlist.id+"] in category ("+categoryId+")");
            // TODO handle caching
        } catch (Exception e) {
            String msg = "Failure adding playlist to category ("+categoryId+"): " + e;
            log.error(msg);
            throw new RuntimeException(msg, e);
        }
        return playlist;
    }

    /**
     * Lookup the kaltura playlist based on the category id and name
     * 
     * @param categoryId the id of the category which contains this playlist
     * @param playlistName the name of the playlist (should have already been processed using {@link #makeSafePlaylistName(String)}
     * @return the KalturaPlaylist OR null if it cannot be found
     * @throws IllegalArgumentException if parameters are invalid
     */
    protected KalturaPlaylist getKalturaPlaylist(int categoryId, String playlistName) {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Cannot get playlist for invalid categoryId");
        }
        if (StringUtils.isEmpty(playlistName)) {
            throw new IllegalArgumentException("playlist name cannot be empty");
        }
        if (log.isDebugEnabled()) log.debug("getKalturaPlaylist(categoryId="+categoryId+", playlistName="+playlistName+")");
        KalturaPlaylist playlist = null;
        KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
        try {
            KalturaPlaylistService playlistService = kc.getPlaylistService();
            KalturaPlaylistFilter filter = new KalturaPlaylistFilter();
            filter.categoriesIdsMatchOr = categoryId + "";
            filter.nameEqual = playlistName; // playlist name should have already been processed
            KalturaFilterPager pager = new KalturaFilterPager();
            pager.pageSize = MAX_ENTRIES_PER_REQUEST; // max items returned is 30 without a pager
            KalturaPlaylistListResponse listResponse = playlistService.list(filter, pager);
            if (listResponse.totalCount > 0) {
                // just get the first item, as Sakai categories should only have one playlist with a unique name
                playlist = listResponse.objects.get(0); 
            } 
            // TODO handle caching
        } catch (Exception e) {
            log.error("Failure finding playlist for category ("+categoryId+") :: " + e, e);
        }
        return playlist;
    }

    /**
     * get a KalturaPlaylist object by associated playlist ID
     * 
     * @param playlistId the playlist ID
     * @return the KalturaPlaylist object or null if no playlist is found
     * @throws IllegalArgumentException if playlist id is invalid
     */
    protected KalturaPlaylist getPlaylistByPlaylistId(String playlistId) {
        if (StringUtils.isEmpty(playlistId)) {
            throw new IllegalArgumentException("playlist id must be set");
        }
        if (log.isDebugEnabled()) log.debug("getPlaylistByPlaylistId(playlistId="+playlistId+")");
        KalturaPlaylist playlist = null;
        if (isOfflineMode()) {
            // make fake playlist
            playlist = makeSampleKP(playlistId, new Random().nextInt(4));
            playlist.id = playlistId;
        } else {
            KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
            KalturaPlaylistService playlistService = kc.getPlaylistService();
            try {
                playlist = playlistService.get(playlistId);
            } catch (Exception e) {
                playlist = null;
            }
        }
        return playlist;
    }

    /**
     * deletes a KalturaPlaylist object
     * 
     * @param playlistId the ID of the KalturaPlaylist to be deleted
     * @return true, if deletion successful, false if not
     */
    protected boolean deleteKalturaPlaylist(String playlistId) {
        if (log.isDebugEnabled()) log.debug("deleteKalturaPlaylist(playlistId="+playlistId+")");
        KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
        KalturaPlaylistService playlistService = kc.getPlaylistService();
        try {
            playlistService.delete(playlistId);
            deleteMetadataObjectsForKalturaObjectIds(METADATA_PROFILE_NAME_PLAYLIST, playlistId);
            // TODO handle caching
            return true;
        } catch (Exception e) {
            log.error("An error occurred deleting playlist " + playlistId + " :: " + e, e);
            return false;
        }
    }

    /**
     * Get all playlists associated with given category ids
     * 
     * @param categoryIds category IDs in which to search for playlists
     * @return ArrayList of KalturaPlaylist objects or empty if no playlists are found
     * @throws IllegalArgumentException if category ids is null or empty
     * @throws RuntimeException if there is a failure looking up the playlists
     */
    protected List<KalturaPlaylist> getPlaylistsInCategoryIds(String... categoryIds) {
        if (categoryIds == null || categoryIds.length <= 0) {
            throw new IllegalArgumentException("category ids must be set and contain at least one category id");
        }
        if (log.isDebugEnabled()) log.debug("getPlaylistsInCategoryIds(categoryIds="+ArrayUtils.toString(categoryIds)+")");
        List<KalturaPlaylist> kalturaPlaylists = new ArrayList<KalturaPlaylist>(0);
        if (isOfflineMode()) {
            // generate 4 fake playlists
            kalturaPlaylists.add( makeSampleKP(1111+"", 4) );
            kalturaPlaylists.add( makeSampleKP(2222+"", 0) );
            kalturaPlaylists.add( makeSampleKP(3333+"", 3) );
            kalturaPlaylists.add( makeSampleKP(4444+"", 1) );
        } else {
            KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
            KalturaPlaylistService playlistService = kc.getPlaylistService();
            KalturaPlaylistFilter filter = new KalturaPlaylistFilter();
            // create comma-separated string of category IDs from ArrayList
            String categoriesIds = StringUtils.join(categoryIds, ",");
            filter.categoriesIdsMatchOr = categoriesIds;
            try {
                KalturaFilterPager pager = new KalturaFilterPager();
                pager.pageSize = MAX_ENTRIES_PER_REQUEST; // max items returned is 30 without a pager
                KalturaPlaylistListResponse listResponse = playlistService.list(filter, pager);
                kalturaPlaylists = listResponse.objects;
            } catch (Exception e) {
                String msg = "An error occurred while searching for playlists in categories: " + ArrayUtils.toString(categoryIds) + ": "+e;
                log.error(msg, e);
                throw new RuntimeException(msg, e);
            }
        }
        return kalturaPlaylists;
    }

    /**
     * updates the ordering of the entries in a playlist
     * 
     * @param playlistId the id of the Kaltura playlist
     * @param kalturaId the id of the Kaltura item
     * @param positionInPlaylist the position to add the entry,
     * <= 0 indicates to put the entry at the end,
     * any other number will place the entry at that location in the playlist
     * @return updated KalturaPlaylist object
     * @throws IllegalArgumentException if parameters are invalid
     * @throws RuntimeException if playlist does not exist
     */
    protected KalturaPlaylist updatePlaylistEntryOrdering(String playlistId, String kalturaId, int positionInPlaylist) {
        if (StringUtils.trimToNull(playlistId) == null) {
            throw new IllegalArgumentException("playlist id must be set");
        }
        if (StringUtils.trimToNull(kalturaId) == null) {
            throw new IllegalArgumentException("katura item id must be set");
        }
        if (log.isDebugEnabled()) log.debug("updatePlaylistEntryOrdering(playlistId="+playlistId+", kalturaId="+kalturaId+", positionInPlaylist="+positionInPlaylist+")");
        KalturaPlaylist playlist = getPlaylistByPlaylistId(playlistId);
        if (playlist == null) {
            throw new RuntimeException("playlist does not exist");
        }
        List<String> entryIds = new ArrayList<String>();
        if (playlist.playlistContent != null) {
            entryIds.addAll(Arrays.asList(StringUtils.splitByWholeSeparator(playlist.playlistContent, ",")));
        }
        if (positionInPlaylist < 0 || positionInPlaylist > entryIds.size()) {
            entryIds.add(kalturaId);
        } else {
            entryIds.add(positionInPlaylist, kalturaId);
        }
        playlist = updateKalturaPlaylistEntries(playlist, entryIds, true, null);
        return playlist;
    }

    /**
     * save updated KalturaPlaylist
     * 
     * @param modifiedPlaylist
     *            temporary KalturaPlaylist holding updated data
     * @return updated KalturaPlaylist
     */
    protected KalturaPlaylist saveUpdatedKalturaPlaylist(KalturaPlaylist modifiedPlaylist) {
        if (modifiedPlaylist == null) {
            throw new IllegalArgumentException("modified playlist must be set");
        }
        String playlistId = modifiedPlaylist.id;
        if (StringUtils.isEmpty(playlistId)) {
            throw new IllegalArgumentException("playlist id list must be set");
        }
        if (log.isDebugEnabled()) log.debug("saveUpdatedPlaylist(modifiedPlaylist="+modifiedPlaylist.id+")");
        KalturaPlaylist existingPlaylist = getPlaylistByPlaylistId(playlistId);
        KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
        KalturaPlaylistService playlistService = kc.getPlaylistService();
        try {
            // if playlist doesn't exist, create a new one
            if (existingPlaylist == null) {
                existingPlaylist = playlistService.add(modifiedPlaylist);
            } else { // if playlist exists, update it
                KalturaPlaylist newPlaylist = new KalturaPlaylist();
                boolean changed = false;
                // only allow certain playlist data to be modified
                // description
                if (!StringUtils.equals(existingPlaylist.description, modifiedPlaylist.description)) {
                    newPlaylist.description = modifiedPlaylist.description;
                    changed = true;
                }
                // name
                if (!StringUtils.equals(existingPlaylist.name, modifiedPlaylist.name)) {
                    newPlaylist.name = modifiedPlaylist.name;
                    changed = true;
                }
                // only run update if fields have changed
                if (changed) {
                    existingPlaylist = playlistService.update(playlistId, newPlaylist);
                    log.info("Playlist "+existingPlaylist.name+" (" + existingPlaylist.id + ") has been updated successfully.");
                } else {
                    log.info("No changes to playlist (" + existingPlaylist.id + ") data. No update occurred.");
                }
            }
        } catch (Exception e) {
            log.error("Error occurred saving playlist " + modifiedPlaylist.id + ": ks="+kc.getSessionId()+"):: ", e);
        }
        return existingPlaylist;
    }

    //END playlist methods (SKE-85)

    //BEGIN metadata methods (SKE-90)

    /**
     * parses an XSD data file from src/main/resources
     * 
     * @param xsdFileName the name of the XSD file
     * @return string representation of the XSD file content OR null if file not found
     * @throws RuntimeException if file cannot be read
     */
    private String parseXsdDataFile(String xsdFileName) {
        if (StringUtils.isEmpty(xsdFileName)) {
            throw new IllegalArgumentException("XSD file name is invalid");
        }
        String xsdData = null;
        // read the metadata XSD, load it into a string variable
        try {
            InputStream xmlStream = this.getClass().getClassLoader().getResourceAsStream(xsdFileName);
            if (xmlStream != null) {
                long length = xmlStream.available();
                byte[] bytes = new byte[(int) length];
                xmlStream.read(bytes);
                xsdData = new String(bytes);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading XSD schema file: " + xsdFileName + " :: " + e);
        }
        return xsdData;
    }

    /**
     * gets an existing KalturaMetadataProfile object or creates a new one if it doesn't exist
     * 
     * @param classType the type of object we are storing metadata for
     * @param xsdFileName the name of the XSD file
     * @param profileName the name to use for the systemName value of the profile
     * @param objectType the metadata object type
     * @return the existing or newly created KalturaMetadataProfile object
     * @throws IllegalArgumentException if parameters are invalid
     * @throws RuntimeException if error creating/updating metadata profile from xsd file
     */
    private KalturaMetadataProfile getOrAddKalturaMetadataProfile(Class<?> classType, String xsdFileName, String profileName, KalturaMetadataObjectType objectType) {
        KalturaMetadataProfile kmp = getKalturaMetadataProfileForProfileName(profileName);
        if (StringUtils.isEmpty(xsdFileName)) {
            throw new IllegalArgumentException("xsd file name must be set");
        }
        if (StringUtils.isEmpty(profileName)) {
            throw new IllegalArgumentException("profile name must be set");
        }
        KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
        KalturaMetadataProfileService kmps = new KalturaMetadataProfileService(kc);
        String xsdData = parseXsdDataFile(xsdFileName);
        // if metadata profile doesn't exist, create it
        if (kmp == null) {
            KalturaMetadataProfile newKmp = new KalturaMetadataProfile();
            if (KalturaPlaylist.class.equals(classType)) {
                /* TODO this is temporary and meant to keep the playlist fields from appearing in the edit entries view in the KMC
                 * Once the KMC supports editing of metadata in the playlists we will restore this to use API again
                 * AZ - 22 Oct 2012
                 */
                newKmp.createMode = KalturaMetadataProfileCreateMode.APP; // won't appear in the KMC fields editor
            } else {
                newKmp.createMode = KalturaMetadataProfileCreateMode.API;
            }
            newKmp.metadataObjectType = objectType;
            newKmp.systemName = profileName;
            newKmp.name = profileName;
            try {
                kmp = kmps.add(newKmp, xsdData);
                log.info("Kaltura metadata profile (" + profileName + ") has been created from XSD file " + xsdFileName);
            } catch (KalturaApiException e) {
                throw new RuntimeException("Error creating Kaltura Metadata Profile (" + profileName + ") (partner="+this.kalturaConfig.getPartnerId()+", ks="+kc.getSessionId()+") :: " + e);
            }
        } else { 
            // if profile exists, check if update required (has changed), then update with latest XSD file if needed
            if (StringUtils.equals(kmp.xsd, xsdData)) {
                // xsd is the same, no update required
            } else {
                try {
                    kmp = kmps.update(kmp.id, kmp, xsdData);
                    log.info("Kaltura metadata profile (" + profileName + ") has been updated with new XSD schema from XSD file " + xsdFileName);
                } catch (KalturaApiException e) {
                    throw new RuntimeException("Error updating Kaltura Metadata Profile (" + profileName + ") with latest xsd (partner="+this.kalturaConfig.getPartnerId()+", ks="+kc.getSessionId()+") :: " + e);
                }
            }
        }
        return kmp;
    }

    /**
     * get a KalturaMetadataProfile for a profile name
     * 
     * @param profileName name of the system
     * @return associated KalturaMetadataProfile object for the profile name OR null if none found
     * @throws IllegalArgumentException if profile name is invalid
     * @throws RuntimeException if error on getting profile object
     */
    private KalturaMetadataProfile getKalturaMetadataProfileForProfileName(String profileName) {
        if (StringUtils.isEmpty(profileName)) {
            throw new IllegalArgumentException("profile name must be set");
        }
        if (log.isDebugEnabled()) log.debug("getKalturaMetadataProfileForProfileName(profileName="+profileName+")");
        KalturaMetadataProfile kmp = null;
        KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
        KalturaMetadataProfileService kmps = new KalturaMetadataProfileService(kc);
        KalturaMetadataProfileFilter filter = new KalturaMetadataProfileFilter();
        filter.systemNameEqual = profileName;
        KalturaFilterPager pager = new KalturaFilterPager();
        pager.pageSize = MAX_ENTRIES_PER_REQUEST; // max items returned is 30 without a pager
        try {
            ArrayList<KalturaMetadataProfile> response = kmps.list(filter, pager).objects;
            if (!response.isEmpty()) {
                //get the first object, since there should only be one
                kmp = response.get(0);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error getting metadata profile for profile name (" + profileName + ") :: " + e);
        }
        return kmp;
    }

    /**
     * updates a Kaltura playlist's metadata
     * 
     * @param playlistId id of the KalturaPlaylist object
     * @param newFields HashMap of new fields
     * @return the updated KalturaMetadata object
     * @throws IllegalArgumentException if parameters are invalid
     * @throws RuntimeException if error updating metadata
     */
    protected KalturaMetadata updatePlaylistMetadata(String playlistId, Map<String, String> newFields) {
        if (StringUtils.isEmpty(playlistId)) {
            throw new IllegalArgumentException("object id must be set");
        }
        if (newFields == null) {
            newFields = new LinkedHashMap<String, String>();
        }
        if (log.isDebugEnabled()) log.debug("updatePlaylistMetadata(playlistId="+playlistId+", newFields="+newFields+")");
        KalturaMetadata metadata = getOrAddPlaylistMetadata(playlistId);
        //create the string representation of the XML data
        Map<String, String> fields = updatePlaylistMetadataFieldsMap(playlistId, newFields);
        String xmlData = createPlaylistXmlMetadataString(fields);
        KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
        KalturaMetadataService kms = new KalturaMetadataService(kc);
        KalturaMetadata newMetadata = null;
        try {
            newMetadata = kms.update(metadata.id, xmlData);
        } catch (KalturaApiException e) {
            throw new RuntimeException("updatePlaylistMetadata: Error updating metadata for kaltura object (" + playlistId + ") :: " + e);
        }
        return newMetadata;
    }

    /**
     * updates a Kaltura entry's metadata
     * 
     * @param entryId id of the Kaltura entry object
     * @param containerType the type of container in which the entry is contained
     * @param containerId the id of the container
     * @param permissions HashMap of the permissions (keys: Owner, Hidden, Reusable, Remixable)
     * @param remove should we remove the permissions metadata from the entry?
     * @return the updated KalturaMetadata object
     * @throws IllegalArgumentException if parameters are invalid
     * @throws RuntimeException if error updating metadata
     */
    protected KalturaMetadata updateEntryMetadata(String entryId, String containerType, String containerId, Map<String, String> permissions, boolean remove) {
        if (StringUtils.isEmpty(entryId)) {
            throw new IllegalArgumentException("entry id must be set");
        }
        if (StringUtils.isEmpty(containerId)) {
            throw new IllegalArgumentException("container id must be set");
        }
        if (StringUtils.isEmpty(containerType)) {
            throw new IllegalArgumentException("container type must be set");
        }
        if (log.isDebugEnabled()) log.debug("updateEntryMetadata(entryId="+entryId+", containerType="+containerType+", containerId="+containerId+", permissions="+permissions+", remove="+remove+")");

        // fetch the metadata if it exists
        KalturaMetadata metadata = getMetadataObjectsForKalturaEntryIds(entryId).get(entryId);
        KalturaMetadata newMetadata = metadata;

        if (!remove && permissions == null) {
            // use defaults if perms are not set
            permissions = initialMetadataFields.get(entryMetadataProfile.name);
        }
        // create the new encoded permissions string
        String ownerId = external.getCurrentUserName();
        if (permissions != null && permissions.containsKey(METADATA_OWNER) && StringUtils.isNotEmpty(permissions.get(METADATA_OWNER))) {
            ownerId = permissions.get(METADATA_OWNER);
        }

        // if metadata object not found for object, create one, otherwise, update it with new metadata values
        if (metadata == null) {
            // create new metdata object (only if we are not removing this value)
            if (remove) {
                // nothing to do here and NO metadata to return....
                newMetadata = new KalturaMetadata(); // just to avoid NPE
            } else {
                // add the new metadata for this entry
                KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
                KalturaMetadataService kms = new KalturaMetadataService(kc);
                // creates the string representation of the XML data
                String newPermissionString = encodeMetadataPermissions(containerType, containerId, ownerId, permissions);
                String xmlData = createEntryXmlMetadataString(Arrays.asList(newPermissionString));
                try {
                    metadata = kms.add(entryMetadataProfile.id, KalturaMetadataObjectType.ENTRY, entryId, xmlData);
                } catch (KalturaApiException e) {
                    throw new RuntimeException("Error adding metadata for kaltura entry object (" + entryId + ") :: " + e);
                }
            }

        } else {
            // update existing metadata

            if (useXSLTMetadataUpdate) {
                // NEW way using XSLT to update metadata (currently fails with error)
                /* ERROR
                 * Error updating metadata for kaltura object (1_wneqds19) :: 
                 * com.kaltura.client.KalturaApiException: The access to service [metadata_metadata->updateFromXSL] is forbidden
                 */
                String xslt;
                if (remove) {
                    // remove this permissions data
                    xslt = createEntryRemoveXsltMetadataString(containerType, containerId);
                } else {
                    // add or update the existing XML
                    // create the new encoded permissions string
                    String newPermissionString = encodeMetadataPermissions(containerType, containerId, ownerId, permissions);
                    xslt = createEntryAddUpdateXsltMetadataString(newPermissionString);
                }

                if (StringUtils.isNotEmpty(xslt)) {
                    // run the actual xslt update process in kaltura
                    KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
                    KalturaMetadataService kms = new KalturaMetadataService(kc);
                    try {
                        // convert string to kaltura file
                        KalturaFile xsltKF = new KalturaFile(IOUtils.toInputStream(xslt, "UTF-8"), "metadataUpdate.xslt", xslt.length());
                        // run the update
                        newMetadata = kms.updateFromXSL(metadata.id, xsltKF);
                    } catch (IOException e) {
                        throw new RuntimeException("IO Error updating metadata for kaltura object (" + entryId + ") :: " + e, e);
                    } catch (KalturaApiException e) {
                        throw new RuntimeException("updateEntryMetadata (XSL): Error ("+e.code+") updating metadata for kaltura object (" + entryId + "), ks="+kc.getSessionId()+" :: " + e, e);
                    }
                }

            } else {
                // Full XML update - OLD WAY - TODO remove this old code once XSLT code works

                //create the string representation of the XML data
                List<String> fields = convertKalturaMetadataToFieldsList(metadata);
                // create the new encoded permissions string
                String newPermissionString = encodeMetadataPermissions(containerType, containerId, ownerId, permissions);
                // check for existing permission string for this entry and container, remove if exists, then add new string
                for (int pos = 0; pos < fields.size(); pos++) {
                    String currentPermission = fields.get(pos);
                    if (StringUtils.trimToNull(currentPermission) != null) {
                        Map<String, String> perms = decodeMetadataPermissions(currentPermission, false);
                        // if non-empty permission string and the container matches both by type and id, remove it
                        if (StringUtils.equals(containerType, perms.get(METADATA_CONTAINER_TYPE)) && StringUtils.equals(containerId, perms.get(METADATA_CONTAINER_ID))) {
                            // remove existing permissions
                            fields.remove(pos);
                            break;
                        }
                    } else { // if string is null, remove it
                        fields.remove(pos);
                    }
                }
                if (permissions != null) {
                    // add new permissions string to end of list
                    fields.add(newPermissionString);
                }
                if (fields.isEmpty()) {
                    fields.add("");
                }

                String xmlData = createEntryXmlMetadataString(fields);
                KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
                KalturaMetadataService kms = new KalturaMetadataService(kc);
                try {
                    newMetadata = kms.update(metadata.id, xmlData);
                } catch (KalturaApiException e) {
                    throw new RuntimeException("updateEntryMetadata (XML): Error updating metadata for kaltura object (" + entryId + ") :: " + e, e);
                }
            }
        }

        return newMetadata;
    }

    /**
     * gets a KaturaMetadata object for a playlist or creates a new one if none found
     * 
     * @param playlistId the id of the KalturaPlaylist object
     * @return the KalturaMetadata object
     * @throws IllegalArgumentException if object id is invalid
     * @throws RuntimeException if error adding metadata
     */
    private KalturaMetadata getOrAddPlaylistMetadata(String playlistId) {
        if (StringUtils.isEmpty(playlistId)) {
            throw new IllegalArgumentException("playlist id must be set");
        }
        if (log.isDebugEnabled()) log.debug("getOrAddPlaylistMetadata(playlistId="+playlistId+")");
        KalturaMetadata metadata = getMetadataObjectsForKalturaPlaylistIds(playlistId).get(playlistId);
        // if metadata object not found for object, create one
        if (metadata == null) {
            KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
            KalturaMetadataService kms = new KalturaMetadataService(kc);
            // create the fields map with default values
            Map<String, String> fields = initialMetadataFields.get(playlistMetadataProfile.name);
            // creates the string representation of the XML data
            String xmlData = createPlaylistXmlMetadataString(fields);
            try {
                metadata = kms.add(playlistMetadataProfile.id, KalturaMetadataObjectType.ENTRY, playlistId, xmlData);
            } catch (KalturaApiException e) {
                throw new RuntimeException("Error adding metadata for kaltura playlist (" + playlistId + ") :: " + e);
            }
        }
        return metadata;
    }

    /**
     * Gets KalturaMetadata objects for one or many Kaltura entry Ids
     * 
     * @param objectId the id of the Kaltura entry
     * @return map of id -> the KalturaMetadata object OR null if no metadata found for that id
     * @throws IllegalArgumentException if object id is invalid
     * @throws RuntimeException if error getting metadata
     */
    private Map<String, KalturaMetadata> getMetadataObjectsForKalturaEntryIds(String... objectId) {
        return getMetadataObjectsForKalturaObjectIds(METADATA_PROFILE_NAME_ENTRY, objectId);
    }

    /**
     * Gets KalturaMetadata objects for one or many Kaltura playlist Ids
     * 
     * @param objectId the id of the Kaltura playlist
     * @return map of id -> the KalturaMetadata object OR null if no metadata found for that id
     * @throws IllegalArgumentException if object id is invalid
     * @throws RuntimeException if error getting metadata
     */
    private Map<String, KalturaMetadata> getMetadataObjectsForKalturaPlaylistIds(String... objectId) {
        return getMetadataObjectsForKalturaObjectIds(METADATA_PROFILE_NAME_PLAYLIST, objectId);
    }

    /**
     * Gets KalturaMetadata objects for one or many Kaltura object Ids
     * (object ids will be typically kaltura entry or playlist ids)
     * 
     * @param metadatProfileName METADATA_PROFILE_NAME_ENTRY or METADATA_PROFILE_NAME_PLAYLIST 
     *          depending on the type of objects the ids are for, Defaults to Entry
     * @param objectId the id of the Kaltura object (entry or playlist)
     * @return map of id -> the KalturaMetadata object OR null if no metadata found for that id
     * @throws IllegalArgumentException if object id is invalid
     * @throws RuntimeException if error getting metadata
     */
    private Map<String, KalturaMetadata> getMetadataObjectsForKalturaObjectIds(String metadatProfileName, String... objectId) {
        if (ArrayUtils.isEmpty(objectId)) {
            throw new IllegalArgumentException("object ids must be set");
        }
        if (log.isDebugEnabled()) log.debug("getMetadataObjectForKalturaObjectId(objectId="+StringUtils.join(objectId, ",")+")");
        Map<String, KalturaMetadata> metadata = new HashMap<String, KalturaMetadata>();
        if (isOfflineMode()) {
            // TODO generate fake metadata (for now we just return nothing)
        } else {
            KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
            KalturaMetadataService kms = new KalturaMetadataService(kc);
            KalturaMetadataFilter filter = new KalturaMetadataFilter();
            // https://jira.sakaiproject.org/browse/SKE-206
            int metadataProfileId = entryMetadataProfile != null ? entryMetadataProfile.id : -1;
            if (METADATA_PROFILE_NAME_PLAYLIST.equals(metadatProfileName) || metadataProfileId < 0) {
                metadataProfileId = playlistMetadataProfile != null ? playlistMetadataProfile.id : -1;
            }
            if (metadataProfileId < 0) {
                log.warn("getMetadataObjectsForKalturaObjectIds Unable to filter metadata by the metadata profile ("+metadatProfileName+"), no ids can be found because the metadataProfile objects are null");
            } else {
                filter.metadataProfileIdEqual = metadataProfileId;
            }
            List<String> objectIds = Arrays.asList(objectId);
            //filter.objectIdEqual = StringUtils.join(objectIds, ",");
            // TODO use fetchMetadataObjectsFromKaltura() instead
            if (objectIds.size() > 500) {
                log.warn("Maximum metadata entries filter is 500 items, this filter is asking for "+objectIds.size()+" items so only the first 500 will be returned");
            }
            filter.objectIdIn = StringUtils.join(objectIds, ",");
            ArrayList<KalturaMetadata> response = new ArrayList<KalturaMetadata>();
            KalturaFilterPager pager = new KalturaFilterPager();
            pager.pageSize = MAX_ENTRIES_PER_REQUEST; // max items returned is 30 without a pager
            try {
                response = kms.list(filter, pager).objects;
                if (!response.isEmpty()) {
                    for (KalturaMetadata r : response) {
                        String id = r.objectId;
                        metadata.put(id, r);
                    }
                }
            } catch (KalturaApiException e) {
                throw new RuntimeException("Error getting metadata object for kaltura objects (" + StringUtils.join(objectIds, ",") + ") :: " + e);
            }
        }
        return metadata;
    }

    /**
     * CURRENTLY UNUSED - do NOT used this unless you finish implementing it
     * TODO implement this to support metadata fetch of greater than 500 items
     *
     * @param kc
     * @param metadatProfileName METADATA_PROFILE_NAME_ENTRY or METADATA_PROFILE_NAME_PLAYLIST 
     *          depending on the type of objects the ids are for, Defaults to Entry
     * @param objectIds
     * @return
     * @throws KalturaApiException
     * @deprecated Not completely implemented yet
     */
    @SuppressWarnings("unused")
    private List<KalturaMetadata> fetchMetadataObjectsFromKaltura(KalturaClient kc, String metadatProfileName, List<String> objectIds) throws KalturaApiException {
        if (kc == null) {
            throw new IllegalArgumentException("KalturaClient kc must be set");
        }
        if (objectIds == null) {
            throw new IllegalArgumentException("KalturaMetadataFilter filter must be set");
        }
        ArrayList<KalturaMetadata> objects = new ArrayList<KalturaMetadata>();
        if (!objectIds.isEmpty()) {
            KalturaMetadataService kms = new KalturaMetadataService(kc);
            KalturaMetadataFilter filter = new KalturaMetadataFilter();
            int metadataProfileId = entryMetadataProfile != null ? entryMetadataProfile.id : -1;
            if (METADATA_PROFILE_NAME_PLAYLIST.equals(metadatProfileName) || metadataProfileId < 0) {
                metadataProfileId = playlistMetadataProfile != null ? playlistMetadataProfile.id : -1;
            }
            if (metadataProfileId < 0) {
                log.warn("getMetadataObjectsForKalturaObjectIds Unable to filter metadata by the metadata profile ("+metadatProfileName+"), no ids can be found because the metadataProfile objects are null");
            } else {
                filter.metadataProfileIdEqual = metadataProfileId;
            }
            // from Gonen - change this so it uses .objectIdIn=id,id,id,id,... - NOTE: max of 50 so you may have to loop this
            if (objectIds.size() < 500) {
                filter.objectIdIn = StringUtils.join(objectIds, ",");
                KalturaFilterPager pager = new KalturaFilterPager();
                pager.pageSize = 500; // max items returned for metadata filter is 49
                objects.addAll( kms.list(filter, pager).objects );
            } else {
                log.info("Maximum metadata entries filter is 50 items, this filter is asking for "+objectIds.size()+" items so multiple requests are required to fetch all data");
                for (int i = 2; i <= 110; i++) { // max iterations = 110
                    // TODO implement this
                }
            }
        }
        return objects;
    }




    /**
     * removes a KalturaMetadata object for a Kaltura object id
     * 
     * @param metadatProfileName METADATA_PROFILE_NAME_ENTRY or METADATA_PROFILE_NAME_PLAYLIST 
     *          depending on the type of objects the ids are for, Defaults to Entry
     * @param objectId the id of the Kaltura object
     * @return true if deletion successful, false if not
     * @throws IllegalArgumentException if object id is invalid
     * @throws RuntimeException if error getting metadata
     */
    private boolean deleteMetadataObjectsForKalturaObjectIds(String metadatProfileName, String... objectIds) {
        if (ArrayUtils.isEmpty(objectIds)) {
            throw new IllegalArgumentException("object ids must be set");
        }
        if (log.isDebugEnabled()) log.debug("deleteMetadataObjectForKalturaObjectId(objectIds="+StringUtils.join(objectIds, ",")+")");
        boolean deleted = false;
        KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
        KalturaMetadataService kms = new KalturaMetadataService(kc);
        KalturaMetadataFilter filter = new KalturaMetadataFilter();
        int metadataProfileId = entryMetadataProfile != null ? entryMetadataProfile.id : -1;
        if (METADATA_PROFILE_NAME_PLAYLIST.equals(metadatProfileName) || metadataProfileId < 0) {
            metadataProfileId = playlistMetadataProfile != null ? playlistMetadataProfile.id : -1;
        }
        if (metadataProfileId < 0) {
            log.warn("getMetadataObjectsForKalturaObjectIds Unable to filter metadata by the metadata profile ("+metadatProfileName+"), no ids can be found because the metadataProfile objects are null");
        } else {
            filter.metadataProfileIdEqual = metadataProfileId;
        }
        //filter.objectIdEqual = StringUtils.join(objectIds, ",");
        filter.objectIdIn = StringUtils.join(objectIds, ",");
        // NOTE: max of 500 so you may have to loop this
        if (objectIds.length > 500) {
            log.warn("Maximum metadata entries delete filter is 500 items, this filter is asking for "+objectIds.length+" items so only the first 500 will be returned");
        }
        KalturaFilterPager pager = new KalturaFilterPager();
        pager.pageSize = MAX_ENTRIES_PER_REQUEST; // max items returned is 30 without a pager
        try {
            ArrayList<KalturaMetadata> response = kms.list(filter, pager).objects;
            if (!response.isEmpty()) {
                for (KalturaMetadata r : response) {
                    kms.delete(r.id);
                }
                deleted = true;
            }
        } catch (KalturaApiException e) {
            throw new RuntimeException("Error getting metadata object for kaltura objects (" + StringUtils.join(objectIds, ",") + ") :: " + e);
        }
        return deleted;
    }

    /**
     * creates initial metadata fields hashmap for an object based on metadata profile name
     * 
     * @param metadataProfileName the name of the metadata profile
     * @return HashMap of metadata fields
     * @throws IllegalArgumentException if profile name is invalid
     */
    private Map<String, String> initializeMetadataFields(String metadataProfileName) {
        Map<String, String> fields = new LinkedHashMap<String, String>();
        // set default fields map based on profile name
        if (StringUtils.equals(METADATA_PROFILE_NAME_PLAYLIST, metadataProfileName)) { // playlist
            fields.put("Title", DEFAULT_METADATA_TITLE);
            fields.put("Type", DEFAULT_METADATA_TYPE);
            fields.put(METADATA_HIDDEN, DEFAULT_METADATA_HIDDEN);
            fields.put(METADATA_OWNER, DEFAULT_METADATA_OWNER);
        } else if (StringUtils.equals(METADATA_PROFILE_NAME_ENTRY,metadataProfileName)) { // entry
            fields.put("Permissions", "");
        } else {
            throw new IllegalArgumentException("invalid metadata profile name");
        }
        return fields;
    }

    /**
     * updates the metadata fields map with the new fields
     * 
     * @param playlistId the id of the KalturaPlaylist object
     * @param newFields the HashMap of the new fields
     * @return the updated HashMap of the fields
     * @throws IllegalArgumentException if object id is invalid
     */
    private Map<String, String> updatePlaylistMetadataFieldsMap(String playlistId, Map<String, String> newFields) {
        if (StringUtils.isEmpty(playlistId)) {
            throw new IllegalArgumentException("playlist id must be set");
        }
        Map<String, String> fields = getPlaylistMetadataFields(playlistId).get(playlistId);
        if (newFields != null) {
            fields.putAll(newFields);
        }
        return fields;
    }

    /**
     * gets the metadata fields for a playlist
     * 
     * @param playlistIds the playlist's id (can be multiple ids)
     * @return map of {playlistId -> map of the playlist metadata {fieldname -> value} }
     * @throws IllegalArgumentException if playlistId is invalid
     * @throws RuntimeException if error getting metadata
     */
    protected Map<String, Map<String, String>> getPlaylistMetadataFields(String... playlistIds) {
        if (log.isDebugEnabled()) log.debug("getPlaylistMetadataFields(playlistId="+Arrays.toString(playlistIds)+")");
        HashMap<String, Map<String, String>> m;
        if (playlistIds == null) {
            throw new IllegalArgumentException("playlistIds must not be null or empty");
        } else if (playlistIds.length == 0) {
            m = new HashMap<String, Map<String, String>>(0);
        } else {
            m = new HashMap<String, Map<String, String>>(playlistIds.length);
            Map<String, KalturaMetadata> metadataMap = getMetadataObjectsForKalturaPlaylistIds(playlistIds);
            for (String playlistId : playlistIds) {
                KalturaMetadata metadata = metadataMap.get(playlistId);
                Map<String, String> fields = new LinkedHashMap<String, String>();
                // if metadata exists for object
                if (metadata != null) {
                    try {
                        Document metadataDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(metadata.xml)));
                        org.w3c.dom.Element root = metadataDoc.getDocumentElement();
                        NodeList childNodes = root.getChildNodes();
                        for (int i = 0; i < childNodes.getLength(); i++) {
                            Node node = childNodes.item(i);
                            if (node instanceof org.w3c.dom.Element) {
                                fields.put(node.getNodeName(), node.getTextContent());
                            }
                        }
                        fields = stripUnusedMetadataFields(fields, METADATA_PROFILE_NAME_PLAYLIST);
                    } catch (Exception e) {
                        throw new RuntimeException("Error getting metadata fields for kaltura playlist " + playlistIds + " :: " + e, e);
                    }
                } else { // if no existing metadata then use default metadata
                    fields = initialMetadataFields.get(METADATA_PROFILE_NAME_PLAYLIST);
                }
                m.put(playlistId, fields);
            }
        }
        return m;
    }

    /**
     * gets the metadata fields for an entry or multiple entries,
     * the returned map is always the same size as the input array
     * 
     * @param entryIds the id of one or more entries (should not contain duplicates)
     * @return map of {id -> (List of string representations of the metadata fields OR empty if not found)}
     * @throws IllegalArgumentException if object id is invalid
     * @throws RuntimeException if error getting metadata
     */
    private Map<String, List<String>> getEntryMetadataFields(String... entryIds) {
        if (log.isDebugEnabled()) log.debug("getEntryMetadataFields(entryId="+ArrayUtils.toString(entryIds)+")");
        HashMap<String, List<String>> m;
        if (entryIds == null) {
            throw new IllegalArgumentException("entry id must not be null or empty");
        } else if (entryIds.length == 0) {
            m = new HashMap<String, List<String>>(0);
        } else {
            m = new HashMap<String, List<String>>(entryIds.length);
            // make the call to kaltura to fetch the metadata for a series of entries
            Map<String, KalturaMetadata> metadataMap = getMetadataObjectsForKalturaEntryIds(entryIds);
            for (String entryId : entryIds) {
                KalturaMetadata metadata = metadataMap.get(entryId);
                List<String> fields = convertKalturaMetadataToFieldsList(metadata);
                m.put(entryId, fields);
            }
        }
        return m;
    }

    /**
     * Method to process the KalturaMetadata object (and the XML it contains) into a list of field strings
     * @param metadata kaltura metadata object
     * @return List of field strings
     */
    private List<String> convertKalturaMetadataToFieldsList(KalturaMetadata metadata) {
        List<String> fields = new ArrayList<String>();
        // if metadata exists for object
        if (metadata != null && StringUtils.isNotEmpty(metadata.xml)) {
            // check for malformed beginning of XML
            String metadataXml = metadata.xml;
            if (StringUtils.startsWithIgnoreCase(metadataXml, "xml=")){ 
                metadataXml = metadataXml.replaceAll("xml=", ""); 
            }

            // ensure XML begins with the <?xml tag
            int lastIndex = StringUtils.lastIndexOf(metadataXml, "<?xml");
            if(lastIndex > 0){
                metadataXml = StringUtils.substring(metadataXml, lastIndex);
            }

            // set the metadata's XML to the updated string
            metadata.xml = metadataXml;

            try {
                Document metadataDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(metadata.xml)));
                org.w3c.dom.Element root = metadataDoc.getDocumentElement();
                NodeList childNodes = root.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node node = childNodes.item(i);
                    if (node instanceof org.w3c.dom.Element) {
                        fields.add(node.getTextContent());
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Error processing metadata fields for kaltura metadata object (" + metadata.objectId + ") :: " + e + ", xml="+metadata.xml, e);
            }
        }
        return fields;
    }

    /**
     * Lookup entry metadata for all given entries and all given containers and then build a map of data,
     * the outer map will include an entry for every input container, however, 
     * the second map will ONLY contain the entries which actually have metadata entered for them
     * (therefore it is unlikely to contain the same number of values as the entryIds input value)
     * 
     * This differs from {@link #getMetadataForEntry(String, String...)} which always returns a map of the same size
     * as the input set of entry ids.
     * 
     * OPTIMIZATION method (fetch lots of metadata at once)
     * 
     * @param containerIds the set of collection (or site) ids to fetch metadata for the input entries
     * @param entryIds the id of the entry (can be 1 or many values)
     * @return Map of the {containerId -> Map of {entryId -> Map of metadata {key -> value} } }
     */
    protected Map<String, Map<String, Map<String, String>>> getMetadataForContainersEntries(Set<String> containerIds, String... entryIds) {
        if (containerIds == null || containerIds.isEmpty()) {
            throw new IllegalArgumentException("playlist ids must be set and not empty");
        }
        if (entryIds == null) {
            throw new IllegalArgumentException("entry ids must be set");
        }
        if (log.isDebugEnabled()) log.debug("getMetadataForContainersEntries(containerIds="+containerIds+", entryIds="+ArrayUtils.toString(entryIds)+")");
        Map<String, Map<String, Map<String, String>>> metadata = new LinkedHashMap<String, Map<String, Map<String, String>>>(containerIds.size());
        // pre-populate the metadata map to ensure every passed in playlist id has an entry
        for (String playlistId : containerIds) {
            metadata.put(playlistId, new LinkedHashMap<String, Map<String, String>>());
        }
        if (ArrayUtils.isNotEmpty(entryIds)) {
            Map<String, List<String>> metadataFieldsMap = getEntryMetadataFields(entryIds);
            for (Map.Entry<String, List<String>> entry : metadataFieldsMap.entrySet()) {
                String entryId = entry.getKey();
                List<String> metadataList = entry.getValue();
                for (String entryMetadata : metadataList) {
                    // search for the matching container Id in the decoded metadata and only use the results if it was found
                    Map<String, String> metadataMap = decodeMetadataPermissions(entryMetadata, false);
                    String containerId = metadataMap.get(METADATA_CONTAINER_ID);
                    if (containerIds.contains(containerId)) {
                        metadata.get(containerId).put(entryId, metadataMap);
                    }
                }
            }
        }
        return metadata;
    }

    /**
     * Gets a HashMap representation of metadata for an entry or set of entries associated with a given container
     * (since each kaltura entry can have multiple permissions related to each collection)
     * NOTE: this will always return a map which is the same size as the input array of entries
     * 
     * OPTIMIZATION method (fetch lots of metadata at once)
     * 
     * @param containerId the id of the container (typically this will be the collection id or site id)
     * @param entryId the id of the entry (can be 1 or many values)
     * @return Map of the {entryId -> Map of metadata {key -> value} }
     */
    protected Map<String, Map<String, String>> getMetadataForEntry(String containerId, String... entryIds) {
        if (StringUtils.isEmpty(containerId)) {
            throw new IllegalArgumentException("container id must be set");
        }
        if (entryIds == null || entryIds.length == 0) {
            throw new IllegalArgumentException("entry ids must be set and not empty");
        }
        if (log.isDebugEnabled()) log.debug("getMetadataForEntry(containerId="+containerId+", entryId="+ArrayUtils.toString(entryIds)+")");
        Map<String, Map<String, String>> metadata = new LinkedHashMap<String, Map<String, String>>(entryIds.length);
        // generate the default set of metadata permissions for when they do not exist
        Map<String, String> defaultMetadata = decodeMetadataPermissions(null, false);
        HashSet<String> containerIds = new HashSet<String>(1);
        containerIds.add(containerId);
        // get a set of metadata entries (only includes the entries which have metadata)
        Map<String, Map<String, String>> entriesMetadata = getMetadataForContainersEntries(containerIds, entryIds).get(containerId);
        // construct a map with all entries and fill in any missing metadata with default metadata (to ensure every input entry id is returned)
        for (String entryId : entryIds) {
            if (entriesMetadata.containsKey(entryId)) {
                metadata.put(entryId, entriesMetadata.get(entryId));
            } else {
                metadata.put(entryId, defaultMetadata);
            }
        }
        return metadata;
    }

    /**
     * encodes the permissions string to the form: ("site:<siteId>::{<ownerId>}h,S,r" OR "playlist:<playlistId>::{<ownerId>}h,S,r")
     * 
     * @param containerType the type of container in which the entry is located (site OR playlist)
     * @param containerId the id of the container
     * @param ownerId the username of the user who added the media item to this container (might be the same as the creatorId)
     * @param permissions Map of permission data (keys: Hidden, Reusable, Remixable)
     * @return string representation of the permissions data
     * @throws IllegalArgumentException if parameters are invalid
     */
    protected String encodeMetadataPermissions(String containerType, String containerId, String ownerId, Map<String, String> permissions) {
        if (StringUtils.isEmpty(containerId)) {
            throw new IllegalArgumentException("containerId must not be empty");
        }
        if ( !("site".equals(containerType) || "playlist".equals(containerType)) ) {
            throw new IllegalArgumentException("container type must be set to site or playlist");
        }
        //fix-up permissions map
        permissions = buildEntryMetadataPermissionsMap(permissions);
        // set container string ("site:<siteId>::{<ownerId>}h,S,r")
        String permissionValues = StringUtils.join(permissions.values(), ",");
        StringBuilder sb = new StringBuilder();
        sb.append(containerType);
        sb.append(":");
        sb.append(containerId);
        sb.append("::{");
        sb.append(ownerId);
        sb.append("}");
        sb.append(permissionValues);
        String encodedPermissions = sb.toString();
        return encodedPermissions;
    }

    /**
     * Decodes the permissions string into a map of values.
     * Keys: containerType, containerId, Owner, Hidden, Reusable, Remixable
     * 
     * @param permissionString the string representation of the permissions
     *      ("site:<siteId>::{<ownerId>}h,S,r" OR "playlist:<playlistId>::{<ownerId>}h,S,r")
     * @param failIfInvalid if true then this will throw an exception if the input string is invalid, otherwise it will return defaults
     * @return Map of permission data OR defaults if input string is invalid
     * @throws IllegalArgumentException if the string decoding fails
     */
    protected Map<String, String> decodeMetadataPermissions(String permissionString, boolean failIfInvalid) {
        Map<String, String> decodedPermissions = new HashMap<String, String>(6);
        // check if permissions string is a valid format
        if (StringUtils.isNotEmpty(permissionString) 
                && StringUtils.contains(permissionString, "::{") 
                && (StringUtils.startsWith(permissionString, "site:") || StringUtils.startsWith(permissionString, "playlist:"))) {
            // first find the last "}" in the string (first from the right)
            int braceRight = StringUtils.lastIndexOf(permissionString, '}');
            if (braceRight == -1) {
                throw new IllegalArgumentException("Invalid format- no '}' (e.g. 'site:<site_library_id>::{<ownerId>}h,S,r') for permissions string: "+permissionString);
            }
            // find the last "::{" from the right of the brace
            int braceLeft = StringUtils.lastIndexOf(permissionString, "::{", braceRight);
            if (braceLeft == -1) {
                throw new IllegalArgumentException("Invalid format - no '::{' (e.g. 'site:<site_library_id>::{<ownerId>}h,S,r') for permissions string: "+permissionString);
            }
            // chop the string into the 3 parts dropping the braces and :: separators
            String containerStr = StringUtils.substring(permissionString, 0, braceLeft);
            braceLeft += 2; // shift over to the actual brace
            String ownerId = StringUtils.substring(permissionString, braceLeft+1, braceRight);
            if (StringUtils.isEmpty(ownerId)) {
                throw new IllegalArgumentException("Invalid format - no ownerId (e.g. 'site:<site_library_id>::{<ownerId>}h,S,r') for permissions string: "+permissionString);
            }
            decodedPermissions.put(METADATA_OWNER, ownerId);
            // get the containerId from the first piece
            if (StringUtils.startsWith(containerStr, "site:")) {
                decodedPermissions.put(METADATA_CONTAINER_TYPE, "site");
                decodedPermissions.put(METADATA_CONTAINER_ID, StringUtils.substring(containerStr, 5));
            } else if (StringUtils.startsWith(containerStr, "playlist:")) {
                decodedPermissions.put(METADATA_CONTAINER_TYPE, "playlist");
                decodedPermissions.put(METADATA_CONTAINER_ID, StringUtils.substring(containerStr, 9));
            } else {
                // should never really happen
                throw new IllegalArgumentException("Invalid format - bad prefix (e.g. 'site:<site_library_id>::{<ownerId>}h,S,r') for permissions string: "+permissionString);
            }
            if (StringUtils.isEmpty(decodedPermissions.get(METADATA_CONTAINER_ID))) {
                throw new IllegalArgumentException("Invalid format - no containerId (e.g. 'site:<site_library_id>::{<ownerId>}h,S,r') for permissions string: "+permissionString);
            }
            // split the permissions
            String perms = StringUtils.substring(permissionString, braceRight+1);
            if (StringUtils.isEmpty(perms)) {
                throw new IllegalArgumentException("Invalid format - no perms (e.g. 'site:<site_library_id>::{<ownerId>}h,S,r') for permissions string: "+permissionString);
            }
            // get the permissions ([0] = hidden, [1] = reusable, [2] = remixable)
            String[] permissions = StringUtils.split(perms, ',');
            decodedPermissions.put(METADATA_HIDDEN, permissions[0]);
            decodedPermissions.put(METADATA_REUSABLE, permissions[1]);
            decodedPermissions.put(METADATA_REMIXABLE, permissions[2]);

        } else { // load default metadata
            if (failIfInvalid) {
                throw new IllegalArgumentException("Invalid string format - fails to match basic rules (e.g. not empty, contains '::{' and starts with 'site' or 'playlist') for permissions string: "+permissionString);
            } else {
                decodedPermissions.put(METADATA_CONTAINER_TYPE, "");
                decodedPermissions.put(METADATA_CONTAINER_ID, "");
                decodedPermissions.put(METADATA_OWNER, null);
                decodedPermissions.put(METADATA_HIDDEN, DEFAULT_METADATA_HIDDEN);
                decodedPermissions.put(METADATA_REUSABLE, DEFAULT_METADATA_REUSABLE);
                decodedPermissions.put(METADATA_REMIXABLE, DEFAULT_METADATA_REMIXABLE);
            }
        }
        return decodedPermissions;
    }

    /**
     * builds a permissions data HashMap, ensuring the permissions are:
     * 1. in the required order (hidden, reusable, remixable) and 
     * 2. contain all permissions values (adds defaults as needed)
     * 
     * @param permissions the HashMap of permissions
     * @return the correctly ordered and filled permissions HashMap
     */
    protected Map<String, String> buildEntryMetadataPermissionsMap(Map<String, String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            permissions = new HashMap<String, String>(0);
        }
        Map<String, String> permissionsMap = new LinkedHashMap<String, String>();
        permissionsMap.put(METADATA_HIDDEN, permissions.containsKey(METADATA_HIDDEN) ? permissions.get(METADATA_HIDDEN) : DEFAULT_METADATA_HIDDEN);
        permissionsMap.put(METADATA_REUSABLE, permissions.containsKey(METADATA_REUSABLE) ? permissions.get(METADATA_REUSABLE) : DEFAULT_METADATA_REUSABLE);
        permissionsMap.put(METADATA_REMIXABLE, permissions.containsKey(METADATA_REMIXABLE) ? permissions.get(METADATA_REMIXABLE) : DEFAULT_METADATA_REMIXABLE);
        return permissionsMap;
    }

    /**
     * removes any unused fields from the metadata that are not currently used
     * 
     * @param fields the HashMap of fields for the object
     * @param metadataProfileName the name of the metadata profile
     * @return the fields HashMap, sans unused metadata fields
     * @throws IllegalArgumentException if parameters are invalid
     */
    private Map<String, String> stripUnusedMetadataFields(Map<String, String> fields, String metadataProfileName) {
        if (StringUtils.isEmpty(metadataProfileName)) {
            throw new IllegalArgumentException("metadataProfileName must be set");
        }
        Map<String, String> currentFields = initialMetadataFields.get(metadataProfileName);
        fields.keySet().retainAll(currentFields.keySet());
        return fields;
    }

    /**
     * creates the XML data string representation
     * 
     * @param fields HashMap of the metadata fields
     * @return string representation of the XML data
     * @throws IllegalArgumentException if parameters are invalid
     */
    protected String createPlaylistXmlMetadataString(Map<String, String> fields) {
        if (fields == null) {
            throw new IllegalArgumentException("fields must not be null");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(XML_HEADER);
        sb.append("<metadata>");
        for (String f : fields.keySet()) {
            sb.append("<");
            sb.append(f);
            sb.append(">");
            sb.append(fields.get(f));
            sb.append("</");
            sb.append(f);
            sb.append(">");
        }
        sb.append("</metadata>");
        String xmlData = sb.toString();
        return xmlData;
    }

    /**
     * creates the XML data string representation for an entry
     * 
     * @param fields ArrayList of strings of the metadata fields
     * @return string representation of the XML data
     * @throws IllegalArgumentException if parameters are invalid
     */
    protected String createEntryXmlMetadataString(List<String> fields) {
        if (fields == null) {
            throw new IllegalArgumentException("fields must not be null");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(XML_HEADER);
        sb.append("<metadata>");
        for (String f : fields) {
            sb.append("<Permissions>"); 
            sb.append(f); 
            sb.append("</Permissions>"); 
        }
        sb.append("</metadata>");
        String xmlData = sb.toString();
        return xmlData;
    }


    /* XSLT test data
<metadata>
<Permissions>site:abcdefg-123456-id1::{azeckoski}h,S,r</Permissions>
<Permissions>site:hjklqwe-098765-id2::{person2}h,S,r</Permissions>
<Permissions>playlist:5678::{azeckoski}h,S,r</Permissions>
<Permissions>playlist:7890::{azeckoski}H,s,r</Permissions>
<Permissions>playlist:1234::{person1}H,s,R</Permissions>
</metadata>
     */

    /**
     * Create XSLT to add or update permissions to a metadata entry, 
     * NOTE: this should work even if the permission does not exist or happens to exist twice in the XML
     * 
     * @param permission the string representation of the permissions to add or update
     *      ("site:<siteId>::{<ownerId>}h,S,r" OR "playlist:<playlistId>::{<ownerId>}h,S,r"),
     *      should be geneated from {@link #encodeMetadataPermissions(String, String, String, Map)}
     * @return string representation of the xslt to transform the metadata XML
     * @throws IllegalArgumentException if input is invalid
     */
    private String createEntryAddUpdateXsltMetadataString(String permission) {
        /*
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 <xsl:output method="xml" encoding="utf-8" indent="no"/>
 <xsl:template match="@*|node()">
  <xsl:copy>
   <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
 </xsl:template>
 <xsl:template match="Permissions[starts-with(.,'site:abcdefg-1234567-id1::{')]" />
 <xsl:template match="metadata">
  <xsl:copy>
   <xsl:apply-templates select="@*|node()" />
   <Permissions>site:abcdefg-1234567-id1::{person3}H,s,R</Permissions>
  </xsl:copy>
 </xsl:template>
</xsl:stylesheet>
         */
        if (permission == null || permission.length() == 0) {
            throw new IllegalArgumentException("currentPermission must not be null or empty");
        }
        Map<String, String> perms = decodeMetadataPermissions(permission, true); // dies if invalid
        String prefix = perms.get(METADATA_CONTAINER_TYPE) + ":" + perms.get(METADATA_CONTAINER_ID) + "::{";
        String xsltData = "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"> <xsl:output method=\"xml\" encoding=\"utf-8\" indent=\"no\"/> <xsl:template match=\"@*|node()\"> <xsl:copy> <xsl:apply-templates select=\"@*|node()\"/> </xsl:copy> </xsl:template> <xsl:template match=\"Permissions[starts-with(.,'"+prefix+"')]\" /> <xsl:template match=\"metadata\"> <xsl:copy> <xsl:apply-templates select=\"@*|node()\" /> <Permissions>"+permission+"</Permissions> </xsl:copy> </xsl:template> </xsl:stylesheet>";
        return xsltData;
    }

    /**
     * Create XSLT to add permissions to a metadata entry, 
     * NOTE: ensure this permission is not already present or this will create duplicates
     * 
     * @param newPermission the string representation of the permissions to add
     *      ("site:<siteId>::{<ownerId>}h,S,r" OR "playlist:<playlistId>::{<ownerId>}h,S,r"),
     *      should be geneated from {@link #encodeMetadataPermissions(String, String, String, Map)}
     * @return string representation of the xslt to transform the metadata XML
     * @throws IllegalArgumentException if input is invalid
     * @deprecated use {@link #createEntryAddUpdateXsltMetadataString(String)} instead
     */
    @SuppressWarnings("unused")
    private String createEntryAddXsltMetadataString(String newPermission) {
        /*
Add new perms for site: site:dfsjdfsjkh-32498723478-id3::{person3}H,s,R
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 <xsl:output method="xml" encoding="utf-8" indent="no"/>
 <xsl:template match="@*|node()">
  <xsl:copy>
   <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
 </xsl:template>
 <xsl:template match="metadata">
  <xsl:copy>
   <xsl:apply-templates select="@*|node()" />
   <Permissions>site:dfsjdfsjkh-32498723478-id3::{person3}H,s,R</Permissions>
  </xsl:copy>
 </xsl:template>
</xsl:stylesheet>
         */
        if (newPermission == null || newPermission.length() == 0) {
            throw new IllegalArgumentException("currentPermission must not be null or empty");
        }
        decodeMetadataPermissions(newPermission, true); // dies if invalid
        String xsltData = "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"> <xsl:output method=\"xml\" encoding=\"utf-8\" indent=\"no\"/> <xsl:template match=\"@*|node()\"> <xsl:copy> <xsl:apply-templates select=\"@*|node()\"/> </xsl:copy> </xsl:template> <xsl:template match=\"metadata\"> <xsl:copy> <xsl:apply-templates select=\"@*|node()\" /> <Permissions>"+newPermission+"</Permissions> </xsl:copy> </xsl:template> </xsl:stylesheet>";
        return xsltData;
    }

    /**
     * Create XSLT to update the permissions in a metadata entry,
     * NOTE: this will not break anything if the item to update is not present
     * 
     * @param updatedPermission the updated string representation of the permissions
     *      ("site:<siteId>::{<ownerId>}h,S,r" OR "playlist:<playlistId>::{<ownerId>}h,S,r"),
     *      should be geneated from {@link #encodeMetadataPermissions(String, String, String, Map)}
     * @return string representation of the xslt to transform the metadata XML
     * @throws IllegalArgumentException if input is invalid
     * @deprecated use {@link #createEntryAddUpdateXsltMetadataString(String)} instead
     */
    @SuppressWarnings("unused")
    private String createEntryUpdateXsltMetadataString(String updatedPermission) {
        /*
Change perms for the site: (site:abcdefg-123456-id1) to H,S,R --------
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 <xsl:output method="xml" encoding="utf-8" indent="no"/>
 <xsl:template match="@*|node()">
  <xsl:copy>
   <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
 </xsl:template>
 <xsl:template match="Permissions/text()[starts-with(.,'site:abcdefg-123456-id1::{')]">
  <xsl:text>site:abcdefg-123456-id1::{azeckoski}H,S,R</xsl:text>
 </xsl:template>
</xsl:stylesheet>
         */
        if (updatedPermission == null || updatedPermission.length() == 0) {
            throw new IllegalArgumentException("currentPermission must not be null or empty");
        }
        Map<String, String> perms = decodeMetadataPermissions(updatedPermission, true); // dies if invalid
        String prefix = perms.get(METADATA_CONTAINER_TYPE) + ":" + perms.get(METADATA_CONTAINER_ID) + "::{";
        String xsltData = "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"> <xsl:output method=\"xml\" encoding=\"utf-8\" indent=\"no\"/> <xsl:template match=\"@*|node()\"> <xsl:copy> <xsl:apply-templates select=\"@*|node()\"/> </xsl:copy> </xsl:template> <xsl:template match=\"Permissions/text()[starts-with(.,'"+prefix+"')]\"> <xsl:text>"+updatedPermission+"</xsl:text> </xsl:template> </xsl:stylesheet>";
        return xsltData;
    }

    /**
     * Create XSLT to remove permissions from a metadata entry, 
     * NOTE: this will not break anything if the item to update is not present
     * 
     * @param containerType site or playlist
     * @param containerId the id of the site or playlist
     * @return string representation of the xslt to transform the metadata XML
     * @throws IllegalArgumentException if input is invalid
     */
    protected String createEntryRemoveXsltMetadataString(String containerType, String containerId) {
        /*
Remove perms for site: (site:abcdefg-123456-id1)
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 <xsl:output method="xml" encoding="utf-8" indent="no"/>
 <xsl:template match="@*|node()">
  <xsl:copy>
   <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
 </xsl:template>
 <xsl:template match="Permissions[starts-with(.,'site:abcdefg-123456-id1::{')]" />
</xsl:stylesheet>
         */
        if (StringUtils.isEmpty(containerType)) {
            throw new IllegalArgumentException("containerType must not be null or empty");
        }
        if (StringUtils.isEmpty(containerId)) {
            throw new IllegalArgumentException("containerId must not be null or empty");
        }
        String prefix = containerType + ":" + containerId + "::{";
        String xsltData = "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"> <xsl:output method=\"xml\" encoding=\"utf-8\" indent=\"no\"/> <xsl:template match=\"@*|node()\"> <xsl:copy> <xsl:apply-templates select=\"@*|node()\"/> </xsl:copy> </xsl:template> <xsl:template match=\"Permissions[starts-with(.,'"+prefix+"')]\" /> </xsl:stylesheet>";
        return xsltData;
    }

    //END metadata methods (SKE-90)

    /**
     * Fetch all the kaltura entries for a given username (not the Sakai user id, the Sakai username which is the kaltura userId)
     * 
     * @param username not the Sakai user id, the Sakai username which is the kaltura userId
     * @param textFilter a search filter to filter the items by
     * @param start 0 for all, or >0 start with that item
     * @param max 0 for all, or >0 to only return that many
     * @return the List of kaltura entries
     */
    public List<KalturaMediaEntry> getKalturaItemsForUser(String username, String textFilter, int start, int max) {
        if (start < 0) {
            start = 0;
        }
        if (max <= 0) {
            max = MAX_ENTRIES_FROM_KALTURA;
        }
        if (StringUtils.isBlank(textFilter)) {
            textFilter = "";
        }
        if (log.isDebugEnabled()) log.debug("getKalturaItemsForUser(username="+username+", textFilter="+textFilter+", start="+start+", max="+max+"), kalturaEnabled="+this.kalturaEnabled);
        List<KalturaMediaEntry> items = new ArrayList<KalturaMediaEntry>(0);
        if (! this.kalturaEnabled) {
            log.warn("Kaltura is disabled, please enable it in sakai.properties, see documentation for details");
        } else {
            if (isOfflineMode()) {
                log.warn("OFFLINE MODE");
                items = new ArrayList<KalturaMediaEntry>(10);
                for (int i = 0; i < 10; i++) {
                    KalturaMediaEntry kme = makeSampleKME(i+"");
                    items.add(kme);
                }
            } else {
                KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
                if (kc != null) {
                    try {
                        KalturaMediaEntryFilter filter = new KalturaMediaEntryFilter();
                        filter.partnerIdEqual = this.kalturaConfig.getPartnerId();
                        if (StringUtils.isNotBlank(textFilter)) {
                            filter.searchTextMatchOr = textFilter; // I think this is what I need but it does not seem to prioritize results?
                            //filter.nameLike = textFilter;
                        }
                        filter.statusIn = "0,1,2"; // KalturaEntryStatus.IMPORT+","+KalturaEntryStatus.PRECONVERT+","+KalturaEntryStatus.READY;
                        // limit to only the entries for this user
                        filter.userIdEqual = username;
                        //kmef.orderBy = "title";

                        items = fetchMediaEntriesFromKaltura(kc, filter, max, start);

                    } catch (KalturaApiException e) {
                        log.error("Unable to get kaltura media items listing using session (oid="+kc.toString()+", tid="+Thread.currentThread().getId()+", ks="+kc.getSessionId()+"):: " + e, e);
                    }
                }
            }
        }
        return items;
    }

    /**
     * Retrieve a set of kaltura items using a list of keids
     * CACHED
     * 
     * @param textFilter a search filter string, null or "" includes all
     * @param keids [OPTIONAL] listing of keids to limit the results to
     * @param start 0 for all, or >0 start with that item
     * @param max 0 for all, or >0 to only return that many
     * @return the List of kaltura entries
     */
    public List<KalturaMediaEntry> getKalturaItems(String textFilter, String[] keids, int start, int max) {
        if (start < 0) {
            start = 0;
        }
        if (max <= 0) {
            max = MAX_ENTRIES_FROM_KALTURA;
        }
        if (StringUtils.isBlank(textFilter)) {
            textFilter = "";
        }
        if (log.isDebugEnabled()) log.debug("getKalturaItems(textFilter="+textFilter+", keids="+Arrays.toString(keids)+", start="+start+", max="+max+"), kalturaEnabled="+this.kalturaEnabled);
        List<KalturaMediaEntry> items = new ArrayList<KalturaMediaEntry>();
        if (! this.kalturaEnabled) {
            log.warn("Kaltura is disabled, please enable it in sakai.properties, see documentation for details");
        } else {
            if (isOfflineMode()) {
                log.warn("OFFLINE MODE");
                for (int i = 0; i < keids.length; i++) {
                    KalturaMediaEntry kme = makeSampleKME(i+"");
                    kme.id = keids[i];
                    items.add(kme);
                }
            } else {
                String[] originalKeids = (String[]) ArrayUtils.clone(keids);
                if ("".equals(textFilter) 
                        && keids != null 
                        && keids.length > 0) {
                    // no search filter so use the cache to get the items by ids
                    log.debug("getKalturaItems: no search filter so using cache to retrieve items: "+Arrays.toString(keids));
                    // See if this stuff is in the cache already
                    List<String> kalturaEIds = new ArrayList<String>( Arrays.asList(keids) );
                    for (Iterator<String> iterator = kalturaEIds.iterator(); iterator.hasNext();) {
                        String keid = iterator.next();
                        Element el = entriesCache.get(keid);
                        if (el != null) {
                            KalturaMediaEntry entry = (KalturaMediaEntry) el.getObjectValue();
                            if (entry != null) {
                                // skip cached items which are in a ready status (so refetch items which are not ready)
                                if (entry.status != KalturaEntryStatus.READY) {
                                    // not ready so we leave it in the list
                                    log.warn("Skipped over cached item with status: "+entry.status);
                                    continue;
                                }
                                if (log.isDebugEnabled()) {
                                    log.debug("Retrieved "+entry.id+" from cache: "+entriesCache.getSize());
                                }
                                iterator.remove(); // take this one out of the list to search
                                items.add(entry); // add the cache value to return list
                            }
                        }
                    }
                    // update keids
                    keids = kalturaEIds.toArray(new String[kalturaEIds.size()]);
                }

                if (!"".equals(textFilter) 
                        || (keids != null && keids.length > 0)
                        ) {
                    if (log.isDebugEnabled()) log.debug("getKalturaItems: doing kaltura server search with filter ("+textFilter+") and "+(keids != null ? keids.length : 0)+" keids: "+Arrays.toString(keids));
                    // we have a search filter OR some keids to lookup (maybe both)
                    KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
                    if (kc != null) {
                        try {
                            KalturaMediaEntryFilter filter = new KalturaMediaEntryFilter();
                            filter.partnerIdEqual = this.kalturaConfig.getPartnerId();
                            if (StringUtils.isNotBlank(textFilter)) {
                                filter.searchTextMatchOr = textFilter; // I think this is what I need but it does not seem to prioritize results?
                                //filter.nameLike = textFilter;
                            }
                            filter.statusIn = "0,1,2"; // KalturaEntryStatus.IMPORT+","+KalturaEntryStatus.PRECONVERT+","+KalturaEntryStatus.READY;
                            // limit to a set of items as needed
                            if (keids != null) {
                                filter.idIn = StringUtils.join(keids, ',');
                            }
                            //kmef.orderBy = "title";

                            List<KalturaMediaEntry> fetchedItems = fetchMediaEntriesFromKaltura(kc, filter, max, start); // this CACHES the found items
                            if (items.isEmpty()) {
                                if (log.isDebugEnabled()) log.debug("getKalturaItems: No entries in set from cache: fetched "+fetchedItems.size()+" from Kaltura server using filter: "+filter.idIn);
                                items = fetchedItems;
                            } else if (fetchedItems.isEmpty()) {
                                if (log.isDebugEnabled()) log.debug("getKalturaItems: No entries found on Kaltura server using filter: "+filter.idIn);
                                // do nothing
                            } else {
                                // merge the results in order of the incoming ids
                                if (log.isDebugEnabled()) log.debug("getKalturaItems: Merging "+fetchedItems.size()+" server entries and "+items.size()+" cache entries");
                                HashMap<String, KalturaMediaEntry> kmeMap = new HashMap<String, KalturaMediaEntry>();
                                for (KalturaMediaEntry kme : items) {
                                    kmeMap.put(kme.id, kme);
                                }
                                for (KalturaMediaEntry kme : fetchedItems) {
                                    kmeMap.put(kme.id, kme);
                                }
                                items.clear();
                                for (int i = 0; i < originalKeids.length; i++) {
                                    String keid = originalKeids[i];
                                    KalturaMediaEntry kme = kmeMap.get(keid);
                                    if (kme != null) {
                                        items.add( kmeMap.get(keid) );
                                    } else {
                                        if (log.isDebugEnabled()) log.debug("getKalturaItems: Merging warning: item "+keid+" not found on server or in cache, skipping...");
                                    }
                                }
                                if (log.isDebugEnabled()) log.debug("getKalturaItems: Merging complete,  "+items.size()+" entries combined into set for original keids request: "+ArrayUtils.toString(originalKeids));
                            }

                        } catch (KalturaApiException e) {
                            log.error("getKalturaItems: Unable to get kaltura media items listing using session (oid="+kc.toString()+", tid="+Thread.currentThread().getId()+", ks="+kc.getSessionId()+"):: " + e, e);
                        }
                    }
                }
            }
        }
        return items;
    }

    /**
     * Fetch entries based on a given filter but by chunks so that we can get around the 500 items limit
     * NOTE: caches all entries
     * 
     * @param kc the kaltura client (with appropriate permissions)
     * @param filter the filter for the entries search
     * @param max maximum num of entries to return, defaults to MAX_ENTRIES_FROM_KALTURA if < 0 or > MAX_ENTRIES_FROM_KALTURA
     * @param start entry number in the complete set to start with, defaults to the begining if <= 0
     * @return the complete list of entries (up to the max) - List of KalturaMediaEntry
     * @throws KalturaApiException
     */
    private List<KalturaMediaEntry> fetchMediaEntriesFromKaltura(KalturaClient kc, 
            KalturaMediaEntryFilter filter, int max, int start) throws KalturaApiException {
        if (kc == null) {
            throw new IllegalArgumentException("KalturaClient kc must be set");
        }
        if (filter == null) {
            throw new IllegalArgumentException("KalturaMediaEntryFilter filter must be set");
        }
        if (max < 0 || max > MAX_ENTRIES_FROM_KALTURA) {
            max = MAX_ENTRIES_FROM_KALTURA;
        }
        if (start < 0) {
            start = 0;
        }
        ArrayList<KalturaMediaEntry> entries = new ArrayList<KalturaMediaEntry>();
        if (max > 0) {
            KalturaMediaService entryService = kc.getMediaService();
            ArrayList<KalturaMediaEntry> kmes = new ArrayList<KalturaMediaEntry>();
            KalturaFilterPager pager = new KalturaFilterPager();
            pager.pageSize = MAX_ENTRIES_PER_REQUEST;
            // FIXME handle the start item (currently we always start on the first one) -AZ
            KalturaMediaListResponse listResponse = entryService.list(filter, pager);
            kmes.addAll(listResponse.objects);
            if (listResponse.totalCount <= MAX_ENTRIES_PER_REQUEST) {
                // nothing else to do so done
            } else {
                // need to loop until we have all the items or reach the max
                int maxIterations = roundUp(max, MAX_ENTRIES_PER_REQUEST);
                if (listResponse.totalCount < max) {
                    maxIterations = roundUp(listResponse.totalCount, MAX_ENTRIES_PER_REQUEST);
                }
                for (int i = 2; i <= maxIterations; i++) {
                    // NOTE - kaltura does not support a start item in the paging API, only a start page (first page = 1)
                    pager.pageIndex = i;
                    listResponse = entryService.list(filter, pager);
                    kmes.addAll(listResponse.objects);
                }
            }
            for (KalturaMediaEntry kme : kmes) {
                entries.add(kme); // KalturaMediaEntry
                if (kme.status == KalturaEntryStatus.READY) {
                    // only cache items which are in a ready status
                    entriesCache.put( new Element(kme.id, kme) );
                    if (log.isDebugEnabled()) {
                        log.debug("Adding "+kme.id+" to cache: "+entriesCache.getSize());
                    }
                } else {
                    log.warn("kbe ("+kme.id+") status ("+kme.status+") is not in ready status ("+KalturaEntryStatus.READY+") so cannot be cached");
                }
            }
        }
        return entries;
    }

    /**
     * Divide 2 numbers and get the ceil result
     * @param num
     * @param divisor
     * @return the result rounded up to the nearest whole number
     */
    static int roundUp(int num, int divisor) {
        return (num + divisor - 1) / divisor;
    }

    /**
     * Retrieve a single KME by the kaltura id
     * CACHED
     * 
     * @param keid the kaltura entry id
     * @return the entry OR null if none found
     */
    public KalturaMediaEntry getKalturaItem(String keid) {
        if (keid == null) {
            throw new IllegalArgumentException("keid must not be null");
        }
        if (log.isDebugEnabled()) log.debug("getKalturaItem(keid="+keid+"), kalturaEnabled="+this.kalturaEnabled);
        KalturaMediaEntry kme = null;
        if (! this.kalturaEnabled) {
            log.warn("Kaltura is disabled, please enable it in sakai.properties, see documentation for details");
        } else {
            Element el = entriesCache.get(keid);
            if (el != null) {
                kme = (KalturaMediaEntry) el.getObjectValue();
                if (log.isDebugEnabled()) {
                    log.debug("Retreived "+keid+" from cache: "+entriesCache.getSize());
                }
            } else {
                if (isOfflineMode()) {
                    log.warn("OFFLINE MODE");
                    kme = makeSampleKME(0+"");
                    kme.id = keid;
                } else {
                    KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
                    if (kc != null) {
                        try {
                            KalturaMediaService mediaService = kc.getMediaService();
                            kme = getKalturaEntry(keid, mediaService);
                        } catch (KalturaApiException e) {
                            log.warn("Unable to find kaltura media item ("+keid+") using session (ks="+kc.getSessionId()+"):: " + e);
                        }
                    }
                }
                if (kme != null) {
                    entriesCache.put( new Element(keid, kme) );
                    if (log.isDebugEnabled()) {
                        log.debug("Adding "+kme.id+" to cache: "+entriesCache.getSize());
                    }
                }
            }
        }
        return kme;
    }

    /**
     * Delete a kaltura entry from the kaltura server
     * CACHE AWARE
     * 
     * @param keid the kaltura id
     * @return true if the entry was found and removed
     */
    public boolean removeKalturaItem(String keid) {
        if (keid == null) {
            throw new IllegalArgumentException("keid must not be null");
        }
        boolean removed = false;
        if (log.isDebugEnabled()) log.debug("removeKalturaItem(keid="+keid+"), kalturaEnabled="+this.kalturaEnabled);
        if (! this.kalturaEnabled) {
            log.warn("Kaltura is disabled, please enable it in sakai.properties, see documentation for details");
        } else {
            if (isOfflineMode()) {
                log.warn("OFFLINE MODE");
                removed = true;
            } else {
                KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
                if (kc != null) {
                    try {
                        KalturaMediaService entryService = kc.getMediaService();
                        KalturaMediaEntry entry = getKalturaEntry(keid, entryService);
                        entryService.delete(entry.id);
                        removed = true;
                    } catch (KalturaApiException e) {
                        log.error("Unable to remove kaltura item ("+keid+") using session (oid="+kc.toString()+", tid="+Thread.currentThread().getId()+", ks="+kc.getSessionId()+"):: " + e, e);
                        removed = false;
                    }
                }
            }
            entriesCache.remove(keid);
        }
        return removed;
    }

    public KalturaMediaEntry createKalturaEntry(KalturaMediaEntry kalturaEntry) {
        if (kalturaEntry == null) {
            throw new IllegalArgumentException("entry must not be null");
        }
        if (log.isDebugEnabled()) log.debug("createKalturaEntry(kalturaEntry="+kalturaEntry.id+")");
        String keid = kalturaEntry.id;
        KalturaMediaEntry kbe = null;
        if (keid != null) {
            // existing entry so update instead
            kbe = updateKalturaItem(kalturaEntry);
        } else {
            if (! this.kalturaEnabled) {
                log.warn("Kaltura is disabled, please enable it in sakai.properties, see documentation for details");
            } else {
                if (isOfflineMode()) {
                    log.warn("OFFLINE MODE");
                    kbe = makeSampleKME(0+"");
                    kbe.id = keid;
                } else {
                    KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
                    if (kc != null) {
                        try {
                            KalturaMediaService entryService = kc.getMediaService();
                            kbe = getKalturaEntry(keid, entryService);
                            if (kbe == null) {
                                throw new IllegalArgumentException("Cannot find KME to update using id ("+keid+")");
                            }
                            // integrate the fields we allow to be changed
                            KalturaMediaEntry fields = new KalturaMediaEntry();
                            //fields.creditUrl = entry.creditUrl;
                            //fields.creditUserName = entry.creditUserName;
                            fields.description = kalturaEntry.description;
                            fields.name = kalturaEntry.name;
                            fields.tags = kalturaEntry.tags;
                            // now update the KME
                            kbe = entryService.update(keid, fields);
                            // have to remove the entry for now because it is always a KalturaBaseEntry and we need Media or Mix
                            entriesCache.remove(kbe.id);
                            //entriesCache.put( new Element(kbe.id, kbe) );
                        } catch (KalturaApiException e) {
                            String msg = "Unable to update kaltura media item ("+keid+") using session (oid="+kc.toString()+", tid="+Thread.currentThread().getId()+", ks="+kc.getSessionId()+"):: " + e;
                            log.error(msg, e);
                            throw new RuntimeException(msg, e);
                        }
                    }
                }
            }
        }
        return kbe;
    }

    /**
     * Updates the fields of a kaltura entry (limited to description, name, tags)
     * CACHE AWARE
     * 
     * @param kalturaEntry entry with updated fields
     * @return the updated entry
     */
    public KalturaMediaEntry updateKalturaItem(KalturaMediaEntry kalturaEntry) {
        if (kalturaEntry == null) {
            throw new IllegalArgumentException("entry must not be null");
        }
        String keid = kalturaEntry.id;
        if (keid == null) {
            throw new IllegalArgumentException("entry keid must not be null");
        }
        KalturaMediaEntry kbe = null;
        if (log.isDebugEnabled()) log.debug("createKalturaMix(kalturaEntry="+kalturaEntry+"), kalturaEnabled="+this.kalturaEnabled);
        if (! this.kalturaEnabled) {
            log.warn("Kaltura is disabled, please enable it in sakai.properties, see documentation for details");
        } else {
            if (isOfflineMode()) {
                log.warn("OFFLINE MODE");
                kbe = makeSampleKME(0+"");
                kbe.id = keid;
            } else {
                KalturaClient kc = getKalturaClient(KS_PERM_ADMIN);
                if (kc != null) {
                    try {
                        KalturaMediaService entryService = kc.getMediaService();
                        kbe = getKalturaEntry(keid, entryService);
                        if (kbe == null) {
                            throw new IllegalArgumentException("Cannot find KME to update using id ("+keid+")");
                        }
                        // integrate the fields we allow to be changed
                        KalturaMediaEntry fields = new KalturaMediaEntry();
                        //fields.creditUrl = entry.creditUrl;
                        //fields.creditUserName = entry.creditUserName;
                        fields.description = kalturaEntry.description;
                        fields.name = kalturaEntry.name;
                        fields.tags = kalturaEntry.tags;
                        // now update the KME
                        kbe = entryService.update(keid, fields);
                        // have to remove the entry for now because it is always a KalturaBaseEntry and we need Media or Mix
                        entriesCache.remove(kbe.id);
                        //entriesCache.put( new Element(kbe.id, kbe) );
                    } catch (KalturaApiException e) {
                        String msg = "Unable to update kaltura media item ("+keid+") using session (oid="+kc.toString()+", tid="+Thread.currentThread().getId()+", ks="+kc.getSessionId()+"):: " + e;
                        log.error(msg, e);
                        throw new RuntimeException(msg, e);
                    }
                }
            }
        }
        return kbe;
    }


    /**
     * Get the KME with a permissions check to make sure the user key matches
     * @param keid the kaltura entry id
     * @param entryService the katura entry service
     * @return the entry
     * @throws KalturaApiException if kaltura cannot be accessed
     * @throws IllegalArgumentException if the keid cannot be found for this user
     */
    private KalturaMediaEntry getKalturaEntry(String keid, KalturaMediaService entryService) throws KalturaApiException {
        if (log.isDebugEnabled()) log.debug("getKalturaEntry(keid="+keid+")");
        // DO NOT CACHE THIS ONE
        KalturaMediaEntry entry = null;
        // Cannot use the KMEF because it cannot filter by id correctly -AZ
        /*
        KalturaBaseEntryFilter kmef = new KalturaBaseEntryFilter();
        kmef.partnerIdEqual = this.kalturaConfig.getPartnerId();
        kmef.userIdEqual = currentUserName;
        kmef.idEqual = keid;
        //kmef.orderBy = "title";
        KalturaMediaListResponse listResponse = mediaService.list(kmef);
        if (listResponse != null && ! listResponse.objects.isEmpty()) {
            kme = listResponse.objects.get(0); // just get the first one
        }
         */
        // have to use - mediaService.get(keid); despite it not even checking if we have access to this - AZ

        // Zendesk 4131 - Kaltura team suggests waiting a second and then retrying up to 10 times.
        for (int i = 0; i < 10; i++) {
            try {
                entry = entryService.get(keid);
            } catch (KalturaApiException kae) {
                // Gonen indicated that the entry service might throw an exception if it isn't found and wanted to trap this.
                // If this is the last past through the loop, we re-throw it since we are done here regardless.
                if (log.isDebugEnabled()) {
                    log.debug("KalturaApiException thrown and trapped", kae);
                }
                if (10 == i) {
                    throw kae;
                }
            }
            if (null != entry) {
                // No need to keep trying.
                break;
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                // We don't care - ignore
            }
        }
        if (entry == null) {
            // did not find the item by keid so we die
            throw new IllegalArgumentException("Cannot find kaltura item ("+keid+") for user ("+external.getCurrentUserId()+")");
        }
        // also do a manual check for security, not so sure about this check though -AZ
        if (entry.partnerId != this.kalturaConfig.getPartnerId()) {
            throw new SecurityException("KME partnerId ("+entry.partnerId+") does not match current one ("+this.kalturaConfig.getPartnerId()+"), cannot access this KME ("+keid+")");
        }
        return entry;
    }

    /**
     * Generate fake but realistic KME
     * @param i a unique-ish number for the set of items
     * @return sample KME
     */
    @NoProfile
    protected KalturaMediaEntry makeSampleKME(String i) {
        KalturaMediaEntry kme = new KalturaMediaEntry();
        kme.createdAt = (int) System.currentTimeMillis();
        kme.description = "this is a sample item, it is item number "+i;
        kme.id = "X_ABCDEFG"+i;
        kme.name = "sample "+i;
        kme.partnerId = this.kalturaConfig.getPartnerId();
        kme.tags = "aaron,test,tags";
        kme.thumbnailUrl = "images/sample_thumbnail.png";
        kme.type = KalturaEntryType.AUTOMATIC;
        kme.userId = external.getCurrentUserName();
        kme.version = 1;
        // media only
        kme.dataUrl = "images/sample_thumbnail.png";
        kme.duration = 100;
        kme.height = 300;
        kme.mediaDate = (int) System.currentTimeMillis();
        kme.mediaType = KalturaMediaType.IMAGE;
        kme.width = 400;
        return kme;
    }

    /**
     * Generate fake but realistic Kaltura playlist
     * @param i a unique-ish number for the set of items
     * @param itemsCount number of items in the playlist (will be fake)
     * @return sample playlist
     */
    @NoProfile
    protected KalturaPlaylist makeSampleKP(String i, int itemsCount) {
        KalturaPlaylist kp = new KalturaPlaylist();
        kp.createdAt = (int) System.currentTimeMillis();
        kp.description = "this is a sample playlist, it is item number "+i;
        kp.id = 1111+i;
        kp.name = "sample "+i;
        kp.partnerId = this.kalturaConfig.getPartnerId();
        kp.tags = "aaron,test,tags";
        kp.thumbnailUrl = "images/sample_thumbnail.png";
        kp.type = KalturaEntryType.PLAYLIST;
        kp.userId = external.getCurrentUserName();
        kp.version = 1;
        String content = "";
        for (int j = 0; j < itemsCount; j++) {
            if (j == 0) {
                content += "X_ABCDEFG0";
            } else {
                content += ",X_ABCDEFG"+j;
            }
        }
        kp.playlistContent = content;
        return kp;
    }

    /**
     * Generate fake but realistic media collection with data in it
     * @param title
     * @param sharing
     * @param itemsCount
     * @return the generated media colection and items
     */
    @NoProfile
    protected MediaCollection makeSampleMC(String title, String sharing, int itemsCount) {
        MediaCollection mc = new MediaCollection(UUID.randomUUID().toString(), external.getCurrentLocationId(), 
                external.getCurrentUserName(), title, false, sharing);
        mc.setKalturaPlaylist( makeSampleKP(new Random().nextInt(9999)+"", itemsCount) );
        // make some items
        if (itemsCount < 0) {
            itemsCount = new Random().nextInt(6);
        }
        ArrayList<MediaItem> items = new ArrayList<MediaItem>(itemsCount);
        for (int i = 0; i < itemsCount; i++) {
            KalturaMediaEntry kme = makeSampleKME(i+"");
            MediaItem mi = new MediaItem(mc, kme, null);
            if (i < 2) {
                mi.setHidden(false);
            }
            if (i < 1) {
                mi.setShared(true);
            }
            items.add(mi);
        }
        mc.setItems(items);
        return mc;
    }


    public enum Widget {PLAYER_IMAGE, PLAYER_AUDIO, PLAYER_VIDEO, PLAYER_EDIT, UPLOADER, UPLOADER_SPECIAL, EDITOR, CLIPPER, CLIPPER_PLAYER};

    /**
     * Returns the widget id if one is configured for the given type,
     * returns null to indicate there is no configuration available and the id should not be included
     * 
     * @param widgetType the type of widget to get the configuration id for
     * @return the configuration id OR null if none found
     */
    @NoProfile
    public String getKalturaWidgetId(Widget widgetType) {
        if (widgetType == null) {
            throw new IllegalArgumentException("widgetType must be set");
        }
        String playerId = null;
        if (Widget.PLAYER_VIDEO.equals(widgetType)) {
            playerId = this.kalturaPlayerIdView;
        } else if (Widget.PLAYER_EDIT.equals(widgetType)) {
            playerId = this.kalturaPlayerIdEdit;
            if (StringUtils.isBlank(playerId)) {
                // fallback to using the view player
                playerId = this.kalturaPlayerIdView;
            }
        } else if (Widget.PLAYER_IMAGE.equals(widgetType)) {
            playerId = this.kalturaPlayerIdImage;
        } else if (Widget.PLAYER_AUDIO.equals(widgetType)) {
            playerId = this.kalturaPlayerIdAudio;
        } else if (Widget.UPLOADER.equals(widgetType)) {
            playerId = this.kalturaUploaderId;
        } else if (Widget.UPLOADER_SPECIAL.equals(widgetType)) {
            playerId = this.kalturaUploaderSpecialId;
        } else if (Widget.EDITOR.equals(widgetType)) {
            playerId = this.kalturaEditorId;
        } else if (Widget.CLIPPER.equals(widgetType)) {
            playerId = this.kalturaClipperId;
        } else if (Widget.CLIPPER_PLAYER.equals(widgetType)) {
            playerId = this.kalturaClipperPlayerId;
        }
        return playerId;
    }

    /**
     * @param widgetType
     * @return the width for the given widget
     */
    @NoProfile
    public int getKalturaWidgetWidth(Widget widgetType) {
        if (widgetType == null) {
            throw new IllegalArgumentException("widgetType must be set");
        }
        int width = 480;
        if (Widget.PLAYER_VIDEO.equals(widgetType) || Widget.PLAYER_EDIT.equals(widgetType)) {
            width = this.kalturaPlayerVideoWidth;
        } else if (Widget.PLAYER_IMAGE.equals(widgetType)) {
            width = this.kalturaPlayerImageWidth;
        } else if (Widget.PLAYER_AUDIO.equals(widgetType)) {
            width = this.kalturaPlayerAudioWidth;
        }
        return width;
    }

    /**
     * @param widgetType
     * @return the height for the given widget
     */
    @NoProfile
    public int getKalturaWidgetHeight(Widget widgetType) {
        if (widgetType == null) {
            throw new IllegalArgumentException("widgetType must be set");
        }
        int width = 480;
        if (Widget.PLAYER_VIDEO.equals(widgetType) || Widget.PLAYER_EDIT.equals(widgetType)) {
            width = this.kalturaPlayerVideoHeight;
        } else if (Widget.PLAYER_IMAGE.equals(widgetType)) {
            width = this.kalturaPlayerImageHeight;
        } else if (Widget.PLAYER_AUDIO.equals(widgetType)) {
            width = this.kalturaPlayerAudioHeight;
        }
        return width;
    }

    /**
     * This will fill in data in the media item which is not available in the model,
     * the kalturaEntry should be populated before this is called
     * 
     * This will NOT fetch the item data from the DB OR fetch kaltura data from the server
     * 
     * @param item the item to populate
     * @throws SecurityException if not allowed
     */
    public void populateMediaItemKalturaData(MediaItem item) {
        item.setKalturaCDN(this.getKalturaCDN());
        int partnerId = this.getKalturaConfig().getPartnerId();
        item.setKalturaPartnerId(partnerId);
        // populate the player info - {SERVER_NAME}/kwidget/wid/_{PARTNER_ID}/ui_conf_id/{PLAYER_ID}/entryId/{ENTRY_ID}
        String playerURL = this.getKalturaConfig().getEndpoint() + "/kwidget/wid/_" + partnerId;
        String playerEntryURL = playerURL;
        // populate player JS url
        String playerJSURL = null; // use default
        String playerId = null;
        int playerWidth = 480;
        int playerHeight = 360;
        Widget widgetType = Widget.PLAYER_VIDEO; // default video
        if (item.findType().equals(MediaItem.TYPE_IMAGE)) {
            widgetType = Widget.PLAYER_IMAGE;
        } else if (item.findType().equals(MediaItem.TYPE_AUDIO)) {
            widgetType = Widget.PLAYER_AUDIO;
        }
        playerId = getKalturaWidgetId(widgetType);
        playerWidth = getKalturaWidgetWidth(widgetType);
        playerHeight = getKalturaWidgetHeight(widgetType);
        String userPlayerURL = playerURL;
        if (playerId != null) {
            if (item.findType().equals(MediaItem.TYPE_VIDEO) && item.isControl()) {
                String userPlayerId = getKalturaWidgetId(Widget.PLAYER_EDIT);
                userPlayerURL = playerURL + "/ui_conf_id/" + userPlayerId;
                playerURL +=  "/ui_conf_id/" + playerId;
            } else {
                playerURL +=  "/ui_conf_id/" + playerId;
                userPlayerURL = playerURL;
            }
            playerEntryURL = playerURL;
            if (item.getKalturaId() != null) {
                playerEntryURL += "/sus/ash/entry_id/" + item.getKalturaId();
            }
        }
        playerJSURL = findKalturaPlayerJSURL(playerId); // null if not enabled
        playerURL +=  "/entryId/" + item.getKalturaId();
        userPlayerURL +=  "/entryId/" + item.getKalturaId();
        item.setPlayerInfo(playerId, playerURL, userPlayerURL, playerJSURL, playerWidth, playerHeight, playerEntryURL);
        item.setDownloadKS(this.getKalturaClient(KS_PERM_DOWNLOAD).getSessionId());
        if (this.kalturaClippingEnabled) {
            // add in the clipping urls and info
            String clipperURL = this.getKalturaConfig().getEndpoint() + "/kgeneric/ui_conf_id//" + getKalturaWidgetId(Widget.CLIPPER);
            String endpoint = this.getKalturaConfig().getEndpoint();
            String host = endpoint.substring(endpoint.indexOf("://") + 3);
            String clipperFlashVars = "&entry_id=" + item.getKalturaId() + "&partner_id=" + this.getKalturaConfig().getPartnerId() 
                    + "&host=" + host + "&ks=" + this.getKalturaClient(KS_PERM_EDIT).getSessionId()
                    + "&show_add_delete_buttons=false&state=clippingState&jsReadyFunc=clipperReady&max_allowed_rows=1&show_control_bar=true&show_message_box=false";
            item.setClippingInfo(this.kalturaClipperId, clipperURL, clipperFlashVars);
        }
    }

    /**
     * Finds the correct JS player to use
     * @param playerId [OPTIONAL] the numeric id of the kaltura player
     * @return the HTML5 JS player for a given player or null if the player is not enabled. If for some reason the player id is
     * also null, then we return null.
     */
    protected String findKalturaPlayerJSURL(String playerId) {
        String playerJSURL = null; // should be null if not enabled or the player id is null.
        if (this.kalturaHtml5PlayerEnabled && StringUtils.isNotEmpty(playerId)) {
            int partnerId = this.getKalturaConfig().getPartnerId();
            // Use the URL provided if there was a property override set
            if (StringUtils.isNotEmpty(this.kalturaHtml5PlayerJS)) {
                playerJSURL = this.kalturaHtml5PlayerJS;
            } else {
                // build custom JS url: {KALTURA HOST}/p/{PARTNER_ID}/sp/{PARTNER_ID}00/embedIframeJs/uiconf_id/{PLAYER_ID}/partner_id/{PARTNER_ID}
                playerJSURL = this.getKalturaConfig().getEndpoint()+"/p/"+partnerId+"/sp/"+partnerId+"00/embedIframeJs/uiconf_id/"+playerId+"/partner_id/"+partnerId;
            }
        }
        return playerJSURL;
    }

    /**
     * @return true if the kaltura clipping is enabled
     */
    @NoProfile
    public boolean isKalturaClippingEnabled() {
        return this.kalturaClippingEnabled;
    }
    @NoProfile
    public void setKalturaClippingEnabled(boolean kalturaClippingEnabled) {
        this.kalturaClippingEnabled = kalturaClippingEnabled;
    }

}
