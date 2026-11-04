<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>社員更新画面</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
<style>
.id-check { font-size: 0.85rem; margin-top: 4px; min-height: 1.2em; }
.id-check.ng { color: #c50000; font-weight: 700; }
.id-check.ok { color: #1a7f1a; }
.id-note { font-size: 0.8rem; color: #718096; margin-top: 2px; }
.form-input.invalid { border-color: #c50000; background: #fff5f5; }
</style>
</head>
<body>
	<%-- 入力エラーで戻ってきたときは、入力した値を表示する --%>
	<c:set var="back" value="${not empty errors}" />
	<div class="card form-card">
		<div class="text-center page-title-container">
			<h1>社員情報の更新</h1>
			<p>
				<c:out value="${shain.name}" />
				さんの情報を編集します。
			</p>
		</div>
		<c:if test="${back}">
			<div class="status-banner status-danger">
				<ul class="error-list">
					<c:forEach var="error" items="${errors}">
						<li><c:out value="${error}" /></li>
					</c:forEach>
				</ul>
			</div>
		</c:if>
		<form id="shain-update-form" action="${pageContext.request.contextPath}/shainUpdateExecute.do" method="post">
			<input type="hidden" name="_csrf" value="<c:out value='${sessionScope.csrfToken}' />">
			<input type="hidden" name="id" id="current-id"
				value="<c:out value="${shain.id}"/>">

			<div class="form-group">
				<label for="newId" class="form-label">社員ID</label>
				<input type="text" id="newId" name="newId" class="form-input"
					inputmode="numeric" pattern="[1-9][0-9]{0,8}" maxlength="9" required autocomplete="off"
					aria-describedby="newId-check newId-note"
					value="<c:out value="${back ? param.newId : shain.id}"/>">
				<div class="id-check" id="newId-check" role="status" aria-live="polite"></div>
				<div class="id-note" id="newId-note">
					変更すると、ログインのユーザー名・勤怠・申請・承認・お知らせなど、この社員のデータがすべて新しいIDに切り替わります（現在のID：<c:out value="${shain.id}"/>）。
				</div>
			</div>

			<div class="form-group">
				<label for="name" class="form-label">名前</label> <input type="text"
					id="name" name="name" value="<c:out value="${back ? param.name : shain.name}"/>"
					class="form-input" required>
			</div>

			<div class="form-group">
				<label for="namekana" class="form-label">カナ氏名</label> <input type="text"
					id="namekana" name="namekana" value="<c:out value="${back ? param.namekana : shain.namekana}"/>"
					class="form-input" required>
			</div>

			<div class="form-group">
				<label for="entryyear" class="form-label">入社年</label> <input
					type="number" id="entryyear" name="entryyear"
					value="<c:out value="${back ? param.entryyear : shain.entryyear}"/>" class="form-input"
					required>
			</div>

			<c:set var="genderValue" value="${back ? param.gender : shain.gender}" />
			<div class="form-group">
				<label for="gender" class="form-label">性別</label> <select id="gender"
					name="gender" class="form-input" required>
					<option value="男"
						<c:if test="${genderValue == '男'}">selected</c:if>>男</option>
					<option value="女"
						<c:if test="${genderValue == '女'}">selected</c:if>>女</option>
				</select>
			</div>

			<c:set var="jobValue" value="${back ? param.jobclass : shain.jobclass}" />
			<div class="form-group">
				<label for="jobclass" class="form-label">役職</label> <select
					id="jobclass" name="jobclass" class="form-input" required>
					<c:forEach var="job" items="${jobList}">
						<option value="<c:out value="${job.name}"/>"
							<c:if test="${jobValue == job.name}">selected</c:if>>
							<c:out value="${job.name}" />
						</option>
					</c:forEach>
				</select>
			</div>

			<c:set var="departmentValue" value="${back ? param.departmentId : currentDepartmentId}" />
			<div class="form-group">
				<label for="departmentId" class="form-label">所属部署</label> <select
					id="departmentId" name="departmentId" class="form-input">
					<option value="">（未所属）</option>
					<c:forEach var="d" items="${departments}">
						<option value="${d.id}" <c:if test="${departmentValue == d.id}">selected</c:if>><c:out value="${d.indent}" /><c:out value="${d.name}" /></option>
					</c:forEach>
				</select>
			</div>

			<div class="form-actions">
				<a href="${pageContext.request.contextPath}/shainList.do" class="btn btn-secondary">‹ キャンセル</a>
				<button type="submit" id="submit-btn" class="btn btn-primary">✓ 更新</button>
			</div>
		</form>
	</div>
<script>
/*
 * 社員IDの重複チェック。入力が止まったらサーバーに問い合わせ、使えないIDなら更新ボタンを押せなくする。
 * 最終的なチェックはサーバー側の更新処理でも行う。
 */
(function () {
    const input = document.getElementById('newId');
    const currentId = document.getElementById('current-id').value;
    const message = document.getElementById('newId-check');
    const submit = document.getElementById('submit-btn');
    const form = document.getElementById('shain-update-form');
    const checkUrl = '${pageContext.request.contextPath}/shainIdCheckApi.do';
    let timer = null;
    let latest = 0;
    let checkedValue = null;
    let checkedOk = true;

    function show(ok, text) {
        message.textContent = text || '';
        message.className = 'id-check' + (text ? (ok ? ' ok' : ' ng') : '');
        input.classList.toggle('invalid', !ok);
        input.setAttribute('aria-invalid', ok ? 'false' : 'true');
        submit.disabled = !ok;
    }

    async function check() {
        const value = input.value.trim();
        if (value === currentId) {
            checkedValue = value; checkedOk = true;
            show(true, '');
            return true;
        }
        if (!/^[1-9][0-9]{0,8}$/.test(value)) {
            checkedValue = value; checkedOk = false;
            show(false, value === '' ? '社員IDを入力してください。' : '社員IDは1〜9桁の数字で入力してください（先頭に0は付けられません）。');
            return false;
        }
        const ticket = ++latest;
        submit.disabled = true;
        message.className = 'id-check';
        message.textContent = '確認中…';
        try {
            const params = new URLSearchParams({ id: currentId, newId: value });
            const res = await fetch(checkUrl + '?' + params, { headers: { 'Accept': 'application/json' } });
            if (!res.ok) throw new Error(res.status);
            const data = await res.json();
            if (ticket !== latest) return checkedOk; // 古い問い合わせの結果は使わない
            checkedValue = value; checkedOk = data.ok;
            show(data.ok, data.ok ? '✓ この社員IDは使えます。' : data.message);
            return data.ok;
        } catch (e) {
            // 確認できなくても、更新時にサーバーでチェックするので押せるようにしておく
            if (ticket === latest) { checkedValue = null; show(true, ''); }
            return true;
        }
    }

    input.addEventListener('input', function () {
        clearTimeout(timer);
        timer = setTimeout(check, 300);
    });
    function confirmChange() {
        const value = input.value.trim();
        return value === currentId
            || confirm('社員IDを ' + currentId + ' から ' + value + ' に変更します。\nログインのユーザー名も ' + value + ' に変わります。よろしいですか？');
    }

    form.addEventListener('submit', async function (e) {
        e.preventDefault();
        clearTimeout(timer);
        const ok = checkedValue === input.value.trim() ? checkedOk : await check();
        if (ok && confirmChange()) form.submit();
    });
    if (input.value.trim() !== currentId) check();
})();
</script>
</body>
</html>
