<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>社員登録画面</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
	<div class="card" style="max-width: 600px;">
		<div class="text-center" style="margin-bottom: 2rem;">
			<h1>新しい仲間を登録</h1>
			<p>新しい社員の情報を入力してください。</p>
		</div>
		<form action="ShainInsertComplete" method="post">
			<div class="form-group">
				<label for="id" class="form-label">社員ID</label> <input type="text"
					id="id" name="id" class="form-input" pattern="\\d{3}" required
					title="IDは3桁の数字で入力してください">
			</div>
			<div class="form-group">
				<label for="name" class="form-label">名前</label> <input type="text"
					id="name" name="name" class="form-input" required>
			</div>
			<div class="form-group">
				<label for="sei" class="form-label">性別</label> <select id="sei"
					name="sei" class="form-input" required>
					<option value="">選択してください</option>
					<option value="男">男</option>
					<option value="女">女</option>
				</select>
			</div>
			<div class="form-group">
				<label for="nen" class="form-label">入社年</label> <select id="nen"
					name="nen" class="form-input" required>
					<option value="">選択してください</option>
					<%
						for (int year = 2001; year <= 2025; year++) {
					%>
					<option value="<%=year%>"><%=year%></option>
					<%
						}
					%>
				</select>
			</div>
			<div class="form-group">
				<label for="address" class="form-label">住所</label> <input
					type="text" id="address" name="address" class="form-input" required>
			</div>

			<div style="display: flex; justify-content: space-between; margin-top: 40px;">
				<a href="${pageContext.request.contextPath}/ShainList" class="btn btn-secondary">‹ 戻る</a>
				<button type="submit" class="btn btn-primary">✓ 登録</button>
			</div>
		</form>
	</div>
</body>
</html>