<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>役職</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
<%@ include file="../_icons.jspf" %>
</head>
<body>
<div class="card">
	<div class="text-center page-title-container">
		<h1>役職</h1>
		<p>役職ごとに権限グループを1つ割り当てます。社員の権限は、その社員の役職で決まります。</p>
	</div>

	<%@ include file="_header.jspf" %>

	<table class="table">
		<thead>
			<tr>
				<th>ID</th>
				<th>役職名</th>
				<th>権限グループ</th>
				<th class="text-center">操作</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="jc" items="${jobClasses}">
				<tr>
					<td><c:out value="${jc.id}" /></td>
					<td><input type="text" name="name" form="j${jc.id}" class="form-input" value="<c:out value='${jc.name}'/>" required></td>
					<td>
						<select name="permissionGroupId" form="j${jc.id}" class="form-input">
							<option value="">（未設定：一般と同じ）</option>
							<c:forEach var="g" items="${groups}">
								<option value="${g.id}" <c:if test="${jc.permissionGroupId == g.id}">selected</c:if>><c:out value="${g.name}" /></option>
							</c:forEach>
						</select>
					</td>
					<td class="text-center">
						<form id="j${jc.id}" action="${pageContext.request.contextPath}/masterJobClass.do" method="post">
							<input type="hidden" name="id" value="${jc.id}">
							<button type="submit" class="btn btn-primary">保存</button>
							<button type="submit" name="op" value="delete" formnovalidate class="btn btn-danger" data-confirm="<c:out value='役職「${jc.name}」を削除しますか？'/>" onclick="return confirm(this.dataset.confirm)">削除</button>
						</form>
					</td>
				</tr>
			</c:forEach>
			<tr>
				<td>新規</td>
				<td><input type="text" name="name" form="jNew" class="form-input" placeholder="例: 課長" required></td>
				<td>
					<select name="permissionGroupId" form="jNew" class="form-input">
						<option value="">（未設定：一般と同じ）</option>
						<c:forEach var="g" items="${groups}">
							<option value="${g.id}"><c:out value="${g.name}" /></option>
						</c:forEach>
					</select>
				</td>
				<td class="text-center">
					<form id="jNew" action="${pageContext.request.contextPath}/masterJobClass.do" method="post">
						<button type="submit" class="btn btn-secondary">追加</button>
					</form>
				</td>
			</tr>
		</tbody>
	</table>
</div>
</body>
</html>
