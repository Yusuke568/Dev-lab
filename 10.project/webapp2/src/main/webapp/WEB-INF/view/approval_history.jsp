<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>勤怠承認履歴</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<div class="card">
	<div class="text-center page-title-container">
		<h1>勤怠承認履歴</h1>
		<p>過去に承認・却下された勤怠の記録です（新しい順）。</p>
	</div>

	<div class="page-header-actions">
		<a href="${pageContext.request.contextPath}/approvalList.do" class="btn btn-secondary">‹ 承認待ち一覧へ戻る</a>
		<a href="${pageContext.request.contextPath}/menu.do" class="btn btn-secondary">メニューへ戻る</a>
	</div>

	<form action="${pageContext.request.contextPath}/approvalHistory.do" method="get" class="filter-form"
		style="display: flex; flex-wrap: wrap; gap: 1rem; align-items: flex-end; margin: 1rem 0;">
		<div class="form-group">
			<label class="form-label">社員ID</label>
			<input type="text" name="employeeId" class="form-input" value="<c:out value="${filterEmployeeId}"/>" placeholder="例: 1">
		</div>
		<div class="form-group">
			<label class="form-label">判定</label>
			<select name="decision" class="form-input">
				<option value="" <c:if test="${empty filterDecision}">selected</c:if>>すべて</option>
				<option value="APPROVED" <c:if test="${filterDecision == 'APPROVED'}">selected</c:if>>承認</option>
				<option value="REJECTED" <c:if test="${filterDecision == 'REJECTED'}">selected</c:if>>却下</option>
			</select>
		</div>
		<div class="form-group">
			<label class="form-label">対象日（開始）</label>
			<input type="date" name="from" class="form-input" value="<c:out value="${filterFrom}"/>">
		</div>
		<div class="form-group">
			<label class="form-label">対象日（終了）</label>
			<input type="date" name="to" class="form-input" value="<c:out value="${filterTo}"/>">
		</div>
		<button type="submit" class="btn btn-primary">絞り込む</button>
		<a href="${pageContext.request.contextPath}/approvalHistory.do" class="btn btn-secondary">条件をクリア</a>
	</form>

	<table class="table">
		<thead>
			<tr>
				<th>社員ID</th>
				<th>氏名</th>
				<th>対象日</th>
				<th>判定</th>
				<th>承認者ID</th>
				<th>承認者名</th>
				<th>判定日時</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="item" items="${historyItems}">
				<tr>
					<td><c:out value="${item.employeeId}" /></td>
					<td><c:out value="${item.employeeName}" /></td>
					<td><c:out value="${item.date}" /></td>
					<td>
						<c:choose>
							<c:when test="${item.decision == '承認'}"><span style="color: green; font-weight: bold;">承認</span></c:when>
							<c:otherwise><span style="color: red; font-weight: bold;">却下</span></c:otherwise>
						</c:choose>
					</td>
					<td><c:out value="${item.decidedById}" /></td>
					<td><c:out value="${item.decidedByName}" /></td>
					<td><c:out value="${item.decidedAt}" /></td>
				</tr>
			</c:forEach>
			<c:if test="${empty historyItems}">
				<tr>
					<td colspan="7" class="text-center">承認・却下の履歴はまだありません。</td>
				</tr>
			</c:if>
		</tbody>
	</table>
</div>
</body>
</html>
