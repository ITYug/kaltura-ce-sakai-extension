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
  <a role="link" key="collections" href="listCollections.htm" title="<spring:message code='app.tooltip.view.media.collection'/>">
    <span><spring:message code="app.collections" /></span>
  </a>
  |
  <a role="link" key="profiling" href="viewProfiling.htm" title="<spring:message code='app.profiling'/>">
    <span><spring:message code="app.profiling" /></span>
  </a>
</div>

<hr/>

<c:if test="${not empty profiles }">
<table style="width: 90%;" class="profiles-table">
<thead>
<tr>
<th style="text-align:left; width:20%; white-space: nowrap;">Name</th>
<th style="text-align:left; width:20%; white-space: nowrap;">Method</th>
<th style="text-align:left; width: 5%; white-space: nowrap;">Calls</th>
<th style="text-align:left; width:10%; white-space: nowrap;">RunTime</th>
<th style="text-align:left; width:10%; white-space: nowrap;">AvgTime</th>
<th style="text-align:left; width:5%; white-space: nowrap;">AvgReq</th>
<th style="text-align:left; width:20%; white-space: nowrap;">AvgReqTime</th>
</tr>
</thead>
<tbody>
<c:forEach var="profile" items="${profiles}" varStatus="counter">
<tr id="${profile.id}" class="profile-item ${profile.callsUnit} profile-time ${profile.avgUnit}">
<td>${profile.name}</td>
<td>${profile.method}</td>
<td class="profile-calls ${profile.callsUnit}">${profile.calls}</td>
<td title="${profile.runTime}" class="profile-time run ${profile.runUnit}">${profile.runStr}</td>
<td title="${profile.avgTime}" class="profile-time average ${profile.avgUnit}">${profile.avgStr}</td>
<td title="${profile.lastReqNamesStr}" class="profile-calls">${profile.reqAvgCallsStr}</td>
<td title="${profile.reqAvgTime}" class="profile-time average ${profile.reqAvgTimeUnit}">${profile.reqAvgTimeStr}</td>
</tr>
</c:forEach>
</tbody>
</table>
</c:if>

<hr/>

<div class="profiles-text">
<div>Text output:</div>
<pre><c:out value="${summary}" /></pre>
</div>

</body>
</html>