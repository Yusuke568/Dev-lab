<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="beans.*"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>社員削除画面</title>
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
	<h1>社員削除画面</h1>
	<form action="ShainDeleteComplete" method="post">
		<table class="form-table">
			<tr>
				<td><label for="id">ID:</label></td>
				<td><c:out value="${shainBean.id}"/></td>
			</tr>
			<tr>
				<td><label for="name">名前:</label></td>
				<td><c:out value="${shainBean.name}"/></td>
			</tr>
			<tr>
				<td><label for="namekana">カナ氏名:</label></td>
				<td><c:out value="${shainBean.namekana}"/></td>
			</tr>
			<tr>
				<td><label for="entryyear">入社年:</label></td>
				<td><c:out value="${shainBean.entryyear}"/></td>
			</tr>
			<tr>
				<td><label for="gender">性別:</label></td>
				<td><c:out value="${shainBean.gender}"/></td>
			</tr>
			<tr>
				<td><label for="jobclass">役職:</label></td>
				<td><c:out value="${shainBean.jobclass}"/></td>
			</tr>
		</table>
		<button type="submit" class="form-button">削除</button>
		<button type="button" class="form-button"
			onclick="location.href='ShainList'">キャンセル</button>

		<input type="hidden" name="id" value="<c:out value="${shainBean.id}"/>"> 
		<input type="hidden" name="name" value="<c:out value="${shainBean.name}"/>">
		<input type="hidden" name="namekana" value="<c:out value="${shainBean.namekana}"/>">
		<input type="hidden" name="entryyear" value="<c:out value="${shainBean.entryyear}"/>">
		<input type="hidden" name="gender" value="<c:out value="${shainBean.gender}"/>">
		<input type="hidden" name="jobclass" value="<c:out value="${shainBean.jobclass}"/>">
	</form>
</body>
</html>