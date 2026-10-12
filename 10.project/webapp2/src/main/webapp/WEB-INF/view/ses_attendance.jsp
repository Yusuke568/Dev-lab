<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.DayOfWeek" %>
<%@ page import="domain.attendance.DailyAttendance" %>
<%@ page import="domain.attendance.AbstractItem" %>
<%@ page import="domain.auth.UserSession" %>
<%@ page import="domain.auth.UserRole" %>
<%@ page import="domain.contract.ProjectContract" %>
<%@ page import="domain.contract.SettlementResult" %>
<%@ page import="domain.contract.SettlementStatus" %>
<%@ page import="domain.weeklyreport.WeeklyReport" %>
<%@ page import="domain.weeklyreport.WeeklyReportComment" %>
<%@ page import="domain.weeklyreport.AuthorRole" %>
<%@ page import="domain.weeklyreport.ReportStatus" %>
<%@ page import="domain.compliance.OvertimeLevel" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>SES勤怠・精算・週報マネージャー</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tailwind.min.css">
  <style>
    body {
      background-color: #0b0f19;
      color: #f1f5f9;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Hiragino Sans", sans-serif;
      -webkit-tap-highlight-color: transparent;
    }
    .panel {
      background-color: #131b2e;
      border: 1px solid #1e293b;
      border-radius: 0.75rem;
    }
    .pb-safe {
      padding-bottom: env(safe-area-inset-bottom, 1rem);
    }
  </style>
