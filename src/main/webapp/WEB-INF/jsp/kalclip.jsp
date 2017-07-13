<%--

    Copyright 2010 Unicon (R) Licensed under the
    Educational Community License, Version 2.0 (the "License"); you may
    not use this file except in compliance with the License. You may
    obtain a copy of the License at

    http://www.osedu.org/licenses/ECL-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an "AS IS"
    BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
    or implied. See the License for the specific language governing
    permissions and limitations under the License.

--%>
<jsp:directive.include file="/WEB-INF/jsp/includes.jsp" />
<jsp:directive.include file="/WEB-INF/jsp/header.jsp" />
<script src="<c:url value="/javascript/jquery/jquery.time.stepper.js"/>" type="text/javascript"></script>
<script src="<c:url value="/javascript/kalclip.js"/>" type="text/javascript"></script>
<% /* vars: config, entry, clipperUrl, clipperFlashvars
form_title, save_message, trim_note, json_entry
config: show_embed, overwrite_entry, host, partner_id, kdp_uiconf_id, clipper_uiconf_id, redirect_save, redirect_url
*/ %>
<div>
  <c:choose>
    <c:when test="${fromSiteLibrary}">
      <a id="returnLink" href="viewCollection.htm">
        <spring:message code="uploadMedia.back.site.library" />
      </a>
      &nbsp;
    </c:when>
    <c:otherwise>
      <a id="returnLink" href="viewCollection.htm?collectionId=${collectionId}">
        <spring:message code="uploadMedia.back.collection" />
      </a>
    </c:otherwise>
  </c:choose>
</div>
  <div id="wrapper">
      <object id="kdp3" name="kdp3" type="application/x-shockwave-flash" wmode="window" allowFullScreen="true" allowNetworking="all"  
              allowScriptAccess="always" bgcolor="#000000" height="280" width="500" resource="${ kdpUrl }" data="${ kdpUrl }">
        <param name="allowFullScreen" value="true" />
        <param name="allowNetworking" value="all" />
        <param name="allowScriptAccess" value="always" />
        <param name="wmode" value="window" />
        <param name="bgcolor" value="#000000" />
        <param name="flashVars" value="&steamerType=rtmp&entryId=${ item.kalturaId }" />
        <param name="movie" value="${ kdpUrl }" />
      </object>
      <div id="form" class="form clearfix">
          <div id="newclip">
            <div class="disable"></div>
            <img id="loader" src="images/kalclip/loader.gif" alt="<spring:message code="app.saving" />" />
            <a href="#">${ form_title }</a>
          </div>
          <div id="embed" class="form clearfix">
              <p>${ save_message }</p>
              <br />
              <c:if test="${ config['show_embed'] }">
              <div class="item clearfix">
                  <label><spring:message code="kalclip.embed" /></label>
                  <input id="embedcode" class="text-field" type="text" value="" />
              </div>
              <br />
              </c:if>
          </div>
          <div id="fields">
              ${ trim_note }
              <div class="disable"></div>
              <div class="item clearfix">
                  <label><spring:message code="kalclip.starttime" /></label>
                  <input id="startTime" value="" />
              </div>
              <div class="item clearfix">
                  <label><spring:message code="kalclip.endtime" /></label>
                  <input id="endTime" value="" />
              </div>
              <div class="item clearfix">
                  <label><spring:message code="kalclip.title" /></label>
                  <input id="entry_title" class="text-field" type="text" value="${ item.name } by ${ username }" /><br /><br />
              </div>
              <div class="item clearfix">
                  <label><spring:message code="kalclip.description" /></label>
                  <textarea id="entry_desc">${ item.desc }</textarea><br /><br />
              </div>
          </div>
      </div>
  
      <object id="clipper" name="clipper" type="application/x-shockwave-flash" wmode="window" allowNetworking="all" allowScriptAccess="always" data="${ clipperUrl }">
        <param name="allowNetworking" value="all" />
        <param name="allowScriptAccess" value="always" />
        <param name="wmode" value="window" />
        <param name="bgcolor" value="#f8f8f8" />
        <param name="flashVars" value="${ clipperFlashvars }" />
        <param name="movie" value="${ clipperUrl }" />
      </object>
  
      <div id="actions" class="clearfix">
          <div class="disable"></div>
          <div class="left clearfix">
              <a href="#" id="setStartTime"><spring:message code="kalclip.setin" /></a>
              <a href="#" id="setEndTime"><spring:message code="kalclip.setout" /></a>
          </div>
          <div class="right clearfix">
              <a href="#" id="preview"><spring:message code="kalclip.preview" /></a>
              <span class="seperator"> | </span>
              <a href="#" id="delete"><spring:message code="kalclip.remove" /></a>
          </div>
      </div>
      <c:if test="${ isKalturaWrite }">
          <div id="options" class="clearfix">
              <div>
                  <input id="addToSiteLibrary" type="checkbox"><spring:message code="kalclip.add.to.site.library" />
              </div>
          </div>
      </c:if>
      <div id="buttons">
          <c:if test="${ showReplaceButton }">
              <span id="replace">
                  <span class="disable"></span>
                  <a id="replace_a" href="#" title="<spring:message code='kalclip.tooltip.replace.clip'/>"><spring:message code="kalclip.replace" /></a>
              </span>      
          </c:if>
          <span id="save">
              <span class="disable"></span>
              <a id="save_a" href="#" title="<spring:message code='kalclip.tooltip.save.clip'/>"><spring:message code="kalclip.save" /></a>
          </span>
      </div>
  </div>
  <input type="hidden" id="locationId" value="${ locationId }" />
  <input type="hidden" id="collectionId" value="${ collectionId }" />
  <input type="hidden" id="position" value="${ position }" />
  <script>
  clipApp.init( {
          "config": "${param.config}",
          "host": "${ config['host'] }",
          "partner_id": "${ config['partner_id'] }",
          "entry": ${ json_entry },
          "ks": "${ks}",
          "kdp_uiconf_id": ${ config['kdp_uiconf_id'] },
          "kclip_uiconf_id": ${ config['clipper_uiconf_id'] },
          "redirect_save": ${ config['redirect_save'] ? "true" : "false" },
          "redirect_url": "${ config['redirect_url'] }",
          "overwrite_entry": ${ config['overwrite_entry'] ? "true" : "false" }
  });
  </script>