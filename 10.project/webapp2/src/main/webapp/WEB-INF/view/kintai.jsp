<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@page import="beans.*"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8" />
<title>勤怠管理システム</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
	<style>
/* Additional styles for kintai page */
.kintai-header {
	display: flex;
	justify-content: space-between;
	align-items: flex-end;
	flex-wrap: wrap;
	margin-bottom: 1.5rem;
}
.stat-card {
	background: var(--card-background);
	padding: 1rem 1.5rem;
	border-radius: var(--border-radius);
	box-shadow: var(--card-shadow);
	text-align: center;
}
.stat-card .label {
	font-size: 0.9rem;
	font-weight: 700;
	color: #6c757d;
}
.stat-card .value {
	font-size: 1.8rem;
	font-weight: 800;
	color: var(--primary-color);
}
.actions-card {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 1.5rem;
	gap: 1rem;
}
.table .form-input {
	padding: 6px 8px; /* Smaller padding for inputs inside table */
	font-size: 0.9rem;
}

.card {
    width: 100%;
    max-width: 100%;
}

.table {
    width: 100%;
    table-layout: fixed;
}

.table th, 
.table td {
    text-align: center;
    vertical-align: middle;
}



/* 列幅の固定 */
.table th:nth-child(1), .table td:nth-child(1) { width: 5%; }   /* 日 */
.table th:nth-child(2), .table td:nth-child(2) { width: 5%; }   /* 曜日 */
.table th:nth-child(3), .table td:nth-child(3) { width: 15%; }  /* 出勤 */
.table th:nth-child(4), .table td:nth-child(4) { width: 10%; }  /* 退勤 */
.table th:nth-child(5), .table td:nth-child(5) { width: 10%; }   /* 時間外 */
.table th:nth-child(6), .table td:nth-child(6) { width: 10%; }  /* 勤務区分 */
.table th:nth-child(7), .table td:nth-child(7) { width: 10%; }  /* 備考 */
.table th:nth-child(8), .table td:nth-child(8) { width: 10%; }   /* 補正CD */
.table th:nth-child(9), .table td:nth-child(9) { width: 10%; }   /* 補正(通) */
.table th:nth-child(10), .table td:nth-child(10) { width: 10%; } /* 補正(深) */


.status-area {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-bottom: 1rem;
}

.status-banner {
    width: 100%;
    padding: 12px 20px;
    border-radius: 6px;
    font-size: 1.05rem;
    font-weight: 700;
    display: flex;
    align-items: center;
    gap: 10px;
}

/* アイコン */
.status-banner .icon {
    font-size: 1.3rem;
}

/* メッセージ */
.status-banner .msg {
    flex: 1;
}

/* 色分け */
.status-info {
    background-color: #e7f3ff;
    color: #0b63c5;
}

.status-warning {
    background-color: #fff4e5;
    color: #c56a00;
}

.status-danger {
    background-color: #ffe5e5;
    
    }
/* ステータスをグリッドで並べる */
.status-grid {
    display: grid;
    grid-template-columns: repeat(3, auto); /* 縦3列 */
    gap: 6px 10px; /* 縦6px 横10px */
    align-items: start;
}

/* コンパクトなステータスタグ */
.status-tag {
    padding: 4px 10px;
    border-radius: 4px;
    font-size: 0.75rem;
    font-weight: 700;
    white-space: normal;   /* ← 折り返しを許可 */
max-width: 250px;
    line-height: 1.2;      /* ← 読みやすくする */
}


/* 色分け */
.status-info {
    background-color: #e7f3ff;
    color: #0b63c5;
}

.status-warning {
    background-color: #fff4e5;
    color: #c56a00;
}

.status-danger {
    background-color: #ffe5e5;
    color: #c50000;
}


.law-check-card {
    background: #ffffff;
    padding: 1rem 1.5rem;
    border-radius: 8px;
    box-shadow: var(--card-shadow);
    margin-bottom: 1.5rem;
}

.law-check-card h2 {
    font-size: 1.2rem;
    font-weight: 800;
    margin-bottom: 0.8rem;
}

.law-check-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.9rem;
}

.law-check-table th {
    width: 30%;
    text-align: left;
    padding: 6px;
    background: #f5f5f5;
    border-bottom: 1px solid #ddd;
}

