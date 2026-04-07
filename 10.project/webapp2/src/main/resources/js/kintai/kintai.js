import { collectAttendanceRecords, getMinutes } from "./utils.js";

// Get data from the root element's dataset
const rootEl = document.getElementById("kintai-app-root");
const staffId = parseInt(rootEl.dataset.staffId, 10);
const workTypes = JSON.parse(rootEl.dataset.workTypesJson);
const year = parseInt(rootEl.dataset.year, 10);
const month = parseInt(rootEl.dataset.month, 10);

function saveAttendance(isTemporary = false) {
  let records = collectAttendanceRecords(staffId);
  
  if (!isTemporary) {
    for (const r of records) {
      if (r.kintaifrom && r.kintaito) {
        const s = getMinutes(r.kintaifrom);
        const e = getMinutes(r.kintaito);
        if (e < s) {
          alert(`${r.kintaidate}: 退勤時刻が出勤時刻より前になっています`);
          return;
        }
        if (e - s > 24 * 60) {
          alert(`${r.kintaidate}: 実働時間が24時間を超えています`);
          return;
        }
      }
      const paidLeaveId = workTypes.find(w => w.name === '有給')?.id;
      if (r.abstractId === paidLeaveId && (r.kintaifrom || r.kintaito)) {
        alert(`${r.kintaidate}: 有給申請日に勤務実績が入力されています`);
        return;
      }
    }
  }

  // add isTemporary to records
  records = records.map(r => ({ ...r, isTemporary }));

  fetch("/webapp2/kintaiUpdateApi.do", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(records),
  }).then((res) => {
    if (res.ok) {
      alert("保存しました！");
      location.reload(); // Optional: Reload to see updated leave days
    } else {
      alert("保存に失敗しました…");
    }
  });
}

// Make it globally accessible from the JSP
window.saveAttendance = saveAttendance;
window.recordAttendance = recordAttendance;
window.insertTemplate = insertTemplate;
window.toggleAllRows = toggleAllRows;
window.applyBulkInput = applyBulkInput;

const workStart = "09:00";
const workEnd = "18:00";

const rows = document.querySelectorAll("#calendar-log tbody tr");

// Holidays - this should ideally come from the server as well
const holidays = ["2025-09-15", "2025-09-23"];

// -------- Functions --------

function toggleAllRows(checkbox) {
  const checkboxes = document.querySelectorAll(".row-checkbox");
  checkboxes.forEach((cb) => (cb.checked = checkbox.checked));
}

function applyBulkInput() {
  const bulkStart = document.getElementById("bulk-start").value;
  const bulkEnd = document.getElementById("bulk-end").value;
  const bulkStatus = document.getElementById("bulk-status").value;

  let updated = false;
  rows.forEach((row) => {
    const cb = row.querySelector(".row-checkbox");
    if (cb && cb.checked) {
      updated = true;
      if (bulkStart) row.cells[3].innerText = bulkStart;
      if (bulkEnd) row.cells[4].innerText = bulkEnd;
      if (bulkStatus !== "") {
        const select = row.querySelector("select[name='status']");
        if (select) select.value = bulkStatus;
      }
    }
  });

  if (updated) {
    updateOvertimePerRow();
    updateOvertimeSummary();
  } else {
    alert("反映する行を選択してください。");
  }
}

function insertTemplate(btnEl) {
  const input = btnEl.previousElementSibling;
  if (input) {
    const templateText = "【所感】\n本日も滞りなく業務を遂行しました。\n";
    if (input.value.indexOf("【所感】") === -1) {
      input.value = (input.value + " " + templateText).trim();
    }
  }
}

function calculateTotalWorkMinutes(start, end) {
  const s = getMinutes(start);
  const e = getMinutes(end);
  if (s >= e) return 0;
  
  let total = e - s;
  
  // 休憩時間 12:00 - 13:00 の自動控除
  const breakStart = getMinutes("12:00");
  const breakEnd = getMinutes("13:00");
  const overlapStart = Math.max(s, breakStart);
  const overlapEnd = Math.min(e, breakEnd);
  
  if (overlapStart < overlapEnd) {
    total -= (overlapEnd - overlapStart);
  }
  
  return total;
}

function calculateOvertime(start, end) {
  const totalMinutes = calculateTotalWorkMinutes(start, end);
  // 1日の所定労働時間を8時間(480分)とする
  const standardWorkMinutes = 480;
  return totalMinutes > standardWorkMinutes ? totalMinutes - standardWorkMinutes : 0;
}

function updateOvertimePerRow() {
  rows.forEach((row) => {
    const start = row.cells[3].innerText.trim();
    const end = row.cells[4].innerText.trim();
    const workTimeCell = row.cells[5]; // 実働時間を表示するセル

    if (start && end) {
      const totalMinutes = calculateTotalWorkMinutes(start, end);
      const hours = Math.floor(totalMinutes / 60);
      const minutes = totalMinutes % 60;
      workTimeCell.innerText =
        String(hours).padStart(2, "0") + ":" + String(minutes).padStart(2, "0");
    } else {
      workTimeCell.innerText = "";
    }
  });
}

