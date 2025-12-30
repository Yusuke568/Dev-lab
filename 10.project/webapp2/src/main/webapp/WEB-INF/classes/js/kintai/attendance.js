import { collectAttendanceRecords } from './utils.js';

document.addEventListener('DOMContentLoaded', () => {
  const today = new Date().toISOString().slice(0, 10);
  const clockInBtn = document.getElementById('clock-in-btn');
  const clockOutBtn = document.getElementById('clock-out-btn');

  const records = collectAttendanceRecords();
  const todayRecord = records.find(r => r.kintaidate === today);

  const isClockedIn = !!todayRecord?.kintaifrom;
  const isClockedOut = !!todayRecord?.kintaito;

  clockInBtn.disabled = isClockedIn;
  clockInBtn.innerText = isClockedIn ? "出勤済み" : "出勤";
  clockInBtn.title = isClockedIn ? "すでに出勤しています" : "出勤時間を記録します";

  clockOutBtn.disabled = isClockedOut;
  clockOutBtn.innerText = isClockedOut ? "退勤済み" : "退勤";
  clockOutBtn.title = isClockedOut ? "すでに退勤しています" : "退勤時間を記録します";
});