.law-check-table td {
    padding: 6px;
    border-bottom: 1px solid #ddd;
}

.law-check-notes {
    margin-top: 1rem;
    font-size: 0.8rem;
    color: #555;
    line-height: 1.4;
}

/* 2 カラムで横に並べる */
.info-grid {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr; /* 横2列 */
    gap: 20px; /* カード間の余白 */
    margin-bottom: 1.5rem;
}

/* カードの高さを揃える */
.law-check-card,
.worktype-card {
    background: #ffffff;
    padding: 1rem 1.5rem;
    border-radius: 8px;
    box-shadow: var(--card-shadow);
}


.status-card {
    background: #ffffff;
    padding: 1rem 1.5rem;
    border-radius: 8px;
    box-shadow: var(--card-shadow);
}

.status-card h2 {
    font-size: 1.2rem;
    font-weight: 800;
    margin-bottom: 0.8rem;
}

.status-item {
    padding: 8px 10px;
    border-radius: 4px;
    font-size: 0.85rem;
    font-weight: 700;
    margin-bottom: 6px;
}

/* 色分け */
.status-success {
    background: #e8f8e8;
    color: #1a7f1a;
}

.status-info {
    background: #e7f3ff;
    color: #0b63c5;
}

.status-warning {
    background: #fff4e5;
    color: #c56a00;
}

.status-danger {
    background: #ffe5e5;
    color: #c50000;

}


/* ステータス通知カードをスクロール可能にする */
.status-card {
    background: #ffffff;
    padding: 1rem 1.5rem;
    border-radius: 8px;
    box-shadow: var(--card-shadow);

    max-height: 250px;     /* 表示上限 */
    overflow-y: auto;      /* スクロール */

    position: sticky;      /* ← 固定表示のポイント */
    top: 20px;             /* 画面上から20pxの位置に固定 */
}

.info-section {
    background: #f0f7ff; /* ← Infoっぽい淡い青 */
    padding: 20px;
    border-radius: 10px;
    margin-bottom: 20px;
    border: 1px solid #d0e4ff; /* 薄い枠線で情報ブロック感UP */
}

.info-grid {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr; /* 3列 */
    grid-template-rows: auto auto;      /* 2行 */
    gap: 20px;
}

.info-toggle {
    font-size: 1.1rem;
    font-weight: 800;
    cursor: pointer;
    margin-bottom: 8px;
    color: #0b63c5;
    user-select: none;
}

.info-toggle:hover {
    opacity: 0.7;
}


/* ステータス通知を横いっぱいに広げる */
.status-wide {
    grid-column: 1 / 4;
}
</style>

<!-- ▼ ステータスエリア（サンプル表示） ▼ -->
<div class="status-area">
    <div class="status-banner status-info">
        <span class="icon">ℹ️</span>
        <span class="msg">勤怠データは最新です（サンプル）</span>
    </div>

    <div class="status-banner status-warning">
        <span class="icon">⚠️</span>
        <span class="msg">36協定：時間外労働が上限に近づいています（サンプル）</span>
    </div>

    <div class="status-banner status-danger">
        <span class="icon">❗</span>
        <span class="msg">未承認の勤怠が 3 件あります（サンプル）</span>
    </div>
</div>
<!-- ▲ ステータスエリア（サンプル表示） ▲ -->


<!-- ▼ ステータスバナー（サンプル表示） ▼ -->
<!--     <div class="status-area"> -->
<%--         <c:forEach var="st" items="${statusList}"> --%>
<%--             <div class="status-banner ${st.level}"> --%>
<%--                 <span class="icon">${st.icon}</span> --%>
<%--                 <span class="msg"><c:out value="${st.message}" /></span> --%>
<!--             </div> -->
<%--         </c:forEach> --%>
<!--     </div> -->
<!-- ▲ ステータスバナー（サンプル表示） 	▲ -->


<div class="card">
	<div class="kintai-header">
		<div>
			<h1>勤怠管理</h1>
			<p style="margin: 0; font-size: 1.2rem; font-weight: 700;">
			
				ID <c:out value="${shainbean.id}" />
				:
				<c:out value="${shainbean.name}" />
				様 |
				<c:out value="${select_year}" />
				年
				<c:out value="${select_month}" />
				月
			</p>


<div class="info-toggle" onclick="toggleInfo()">
    <span id="info-arrow">▼</span> 勤務情報一覧
