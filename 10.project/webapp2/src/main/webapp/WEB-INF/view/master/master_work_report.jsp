<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>作業報告書の設定</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
<%@ include file="../_icons.jspf" %>
<style>
.wr-form { display: flex; flex-direction: column; gap: 0.75rem; max-width: 640px; }
.wr-form .form-input { width: 100%; box-sizing: border-box; }
.wr-preview { padding: 10px 12px; border: 1px solid #e2e8f0; border-radius: 8px; background: #f8fafc; word-break: break-all; }
.wr-preview b { font-family: Consolas, "Courier New", monospace; }
.wr-keys { border-collapse: collapse; font-size: 0.9rem; }
.wr-keys th, .wr-keys td { border-bottom: 1px solid #e2e8f0; padding: 6px 10px; text-align: left; }
.wr-keys code { font-weight: 700; }
.wr-keys button { font-size: 0.8rem; padding: 2px 8px; }
.wr-note { font-size: 0.85rem; color: #4a5568; }
</style>
</head>
<body>
<div class="card">
	<div class="text-center page-title-container">
		<h1>作業報告書の設定</h1>
		<p>作業報告書（Excel）のファイル名の形を決めます。勤怠画面からのダウンロード、部下の勤怠表からのまとめ出し（ZIP）、サーバーの保存フォルダのファイル名に使います。</p>
	</div>

	<%@ include file="_header.jspf" %>

	<form class="wr-form" action="${pageContext.request.contextPath}/masterWorkReport.do" method="post">
		<input type="hidden" name="_csrf" value="<c:out value='${sessionScope.csrfToken}' />">
		<div class="form-group">
			<label for="fileNamePattern" class="form-label">ファイル名の形（拡張子 .xlsm は自動で付きます）</label>
			<input type="text" id="fileNamePattern" name="fileNamePattern" class="form-input" maxlength="${maxLength}" required
				value="<c:out value='${fileNamePattern}' />" placeholder="例：作業報告書{年}（{氏名}）_41">
		</div>
		<div class="wr-preview" aria-live="polite">
			<span class="wr-note">見本（<c:out value="${sampleName}" />さん・社員ID <c:out value="${sampleId}" /> の今月分）：</span><br>
			<b id="wr-sample"><c:out value="${fileNameSample}" /></b>
			<c:if test="${fileNameIsDefault}"><br><span class="wr-note">※ まだ決めていないので、今までと同じ名前で出しています。</span></c:if>
		</div>

		<table class="wr-keys">
			<thead><tr><th>差し込み</th><th>入る値</th><th></th></tr></thead>
			<tbody>
				<tr><td><code>{年}</code></td><td>年（例：2026）</td><td><button type="button" class="btn btn-secondary" data-key="{年}">入れる</button></td></tr>
				<tr><td><code>{月}</code></td><td>2けたの月（例：11）</td><td><button type="button" class="btn btn-secondary" data-key="{月}">入れる</button></td></tr>
				<tr><td><code>{氏名}</code></td><td>社員の氏名（空白は詰めます）</td><td><button type="button" class="btn btn-secondary" data-key="{氏名}">入れる</button></td></tr>
				<tr><td><code>{社員ID}</code></td><td>社員ID</td><td><button type="button" class="btn btn-secondary" data-key="{社員ID}">入れる</button></td></tr>
			</tbody>
		</table>
		<p class="wr-note">
			それ以外の文字（様式のバージョンなど）は、そのままファイル名になります。ファイル名に使えない文字（\ / : * ? " &lt; &gt; |）は入れられません。<br>
			サーバーの保存フォルダでは「年/月」のフォルダの中に置きます。同じ年・月で同じファイル名になる人（同姓同名など）がいると上書きされるので、
			気になるときは <code>{社員ID}</code> も入れてください。
		</p>
		<div><button type="submit" class="btn btn-primary">保存</button></div>
	</form>
</div>
<script>
(function () {
	const input = document.getElementById("fileNamePattern");
	const sample = document.getElementById("wr-sample");
	const now = new Date();
	const values = { "{年}": String(now.getFullYear()), "{月}": String(now.getMonth() + 1).padStart(2, "0"),
		"{氏名}": "<c:out value='${sampleName}' />", "{社員ID}": "<c:out value='${sampleId}' />" };
	const render = () => {
		let name = input.value.trim().replace(/\{(年|月|氏名|社員ID)\}/g, (k) => values[k]).replace(/[\\/:*?"<>|]/g, "");
		sample.textContent = (name || "（空）") + ".xlsm";
	};
	input.addEventListener("input", render);
	document.querySelectorAll("button[data-key]").forEach((btn) => btn.addEventListener("click", () => {
		const start = input.selectionStart != null ? input.selectionStart : input.value.length;
		const end = input.selectionEnd != null ? input.selectionEnd : input.value.length;
		input.value = input.value.slice(0, start) + btn.dataset.key + input.value.slice(end);
		input.focus();
		input.setSelectionRange(start + btn.dataset.key.length, start + btn.dataset.key.length);
		render();
	}));
})();
</script>
</body>
</html>
