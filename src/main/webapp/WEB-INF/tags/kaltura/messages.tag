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
<%@ tag isELIgnored="false" dynamic-attributes="attributes" body-content="empty" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:if test="${not empty requestScope.alerts}">
	<div class="portlet-msg alert" role="alert">
		<div class="titlebar">
			<h3 class="title">Alert</h3>
		</div>
		<div class="content">
            <c:forEach var="message" items="${requestScope.alerts}">
                <p><spring:message code="${message}" text="${ message }"/></p>
            </c:forEach>
        </div>
    </div>
</c:if>
<c:if test="${not empty requestScope.infos}">
    <div class="portlet-msg info" role="status">
		<div class="titlebar">
<!-- 		<h3 class="title">Message</h3>
		</div>
		<div class="content"> -->
            <c:forEach var="message" items="${requestScope.infos}">
                <p><spring:message code="${message}" text="${ message }"/></p>
            </c:forEach>
        </div>
    </div>
</c:if>