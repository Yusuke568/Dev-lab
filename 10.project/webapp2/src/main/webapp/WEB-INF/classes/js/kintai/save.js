import { collectAttendanceRecords } from './utils.js';

document.addEventListener('DOMContentLoaded', () => {
  const saveBtn = document.getElementById('save-btn');

  saveBtn.addEventListener('click', () => {
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
  });
});
