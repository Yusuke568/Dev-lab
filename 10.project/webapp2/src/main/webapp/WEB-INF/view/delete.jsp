<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="beans.*"%>
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

<%-- shainBeanから社員情報を取得する --%>
<%
ShainBean shainBean = (ShainBean)request.getAttribute("shainBean");
%>

<body>
	<h1>社員削除画面</h1>
	<form action="ShainDeleteComplete" method="post">
		<table class="form-table">
			<tr>
				<td><label for="id">ID:</label></td>
				<td><%=shainBean.getId() %></td>
			</tr>
			<tr>
				<td><label for="name">名前:</label></td>
				<td><%=shainBean.getName() %></td>
			</tr>
			<tr>
				<td><label for="sei">姓:</label></td>
				<td><%=shainBean.getSei() %></td>
			</tr>
			<tr>
				<td><label for="nen">年:</label></td>
				<td><%=shainBean.getNen() %>
			</tr>
			<tr>
				<td><label for="address">住所:</label></td>
				<td><%=shainBean.getAddress() %>
			</tr>
		</table>
		<button type="submit" class="form-button">削除</button>


		<input type="hidden" name="id" value="<%=shainBean.getId()%>">
		<input type="hidden" name="name" value="<%=shainBean.getName()%>">
		<input type="hidden" name="sei" value="<%=shainBean.getSei()%>">
		<input type="hidden" name="nen" value="<%=shainBean.getNen()%>">
		<input type="hidden" name="address" value="<%=shainBean.getAddress()%>">
	</form>
</body>
</html>