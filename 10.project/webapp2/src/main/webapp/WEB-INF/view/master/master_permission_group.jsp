<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>権限グループ</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="card">
	<div class="text-center page-title-container">
		<h1>権限グループ</h1>
		<p>グループごとに使える機能を設定します。役職をグループに置くと、その役職の社員がこの機能を使えるようになります（次回ログインから反映）。</p>
		<p>マスタ管理は、マスタごとに使える・使えないを設定できます。1つでも使えるマスタがあれば、メニューに「マスタ管理」が出ます。</p>
	</div>

	<%@ include file="_header.jspf" %>

	<div style="overflow-x: auto;">
	<table class="table">
		<thead>
			<tr>
				<th rowspan="2">ID</th>
				<th rowspan="2">グループ名</th>
				<c:forEach var="f" items="${features}">
					<c:if test="${not f.master}">
						<th class="text-center" rowspan="2"><c:out value="${f.label}" /></th>
					</c:if>
				</c:forEach>
				<th class="text-center" colspan="${masterFeatureCount}">マスタ管理</th>
				<th class="text-center" rowspan="2">操作</th>
			</tr>
			<tr>
				<c:forEach var="f" items="${features}">
					<c:if test="${f.master}">
						<th class="text-center"><c:out value="${f.shortLabel}" /></th>
					</c:if>
				</c:forEach>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="g" items="${groups}">
				<tr>
					<td><c:out value="${g.id}" /></td>
					<td><input type="text" name="name" form="g${g.id}" class="form-input" value="<c:out value='${g.name}'/>" required></td>
					<c:forEach var="f" items="${features}">
						<td class="text-center">
							<input type="checkbox" name="feature" value="${f.code}" form="g${g.id}"
								<c:if test="${g.featureMap[f.code]}">checked</c:if>>
						</td>
					</c:forEach>
					<td class="text-center">
						<form id="g${g.id}" action="${pageContext.request.contextPath}/masterPermissionGroup.do" method="post">
							<input type="hidden" name="id" value="${g.id}">
							<button type="submit" class="btn btn-primary">保存</button>
						</form>
					</td>
				</tr>
			</c:forEach>
			<tr>
				<td>新規</td>
				<td><input type="text" name="name" form="gNew" class="form-input" placeholder="例: 総務" required></td>
				<c:forEach var="f" items="${features}">
					<td class="text-center"><input type="checkbox" name="feature" value="${f.code}" form="gNew"></td>
				</c:forEach>
				<td class="text-center">
					<form id="gNew" action="${pageContext.request.contextPath}/masterPermissionGroup.do" method="post">
						<button type="submit" class="btn btn-secondary">追加</button>
					</form>
				</td>
			</tr>
		</tbody>
	</table>
	</div>
	<p style="font-size: 0.85rem; color: #666;">※ 「承認」がある社員でも、組織図でどこかの部署の承認者になっていなければ承認待ちは届きません。</p>
	<p style="font-size: 0.85rem; color: #666;">※ 「マスタ管理（権限グループ）」を使えるグループが1つもなくなる変更は保存できません（権限を画面から戻せなくなるため）。</p>
</div>
</body>
</html>
