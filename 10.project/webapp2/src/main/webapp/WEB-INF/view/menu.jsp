<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8" />
<title>機能選択画面</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
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

<div class="card">
	<div class="text-center">
		<h1>メインメニュー</h1>
		<p>実行したい操作を選んでください</p>
	</div>

	<div class="menu-grid">
		<!-- 勤怠管理カード -->
		<div class="menu-card">
			<div class="icon">📅</div>
			<h3>勤怠管理</h3>
			<form action="${pageContext.request.contextPath}/ShainKintai"
				method="post">
				<div class="form-group">
					<label for="id-select" class="form-label">社員ID</label>
					
					<select name="id" id="id-select" class="form-input">
						<option value="1">1</option>
						<option value="7">7</option>
						<option value="47">47</option>
						<option value="125">125</option>
						<option value="505">505</option>
						<option value="617">617</option>
						<option value="668">668</option>
						<option value="711">711</option>
						<option value="830">830</option>
						<option value="1023">1023</option>
						<option value="1126">1126</option>
						<option value="1224">1224</option>


					</select>
				</div>
				<div class="form-group">
					<label for="year-select" class="form-label">年</label>
					<select name="year" id="year-select" class="form-input">
						<option value="2026">2026</option>
						<option value="2025">2025</option>
						<option value="2024">2024</option>

					</select>
				</div>
				<div class="form-group">
					<label for="month-select" class="form-label">月</label>
					<select name="month" id="month-select" class="form-input">
					
						<option value="1">1</option>
						<option value="2">2</option>
						<option value="3">3</option>
						<option value="4">4</option>
						<option value="5">5</option>
						<option value="6">6</option>
						<option value="7">7</option>
						<option value="8">8</option>
						<option value="9">9</option>
						<option value="10">10</option>
						<option value="11">11</option>
						<option value="12">12</option>
					</select>
				</div>
				<button type="submit" class="btn btn-primary" style="width: 100%;">勤怠情報を表示</button>
			</form>
		</div>

		<!-- 社員一覧カード -->
		<a href="${pageContext.request.contextPath}/ShainList" class="menu-card" style="text-decoration: none; color: inherit;">
			<div class="icon">👥</div>
			<h3>社員一覧</h3>
			<p>社員情報の確認や編集を行います。</p>
		</a>
		
		<!-- ログイン画面へ戻るカード -->
		<a href="${pageContext.request.contextPath}/" class="menu-card" style="text-decoration: none; color: inherit;">
			<div class="icon">↩️</div>
			<h3>ログアウト</h3>
			<p>ログイン画面に戻ります。</p>
		</a>
	</div>
</div>
</body>
</html>
