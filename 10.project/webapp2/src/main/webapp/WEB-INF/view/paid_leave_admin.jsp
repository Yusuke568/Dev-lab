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
</head>
<body data-context-path="${pageContext.request.contextPath}">

<div class="card">
	<div class="text-center page-title-container">
		<h1>有給休暇管理</h1>
		<p>年度更新や個別での有給休暇付与を管理します。</p>
	</div>
	
	<div class="page-header-actions">
		<a href="${pageContext.request.contextPath}/shain/list.do" class="btn btn-secondary">‹ 社員一覧へ戻る</a>
	</div>

	<div class="admin-grid">
		<!-- Action Section 1: Grant by Years of Service -->
		<div class="card">
			<h2>🗓️ 勤続年数で一括付与</h2>
			<p>定義済みのルールに基づき、全社員の有給休暇を一括で更新します。</p>
			<form action="${pageContext.request.contextPath}/paid_leave/grant_by_year.do" method="post" class="leave-grant-form">
				<button type="submit" class="btn btn-primary">一括付与を実行</button>
			</form>
		</div>

		<!-- Action Section 2: Grant to Selected Employees -->
		<div class="card">
			<h2>✨ 選択して一括付与</h2>
			<p>チェックを入れた社員に、指定した日数の有給休暇を付与します。</p>
			<form action="${pageContext.request.contextPath}/paid_leave/grant_selected.do" method="post" id="grant-selected-form" class="leave-grant-form selected-grant">
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
					<td><c:out value="${shain.yearsOfService}" /> 年</td>
					<td class="paid-leave-days"><c:out value="${shain.paidLeaveDays}" /> 日</td>
					<td>
						<div class="individual-update-form">
							<input type="number" name="individual_days"
								min="0" value="${shain.paidLeaveDays}" class="form-input">
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
