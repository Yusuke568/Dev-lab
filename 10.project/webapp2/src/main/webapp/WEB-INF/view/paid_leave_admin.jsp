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
<body>
	<style>
.admin-grid {
	display: grid;
	grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
	gap: 24px;
	margin: 2rem 0;
}
</style>
<div class="card">
	<div class="text-center" style="margin-bottom: 2rem;">
		<h1>有給休暇管理</h1>
		<p>年度更新や個別での有給休暇付与を管理します。</p>
	</div>
	
	<div style="text-align: right; margin-bottom: 2rem;">
		<a href="${pageContext.request.contextPath}/ShainList" class="btn btn-secondary">‹ 社員一覧へ戻る</a>
	</div>

	<div class="admin-grid">
		<!-- Action Section 1: Grant by Years of Service -->
		<div class="card">
			<h2>🗓️ 勤続年数で一括付与</h2>
			<p>定義済みのルールに基づき、全社員の有給休暇を一括で更新します。</p>
			<form action="GrantLeaveByYearServlet" method="post" style="margin-top: 1rem;">
				<button type="submit" class="btn btn-primary">一括付与を実行</button>
			</form>
		</div>

		<!-- Action Section 2: Grant to Selected Employees -->
		<div class="card">
			<h2>✨ 選択して一括付与</h2>
			<p>チェックを入れた社員に、指定した日数の有給休暇を付与します。</p>
			<form action="GrantLeaveSelectedServlet" method="post" id="grant-selected-form" style="margin-top: 1rem;">
				<div style="display: flex; align-items: center; gap: 10px;">
					<input type="number" name="days" min="1" required
						placeholder="付与日数" class="form-input" style="width: 120px;">
					<button type="submit" class="btn btn-primary">選択者に付与</button>
				</div>
			</form>
		</div>
	</div>


	<!-- Employee List Table -->
	<h2 style="margin-top: 3rem;">社員一覧</h2>
	<table id="employee-table" class="table">
		<thead>
			<tr>
				<th><input type="checkbox" id="select-all" form="grant-selected-form"></th>
				<th>社員ID</th>
				<th>氏名</th>
				<th>勤続年数</th>
				<th>有給休暇残日数</th>
				<th style="text-align: center;">個別操作</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="shain" items="${shainList}">
				<tr>
					<td><input type="checkbox" name="selected_ids" value="${shain.id}" form="grant-selected-form"></td>
					<td><c:out value="${shain.id}" /></td>
					<td><c:out value="${shain.name}" /></td>
					<td><c:out value="${shain.yearsOfService}" /> 年</td>
					<td><c:out value="${shain.paidLeaveDays}" /> 日</td>
					<td>
						<div style="display: flex; align-items: center; justify-content: center; gap: 8px;">
							<input type="number" name="individual_days_${shain.id}"
								min="0" value="${shain.paidLeaveDays}" class="form-input"
								style="width: 80px;">
							<button type="button" onclick="updateIndividual(${shain.id})"
								class="btn btn-secondary" style="padding: 8px 16px;">✓</button>
						</div>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<script src="${pageContext.request.contextPath}/resources/js/paid_leave_admin.js"></script>
</body>
</html>
