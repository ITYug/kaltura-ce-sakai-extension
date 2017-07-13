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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link media="all" href="/library/skin/tool_base.css" rel="stylesheet" type="text/css" />
<link media="all" href="/library/skin/default/tool.css" rel="stylesheet" type="text/css" />
<link media="all" href="<c:url value="/css/smoothness/jquery-ui-1.7.2.custom.css"/>" rel="stylesheet" type="text/css" />
<link media="all" href="<c:url value="/css/fss-layout.css"/>" rel="stylesheet" type="text/css" />
<link media="all" href="<c:url value="/css/portlet.css"/>" rel="stylesheet" type="text/css" />
<link media="all" href="<c:url value="/css/kaltura.css"/>" rel="stylesheet" type="text/css" />

<script src="<c:url value="/javascript/fluid/InfusionAll.js"/>" language="JavaScript" type="text/javascript"></script>
<script src="<c:url value="/javascript/jquery/jquery.loadmask.js"/>" language="JavaScript" type="text/javascript"></script>
<script src="<c:url value="/javascript/KalturaMediaSelector.js"/>" language="JavaScript" type="text/javascript"></script>
<script src="<c:url value="/javascript/KalturaWidgets.js"/>" language="JavaScript" type="text/javascript"></script>

</head>
<body class="portlet">
  <div class="browse-media-window">
    <div class="media-browser">

        <!--  Search bar -->
        <div class="search">
          <form class="search-form">
            <input type="text" name="searchText" />
            <select name="mediaType">
              <option value="">
                <spring:message code="app.media.type.all" />
              </option>
              <option value="video">
                <spring:message code="app.media.type.video" />
              </option>
              <option value="image">
                <spring:message code="app.media.type.image" />
              </option>
              <option value="audio">
                <spring:message code="app.media.type.audio" />
              </option>
            </select>
            <input type="submit" value="Search" />
          </form>
        </div>
        <div class="pager">
          <!-- upload control -->
          <div class="upload-media-link-wrapper" style="float: left; display: none;">
            <a class="upload-media-link" href="#null">
              <spring:message code="fckEditorSelector.upload" />
            </a>
          </div>
          <div class="refresh-media-link-wrapper" style="float: left;">
            <a class="refresh-media-link" href="#null">
              <spring:message code="fckEditorSelector.refresh.media" />
            </a>
            <img class="refresh-media-spinner" src="<c:url value="/images/ajax-loader.gif"/>" alt="loading..." style="display: none;"/>
          </div>
          <ul id="pager-top" class="flc-pager-top">
            <li>
              <ul class="pager-links flc-pager-links">
                <li class="flc-pager-pageLink">
                  <a href="#null">
                    <spring:message code="app.pager.numeric.one" />
                  </a>
                </li>
                <li class="flc-pager-pageLink-skip">...</li>
                <li class="flc-pager-pageLink">
                  <a href="#null">
                    <spring:message code="app.pager.numeric.two" />
                  </a>
                </li>
              </ul>
            </li>
            <li class="flc-pager-previous">
              <a href="#null">
                &lt;
                <spring:message code="app.pager.previous" />
              </a>
            </li>
            <li class="flc-pager-next">
              <a href="#null">
                <spring:message code="app.pager.next" />
                &gt;
              </a>
            </li>
            <li>
              <spring:message code="app.pager.show" />
              <span> <select class="flc-pager-page-size">
                  <option value="6">
                    <spring:message code="app.pager.numeric.six" />
                  </option>
                  <option value="12">
                    <spring:message code="app.pager.numeric.twelve" />
                  </option>
                  <option value="24">
                    <spring:message code="app.pager.numeric.twentyfour" />
                  </option>
                  <option value="48">
                    <spring:message code="app.pager.numeric.fourtyeight" />
                  </option>
                </select></span>&nbsp;
              <spring:message code="app.pager.per.page" />
            </li>
          </ul>
        </div>

        <div class="pager-body" xmlns:rsf="http://ponder.org.uk/rsf">
          <div rsf:id="~item:" class="media-item-wrapper">
            <ul class="media-list kaltura_collection" xmlns:rsf="http://ponder.org.uk/rsf">
              <li rsf:id="media-item" class="media-item kaltura_item">
                <div class="kaltura_item_main">
                  <div class="thumbnail">
                    <a rsf:id="thumbnail-image-link" class="thumbnail-link action_add" href="javascript:;">
                      <span><img rsf:id="thumbnail-image" class="thumbnail-image" /></span>
                    </a>
                  </div>
                  <div class="thumbnail-title">
                    <a headers="nameSort" href="javascript:;" rsf:id="thumbnail-title-link" class="thumbnail-title-link"></a>
                  </div>
                </div>
              </li>
            </ul>
          </div>
        </div>

    </div>

  </div>

  <div class="upload-media-window" style="display: none">
    <div id="kaltura-uploader"></div>
    <div style="padding-top: 4px;">
      <a class="close-upload-link" href="javascript:;">
        <spring:message code="fckEditorSelector.upload.close" />
      </a>
    </div>
  </div>

  <div class="nav-warning-message alertMessage">
    <spring:message code="fckEditorSelector.navigation.warning" />
  </div>

  <!-- Hidden block to declare resource bundle variables for use in JS -->
  <div style="display: none;">
    <!-- By convention, the id of the span must be "i18n_"+{message code}, the container div should be set to display:none;, use kaltura.i18n(key) in JS to lookup the message -->
    <span id="i18n_error.missing.entry.id"><spring:message code="error.missing.entry.id" /></span> 
    <span id="i18n_error.missing.html5.library"><spring:message code="error.missing.html5.library" /></span> 
    <span id="i18n_error.delete.confirmation"><spring:message code="delete.confirmation" /></span> 
    <span id="i18n_error.delete.item.confirmation"><spring:message code="delete.item.confirmation" /></span> 
    <span id="i18n_error.unable.to.insert.in.editor"><spring:message code="error.unable.to.insert.in.editor" /></span> 
    <span id="i18n_error.unable.to.mix"><spring:message code="error.unable.to.mix" /></span>
  </div>

  <form id="currentInfo" style="display: none;">
    <input id="userId" value="${currentUserId}" type="hidden" />
    <!-- insert site or tool here when known as currentLocationRef -->
  </form>

  <script type="text/javascript">
    kaltura.initFckEditorSelector("${ isSuperUser }", "${ uploadSpecialId }", "${ uploadSpecialKS }");
    // has to be global - triggered by the kaltura Finish button
    var closeKalturaUploader = function() {
      kaltura.closeEditorKalturaUploader();
    };
  </script>

</body>
</html>