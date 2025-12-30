<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8" />
<title>勤怠管理システム</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">
	<div id="kintai-app-root" class="card" 
		data-staff-id="${staff_id}"
		data-status-options-json='${statusOptionsJson}'
		data-year="${year}"
		data-month="${month}">
		<div class="kintai-header">
			<div>
				<h1>勤怠管理</h1>
				<p class="user-info">
					<c:out value="${shainbean.name}" />
					様 |
					<c:out value="${year}" />
					年
					<c:out value="${month}" />
					月
				</p>
			</div>
			<div class="stat-card">
				<div class="label">有給休暇残日数</div>
				<div class="value">
					<c:out value="${shainbean.paidLeaveDays}" />日
				</div>
			</div>
		</div>

		<div class="card actions-card">
			<div style="display: flex; gap: 1rem;">
				<button id="clock-in-btn" onclick="recordAttendance('出勤')"
					class="btn btn-primary">☀️ 出勤</button>
				<button id="clock-out-btn" onclick="recordAttendance('退勤')"
					class="btn btn-primary">🌙 退勤</button>
			</div>
			<div id="overtime-summary"></div>
			<div>
				<button id="save-btn" onclick="saveAttendance()"
					class="btn btn-success">💾 保存</button>
			</div>
		</div>

		<table id="calendar-log" class="table">
			<thead>
				<tr>
					<th style="width: 5%;">日</th>
					<th style="width: 8%;">曜日</th>
					<th style="width: 8%;">出勤</th>
					<th style="width: 8%;">退勤</th>
					<th style="width: 8%;">時間外</th>
					<th style="width: 15%;">勤務区分</th>
					<th>備考</th>
					<th style="width: 7%;">補正CD</th>
					<th style="width: 7%;">補正(通)</th>
					<th style="width: 7%;">補正(深)</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="calendarbean" items="${calendarBeanList}">
					<c:set var="week" value="${calendarbean.week.toString()}" />
					<tr data-date="${calendarbean.kintaidate}"
						class="${week == 'SATURDAY' ? 'saturday' : ''} ${week == 'SUNDAY' ? 'sunday' : ''}">
						<td><fmt:formatDate value="${calendarbean.getDateOfKintaidate()}"
								pattern="d" /></td>
						<td>${fn:substring(week, 0, 3)}</td>
						<td><fmt:formatDate value="${calendarbean.kintaifrom}"
								pattern="HH:mm" /></td>
						<td><fmt:formatDate value="${calendarbean.kintaito}"
								pattern="HH:mm" /></td>
						<td><c:out value="${calendarbean.jikangai}" /></td>
						<td><select name="status" class="form-input">
								<c:forEach var="opt" items="${statusOptions}">
									<option value="${opt.id}"
										${opt.id == calendarbean.abstractId ? 'selected' : ''}>
										<c:out value="${opt.name}" />
									</option>
								</c:forEach>
						</select></td>
						<td><c:out value="${calendarbean.memo}" /></td>
						<td><input type="number" name="correctionId"
							value="${calendarbean.correctionId}" class="form-input" /></td>
						<td><input type="number" name="correctionUsTime"
							value="${calendarbean.correctionUsTime}" class="form-input" /></td>
						<td><input type="number" name="correctionMidTime"
							value="${calendarbean.correctionMidTime}" class="form-input" /></td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>

	<!-- Status dropdown template for JS -->
	<select id="status-template" style="display: none;">
		<c:forEach var="opt" items="${statusOptions}">
			<option value="<c:out value="${opt.id}"/>">
				<c:out value="${opt.name}" />
			</option>
		</c:forEach>
	</select>

	<script
		src="${pageContext.request.contextPath}/resources/js/kintai/kintai.js"
		type="module">
	</script>

</body>
</html>
