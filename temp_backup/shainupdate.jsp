<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="beans.*"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>社員更新画面</title>
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
	<h1>社員更新画面</h1>
	<form action="ShainUpdateComplete" method="post">
		<table class="form-table">
			<tr>
				<td><label for="id">ID:</label></td>
				<td>${shainBean.id}</td>
			</tr>
			<tr>
				<td><label for="name">名前:</label></td>
				<td><input type="text" id="name" name="name"
					value="${shainBean.name}" class="form-input" required></td>
			</tr>
			<tr>
				<td><label for="namekana">カナ氏名:</label></td>
				<td><input type="text" id="namekana" name="namekana"
					value="${shainBean.namekana}" class="form-input" required></td>
			</tr>
			<tr>
				<td><label for="entryyear">入社年:</label></td>
				<td><input type="number" id="entryyear" name="entryyear"
					value="${shainBean.entryyear}" class="form-input" required></td>
			</tr>
			<tr>
				<td><label for="gender">性別:</label></td>
				<td><select id="gender" name="gender" class="form-input"
					required>
						<option value="男" ${shainBean.gender == '男' ? 'selected' : ''}>男</option>
						<option value="女" ${shainBean.gender == '女' ? 'selected' : ''}>女</option>
				</select></td>
			</tr>
			<tr>
				<td><label for="jobclass">役職:</label></td>
				<td><select id="jobclass" name="jobclass" class="form-input"
					required>
						<c:forEach var="job" items="${jobclassList}">
							<option value="${job}"
								${shainBean.jobclass == job ? 'selected' : ''}>${job}</option>
						</c:forEach>
				</select></td>
			</tr>
		</table>
		<button type="submit" class="form-button">更新</button>
		<button type="button" class="form-button"
			onclick="location.href='ShainList'">キャンセル</button>


		<input type="hidden" name="id" value="${shainBean.id}"> <input
			type="hidden" name="name" value="${shainBean.name}"> <input
			type="hidden" name="namekana" value="${shainBean.namekana}">
		<input type="hidden" name="entryyear" value="${shainBean.entryyear}">
		<input type="hidden" name="gender" value="${shainBean.gender}">
		<input type="hidden" name="jobclass" value="${shainBean.jobclass}">
	</form>
</body>
</html>