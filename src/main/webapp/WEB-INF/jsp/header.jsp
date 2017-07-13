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
<?xml version="1.0" encoding="UTF-8" ?>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" 
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" 
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" 
%><%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" 
%><%@ taglib prefix="spring" uri="http://www.springframework.org/tags" 
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<%
    String toolBaseCSS = org.sakaiproject.kaltura.tool.CSSUtils.getCssToolBase();
    String toolCSS = org.sakaiproject.kaltura.tool.CSSUtils.getCssToolSkin((String)null);
%>
<link media="all" href="<%=toolBaseCSS%>" rel="stylesheet" type="text/css" />
<link media="all" href="<%=toolCSS%>" rel="stylesheet" type="text/css" />
<link media="all" href="<c:url value='/css/smoothness/jquery-ui-1.7.2.custom.css'/>" rel="stylesheet" type="text/css" />
<link media="all" href="<c:url value='/css/fss-layout.css'/>" rel="stylesheet" type="text/css" />
<link media="all" href="<c:url value='/css/portlet.css'/>" rel="stylesheet" type="text/css" />
<link media="all" href="<c:url value='/css/kaltura.css'/>" rel="stylesheet" type="text/css" />
<link media="all" href="<c:url value='/css/kalclip.css'/>" rel="stylesheet" type="text/css" />

<script src="/library/js/headscripts.js" language="JavaScript" type="text/javascript"></script>
<script src="<c:url value="/javascript/fluid/InfusionAll.min.js"/>" language="JavaScript" type="text/javascript"></script>
<script src="<c:url value="/javascript/jquery/jquery.ui.tabs.min.js"/>" language="JavaScript" type="text/javascript"></script>
<script src="<c:url value="/javascript/jquery/jquery.loadmask.js"/>" language="JavaScript" type="text/javascript"></script>
<!-- <script src="<c:url value="/javascript/jquery/jquery.ajaxQueue.js"/>" language="JavaScript" type="text/javascript"></script> requires jquery 1.6-->
<c:if test="${ not empty html5PlayerJS }">
  <script src="${ html5PlayerJS }" language="JavaScript" type="text/javascript"></script>
</c:if>
<script src="<c:url value="/javascript/KalturaMediaSelector.js"/>" language="JavaScript" type="text/javascript"></script>
<script src="<c:url value="/javascript/KalturaWidgets.js"/>" language="JavaScript" type="text/javascript"></script>

<title><spring:message code="app.title" /></title>
</head>
<body onload="<%=request.getAttribute("sakai.html.body.onload")%>">
  <!-- Hidden block to declare resource bundle variables for use in JS -->
  <div style="display: none;">
    <!-- By convention, the id of the span must be "i18n_"+{message code}, the container div should be set to display:none;, use kaltura.i18n(key) in JS to lookup the message -->
    <span id="i18n_error.missing.entry.id"><spring:message code="error.missing.entry.id" /></span> <span id="i18n_error.missing.html5.library"><spring:message
        code="error.missing.html5.library" /></span> <span id="i18n_error.delete.confirmation"><spring:message code="delete.confirmation" /></span> <span
      id="i18n_error.delete.item.confirmation"><spring:message code="delete.item.confirmation" /></span> <span
      id="i18n_error.unable.to.insert.in.editor"><spring:message code="error.unable.to.insert.in.editor" /></span> <span
      id="i18n_error.unable.to.mix"><spring:message code="error.unable.to.mix" /></span>
  </div>
  <div class="portletBody">
    <c:if test="${ canAdministrateKalturaPermissions }">
      <ul class="navIntraTool actionToolBar">
        <li class="firstToolBarItem">
          <span class>
            <a href="setPermissions.htm" title="<spring:message code='app.permissions.tooltip'/>">
              <spring:message code="app.permissions" />
            </a>
          </span>
        </li>
      </ul>
    </c:if>