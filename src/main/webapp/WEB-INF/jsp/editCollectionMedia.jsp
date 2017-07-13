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

<!-- Portlet -->
<div class="portlet kaltura-gallery view-managemedia" role="section">
  <!-- Portlet Views -->
  <div class="portlet-views">
    <ul role="tablist" class="fl-tabs fl-tabs-left ui-helper-clearfix view-tabs">
      <c:if test="${isSiteLibraryVisible}">
        <li tabindex="-1" role="tab">
          <a role="link" key="library" href="viewCollection.htm" title="<spring:message code='app.tooltip.view.site.library'/>">
            <span><spring:message code="app.site.library" /></span>
          </a>
        </li>
      </c:if>
      <li tabindex="-1" role="tab" id="collectionsTab" class="fl-activeTab">
        <a role="link" key="collections" href="listCollections.htm" title="<spring:message code='app.tooltip.view.media.collection'/>">
          <span><spring:message code="app.collections" /></span>
        </a>
      </li>
      <c:if test="${showMyMediaTab}">
        <li tabindex="-1" role="tab">
          <a role="link" key="library" href="viewCollection.htm?view=myMedia" title="<spring:message code='app.my.media'/>">
            <span><spring:message code='app.my.media'/></span>
          </a>
        </li> 
      </c:if>
    </ul>
    <div class="fl-tab-content tab-content" role="tabpanel">
      <!-- Portlet Toolbar -->
      <div class="toolbar" role="toolbar">
        <ul>
          <li>
            <c:if test="${isKalturaWrite}">
              <a href="uploadMedia.htm?collectionId=${collection.id}" title="<spring:message code='app.tooltip.upload.media.to.collection'/>">
                <spring:message code="app.upload.media" />
              </a>
            </c:if>
            <a href="viewCollection.htm?collectionId=${collection.id}" title="<spring:message code='editCollectionMedia.tooltip.view.collection'/>">
              <spring:message code="editCollectionMedia.toolbar.view" />
            </a>
          </li>
        </ul>
      </div>

      <!-- Portlet Content -->
      <div class="content portlet-content" role="main">
        <!-- Portlet Title -->
        <div class="titlebar portlet-titlebar" role="sectionhead">
          <!--<div class="breadcrumb">
                <span class="breadcrumb-1"><a href="listCollections.htm">Collections</a></span><span class="separator"> &gt; </span>
                <span class="breadcrumb-2"><a href="viewCollection.htm?collectionId=${collection.id}">${collection.title}</a></span>
            </div>-->
          <h2 class="title" role="heading">
            <spring:message code="app.manage.media" />
          </h2>
          <a class="button" href="viewCollection.htm?collectionId=${collection.id}"
            title="<spring:message code='editCollectionMedia.tooltip.finished'/>">
            <spring:message code="editCollectionMedia.finished" />
          </a>
        </div>
        <!-- end: .portlet-title -->

        <kaltura:messages />

        <div class="description" role="note">
          <p>
            <spring:message code="editCollectionMedia.instructions" />
          </p>
        </div>
        <!-- Two Panel Viewer -->
        <div class="fl-col-flex2 media-manager">
          <div class="fl-col column first">
            <div class="inner">
              <div class="collection-media">
                <div class="titlebar">
                  <h3 class="title">
                    <spring:message code="app.collection" />
                    : ${collection.title}
                  </h3>
                  <div class="ajax-running" style="float:right; display: none;">Updating data in progress</div>
                  <div class="ajax-running-complete" style="float:right; display: none;">Update operation(s) complete</div>
                </div>
                <div id="contentsDiv" class="content" style="display: none;">
                  <div class="note empty no_items" role="note" style="display: none;">
                    <p>
                      <spring:message code="editCollectionMedia.note.empty.no.items" />
                    </p>
                  </div>
                  <!-- Media List -->
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
                      <ul id="pager-top" class="flc-pager-top">
                        <li>
                          <ul class="pager-links flc-pager-links">
                            <li class="flc-pager-pageLink">
                              <a href="#null" class="ajax_link">
                                <spring:message code="app.pager.numeric.one" />
                              </a>
                            </li>
                            <li class="flc-pager-pageLink-skip">...</li>
                            <li class="flc-pager-pageLink">
                              <a href="#null" class="ajax_link">
                                <spring:message code="app.pager.numeric.two" />
                              </a>
                            </li>
                          </ul>
                        </li>

                        <li class="flc-pager-previous">
                          <a href="#null" class="ajax_link">
                            &lt;
                            <spring:message code="app.pager.previous" />
                          </a>
                        </li>
                        <li class="flc-pager-next">
                          <a href="#null" class="ajax_link">
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
                      <ul class="media-list" id="collection_pager">
                        <div rsf:id="~item:" note="this will be removed when processed, do not change">
                        <li rsf:id="media-item" class="media-item media_item">
                          <div class="media_item_main">
                            <div class="thumbnail">
                              <a rsf:id="thumbnail-image-link" class="thumbnail-link action_remove ajax_link" href="#null"
                                title="<spring:message code='editCollectionMedia.tooltip.remove.item.from.collection' />">
                                <span class="remove"><span class="action-marker"><spring:message code="app.remove" /></span></span> <span
                                  class="image"><img rsf:id="thumbnail-image" src="http://item_thumbnail" alt="item_title" /></span>
                              </a>
                              <span class="hidden-marker"><spring:message code="app.private" /></span>
                              <div class="perms">
                                <ul>
                                  <li class="owned-marker" title="<spring:message code='app.tooltip.permission.own'/>">
                                    <span><spring:message code="app.item.permission.own" /></span>
                                  </li>
                                  <li class="control-marker" title="<spring:message code='app.tooltip.permission.control'/>">
                                    <span><spring:message code="app.item.permission.control" /></span>
                                  </li>
                                  <li class="shared-marker" title="<spring:message code='app.tooltip.permission.shared'/>">
                                    <span><spring:message code="app.item.permission.share" /></span>
                                  </li>
                                  <li class="mixable-marker" title="<spring:message code='app.tooltip.permission.remix'/>">
                                    <span><spring:message code="app.item.permission.remix" /></span>
                                  </li>
                                </ul>
                              </div>
                            </div>
                            <div class="thumbnail-title">
                              <a headers="nameSort" rsf:id="thumbnail-title-link" href="#null" class="ajax_link">item_name</a>
                            </div>
                          </div>
                        </li>
                        </div>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <!-- end: .inner -->
          </div>
          <!-- end: .column -->
          <div class="fl-col column last">
            <div class="inner">
              <div class="site-library">
                <div class="titlebar">
                  <h3 class="title">
                    <span class="media-source-link">
                      <span class="mymedia-active">
                        <spring:message code="editCollectionMedia.my.media" />
                      </span>
                      <a href="#null" title="<spring:message code='app.tooltip.view.my.media' />" class="sitelibrary-active hide ajax_link"><spring:message code="editCollectionMedia.my.media" /></a>
                      <c:if test="${isSiteLibraryVisible}"> 
                        |
                        <span class="sitelibrary-active hide">
                          <spring:message code="editCollectionMedia.site.library" />
                        </span>                      
                        <a href="#null" title="<spring:message code='app.tooltip.view.site.library' />" class="mymedia-active ajax_link"><spring:message code="editCollectionMedia.site.library" /></a>
                      </c:if>
                    </span>
                  </h3>
                  <span class="details">
                    <span class="library-count"></span>
                      <span class="mymedia-active">
                        <spring:message code="editCollectionMedia.my.media.owner" />
                      </span>
                      <span class="sitelibrary-active hide">
                        <spring:message code="editCollectionMedia.library.owner" />
                      </span>
                      ${currentUserDisplay}
                    </span>
                </div>
                <div class="media-browser content scrollable">
                  <div class="note empty no_items" role="note" style="display: none">
                    <p>
                      <spring:message code="app.library.empty" />
                      <br />
                      <c:if test="${isKalturaWrite}">
                        <c:if test="${isSiteLibraryVisible}">
                          <a href="uploadMedia.htm?collectionId=${siteId}" title="<spring:message code='app.tooltip.upload.new.media'/>">
                            <spring:message code="editCollectionMedia.upload.media.to.library" />
                          </a>
                        </c:if>
                        &nbsp;&nbsp;
                        <a href="uploadMedia.htm?collectionId=${collection.id}"
                          title="<spring:message code='app.tooltip.upload.media.to.collection'/>">
                          <spring:message code="app.upload.media.to.collection" />
                        </a>
                      </c:if>
                    </p>
                  </div>
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
                    <ul id="pager-top" class="flc-pager-top">
                      <li>
                        <ul class="pager-links flc-pager-links">
                          <li class="flc-pager-pageLink">
                            <a href="#null" class="ajax_link">
                              <spring:message code="app.pager.numeric.one" />
                            </a>
                          </li>
                          <li class="flc-pager-pageLink-skip">...</li>
                          <li class="flc-pager-pageLink">
                            <a href="#null" class="ajax_link">
                              <spring:message code="app.pager.numeric.two" />
                            </a>
                          </li>
                        </ul>
                      </li>
                      <li class="flc-pager-previous">
                        <a href="#null" class="ajax_link">
                          &lt;
                          <spring:message code="app.pager.previous" />
                        </a>
                      </li>
                      <li class="flc-pager-next">
                        <a href="#null" class="ajax_link">
                          <spring:message code="app.pager.next" />
                          &gt;
                        </a>
                      </li>
                      <li>
                        <spring:message code="app.pager.show" />
                        <span><select class="flc-pager-page-size">
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
                    <!-- Kaltura List -->
                    <div class="media-list kaltura_collection items">
                      <div rsf:id="~item:">
                        <div rsf:id="media-item" class="media-item kaltura_item">
                          <div class="kaltura_item_main">
                            <div class="thumbnail">
                              <a rsf:id="thumbnail-image-link" class="thumbnail-link action_add ajax_link" href="#null">
                                <span class="add"><span class="action-marker"><spring:message code="editCollectionMedia.library.add" /></span></span>
                                <span class="image"><img rsf:id="thumbnail-image" class="thumbnail-image" /></span>
                              </a>
                              <span class="hidden-marker"><spring:message code="app.private" /></span>
                              <div class="perms">
                                <ul>
                                  <li class="owned-marker" title="<spring:message code='app.tooltip.permission.own'/>">
                                    <span><spring:message code="app.item.permission.own" /></span>
                                  </li>
                                  <li class="control-marker" title="<spring:message code='app.tooltip.permission.control'/>">
                                    <span><spring:message code="app.item.permission.control" /></span>
                                  </li>
                                  <li class="shared-marker" title="<spring:message code='app.tooltip.permission.shared'/>">
                                    <span><spring:message code="app.item.permission.share" /></span>
                                  </li>
                                  <li class="mixable-marker" title="<spring:message code='app.tooltip.permission.remix'/>">
                                    <span><spring:message code="app.item.permission.remix" /></span>
                                  </li>
                                </ul>
                              </div>
                            </div>
                            <div headers="nameSort" rsf:id="thumbnail-title-link" class="thumbnail-title">
                              <a href="#null" class="thumbnail-title-link ajax_link"></a>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <!-- end: .inner -->
          </div>
          <!-- end: .column -->
        </div>
        <!-- end: .two-column -->
      </div>
      <!-- end: .portlet-content -->
    </div>
    <!-- end: .tab-content -->
  </div>
  <!-- end: .portlet-views -->
</div>
<!-- end: .portlet -->

<script type="text/javascript">
  kaltura.initEditCollectionMedia("${collection.id}", "${toolId}");
  $("#contentsDiv").show();
</script>

<jsp:directive.include file="/WEB-INF/jsp/footer.jsp" />
