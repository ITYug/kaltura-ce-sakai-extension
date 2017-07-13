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
<div class="portlet kaltura-gallery view-collection view-type-${mediaContainerType}" role="section">
  <!-- Portlet Views -->
  <div class="portlet-views">
    <ul role="tablist" class="fl-tabs fl-tabs-left ui-helper-clearfix view-tabs">
      <c:choose>
        <c:when test="${ isLibrary }">
          <c:if test="${ isSiteLibraryVisible }">
            <li tabindex="0" role="tab" id="siteLibraryTab">
              <a role="link" key="library" href="viewCollection.htm" title="<spring:message code='app.tooltip.view.site.library'/>">
                <span><spring:message code="app.site.library" /></span>
              </a>
            </li>
          </c:if>
          <li tabindex="-1" role="tab" id="collectionsTab">
            <a role="link" key="collections" href="listCollections.htm" title="<spring:message code='app.tooltip.view.media.collection'/>">
              <span><spring:message code="app.collections" /></span>
            </a>
          </li>
          <c:if test="${ showMyMediaTab }">
            <li tabindex="-1" role="tab" id="myMediaTab">
              <a role="link" key="library" href="viewCollection.htm?view=myMedia" title="<spring:message code='app.my.media'/>"><span><spring:message code='app.my.media'/></span></a>
            </li> 
          </c:if>
        </c:when>
        <c:otherwise>
          <c:if test="${ isSiteLibraryVisible }">
            <li tabindex="-1" role="tab" id="siteLibraryTab">
              <a role="link" key="library" href="viewCollection.htm" title="<spring:message code='app.tooltip.view.site.library'/>">
                <span><spring:message code="app.site.library" /></span>
              </a>
            </li>
          </c:if>
          <li tabindex="-1" role="tab" id="collectionsTab">
            <a role="link" key="collections" href="listCollections.htm" title="<spring:message code='app.tooltip.view.media.collection'/>">
              <span><spring:message code="app.collections" /></span>
            </a>
          </li>
          <c:if test="${ showMyMediaTab }">
            <li tabindex="-1" role="tab" id="myMediaTab">
              <a role="link" key="library" href="viewCollection.htm?view=myMedia" title="<spring:message code='app.my.media'/>"><span><spring:message code='app.my.media'/></span></a>
            </li> 
          </c:if>
        </c:otherwise>
      </c:choose>
    </ul>
    <div class="fl-tab-content tab-content" role="tabpanel">
      <!-- Portlet Toolbar -->
      <div class="toolbar" role="toolbar">
        <ul>
          <c:choose>
            <c:when test="${ isCollection }">
              <c:if test="${ collection.control or collection.addItems }">
                <li>
                  <a class="add" href="editCollectionMedia.htm?collectionId=${currentCollectionId}"
                    title="<spring:message code='app.tooltip.add.media'/>">
                    <spring:message code="app.manage.media" />
                  </a>
                </li>
                <c:if test="${ isKalturaWrite }">
                  <li>
                    <a href="uploadMedia.htm?collectionId=${collection.idStr}" title="<spring:message code='app.tooltip.upload.media.to.collection'/>">
                      <spring:message code="app.upload.media" />
                    </a>
                  </li>
                </c:if>
              </c:if>
              <c:if test="${ collection.control }">
                <li>
                  <a class="edit" href="editCollectionDetails.htm?collectionId=${currentCollectionId}"
                    title="<spring:message code='viewCollection.tooltip.edit.collection'/>">
                    <spring:message code="app.edit.collection" />
                  </a>
                </li>
                <li>
                  <a class="delete delete_coll_confirm" href="deleteCollection.htm?collectionId=${currentCollectionId}"
                    title="<spring:message code='viewCollection.tooltip.delete.collection'/>">
                    <spring:message code="viewCollection.delete.collection" />
                  </a>
                </li>
              </c:if>
            </c:when>
            <c:otherwise><!-- library or myMedia -->
              <c:if test="${ isKalturaWrite }">
                <li>
                  <c:choose>
                    <c:when test="${ 'myMedia' eq currentCollectionId }"><!-- myMedia -->
                      <a class="upload" href="uploadMedia.htm?collectionId=${currentCollectionId}"
                        title="<spring:message code='app.tooltip.upload.new.media.my.media'/>">
                        <spring:message code="app.upload.media" />
                      </a>
                    </c:when>
                    <c:otherwise><!-- library -->
                      <a class="upload" href="uploadMedia.htm?collectionId=${currentCollectionId}"
                        title="<spring:message code='viewCollection.tooltip.upload.media.to.library'/>">
                        <spring:message code="app.upload.media" />
                      </a>
                    </c:otherwise>
                  </c:choose>
                </li>
              </c:if>
            </c:otherwise>
          </c:choose>
        </ul>
      </div>
      <!-- end: portlet-toolbar -->

      <!-- Portlet Content -->
      <div class="content portlet-content" role="main">
        <div id="addSuccess" style="display : none;">
           <h4><spring:message code="viewCollection.add.to.collection.success" /></h4>
           <br/>
        </div>
        <!-- Portlet Title -->
        <div class="titlebar portlet-titlebar" role="sectionhead">
          <c:choose>
            <c:when test="${ isLibrary and not isMyMedia }">
              <h2 class="title" role="heading" title="<spring:message code='app.site.library'/>">
                <spring:message code="app.site.library" />
              </h2>
            </c:when>
            <c:when test="${ isMyMedia }">
               <h2 class="title" role="heading" title="<spring:message code='app.my.media'/>"><spring:message code='app.my.media'/></h2>
            </c:when>
            <c:otherwise>
              <h2 class="title" role="heading" title="${collection.title}">
                <spring:message code="app.collection" />
                : ${collection.title}
                <!-- Owner: ${collection.ownerName}, Control: ${collection.control}, Admin: ${isKalturaAdmin} -->
              </h2>
            </c:otherwise>
          </c:choose>
        </div>
        <!-- end: .portlet-title -->

        <kaltura:messages />
        <div class="description">
          <c:choose>
            <c:when test="${ isLibrary and not isMyMedia }">
              <p title="<spring:message code='app.site.library'/>">
                <spring:message code="viewCollection.site.library.desc.1" />
                <c:if test="${ isKalturaWrite }">
                  <spring:message code="viewCollection.site.library.desc.2" />
                </c:if>
              </p>
            </c:when>
            <c:when test="${ isMyMedia }">
               <p title="<spring:message code='app.my.media'/>"><spring:message code="viewCollection.my.media.desc"/></p>
            </c:when>
            <c:otherwise>
              <p title="${collection.description}">${collection.shortDescription}</p>
            </c:otherwise>
          </c:choose>
        </div>

        <div id="collection_items_loading" class="loading"></div>

        <!-- render when there are no items to show -->
        <div id="no_items_in_collection" class="portlet-msg info empty" role="note" style="display:none;">
          <c:choose>
            <c:when test="${ isLibrary and not isMyMedia }">
              <span><spring:message code="app.library.empty" /></span>
              <c:if test="${ isKalturaWrite }">
                <a class="upload" href="uploadMedia.htm?collectionId=${currentCollectionId}"
                  title="<spring:message code='viewCollection.tooltip.upload.media.to.library'/>">
                  <spring:message code="app.upload.new.media" />
                </a>.
              </c:if>
            </c:when>
            <c:when test="${ isMyMedia }">
              <span><spring:message code="viewCollection.my.media.empty"/></span>
              <c:if test="${ isKalturaWrite and isSiteLibraryVisible }">
                <a class="upload" href="uploadMedia.htm" title="<spring:message code='viewCollection.tooltip.upload.media.to.library'/>">
                  <spring:message code="app.upload.new.media"/>
                </a>.
              </c:if>
            </c:when>
            <c:otherwise>
              <span><spring:message code="app.collection.empty" /></span>
              <c:if test="${ collection.control or collection.addItems }">
                <a href="editCollectionMedia.htm?collectionId=${currentCollectionId}" title="<spring:message code='app.tooltip.add.media'/>"
                  class="add">
                  <spring:message code="app.collection.add.media" />
                </a>&nbsp;<spring:message code="viewCollection.to.collection.from.site.library" />
                <c:if test="${ isKalturaWrite }">
                                  &nbsp;<spring:message code='app.or' />
                  <a href="uploadMedia.htm?collectionId=${currentCollectionId}"
                    title="<spring:message code='app.tooltip.upload.media.to.collection'/>">
                    <spring:message code="app.upload.media.to.collection" />
                  </a>
                </c:if>
              </c:if>
            </c:otherwise>
          </c:choose>
        </div>
        <div id="items_in_collection" style="display:none;">
            <!-- render where there are items to show -->
            <form id="currentInfo" style="display: none;">
              <input id="userId" value="${currentUserId}" type="hidden" />
              <input id="locationId" value="${currentLocationId}" type="hidden" />
              <input id="collectionId" value="${currentCollectionId}" type="hidden" />
              <input id="itemId" value="${currentItemId}" type="hidden" />
              <c:if test="${ not empty siteCategoryId }"><input id="siteCategoryId" value="${siteCategoryId}" type="hidden" /></c:if>
            </form>

            <!-- Two Panel Viewer -->
            <div class="fl-col-mixed-522 columns-2">
              <div class="fl-col-fixed fl-force-left column first">
                <div class="inner">

                  <!-- Media Viewer -->
                  <div class="media-viewer-loading loading"></div>
                  <div class="media-viewer">
                    <div class="titlebar">
                      <h3 class="title current-item-title">item_name_here</h3>
                    </div>
                    <div class="content">
                      <div class="media-item"><!--  TODO type-${ currentItem.mediaType }" -->
                        <div class="media-player kaltura-player">
                          <div id="kplayerId"></div>
                        </div>
                        <div class="actions">
                          <ul>
                            <li style="display: none;">
                              <a class="download" href="http://item_download_URL_here"
                                title="<spring:message code='viewCollection.tooltip.download.media'/>">
                                <spring:message code="viewCollection.download" />
                              </a>
                            </li>
                            <li style="display: none;">
                              <a class="embed" href="#null" id="kplayerEmbedButton"
                                title="<spring:message code='viewCollection.tooltip.view.player.embed.code'/>">
                                <spring:message code="viewCollection.embed" />
                              </a>
                            </li>
                            <li style="display: none;">
                              <a class="edit-item-details" href="#null"
                                title="<spring:message code='viewCollection.tooltip.edit.media.details'/>">
                                <spring:message code="viewCollection.edit.details" />
                              </a>
                            </li>
                            <!-- Whether or not to display this item depends on the item's settings and user's perms.
                                 See: KalturaMediaSelector.js function initializeFocusedView -->     
                            <li style="display: none;">
                              <a class="edit-video" href="kalClip.htm?&collectionId=XXX&entryId=YYY" 
                                 title="<spring:message code='viewCollection.tooltip.edit.media.content'/>">
                                <spring:message code="viewCollection.edit.media" />
                              </a>
                            </li>
                            <li style="display: none;">
                              <a class="item-remove-link" href="#null"
                                  title="<spring:message code='viewCollection.tooltip.remove.media.from.collection'/>">
                                <spring:message code="viewCollection.remove" />
                              </a>
                            </li>
                            <li style="display: none;">
                              <a class="add-media" href="#null" title="<spring:message code='viewCollection.tooltip.add.to.collection'/>">
                                <spring:message code="viewCollection.add" /> 
                              </a>
                            </li>
                          </ul>
                        </div>
                        <div id="kplayerEmbedArea" style="display: none; padding: 1em;">
                          <spring:message code="viewCollection.embed.instructions" />
                          <textarea id="kplayerEmbed" cols="54" rows="16"></textarea>
                        </div>
                        <div class="meta">
                          <div class="tags">
                            <div class="taglist">
                              <h3 class="label">
                                <spring:message code="viewCollection.tags" />
                                :
                              </h3>
                              <span class="item-tags"></span>
                            </div>
                          </div>
                          <div class="desc">
                            <h3 class="label">
                              <spring:message code="viewCollection.description" />
                              :
                            </h3>
                            <p class="item-desc"></p>
                          </div>
                          <div class="author">
                            <h3 class="label">
                              <spring:message code="viewCollection.uploaded.by" />
                              :
                            </h3>
                            <p>
                              <strong class="author-name"></strong>
                            </p>
                          </div>
                          <div class="date">
                            <h3 class="label">
                              <spring:message code="viewCollection.creation.date" />
                              :
                            </h3>
                            <p class="creation-date"></p>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                </div>
                <!-- end: .inner -->
              </div>
              <!-- end: .column -->
              <div class="fl-col-flex column last">
                <div class="inner">
                  <div class="collection-media">
                    <div class="titlebar">
                      <c:choose>
                        <c:when test="${ isLibrary and not isMyMedia }">
                          <h3 class="title" title="<spring:message code='viewCollection.site.media'/>">
                            <spring:message code="viewCollection.site.media" />
                          </h3>
                        </c:when>
                        <c:when test="${ isMyMedia }">
                          <h3 class="title" title="<spring:message code='app.my.media'/>">
                            <spring:message code="app.my.media"/>
                          </h3>
                        </c:when>
                        <c:otherwise>
                          <h3 class="title" title="<spring:message code='viewCollection.collection.media'/>">
                            <spring:message code="viewCollection.collection.media" />
                          </h3>
                        </c:otherwise>
                      </c:choose>
                      <div class="ajax-running" style="float:right; display: none;">Updating data in progress</div>
                      <div class="ajax-running-complete" style="float:right; display: none;">Update operation(s) complete</div>
                    </div>
                    <!--  Search bar -->
                    <div class="search">
                      <form class="search-form">
                        <input type="text" name="searchText" id="searchForm_searchText" />
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
                        <li>
                          <span class="pager_items_count"><span id="pager_itemsCount"><spring:message
                                code="viewCollection.pager.numeric.zero" /></span>&nbsp;<spring:message code="viewCollection.items" /></span>
                        </li>
                      </ul>
                    </div>
                    <div class="pager-body" xmlns:rsf="http://ponder.org.uk/rsf">
                      <div style="display: none;" class="no_items">No items to show</div>
                      <!-- Media List -->
                      <div rsf:id="~item:" class="media-item-wrapper">
                        <ul class="media-list" id="collection_pager">
                          <li class="media-item" rsf:id="media-item">
                            <div class="thumbnail">
                              <a rsf:id="thumbnail-image-link" class="thumbnail-link"
                                title="<spring:message code='viewCollection.tooltip.view.media.item'/>">
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
                            <div class="thumbnail-title">
                              <a headers="nameSort" rsf:id="thumbnail-title-link" class="thumbnail-title-link" href="#null"
                                title="<spring:message code='viewCollection.tooltip.view.media.item'/>"></a>
                            </div>
                            <div class="actions hide">
                              <ul>
                                <li>
                                  <a rsf:id="item-details-link" class="edit-item-details" href="#null"
                                    title="<spring:message code='viewCollection.tooltip.edit.media.details'/>">
                                    <spring:message code="viewCollection.details" />
                                  </a>
                                </li>
                                <c:if test="${ isMyMedia and isKalturaWrite }">
                                  <li>
                                    <a rsf:id="item-add-link" class="item-add-link" href="#null" title="<spring:message code='viewCollection.tooltip.add.to.collection'/>">
                                      <spring:message code="viewCollection.add" />
                                    </a>
                                  </li>
                                </c:if>
                                <c:if test="${ not isMyMedia }">
                                  <li>
                                    <a rsf:id="item-remove-link" class="item-remove-link" href="#null"
                                      title="<spring:message code='viewCollection.tooltip.remove.media.from.collection'/>">
                                      <spring:message code="viewCollection.remove" />
                                    </a>
                                  </li>
                                </c:if>
                              </ul>
                            </div>
                            <div class="details hide">
                              <div class="inner">
                                <h4 rsf:id="itemTitle" class="item-details-title"></h4>
                                <span rsf:id="item-details-date" class="item-details-creation-date"></span> <span class="kaltura-info"></span>
                                <p></p>
                              </div>
                            </div>
                          </li>
                        </ul>
                      </div>
                    </div>
                  </div>
                </div>
                <!-- end: .inner -->
              </div>
              <!-- end: .column -->
            </div>
            <!-- end: .two-column -->
        </div><!-- show items -->


        <!-- Edit media details -->
        <div id="editDetails" class="dialog portlet" title="<spring:message code='viewCollection.tooltip.edit.media.details'/>" style="display:none;">
          <div class="content">
            <div class="">
              <form name="itemDetails" id="itemDetails">
                <input type="hidden" class="kaltura-item-id" value="" />
                <input type="hidden" class="kaltura-container-type" value="${mediaContainerType}" />
                <input type="hidden" class="kaltura-container-id" value="${mediaContainerId}" />
                <input type="hidden" name="currentItem" value="" />
                <input type="hidden" name="validated" value="" />
                <p style="display: none;" class="error invalid_form">
                  <spring:message code="viewCollection.error.please.correct" />
                </p>
                <div class="kaltura-item-edit">
                  <label for="title"><spring:message code="viewCollection.title" />:</label>
                  <input name="title" type="text" class="kaltura-item-title-edit" />
                  <p style="display: none;" class="error invalid_title">
                    <spring:message code="viewCollection.error.title" />
                  </p>
                  <label for="desc"><spring:message code="viewCollection.description" />:</label>
                  <input name="desc" type="text" class="kaltura-item-description-edit" />
                  <label for="tags"><spring:message code="viewCollection.tags" />:</label>
                  <input name="tags" type="text" class="kaltura-item-tags-edit" />
                  <p class="error invalid-tag" style="display: none;">
                    <spring:message code="viewCollection.error.tag" />
                    <span class="invalid_tag_list"></span>
                    <br />
                    <spring:message code="viewCollection.error.tag.desc" />
                  </p>
                </div>
                <c:if test="${ isMyMedia }">
                  <fieldset class="kaltura-managed">
                    <input type="hidden" name="public" value=""/>
                    <input type="hidden" name="shared" value=""/>
                    <input type="hidden" name="remixable" value=""/>
                  </fieldset>
                </c:if>
                <c:if test="${ not isMyMedia }">
                  <fieldset class="kaltura-managed">
                    <div class="row">
                      <label for="public"><spring:message code="viewCollection.public"/>:</label>
                      <input name="public" type="checkbox" class="kaltura-item-public-edit"/>
                    <p class="instruction">
                      <spring:message code="viewCollection.public.description" />
                    </p>
                    </div>
                    <div class="row">
                      <label for="shared"><spring:message code="viewCollection.reusable"/>:</label>
                      <input name="shared" type="checkbox" class="kaltura-item-shared-edit"/>
                    <p class="instruction">
                      <spring:message code="viewCollection.reusable.description" />
                    </p>
                    </div>
                    <div class="row kaltura-remix-control">
                      <label for="remix"><spring:message code="app.item.permission.remix" />:</label>
                      <input name="remixable" type="checkbox" class="kaltura-item-remixable-edit" />
                      <p class="instruction">
                        <spring:message code="viewCollection.remixable.description" />
                      </p>
                    </div>
                  </fieldset>
                </c:if>
              </form>
            </div>
          </div>
        </div>


        <!-- Add media from My Media to site library or a collection -->
        <div id="addMedia" class="dialog portlet" title="<spring:message code="viewCollection.my.media.collection.add.title" />" style="display:none;">
          <div class="content">
            <div class="" style="width: 474px;">
              <div id="addToCollectionDiv" style="float: left; width: 192px">
                <form name="addToCollectionDetails" id="addToCollectionDetails">
                  <input type="hidden" id="kalturaId" value="" />
                  <input type="hidden" id="mediaLocationId" value="${ currentLocationId }" />
                  <select id="targetCollections" style="width:190px;">
                  </select>
                  <div id="noAvailableCollectionsMessageDiv" style="font-size:0.8em; display: none;">
                    <spring:message code="viewCollection.my.media.unavailable.collection.description" />
                  </div>
                  <!-- instructions -->
                  <div id="availableCollectionsMessageDiv" style="font-size: 0.9em; padding-top: 1em;">
                    <spring:message code="viewCollection.my.media.available.collection.instruction" />
                  </div>
                </form>
              </div>
              <div style="float:right; width: 240px;">
                <div>
                  <spring:message code="viewCollection.my.media.unavailable.collection" />
                </div>
                <ul id="cannotAddToCollectionList" style="margin: 0.1em; padding-left: 1.5em; font-size:0.9em;"></ul>
                <!-- instructions -->
                <div style="font-size: 0.8em; padding-top: 1em;">
                  <spring:message code="viewCollection.my.media.unavailable.collection.instruction" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- end: .portlet-content -->
    </div>
    <!-- end: .tab-content -->
  </div>
  <!-- end: .portlet-views -->
</div>
<!-- end: .portlet -->

<c:choose>
    <%-- DO NOT change the displayTab values here as they are used in JS and CSS --%>
    <c:when test="${isMyMedia}"><span id="displayTab" style="display: none;">myMediaTab</span></c:when>
    <c:when test='${isViewCollection}'><span id="displayTab" style="display: none;">collectionsTab</span></c:when>
    <c:otherwise><span id="displayTab" style="display: none;">siteLibraryTab</span></c:otherwise>  
</c:choose>

<script type="text/javascript">
  kaltura.initViewCollection("${currentCollectionId}", "${currentItemId}", "${currentLocationId}", 
      ${isCollection}, ${isKalturaAdmin || isKalturaManager}, ${showDeleteItemWarnings}, ${isMyMedia}, ${clippingEnabled});

  $(document).ready(function() {  
    $('#' + $('#displayTab').text()).addClass('fl-activeTab');
  });
</script>

<jsp:directive.include file="/WEB-INF/jsp/footer.jsp" />
