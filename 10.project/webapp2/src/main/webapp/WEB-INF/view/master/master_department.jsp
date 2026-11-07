<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>組織図</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
<%@ include file="../_icons.jspf" %>
</head>
<body>
<div class="card">
	<div class="text-center page-title-container">
		<h1>組織図</h1>
		<p>部署の親子関係と、各部署の承認者を設定します。残業事前申請・休暇申請は、申請者の所属部署の承認者から上位部署の承認者へ順に回ります。<br>
		「副承認者」は承認者と同じ段の候補で、どちらか1人が承認すれば次の段へ進みます（不在時の代理など）。<br>
		勤怠承認は、承認者・副承認者が担当する部署とその配下の部署の社員（兼任を含む）の分が表示されます。<br>
		社員や部署ごとに決まったルートを使いたいときは「承認ルート」マスタで設定します（組織図より優先されます）。</p>
	</div>

	<%@ include file="_header.jspf" %>

	<h2 class="table-title">部署</h2>
	<div style="overflow-x: auto;">
	<table class="table">
		<thead>
			<tr>
				<th>部署</th>
				<th>部署名</th>
				<th>上位部署</th>
				<th>承認者</th>
				<th>副承認者<br><span style="font-weight: normal; font-size: 0.75rem;">Ctrl/⌘で複数選択</span></th>
				<th>並び順</th>
				<th>所属人数</th>
				<th class="text-center">操作</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="d" items="${departments}">
				<tr>
					<td style="white-space: nowrap;"><c:out value="${d.indent}" /><c:out value="${d.name}" /></td>
					<td><input type="text" name="name" form="d${d.id}" class="form-input" value="<c:out value='${d.name}'/>" required></td>
					<td>
						<select name="parentId" form="d${d.id}" class="form-input">
							<option value="">（なし：最上位）</option>
							<c:forEach var="p" items="${departments}">
								<c:if test="${p.id != d.id}">
									<option value="${p.id}" <c:if test="${d.parentId == p.id}">selected</c:if>><c:out value="${p.indent}" /><c:out value="${p.name}" /></option>
								</c:if>
							</c:forEach>
						</select>
					</td>
					<td>
						<select name="approverStaffId" form="d${d.id}" class="form-input">
							<option value="">（未設定）</option>
							<c:forEach var="s" items="${staffList}">
								<option value="${s.id}" <c:if test="${d.approverStaffId == s.id}">selected</c:if>><c:out value="${s.id}" />: <c:out value="${s.name}" /></option>
							</c:forEach>
						</select>
					</td>
					<td>
						<select name="subApproverIds" form="d${d.id}" class="form-input" multiple size="3" aria-label="<c:out value='${d.name}'/>の副承認者">
							<c:forEach var="s" items="${staffList}">
								<option value="${s.id}" <c:if test="${subApprovers[d.id] != null and subApprovers[d.id].contains(s.id)}">selected</c:if>><c:out value="${s.id}" />: <c:out value="${s.name}" /></option>
							</c:forEach>
						</select>
					</td>
					<td><input type="number" name="sortOrder" form="d${d.id}" class="form-input" value="${d.sortOrder}" style="width: 5rem;"></td>
					<td class="text-center"><c:out value="${d.memberCount}" /></td>
					<td class="text-center">
						<form id="d${d.id}" action="${pageContext.request.contextPath}/masterDepartment.do" method="post">
							<input type="hidden" name="id" value="${d.id}">
							<button type="submit" class="btn btn-primary">保存</button>
							<button type="submit" name="op" value="delete" formnovalidate class="btn btn-danger" data-confirm="<c:out value='部署「${d.name}」を削除しますか？'/>" onclick="return confirm(this.dataset.confirm)">削除</button>
						</form>
					</td>
				</tr>
			</c:forEach>
			<tr>
				<td>新規</td>
				<td><input type="text" name="name" form="dNew" class="form-input" placeholder="例: 開発1課" required></td>
				<td>
					<select name="parentId" form="dNew" class="form-input">
						<option value="">（なし：最上位）</option>
						<c:forEach var="p" items="${departments}">
							<option value="${p.id}"><c:out value="${p.indent}" /><c:out value="${p.name}" /></option>
						</c:forEach>
					</select>
				</td>
				<td>
					<select name="approverStaffId" form="dNew" class="form-input">
						<option value="">（未設定）</option>
						<c:forEach var="s" items="${staffList}">
							<option value="${s.id}"><c:out value="${s.id}" />: <c:out value="${s.name}" /></option>
						</c:forEach>
					</select>
				</td>
				<td>
					<select name="subApproverIds" form="dNew" class="form-input" multiple size="3" aria-label="新しい部署の副承認者">
						<c:forEach var="s" items="${staffList}">
							<option value="${s.id}"><c:out value="${s.id}" />: <c:out value="${s.name}" /></option>
						</c:forEach>
					</select>
				</td>
				<td><input type="number" name="sortOrder" form="dNew" class="form-input" value="0" style="width: 5rem;"></td>
				<td></td>
				<td class="text-center">
					<form id="dNew" action="${pageContext.request.contextPath}/masterDepartment.do" method="post">
						<button type="submit" class="btn btn-secondary">追加</button>
					</form>
				</td>
			</tr>
		</tbody>
	</table>
	</div>

	<h2 class="table-title">社員の所属</h2>
	<table class="table">
		<thead>
			<tr>
				<th>社員ID</th>
				<th>氏名</th>
				<th>所属部署（主）</th>
				<th>兼任部署<br><span style="font-weight: normal; font-size: 0.75rem;">Ctrl/⌘で複数選択</span></th>
				<th class="text-center">操作</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="s" items="${staffList}">
				<tr>
					<td><c:out value="${s.id}" /></td>
					<td><c:out value="${s.name}" /></td>
					<td>
						<select name="departmentId" form="s${s.id}" class="form-input">
							<option value="">（未所属）</option>
							<c:forEach var="d" items="${departments}">
								<option value="${d.id}" <c:if test="${s.departmentId == d.id}">selected</c:if>><c:out value="${d.indent}" /><c:out value="${d.name}" /></option>
							</c:forEach>
						</select>
					</td>
					<td>
						<select name="concurrentDepartmentIds" form="s${s.id}" class="form-input" multiple size="3" aria-label="<c:out value='${s.name}'/>の兼任部署">
							<c:forEach var="d" items="${departments}">
								<option value="${d.id}" <c:if test="${concurrentDepartments[s.id] != null and concurrentDepartments[s.id].contains(d.id)}">selected</c:if>><c:out value="${d.indent}" /><c:out value="${d.name}" /></option>
							</c:forEach>
						</select>
					</td>
					<td class="text-center">
						<form id="s${s.id}" action="${pageContext.request.contextPath}/masterDepartment.do" method="post">
							<input type="hidden" name="mode" value="assign">
							<input type="hidden" name="staffId" value="${s.id}">
							<button type="submit" class="btn btn-primary">保存</button>
						</form>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>
</body>
</html>
