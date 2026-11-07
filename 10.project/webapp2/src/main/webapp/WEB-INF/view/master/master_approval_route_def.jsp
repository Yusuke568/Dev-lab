<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>承認ルート</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
<style>
.route-levels { margin: 0; padding-left: 1.2rem; }
.route-levels li { margin: 2px 0; }
.route-form .form-row { display: flex; flex-wrap: wrap; gap: 0.75rem 1rem; }
.route-form .form-row .form-group { flex: 1 1 240px; }
.level-block { border: 1px solid #e2e8f0; border-radius: 8px; padding: 10px 12px; margin-bottom: 10px; }
.level-block h4 { margin: 0 0 6px; font-size: 0.95rem; }
.level-block select { width: 100%; }
.hint { font-size: 0.8rem; color: #718096; }
.preview-result { margin-top: 10px; padding: 10px 12px; border-radius: 8px; background: #ebf4ff; }
.preview-result.ng { background: #fff5f5; color: #742a2a; }
.assign-list { margin: 0; padding-left: 1.2rem; font-size: 0.85rem; }
</style>
<%@ include file="../_icons.jspf" %>
</head>
<body>
<div class="card">
	<div class="text-center page-title-container">
		<h1>承認ルート</h1>
		<p>段ごとに承認者を並べた承認ルートを作り、社員や部署に割り当てます。1つの段に複数の承認者を選ぶと、誰か1人が承認すれば次の段へ進みます。<br>
		ルートは「社員への割り当て」→「部署への割り当て（なければ上位部署の割り当て）」→「組織図」の順に決まります。兼任している社員は、申請するときに選んだ部署で決まります。<br>
		申請者本人は承認者から除き、ほかに承認者がいなければ自動で承認されます。</p>
	</div>

	<%@ include file="_header.jspf" %>

	<h2 class="table-title">登録済みのルート</h2>
	<c:choose>
		<c:when test="${empty routes}">
			<p>まだありません。割り当てのない社員・部署は、組織図と「承認ルート設定」の段数で承認ルートが決まります。</p>
		</c:when>
		<c:otherwise>
			<div style="overflow-x: auto;">
			<table class="table">
				<thead>
					<tr>
						<th>ルート名</th>
						<th>申請種別</th>
						<th>承認者（段ごと）</th>
						<th>割り当て先</th>
						<th class="text-center">操作</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="r" items="${routes}">
						<tr>
							<td><strong><c:out value="${r.name}" /></strong><c:if test="${not empty r.note}"><br><span class="hint"><c:out value="${r.note}" /></span></c:if></td>
							<td><c:out value="${r.requestTypeLabel}" /></td>
							<td>
								<ol class="route-levels" style="list-style: none; padding-left: 0;">
									<c:forEach var="label" items="${r.levelLabels}"><li><c:out value="${label}" /></li></c:forEach>
								</ol>
							</td>
							<td>
								<c:choose>
									<c:when test="${empty r.assignments}"><span class="hint">（未割り当て）</span></c:when>
									<c:otherwise>
										<ul class="assign-list">
											<c:forEach var="a" items="${r.assignments}"><li><c:out value="${a.label}" /></li></c:forEach>
										</ul>
									</c:otherwise>
								</c:choose>
							</td>
							<td class="text-center" style="white-space: nowrap;">
								<a class="btn btn-secondary" href="${pageContext.request.contextPath}/masterApprovalRouteDef.do?edit=${r.id}#route-form">編集</a>
								<form action="${pageContext.request.contextPath}/masterApprovalRouteDef.do" method="post" style="display: inline;"
									onsubmit="return confirm('承認ルート「<c:out value='${r.name}' />」を削除しますか？\n割り当てていた社員・部署は、組織図で承認ルートが決まるようになります。');">
									<input type="hidden" name="mode" value="delete">
									<input type="hidden" name="id" value="${r.id}">
									<button type="submit" class="btn btn-danger">削除</button>
								</form>
							</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
			</div>
		</c:otherwise>
	</c:choose>

	<div class="card route-form" id="route-form">
		<h2><c:choose><c:when test="${not empty editing}">ルートを編集：<c:out value="${editing.name}" /></c:when><c:otherwise>新しいルート</c:otherwise></c:choose></h2>
		<form action="${pageContext.request.contextPath}/masterApprovalRouteDef.do" method="post" id="route-edit-form">
			<c:if test="${not empty editing}"><input type="hidden" name="id" value="${editing.id}"></c:if>
			<div class="form-row">
				<div class="form-group">
					<label class="form-label" for="route-name">ルート名</label>
					<input type="text" id="route-name" name="name" class="form-input" maxlength="100" required
						placeholder="例：開発部ルート、派遣先A 責任者ルート" value="<c:out value='${editing.name}' />">
				</div>
				<div class="form-group">
					<label class="form-label" for="route-type">申請種別</label>
					<select id="route-type" name="requestType" class="form-input">
						<option value="">残業・休暇の両方</option>
						<c:forEach var="t" items="${requestTypes}">
							<option value="${t.code}" <c:if test="${editing.requestTypeCode == t.code}">selected</c:if>><c:out value="${t.label}" /></option>
						</c:forEach>
					</select>
				</div>
				<div class="form-group">
					<label class="form-label" for="route-note">メモ（任意）</label>
					<input type="text" id="route-note" name="note" class="form-input" maxlength="255" value="<c:out value='${editing.note}' />">
				</div>
			</div>

			<h3 style="margin-top: 0.5rem;">承認者（段ごと）</h3>
			<p class="hint">1段目から順に承認します。同じ段に複数選ぶと、誰か1人の承認でその段は終わります（Ctrl/⌘ を押しながら選ぶと複数選べます）。</p>
			<div id="levels">
				<c:choose>
					<c:when test="${not empty editing}">
						<c:forEach var="set" items="${editing.levelSets}" varStatus="st">
							<div class="level-block">
								<h4>${st.index + 1}段目</h4>
								<select name="level${st.index + 1}" class="form-input level-select" multiple size="5" aria-label="${st.index + 1}段目の承認者">
									<c:forEach var="s" items="${staffList}">
										<option value="${s.id}" <c:if test="${set.contains(s.id)}">selected</c:if>><c:out value="${s.id}" />: <c:out value="${s.name}" /><c:if test="${not empty s.departmentName}">（<c:out value="${s.departmentName}" />）</c:if></option>
									</c:forEach>
								</select>
							</div>
						</c:forEach>
					</c:when>
					<c:otherwise>
						<div class="level-block">
							<h4>1段目</h4>
							<select name="level1" class="form-input level-select" multiple size="5" aria-label="1段目の承認者">
								<c:forEach var="s" items="${staffList}">
									<option value="${s.id}"><c:out value="${s.id}" />: <c:out value="${s.name}" /><c:if test="${not empty s.departmentName}">（<c:out value="${s.departmentName}" />）</c:if></option>
								</c:forEach>
							</select>
						</div>
					</c:otherwise>
				</c:choose>
			</div>
			<input type="hidden" name="levelCount" id="level-count" value="1">
			<div style="display: flex; gap: 8px; margin-bottom: 1rem;">
				<button type="button" class="btn btn-secondary" onclick="addLevel()">＋ 段を追加</button>
				<button type="button" class="btn btn-secondary" onclick="removeLevel()">－ 最後の段を削除</button>
			</div>

			<h3>割り当て先</h3>
			<p class="hint">社員への割り当ては部署への割り当てより優先されます。部署に割り当てると、その下の部署（別のルートを割り当てていない部署）にも使われます。</p>
			<div class="form-row">
				<div class="form-group">
					<label class="form-label" for="assign-dept">部署</label>
					<select id="assign-dept" name="assignDepartmentIds" class="form-input" multiple size="6">
						<c:forEach var="d" items="${departments}">
							<option value="${d.id}" <c:if test="${editing != null and editing.assignedDepartmentIds.contains(d.id)}">selected</c:if>><c:out value="${d.indent}" /><c:out value="${d.name}" /></option>
						</c:forEach>
					</select>
				</div>
				<div class="form-group">
					<label class="form-label" for="assign-staff">社員</label>
					<select id="assign-staff" name="assignStaffIds" class="form-input" multiple size="6">
						<c:forEach var="s" items="${staffList}">
							<option value="${s.id}" <c:if test="${editing != null and editing.assignedStaffIds.contains(s.id)}">selected</c:if>><c:out value="${s.id}" />: <c:out value="${s.name}" /></option>
						</c:forEach>
					</select>
				</div>
			</div>

			<div class="form-actions">
				<c:if test="${not empty editing}">
					<a href="${pageContext.request.contextPath}/masterApprovalRouteDef.do" class="btn btn-secondary">新しいルートを作る</a>
				</c:if>
				<button type="submit" class="btn btn-primary"><c:choose><c:when test="${not empty editing}">更新</c:when><c:otherwise>追加</c:otherwise></c:choose></button>
			</div>
		</form>
	</div>

	<div class="card" id="route-preview">
		<h2>ルートの確認</h2>
		<p class="hint">社員と申請種別を選ぶと、その社員が申請したときの承認ルートを表示します（申請は出しません）。</p>
		<form action="${pageContext.request.contextPath}/masterApprovalRouteDef.do#route-preview" method="get" class="route-form">
			<div class="form-row">
				<div class="form-group">
					<label class="form-label" for="preview-staff">社員</label>
					<select id="preview-staff" name="previewStaffId" class="form-input" required>
						<c:forEach var="s" items="${staffList}">
							<option value="${s.id}" <c:if test="${previewStaffId == s.id}">selected</c:if>><c:out value="${s.id}" />: <c:out value="${s.name}" /></option>
						</c:forEach>
					</select>
				</div>
				<div class="form-group">
					<label class="form-label" for="preview-type">申請種別</label>
					<select id="preview-type" name="previewType" class="form-input">
						<c:forEach var="t" items="${requestTypes}">
							<option value="${t.code}" <c:if test="${previewType == t.code}">selected</c:if>><c:out value="${t.label}" /></option>
						</c:forEach>
					</select>
				</div>
				<div class="form-group">
					<label class="form-label" for="preview-dept">申請する部署（兼任のとき）</label>
					<select id="preview-dept" name="previewDept" class="form-input">
						<option value="">主所属</option>
						<c:forEach var="d" items="${departments}">
							<option value="${d.id}" <c:if test="${previewDept == d.id}">selected</c:if>><c:out value="${d.indent}" /><c:out value="${d.name}" /></option>
						</c:forEach>
					</select>
				</div>
			</div>
			<button type="submit" class="btn btn-primary">確認</button>
		</form>
		<c:if test="${not empty previewError}">
			<div class="preview-result ng" role="status">⚠ <c:out value="${previewError}" /></div>
		</c:if>
		<c:if test="${not empty previewSource}">
			<div class="preview-result" role="status">
				<div><strong>決まり方：</strong><c:out value="${previewSource}" /></div>
				<c:if test="${not empty previewLevels}">
					<ol class="route-levels" style="list-style: none; padding-left: 0; margin-top: 4px;">
						<c:forEach var="label" items="${previewLevels}"><li><c:out value="${label}" /></li></c:forEach>
					</ol>
				</c:if>
			</div>
		</c:if>
	</div>
</div>
<script>
// 段の追加・削除。段の select は level1, level2, … の名前にする
(function () {
    const container = document.getElementById('levels');
    const count = document.getElementById('level-count');
    function blocks() { return container.querySelectorAll('.level-block'); }
    function renumber() {
        blocks().forEach(function (block, i) {
            block.querySelector('h4').textContent = (i + 1) + '段目';
            const select = block.querySelector('select');
            select.name = 'level' + (i + 1);
            select.setAttribute('aria-label', (i + 1) + '段目の承認者');
        });
        count.value = blocks().length;
    }
    window.addLevel = function () {
        const all = blocks();
        const copy = all[all.length - 1].cloneNode(true);
        Array.from(copy.querySelectorAll('option')).forEach(function (o) { o.selected = false; });
        container.appendChild(copy);
        renumber();
        copy.querySelector('select').focus();
    };
    window.removeLevel = function () {
        const all = blocks();
        if (all.length > 1) {
            all[all.length - 1].remove();
            renumber();
        }
    };
    renumber();
})();
</script>
</body>
</html>