</head>
<body class="min-h-screen pb-20 md:pb-8">
  <%
    UserSession userSession = (UserSession) request.getAttribute("userSession");
    String targetEmployeeId = (String) request.getAttribute("targetEmployeeId");
    List<UserSession> allStaff = (List<UserSession>) request.getAttribute("allStaff");
    String yearMonth = (String) request.getAttribute("yearMonth");
    String prevMonth = (String) request.getAttribute("prevMonth");
    String nextMonth = (String) request.getAttribute("nextMonth");
    ProjectContract contract = (ProjectContract) request.getAttribute("contract");
    List<DailyAttendance> attendances = (List<DailyAttendance>) request.getAttribute("attendances");
    List<AbstractItem> abstractItems = (List<AbstractItem>) request.getAttribute("abstractItems");
    int staffPaidLeaveDays = (Integer) request.getAttribute("staffPaidLeaveDays");
    double monthPaidDays = (Double) request.getAttribute("monthPaidDays");
    double remainingPaidDays = (Double) request.getAttribute("remainingPaidDays");
    int totalDirect = (Integer) request.getAttribute("totalDirectMinutes");
    int totalIndirect = (Integer) request.getAttribute("totalIndirectMinutes");
    int totalWork = (Integer) request.getAttribute("totalWorkMinutes");
    int legalOvertime = (Integer) request.getAttribute("legalOvertimeMinutes");
    SettlementResult settlement = (SettlementResult) request.getAttribute("settlementResult");
    OvertimeLevel overtimeLevel = (OvertimeLevel) request.getAttribute("overtimeLevel");
    String paidLeaveWarning = (String) request.getAttribute("paidLeaveWarning");
    List<String> monthWeeks = (List<String>) request.getAttribute("monthWeeks");
    String currentYearWeek = (String) request.getAttribute("currentYearWeek");
    WeeklyReport weeklyReport = (WeeklyReport) request.getAttribute("currentWeeklyReport");
    WeeklyReport previousWeeklyReport = (WeeklyReport) request.getAttribute("previousWeeklyReport");

    double directHours = Math.round((totalDirect / 60.0) * 10.0) / 10.0;
    double indirectHours = Math.round((totalIndirect / 60.0) * 10.0) / 10.0;
    double overtimeHours = Math.round((legalOvertime / 60.0) * 10.0) / 10.0;
  %>

  <!-- ヘッダー（レスポンシブ & 管理リンク） -->
  <header class="sticky top-0 z-40 bg-slate-900/90 backdrop-blur border-b border-slate-800 px-4 py-3">
    <div class="max-w-7xl mx-auto flex flex-wrap items-center justify-between gap-3">
      <div class="flex items-center gap-2">
        <span class="p-1.5 rounded-lg bg-sky-500/20 text-sky-400">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </span>
        <div>
          <div class="flex items-center gap-2">
            <h1 class="text-sm md:text-base font-bold text-white leading-tight">SES勤怠・週報</h1>
            <!-- 有休残日数バッジ -->
            <span class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
              有休残 <%= remainingPaidDays %>日 (当月取得 <%= monthPaidDays %>日)
            </span>
          </div>
          <div class="flex items-center gap-1.5 text-[11px] text-slate-400">
            <span><%= userSession.getName() %></span>
            <span class="px-1.5 py-0.2 rounded text-[10px] font-bold <%= userSession.isAdmin() ? "bg-rose-500/20 text-rose-300" : (userSession.canManageOthers() ? "bg-amber-500/20 text-amber-300" : "bg-sky-500/20 text-sky-300") %>">
              <%= userSession.getRole().name() %>
            </span>
          </div>
        </div>
      </div>

      <!-- 管理者・上司用メニューリンク -->
      <% if (userSession.canManageOthers()) { %>
        <div class="flex items-center gap-1.5 text-xs">
          <a href="shainList.do" class="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 flex items-center gap-1">
            <span>👥</span><span class="hidden sm:inline">社員管理</span>
          </a>
          <a href="paidLeaveAdmin.do" class="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 flex items-center gap-1">
            <span>🏖️</span><span class="hidden sm:inline">有休管理</span>
          </a>
        </div>
      <% } %>

      <!-- 右側コントロール（前月/翌月・年月選択・社員切替・ログアウト） -->
      <div class="flex items-center gap-2">
        <form method="get" action="sesAttendance.do" id="filterForm" class="flex items-center gap-1.5">
          <% if (userSession.canManageOthers() && allStaff != null) { %>
            <select name="employeeId" onchange="document.getElementById('filterForm').submit()"
                    class="bg-slate-950 border border-slate-700 rounded-lg px-2 py-1.5 text-xs text-white">
              <% for (UserSession staff : allStaff) { %>
                <option value="<%= staff.getEmployeeId() %>" <%= staff.getEmployeeId().equals(targetEmployeeId) ? "selected" : "" %>>
                  <%= staff.getName() %> (<%= staff.getRole() %>)
                </option>
              <% } %>
            </select>
          <% } else { %>
            <input type="hidden" name="employeeId" value="<%= targetEmployeeId %>" />
          <% } %>

          <input type="hidden" name="yearWeek" value="<%= currentYearWeek %>" />

          <!-- 前月移動ボタン -->
          <a href="sesAttendance.do?yearMonth=<%= prevMonth %>&employeeId=<%= targetEmployeeId %>"
             class="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs border border-slate-700" title="前月へ">
            ◀
          </a>

          <!-- 年月選択 -->
          <input type="month" name="yearMonth" value="<%= yearMonth %>" onchange="document.getElementById('filterForm').submit()"
                 class="bg-slate-950 border border-slate-700 rounded-lg px-2 py-1.5 text-xs text-white" />

          <!-- 翌月移動ボタン -->
          <a href="sesAttendance.do?yearMonth=<%= nextMonth %>&employeeId=<%= targetEmployeeId %>"
             class="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs border border-slate-700" title="翌月へ">
            ▶
          </a>
        </form>

        <a href="logout.do" class="text-xs px-2.5 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700">
          終了
        </a>
      </div>
    </div>
  </header>

  <!-- スマホ用ボトムナビゲーション -->
  <div class="fixed bottom-0 left-0 right-0 z-40 bg-slate-900 border-t border-slate-800 flex justify-around p-2 md:hidden">
    <button onclick="switchTab('attendance')" id="tabBtn-attendance" class="flex-1 py-2 rounded-lg bg-sky-600 text-white text-xs font-bold transition">
      📅 勤怠
    </button>
    <button onclick="switchTab('report')" id="tabBtn-report" class="flex-1 py-2 rounded-lg text-slate-400 hover:text-white text-xs font-bold transition">
      📝 週報・所見
    </button>
    <button onclick="switchTab('summary')" id="tabBtn-summary" class="flex-1 py-2 rounded-lg text-slate-400 hover:text-white text-xs font-bold transition">
      📊 精算・労務
    </button>
  </div>

  <div class="max-w-7xl mx-auto px-4 py-4 md:py-6 space-y-6">

    <!-- サマリカード（精算幅メーター & 残業アラート） -->
    <div id="section-summary" class="grid grid-cols-1 md:grid-cols-4 gap-3">
      <!-- 稼働合計 -->
      <div class="panel p-3.5 space-y-1">
        <span class="text-[11px] text-slate-400 block">今月の実労働時間</span>
        <div class="text-xl font-mono font-bold text-white"><%= Math.round((totalWork / 60.0) * 10.0) / 10.0 %> <span class="text-xs text-slate-400 font-normal">時間</span></div>
        <div class="text-[11px] text-slate-400 flex items-center justify-between">
          <span>客先直接: <b class="text-sky-400"><%= directHours %>h</b></span>
          <span>社内間接: <b class="text-amber-400"><%= indirectHours %>h</b></span>
        </div>
      </div>

      <!-- 客先精算幅ゲージ -->
      <div class="panel p-3.5 space-y-1.5 md:col-span-2">
        <div class="flex items-center justify-between text-[11px]">
          <span class="text-slate-400">精算幅: <%= contract != null ? ((int) Math.round(contract.getBillingLowerLimitHours())) + "h 〜 " + ((int) Math.round(contract.getBillingUpperLimitHours())) + "h" : "未設定" %></span>
          <span class="font-bold px-1.5 py-0.2 rounded text-[10px] <%= settlement != null && settlement.getStatus() == SettlementStatus.WITHIN_RANGE ? "bg-emerald-500/20 text-emerald-400" : (settlement != null && settlement.getStatus() == SettlementStatus.OVERTIME ? "bg-rose-500/20 text-rose-400" : "bg-amber-500/20 text-amber-400") %>">
            <%= settlement != null ? settlement.getStatus().name() : "判定中" %>
          </span>
        </div>
        <%
          int lower = contract != null ? (int) Math.round(contract.getBillingLowerLimitHours()) : 140;
          int upper = contract != null ? (int) Math.round(contract.getBillingUpperLimitHours()) : 180;
          double percent = Math.min(100.0, Math.max(0.0, (directHours / (double) upper) * 100.0));
        %>
        <div class="w-full bg-slate-950 rounded-full h-2.5 overflow-hidden border border-slate-800">
          <div class="h-2.5 rounded-full transition-all <%= directHours < lower ? "bg-amber-500" : (directHours > upper ? "bg-rose-500" : "bg-emerald-500") %>" style="width: <%= percent %>%;"></div>
        </div>
        <div class="flex items-center justify-between text-[10px] text-slate-500">
          <span>0h</span>
          <span>下限: <%= lower %>h</span>
          <span>上限: <%= upper %>h</span>
        </div>
      </div>

      <!-- 36協定・法定外残業 -->
      <div class="panel p-3.5 space-y-1">
        <span class="text-[11px] text-slate-400 block">法定外残業（36協定）</span>
        <div class="text-xl font-mono font-bold <%= overtimeHours >= 35 ? "text-amber-400" : "text-emerald-400" %>">
          <%= overtimeHours %> <span class="text-xs text-slate-400 font-normal">/ 45h</span>
        </div>
        <div class="text-[11px] text-slate-400 truncate"><%= contract != null ? contract.getClientName() : "未設定" %></div>
      </div>
    </div>

    <!-- メインコンテンツグリッド -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">

      <!-- 日次勤怠パネル (2カラム幅) -->
      <div id="section-attendance" class="panel p-4 md:p-5 lg:col-span-2 space-y-4">
        <div class="flex flex-wrap items-center justify-between gap-2 border-b border-slate-800 pb-3">
          <div>
            <h2 class="text-sm md:text-base font-bold text-white flex items-center gap-2">
              <span>📅 日次勤怠レコード</span>
              <span class="text-xs text-slate-400">(<%= attendances != null ? attendances.size() : 0 %>日分)</span>
            </h2>
            <p class="text-[11px] text-slate-400">摘要の変更や残業がある日のみ「編集」してください</p>
          </div>

          <div class="flex items-center gap-2">
            <button onclick="applyTemplate()" class="px-3 py-1.5 rounded-lg bg-sky-600 hover:bg-sky-500 text-xs font-bold text-white shadow">
              月初一括展開
            </button>
            <button onclick="calculateSettlement()" class="px-3 py-1.5 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-xs font-bold text-white shadow">
              月次集計確定
            </button>
          </div>
        </div>

        <!-- 勤怠テーブル -->
        <div class="overflow-x-auto max-h-[540px] overflow-y-auto">
          <table class="w-full text-xs text-left border-collapse whitespace-nowrap md:whitespace-normal">
            <thead class="sticky top-0 bg-slate-900 text-slate-400 border-b border-slate-800">
              <tr>
                <th class="py-2.5 px-3">日付</th>
                <th class="py-2.5 px-2">摘要</th>
                <th class="py-2.5 px-2">勤務時間</th>
                <th class="py-2.5 px-2">直接時間</th>
                <th class="py-2.5 px-2">間接時間</th>
                <th class="py-2.5 px-2">状態</th>
                <th class="py-2.5 px-2 text-right">操作</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-800/80 text-slate-300">
              <% if (attendances != null && !attendances.isEmpty()) { %>
                <% for (DailyAttendance a : attendances) {
                     DayOfWeek dow = a.getWorkDate().getDayOfWeek();
                     String dowName = "月";
                     String dowColor = "text-slate-400";
                     String rowBg = "hover:bg-slate-800/40";
                     if (dow == DayOfWeek.TUESDAY) dowName = "火";
                     else if (dow == DayOfWeek.WEDNESDAY) dowName = "水";
                     else if (dow == DayOfWeek.THURSDAY) dowName = "木";
                     else if (dow == DayOfWeek.FRIDAY) dowName = "金";
                     else if (dow == DayOfWeek.SATURDAY) {
                       dowName = "土";
                       dowColor = "text-sky-400 font-bold";
                       rowBg = "bg-sky-950/20 hover:bg-sky-900/30";
                     } else if (dow == DayOfWeek.SUNDAY) {
                       dowName = "日";
                       dowColor = "text-rose-400 font-bold";
                       rowBg = "bg-rose-950/20 hover:bg-rose-900/30";
                     }
                     boolean isWeekend = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);
                     boolean canClear = !a.isLocked() && (a.isFilled() || (!isWeekend && a.getAbstractId() != 1) || (isWeekend && a.getAbstractId() != 10));
                %>
                  <tr class="<%= rowBg %>">
                    <td class="py-2.5 px-3 font-mono font-medium text-white">
                      <div class="flex items-center gap-1.5">
                        <span><%= a.getWorkDate() %></span>
                        <span class="text-[11px] <%= dowColor %>">(<%= dowName %>)</span>
                      </div>
                    </td>
                    <!-- 摘要バッジ -->
                    <td class="py-2.5 px-2">
                      <%
                        int absId = a.getAbstractId();
                        String absName = a.getAbstractName() != null ? a.getAbstractName() : (absId == 3 ? "有給" : (absId == 5 ? "半休" : (absId == 10 ? "休日" : (absId == 2 ? "休出" : "出勤"))));
                        String absClass = "bg-slate-800 text-slate-300";
                        if (absId == 1) absClass = "bg-sky-500/20 text-sky-300";
                        else if (absId == 3) absClass = "bg-emerald-500/20 text-emerald-300 font-bold";
                        else if (absId == 5) absClass = "bg-lime-500/20 text-lime-300";
                        else if (absId == 2) absClass = "bg-rose-500/20 text-rose-300 font-bold";
                        else if (absId == 10) absClass = "bg-slate-800/80 text-slate-400";
                      %>
                      <span class="px-2 py-0.5 rounded text-[10px] <%= absClass %>">
                        <%= absName %>
                      </span>
                    </td>
                    <td class="py-2.5 px-2 font-mono">
                      <%= a.getStartTime() != null ? a.getStartTime().toString().substring(0, 5) : "--:--" %> - <%= a.getEndTime() != null ? a.getEndTime().toString().substring(0, 5) : "--:--" %>
                      <span class="text-[10px] text-slate-500">(休<%= a.getBreakMinutes() %>分)</span>
                    </td>
                    <td class="py-2.5 px-2 font-bold text-sky-400"><%= Math.round((a.getDirectMinutes() / 60.0) * 10.0) / 10.0 %>h</td>
                    <td class="py-2.5 px-2 text-amber-400"><%= Math.round((a.getIndirectMinutes() / 60.0) * 10.0) / 10.0 %>h</td>
                    <td class="py-2.5 px-2">
                      <% if (a.isLocked()) { %>
                        <span class="px-1.5 py-0.5 rounded bg-slate-800 text-slate-400 font-bold text-[10px]">確定</span>
                      <% } else if (a.isFilled()) { %>
                        <span class="px-1.5 py-0.5 rounded bg-emerald-500/20 text-emerald-400 font-bold text-[10px]">入力済</span>
                      <% } else if (absId == 10) { %>
                        <span class="px-1.5 py-0.5 rounded bg-slate-800 text-slate-400 font-bold text-[10px]">休日</span>
                      <% } else { %>
                        <span class="px-1.5 py-0.5 rounded bg-amber-500/20 text-amber-400 font-bold text-[10px]">未入力</span>
                      <% } %>
                    </td>
                    <td class="py-2.5 px-2 text-right">
                      <% if (!a.isLocked() && userSession.canEdit(targetEmployeeId)) { %>
                        <div class="inline-flex items-center gap-1">
                          <button onclick="openEditModal('<%= a.getWorkDate() %>', '<%= a.getStartTime() != null ? a.getStartTime() : "" %>', '<%= a.getEndTime() != null ? a.getEndTime() : "" %>', <%= a.getBreakMinutes() %>, <%= a.getAbstractId() %>, <%= a.getDirectMinutes() %>, <%= a.getIndirectMinutes() %>, '<%= a.getRemarks() != null ? a.getRemarks() : "" %>', <%= canClear %>)"
                                  class="px-2 py-1 rounded bg-sky-600/20 hover:bg-sky-600/40 text-sky-400 text-xs font-semibold">
                            編集
                          </button>
                          <% if (canClear) { %>
                            <button onclick="clearAttendance('<%= a.getWorkDate() %>')"
                                    title="勤怠入力をクリアして未入力/休日に戻す"
                                    class="px-2 py-1 rounded bg-slate-800 hover:bg-rose-900/30 text-slate-400 hover:text-rose-300 text-xs">
                              クリア
                            </button>
                          <% } %>
                        </div>
                      <% } else { %>
                        <span class="text-slate-600 text-xs">-</span>
                      <% } %>
                    </td>
                  </tr>
                <% } %>
              <% } else { %>
                <tr>
                  <td colspan="7" class="py-12 text-center text-slate-500">
                    勤怠レコードがありません。「月初一括展開」を押してカレンダーを初期生成してください。
                  </td>
                </tr>
              <% } %>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 週報 & 労務コンプライアンスパネル (1カラム幅) -->
      <div id="section-report" class="space-y-5">

        <!-- 週報カード -->
        <div class="panel p-4 md:p-5 space-y-4 border-t-2 border-indigo-500">
          <div class="flex items-center justify-between gap-2">
            <div>
              <h2 class="text-sm md:text-base font-bold text-white flex items-center gap-1.5">
                <span>📝 週報</span>
              </h2>
              <!-- 週切替セレクター -->
              <form method="get" action="sesAttendance.do" id="weekForm" class="mt-1">
                <input type="hidden" name="employeeId" value="<%= targetEmployeeId %>" />
                <input type="hidden" name="yearMonth" value="<%= yearMonth %>" />
                <select name="yearWeek" onchange="document.getElementById('weekForm').submit()"
                        class="bg-slate-950 border border-slate-700 rounded-lg px-2 py-1 text-xs text-sky-300 font-semibold">
                  <% if (monthWeeks != null) { %>
                    <% for (int i = 0; i < monthWeeks.size(); i++) {
                         String yw = monthWeeks.get(i);
                    %>
                      <option value="<%= yw %>" <%= yw.equals(currentYearWeek) ? "selected" : "" %>>
                        第<%= (i + 1) %>週 (<%= yw %>)
                      </option>
                    <% } %>
                  <% } %>
                </select>
              </form>
            </div>

            <!-- ステータスバッジ -->
            <span class="text-[10px] px-2 py-0.5 rounded font-bold
              <%= weeklyReport != null && weeklyReport.getStatus() == ReportStatus.APPROVED_FINAL ? "bg-emerald-500/20 text-emerald-400 border border-emerald-500/30" :
                 (weeklyReport != null && weeklyReport.getStatus() == ReportStatus.REVIEWED_PRIMARY ? "bg-indigo-500/20 text-indigo-400 border border-indigo-500/30" :
                 (weeklyReport != null && weeklyReport.getStatus() == ReportStatus.REMANDED ? "bg-rose-500/20 text-rose-400 border border-rose-500/30" :
                 (weeklyReport != null && weeklyReport.getStatus() == ReportStatus.SKIPPED ? "bg-slate-700 text-slate-300" :
                 (weeklyReport != null && weeklyReport.getStatus() == ReportStatus.SUBMITTED ? "bg-sky-500/20 text-sky-400 border border-sky-500/30" : "bg-slate-800 text-slate-400")))) %>">
              <%
                String statusLabel = "未提出";
                if (weeklyReport != null) {
                  switch (weeklyReport.getStatus()) {
                    case SUBMITTED: statusLabel = "提出済 (課長確認中)"; break;
                    case REVIEWED_PRIMARY: statusLabel = "課長確認済 (所属長決裁待)"; break;
                    case APPROVED_FINAL: statusLabel = "所属長承認完了"; break;
                    case REMANDED: statusLabel = "差戻し"; break;
                    case SKIPPED: statusLabel = "休暇スキップ"; break;
                    default: statusLabel = "下書き";
                  }
                }
              %>
              <%= statusLabel %>
            </span>
          </div>

          <!-- 週報入力フォーム（3分割実績） -->
          <form id="weeklyReportForm" onsubmit="submitWeeklyReport(event)" class="space-y-3 text-xs">
            <!-- 前週コピーボタン -->
            <% if (userSession.canEdit(targetEmployeeId) && (weeklyReport == null || weeklyReport.getStatus() == ReportStatus.DRAFT || weeklyReport.getStatus() == ReportStatus.REMANDED)) { %>
              <div class="flex items-center justify-between pb-1">
                <span class="text-[11px] text-slate-400">実績の3分割入力:</span>
                <% if (previousWeeklyReport != null) { %>
                  <button type="button" onclick="copyPreviousWeek()" class="text-[11px] px-2 py-1 rounded bg-indigo-950 hover:bg-indigo-900 text-indigo-300 border border-indigo-700">
                    📋 前週の内容をコピー
                  </button>
                <% } %>
              </div>
            <% } %>

            <div>
              <label class="block text-slate-300 font-semibold mb-1">① 作業件名、工程名・作業名</label>
              <textarea id="taskTitle" rows="2" required class="w-full bg-slate-950 border border-slate-700 rounded-lg p-2.5 text-white" placeholder="例: ○○様 顧客管理システム刷新 / 製造・単体テスト"><%= weeklyReport != null && weeklyReport.getTaskTitle() != null ? weeklyReport.getTaskTitle() : "" %></textarea>
            </div>

            <div>
              <label class="block text-slate-300 font-semibold mb-1">② 作業進捗、作業内容と状況</label>
              <textarea id="taskProgress" rows="3" required class="w-full bg-slate-950 border border-slate-700 rounded-lg p-2.5 text-white" placeholder="例: 進捗100%、担当機能A・Bの実装および単体テスト完了"><%= weeklyReport != null && weeklyReport.getTaskProgress() != null ? weeklyReport.getTaskProgress() : "" %></textarea>
            </div>

            <div>
              <label class="block text-slate-300 font-semibold mb-1">③ 本人所感</label>
              <textarea id="personalReflection" rows="2" required class="w-full bg-slate-950 border border-slate-700 rounded-lg p-2.5 text-white" placeholder="例: 予定通り完了。来週の結合テスト仕様書を事前確認中"><%= weeklyReport != null && weeklyReport.getPersonalReflection() != null ? weeklyReport.getPersonalReflection() : "" %></textarea>
            </div>

            <!-- アクションボタン（提出 & 休暇スキップ） -->
            <% if (userSession.canEdit(targetEmployeeId) && (weeklyReport == null || weeklyReport.getStatus() == ReportStatus.DRAFT || weeklyReport.getStatus() == ReportStatus.REMANDED)) { %>
              <div class="grid grid-cols-2 gap-2 pt-1">
                <button type="submit" class="w-full bg-indigo-600 hover:bg-indigo-500 text-white font-bold py-2.5 rounded-lg shadow transition">
                  週報を提出する
                </button>
                <button type="button" onclick="skipWeeklyReport()" class="w-full bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold py-2.5 rounded-lg border border-slate-700 transition">
                  今週を休暇スキップ
                </button>
              </div>
            <% } %>
          </form>

          <!-- 所見（レビューコメント）履歴（社員に公開） -->
          <% if (weeklyReport != null && !weeklyReport.getComments().isEmpty()) { %>
            <div class="border-t border-slate-800 pt-3 space-y-2">
              <span class="text-[11px] font-bold text-slate-400 block">💬 所見・決裁コメント履歴（公開）</span>
              <% for (WeeklyReportComment comm : weeklyReport.getComments()) { %>
                <%-- 本人非公開メモ以外を表示 --%>
                <% if (!comm.isPrivateToMgmt()) { %>
                  <div class="p-2.5 rounded-lg bg-slate-900 border border-slate-800 text-xs space-y-1">
                    <div class="flex items-center justify-between text-[10px] text-slate-400">
                      <span class="font-bold <%= comm.getAuthorRole() == AuthorRole.FINAL_APPROVER ? "text-rose-300" : "text-sky-300" %>">
                        <%= comm.getAuthorRole() == AuthorRole.FINAL_APPROVER ? "所属長所見" : "課長所見" %> (<%= comm.getAuthorId() %>)
                      </span>
                      <span class="text-slate-500"><%= comm.getCreatedAt().toString().substring(0, 16) %></span>
                    </div>
                    <p class="text-slate-200 whitespace-pre-wrap"><%= comm.getComment() %></p>
                  </div>
                <% } %>
              <% } %>
            </div>
          <% } %>

          <!-- 課長所見入力枠（SUPERVISOR / ADMIN、提出済時に表示） -->
          <% if (userSession.canManageOthers() && weeklyReport != null && weeklyReport.getStatus() == ReportStatus.SUBMITTED) { %>
            <div class="border-t border-slate-800 pt-3 space-y-2 text-xs">
              <div class="flex items-center justify-between">
                <span class="font-bold text-sky-400 block">✍️ 課長所見の記入（社員に公開）</span>
                <span class="text-[10px] text-slate-400">一次確認</span>
              </div>
              <textarea id="sectionChiefComment" rows="2" class="w-full bg-slate-950 border border-slate-700 rounded-lg p-2 text-white" placeholder="業務フォロー方針、アドバイス"></textarea>
              <div class="flex items-center justify-end gap-2">
                <button onclick="remandReport('<%= weeklyReport.getId() %>')" class="px-3 py-1.5 rounded-lg bg-rose-600/30 hover:bg-rose-600/50 text-rose-300 font-semibold border border-rose-500/40">
                  差戻し
                </button>
                <button onclick="submitSupervisorReview('<%= weeklyReport.getId() %>')" class="px-3 py-1.5 rounded-lg bg-sky-600 hover:bg-sky-500 font-bold text-white">
                  課長所見を登録（所属長へ）
                </button>
              </div>
            </div>
          <% } %>

          <!-- 所属長所見入力枠（ADMIN、課長確認済時に表示） -->
          <% if (userSession.isAdmin() && weeklyReport != null && weeklyReport.getStatus() == ReportStatus.REVIEWED_PRIMARY) { %>
            <div class="border-t border-slate-800 pt-3 space-y-2 text-xs">
              <div class="flex items-center justify-between">
                <span class="font-bold text-rose-400 block">✍️ 所属長所見の記入（最終決裁・社員に公開）</span>
                <span class="text-[10px] text-slate-400">最終承認</span>
              </div>
              <textarea id="deptHeadComment" rows="2" class="w-full bg-slate-950 border border-slate-700 rounded-lg p-2 text-white" placeholder="全体評価・承認コメント"></textarea>
              <div class="flex items-center justify-end gap-2">
                <button onclick="remandReport('<%= weeklyReport.getId() %>')" class="px-3 py-1.5 rounded-lg bg-rose-600/30 hover:bg-rose-600/50 text-rose-300 font-semibold border border-rose-500/40">
                  差戻し
                </button>
                <button onclick="approveFinalReport('<%= weeklyReport.getId() %>')" class="px-3 py-1.5 rounded-lg bg-rose-600 hover:bg-rose-500 font-bold text-white">
                  所属長所見を登録（最終承認完了）
                </button>
              </div>
            </div>
          <% } %>

        </div>

        <!-- 管理者共有メモカード（本人非公開・別枠・SUPERVISOR/ADMINのみ） -->
        <% if (userSession.canManageOthers() && weeklyReport != null) { %>
          <div class="panel p-4 space-y-3 border border-amber-800/40 bg-amber-950/20 text-xs">
            <h3 class="font-bold text-amber-300 flex items-center justify-between">
              <span>🔒 管理者共有メモ（本人非公開）</span>
              <span class="text-[10px] text-amber-400/80">※社員画面には一切表示されません</span>
            </h3>

            <!-- 過去の非公開メモ一覧 -->
            <div class="space-y-1.5">
              <%
                boolean hasPrivate = false;
                for (WeeklyReportComment comm : weeklyReport.getComments()) {
                  if (comm.isPrivateToMgmt()) {
                    hasPrivate = true;
              %>
                <div class="p-2 rounded bg-slate-900/80 border border-amber-900/40 space-y-1">
                  <div class="flex items-center justify-between text-[10px] text-amber-400">
                    <span><%= comm.getAuthorRole() %> (<%= comm.getAuthorId() %>)</span>
                    <span><%= comm.getCreatedAt().toString().substring(0, 16) %></span>
                  </div>
                  <p class="text-slate-200"><%= comm.getComment() %></p>
                </div>
              <%
                  }
                }
                if (!hasPrivate) {
              %>
                <p class="text-slate-500 text-[11px]">記録された非公開メモはありません。</p>
              <% } %>
            </div>

            <!-- 新規非公開メモ追加 -->
            <div class="space-y-2 pt-2 border-t border-amber-900/30">
              <textarea id="privateMgmtNote" rows="2" class="w-full bg-slate-950 border border-slate-700 rounded-lg p-2 text-white" placeholder="上司・営業間の引き継ぎ・懸念事項など（本人非公開）"></textarea>
              <div class="flex justify-end">
                <button onclick="submitPrivateNote('<%= weeklyReport.getId() %>')" class="px-3 py-1.5 rounded-lg bg-amber-600 hover:bg-amber-500 font-bold text-white">
                  非公開メモを保存
                </button>
              </div>
            </div>
          </div>
        <% } %>

        <!-- 労務コンプライアンスカード -->
        <div class="panel p-4 md:p-5 space-y-3">
          <h2 class="text-sm font-bold text-white flex items-center gap-2">
            <span>🛡️ 労務コンプライアンス状況</span>
          </h2>
          <% if (paidLeaveWarning != null) { %>
            <div class="p-3 rounded-lg bg-amber-500/10 border border-amber-500/30 text-amber-300 text-xs">
              ⚠️ <%= paidLeaveWarning %>
            </div>
          <% } else { %>
            <div class="p-2.5 rounded-lg bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-xs">
              ✓ 有給休暇年5日取得義務の進捗は適正です（残: <%= remainingPaidDays %>日）
            </div>
          <% } %>
          <div class="text-[11px] text-slate-400 space-y-1">
            <p>・有給付与残日数: <b class="text-white"><%= remainingPaidDays %>日</b>（当月取得: <%= monthPaidDays %>日）</p>
            <p>・36協定限度: 月45時間（35時間到達で注意アラート）</p>
          </div>
        </div>

      </div>
    </div>

  </div>

  <!-- 勤怠編集モーダル（摘要プルダウン連動） -->
  <div id="editModal" class="fixed inset-0 bg-black/75 flex items-end md:items-center justify-center p-0 md:p-4 hidden z-50">
    <div class="panel p-5 md:p-6 w-full max-w-lg rounded-t-2xl md:rounded-2xl space-y-4 max-h-[90vh] overflow-y-auto pb-safe">
      <div class="flex items-center justify-between border-b border-slate-800 pb-2.5">
        <h3 class="font-bold text-white text-sm" id="modalTitle">勤怠編集</h3>
        <button onclick="closeEditModal()" class="text-slate-400 hover:text-white p-1 text-lg leading-none">&times;</button>
      </div>

      <form id="editForm" onsubmit="saveAttendance(event)" class="space-y-3.5 text-xs">
        <input type="hidden" id="editWorkDate" />

        <!-- 隠し送信フィールド（自動計算された値が入る） -->
        <input type="hidden" id="editStartTime" />
        <input type="hidden" id="editEndTime" />
        <input type="hidden" id="editBreakMinutes" />
        <input type="hidden" id="editDirectMinutes" />
        <input type="hidden" id="editIndirectMinutes" />

        <!-- 摘要区分 -->
        <div>
          <label class="block text-slate-400 mb-1 font-semibold">摘要区分</label>
          <select id="editAbstractId" onchange="onAbstractChange()" class="w-full bg-slate-950 border border-slate-700 rounded-lg p-2.5 text-white font-bold">
            <% if (abstractItems != null) { %>
              <% for (AbstractItem item : abstractItems) { %>
                <option value="<%= item.getId() %>"><%= item.getName() %></option>
              <% } %>
            <% } else { %>
              <option value="1">出勤</option>
              <option value="2">休出</option>
              <option value="3">有給</option>
              <option value="5">半休</option>
              <option value="10">休日</option>
            <% } %>
          </select>
        </div>

        <!-- ① 客先直接労働ブロック -->
        <div class="p-3 rounded-xl bg-slate-900/90 border border-sky-500/30 space-y-2.5">
          <div class="flex items-center justify-between">
            <span class="font-bold text-sky-400 flex items-center gap-1.5">
              <span>💼 客先直接労働</span>
            </span>
            <span id="directBadge" class="text-[11px] font-mono font-bold text-sky-300">8.0h (480分)</span>
          </div>
          <div class="grid grid-cols-3 gap-2">
            <div>
              <label class="block text-[11px] text-slate-400 mb-1">開始時刻</label>
              <input type="time" id="directStartTime" onchange="calculateWorkTimes()" required
                     class="w-full bg-slate-950 border border-slate-700 rounded-lg p-2 text-white font-mono" />
            </div>
            <div>
              <label class="block text-[11px] text-slate-400 mb-1">終了時刻</label>
              <input type="time" id="directEndTime" onchange="calculateWorkTimes()" required
                     class="w-full bg-slate-950 border border-slate-700 rounded-lg p-2 text-white font-mono" />
            </div>
            <div>
              <label class="block text-[11px] text-slate-400 mb-1">昼休憩 (分)</label>
              <input type="number" id="directBreakMinutes" min="0" onchange="calculateWorkTimes()" required
                     class="w-full bg-slate-950 border border-slate-700 rounded-lg p-2 text-white font-mono" />
            </div>
          </div>
        </div>

        <!-- ② 社内間接労働ブロック（中抜けスケジュール対応） -->
        <div class="p-3 rounded-xl bg-slate-900/90 border border-amber-500/30 space-y-2.5">
          <div class="flex items-center justify-between">
            <label class="flex items-center gap-2 cursor-pointer">
              <input type="checkbox" id="enableIndirect" onchange="toggleIndirectSection()"
                     class="w-4 h-4 rounded text-amber-500 bg-slate-950 border-slate-700 focus:ring-amber-500" />
              <span class="font-bold text-amber-400">🏢 社内間接業務・研修（中抜け含む）</span>
            </label>
            <span id="indirectBadge" class="text-[11px] font-mono font-bold text-amber-300">なし</span>
          </div>

          <div id="indirectInputs" class="hidden space-y-2 pt-1 border-t border-slate-800">
            <div class="grid grid-cols-2 gap-2">
              <div>
                <label class="block text-[11px] text-slate-400 mb-1">開始時刻（例: 19:00）</label>
                <input type="time" id="indirectStartTime" onchange="calculateWorkTimes()"
                       class="w-full bg-slate-950 border border-slate-700 rounded-lg p-2 text-white font-mono" />
              </div>
              <div>
                <label class="block text-[11px] text-slate-400 mb-1">終了時刻（例: 20:00）</label>
                <input type="time" id="indirectEndTime" onchange="calculateWorkTimes()"
                       class="w-full bg-slate-950 border border-slate-700 rounded-lg p-2 text-white font-mono" />
              </div>
            </div>
            <div>
              <label class="block text-[11px] text-slate-400 mb-1">間接業務メモ (社内研修・定例MTG等)</label>
              <input type="text" id="editIndirectTask" class="w-full bg-slate-950 border border-slate-700 rounded-lg p-2 text-white" placeholder="帰社日MTG / 社内研修 / 1on1" />
            </div>
          </div>
        </div>

        <!-- ③ 自動計算サマリ -->
        <div class="p-2.5 rounded-lg bg-slate-950 border border-slate-800 text-[11px] space-y-1" id="timeCalcSummary">
          <div class="flex items-center justify-between">
            <span class="text-slate-400">実労働時間合計:</span>
            <span class="font-bold text-white text-sm" id="summaryTotalWork">8.0h</span>
          </div>
          <div class="flex items-center justify-between text-slate-400">
            <span>客先直接: <b class="text-sky-400 font-bold" id="summaryDirect">8.0h</b> / 社内間接: <b class="text-amber-400 font-bold" id="summaryIndirect">0.0h</b></span>
            <span id="summaryBreak">休憩: 60分</span>
          </div>
        </div>

        <div>
          <label class="block text-slate-400 mb-1">備考（遅刻・早退・残業理由など）</label>
          <input type="text" id="editRemarks" class="w-full bg-slate-950 border border-slate-700 rounded-lg p-2.5 text-white" />
        </div>

        <div class="flex items-center justify-between pt-2">
          <button type="button" id="modalClearBtn" onclick="onModalClear()" class="px-3 py-2 rounded-lg bg-rose-950/80 hover:bg-rose-900 text-rose-300 font-semibold border border-rose-800 text-xs hidden">
            🗑️ 入力をクリア
          </button>
          <div class="flex items-center gap-2 ml-auto">
            <button type="button" onclick="closeEditModal()" class="px-4 py-2.5 rounded-lg bg-slate-800 text-slate-300 font-semibold">閉じる</button>
            <button type="submit" class="px-5 py-2.5 rounded-lg bg-sky-600 hover:bg-sky-500 font-bold text-white shadow">保存する</button>
          </div>
        </div>
      </form>
    </div>
  </div>

  <script>
    const EMPLOYEE_ID = "<%= targetEmployeeId %>";
    const YEAR_MONTH = "<%= yearMonth %>";
    const CURRENT_WEEK = "<%= currentYearWeek %>";

    // 前週コピー用データ
    const PREV_TASK_TITLE = "<%= (previousWeeklyReport != null && previousWeeklyReport.getTaskTitle() != null) ? previousWeeklyReport.getTaskTitle().replace("\"", "\\\"").replace("\n", "\\n") : "" %>";
    const PREV_TASK_PROGRESS = "<%= (previousWeeklyReport != null && previousWeeklyReport.getTaskProgress() != null) ? previousWeeklyReport.getTaskProgress().replace("\"", "\\\"").replace("\n", "\\n") : "" %>";

    function copyPreviousWeek() {
      if (!PREV_TASK_TITLE) {
        alert("コピー可能な前週の週報が見つかりません。");
        return;
      }
      document.getElementById("taskTitle").value = PREV_TASK_TITLE;
      document.getElementById("taskProgress").value = PREV_TASK_PROGRESS;
      alert("前週の件名・工程および進捗をコピーしました。今週の差分を更新してください。");
    }

    function timeToMinutes(tStr) {
      if (!tStr) return 0;
      const parts = tStr.split(":");
      return parseInt(parts[0], 10) * 60 + parseInt(parts[1], 10);
    }

    function minutesToTime(mins) {
      const norm = ((mins % (24 * 60)) + (24 * 60)) % (24 * 60);
      const h = Math.floor(norm / 60);
      const m = norm % 60;
      return String(h).padStart(2, '0') + ":" + String(m).padStart(2, '0');
    }

    function toggleIndirectSection() {
      const enable = document.getElementById("enableIndirect").checked;
      const container = document.getElementById("indirectInputs");
      if (enable) {
        container.classList.remove("hidden");
        if (!document.getElementById("indirectStartTime").value) {
          document.getElementById("indirectStartTime").value = "19:00";
        }
        if (!document.getElementById("indirectEndTime").value) {
          document.getElementById("indirectEndTime").value = "20:00";
        }
      } else {
        container.classList.add("hidden");
      }
      calculateWorkTimes();
    }

    function calculateWorkTimes() {
      const absId = document.getElementById("editAbstractId").value;
      if (absId === "3" || absId === "10") {
        document.getElementById("editStartTime").value = "00:00";
        document.getElementById("editEndTime").value = "00:00";
        document.getElementById("editBreakMinutes").value = 0;
        document.getElementById("editDirectMinutes").value = 0;
        document.getElementById("editIndirectMinutes").value = 0;
        document.getElementById("summaryTotalWork").innerText = "0.0h";
        document.getElementById("summaryDirect").innerText = "0.0h";
        document.getElementById("summaryIndirect").innerText = "0.0h";
        document.getElementById("summaryBreak").innerText = "休憩: 0分";
        document.getElementById("directBadge").innerText = "0.0h (0分)";
        document.getElementById("indirectBadge").innerText = "0.0h (0分)";
        return;
      }

      const dStart = document.getElementById("directStartTime").value;
      const dEnd = document.getElementById("directEndTime").value;
      const dBreak = parseInt(document.getElementById("directBreakMinutes").value || "0", 10);

      if (!dStart || !dEnd) return;

      const dStartMin = timeToMinutes(dStart);
      let dEndMin = timeToMinutes(dEnd);
      if (dEndMin < dStartMin) dEndMin += 24 * 60;

      const directMinutes = Math.max(0, (dEndMin - dStartMin) - dBreak);
      document.getElementById("directBadge").innerText = (directMinutes / 60).toFixed(1) + "h (" + directMinutes + "分)";

      let indirectMinutes = 0;
      let totalEndMin = dEndMin;
      let totalBreak = dBreak;
      let gapMinutes = 0;

      const hasIndirect = document.getElementById("enableIndirect").checked;
      const indStart = document.getElementById("indirectStartTime").value;
      const indEnd = document.getElementById("indirectEndTime").value;

      if (hasIndirect && indStart && indEnd) {
        let indStartMin = timeToMinutes(indStart);
        let indEndMin = timeToMinutes(indEnd);
        if (indStartMin < dStartMin) indStartMin += 24 * 60;
        if (indEndMin < indStartMin) indEndMin += 24 * 60;

        indirectMinutes = Math.max(0, indEndMin - indStartMin);
        totalEndMin = Math.max(dEndMin, indEndMin);

        // 中抜け時間（直接終了〜間接開始）
        if (indStartMin > dEndMin) {
          gapMinutes = indStartMin - dEndMin;
        }
        totalBreak += gapMinutes;
        document.getElementById("indirectBadge").innerText = (indirectMinutes / 60).toFixed(1) + "h (" + indirectMinutes + "分)";
      } else {
        document.getElementById("indirectBadge").innerText = "なし";
      }

      // 隠し送信フィールドに反映
      document.getElementById("editStartTime").value = dStart;
      document.getElementById("editEndTime").value = minutesToTime(totalEndMin);
      document.getElementById("editBreakMinutes").value = totalBreak;
      document.getElementById("editDirectMinutes").value = directMinutes;
      document.getElementById("editIndirectMinutes").value = indirectMinutes;

      // サマリ表示更新
      const totalWorkMin = directMinutes + indirectMinutes;
      document.getElementById("summaryTotalWork").innerText = (totalWorkMin / 60).toFixed(1) + "h";
      document.getElementById("summaryDirect").innerText = (directMinutes / 60).toFixed(1) + "h";
      document.getElementById("summaryIndirect").innerText = (indirectMinutes / 60).toFixed(1) + "h";
      document.getElementById("summaryBreak").innerText = "休憩: " + totalBreak + "分" + (gapMinutes > 0 ? " (昼" + dBreak + "分 + 中抜け" + gapMinutes + "分)" : "");
    }

    // 摘要プルダウン変更時の連動入力
    function onAbstractChange() {
      const absId = document.getElementById("editAbstractId").value;
      const indContainer = document.getElementById("indirectInputs");
      const enableInd = document.getElementById("enableIndirect");

      if (absId === "3" || absId === "10") { // 有給 or 休日
        document.getElementById("directStartTime").value = "00:00";
        document.getElementById("directEndTime").value = "00:00";
        document.getElementById("directBreakMinutes").value = 0;
        enableInd.checked = false;
        indContainer.classList.add("hidden");
      } else if (absId === "5") { // 半休
        document.getElementById("directStartTime").value = "09:00";
        document.getElementById("directEndTime").value = "13:00";
        document.getElementById("directBreakMinutes").value = 0;
        enableInd.checked = false;
        indContainer.classList.add("hidden");
      } else if (absId === "2" || absId === "1") { // 休出 or 通常出勤
        document.getElementById("directStartTime").value = "09:00";
        document.getElementById("directEndTime").value = "18:00";
        document.getElementById("directBreakMinutes").value = 60;
      }
      calculateWorkTimes();
    }

    // スマホタブ切替
    function switchTab(tab) {
      const tabs = ['attendance', 'report', 'summary'];
      tabs.forEach(t => {
        const sec = document.getElementById('section-' + t);
        const btn = document.getElementById('tabBtn-' + t);
        if (sec) {
          if (t === tab) sec.classList.remove('hidden');
          else sec.classList.add('hidden');
        }
        if (btn) {
          if (t === tab) btn.className = "flex-1 py-2 rounded-lg bg-sky-600 text-white text-xs font-bold transition";
          else btn.className = "flex-1 py-2 rounded-lg text-slate-400 hover:text-white text-xs font-bold transition";
        }
      });
    }

    function handleResize() {
      if (window.innerWidth >= 768) {
        ['attendance', 'report', 'summary'].forEach(t => {
          const sec = document.getElementById('section-' + t);
          if (sec) sec.classList.remove('hidden');
        });
      }
    }
    window.addEventListener('resize', handleResize);
    handleResize();

    // 月初一括展開
    function applyTemplate() {
      if (!confirm(YEAR_MONTH + " の勤務テンプレートを一括展開しますか？")) return;
      const params = new URLSearchParams({
        actionType: "applyTemplate",
        employeeId: EMPLOYEE_ID,
        yearMonth: YEAR_MONTH
      });
      fetch("sesAttendanceApi.do", { method: "POST", body: params })
        .then(res => res.json())
        .then(data => {
          alert(data.message);
          if (data.success) location.reload();
        });
    }

    // 月次精算確定
    function calculateSettlement() {
      const params = new URLSearchParams({
        actionType: "calculateSettlement",
        employeeId: EMPLOYEE_ID,
        yearMonth: YEAR_MONTH
      });
      fetch("sesAttendanceApi.do", { method: "POST", body: params })
        .then(res => res.json())
        .then(data => {
          alert(data.message + "\n精算判定: " + data.settlementStatus + " (直接時間: " + (data.directMinutes / 60).toFixed(1) + "h)");
          if (data.success) location.reload();
        });
    }

    // 編集モーダル
    function openEditModal(workDate, startTime, endTime, breakMin, abstractId, directMin, indirectMin, remarks, canClear) {
      document.getElementById("modalTitle").innerText = workDate + " の勤怠編集";
      document.getElementById("editWorkDate").value = workDate;
      document.getElementById("editAbstractId").value = abstractId || 1;
      document.getElementById("editRemarks").value = remarks || "";

      const clearBtn = document.getElementById("modalClearBtn");
      if (clearBtn) {
        if (canClear) clearBtn.classList.remove("hidden");
        else clearBtn.classList.add("hidden");
      }

      // 有給・休日の場合
      if (abstractId === 3 || abstractId === 10) {
        document.getElementById("directStartTime").value = "00:00";
        document.getElementById("directEndTime").value = "00:00";
        document.getElementById("directBreakMinutes").value = 0;
        document.getElementById("enableIndirect").checked = false;
        document.getElementById("indirectInputs").classList.add("hidden");
        calculateWorkTimes();
        document.getElementById("editModal").classList.remove("hidden");
        return;
      }

      // 間接業務の有無判定
      const hasInd = (indirectMin !== undefined && indirectMin > 0);
      document.getElementById("enableIndirect").checked = hasInd;
      const indContainer = document.getElementById("indirectInputs");
      if (hasInd) {
        indContainer.classList.remove("hidden");
      } else {
        indContainer.classList.add("hidden");
      }

      // 直接労働の開始・終了時刻の設定
      const startStr = (startTime && startTime.trim().length > 0) ? startTime.substring(0, 5) : "09:00";
      document.getElementById("directStartTime").value = startStr;

      if (hasInd) {
        const dMin = directMin !== undefined ? directMin : 480;
        const dStartM = timeToMinutes(startStr);
        const dEndM = dStartM + dMin + 60; // 昼60分
        document.getElementById("directEndTime").value = minutesToTime(dEndM);
        document.getElementById("directBreakMinutes").value = 60;

        const endStr = (endTime && endTime.trim().length > 0) ? endTime.substring(0, 5) : "20:00";
        const indEndM = timeToMinutes(endStr);
        const indStartM = indEndM - indirectMin;
        document.getElementById("indirectStartTime").value = minutesToTime(indStartM);
        document.getElementById("indirectEndTime").value = endStr;
      } else {
        const endStr = (endTime && endTime.trim().length > 0) ? endTime.substring(0, 5) : "18:00";
        document.getElementById("directEndTime").value = endStr;
        document.getElementById("directBreakMinutes").value = (breakMin !== undefined && breakMin > 0) ? breakMin : 60;
        document.getElementById("indirectStartTime").value = "19:00";
        document.getElementById("indirectEndTime").value = "20:00";
      }

      calculateWorkTimes();
      document.getElementById("editModal").classList.remove("hidden");
    }

    function closeEditModal() {
      document.getElementById("editModal").classList.add("hidden");
    }

    // 勤怠クリア（未入力または休日にリセット）
    function clearAttendance(workDate) {
      if (!confirm(workDate + " の勤怠入力をクリアしますか？\n（未入力または休日の状態に戻ります）")) return;
      const params = new URLSearchParams({
        actionType: "clearAttendance",
        employeeId: EMPLOYEE_ID,
        workDate: workDate
      });
      fetch("sesAttendanceApi.do", { method: "POST", body: params })
        .then(res => res.json())
        .then(data => {
          alert(data.message);
          if (data.success) location.reload();
        });
    }

    function onModalClear() {
      const workDate = document.getElementById("editWorkDate").value;
      if (!workDate) return;
      closeEditModal();
      clearAttendance(workDate);
    }

    // 勤怠保存
    function saveAttendance(e) {
      e.preventDefault();
      const params = new URLSearchParams({
        actionType: "fillAttendance",
        employeeId: EMPLOYEE_ID,
        workDate: document.getElementById("editWorkDate").value,
        abstractId: document.getElementById("editAbstractId").value,
        startTime: document.getElementById("editStartTime").value + ":00",
        endTime: document.getElementById("editEndTime").value + ":00",
        breakMinutes: document.getElementById("editBreakMinutes").value,
        directMinutes: document.getElementById("editDirectMinutes").value,
        indirectMinutes: document.getElementById("editIndirectMinutes").value,
        indirectTask: document.getElementById("editIndirectTask").value,
        remarks: document.getElementById("editRemarks").value
      });

      fetch("sesAttendanceApi.do", { method: "POST", body: params })
        .then(res => res.json())
        .then(data => {
          if (data.success) {
            closeEditModal();
            location.reload();
          } else {
            alert(data.message);
          }
        });
    }

    // 週報提出（3分割）
    function submitWeeklyReport(e) {
      e.preventDefault();
      const params = new URLSearchParams({
        actionType: "submitWeeklyReport",
        employeeId: EMPLOYEE_ID,
        yearWeek: CURRENT_WEEK,
        taskTitle: document.getElementById("taskTitle").value,
        taskProgress: document.getElementById("taskProgress").value,
        personalReflection: document.getElementById("personalReflection").value
      });

      fetch("sesAttendanceApi.do", { method: "POST", body: params })
        .then(res => res.json())
        .then(data => {
          alert(data.message);
          if (data.success) location.reload();
        });
    }

    // 休暇スキップ
    function skipWeeklyReport() {
      const reason = prompt("長期休暇の理由を入力してください（例: ゴールデンウィーク休暇、夏季休暇）:", "長期休暇のため");
      if (reason === null) return;
      const params = new URLSearchParams({
        actionType: "skipWeeklyReport",
        employeeId: EMPLOYEE_ID,
        yearWeek: CURRENT_WEEK,
        reason: reason
      });
      fetch("sesAttendanceApi.do", { method: "POST", body: params })
        .then(res => res.json())
        .then(data => {
          alert(data.message);
          if (data.success) location.reload();
        });
    }

    // 課長所見登録
    function submitSupervisorReview(reportId) {
      const comment = document.getElementById("sectionChiefComment").value;
      if (!comment || !comment.trim()) {
        alert("課長所見を入力してください。");
        return;
      }
      const params = new URLSearchParams({
        actionType: "addSupervisorReview",
        reportId: reportId,
        comment: comment
      });
      fetch("sesAttendanceApi.do", { method: "POST", body: params })
        .then(res => res.json())
        .then(data => {
          alert(data.message);
          if (data.success) location.reload();
        });
    }

    // 所属長最終承認
    function approveFinalReport(reportId) {
      const comment = document.getElementById("deptHeadComment").value;
      const params = new URLSearchParams({
        actionType: "approveFinal",
        reportId: reportId,
        comment: comment || ""
      });
      fetch("sesAttendanceApi.do", { method: "POST", body: params })
        .then(res => res.json())
        .then(data => {
          alert(data.message);
          if (data.success) location.reload();
        });
    }

    // 週報差戻し
    function remandReport(reportId) {
      const reason = prompt("差戻し理由を入力してください:");
      if (!reason || !reason.trim()) return;
      const params = new URLSearchParams({
        actionType: "remandReport",
        reportId: reportId,
        reason: reason.trim()
      });
      fetch("sesAttendanceApi.do", { method: "POST", body: params })
        .then(res => res.json())
        .then(data => {
          alert(data.message);
          if (data.success) location.reload();
        });
    }

    // 管理者共有メモ登録
    function submitPrivateNote(reportId) {
      const note = document.getElementById("privateMgmtNote").value;
      if (!note || !note.trim()) {
        alert("非公開メモを入力してください。");
        return;
      }
      const params = new URLSearchParams({
        actionType: "addPrivateNote",
        reportId: reportId,
        note: note
      });
      fetch("sesAttendanceApi.do", { method: "POST", body: params })
        .then(res => res.json())
        .then(data => {
          alert(data.message);
          if (data.success) location.reload();
        });
    }
  </script>
</body>
</html>
