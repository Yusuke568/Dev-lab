<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8" />
<title>機能選択画面</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
<style>
/* Additional styles for menu page */
.menu-grid {
	display: grid;
	grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
	gap: 24px;
	margin-top: 2rem;
}

.menu-card {
	background: var(--card-background);
	border-radius: var(--border-radius);
	box-shadow: var(--card-shadow);
	padding: 24px;
	text-align: center;
	transition: transform 0.2s, box-shadow 0.2s;
	border: 1px solid #e9ecef;
}

.menu-card:hover {
	transform: translateY(-5px);
	box-shadow: 0 15px 35px rgba(0,0,0,0.1);
}

.menu-card .icon {
	font-size: 3rem;
	margin-bottom: 1rem;
}

.menu-card h3 {
	margin-bottom: 1.5rem;
}
</style>
</head>
<body>
<div class="card">
	<div class="text-center">
		<h1>メインメニュー</h1>
		<p>実行したい操作を選んでください</p>
	</div>

	<c:if test="${not empty alertMessage}">
		<div style="background-color: #fff3cd; color: #856404; padding: 1rem; border-radius: 4px; margin-top: 1rem; border: 1px solid #ffeeba;">
			<strong>お知らせ: </strong> <c:out value="${alertMessage}" />
		</div>
	</c:if>

	<div class="menu-grid">
		<!-- 勤怠管理カード -->
		<div class="menu-card">
			<div class="icon">📅</div>
			<h3>勤怠管理</h3>
			<form action="${pageContext.request.contextPath}/kintaiDisplay.do"
				method="post">
				<input type="hidden" name="id" value="${sessionScope.loginUser}" />
				<div class="form-group" style="text-align: left; padding: 0.5rem 0;">
					<label class="form-label" style="display: inline;">社員ID: </label>
					<span style="font-weight: bold;"><c:out value="${sessionScope.loginUser}" /></span>
				</div>
				<div class="form-group">
					<label for="ym-select" class="form-label">年月</label>
					<select id="ym-select" class="form-input" onchange="document.getElementById('hidden-year').value = this.value.split('-')[0]; document.getElementById('hidden-month').value = this.value.split('-')[1];">
						<c:forEach var="ym" items="${availableMonths}">
							<option value="${ym.year}-${ym.monthValue}" <c:if test="${ym == currentMonth}">selected</c:if>>${ym.year}年${ym.monthValue}月</option>
						</c:forEach>
					</select>
				</div>
				<input type="hidden" name="year" id="hidden-year" value="${currentMonth.year}">
				<input type="hidden" name="month" id="hidden-month" value="${currentMonth.monthValue}">
				<button type="submit" class="btn btn-primary" style="width: 100%;">勤怠情報を表示</button>
			</form>
		</div>

		<c:if test="${sessionScope.loginUserRole != '0'}">
			<!-- 社員一覧カード -->
			<div class="menu-card-wrapper">
				<div class="menu-card" style="cursor: pointer; height: 100%;" onclick="location.href='${pageContext.request.contextPath}/shainList.do'">
					<div class="icon">👥</div>
					<h3>社員一覧</h3>
					<p>社員情報の確認や編集を行います。</p>
				</div>
			</div>
			
			<!-- 有給休暇管理カード -->
			<div class="menu-card-wrapper">
				<div class="menu-card" style="cursor: pointer; height: 100%;" onclick="location.href='${pageContext.request.contextPath}/paidLeaveAdmin.do'">
					<div class="icon">🏖️</div>
					<h3>有給休暇管理</h3>
					<p>有給休暇の付与や日数の管理を行います。</p>
				</div>
			</div>
		</c:if>
		
		<!-- ログイン画面へ戻るカード -->
		<div class="menu-card-wrapper">
			<div class="menu-card" style="cursor: pointer; height: 100%;" onclick="location.href='${pageContext.request.contextPath}/logout.do'">
				<div class="icon">↩️</div>
				<h3>ログアウト</h3>
				<p>ログアウトしてログイン画面に戻ります。</p>
			</div>
		</div>
	</div>
</div>
</body>
</html>
