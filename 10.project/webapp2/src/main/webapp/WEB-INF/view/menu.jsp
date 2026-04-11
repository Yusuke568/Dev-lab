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
				<c:if test="${sessionScope.loginUserRole != '0'}">
					<div class="form-group">
						<label for="id-select" class="form-label">社員ID</label>
						<select name="id" id="id-select" class="form-input">
							<option value="668">668</option>
							<option value="669">669</option>
							<option value="670">670</option>
						</select>
					</div>
				</c:if>
				<c:if test="${sessionScope.loginUserRole == '0'}">
					<input type="hidden" name="id" value="${sessionScope.loginUser}" />
				</c:if>
				<div class="form-group">
					<label for="year-select" class="form-label">年</label>
					<select name="year" id="year-select" class="form-input">
						<option value="2025">2025</option>
						<option value="2024">2024</option>
					</select>
				</div>
				<div class="form-group">
					<label for="month-select" class="form-label">月</label>
					<select name="month" id="month-select" class="form-input">
						<option value="9">9</option>
						<option value="10">10</option>
						<option value="11">11</option>
					</select>
				</div>
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
