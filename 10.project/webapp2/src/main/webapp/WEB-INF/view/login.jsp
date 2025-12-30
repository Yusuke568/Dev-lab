<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="beans.*"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8" />
<title>ログイン画面</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
	<div class="card login-card">
		<div class="text-center">
			<h1>ようこそ！</h1>
			<p>IDとパスワードを入力してログインしてください</p>
		</div>
		<form action="${pageContext.request.contextPath}/ShainMenu"
			method="post" style="margin-top: 2rem;">
			<div class="form-group">
				<label for="username" class="form-label">ユーザー名</label>
				<input type="text" id="username" name="username" placeholder="ユーザー名"
					class="form-input" required />
			</div>
			<div class="form-group">
				<label for="password" class="form-label">パスワード</label>
				<input type="password" id="password" name="password" placeholder="パスワード"
					class="form-input" required />
			</div>
			<div class="text-center" style="margin-top: 2rem;">
				<button type="submit" class="btn btn-primary">ログイン</button>
			</div>
		</form>
	</div>
</body>
</html>
