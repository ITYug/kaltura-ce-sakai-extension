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
<div class="portlet kaltura-gallery view-edit" role="section">
  <!-- Portlet Views -->
  <div class="portlet-views">
    <ul role="tablist" class="fl-tabs fl-tabs-left ui-helper-clearfix view-tabs">
      <c:if test="${isSiteLibraryVisible }">
        <li tabindex="-1" role="tab">
          <a role="link" key="library" href="viewCollection.htm" title="<spring:message code='app.tooltip.view.site.library'/>">
            <span><spring:message code="app.site.library" /></span>
          </a>
        </li>
      </c:if>
      <li class="fl-activeTab" tabindex="0" role="tab">
        <a role="link" key="collections" href="listCollections.htm" title="<spring:message code='app.tooltip.view.media.collection'/>">
          <span><spring:message code="app.collections" /></span>
        </a>
      </li>
    </ul>
    <div class="fl-tab-content tab-content" role="tabpanel">

      <!-- Portlet Toolbar -->
      <div class="toolbar" role="toolbar"></div>

      <!-- Portlet Body -->
      <div class="content portlet-content" role="main">
        <!-- Portlet Title -->
        <div class="titlebar portlet-titlebar" role="sectionhead">
          <!--<div class="breadcrumb">
                <span class="breadcrumb-1"><a href="listCollections.htm">Collections</a></span>
                <span class="separator"> &gt; </span><span class="breadcrumb-2">
                    <a href="viewCollection.htm?collectionId=${collectionForm.id}">${collectionForm.title}</a>
                </span>
            </div>-->
          <h2 class="title" role="heading">
            <c:choose>
              <c:when test="${ isAdding }">
                <spring:message code="app.toolbar.add.collection" />
              </c:when>
              <c:otherwise>
                <spring:message code="app.edit.collection" />
              </c:otherwise>
            </c:choose>
          </h2>
        </div>

        <kaltura:messages />
        <div class="portlet-form">
          <form:form action="editCollectionDetails.htm" method="post" cssClass="button_form" commandName="collectionForm">
            <form:hidden path="id" />
            <table>
              <tr>
                <td class="label required">
                  <form:label path="title">
                    <spring:message code="editCollectionDetails.name" />:</form:label>
                </td>
                <td>
                  <form:input path="title" cssClass="collection_title inputtext" maxlength="200" size="80" />
                  <span class="collection_title_missing error hide"><spring:message code="editCollectionDetails.title.warning" /></span>
                </td>
              </tr>
              <tr>
                <td class="label">
                  <form:label path="title">
                    <spring:message code="editCollectionDetails.description" />:</form:label>
                </td>
                <td>
                  <form:textarea path="description" cssClass="inputtext" cols="80" rows="3" />
                </td>
              </tr>
              <tr>
                <td class="label required">
                  <form:label path="sharing">
                    <spring:message code="editCollectionDetails.management" />:</form:label>
                </td>
                <td>
                  <%-- NOTE: the option keys must match the MediaCollection.SHARING_* constants --%>
                  <form:select path="sharing">
                    <form:option value="admin">
                      <spring:message code="editCollectionDetails.options.instructor" />
                    </form:option>
                    <form:option value="public">
                      <spring:message code="editCollectionDetails.options.community" />
                    </form:option>
                    <form:option value="shared">
                      <spring:message code="editCollectionDetails.options.personal" />
                    </form:option>
                    <form:option value="private">
                      <spring:message code="editCollectionDetails.options.owner" />
                    </form:option>
                  </form:select>
                </td>
              </tr>
              <tr>
                <td class="label">&nbsp;</td>
                <td>
                  <div class="help-legend">
                    <h3>
                      <spring:message code="editCollectionDetails.legend.description" />
                    </h3>
                    <ul>
                      <li>
                        <strong><spring:message code="editCollectionDetails.options.instructor" /></strong>:
                        <spring:message code="editCollectionDetails.legend.instructor.description" />
                      </li>
                      <li>
                        <strong><spring:message code="editCollectionDetails.options.community" /></strong>:
                        <spring:message code="editCollectionDetails.legend.community.description" />
                      </li>
                      <li>
                        <strong><spring:message code="editCollectionDetails.options.personal" /></strong>:
                        <spring:message code="editCollectionDetails.legend.personal.description" />
                      </li>
                      <li>
                        <strong><spring:message code="editCollectionDetails.options.owner" /></strong>:
                        <spring:message code="editCollectionDetails.legend.owner.description" />
                      </li>
                    </ul>
                  </div>
                </td>
              </tr>
            </table>
            <div id="submit_buttons" class="buttons">
              <input id="submit_save" class="button primary save" type="submit" value="save" />
              <input id="submit_cancel" class="button cancel" type="submit" name="_cancel" value="cancel" />
            </div>
            <div id="submit_spinner" style="display:none;text-align: center;">
              <strong><spring:message code="editCollectionDetails.submit.processing" /></strong>
              <br/>
              <img alt="spinner" src="images/ajax-loader.gif">
            </div>
          </form:form>
        </div>
      </div>
      <!-- end: .portlet-content -->
    </div>
    <!-- end: .portlet -->
  </div>
</div>

<script type="text/javascript">
$(document).ready(function() {

    $("form.button_form input:submit").bind("click keypress", function() {
      // store the id of the submit-input on it's enclosing form
        var $form = $(this).parents('form:first');
        $form.data("callerID", this.id);
    });

    // validate the form
    $("form.button_form").submit(function() {
        var $form = $(this);
        // only validate if the submit was used (and not cancel)
        var validate = false;
        if ($form.data("callerID") == "submit_save") {
            validate = true;
        }
        var $ctitle = $form.find(".collection_title");
        var $ctitlemsg = $form.find(".collection_title_missing");
        if (validate && $ctitle.val().length <= 0) {
            $ctitle.addClass("error");
            $ctitlemsg.removeClass("hide");
            return false;
        } else {
            $ctitle.removeClass("error");
            $ctitlemsg.addClass("hide");
            $("#submit_spinner").show(); // turn on the processing spinner
            $("#submit_buttons").hide(); // hide the submit to reduce confusion
            return true;
        }
    });

});
</script>

<jsp:directive.include file="/WEB-INF/jsp/footer.jsp" />