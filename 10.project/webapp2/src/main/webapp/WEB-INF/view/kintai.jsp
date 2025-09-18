<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
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
  background-color: #ccc;
  color: #666;
  cursor: not-allowed;
  opacity: 0.6;
  border: 1px solid #999;
}

table {
	width: 100%;
	table-layout: fixed;
	border-collapse: collapse;
	background-color: white;
	box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
	font-size: 13px;
}

th, td {
	border: 1px solid #ccc;
	padding: 6px;
	text-align: center;
	word-wrap: break-word;
}

th:nth-child(1), td:nth-child(1) {
	width: 10px;
} /* 日付 */
th:nth-child(2), td:nth-child(2) {
	width: 10px;
} /* 出勤 */
th:nth-child(3), td:nth-child(3) {
	width: 30px;
} /* 退勤 */
th:nth-child(4), td:nth-child(4) {
	width: 30px;
<<<<<<< HEAD
}
 /* 退勤 */
th:nth-child(5), td:nth-child(4) {
	width: 30px;
} 
 /* 勤務区分 */
th:nth-child(6), td:nth-child(5){
	width: 30px;
}
 /*　備考欄 */
th:nth-child(7), td:nth-child(6) {
	width: 50px;
=======
} /* 勤務区分 */
th:nth-child(5), td:nth-child(5){
	width: 30px;
}
 /*　備考欄 */
