<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>時間外申請書の承認日の取込</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
<style>
.fi-steps { margin: 0 0 1rem; padding-left: 1.2rem; font-size: 0.9rem; color: #4a5568; }
.fi-box { border: 1px solid #e2e8f0; border-radius: 10px; padding: 14px 16px; margin-bottom: 1rem; }
.fi-box h3 { margin: 0 0 8px; font-size: 1rem; }
.fi-upload { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; }
.fi-upload input[type="file"] { max-width: 100%; }
.fi-file { border-top: 1px solid #e2e8f0; padding: 12px 0; }
.fi-file:first-of-type { border-top: 0; }
.fi-file-head { display: flex; flex-wrap: wrap; gap: 8px 16px; align-items: center; margin-bottom: 8px; }
.fi-file-name { font-weight: 700; word-break: break-all; }
.fi-file-head select { min-width: 14em; }
.fi-table { width: 100%; border-collapse: collapse; font-size: 0.85rem; }
.fi-table th, .fi-table td { border-bottom: 1px solid #edf2f7; padding: 6px 8px; text-align: left; vertical-align: top; }
.fi-table th { background: #f7fafc; font-size: 0.78rem; color: #4a5568; white-space: nowrap; }
.fi-table tr.warn td { background: #fffaf0; }
.fi-table tr.none td { color: #a0aec0; }
.fi-marks { white-space: nowrap; font-variant-numeric: tabular-nums; }
.fi-ok { color: #22543d; }
.fi-warn { color: #b7791f; font-weight: 700; }
.fi-err { color: #c50000; font-weight: 700; }
.fi-muted { color: #718096; font-size: 0.8rem; }
.fi-list { margin: 0; padding-left: 1.2rem; }
.fi-actions { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 12px; }
@media (max-width: 768px) {
	.fi-table thead { display: none; }
	.fi-table, .fi-table tbody, .fi-table tr, .fi-table td { display: block; width: 100%; }
	.fi-table tr { border: 1px solid #e2e8f0; border-radius: 8px; margin-bottom: 8px; padding: 4px; }
	.fi-table td { border: 0; padding: 3px 8px; }
	.fi-table td[data-label]::before { content: attr(data-label); display: block; font-size: 0.72rem; color: #718096; font-weight: 700; }
}
</style>
<%@ include file="_icons.jspf" %>
</head>
<body>
<div class="card">
	<div class="text-center page-title-container">
		<h1>時間外申請書の承認日の取込</h1>
		<p>Excel の時間外申請書（個人別時間外勤務申請書）に書かれた承認日を、勤怠システムの残業事前申請と残業の実績確認に取り込みます。</p>
	</div>

	<div class="page-header-actions">
		<a href="${pageContext.request.contextPath}/approvalList.do" class="btn btn-secondary">‹ 勤怠承認へ戻る</a>
	</div>

	<c:if test="${not empty messages}">
		<div class="status-item status-info" style="margin: 1rem 0;">
			<ul class="fi-list"><c:forEach var="m" items="${messages}"><li><c:out value="${m}" /></li></c:forEach></ul>
		</div>
	</c:if>
	<c:if test="${not empty errors}">
		<div class="error-message" style="margin: 1rem 0;">
			<ul class="fi-list"><c:forEach var="m" items="${errors}"><li><c:out value="${m}" /></li></c:forEach></ul>
		</div>
	</c:if>

	<ol class="fi-steps">
		<li>承認日を記入した時間外申請書（.xlsx）を、まとめて選んで「読み込む」を押します（まだ保存しません）。</li>
		<li>ファイルごとに社員と、取り込むと何が変わるかを確かめます。社員は氏名欄・ファイル名から自動で選びます。</li>
		<li>「取り込む」を押すと、承認日がシステムに入ります。承認者はその段の主承認者として、理由に「時間外申請書から取込」と残ります。</li>
	</ol>
	<p class="fi-muted">
		申請の承認欄（課長／所属長／社長）は、システムの承認ルートの 1段目／2段目／3段目以降 に当てはめます。実績報告の承認欄も同じです。
		すでにシステムで承認・確認した段も、時間外申請書の日付と違えばその日付に直します（承認した人はそのまま）。却下・取り下げの申請、システムにない申請は取り込みません。
	</p>

	<c:choose>
		<c:when test="${empty targets}">
			<p class="fi-err">取り込める社員がいません。組織図で、あなたが承認者の部署に社員が所属しているか確かめてください。</p>
		</c:when>
		<c:otherwise>
			<div class="fi-box">
				<h3>1. 時間外申請書を選ぶ</h3>
				<form action="${pageContext.request.contextPath}/overtimeFormImport.do" method="post" enctype="multipart/form-data" class="fi-upload">
					<input type="hidden" name="_csrf" value="<c:out value='${sessionScope.csrfToken}' />">
					<input type="hidden" name="op" value="upload">
					<input type="file" name="files" accept=".xlsx" multiple required aria-label="時間外申請書（複数選べます）">
					<button type="submit" class="btn btn-primary">読み込む</button>
				</form>
				<p class="fi-muted">Excel で開いているファイルは、上書き保存して閉じてから選んでください。一度に50ファイルまで。</p>
			</div>

			<c:if test="${not empty pending}">
				<div class="fi-box">
					<h3>2. 取り込む内容を確かめる</h3>
					<%-- 社員を選び直したら、その社員で照らし合わせ直す（op=staff） --%>
					<form id="fi-form" action="${pageContext.request.contextPath}/overtimeFormImport.do" method="post">
						<input type="hidden" name="_csrf" value="<c:out value='${sessionScope.csrfToken}' />">
						<input type="hidden" name="op" value="import" id="fi-op">
						<input type="hidden" name="token" value="" id="fi-token">
						<c:forEach var="f" items="${pending}">
							<div class="fi-file">
								<div class="fi-file-head">
									<span class="fi-file-name"><c:out value="${f.fileName}" /></span>
									<c:choose>
										<c:when test="${not empty f.error}"><span class="fi-err"><c:out value="${f.error}" /></span></c:when>
										<c:otherwise>
											<span class="fi-muted">氏名欄：<c:out value="${empty f.staffName ? '（空欄）' : f.staffName}" /></span>
											<label>社員
												<select name="staff_${f.token}" class="form-input fi-staff" data-token="${f.token}"
													aria-label="<c:out value='${f.fileName}'/> を取り込む社員">
													<option value="">（取り込まない）</option>
													<c:forEach var="t" items="${targets}">
														<option value="${t.staffId}" ${t.staffId == f.staffId ? 'selected' : ''}><c:out value="${t.staffId}" /> <c:out value="${t.name}" /></option>
													</c:forEach>
												</select>
											</label>
											<c:if test="${empty f.staffId}"><span class="fi-warn">社員を自動で決められませんでした。選んでください。</span></c:if>
											<c:if test="${not empty f.staffId}"><span class="fi-muted">取り込む日 <b><c:out value="${f.importCount}" /></b>日<c:if test="${f.warnCount > 0}"> ／ <span class="fi-warn">確かめてほしい日 <c:out value="${f.warnCount}" />日</span></c:if></span></c:if>
										</c:otherwise>
									</c:choose>
								</div>
								<c:if test="${empty f.error and not empty f.staffId}">
									<c:choose>
										<c:when test="${empty f.preview}"><p class="fi-muted">承認欄に日付の入った日がありません。</p></c:when>
										<c:otherwise>
											<div style="overflow-x: auto;">
											<table class="fi-table">
												<thead><tr><th>対象日</th><th>申請の承認欄<br>課長 / 所属長 / 社長</th><th>実績報告の承認欄<br>課長 / 所属長 / 社長</th><th>システムの申請</th><th>取り込むと</th></tr></thead>
												<tbody>
												<c:forEach var="r" items="${f.preview}">
													<tr class="${r.state}">
														<td data-label="対象日"><c:out value="${r.dateLabel}" /></td>
														<td data-label="申請の承認欄" class="fi-marks"><c:out value="${r.plannedLabel}" /></td>
														<td data-label="実績報告の承認欄" class="fi-marks"><c:out value="${r.actualLabel}" /></td>
														<td data-label="システムの申請"><c:out value="${r.systemStatus}" /></td>
														<td data-label="取り込むと">
															<c:if test="${not empty r.actions}"><div class="fi-ok"><c:out value="${r.actionsText}" /></div></c:if>
															<c:if test="${empty r.actions and empty r.notes}">変わりません（システムと同じ日付）</c:if>
															<c:if test="${not empty r.notes}"><div class="fi-warn">⚠ <c:out value="${r.notesText}" /></div></c:if>
														</td>
													</tr>
												</c:forEach>
												</tbody>
											</table>
											</div>
										</c:otherwise>
									</c:choose>
								</c:if>
							</div>
						</c:forEach>
						<div class="fi-actions">
							<button type="submit" class="btn btn-success"
								onclick="document.getElementById('fi-op').value='import'; return confirm('表の「取り込むと」の内容で、承認日をシステムに入れます。よろしいですか？');">取り込む</button>
						</div>
					</form>
					<form action="${pageContext.request.contextPath}/overtimeFormImport.do" method="post" style="margin-top: 8px;">
						<input type="hidden" name="_csrf" value="<c:out value='${sessionScope.csrfToken}' />">
						<input type="hidden" name="op" value="clear">
						<button type="submit" class="btn btn-secondary">読み込んだファイルを捨てる</button>
					</form>
				</div>
			</c:if>
		</c:otherwise>
	</c:choose>
</div>
<script>
// 社員を選び直したら、その社員で照らし合わせ直す
document.querySelectorAll(".fi-staff").forEach((sel) => sel.addEventListener("change", () => {
	document.getElementById("fi-op").value = "staff";
	document.getElementById("fi-token").value = sel.dataset.token;
	document.getElementById("fi-form").submit();
}));
</script>
</body>
</html>
