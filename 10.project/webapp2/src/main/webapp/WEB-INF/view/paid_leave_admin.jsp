<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>有給休暇管理</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
<%@ include file="_icons.jspf" %>
</head>
<body data-context-path="${pageContext.request.contextPath}">

<div class="card">
	<div class="text-center page-title-container">
		<h1>有給休暇管理</h1>
		<p>年度更新や個別での有給休暇付与を管理します。</p>
	</div>
	
	<c:if test="${not empty message}">
		<div class="status-item status-info" style="margin: 1rem 0;">✓ <c:out value="${message}" /></div>
	</c:if>
	<c:if test="${not empty errorMessage}">
		<div class="error-message" style="margin: 1rem 0;"><c:out value="${errorMessage}" /></div>
	</c:if>

	<div class="page-header-actions">
		<a href="${pageContext.request.contextPath}/shainList.do" class="btn btn-secondary">‹ 社員一覧へ戻る</a>
	</div>

	<div class="admin-grid">
		<!-- Action Section 1: Grant by Years of Service -->
		<div class="card">
			<h2>🗓️ 勤続年数で一括付与</h2>
			<p>マスタ管理の「有給付与ルール」に基づき、勤続年数（入社年から数えます）に合う日数を全社員の残日数に足します。
				ルールより長く勤めている人には、一番長いルールの日数を足します。入社年が未設定の社員には付与しません。<br>
				<strong>実行するたびに足されます。</strong>年に1回だけ実行してください。足される日数は下の一覧の「一括付与で足す日数」で確かめられます。</p>
			<form action="${pageContext.request.contextPath}/grantLeaveByYear.do" method="post" class="leave-grant-form"
				onsubmit="return confirm('${grantPreviewCount}人に、勤続年数に応じた有給休暇を付与します。\n実行するたびに足されます。実行しますか？');">
				<button type="submit" class="btn btn-primary">一括付与を実行</button>
			</form>
		</div>

		<!-- Action Section 2: Grant to Selected Employees -->
		<div class="card">
			<h2>✨ 選択して一括付与</h2>
			<p>チェックを入れた社員に、指定した日数の有給休暇を付与します。</p>
			<form action="${pageContext.request.contextPath}/grantLeaveSelected.do" method="post" id="grant-selected-form" class="leave-grant-form selected-grant">
				<input type="number" name="days" min="1" required
					placeholder="付与日数" class="form-input">
				<button type="submit" class="btn btn-primary">選択者に付与</button>
			</form>
		</div>
	</div>


	<!-- Employee List Table -->
	<h2 class="table-title">社員一覧</h2>
	<table id="employee-table" class="table">
		<thead>
			<tr>
				<th><input type="checkbox" id="select-all" form="grant-selected-form"></th>
				<th>社員ID</th>
				<th>氏名</th>
				<th>勤続年数</th>
				<th>一括付与で足す日数</th>
				<th>有給休暇残日数</th>
				<th class="text-center">個別操作</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="shain" items="${shainList}">
				<tr data-employee-id="${shain.id}">
					<td><input type="checkbox" name="selected_ids" value="${shain.id}" form="grant-selected-form"></td>
					<td><c:out value="${shain.id}" /></td>
					<td><c:out value="${shain.name}" /></td>
					<td><c:choose>
						<c:when test="${shain.entryyear > 0}"><c:out value="${shain.yearsOfService}" /> 年</c:when>
						<c:otherwise><span style="color:#a0aec0;">入社年 未設定</span></c:otherwise>
					</c:choose></td>
					<td><c:choose>
						<c:when test="${not empty grantPreview[shain.id.toString()]}">＋<c:out value="${grantPreview[shain.id.toString()]}" /> 日</c:when>
						<c:otherwise><span style="color:#a0aec0;">なし</span></c:otherwise>
					</c:choose></td>
					<td class="paid-leave-days"><c:out value="${shain.paidLeaveDaysLabel}" /> 日</td>
					<td>
						<div class="individual-update-form">
							<input type="number" name="individual_days"
								min="0" step="0.5" value="${shain.paidLeaveDaysLabel}" class="form-input">
							<button type="button" class="btn btn-secondary individual-update-btn">✓</button>
						</div>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<script src="${pageContext.request.contextPath}/resources/js/paid_leave_admin.js" type="module"></script>
</body>
</html>