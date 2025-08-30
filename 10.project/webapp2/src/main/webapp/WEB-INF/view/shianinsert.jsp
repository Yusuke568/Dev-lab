<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>社員登録画面</title>
<style>
.form-input {
	width: 100%;
}

.form-table td {
	padding: 5px;
}

.form-table label {
	text-align: right;
}

.form-button {
	margin-top: 10px;
}
</style>
</head>
<body>
	<h1>社員登録画面</h1>
	<form action="ShainInsertComplete" method="post">
		<table class="form-table">
			<tr>
				<td><label for="id">ID:</label></td>
				<td><input type="text" id="id" name="id" class="form-input"
					pattern="\d{3}" required title="IDは3桁の数字で入力してください"></td>
			</tr>
			<tr>
				<td><label for="name">名前:</label></td>
				<td><input type="text" id="name" name="name" class="form-input"
					required></td>
			</tr>
			<tr>
				<td><label for="sei">姓:</label></td>
				<td><select id="sei" name="sei" class="form-input" required>
						<option value="">選択してください</option>
						<option value="男">男</option>
						<option value="女">女</option>
				</select></td>
			</tr>
			<tr>
				<td><label for="nen">年:</label></td>
				<td><select id="nen" name="nen" class="form-input" required>
						<option value="">選択してください</option>
						<option value="">選択してください</option>
						<%
						for (int year = 2001; year <= 2025; year++) {
						%>
						<option value="<%=year%>"><%=year%></option>
						<%
}
%>
				</select></td>
			</tr>
			<tr>
				<td><label for="address">住所:</label></td>
				<td><input type="text" id="address" name="address"
					class="form-input" required></td>
			</tr>
		</table>
		<button type="submit" class="form-button">登録</button>
	</form>
</body>
</html>