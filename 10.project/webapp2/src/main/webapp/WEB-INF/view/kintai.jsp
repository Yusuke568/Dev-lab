<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
	<%@page import="java.util.ArrayList"%>
<%@page import="beans.*"%>
<!DOCTYPE html>
<html lang="ja">
  <head>
    <meta charset="UTF-8" />
    <title>勤怠管理システム</title>
    <style>
      body {
        font-family: 'Segoe UI', 'Noto Sans JP', sans-serif;
        background-color: #f3f6fb;
        padding: 20px;
      }
      h1 {
        text-align: center;
        color: #2c3e50;
      }
      .buttons {
        text-align: center;
        margin-bottom: 20px;
      }
      button {
        margin: 5px;
        padding: 8px 12px;
        font-size: 13px;
        border: none;
        background-color: #3498db;
        color: white;
        border-radius: 4px;
        cursor: pointer;
      }
      button:disabled {
        background-color: #aaa;
      }
      table {
        width: 100%;
        table-layout: fixed;
        border-collapse: collapse;
        background-color: white;
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
        font-size: 13px;
      }
      th,
      td {
        border: 1px solid #ccc;
        padding: 6px;
        text-align: center;
        word-wrap: break-word;
      }
      th:nth-child(1),
      td:nth-child(1) {
        width: 100px;
      } /* 日付 */
      th:nth-child(2),
      td:nth-child(2) {
        width: 70px;
      } /* 出勤 */
      th:nth-child(3),
      td:nth-child(3) {
        width: 70px;
      } /* 退勤 */
      th:nth-child(4),
      td:nth-child(4) {
        width: 90px;
      } /* 勤務区分 */
      th {
        background-color: #2c3e50;
        color: white;
      }
      .saturday {
        background-color: #e3f2fd;
      }
      .sunday {
        background-color: #ffebee;
      }
      .holiday {
        background-color: #fff8dc;
      }
      .today {
        border: 2px solid #27ae60;
      }
      select {
        width: 100%;
        padding: 4px;
        font-size: 13px;
      }
    </style>
  </head>
  <body>
    <h1>勤怠管理システム</h1>

    <div class="buttons">
      <button id="clock-in-btn" onclick="recordAttendance('出勤')">出勤</button>
      <button id="clock-out-btn" onclick="recordAttendance('退勤')">
        退勤
      </button>
      <button onclick="recordAttendance('有給')">有給</button>
      <button onclick="recordAttendance('欠勤')">欠勤</button>
      <p id="overtime-summary">時間外労働: 0時間0分</p>
    </div>

    <table id="calendar-log">
      <thead>
        <tr>
          <th>日付</th>
          <th>出勤</th>
          <th>退勤</th>
          <th>勤務区分</th>
        </tr>
      </thead>
      <tbody></tbody>
    </table>

    <script>
      const workStart = '09:00';
      const workEnd = '18:00';
      const attendanceRecords = [];
      const statusOptions = [
        '通常勤務',
        '有給',
        '欠勤',
        '振替',
        '時短',
        'その他',
      ];
      const holidays = { '2025/08/11': '山の日' };

      function generateCalendar(year, month) {
        const tbody = document.querySelector('#calendar-log tbody');
        tbody.innerHTML = '';
        const todayStr = new Date().toLocaleDateString();

        const date = new Date(year, month, 1);
        while (date.getMonth() === month) {
          const row = tbody.insertRow();
          const dateStr = date.toLocaleDateString();
          row.dataset.date = dateStr;

          const day = date.getDay();
          row.insertCell(0).innerText = dateStr;
          row.insertCell(1).innerText = '—';
          row.insertCell(2).innerText = '—';
          row.insertCell(3).innerText = '';

          if (day === 0) row.classList.add('sunday');
          if (day === 6) row.classList.add('saturday');
          if (holidays[dateStr]) row.classList.add('holiday');
          if (dateStr === todayStr) row.classList.add('today');

          date.setDate(date.getDate() + 1);
        }
      }

      function getMinutes(t) {
        const [h, m] = t.split(':').map(Number);
        return h * 60 + m;
      }

      function calculateOvertime(start, end) {
        const s = getMinutes(start);
        const e = getMinutes(end);
        const rS = getMinutes(workStart);
        const rE = getMinutes(workEnd);
        return (rS > s ? rS - s : 0) + (e > rE ? e - rE : 0);
      }

      function updateOvertimeSummary() {
        let total = 0;
        attendanceRecords.forEach((r) => {
          if (r.start && r.end) total += calculateOvertime(r.start, r.end);
        });
        document.getElementById(
          'overtime-summary'
        ).innerText = `時間外労働: ${Math.floor(total / 60)}時間${
          total % 60
        }分`;
      }

      function updateButtonState() {
        const today = new Date().toLocaleDateString();
        const rec = attendanceRecords.find((r) => r.date === today);
        document.getElementById('clock-in-btn').disabled = rec?.start;
        document.getElementById('clock-out-btn').disabled = rec?.end;
      }

      function updateRecord(date, key, value, status = '通常勤務') {
        let rec = attendanceRecords.find((r) => r.date === date);
        if (!rec) {
          rec = { date, start: null, end: null, status };
          attendanceRecords.push(rec);
        }
        rec[key] = value;
        if (key === 'status') rec.status = status;
      }

      function renderStatusCell(cell, currentValue, date) {
        cell.innerHTML = '';
        const select = document.createElement('select');
        statusOptions.forEach((opt) => {
          const option = document.createElement('option');
          option.value = opt;
          option.text = opt;
          if (opt === currentValue) option.selected = true;
          select.appendChild(option);
        });
        select.addEventListener('change', () => {
          const newVal = select.value;
          cell.innerText = newVal;
          const rec = attendanceRecords.find((r) => r.date === date);
          if (rec) rec.status = newVal;
          updateOvertimeSummary();
          updateButtonState();
        });
        cell.appendChild(select);
      }

      function recordAttendance(type) {
        const now = new Date();
        const dateStr = now.toLocaleDateString();
        const timeStr = now.toLocaleTimeString([], {
          hour: '2-digit',
          minute: '2-digit',
        });

        const rows = document.querySelectorAll('#calendar-log tbody tr');
        for (const row of rows) {
          if (row.dataset.date === dateStr) {
            if (type === '出勤') {
              row.cells[1].innerText = timeStr;
              updateRecord(dateStr, 'start', timeStr);
            } else if (type === '退勤') {
              row.cells[2].innerText = timeStr;
              updateRecord(dateStr, 'end', timeStr);
            } else {
              row.cells[3].innerText = type;
              updateRecord(dateStr, 'status', null, type);
            }
            break;
          }
        }
        updateOvertimeSummary();
        updateButtonState();
      }

      document.addEventListener('DOMContentLoaded', () => {
        const table = document.getElementById('calendar-log');

        table.addEventListener('click', (e) => {
          const cell = e.target;
          if (cell.tagName === 'TD' && cell.cellIndex >= 1) {
            const row = cell.parentElement;
            const date = row.dataset.date;

            if (cell.cellIndex === 3) {
              // 勤務区分セル → プルダウン表示
              const rec = attendanceRecords.find((r) => r.date === date);
              const currentVal = rec?.status || '';
              renderStatusCell(cell, currentVal, date);
            } else {
              // 出退勤セル → 直接編集
              const original = cell.innerText;
              const input = document.createElement('input');
              input.type = 'text';
              input.value = original;
              cell.innerText = '';
              cell.appendChild(input);
              input.focus();

              input.addEventListener('blur', () => {
                const newVal = input.value || original;
                cell.innerText = newVal;
                const rec = attendanceRecords.find((r) => r.date === date);
                if (rec) {
                  if (cell.cellIndex === 1) rec.start = newVal;
                  if (cell.cellIndex === 2) rec.end = newVal;
                }
                updateOvertimeSummary();
                updateButtonState();
              });

              input.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') input.blur();
              });
            }
          }
        });

        const today = new Date();
        generateCalendar(today.getFullYear(), today.getMonth());
        updateButtonState();
      });
    </script>
  </body>
</html>
