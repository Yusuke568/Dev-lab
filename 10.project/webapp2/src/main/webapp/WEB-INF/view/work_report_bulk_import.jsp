<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>作業報告書の取込</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
<style>
.bi-steps { margin: 0 0 1rem; padding-left: 1.2rem; font-size: 0.9rem; color: #4a5568; }
.bi-box { border: 1px solid #e2e8f0; border-radius: 10px; padding: 14px 16px; margin-bottom: 1rem; }
.bi-box h3 { margin: 0 0 8px; font-size: 1rem; }
.bi-upload { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; }
.bi-upload input[type="file"] { max-width: 100%; }
.bi-table { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
.bi-table th, .bi-table td { border-bottom: 1px solid #e2e8f0; padding: 8px 10px; text-align: left; vertical-align: top; }
.bi-table th { background: #f7fafc; font-size: 0.8rem; color: #4a5568; white-space: nowrap; }
.bi-table td.bi-file { word-break: break-all; min-width: 12em; }
.bi-table select { width: 100%; min-width: 12em; }
.bi-muted { color: #718096; font-size: 0.8rem; }
.bi-warn { color: #b7791f; font-size: 0.8rem; font-weight: 700; }
.bi-err { color: #c50000; font-weight: 700; }
.bi-actions { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 12px; }
.bi-list { margin: 0; padding-left: 1.2rem; }
.bi-targets { font-size: 0.85rem; color: #4a5568; }
@media (max-width: 768px) {
	.bi-table thead { display: none; }
	.bi-table, .bi-table tbody, .bi-table tr, .bi-table td { display: block; width: 100%; }
	.bi-table tr { border: 1px solid #e2e8f0; border-radius: 8px; margin-bottom: 8px; padding: 4px; }
	.bi-table td { border: 0; padding: 4px 8px; }
	.bi-table td[data-label]::before { content: attr(data-label); display: block; font-size: 0.72rem; color: #718096; font-weight: 700; }
}
</style>
<%@ include file="_icons.jspf" %>
</head>
<body data-context-path="${pageContext.request.contextPath}">
<div class="card">
	<div class="text-center page-title-container">
		<h1>作業報告書の取込（週報担当）</h1>
		<p>所属部署とその下の部署の社員の作業報告書（Excel）をまとめて取り込みます。勤怠・週次作業報告・リーダー所見が、それぞれの社員に入ります。</p>
	</div>

	<div class="page-header-actions">
		<a href="${pageContext.request.contextPath}/menu.do" class="btn btn-secondary">‹ メニューへ戻る</a>
	</div>

	<c:if test="${not empty messages}">
		<div class="status-item status-info" style="margin: 1rem 0;">
			<ul class="bi-list"><c:forEach var="m" items="${messages}"><li><c:out value="${m}" /></li></c:forEach></ul>
		</div>
	</c:if>
	<c:if test="${not empty errors}">
		<div class="error-message" style="margin: 1rem 0;">
			<ul class="bi-list"><c:forEach var="m" items="${errors}"><li><c:out value="${m}" /></li></c:forEach></ul>
		</div>
	</c:if>

	<ol class="bi-steps">
		<li>リーダー所見まで記入した作業報告書を、まとめて選んで「読み込む」を押します（まだ保存しません）。</li>
		<li>ファイルごとに、取り込む社員を確かめます。名前欄・ファイル名から自動で選びます。違っていれば選び直してください。</li>
		<li>「取り込む」を押すと、選んだ社員の勤怠表と週次作業報告に入ります。</li>
	</ol>

	<c:choose>
		<c:when test="${empty targets}">
			<p class="bi-err">取り込める社員がいません。あなたの所属部署が組織図に設定されているか、管理者に確かめてください。</p>
		</c:when>
		<c:otherwise>
			<div class="bi-box">
				<h3>1. 作業報告書を選ぶ</h3>
				<form action="${pageContext.request.contextPath}/workReportBulkImport.do" method="post" enctype="multipart/form-data" class="bi-upload">
					<input type="hidden" name="op" value="upload">
					<input type="file" name="files" accept=".xlsm" multiple required aria-label="作業報告書（複数選べます）">
					<button type="submit" class="btn btn-primary">読み込む</button>
				</form>
				<p class="bi-muted">Excelで開いているファイルは、上書き保存して閉じてから選んでください。1ファイル10MB・一度に50ファイルまで。</p>
			</div>

			<c:if test="${not empty pending}">
				<div class="bi-box">
					<h3>2. 取り込む社員を確かめる</h3>
					<form action="${pageContext.request.contextPath}/workReportBulkImport.do" method="post"
						onsubmit="return confirm('選んだ社員に取り込みます。同じ日・同じ週の勤怠と週次作業報告は、ファイルの内容で上書きされます。よろしいですか？');">
						<input type="hidden" name="op" value="import">
						<div style="overflow-x: auto;">
						<table class="bi-table">
							<thead><tr><th>ファイル</th><th>中身</th><th>名前欄</th><th>取り込む社員</th></tr></thead>
							<tbody>
							<c:forEach var="f" items="${pending}">
								<tr>
									<td class="bi-file" data-label="ファイル"><c:out value="${f.fileName}" /></td>
									<c:choose>
										<c:when test="${not empty f.error}">
											<td colspan="3" class="bi-err" data-label="結果"><c:out value="${f.error}" /></td>
										</c:when>
										<c:otherwise>
											<td data-label="中身">
												<c:out value="${f.monthsLabel}" />
												<div class="bi-muted">勤怠 <c:out value="${f.preview.dayCount}" />日・週次作業報告 <c:out value="${f.preview.weekCount}" />週</div>
											</td>
											<td data-label="名前欄"><c:out value="${empty f.preview.staffName ? '（空欄）' : f.preview.staffName}" /></td>
											<td data-label="取り込む社員">
												<select name="staff_${f.token}" class="form-input" aria-label="<c:out value='${f.fileName}'/> を取り込む社員">
													<option value="">（取り込まない）</option>
													<c:forEach var="t" items="${targets}">
														<option value="${t.staffId}" ${t.staffId == f.suggestedStaffId ? 'selected' : ''}><c:out value="${t.staffId}" /> <c:out value="${t.name}" /><c:if test="${not empty t.departmentName}">（<c:out value="${t.departmentName}" />）</c:if></option>
													</c:forEach>
												</select>
												<c:if test="${empty f.suggestedStaffId}"><div class="bi-warn">社員を自動で決められませんでした。選んでください。</div></c:if>
											</td>
										</c:otherwise>
									</c:choose>
								</tr>
							</c:forEach>
							</tbody>
						</table>
						</div>
						<div class="bi-actions">
							<button type="submit" class="btn btn-success">取り込む</button>
						</div>
					</form>
					<form action="${pageContext.request.contextPath}/workReportBulkImport.do" method="post" style="margin-top: 8px;">
						<input type="hidden" name="op" value="clear">
						<button type="submit" class="btn btn-secondary">読み込んだファイルを捨てる</button>
					</form>
				</div>
			</c:if>

			<details class="bi-targets">
				<summary>取り込める社員（<c:out value="${targets.size()}" />人）</summary>
				<ul class="bi-list">
					<c:forEach var="t" items="${targets}"><li><c:out value="${t.staffId}" /> <c:out value="${t.name}" /><c:if test="${not empty t.departmentName}">（<c:out value="${t.departmentName}" />）</c:if></li></c:forEach>
				</ul>
			</details>
		</c:otherwise>
	</c:choose>
</div>
</body>
</html>