</div>

		<div class="info-section" id="info-section">
					<div class="info-grid">
						<!-- 労働基準法チェック -->
						<div class="law-check-card">
							<h2>労働基準法に基づく制限チェック</h2>
							<table class="law-check-table">
								<tr>
									<th>当月時間外</th>
									<td>0:00</td>
									<td>〇 原則45H迄</td>
								</tr>
								<tr>
									<th>当月休日出勤</th>
									<td>0</td>
									<td>〇 月4回迄</td>
								</tr>
								<tr>
									<th>日次残業制限超過</th>
									<td>0</td>
									<td>〇 5H/日迄</td>
								</tr>
								<tr>
									<th>年間時間外</th>
									<td>93:10</td>
									<td>〇 540H/年迄</td>
								</tr>
								<tr>
									<th>特別条項（累計回数）</th>
									<td>0</td>
									<td>〇 年6回迄</td>
								</tr>
								<tr>
									<th>特別条項（連続月数）</th>
									<td>0</td>
									<td>〇 連続2ヶ月迄</td>
								</tr>
							</table>
							<div class="law-check-notes">
								<p>※1. チェック結果がOKではない場合、その内容を必ず確認すること。</p>
								<p>※2. 開始・終了時刻は総勤務時間の開始・終了時刻を記入。</p>
								<p>※3. 勤務時間(直接)は各プロジェクトでの作業時間（時間外含む）。</p>
								<p>※4. 勤務時間(間接)は社内会議、教育全般の時間。</p>
							</div>
						</div>

						<!-- 勤務区分一覧 -->
						<div class="worktype-card">
							<h2>勤務区分一覧</h2>
							<table class="worktype-table">
								<tr>
									<th>出勤</th>
									<td>1</td>
									<td>通常出勤</td>
								</tr>
								<tr>
									<th>休出</th>
									<td>0</td>
									<td>振休無し休出（原則禁止）</td>
								</tr>
								<tr>
									<th>有休</th>
									<td>0</td>
									<td>有給休暇</td>
								</tr>
								<tr>
									<th>代休</th>
									<td>0</td>
									<td>振休なし休出の代休</td>
								</tr>
								<tr>
									<th>半休</th>
									<td>0</td>
									<td>半日有給休暇</td>
								</tr>
								<tr>
									<th>特休</th>
									<td>0</td>
									<td>特別休暇</td>
								</tr>
								<tr>
									<th>振出</th>
									<td>0</td>
									<td>振休ありの休日出勤</td>
								</tr>
								<tr>
									<th>振休</th>
									<td>0</td>
									<td>週内（日〜土）の休日振替</td>
								</tr>
								<tr>
									<th>その他</th>
									<td>0</td>
									<td>その他の勤務区分</td>
								</tr>
							</table>
						</div>

						<!-- ステータス通知カード -->
						<div class="status-wrapper">
							<div class="status-card">
								<h2>ステータス通知</h2>
								<div class="status-item status-success">✔ 時間外申請が承認されました</div>
								<div class="status-item status-warning">⚠ 今月の残業が上限に近づいています</div>
								<div class="status-item status-info">ℹ 勤怠データが更新されました</div>
								<div class="status-item status-danger">❗ 未承認の勤怠が 2 件あります</div>
								<div class="status-item status-success">✔ 時間外申請が承認されました</div>
								<div class="status-item status-warning">⚠ 今月の残業が上限に近づいています</div>
								<div class="status-item status-info">ℹ 勤怠データが更新されました</div>
								<div class="status-item status-danger">❗ 未承認の勤怠が 2 件あります</div>
								<div class="status-item status-success">✔ 時間外申請が承認されました</div>
								<div class="status-item status-warning">⚠ 今月の残業が上限に近づいています</div>
								<div class="status-item status-info">ℹ 勤怠データが更新されました</div>
								<div class="status-item status-danger">❗ 未承認の勤怠が 2 件あります</div>
							</div>



						</div>
						
						<div class="status-wrapper status-wide">
						<div class="status-card">
											<div class="law-check-notes">
								<p>※列挙する時間帯は休憩時間：5:00～5:30、8:30～9:00、12:00～13:00、18:00～18:30、22:00～22:30、2:00～3:00</p>
								<p>※18:30以降の稼働は事前申請が必要。</p>
								<p>※22:00以降の稼働は原則禁止とする。</p>
							</div>
						
						</div>
						</div>
					</div>



				</div>
				
						<div class="stat-card">
						<div class="label">稼働補正テンプレート</div>
						<select id="correction-template" class="value"
							style="font-size: 1.2rem; font-weight: 700;">
							<c:forEach var="tpl" items="${correctionTemplateList}">
