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

import static org.sakaiproject.authz.api.PermissionsHelper.DESCRIPTION;
import static org.sakaiproject.authz.api.PermissionsHelper.PREFIX;
import static org.sakaiproject.authz.api.PermissionsHelper.TARGET_REF;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.kaltura.logic.ExternalLogic;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Special Sakai only controller which allows the permissions helper to be used in the tool
 * 
 * @author chasegawa@unicon.net
 */
@SuppressWarnings("deprecation")
public class PermissionController extends AbstractController {
    final protected Log log = LogFactory.getLog(getClass());

    /**
     * The request is offloaded to the PermissionsHelper, therefore we return a null since the output stream should have already
     * been redirected/written to.
     * 
     * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ToolSession toolSession = sessionManager.getCurrentToolSession();
        toolSession.setAttribute(TARGET_REF, external.getCurrentLocationId());
        toolSession.setAttribute(PREFIX, "kaltura.");

        ResourceLoader permissionsResourceBundle = new ResourceLoader("permissions");
        HashMap<String, String> permissionsResourceBundleValues = new HashMap<String, String>();
        for (Object key : permissionsResourceBundle.keySet()) {
            permissionsResourceBundleValues.put((String) key, (String) permissionsResourceBundle.get(key));
        }
        toolSession.setAttribute("permissionDescriptions", permissionsResourceBundleValues);
        toolSession.setAttribute(DESCRIPTION, getPermissionsMessage(permissionsResourceBundle));

        ActiveTool helperTool = ActiveToolManager.getActiveTool("sakai.permissions.helper");
        toolSession.setAttribute(helperTool.getId() + Tool.HELPER_DONE_URL, request.getContextPath() + "/listCollections.htm");

        try {
            helperTool.help(request, response, "", "");
        } catch (ToolException e) {
            log.error("Exception trying to use permissions helper", e);
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Build the message for the top of the portlet from the resource bundle.
     */
    private String getPermissionsMessage(ResourceLoader permissionsResourceBundle) throws IdUnusedException {
        return permissionsResourceBundle.getFormattedMessage("permissions.description", new Object[]
            { toolManager.getCurrentTool().getTitle(), SiteService.getSite(toolManager.getCurrentPlacement().getContext()).getTitle() });
    }

    private ExternalLogic external;
    public void setExternal(ExternalLogic external) {
        this.external = external;
    }

    private SessionManager sessionManager;
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    private ToolManager toolManager;
    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }
}
