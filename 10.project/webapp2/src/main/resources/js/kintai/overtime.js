import { collectAttendanceRecords, getMinutes } from './utils.js';

const workStart = '09:00';
const workEnd = '18:00';

function calculateOvertime(start, end) {
  const s = getMinutes(start);
  const e = getMinutes(end);
  const rS = getMinutes(workStart);
  const rE = getMinutes(workEnd);
  return (rS > s ? rS - s : 0) + (e > rE ? e - rE : 0);
}

function updateOvertimeSummary() {
  let total = 0;
  const records = collectAttendanceRecords();

  records.forEach(r => {
    if (r.kintaifrom && r.kintaito) {
      total += calculateOvertime(r.kintaifrom, r.kintaito);
    }
  });

  const summaryEl = document.getElementById('overtime-summary');
  if (summaryEl) {
    const hours = Math.floor(total / 60);
    const minutes = total % 60;
    summaryEl.innerText = `時間外労働: ${hours}時間${minutes}分`;
  }
}

function updateOvertimePerRow() {
  const rows = document.querySelectorAll("#calendar-log tbody tr");

  rows.forEach(row => {
    const start = row.cells[2].innerText.trim();
    const end = row.cells[3].innerText.trim();
    const overtimeCell = row.cells[4];

    if (start && end) {
      const overtimeMinutes = calculateOvertime(start, end);
      const hours = Math.floor(overtimeMinutes / 60);
      const minutes = overtimeMinutes % 60;
      overtimeCell.innerText = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`;
    } else {
      overtimeCell.innerText = '';
    }
  });
}

document.addEventListener('DOMContentLoaded', () => {
  updateOvertimeSummary();
  updateOvertimePerRow();
});