th:nth-child(6), td:nth-child(6) {
	width: 70px;
>>>>>>> ef369d98792231d78d6225993f704111cea9ae00
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
  background-color: #dfffe0; /* やさしい緑系 */
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
		<p>リストサイズ: ${fn:length(calendarBeanList)}</p>
		<div class="buttons">
			<button id="clock-in-btn" onclick="recordAttendance('出勤')">出勤</button>
			<button id="clock-out-btn" onclick="recordAttendance('退勤')">退勤</button>
			<p id="overtime-summary">時間外労働: 0時間0分</p>
		</div>
<<<<<<< HEAD
		
		<div class="save-area">
  <button id="save-btn" onclick="saveAttendance()">保存</button>
</div>
=======
>>>>>>> ef369d98792231d78d6225993f704111cea9ae00
	
		<table id="calendar-log">
			<thead>
				<tr>
					<th>日付</th>
					<th>曜日</th>
					<th>出勤</th>
					<th>退勤</th>
<<<<<<< HEAD
					<th>時間外</th>
=======
>>>>>>> ef369d98792231d78d6225993f704111cea9ae00
					<th>勤務区分</th>
					<th>備考欄</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="calendarbean" items="${calendarBeanList}">
					<tr data-date="${calendarbean.kintaidate}">
						<td>${calendarbean.kintaidate}</td>
						<td>${calendarbean.week}</td>
<<<<<<< HEAD
						<td data-form="${calendarbean.kintaifrom }">${fn:substring(calendarbean.kintaifrom,0,5) }</td>
						<td data-to="${calendarbean.kintaito }">${fn:substring(calendarbean.kintaito,0,5) }</td>
						<td data-jikangai="${calendarbean.jikangai}">${calendarbean.jikangai}</td>
=======
						<td data-form="${calendarbean.kintaifrom }">${calendarbean.kintaifrom }</td>
						<td data-to="${calendarbean.kintaito }">${calendarbean.kintaito }</td>
>>>>>>> ef369d98792231d78d6225993f704111cea9ae00
						<td><select name="status">
								<c:forEach var="opt" items="${statusOptions}">
									<option value="${opt}" ${opt == currentStatus ? 'selected' : ''}>${opt}</option>
								</c:forEach>
						</select></td>
<<<<<<< HEAD
					<td> ${calendarbean.memo}</td>
=======
					<td ${calendarbean.memo}></td>
>>>>>>> ef369d98792231d78d6225993f704111cea9ae00
					</tr>
				</c:forEach>
		</tbody>
		</table>
			<select id="status-template" style="display: none;">
				<c:forEach var="opt" items="${statusOptions}">
					<option value="${opt}">${opt}</option>
				</c:forEach>
			</select>
	<script>

<<<<<<< HEAD
	function saveAttendance() {
		  const records = collectAttendanceRecords();

		  

		  fetch("/webapp2/Kintaiinsert", {
		    method: "POST",
		    headers: {
		      "Content-Type": "application/json"
		    },
		    body: JSON.stringify(records)
		  })
		  .then(res => {
		    if (res.ok) {
		      alert("保存しました！");
		    } else {
		      alert("保存に失敗しました…");
		    }
		  });
	}
	
=======
>>>>>>> ef369d98792231d78d6225993f704111cea9ae00
	
      const workStart = '09:00';
      const workEnd = '18:00';
      const attendanceRecords = [];
      const rows = document.querySelectorAll('#calendar-log tbody tr');

      function collectAttendanceRecords() {
    	  const records = [];
<<<<<<< HEAD
    	  const rows = document.querySelectorAll("#calendar-log tbody tr");

    	  rows.forEach(row => {
    	    const kintaidate = row.dataset.date;
    	    const week = row.querySelector("td:nth-child(2)").textContent.trim();
    	    const kintaifrom = row.querySelector("td:nth-child(3)").dataset.form;
    	    const kintaito = row.querySelector("td:nth-child(4)").dataset.to;
    	    const jikangai = row.querySelector("td:nth-child(5)").dataset.jikangai;
    	    const tekiyoukbn = row.querySelector("select[name='status']").value;
    	    const memo = row.querySelector("td:nth-child(7)").textContent.trim();

    	    records.push({
    	      kintaidate,
    	      week,
    	      kintaifrom,
    	      kintaito,
    	      jikangai,
    	      tekiyoukbn,
    	      memo
    	    });
=======

    	  rows.forEach((row) => {
    	    const date = row.dataset.date;
    	    const week = row.cells[1].innerText.trim();
    	    const start = row.cells[2].innerText.trim();
    	    const end = row.cells[3].innerText.trim();
    	    const memo = row.cells[4].innerText.trim();

    	    records.push({ date, week, start, end, memo });
>>>>>>> ef369d98792231d78d6225993f704111cea9ae00
    	  });

    	  return records;
    	}
    	      

      //TODO:外部から参照する
      const holidays = ["2025-09-15", "2025-09-23"];

      //スケジュールの色付
      document.addEventListener('DOMContentLoaded', () => {
        const today = new Date().toISOString().slice(0, 10);

        rows.forEach(row => {
          const date = row.dataset.date;
          const weekText = row.cells[1].innerText.trim();

          if (date === today) {
            row.classList.add('today');
          }

          if (weekText === "SATURDAY") {
            row.classList.add('saturday');
          } else if (weekText === "SUNDAY") {
            row.classList.add('sunday');
          }

          if (holidays.includes(date)) {
            row.classList.add('holiday');
          }
        });
      });


      //時間を分に変換
      function getMinutes(t) {
        const [h, m] = t.split(':').map(Number);
        return h * 60 + m;
      }

      //時間外を分として算出
      function calculateOvertime(start, end) {
        const s = getMinutes(start);
        const e = getMinutes(end);
        const rS = getMinutes(workStart);
        const rE = getMinutes(workEnd);
        return (rS > s ? rS - s : 0) + (e > rE ? e - rE : 0);
      }

      function updateOvertimeSummary() {
    	  let total = 0;
    	  const attendanceRecords = collectAttendanceRecords();

    	  attendanceRecords.forEach((r) => {
    	    if (r.start && r.end) {
    	      total += calculateOvertime(r.start, r.end);
    	    }
    	  });

    	  const summaryEl = document.getElementById('overtime-summary');
    	  if (summaryEl) {
    	    const hours = Math.floor(total / 60);
    	    const minutes = total % 60;
    	    summaryEl.innerText = "時間外労働:" + hours + "時間" + minutes + "分";
    	  } else {
    	    console.warn("overtime-summary 要素が見つかりませんでした！");
    	  }
    	}

<<<<<<< HEAD
      function updateOvertimePerRow() {
    	  rows.forEach(row => {
    	    const start = row.cells[2].innerText.trim();
    	    const end = row.cells[3].innerText.trim();
    	    const overtimeCell = row.cells[4];

    	    if (start && end) {
    	      const overtimeMinutes = calculateOvertime(start, end);
    	      const hours = Math.floor(overtimeMinutes / 60);
    	      const minutes = overtimeMinutes % 60;
    	      overtimeCell.innerText = String(hours).padStart(2,'0') + ":" + String(minutes).padStart(2,'0');
    	    } else {
    	      overtimeCell.innerText = '';
    	    }
    	  });
    	}

=======
>>>>>>> ef369d98792231d78d6225993f704111cea9ae00
      //one click
      function updateButtonState() {
    	  const today = new Date().toISOString().slice(0, 10); 
    	  const attendanceRecords = collectAttendanceRecords();
    	  const rec = attendanceRecords.find((r) => r.date === today);

    	  const clockInBtn = document.getElementById('clock-in-btn');
    	  const clockOutBtn = document.getElementById('clock-out-btn');

    	  const isClockedIn = !!rec?.start;
    	  const isClockedOut = !!rec?.end;

    	  // 出勤ボタンの状態
    	  clockInBtn.disabled = isClockedIn;
    	  clockInBtn.innerText = isClockedIn ? "出勤済み" : "出勤";
    	  clockInBtn.title = isClockedIn ? "すでに出勤しています" : "出勤時間を記録します";

    	  // 退勤ボタンの状態
    	  clockOutBtn.disabled = isClockedOut;
    	  clockOutBtn.innerText = isClockedOut ? "退勤済み" : "退勤";
    	  clockOutBtn.title = isClockedOut ? "すでに退勤しています" : "退勤時間を記録します";

    	  console.log("attendanceRecords:", attendanceRecords);
    	  console.log("today:", today);
    	  console.log("rec:", rec);
<<<<<<< HEAD

    	  updateOvertimePerRow();
=======
>>>>>>> ef369d98792231d78d6225993f704111cea9ae00
    	}

      //指定した任意のレコードを更新する。
      function updateRecord(date, key, value, status = '通常勤務') {
    	  const attendanceRecords = collectAttendanceRecords();
        let rec = attendanceRecords.find((r) => r.date === date);
        if (!rec) {
      	  console.log("attendanceRecords:", attendanceRecords);
          return;
        }
        rec[key] = value;
        if (key === 'status') rec.status = status;
  	  console.log("attendanceRecords:", attendanceRecords);
      }		

      function renderStatusCell(cell, currentValue, date) {
    	  cell.innerHTML = '';
    	  const attendanceRecords = collectAttendanceRecords();

    	  // テンプレートから select を複製
    	  const template = document.getElementById('status-template');
    	  const select = template.cloneNode(true);
    	  select.style.display = 'inline'; // 表示する

    	  // 初期値を設定
    	  select.value = currentValue || "通常勤務";

    	  select.addEventListener('change', () => {
    	    const newVal = select.value;
    	    cell.innerText = newVal;

    	    const rec = attendanceRecords.find((r) => r.date === date);
    	    if (rec) rec.status = newVal;

    	    updateOvertimeSummary();
    	    updateButtonState();
    	  });

    	  cell.appendChild(select);

      	  console.log("attendanceRecords:", attendanceRecords);
    	}

      function recordAttendance(type) {
        const now = new Date();
        const dateStr = new Date().toISOString().slice(0, 10); // "2025-09-04"

        const timeStr = now.toLocaleTimeString([], {
          hour: '2-digit',
          minute: '2-digit',
        });

        for (const row of rows) {
          if (row.dataset.date === dateStr) {
            if (type === '出勤') {
              row.cells[2].innerText = timeStr; //勤務時間
<<<<<<< HEAD
              row.cells[2].dataset.form = timeStr; // ← これも追加！
              updateRecord(dateStr, 'kintaifrom', timeStr); // ← 修正！
            } else if (type === '退勤') {
              row.cells[3].innerText = timeStr; //退勤時間
              row.cells[3].dataset.to = timeStr; // ← これも追加！
              updateRecord(dateStr, 'kintaito', timeStr); // ← 修正！
            } 
            else if (type === '備考欄') {
                row.cells[6].innerText = timeStr; //退勤時間
                row.cells[6].dataset.to = timeStr; // ← これも追加！
                updateRecord(dateStr, 'memo', timeStr); // ← 修正！
              } else {
              row.cells[5].innerText = type; //勤務区分
=======
              updateRecord(dateStr, 'start', timeStr);
            } else if (type === '退勤') {
              row.cells[3].innerText = timeStr; //退勤時間
              updateRecord(dateStr, 'end', timeStr);
            } else {
              row.cells[4].innerText = type; //勤務区分
>>>>>>> ef369d98792231d78d6225993f704111cea9ae00
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
        const attendanceRecords = collectAttendanceRecords();

        table.addEventListener('click', (e) => {
          const cell = e.target;
          if (cell.tagName === 'TD' && cell.cellIndex >= 1) {
            const row = cell.parentElement;
            const date = row.dataset.date;

                
            
<<<<<<< HEAD
            if (cell.cellIndex === 5) {
=======
            if (cell.cellIndex === 4) {
>>>>>>> ef369d98792231d78d6225993f704111cea9ae00
              // 勤務区分セル → プルダウン表示
              const rec = attendanceRecords.find((r) => r.date === date);
              const currentVal = rec?.status || '';
              renderStatusCell(cell, currentVal, date);
<<<<<<< HEAD
            } else if(cell.cellIndex === 0  || cell.cellIndex === 1 || cell.cellIndex === 4) {
=======
            } else if(cell.cellIndex === 0  || cell.cellIndex === 1) {
>>>>>>> ef369d98792231d78d6225993f704111cea9ae00
                
            }
            else{
              // 出退勤セルor備考欄 → 直接編集
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
                  if (cell.cellIndex === 2) rec.start = newVal;
                  if (cell.cellIndex === 3) rec.end = newVal;
                  console.log("attendanceRecords:", attendanceRecords);
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
        updateButtonState();
      });
    </script>
</body>
</html>
