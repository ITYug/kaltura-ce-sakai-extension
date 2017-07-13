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
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@ taglib
  prefix="spring" uri="http://www.springframework.org/tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link media="all" href="<c:url value="/css/kaltura.css"/>" rel="stylesheet" type="text/css" />
<script src="<c:url value="/javascript/fluid/InfusionAll.js"/>" language="JavaScript" type="text/javascript"></script>
<c:if test="${ not empty html5PlayerJS }">
<script src="${ html5PlayerJS }" language="JavaScript" type="text/javascript"></script>
</c:if>
<script src="<c:url value="/javascript/KalturaWidgets.js"/>" language="JavaScript" type="text/javascript"></script>

<style type="text/css">
html,body {
    padding: 0;
    margin: 0
}
</style>
</head>
<body class="portlet">
  <c:choose>
    <c:when test="${ allowed }">
      <form id="currentInfo" style="display: none;">
        <input id="userId" value="${currentUserId}" type="hidden" />
        <input id="locationId" value="${currentLocationId}" type="hidden" />
        <input id="itemId" value="${item.id}" type="hidden" />
      </form>

      <div class="media-viewer">
        <div class="content">
          <div class="media-item">
            <div id="kplayerId"></div>
          </div>
        </div>
      </div>
      <script type="text/javascript">
        $(document).ready(function() {
            kaltura.KCP(".media-item", 
                {
                    entryId: "${entryId}",
                    entryType: "${entryType}",
                    entryOwner: "${item.ownerId}",
                    playerId: "${item.playerId}",
                    playerURL: "${item.userPlayerURL}",
                    playerWidth: "${item.playerWidth}",
                    playerHeight: "${item.playerHeight}",
                    useHtml5Player: ${ not empty html5PlayerJS },
                    containerId: "kplayerId"
                }
            );
        });
    </script>
    </c:when>
    <c:otherwise>
      <div class="media-viewer">
        <div class="content">
          <span class="not_allowed"><spring:message code="viewMedia.forbidden" /></span>
        </div>
      </div>
    </c:otherwise>
  </c:choose>

  <!-- Hidden block to declare resource bundle variables for use in JS -->
  <div style="display: none;">
    <!-- By convention, the id of the span must be "i18n_"+{message code}, the container div should be set to display:none;, use kaltura.i18n(key) in JS to lookup the message -->
    <span id="i18n_error.missing.entry.id"><spring:message code="error.missing.entry.id" /></span> 
    <span id="i18n_error.missing.html5.library"><spring:message code="error.missing.html5.library" /></span>
  </div>

</body>
</html>