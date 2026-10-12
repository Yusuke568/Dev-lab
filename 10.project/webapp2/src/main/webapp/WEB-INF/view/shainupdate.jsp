<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="domain.shain.Shain" %>
<%@ page import="domain.shain.Classmaster" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>社員情報更新 - SES勤怠マネージャー</title>
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
<body class="min-h-screen flex items-center justify-center p-4">
  <%
    Shain shain = (Shain) request.getAttribute("shain");
    List<Classmaster> jobList = (List<Classmaster>) request.getAttribute("jobList");
  %>

  <div class="panel w-full max-w-lg p-6 md:p-8 space-y-6 shadow-2xl">
    <div class="border-b border-slate-800 pb-3 flex items-center justify-between">
      <div>
        <h1 class="text-lg font-bold text-white"><%= shain != null ? shain.getName() : "" %> さんの情報更新</h1>
        <p class="text-xs text-slate-400">社員属性や役職・有休日数を変更します。</p>
      </div>
      <a href="shainList.do" class="text-xs text-slate-400 hover:text-white">‹ 社員一覧へ戻る</a>
    </div>

    <form action="shainUpdateExecute.do" method="post" class="space-y-4 text-xs">
      <input type="hidden" name="id" value="<%= shain != null ? shain.getId() : "" %>" />

      <div>
        <label class="block text-slate-400 mb-1 font-semibold">社員ID</label>
        <input type="text" value="<%= shain != null ? shain.getId() : "" %>" readonly
               class="w-full bg-slate-950 border border-slate-800 rounded-lg p-2.5 text-white font-mono opacity-80" />
      </div>

      <div class="grid grid-cols-2 gap-3">
        <div>
          <label class="block text-slate-300 mb-1 font-semibold">氏名</label>
          <input type="text" name="name" value="<%= shain != null ? shain.getName() : "" %>" required
                 class="w-full bg-slate-950 border border-slate-700 rounded-lg p-2.5 text-white" />
        </div>
        <div>
          <label class="block text-slate-300 mb-1 font-semibold">カナ氏名</label>
          <input type="text" name="namekana" value="<%= shain != null ? shain.getNamekana() : "" %>" required
                 class="w-full bg-slate-950 border border-slate-700 rounded-lg p-2.5 text-white" />
        </div>
      </div>

      <div class="grid grid-cols-2 gap-3">
        <div>
          <label class="block text-slate-300 mb-1 font-semibold">性別</label>
          <select name="gender" class="w-full bg-slate-950 border border-slate-700 rounded-lg p-2.5 text-white">
            <option value="男" <%= (shain != null && "男".equals(shain.getGender())) ? "selected" : "" %>>男</option>
            <option value="女" <%= (shain != null && "女".equals(shain.getGender())) ? "selected" : "" %>>女</option>
          </select>
        </div>
        <div>
          <label class="block text-slate-300 mb-1 font-semibold">入社年</label>
          <input type="number" name="entryyear" value="<%= shain != null ? shain.getEntryyear() : 2024 %>" required
                 class="w-full bg-slate-950 border border-slate-700 rounded-lg p-2.5 text-white font-mono" />
        </div>
      </div>

      <div class="grid grid-cols-2 gap-3">
        <div>
          <label class="block text-slate-300 mb-1 font-semibold">役職 / 権限</label>
          <select name="jobclass" class="w-full bg-slate-950 border border-slate-700 rounded-lg p-2.5 text-white font-semibold">
            <% if (jobList != null) { %>
              <% for (Classmaster job : jobList) { %>
                <option value="<%= job.getName() %>" <%= (shain != null && job.getName().equals(shain.getJobclass())) ? "selected" : "" %>>
                  <%= job.getName() %>
                </option>
              <% } %>
            <% } %>
          </select>
        </div>
        <div>
          <label class="block text-slate-300 mb-1 font-semibold">有給付与残日数</label>
          <input type="number" name="paidLeaveDays" value="<%= shain != null ? shain.getPaidLeaveDays() : 20 %>" required min="0" max="60"
                 class="w-full bg-slate-950 border border-slate-700 rounded-lg p-2.5 text-white font-mono" />
        </div>
      </div>

      <div class="flex items-center justify-end gap-2 pt-3 border-t border-slate-800">
        <a href="shainList.do" class="px-4 py-2.5 rounded-lg bg-slate-800 text-slate-300 font-semibold hover:bg-slate-700">キャンセル</a>
        <button type="submit" class="px-5 py-2.5 rounded-lg bg-sky-600 hover:bg-sky-500 font-bold text-white shadow">
          保存する
        </button>
      </div>
    </form>
  </div>
</body>
</html>