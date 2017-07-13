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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.azeckoski.reflectutils.ClassFields.FieldsFilter;
import org.azeckoski.reflectutils.ReflectUtils;
import org.azeckoski.reflectutils.transcoders.JSONTranscoder;
import org.sakaiproject.kaltura.logic.ExternalLogic;
import org.sakaiproject.kaltura.logic.KalturaAPIService;
import org.sakaiproject.kaltura.logic.KalturaAPIService.Widget;
import org.sakaiproject.kaltura.logic.MediaService;
import org.sakaiproject.kaltura.model.MediaCollection;
import org.sakaiproject.kaltura.model.MediaItem;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.kaltura.client.enums.KalturaEnumAsInt;
import com.kaltura.client.enums.KalturaEnumAsString;
import com.kaltura.client.types.KalturaBaseEntry;
import com.kaltura.client.types.KalturaMediaEntry;

/**
 * KalClipController provides processing logic for the kaltura clipping tool
 * 
 * @author Aaron Zeckoski, azeckoski@unicon.net
 * @version $Revision: 109352 $
 */
public class KalClipController extends AbstractController {

    final protected Log log = LogFactory.getLog(getClass());

    /**
     * The JSONTranscoder doesn't do well with ENUMs and doesn't give us the code we need, so we manually populate a map of the
     * values we need.
     * 
     * @param newEntry
     * @return JSON representation of the values from the KalturaBaseEntry
     */
    private Object getKalturaBaseEntryAsJSON(KalturaBaseEntry kalturaBaseEntry) {
        Map<String, Object> map = ReflectUtils.getInstance().getObjectValues(kalturaBaseEntry, FieldsFilter.SERIALIZABLE, false);
        for (String key : map.keySet()) {
            Object value = map.get(key);
            if (value instanceof KalturaEnumAsInt) {
                map.put(key, ((KalturaEnumAsInt) value).getHashCode());
            } else if (value instanceof KalturaEnumAsString) {
                map.put(key, ((KalturaEnumAsString) value).getHashCode());
            }
        }
        return JSONTranscoder.makeJSON(map);
    }

    private Object getSessionIdFromClipperVars(String clipperFlashVars) {
        StringTokenizer st = new StringTokenizer(clipperFlashVars, "&");
        while (st.hasMoreTokens()) {
            String value = st.nextToken();
            if (value.indexOf("ks=") > -1) {
                return value.substring(3);
            }
        }
        return null;
    }

    /**
     * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        Map<String,Object> model = new HashMap<String,Object>();
        // default model values
        model.put("entry", "");
        model.put("clipperUrl", "");
        model.put("clipperFlashvars", "");
        model.put("form_title", "");
        model.put("save_message", "");
        model.put("trim_note", "");
        model.put("json_entry", "{}");
        model.put("position", -1);
        model.put("fromSiteLibrary", false);

        String locationId = external.getCurrentLocationId();
        String userId = external.getCurrentUserId();
        String host = external.getConfigurationSetting("server.main.URL", "http://localhost");

        model.put("username", external.getUser(userId).username);
        model.put("locationId", locationId);
        model.put("isKalturaWrite", mediaService.isKalturaWrite(userId, locationId));
        Map<String,Object> config = new HashMap<String,Object>();
        // default config values
        config.put("show_embed", false);
        config.put("overwrite_entry", false);
        config.put("host", host);
        config.put("partner_id", mediaService.getKalturaPartnerId());
        config.put("kdp_uiconf_id", kalturaAPIService.getKalturaWidgetId(Widget.CLIPPER_PLAYER));
        config.put("clipper_uiconf_id", kalturaAPIService.getKalturaWidgetId(Widget.CLIPPER));
        config.put("redirect_save", false);
        config.put("redirect_url", "");

        model.put("config", config);

        // we need an entry id in all cases - cannot launch without one
        if (StringUtils.isBlank(request.getParameter("entryId"))) {
            throw new IllegalArgumentException("entryId must be set in order to launch clipping tool");
        }
        String entryId = request.getParameter("entryId");
        KalturaMediaEntry kbe = kalturaAPIService.getKalturaItem(entryId);
        if (kbe == null) {
            throw new IllegalArgumentException("kaltura entry (" + entryId + ") does not exist, only valid entries can be used");
        }
        model.put("entry", kbe);
        model.put("json_entry", getKalturaBaseEntryAsJSON(kbe));

        MediaItem mi = new MediaItem(locationId, entryId, userId, true, false, false);
        mi.setKalturaItem(kbe);
        mediaService.populateItem(mi);
        model.put("item", mi);
        model.put("collectionId", request.getParameter("collectionId"));
        String collectionId = request.getParameter("collectionId");
        MediaCollection mc = mediaService.getCollection(collectionId, -1, -1);
        if (mc != null) {
            List<MediaItem> mediaList = mc.getItems();
            for (int i = 0; i < mediaList.size(); i++) {
                if (mediaList.get(i).getKalturaId().equals(mi.getKalturaId())) {
                    model.put("position", i);
                    break;
                }
            }
        } else {
            model.put("fromSiteLibrary", true);
        }

        // media item contains the 3 urls: html5 library, entry player, clipper and the clipper flash vars
        model.put("html5Url", mi.getPlayerJSURL());
        model.put("kdpUrl", mi.getPlayerURL());
        model.put("clipperUrl", mi.getClipperURL());
        model.put("clipperFlashvars", mi.getClipperFlashVars());
        model.put("ks", getSessionIdFromClipperVars(mi.getClipperFlashVars()));

        if (config.get("overwrite_entry") != null && (Boolean) config.get("overwrite_entry")) {
            model.put("save_message", external.getI18nMessage("kalclip.trim_save_message", null));
            model.put("form_title", external.getI18nMessage("kalclip.trim_form_title", null));
            model.put("trim_note", external.getI18nMessage("kalclip.trim_note", null));
        } else {
            model.put("save_message", external.getI18nMessage("kalclip.clip_save_message", null));
            model.put("form_title", external.getI18nMessage("kalclip.clip_form_title", null));
            // no trim note
        }
        boolean isMyMedia = "true".equalsIgnoreCase(request.getParameter("isMyMedia"));
        boolean isKalturaWrite = mediaService.isKalturaWrite(userId, locationId);
        // NOTE: the replace button should NOT appear if:
        // - clipping from my media
        // - clipping from site library AND user does not have permissions to write to the site library
        // - clipping from a collection AND user does not have permissions to write to the collection
        // since we are dealing with a site lib or collection if it is not mymedia, we just check for kaltura write perm.
        boolean showReplaceButton = !isMyMedia && isKalturaWrite;
        model.put("showReplaceButton", showReplaceButton);
        model.put("config", config);
        return new ModelAndView("kalclip", model);
    }


    private MediaService mediaService;
    public void setMediaService(MediaService service) {
        this.mediaService = service;
    }

    private KalturaAPIService kalturaAPIService;
    public void setKalturaAPIService(KalturaAPIService kalturaAPIService) {
        this.kalturaAPIService = kalturaAPIService;
    }

    private ExternalLogic external;
    public void setExternal(ExternalLogic external) {
        this.external = external;
    }

}
