<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
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
}
th:nth-child(8), td:nth-child(7) {
	width: 30px;
} /* 稼働補正テンプレートCD */
th:nth-child(9), td:nth-child(8) {
	width: 30px;
} /* 稼働時間補正（通常） */
th:nth-child(10), td:nth-child(9) {
	width: 30px;
} /* 稼働時間補正（深夜） */
th:nth-child(11), td:nth-child(10) {
	width: 30px;
} /* 間接時間 */
th:nth-child(12), td:nth-child(11) {
	width: 30px;
} /* 総稼働時間 */
th:nth-child(13), td:nth-child(12) {
	width: 30px;
} /* 総稼働時間（直接） */

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
		<h2>氏名：<c:out value="${shainbean.name}"/></h2>
		<p>有給休暇残日数：<c:out value="${shainbean.paidLeaveDays}"/>日</p>
		<p>リストサイズ: ${fn:length(calendarBeanList)}</p>
		<div class="buttons">
			<button id="clock-in-btn" onclick="recordAttendance('出勤')">出勤</button>
			<button id="clock-out-btn" onclick="recordAttendance('退勤')">退勤</button>
			<p id="overtime-summary">時間外労働: 0時間0分</p>
		</div>
		
		<div class="save-area">
  <button id="save-btn" onclick="saveAttendance()">保存</button>
