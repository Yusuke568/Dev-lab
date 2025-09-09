<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
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
				<td><label for="id">社員ID:</label></td>
				<td><input type="text" name="id" value="${nextId}"></td>
			</tr>
			<tr>
				<td><label for="name">名前:</label></td>
				<td><input type="text" id="name" name="name" class="form-input"
					required></td>
			</tr>
			<tr>
				<td><label for="namekana">カナ氏名:</label></td>
				<td><input type="text" id="namekana" name="namekana"
					class="form-input" required></td>
			</tr>
			<tr>
				<td><label for="entryyear">入社年:</label></td>
				<td><select id="entryyear" name="entryyear" class="form-input" required>
						<option value="">選択してください</option>
						<c:forEach var="entryyear" items="${yearList}">
							<option value="${entryyear}">${entryyear}</option>
						</c:forEach>
				</select></td>
			</tr>
			<tr>
				<td><label for="gender">性別:</label></td>
				<td><select id="gender" name="gender" class="form-input" required>
						<option value="">選択してください</option>
						<option value="男">男</option>
						<option value="女">女</option>
				</select></td>
			</tr>
			<tr>
				<td><label for="jobclass">役職:</label></td>
				<td><select id="jobclass" name="jobclass" class="form-input" required>
						<option value="">選択してください</option>
						<c:forEach var="jobclass" items="${jobList}">
							<option value="${jobclass.name}">${jobclass.name}</option>
						</c:forEach>
				</select></td>
			</tr>

		</table>
		<button type="submit" class="form-button">登録</button>
	</form>
</body>
</html>