function updateOvertimeSummary() {
  let total = 0;
  const attendanceRecords = collectAttendanceRecords(staffId);

  attendanceRecords.forEach((r) => {
    if (r.kintaifrom && r.kintaito) {
      total += calculateOvertime(r.kintaifrom, r.kintaito);
    }
  });

  const summaryEl = document.getElementById("overtime-summary");
  if (summaryEl) {
    const hours = Math.floor(total / 60);
    const minutes = total % 60;
    summaryEl.innerText = "時間外労働: " + hours + "時間" + minutes + "分";
  }
}

function updateButtonState() {
  const today = new Date().toISOString().slice(0, 10);
  const attendanceRecords = collectAttendanceRecords(staffId);
  const rec = attendanceRecords.find((r) => r.kintaidate === today);

  const clockInBtn = document.getElementById("clock-in-btn");
  const clockOutBtn = document.getElementById("clock-out-btn");

  const isClockedIn = !!(rec && rec.kintaifrom);
  const isClockedOut = !!(rec && rec.kintaito);

  if (clockInBtn) clockInBtn.disabled = isClockedIn;
  if (clockOutBtn) clockOutBtn.disabled = isClockedOut;

  updateOvertimePerRow();
}

function recordAttendance(type) {
  const now = new Date();
  const dateStr = now.toISOString().slice(0, 10);

  const timeStr = now.toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
  });

  for (const row of rows) {
    if (row.dataset.date === dateStr) {
      switch (type) {
        case "出勤":
          row.cells[3].innerText = timeStr;
          break;
        case "退勤":
          row.cells[4].innerText = timeStr;
          break;
      }
      break;
    }
  }
  updateOvertimeSummary();
  updateButtonState();
}

function renderStatusCell(cell, currentValue, date) {
  cell.innerHTML = "";
  const template = document.getElementById("status-template");
  const select = template.cloneNode(true);
  select.style.display = "inline";
  select.value = currentValue || "";
  select.classList.add("form-input"); // Apply new style

  select.addEventListener("change", () => {
    const newVal = parseInt(select.value, 10);
    const selectedOption = workTypes.find((opt) => opt.id === newVal);
    cell.innerText = selectedOption ? selectedOption.name : "";
    // No need to update a separate array, `collectAttendanceRecords` will get it
  });

  cell.appendChild(select);
}

// -------- Event Listeners --------

document.addEventListener("DOMContentLoaded", () => {
  const today = new Date().toISOString().slice(0, 10);

  rows.forEach((row) => {
    const date = row.dataset.date;
    const weekText = row.cells[2].innerText.trim();
    const select = row.querySelector("select[name='status']");

    // Apply styles for weekends and holidays
    if (date === today) row.classList.add("today");
    if (weekText === "SATURDAY") row.classList.add("saturday");
    else if (weekText === "SUNDAY") row.classList.add("sunday");
    if (holidays.includes(date)) row.classList.add("holiday");

    // Set default status for holidays/weekends
    const holidayOption = workTypes.find((opt) => opt.name === "休日");
    if (
      holidayOption &&
      (weekText === "SATURDAY" ||
        weekText === "SUNDAY" ||
        holidays.includes(date))
    ) {
      if (select) select.value = holidayOption.id;
    }
  });

  // Cell click to edit
  const table = document.getElementById("calendar-log");
  if (table) {
    table.addEventListener("click", (e) => {
      const cell = e.target;
      if (
        cell.tagName !== "TD" ||
        cell.parentElement.parentElement.tagName !== "TBODY"
      )
        return;

      const row = cell.parentElement;
      const date = row.dataset.date;

      // Non-editable cells
      if ([0, 1, 2, 5, 8].includes(cell.cellIndex) || cell.querySelector("select"))
        return;

      // Switch to input for editable cells
      const isEditableInput = cell.querySelector("input");
      if (isEditableInput) return; // Already in edit mode

      const originalText = cell.innerText;

      // For status dropdown
      if (cell.cellIndex === 6) {
        const record = collectAttendanceRecords(staffId).find((r) => r.kintaidate === date);
        const currentVal = record ? record.abstractId : null; // Use null to be explicit
        renderStatusCell(cell, currentVal || "", date); // Pass empty string if null
        cell.querySelector("select").focus();
        return;
      }

      // For other text/time inputs
      const input = document.createElement("input");
      input.type = "text";
      input.value = originalText;
      input.classList.add("form-input"); // Use a consistent input style

      cell.innerText = "";
      cell.appendChild(input);
      input.focus();

      const handleBlur = () => {
        cell.innerText = input.value;
        updateOvertimeSummary();
        updateButtonState();
      };

      input.addEventListener("blur", handleBlur, { once: true });
      input.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
          input.blur();
        } else if (e.key === "Escape") {
          input.removeEventListener("blur", handleBlur);
          cell.innerText = originalText;
        }
      });
    });
  }

  // Initial state update
  updateButtonState();
  updateOvertimeSummary();
});
