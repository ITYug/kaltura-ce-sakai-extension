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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.kaltura.logic.KalturaAPIService;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;


/**
 * This ensure that we are properly cleaning things up after each request
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class AllRequestInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {
        // clear out any existing Kaltura session in this thread
        kalturaAPIService.clearKalturaClient();
        return super.preHandle(request, response, handler);
    }

    private KalturaAPIService kalturaAPIService;
    public void setKalturaAPIService(KalturaAPIService kalturaAPIService) {
        this.kalturaAPIService = kalturaAPIService;
    }

}
