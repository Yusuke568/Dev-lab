<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>勤怠承認</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<div class="card">
	<div class="text-center page-title-container">
		<h1>勤怠承認</h1>
		<p>残業の事前申請、および有給取得を含む勤怠申請を確認し、承認または却下します。（通常の勤務は自動承認され、ここには表示されません）</p>
	</div>

	<div class="page-header-actions">
		<a href="${pageContext.request.contextPath}/menu.do" class="btn btn-secondary">‹ メニューへ戻る</a>
		<a href="${pageContext.request.contextPath}/approvalHistory.do" class="btn btn-secondary">📜 承認履歴を見る</a>
	</div>

	<c:if test="${not empty errorMessage}">
		<div class="error-message" style="margin: 1rem 0;"><c:out value="${errorMessage}" /></div>
	</c:if>

	<h2 class="table-title">残業事前申請（承認待ち）</h2>
	<table class="table">
		<thead>
			<tr>
				<th>社員ID</th>
				<th>氏名</th>
				<th>対象日</th>
				<th>予定出勤時刻</th>
				<th>予定退勤時刻</th>
				<th>時間外内訳</th>
				<th>申請理由</th>
				<th>申請日時</th>
				<th class="text-center">操作</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="req" items="${pendingOvertimeRequests}">
				<tr>
					<td><c:out value="${req.employeeId}" /></td>
					<td><c:out value="${req.employeeName}" /></td>
					<td><c:out value="${req.targetDate}" /></td>
					<td><c:out value="${not empty req.plannedStartTime ? req.plannedStartTime : '09:00'}" /></td>
					<td><c:out value="${not empty req.plannedEndTime ? req.plannedEndTime : '18:00'}" /></td>
					<td><c:out value="${req.plannedOvertimeBreakdown}" /></td>
					<td><c:out value="${req.reason}" /></td>
					<td><c:out value="${req.createdAt}" /></td>
					<td>
						<div style="display: flex; gap: 0.5rem; justify-content: center;">
							<form action="${pageContext.request.contextPath}/overtimeRequestExecute.do" method="post">
								<input type="hidden" name="requestId" value="${req.id}">
								<input type="hidden" name="decision" value="approve">
								<button type="submit" class="btn btn-primary">承認</button>
							</form>
							<form action="${pageContext.request.contextPath}/overtimeRequestExecute.do" method="post">
								<input type="hidden" name="requestId" value="${req.id}">
								<input type="hidden" name="decision" value="reject">
								<button type="submit" class="btn btn-danger">却下</button>
							</form>
						</div>
					</td>
				</tr>
			</c:forEach>
			<c:if test="${empty pendingOvertimeRequests}">
				<tr>
					<td colspan="9" class="text-center">承認待ちの残業事前申請はありません。</td>
				</tr>
			</c:if>
		</tbody>
	</table>

	<h2 class="table-title">勤怠申請（有給・想定外の時間外）</h2>
	<div class="filter-form" style="display: flex; flex-wrap: wrap; gap: 1rem; align-items: flex-end; margin: 1rem 0;">
		<div class="form-group">
			<label class="form-label">社員ID</label>
			<input type="text" id="filterEmployeeId" class="form-input" placeholder="例: 1">
		</div>
		<div class="form-group">
			<label class="form-label">理由</label>
			<select id="filterReason" class="form-input">
				<option value="">すべて</option>
				<option value="overtime">時間外</option>
				<option value="leave">有給取得</option>
			</select>
		</div>
	</div>

	<table class="table" id="approval-table">
		<thead>
			<tr>
				<th>社員ID</th>
				<th>氏名</th>
				<th>日付</th>
				<th>曜日</th>
				<th>出勤</th>
				<th>退勤</th>
				<th>勤務区分</th>
				<th>理由</th>
				<th>備考</th>
				<th class="text-center">操作</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="item" items="${pendingItems}">
				<tr data-employee-id="${item.employeeId}"
					data-reason="${item.abstractName == '有給' ? 'leave' : (item.overtimeMinutes > 0 ? 'overtime' : '')}">
					<td><c:out value="${item.employeeId}" /></td>
					<td><c:out value="${item.employeeName}" /></td>
					<td><c:out value="${item.date}" /></td>
					<td><c:out value="${item.week}" /></td>
					<td><c:out value="${item.startTime}" /></td>
					<td><c:out value="${item.endTime}" /></td>
					<td><c:out value="${item.abstractName}" /></td>
					<td>
						<c:if test="${item.abstractName == '有給'}">有給取得</c:if>
						<c:if test="${item.overtimeMinutes > 0}">時間外 (${item.overtimeMinutes}分)</c:if>
					</td>
					<td><c:out value="${item.memo}" /></td>
					<td>
						<div style="display: flex; gap: 0.5rem; justify-content: center;">
							<form action="${pageContext.request.contextPath}/approvalExecute.do" method="post">
								<input type="hidden" name="employeeId" value="${item.employeeId}">
								<input type="hidden" name="date" value="${item.date}">
								<input type="hidden" name="decision" value="approve">
								<button type="submit" class="btn btn-primary">承認</button>
							</form>
							<form action="${pageContext.request.contextPath}/approvalExecute.do" method="post">
								<input type="hidden" name="employeeId" value="${item.employeeId}">
								<input type="hidden" name="date" value="${item.date}">
								<input type="hidden" name="decision" value="reject">
								<button type="submit" class="btn btn-danger">却下</button>
							</form>
						</div>
					</td>
				</tr>
			</c:forEach>
			<c:if test="${empty pendingItems}">
				<tr>
					<td colspan="10" class="text-center">承認待ちの勤怠はありません。</td>
				</tr>
			</c:if>
		</tbody>
	</table>
</div>

<script>
document.addEventListener('DOMContentLoaded', () => {
	const employeeIdInput = document.getElementById('filterEmployeeId');
	const reasonSelect = document.getElementById('filterReason');
	const rows = document.querySelectorAll('#approval-table tbody tr[data-employee-id]');

	function applyFilter() {
		const employeeId = employeeIdInput.value.trim();
		const reason = reasonSelect.value;

		rows.forEach(row => {
			const matchesEmployee = !employeeId || row.dataset.employeeId === employeeId;
			const matchesReason = !reason || row.dataset.reason === reason;
			row.style.display = (matchesEmployee && matchesReason) ? '' : 'none';
		});
	}

	employeeIdInput.addEventListener('input', applyFilter);
	reasonSelect.addEventListener('change', applyFilter);
});
</script>
</body>
</html>
