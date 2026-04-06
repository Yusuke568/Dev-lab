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
		data-staff-id="${attendanceData.employeeName}"
		data-work-types-json='${workTypesJson}'
		data-year="${attendanceData.yearMonth.year}"
		data-month="${attendanceData.yearMonth.monthValue}">
		<div class="kintai-header">
			<div>
				<h1>勤怠管理</h1>
				<p class="user-info">
					<c:out value="${attendanceData.employeeName}" />
					様 |
					<c:out value="${attendanceData.yearMonth.year}" />
					年
					<c:out value="${attendanceData.yearMonth.monthValue}" />
					月
				</p>
			</div>
			<div class="stat-card">
				<div class="label">合計勤務時間</div>
				<div class="value">
					<c:out value="${attendanceData.totalWorkHours}" />
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
					<th style="width: 8%;">時間</th>
					<th style="width: 15%;">勤務区分</th>
					<th>備考</th>
					<th style="width: 7%;">補正CD</th>
					<th style="width: 7%;">補正(通)</th>
					<th style="width: 7%;">補正(深)</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="daily" items="${attendanceData.dailyRecords}">
					<c:set var="week" value="${daily.date.dayOfWeek.toString()}" />
					<tr data-date="${daily.date}"
						class="${week == 'SATURDAY' ? 'saturday' : ''} ${week == 'SUNDAY' ? 'sunday' : ''}">
						<td><c:out value="${daily.date.dayOfMonth}" /></td>
						<td>${fn:substring(week, 0, 3)}</td>
						<td><c:out value="${daily.startTime}" /></td>
						<td><c:out value="${daily.endTime}" /></td>
						<td><c:out value="${daily.workHours}" /></td>
						<td><select name="status" class="form-input">
								<c:forEach var="opt" items="${workTypes}">
									<option value="${opt.id}"
										${opt.id == daily.abstractId ? 'selected' : ''}>
										<c:out value="${opt.name}" />
									</option>
								</c:forEach>
						</select></td>
						<td><input type="text" name="workDescription"
							value="${daily.workDescription}" class="form-input" /></td>
						<td><input type="number" name="correctionId"
							value="${daily.correctionId}" class="form-input" /></td>
						<td><input type="number" name="correctionUsTime"
							value="${daily.correctionUsTime}" class="form-input" /></td>
						<td><input type="number" name="correctionMidTime"
							value="${daily.correctionMidTime}" class="form-input" /></td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>

	<!-- Status dropdown template for JS -->
	<select id="status-template" style="display: none;">
		<c:forEach var="opt" items="${workTypes}">
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
