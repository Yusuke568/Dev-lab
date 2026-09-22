<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>残業事前申請</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<div class="card">
	<div class="text-center page-title-container">
		<h1>残業事前申請</h1>
		<p>時間外労働を行う前に、この画面から申請してください。承認結果はこの画面で確認できます。却下された場合はこの画面から再申請できます。</p>
	</div>

	<div class="page-header-actions">
		<a href="${pageContext.request.contextPath}/menu.do" class="btn btn-secondary">‹ メニューへ戻る</a>
	</div>

	<c:if test="${not empty errorMessage}">
		<div class="error-message" style="margin: 1rem 0;"><c:out value="${errorMessage}" /></div>
	</c:if>

	<c:if test="${not empty prefillDate}">
		<div class="status-item status-info" style="margin: 1rem 0;">
			ℹ <c:out value="${prefillDate}" /> の勤怠実績から予定出勤・予定退勤時刻を自動入力しました。内容を確認のうえ申請してください。
		</div>
	</c:if>

	<div class="card">
		<h2>新規申請</h2>
		<form action="${pageContext.request.contextPath}/overtimeRequestSubmit.do" method="post"
			style="display: flex; flex-wrap: wrap; gap: 1rem; align-items: flex-end;">
			<div class="form-group">
				<label class="form-label">対象日</label>
				<input type="date" id="targetDate" name="targetDate" class="form-input" value="${prefillDate}" required>
			</div>
			<div class="form-group">
				<label class="form-label">予定出勤時刻</label>
				<input type="time" id="plannedStartTime" name="plannedStartTime" class="form-input" value="${prefillStartTime}">
			</div>
			<div class="form-group">
				<label class="form-label">予定退勤時刻</label>
				<input type="time" id="plannedEndTime" name="plannedEndTime" class="form-input" value="${prefillEndTime}">
			</div>
			<div class="form-group" style="flex: 1; min-width: 200px;">
				<label class="form-label">申請理由</label>
				<input type="text" name="reason" class="form-input" placeholder="例: 月末締め作業のため" style="width: 100%;">
			</div>
			<button type="submit" class="btn btn-primary">申請</button>
		</form>
		<p style="font-size: 0.85rem; color: #666; margin-top: 0.5rem;">
			※ 標準勤務時間（09:00〜18:00）より早い予定出勤・遅い予定退勤の分が、それぞれ早出・残業として時間外申請されます（両方入力すれば同日で両方申請できます）。標準どおりの時刻は空欄のままで構いません。<br>
			※ 却下された対象日については、同じ対象日を指定して再度申請するとその申請が更新され、再度承認待ちになります。
		</p>
	</div>

	<h2 class="table-title">申請履歴</h2>
	<table class="table">
		<thead>
			<tr>
				<th>対象日</th>
				<th>予定出勤時刻</th>
				<th>予定退勤時刻</th>
				<th>時間外内訳</th>
				<th>申請理由</th>
				<th>ステータス</th>
				<th>申請日時</th>
				<th>承認/却下日時</th>
				<th>実際の勤怠実績</th>
				<th class="text-center">操作</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="req" items="${myRequests}">
				<c:set var="actual" value="${attendanceByDate[req.targetDate]}" />
				<tr>
					<td><c:out value="${req.targetDate}" /></td>
					<td><c:out value="${not empty req.plannedStartTime ? req.plannedStartTime : '09:00'}" /></td>
					<td><c:out value="${not empty req.plannedEndTime ? req.plannedEndTime : '18:00'}" /></td>
					<td><c:out value="${req.plannedOvertimeBreakdown}" /></td>
					<td><c:out value="${req.reason}" /></td>
					<td>
						<c:choose>
							<c:when test="${req.status == 'APPROVED'}"><span style="color: green; font-weight: bold;">承認済み</span></c:when>
							<c:when test="${req.status == 'REJECTED'}"><span style="color: red; font-weight: bold;">却下</span></c:when>
							<c:otherwise><span style="color: orange; font-weight: bold;">申請中</span></c:otherwise>
						</c:choose>
					</td>
					<td><c:out value="${req.createdAt}" /></td>
					<td><c:out value="${req.decidedAt}" /></td>
					<td>
						<c:choose>
							<c:when test="${not empty actual}">
								出勤<c:out value="${actual.startTime}" />〜退勤<c:out value="${actual.endTime}" />（実働<c:out value="${actual.workHours}" />）
							</c:when>
							<c:otherwise>未入力</c:otherwise>
						</c:choose>
					</td>
					<td class="text-center">
						<c:if test="${req.status == 'REJECTED'}">
							<button type="button" class="btn btn-secondary" onclick="prefillReapply('${req.targetDate}', '${req.plannedStartTime}', '${req.plannedEndTime}')">この日で再申請</button>
						</c:if>
					</td>
				</tr>
			</c:forEach>
			<c:if test="${empty myRequests}">
				<tr>
					<td colspan="10" class="text-center">申請履歴はまだありません。</td>
				</tr>
			</c:if>
		</tbody>
	</table>
</div>

<script>
function prefillReapply(targetDate, plannedStartTime, plannedEndTime) {
	document.getElementById('targetDate').value = targetDate;
	document.getElementById('plannedStartTime').value = plannedStartTime || '';
	document.getElementById('plannedEndTime').value = plannedEndTime || '';
	document.getElementById('targetDate').scrollIntoView({ behavior: 'smooth', block: 'center' });
}
</script>
</body>
</html>
