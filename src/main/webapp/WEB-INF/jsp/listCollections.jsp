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

<c:choose>
  <c:when test="${! isKalturaEnabled}">
    <h2 class="error">
      <spring:message code="listCollections.kaltura.enabled.warning" />
    </h2>
    <c:if test="${! isKalturaConfigured}">
      <h2 class="error">
        <spring:message code="listCollections.kaltura.configured.warning" />
      </h2>
    </c:if>
  </c:when>
  <c:when test="${isKalturaMigrating}">
    <h2 class="error">
      <spring:message code="listCollections.kaltura.migrating.warning" arguments="${migrationMessageNumbers}" />
    </h2>
  </c:when>
  <c:otherwise>
    <!-- Portlet -->
    <div class="portlet kaltura-gallery view-home" role="section">
      <!-- Portlet Views -->
      <div class="portlet-views">
        <ul role="tablist" class="fl-tabs fl-tabs-left ui-helper-clearfix view-tabs">
          <c:if test="${isSiteLibraryVisible }">
            <li tabindex="-1" role="tab">
              <a role="link" key="library" href="viewCollection.htm" title="<spring:message code='app.tooltip.view.site.library'/>"><span><spring:message
                    code="app.site.library" /></span></a>
            </li>
          </c:if>
          <li class="fl-activeTab" tabindex="0" role="tab">
            <a role="link" key="collections" href="listCollections.htm" title="<spring:message code='app.tooltip.view.media.collection'/>"><span><spring:message
                  code="app.collections" /></span></a>
          </li>
          <c:if test="${ showMyMediaTab }">
            <li tabindex="-1" role="tab" id="myMediaTab">
              <a role="link" key="library" href="viewCollection.htm?view=myMedia" title="<spring:message code='app.my.media'/>"><span><spring:message code='app.my.media'/></span></a>
            </li>  
          </c:if>
          <c:if test="${ isProfilingEnabled }">
            <li tabindex="-1" role="tab" id="profilingTab">
              <a role="link" key="library" href="viewProfiling.htm" title="<spring:message code='app.profiling'/>">
                <spring:message code="app.profiling" />
              </a>
            </li> 
          </c:if>
        </ul>
        <div class="fl-tab-content tab-content" role="tabpanel">
          <!-- Portlet Toolbar -->
          <div class="toolbar">
            <ul>
              <c:if test="${isKalturaAdmin}">
                <li>
                  <a href="editCollectionDetails.htm" class="add" title="<spring:message code='listCollections.tooltip.add.new.collection'/>"> <spring:message
                      code="app.toolbar.add.collection" />
                  </a>
                </li>
              </c:if>
            </ul>
          </div>

          <!-- Portlet Body -->
          <div class="content portlet-content" role="main">
            <!-- Portlet Title -->
            <c:if test="${isInitialized}">
              <div class="titlebar portlet-titlebar" role="sectionhead">
                <h2 class="title" role="heading">
                  <spring:message code="listCollections.title" />
                </h2>
              </div>
            </c:if>
            <kaltura:messages />
            <c:if test="${toolIntroText != null}">${ toolIntroText }</c:if>
            <c:choose>
              <c:when test="${! isInitialized}">
                <h2 class="error">
                  <spring:message code="listCollections.kaltura.initialized.warning" />
                </h2>
              </c:when>
              <c:when test="${isInitialized && empty collections }">
                <div class="portlet-msg info empty" role="note">
                  <div class="titlebar">
                    <h3 class="title">                    
                      <spring:message code="listCollections.no.collections.title" />
                    </h3>
                  </div>
                  <div class="content">
                    <p>
                      <spring:message code="listCollections.no.media.collections" />
                      <c:choose>
                        <c:when test="${ isKalturaAdmin || isKalturaWrite }">
                          <c:if test="${ isKalturaAdmin }">
                            <a href="editCollectionDetails.htm" title="<spring:message code='listCollections.tooltip.add.new.collection'/>"> <spring:message
                                code="app.toolbar.add.collection" />
                            </a>
                          </c:if>
                          <c:if test="${ isKalturaWrite && isSiteLibraryVisible }">
                            <spring:message code="app.or" />
                            <a href="uploadMedia.htm" title="<spring:message code='app.tooltip.upload.new.media'/>"> <spring:message
                                code="app.upload.new.media" />
                            </a>.
                    </c:if>
                        </c:when>
                        <c:otherwise>
                          <span><spring:message code="listCollections.assistance" /></span>
                        </c:otherwise>
                      </c:choose>
                    </p>
                  </div>
                </div>
                <c:choose>
                  <c:when test="${ isKalturaAdmin }">
                    <div>
                      <c:choose>
                        <c:when test="${ emptyCollText == null }">
                          <h3>
                            <spring:message code="listCollections.media.gallery.description" />
                          </h3>
                          <c:if test="${ isSiteLibraryVisible }">
                            <h4>
                              <spring:message code="app.site.library" />
                            </h4>
                            <p>
                              <spring:message code="listCollections.site.library.desc.1" />
                              <spring:message code="listCollections.site.library.desc.2" />
                              <c:set var="libraryLink">
                                <a href="viewCollection.htm" title="<spring:message code='listCollections.tooltip.site.library'/>">
                                    <spring:message code="app.site.library" />
                                </a>
                              </c:set>
                              <spring:message code="listCollections.site.library.desc.3" arguments="${libraryLink}" />
                            </p>
                          </c:if>
                          <h4>
                            <spring:message code="listCollections.media.collections" />
                          </h4>
                          <p>
                            <spring:message code="listCollections.media.collections.desc" />
                            &nbsp; <a href="editCollectionDetails.htm" title="<spring:message code='listCollections.tooltip.add.new.collection'/>">
                              <spring:message code="app.toolbar.add.collection" />
                            </a>
                          </p>
                          <h4>
                            <spring:message code="listCollections.media.view.play" />
                          </h4>
                          <p>
                            <spring:message code="listCollections.media.view.play.desc" />
                          </p>
                        </c:when>
                        <c:otherwise>${ emptyCollText }</c:otherwise>
                      </c:choose>
                    </div>
                  </c:when>
                  <c:otherwise>
                    <c:if test="${ isSiteLibraryVisible }">
                      <c:set var="libraryLink">
                        <a href="viewCollection.htm" title="<spring:message code='listCollections.tooltip.site.library'/>"><spring:message
                            code="app.site.library" /></a>
                      </c:set>
                      <p>
                        <spring:message code="listCollections.go.to.site.library.message" arguments="${libraryLink}" />
                      </p>
                    </c:if>
                  </c:otherwise>
                </c:choose>
              </c:when>
              <c:when test="${ not viewableCollections }">
                <div class="portlet-msg info empty" role="note">
                  <div class="titlebar">
                    <h3 class="title">
                      <spring:message code="listCollections.no.viewable.collections" />
                    </h3>
                  </div>
                </div>
              </c:when>
              <c:otherwise>
                <!-- Portlet Panel List -->
                <div class="portlet-panellist collections-list">
                  <%-- KAL-40 collection visibility
                   * Show the collection to admin/manage/editor users all the time. 
                   * Show the "no items" message to admin/manage/editor users when there are no items in the collection. 
                   * Show the "no public items" message to admin/manage/editor users when all the items in the collection are private. 
                   * Show a collection to read/write users only when the collection has at least one public item
                   * Show to all users when collection is shared OR public
                   --%>
                  <c:forEach var="collection" items="${collections}" varStatus="counter">
                    <c:choose>
                      <c:when test="${ not collection.viewable }">
                        <!-- collection ${collection.idStr} - ${collection.title}, no rendering of collection for ${currentUserId} user -->
                      </c:when>
                      <c:otherwise>
                        <!-- Panel -->
                        <div class="panel">
                          <div class="panel-titlebar">
                            <h4 class="title">
                              <a href="viewCollection.htm?collectionId=${collection.idStr}" title="${collection.title}">${collection.shortTitle}</a>
                            </h4>
                          </div>
                          <div class="content">
                            <p class="description" title="${collection.description}">${collection.shortDescription}</p>
                            <c:choose>
                              <c:when test="${ collection.unpopulated }">
                                <div class="portlet-msg info empty" role="note">
                                  <div class="titlebar">
                                    <h3 class="title">
                                      <spring:message code="listCollections.no.media.items" />
                                    </h3>
                                  </div>
                                  <div class="content">
                                    <p>
                                      <spring:message code="app.collection.empty" />
                                      <c:if test="${collection.addItems}">
                                        <a href="editCollectionMedia.htm?collectionId=${collection.idStr}"
                                          title="<spring:message code='app.tooltip.add.media'/>"> <spring:message code="app.collection.add.media" />
                                        </a>
                                        <c:if test="${ isKalturaWrite }">
                                          <spring:message code='app.or' />
                                          <a href="uploadMedia.htm?collectionId=${collection.idStr}"
                                            title="<spring:message code='app.tooltip.upload.media.to.collection'/>"> <spring:message
                                              code="app.upload.media.to.collection" />
                                          </a>
                                        </c:if>
                                      </c:if>
                                    </p>
                                  </div>
                                </div>
                              </c:when>
                              <c:otherwise>
                                <c:choose>
                                  <c:when test="${not collection.visibleItems}">
                                    <div class="portlet-msg info empty" role="note">
                                      <div class="titlebar">
                                        <h3 class="title">
                                          <spring:message code="listCollections.collection.no.public.media" />
                                        </h3>
                                      </div>
                                      <div class="content">
                                        <p>
                                          <spring:message code="listCollections.collection.no.public.media.desc" />
                                        </p>
                                      </div>
                                    </div>
                                  </c:when>
                                  <c:otherwise>
                                    <%-- show first public item --%>
                                    <div class="featured-item">
                                      <div class="media-item type-${collection.displayItem.mediaType} ${collection.displayItem.shared ? 'shared':''}">
                                        <%--  ${collection.items[0].hidden ? 'hidden':''} --%>
                                        <div class="fl-col-flex2">
                                          <div class="fl-col">
                                            <div class="thumbnail">
                                              <a class="thumbnail-link"
                                                href="viewCollection.htm?collectionId=${collection.idStr}&mid=${collection.displayItem.id}"
                                                title="${collection.displayItem.name}"> <span> <img
                                                  src="${collection.displayItem.thumbnail}" alt="${collection.displayItem.name}" />
                                              </span>
                                              </a>
                                              <c:if test="collection.displayItem.hidden">
                                                <span class="hidden-marker"> <spring:message code="listCollections.private" />
                                                </span>
                                              </c:if>
                                            </div>
                                          </div>
                                          <div class="fl-col">
                                            <div class="thumbnail-title">
                                              <a href="viewCollection.htm?collectionId=${collection.idStr}&mid=${collection.displayItem.id}"
                                                title="${collection.displayItem.name}">${collection.displayItem.shortName}</a>
                                            </div>
                                            <c:if test="${!empty collection.displayItem.desc}">
                                              <p class="description">${collection.displayItem.desc}</p>
                                            </c:if>
                                            <p class="author-name">${collection.displayItem.author.name}</p>
                                          </div>
                                        </div>
                                      </div>
                                    </div>
                                  </c:otherwise>
                                </c:choose>
                              </c:otherwise>
                            </c:choose>
                          </div>
                          <!-- end: .content -->
                        </div>
                        <!-- end: .panel -->
                      </c:otherwise>
                    </c:choose>
                  </c:forEach>
                </div>
                <!-- end: .portlet-panellist -->
              </c:otherwise>
            </c:choose>
          </div>
          <!-- end: .portlet-content -->
        </div>
        <!-- end: .tab-content -->
      </div>
      <!-- end: .portlet-views -->
    </div>
    <!-- end: .portlet -->

    <!-- Hidden block to declare resource bundle variables for use in JS -->
    <div style="display: none;">
      <!-- By convention, the id of the span must be "i18n_"+{message code}, the container div should be set to display:none;, use kaltura.i18n(key) in JS to lookup the message -->
      <span id="i18n_listCollections.delete.collection.confirmation"> <spring:message code="listCollections.delete.collection.confirmation" />
      </span>
    </div>

<script type="text/javascript">
$(function(){
    $(document).ready(function(){
        // Set hover state for panels.
        $(".panel").hover(
            function(){ $(this).addClass("panel-hover"); },
            function(){ $(this).removeClass("panel-hover"); }
        );
        
        // Send user to the collection view when the panel is clicked.
        $('.panel').click(function(){
            var url = $(this).find(".panel-titlebar .title a").attr("href");
            document.location.href = url;
            return;
        });
    });
    
    $('.delete_coll_confirm').click(function(){
        var answer = confirm(kaltura.i18n("i18n_listCollections.delete.collection.confirmation"));
        return answer;
    });
});
</script>

  </c:otherwise>
</c:choose>

<jsp:directive.include file="/WEB-INF/jsp/footer.jsp" />
