<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="domain.shain.Shain" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>社員・権限管理 - SES勤怠マネージャー</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tailwind.min.css">
  <style>
    body {
      background-color: #0b0f19;
      color: #f1f5f9;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Hiragino Sans", sans-serif;
    }
    .panel {
      background-color: #131b2e;
      border: 1px solid #1e293b;
      border-radius: 0.75rem;
    }
  </style>
</head>
<body class="min-h-screen p-4 md:p-6">
  <%
    List<Shain> shainList = (List<Shain>) request.getAttribute("shainList");
  %>

  <div class="max-w-6xl mx-auto space-y-6">

    <!-- ナビゲーションバー -->
    <div class="flex flex-wrap items-center justify-between gap-3 border-b border-slate-800 pb-4">
      <div class="flex items-center gap-3">
        <a href="sesAttendance.do" class="px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-slate-300 border border-slate-700 flex items-center gap-1.5">
          <span>‹ 勤怠・週報へ戻る</span>
        </a>
        <h1 class="text-lg md:text-xl font-bold text-white flex items-center gap-2">
          <span>👥 社員・権限管理</span>
        </h1>
      </div>

      <div class="flex items-center gap-2">
        <a href="paidLeaveAdmin.do" class="px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-slate-300 border border-slate-700">
          🏖️ 有休管理画面
        </a>
        <a href="shainInsertForm.do" class="px-3.5 py-1.5 rounded-lg bg-sky-600 hover:bg-sky-500 text-xs font-bold text-white shadow shadow-sky-600/30">
          + 新規社員を登録（入社）
        </a>
      </div>
    </div>

    <!-- 検索 & フィルターパネル -->
    <div class="panel p-4 space-y-3">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <!-- 検索バー -->
        <div class="w-full md:w-72">
          <input type="text" id="searchInput" oninput="filterTable()" placeholder="社員名・カナで検索..."
                 class="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-sky-500" />
        </div>

        <!-- 階級・権限別フィルター -->
        <div class="flex flex-wrap items-center gap-1 text-xs">
          <span class="text-slate-400 mr-1 text-[11px]">階級:</span>
          <button onclick="setRoleFilter('all')" class="role-btn px-2.5 py-1 rounded bg-sky-600 text-white font-bold" data-role="all">すべて</button>
          <button onclick="setRoleFilter('一般')" class="role-btn px-2.5 py-1 rounded bg-slate-800 text-slate-300 hover:text-white" data-role="一般">一般</button>
          <button onclick="setRoleFilter('課長')" class="role-btn px-2.5 py-1 rounded bg-slate-800 text-slate-300 hover:text-white" data-role="課長">課長</button>
          <button onclick="setRoleFilter('部長')" class="role-btn px-2.5 py-1 rounded bg-slate-800 text-slate-300 hover:text-white" data-role="部長">部長</button>
        </div>

        <!-- 在籍/退職フィルター -->
        <div class="flex items-center gap-1 text-xs">
          <span class="text-slate-400 mr-1 text-[11px]">状態:</span>
          <select id="statusFilter" onchange="filterTable()" class="bg-slate-950 border border-slate-700 rounded-lg px-2 py-1 text-xs text-white">
            <option value="all">すべて</option>
            <option value="active" selected>在籍中のみ</option>
            <option value="retired">退職済のみ</option>
          </select>
        </div>
      </div>
    </div>

    <!-- 社員テーブル -->
    <div class="panel overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-xs text-left border-collapse" id="shainTable">
          <thead class="bg-slate-900 text-slate-400 border-b border-slate-800">
            <tr>
              <th class="py-3 px-4">社員ID</th>
              <th class="py-3 px-3">氏名</th>
              <th class="py-3 px-3">カナ</th>
              <th class="py-3 px-3">階級 / 権限</th>
              <th class="py-3 px-3">入社年</th>
              <th class="py-3 px-3">有休残日数</th>
              <th class="py-3 px-3">状態</th>
              <th class="py-3 px-4 text-right">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-800 text-slate-300" id="shainTbody">
            <% if (shainList != null && !shainList.isEmpty()) { %>
              <% for (Shain s : shainList) { %>
                <tr class="shain-row hover:bg-slate-800/40"
                    data-name="<%= s.getName() != null ? s.getName() : "" %>"
                    data-kana="<%= s.getNamekana() != null ? s.getNamekana() : "" %>"
                    data-role="<%= s.getJobclass() != null ? s.getJobclass() : "" %>"
                    data-status="<%= s.isEnabled() ? "active" : "retired" %>">
                  <td class="py-3 px-4 font-mono font-medium text-white"><%= s.getId() %></td>
                  <td class="py-3 px-3 font-semibold text-white"><%= s.getName() %></td>
                  <td class="py-3 px-3 text-slate-400"><%= s.getNamekana() != null ? s.getNamekana() : "" %></td>
                  <td class="py-3 px-3">
                    <%
                      String jc = s.getJobclass() != null ? s.getJobclass() : "一般";
                      String badgeClass = "bg-slate-800 text-slate-300";
                      if (jc.contains("部長")) badgeClass = "bg-rose-500/20 text-rose-300 border border-rose-500/30 font-bold";
                      else if (jc.contains("課長") || jc.contains("リーダー")) badgeClass = "bg-amber-500/20 text-amber-300 border border-amber-500/30 font-bold";
                      else badgeClass = "bg-sky-500/20 text-sky-300";
                    %>
                    <span class="px-2 py-0.5 rounded text-[11px] <%= badgeClass %>">
                      <%= jc %>
                    </span>
                  </td>
                  <td class="py-3 px-3 font-mono"><%= s.getEntryyear() %>年</td>
                  <td class="py-3 px-3 font-mono font-bold text-emerald-400"><%= s.getPaidLeaveDays() %>日</td>
                  <td class="py-3 px-3">
                    <% if (s.isEnabled()) { %>
                      <span class="px-1.5 py-0.5 rounded text-[10px] bg-emerald-500/20 text-emerald-300 font-bold">在籍</span>
                    <% } else { %>
                      <span class="px-1.5 py-0.5 rounded text-[10px] bg-slate-800 text-slate-500">退職済</span>
                    <% } %>
                  </td>
                  <td class="py-3 px-4 text-right space-x-1">
                    <a href="shainUpdateForm.do?id=<%= s.getId() %>"
                       class="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 font-semibold border border-slate-700">
                      変更
                    </a>
                    <a href="shainDeleteForm.do?id=<%= s.getId() %>"
                       class="px-2.5 py-1 rounded bg-rose-950/40 hover:bg-rose-900/60 text-rose-400 font-semibold border border-rose-800/40">
                      退職/削除
                    </a>
                  </td>
                </tr>
              <% } %>
            <% } else { %>
              <tr>
                <td colspan="8" class="py-12 text-center text-slate-500">社員データがありません。</td>
              </tr>
            <% } %>
          </tbody>
        </table>
      </div>
    </div>

  </div>

  <script>
    let currentRole = 'all';

    function setRoleFilter(role) {
      currentRole = role;
      document.querySelectorAll('.role-btn').forEach(btn => {
        if (btn.getAttribute('data-role') === role) {
          btn.className = "role-btn px-2.5 py-1 rounded bg-sky-600 text-white font-bold";
        } else {
          btn.className = "role-btn px-2.5 py-1 rounded bg-slate-800 text-slate-300 hover:text-white";
        }
      });
      filterTable();
    }

    function filterTable() {
      const keyword = (document.getElementById('searchInput').value || '').trim().toLowerCase();
      const status = document.getElementById('statusFilter').value;
      const rows = document.querySelectorAll('.shain-row');

      rows.forEach(row => {
        const name = (row.getAttribute('data-name') || '').toLowerCase();
        const kana = (row.getAttribute('data-kana') || '').toLowerCase();
        const role = row.getAttribute('data-role') || '';
        const rowStatus = row.getAttribute('data-status') || '';

        const matchKeyword = !keyword || name.includes(keyword) || kana.includes(keyword);
        const matchRole = currentRole === 'all' || role.includes(currentRole);
        const matchStatus = status === 'all' || rowStatus === status;

        if (matchKeyword && matchRole && matchStatus) {
          row.style.display = '';
        } else {
          row.style.display = 'none';
        }
      });
    }

    // 初期フィルタリング（在籍中のみ）
    document.addEventListener('DOMContentLoaded', () => {
      filterTable();
    });
  </script>
</body>
</html>
