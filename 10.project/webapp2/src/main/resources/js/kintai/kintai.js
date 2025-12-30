
import { collectAttendanceRecords, getMinutes } from './utils.js';

// These are now passed from the JSP in a separate script tag
// const staffId = <c:out value="${staff_id}"/>;
// const statusOptions = JSON.parse('<c:out value="${statusOptionsJson}"/>');
const statusOptions = JSON.parse(statusOptionsJson);


function saveAttendance() {
	  const records = collectAttendanceRecords(staffId);
	  
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

// Make it globally accessible from the JSP
window.saveAttendance = saveAttendance;
window.recordAttendance = recordAttendance;


const workStart = '09:00';
const workEnd = '18:00';

const rows = document.querySelectorAll('#calendar-log tbody tr');

// Holidays - this should ideally come from the server as well
const holidays = ["2025-09-15", "2025-09-23"];

// -------- Functions --------

function calculateOvertime(start, end) {
  const s = getMinutes(start);
  const e = getMinutes(end);
  const rS = getMinutes(workStart);
  const rE = getMinutes(workEnd);
  return (rS > s ? rS - s : 0) + (e > rE ? e - rE : 0);
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

function updateOvertimeSummary() {
  let total = 0;
  const attendanceRecords = collectAttendanceRecords(staffId);

  attendanceRecords.forEach((r) => {
    if (r.kintaifrom && r.kintaito) {
      total += calculateOvertime(r.kintaifrom, r.kintaito);
    }
  });

  const summaryEl = document.getElementById('overtime-summary');
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

	  const clockInBtn = document.getElementById('clock-in-btn');
	  const clockOutBtn = document.getElementById('clock-out-btn');

	  const isClockedIn = !!rec?.kintaifrom;
	  const isClockedOut = !!rec?.kintaito;

	  if(clockInBtn) clockInBtn.disabled = isClockedIn;
      if(clockOutBtn) clockOutBtn.disabled = isClockedOut;
	  
      updateOvertimePerRow();
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
          break;
        case '退勤':
          row.cells[3].innerText = timeStr;
          break;
      }
      break; 
    }
  }
  updateOvertimeSummary();
  updateButtonState();
}

function renderStatusCell(cell, currentValue, date) {
    cell.innerHTML = '';
    const template = document.getElementById('status-template');
    const select = template.cloneNode(true);
    select.style.display = 'inline';
    select.value = currentValue || "";
    select.classList.add('form-input'); // Apply new style

    select.addEventListener('change', () => {
        const newVal = parseInt(select.value, 10);
        const selectedOption = statusOptions.find(opt => opt.id === newVal);
        cell.innerText = selectedOption ? selectedOption.name : "";
        // No need to update a separate array, `collectAttendanceRecords` will get it
    });

    cell.appendChild(select);
}

// -------- Event Listeners --------

document.addEventListener('DOMContentLoaded', () => {
  const today = new Date().toISOString().slice(0, 10);

  rows.forEach(row => {
    const date = row.dataset.date;
    const weekText = row.cells[1].innerText.trim();
    const select = row.querySelector("select[name='status']");
    
    // Apply styles for weekends and holidays
    if (date === today) row.classList.add('today');
    if (weekText === "SATURDAY") row.classList.add('saturday');
    else if (weekText === "SUNDAY") row.classList.add('sunday');
    if (holidays.includes(date)) row.classList.add('holiday');
    
    // Set default status for holidays/weekends
    const holidayOption = statusOptions.find(opt => opt.name === "休日");
    if (holidayOption && (weekText === "SATURDAY" || weekText === "SUNDAY" || holidays.includes(date))) {
        if(select) select.value = holidayOption.id;
    }
  });

  // Cell click to edit
  const table = document.getElementById('calendar-log');
  if(table) {
      table.addEventListener('click', (e) => {
        const cell = e.target;
        if (cell.tagName !== 'TD' || cell.parentElement.parentElement.tagName !== 'TBODY') return;

        const row = cell.parentElement;
        const date = row.dataset.date;

        // Non-editable cells
        if ([0, 1, 4].includes(cell.cellIndex) || cell.querySelector('select')) return;

        // Switch to input for editable cells
        const isEditableInput = cell.querySelector('input');
        if(isEditableInput) return; // Already in edit mode

        const originalText = cell.innerText;
        
        // For status dropdown
        if (cell.cellIndex === 5) {
            const currentVal = collectAttendanceRecords(staffId).find(r => r.kintaidate === date)?.abstractId || '';
            renderStatusCell(cell, currentVal, date);
            cell.querySelector('select').focus();
            return;
        }

        // For other text/time inputs
        const input = document.createElement('input');
        input.type = 'text';
        input.value = originalText;
        input.classList.add('form-input'); // Use a consistent input style
        
        cell.innerText = '';
        cell.appendChild(input);
        input.focus();

        const handleBlur = () => {
            cell.innerText = input.value;
            updateOvertimeSummary();
            updateButtonState();
        };

        input.addEventListener('blur', handleBlur, { once: true });
        input.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                input.blur();
            } else if (e.key === 'Escape') {
                input.removeEventListener('blur', handleBlur);
                cell.innerText = originalText;
            }
        });
    });
  }

  // Initial state update
  updateButtonState();
  updateOvertimeSummary();
});
