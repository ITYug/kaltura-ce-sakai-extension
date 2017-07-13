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

<div>
  <c:choose>
    <c:when test="${!(empty collectionId)}">
      <c:choose>
        <c:when test="${ isSiteLibraryVisible and 'myMedia' ne collectionId }">
          <a id="backToLibrary" href="viewCollection.htm">
            <spring:message code="uploadMedia.back.site.library" />
          </a>
          &nbsp;
          <a id="returnLink" href="viewCollection.htm?collectionId=${ collectionId }">
            <spring:message code="uploadMedia.back.collection" />
          </a>
        </c:when>
        <c:when test="${ 'myMedia' eq collectionId }">
          <a id="returnLink" href="viewCollection.htm?view=${ collectionId }">
            <spring:message code="uploadMedia.back.my.media" />
          </a>
        </c:when>
        <c:otherwise>
          <a id="returnLink" href="viewCollection.htm?collectionId=${collectionId}">
            <spring:message code="uploadMedia.back.collection" />
          </a>
        </c:otherwise>
      </c:choose>
      <!-- used by JS -->
      <span id="collectionId" style="display: none;"><c:out value="${collectionId}" /></span>
    </c:when>
    <c:otherwise>
      <c:if test="${isSiteLibraryVisible}">
        <a id="returnLink" href="viewCollection.htm">
          <spring:message code="uploadMedia.back.site.library" />
        </a>
      </c:if>
    </c:otherwise>
  </c:choose>
</div>

<div class="kaltura-uploader" style="min-height: 400px">
  <div id="kcwId"></div>
</div>

<form id="currentInfo" style="display: none;">
  <input id="userId" value="${currentUserId}" type="hidden" />
  <input id="locationId" value="${currentLocationId}" type="hidden" />
  <c:if test="${!(empty collectionId)}"><input id="collectionId" value="${collectionId}" type="hidden" /></c:if>
</form>

<script type="text/javascript">
kaltura.kcwToolId = "${ toolId }";
$(document).ready(function() {
    kaltura.KCW(".kaltura-uploader", 
        {
            containerId: "kcwId",
            uploadSpecialId: "${ uploadSpecialId }", // pass the special upload id if set (empty string otherwise)
            uploadSpecialKS: "${ uploadSpecialKS }", // pass special KS if set (empty string otherwise)
            onAddEntry: (${ 'myMedia' eq collectionId } ? "" : "addEntriesToSiteLibrary")
        }
    );
});

// should get called when finish button is pressed
var closeKalturaUploader = function() {
    var url = $("#returnLink").attr('href');
    if (!url) { // failsafe
        url = "viewCollection.htm";
    }
    document.location = url;
};
</script>

<jsp:directive.include file="/WEB-INF/jsp/footer.jsp" />