<%-- 								<option value="${tpl.id}" --%>
<%-- 									${tpl.id == calendarbean.correctionId ? 'selected' : ''}> --%>
<%-- 									<c:out value="${tpl.name}" /> --%>
<!-- 								</option> -->
							</c:forEach>
						</select>
					</div>

					<div class="stat-card">
						<div class="label">有給休暇残日数</div>
						<div class="value">
							<c:out value="${shainbean.paidLeaveDays}" />
							日
						</div>
					</div>

				<div class="card actions-card">
		<div style="display: flex; gap: 1rem;">
			<button id="clock-in-btn" onclick="recordAttendance('出勤')"
				class="btn btn-primary">☀️ 出勤</button>
			<button id="clock-out-btn" onclick="recordAttendance('退勤')"
				class="btn btn-primary">🌙 退勤</button>
		</div>
		<div id="overtime-summary" style="font-weight: 700;"></div>
		<div>
			<button id="save-btn" onclick="saveAttendance()"
				class="btn btn-success">💾 保存</button>
		</div>
	</div>

	<table id="calendar-log" class="table">
		<thead>
			<tr>
				<th style="width: 5%;">日</th>
				<th style="width: 8%;">曜日</th>
				<th style="width: 8%;">出勤</th>
				<th style="width: 8%;">退勤</th>
				<th style="width: 8%;">時間外</th>
				<th style="width: 15%;">勤務区分</th>
				<th>備考</th>
				<th style="width: 7%;">補正(通)</th>
				<th style="width: 7%;">補正(深)</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="calendarbean" items="${calendarBeanList}">
				<c:set var="week" value="${calendarbean.week.toString()}" />
				<tr data-id="${calendarbean.id}" data-correction-id="${calendarbean.correctionId}" data-date="${calendarbean.kintaidate}"
					class="${week == 'SATURDAY' ? 'saturday' : ''} ${week == 'SUNDAY' ? 'sunday' : ''}">
					
					<td><fmt:formatDate value="${calendarbean.getDateOfKintaidate()}"
							pattern="d" /></td>
					<td>${week.substring(0,3)}</td>
					<td>${calendarbean.kintaifrom}</td>
					<td>${calendarbean.kintaito}</td>
					<td><c:out value="${calendarbean.jikangai}" /></td>
					<td><select name="status" class="form-input">
							<c:forEach var="opt" items="${statusOptions}">
								<option value="${opt.id}"
									${opt.id == calendarbean.abstractId ? 'selected' : ''}>
									<c:out value="${opt.name}" />
								</option>
							</c:forEach>
					</select></td>
					<td><c:out value="${calendarbean.memo}" /></td>
					<td><input type="number" name="correctionUsTime"
						value="${calendarbean.correctionUsTime}" class="form-input" /></td>
					<td><input type="number" name="correctionMidTime"
						value="${calendarbean.correctionMidTime}" class="form-input" /></td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<!-- Status dropdown template for JS -->
<select id="status-template" style="display: none;">
	<c:forEach var="opt" items="${statusOptions}">
		<option value="<c:out value="${opt.id}"/>">
			<c:out value="${opt.name}" />
		</option>
	</c:forEach>
</select>

<script>
	// Pass JSP variables to the global scope for the external JS file
	const staffId = <c:out value="${staff_id}" />;
	const statusOptionsJson = '${statusOptionsJson}';
	const year = <c:out value="${select_year}" />;
	const month = <c:out value="${select_month}" />;
</script>


<script
    src="${pageContext.request.contextPath}/resources/js/kintai/kintai.js"
    type="module"
    defer>
</script>

<script>
function toggleInfo() {
    const section = document.getElementById("info-section");
    const arrow = document.getElementById("info-arrow");

    if (section.style.display === "none") {
        section.style.display = "block";
        arrow.textContent = "▲";
    } else {
        section.style.display = "none";
        arrow.textContent = "▼";
    }
}
</script>

</body>
</html>