</div>

	
		<table id="calendar-log">
			<thead>
				<tr>
					<th>日付</th>
					<th>曜日</th>
					<th>出勤</th>
					<th>退勤</th>
					<th>時間外</th>
					<th>勤務区分</th>
					<th>備考欄</th>
					<th>稼働補正テンプレートCD</th>
					<th>稼働時間補正（通常）</th>
					<th>稼働時間補正（深夜）</th>
					<th>間接時間</th>
					<th>総稼働時間</th>
					<th>総稼働時間（直接）</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="calendarbean" items="${calendarBeanList}">
					<tr data-date="<c:out value="${calendarbean.kintaidate}"/>">
						<td><c:out value="${calendarbean.kintaidate}"/></td>
						<td><c:out value="${calendarbean.week}"/></td>
						<td data-form="<fmt:formatDate value="${calendarbean.kintaifrom}" pattern="HH:mm"/>"><fmt:formatDate value="${calendarbean.kintaifrom}" pattern="HH:mm"/></td>
						<td data-to="<fmt:formatDate value="${calendarbean.kintaito}" pattern="HH:mm"/>"><fmt:formatDate value="${calendarbean.kintaito}" pattern="HH:mm"/></td>
						<td data-jikangai="<c:out value="${calendarbean.jikangai}"/>"><c:out value="${calendarbean.jikangai}"/></td>
						<td><select name="status">
								<c:forEach var="opt" items="${statusOptions}">
									<option value="<c:out value="${opt.id}"/>" ${opt.id == calendarbean.abstractId ? 'selected' : ''}><c:out value="${opt.name}"/></option>
								</c:forEach>
						</select></td>
					<td><c:out value="${calendarbean.memo}"/></td>
					<td><input type="number" name="correctionId" value="${calendarbean.correctionId}"/></td>
					<td><input type="number" name="correctionUsTime" value="${calendarbean.correctionUsTime}"/></td>
					<td><input type="number" name="correctionMidTime" value="${calendarbean.correctionMidTime}"/></td>
					<td><input type="number" name="indirectTime" value="${calendarbean.indirectTime}"/></td>
					<td><input type="number" name="totalWorkTime" value="${calendarbean.totalWorkTime}"/></td>
					<td><input type="number" name="totalDirectWorkTime" value="${calendarbean.totalDirectWorkTime}"/></td>
					</tr>
				</c:forEach>
		</tbody>
		</table>			<select id="status-template" style="display: none;">
				<c:forEach var="opt" items="${statusOptions}">
					<option value="<c:out value="${opt.id}"/>"><c:out value="${opt.name}"/></option>
				</c:forEach>
			</select>
	<script type="module">
	import { collectAttendanceRecords, getMinutes } from './resources/js/kintai/utils.js';

	const staffId = <c:out value="${staff_id}"/>;
	const statusOptions = JSON.parse('<c:out value="${statusOptionsJson}"/>');

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
		      location.reload(); // Optional: Reload to see updated leave days
		    } else {
		      alert("保存に失敗しました…");
		    }
		  });
	}
	

	
      const workStart = '09:00';
      const workEnd = '18:00';

      const rows = document.querySelectorAll('#calendar-log tbody tr');

      function collectAttendanceRecords() {
    	  const records = [];
    	  const rows = document.querySelectorAll("#calendar-log tbody tr");

    	  rows.forEach(row => {
    	    const kintaidate = row.dataset.date;
    	    const week = row.querySelector("td:nth-child(2)").textContent.trim();
    	    const kintaifrom = row.querySelector("td:nth-child(3)").innerText.trim();
    	    const kintaito = row.querySelector("td:nth-child(4)").innerText.trim();

    	    const jikangaiStr = row.querySelector("td:nth-child(5)").innerText.trim();
    	    const abstractId = parseInt(row.querySelector("select[name='status']").value, 10); // Capture as int
    	    const memo = row.querySelector("td:nth-child(7)").textContent.trim();
    	    // New fields
    	    const correctionId = row.querySelector("input[name='correctionId']")?.value;
    	    const correctionUsTime = row.querySelector("input[name='correctionUsTime']")?.value;
    	    const correctionMidTime = row.querySelector("input[name='correctionMidTime']")?.value;
    	    const indirectTime = row.querySelector("input[name='indirectTime']")?.value;
    	    const totalWorkTime = row.querySelector("input[name='totalWorkTime']")?.value;
    	    const totalDirectWorkTime = row.querySelector("input[name='totalDirectWorkTime']")?.value;

    	    let jikangai = 0;
    	    if (jikangaiStr && jikangaiStr !== '') { // Check for empty string as well
    	      const [h, m] = jikangaiStr.split(':').map(Number);
    	      jikangai = h * 60 + m; // Storing as minutes, if needed
    	    }

    	    records.push({
			  id: staffId, // Add staffId to the record
    	      kintaidate,
    	      week,
    	      kintaifrom,
    	      kintaito,
    	      jikangai, // Send as int (minutes)
    	      abstractId, // Renamed from tekiyoukbn
    	      memo,
    	      correctionId: correctionId === '' ? null : Number(correctionId),
    	      correctionUsTime: correctionUsTime === '' ? null : Number(correctionUsTime),
    	      correctionMidTime: correctionMidTime === '' ? null : Number(correctionMidTime),
    	      indirectTime: indirectTime === '' ? null : Number(indirectTime),
    	      totalWorkTime: totalWorkTime === '' ? null : Number(totalWorkTime),
    	      totalDirectWorkTime: totalDirectWorkTime === '' ? null : Number(totalDirectWorkTime)
    	    });
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
        updateButtonState(); // Call on load to set initial button states
        updateOvertimeSummary(); // Call on load to set initial summary
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
    	    if (r.kintaifrom && r.kintaito) {
    	      total += calculateOvertime(r.kintaifrom, r.kintaito);
    	    }
    	  });

    	  console.log("total:", total);

    	  const summaryEl = document.getElementById('overtime-summary');
    	  if (summaryEl) {
    	    const hours = Math.floor(total / 60);
    	    const minutes = total % 60;
    	    summaryEl.innerText = "時間外労働:" + hours + "時間" + minutes + "分";
    	  } else {
    	    console.warn("overtime-summary 要素が見つかりませんでした！");
    	  }
    	}

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

      //one click
      function updateButtonState() {
    	  const today = new Date().toISOString().slice(0, 10); 
    	  const attendanceRecords = collectAttendanceRecords();
    	  const rec = attendanceRecords.find((r) => r.kintaidate === today);

    	  const clockInBtn = document.getElementById('clock-in-btn');
    	  const clockOutBtn = document.getElementById('clock-out-btn');

    	  const isClockedIn = !!rec?.kintaifrom;
    	  const isClockedOut = !!rec?.kintaito;

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

    	  updateOvertimePerRow();
    	}

      //指定した任意のレコードを更新する。
      function updateRecord(date, key, value, abstractId = null) { // Renamed status to abstractId
    	  const attendanceRecords = collectAttendanceRecords();
        let rec = attendanceRecords.find((r) => r.kintaidate === date);
        if (!rec) {
      	  console.log("attendanceRecords:", attendanceRecords);
          return;
        }
        rec[key] = value;
        if (key === 'abstractId') rec.abstractId = abstractId; // Updated for abstractId
  	  console.log("attendanceRecords:", attendanceRecords);
      }		

      function renderStatusCell(cell, currentValue, date) {
    	  cell.innerHTML = '';
    	  const records = collectAttendanceRecords(); // Use collectAttendanceRecords directly

    	  // テンプレートから select を複製
    	  const template = document.getElementById('status-template');
    	  const select = template.cloneNode(true);
    	  select.style.display = 'inline'; // 表示する

    	  // 初期値を設定
    	  select.value = currentValue || ""; // Use empty string for no selection

    	  select.addEventListener('change', () => {
    	    const newVal = parseInt(select.value, 10); // Parse to int
    	    // Find the name for display
    	    const selectedOption = statusOptions.find(opt => opt.id === newVal);
    	    cell.innerText = selectedOption ? selectedOption.name : "";

    	    const rec = records.find((r) => r.kintaidate === date);
    	    if (rec) rec.abstractId = newVal; // Updated for abstractId

    	    updateOvertimeSummary();
    	    updateButtonState();
    	  });

    	  cell.appendChild(select);

      	  console.log("attendanceRecords:", records);
    	}

      function recordAttendance(type) {
    	  const now = new Date();
    	  const dateStr = now.toISOString().slice(0, 10);

    	  const timeStr = now.toLocaleTimeString([], {
    	    hour: '2-digit',
    	    minute: '2-digit',
    	  });

    	  for (const row of rows) {
    	    if (row.dataset.date === dateStr) {
    	      switch (type) {
    	        case '出勤':
    	          row.cells[2].innerText = timeStr;
    	          row.cells[2].dataset.form = timeStr;
    	          updateRecord(dateStr, 'kintaifrom', timeStr);
    	          break;

    	        case '退勤':
    	          row.cells[3].innerText = timeStr;
    	          row.cells[3].dataset.to = timeStr;
    	          updateRecord(dateStr, 'kintaito', timeStr);
    	          break;

    	        case '備考欄':
    	          row.cells[6].innerText = type; // Assuming type is the memo content
    	          row.cells[6].dataset.memo = type;
    	          updateRecord(dateStr, 'memo', type);
    	          break;

    	        default:
    	          // This case seems to be for directly setting the abstractId based on a string
    	          // If 'type' is supposed to be the ID, then it needs to be parsed
    	          // Assuming 'type' here would be the abstract name, which we now map to ID
    	          const selectedOption = statusOptions.find(opt => opt.name === type);
    	          if (selectedOption) {
    	            row.querySelector("select[name='status']").value = selectedOption.id;
    	            updateRecord(dateStr, 'abstractId', selectedOption.id);
    	          }
    	          break;
    	      }
    	      break; // 対象の日付の行が見つかったのでループを抜ける
    	    }
    	  }

    	  updateOvertimeSummary();
    	  updateButtonState();
    	}

      document.addEventListener('DOMContentLoaded', () => {
    	  const rows = document.querySelectorAll("#calendar-log tbody tr");
    	  const holidays = ["2025-09-15", "2025-09-23"];
    	  const holidayOption = statusOptions.find(opt => opt.name === "休日");
    	  const holidayValue = holidayOption ? holidayOption.name : null;

    	  if (!holidayValue) {
    	    console.warn("休日の区分が見つかりませんでした！");
    	    return;
    	  }

    	  rows.forEach(row => {
    	    const date = row.dataset.date;
    	    const weekText = row.cells[1].innerText.trim();
    	    const select = row.querySelector("select[name='status']");

    	    const isSaturday = weekText === "SATURDAY";
    	    const isSunday = weekText === "SUNDAY";
    	    const isHoliday = holidays.includes(date);

    	    if ((isSaturday || isSunday || isHoliday) && select) {
    	      // ① 見た目を変更
    	      select.value = holidayValue;

    	      // ② データにも反映
    	      const rec = attendanceRecords.find(r => r.kintaidate === date);
    	      if (rec) {
    	        rec.status = holidayValue;
    	      }
    	    }
    	  });
    	});

  	
            document.addEventListener('DOMContentLoaded', () => {
              const table = document.getElementById('calendar-log');
              const records = collectAttendanceRecords(); // Use collectAttendanceRecords directly
      
              const rows = document.querySelectorAll("#calendar-log tbody tr");
        	  const holidays = ["2025-09-15", "2025-09-23"];
        	  const holidayOption = statusOptions.find(opt => opt.name === "休日");
        	  const holidayId = holidayOption ? holidayOption.id : null; // Use holidayId
      
        	  if (!holidayId) {
        	    console.warn("休日の区分が見つかりませんでした！");
        	    return;
        	  }
      
        	  rows.forEach(row => {
        	    const date = row.dataset.date;
        	    const weekText = row.cells[1].innerText.trim();
        	    const select = row.querySelector("select[name='status']");
      
        	    const isSaturday = weekText === "SATURDAY";
        	    const isSunday = weekText === "SUNDAY";
          	    // This checks for calculated holidays, not necessarily from DB flags
          	    // The 'holidays' array comes from the JS, not necessarily the bean 'isholiday'
        	    const isHoliday = holidays.includes(date); 
      
          	    // This part runs twice if DOMContentLoaded is called twice
          	    // which it is due to `collectAttendanceRecords` in the outer scope
          	    // and this block in the inner scope. This should be refactored.
      
          	    if ((isSaturday || isSunday || isHoliday) && select) {
          	      select.value = holidayId; // Set value to ID
          	      const rec = records.find(r => r.kintaidate === date); // This records is local
          	      if (rec) {
          	        rec.abstractId = holidayId; // Set abstractId
          	      }
          	    }
        	  });     	        
      
                      table.addEventListener('click', (e) => {
      
                        const cell = e.target;
      
                        if (cell.tagName === 'TD' && cell.cellIndex >= 1) {
      
                          const row = cell.parentElement;
      
                          const date = row.dataset.date;
      
                          
      
                          const isSelect = cell.querySelector("select[name='status']");
      
                          // Removed check for isInputNumber here as inputs are always present
      
              
      
                          if (cell.cellIndex === 5 && isSelect) { // 勤務区分セル → プルダウン表示
      
                            const rec = collectAttendanceRecords().find((r) => r.kintaidate === date); // Get fresh records
      
                            const currentVal = rec?.abstractId || ''; // Use abstractId
      
                            renderStatusCell(cell, currentVal, date);
      
                          } else if ([0, 1, 4].includes(cell.cellIndex)) { // Date, Day of Week, Overtime - non-editable cells
      
                            // Do nothing for these cells
      
                          } // Removed else if (isInputNumber) block
      
                          else{ // 出退勤セルor備考欄 → 直接編集 (text input fields)
      
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
      
                          	  const currentRecords = collectAttendanceRecords(); // Get fresh records
      
                          	  const rec = currentRecords.find((r) => r.kintaidate === date);
      
                          	  if (rec) {
      
                          	    if (cell.cellIndex === 2) {
      
                          	      rec.kintaifrom = newVal;
      
                          	      row.cells[2].dataset.form = newVal;
      
                          	    }
      
                          	    if (cell.cellIndex === 3) {
      
                          	      rec.kintaito = newVal;
      
                          	      row.cells[3].dataset.to = newVal;
      
                          	    }
      
                          	    if (cell.cellIndex === 6) {
      
                          	      rec.memo = newVal;
      
                          	      row.cells[6].dataset.memo = newVal;
      
                          	    }
      
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
      
              
      
                      updateButtonState();
      
                      updateOvertimeSummary();
      
                    });    </script>
</body>
</html>
