<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="domain.shain.Shain" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>有給休暇管理 - SES勤怠マネージャー</title>
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
        <a href="sesAttendance.do" class="px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-slate-300 border border-slate-700">
          ‹ 勤怠・週報へ戻る
        </a>
        <h1 class="text-lg md:text-xl font-bold text-white flex items-center gap-2">
          <span>🏖️ 有給休暇管理（管理者用）</span>
        </h1>
      </div>

      <div class="flex items-center gap-2">
        <a href="shainList.do" class="px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-slate-300 border border-slate-700">
          👥 社員一覧へ
        </a>
      </div>
    </div>

    <!-- 一括付与カード（勤続年数基準 & 選択付与） -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <!-- 勤続年数で一括付与 -->
      <div class="panel p-5 space-y-3 border-t-2 border-emerald-500">
        <h2 class="text-sm font-bold text-white flex items-center gap-2">
          <span>🗓️ 勤続年数による法定一括付与</span>
        </h2>
        <p class="text-xs text-slate-400">
          労働基準法および定義済みの付与ルールに基づき、全社員の勤続年数に応じた有給休暇を一括更新・付与します。
        </p>
        <form action="grantLeaveByYear.do" method="post" onsubmit="return confirm('全社員に勤続年数に応じた有給休暇を一括付与しますか？');">
          <button type="submit" class="w-full bg-emerald-600 hover:bg-emerald-500 text-white font-bold py-2.5 rounded-lg text-xs shadow transition">
            全社員へ一括付与を実行
          </button>
        </form>
      </div>

      <!-- 選択して付与 -->
      <div class="panel p-5 space-y-3 border-t-2 border-sky-500">
        <h2 class="text-sm font-bold text-white flex items-center gap-2">
          <span>✨ 選択した社員へ一括付与</span>
        </h2>
        <p class="text-xs text-slate-400">
          下の社員一覧でチェックを入れた社員に対し、指定した日数を付与します。
        </p>
        <form action="grantLeaveSelected.do" method="post" id="grantSelectedForm" class="flex items-center gap-2">
          <input type="number" name="days" min="1" max="40" required placeholder="付与日数 (例: 10)"
                 class="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-xs text-white" />
          <button type="submit" class="whitespace-nowrap px-4 py-2 bg-sky-600 hover:bg-sky-500 text-white font-bold rounded-lg text-xs shadow transition">
            選択者に付与
          </button>
        </form>
      </div>
    </div>

    <!-- 社員一覧テーブル -->
    <div class="panel overflow-hidden space-y-3 p-4">
      <div class="flex items-center justify-between">
        <h2 class="text-sm font-bold text-white">全社員の有給休暇状況</h2>
        <span class="text-xs text-slate-400"><%= shainList != null ? shainList.size() : 0 %>名</span>
      </div>

      <div class="overflow-x-auto">
        <table class="w-full text-xs text-left border-collapse" id="employeeTable">
          <thead class="bg-slate-900 text-slate-400 border-b border-slate-800">
            <tr>
              <th class="py-2.5 px-3 w-10 text-center">
                <input type="checkbox" id="selectAll" form="grantSelectedForm" onclick="toggleSelectAll(this)" class="rounded bg-slate-950 border-slate-700 text-sky-500" />
              </th>
              <th class="py-2.5 px-3">社員ID</th>
              <th class="py-2.5 px-3">氏名</th>
              <th class="py-2.5 px-3">勤続年数</th>
              <th class="py-2.5 px-3">有休残日数</th>
              <th class="py-2.5 px-3 text-right">個別直接更新</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-800 text-slate-300">
            <% if (shainList != null && !shainList.isEmpty()) { %>
              <% for (Shain s : shainList) { %>
                <tr class="hover:bg-slate-800/40">
                  <td class="py-2.5 px-3 text-center">
                    <input type="checkbox" name="selected_ids" value="<%= s.getId() %>" form="grantSelectedForm" class="emp-check rounded bg-slate-950 border-slate-700 text-sky-500" />
                  </td>
                  <td class="py-2.5 px-3 font-mono font-medium text-white"><%= s.getId() %></td>
                  <td class="py-2.5 px-3 font-semibold text-white"><%= s.getName() %></td>
                  <td class="py-2.5 px-3 font-mono"><%= s.getYearsOfService() %> 年</td>
                  <td class="py-2.5 px-3 font-mono font-bold text-emerald-400"><%= s.getPaidLeaveDays() %> 日</td>
                  <td class="py-2.5 px-3 text-right">
                    <div class="inline-flex items-center gap-1.5 justify-end">
                      <input type="number" id="input_<%= s.getId() %>" min="0" max="60" value="<%= s.getPaidLeaveDays() %>"
                             class="w-16 bg-slate-950 border border-slate-700 rounded px-2 py-1 text-xs text-white text-right font-mono" />
                      <button onclick="updateIndividualLeave(<%= s.getId() %>)"
                              class="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-sky-400 font-bold border border-slate-700">
                        更新
                      </button>
                    </div>
                  </td>
                </tr>
              <% } %>
            <% } else { %>
              <tr>
                <td colspan="6" class="py-8 text-center text-slate-500">社員データがありません。</td>
              </tr>
            <% } %>
          </tbody>
        </table>
      </div>
    </div>

  </div>

  <script>
    function toggleSelectAll(master) {
      document.querySelectorAll('.emp-check').forEach(cb => {
        cb.checked = master.checked;
      });
    }

    function updateIndividualLeave(employeeId) {
      const days = document.getElementById("input_" + employeeId).value;
      if (days === "" || isNaN(days)) {
        alert("有効な日数を入力してください。");
        return;
      }
      const params = new URLSearchParams({
        id: employeeId,
        individual_days: days
      });
      fetch("updateLeave.do", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: params
      }).then(res => {
        if (res.ok) {
          alert("社員ID " + employeeId + " の有休日数を " + days + " 日に更新しました。");
          location.reload();
        } else {
          alert("更新に失敗しました。");
        }
      }).catch(err => {
        alert("通信エラー: " + err);
      });
    }
  </script>
</body>
</html>
