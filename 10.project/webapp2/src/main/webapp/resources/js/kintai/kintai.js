import {
  collectAttendanceRecords, collectRowRecord, getMinutes, calculateTotalWorkMinutes,
  localDateString, currentTimeString, getTimeInput
} from "./utils.js";

// Get data from the root element's dataset
const rootEl = document.getElementById("kintai-app-root");
const staffId = parseInt(rootEl.dataset.staffId, 10);
const workTypes = JSON.parse(rootEl.dataset.workTypesJson);
const year = parseInt(rootEl.dataset.year, 10);
const month = parseInt(rootEl.dataset.month, 10);
const contextPath = document.body.dataset.contextPath || "";

/** 保存していない変更がある日（YYYY-MM-DD） */
const dirtyDates = new Set();

function postRecords(records) {
  return fetch(contextPath + "/kintaiUpdateApi.do", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(records),
  });
}

/** 画面下の保存バーに、保存の結果を表示する（alert の代わり）。 */
function showSaveMessage(text, type) {
  const el = document.getElementById("save-message");
  if (!el) return;
  el.textContent = text;
  el.className = "inline-message" + (type ? " " + type : "");
}

/** 入力に問題がある日へ移動し、枠で示す。 */
function showRowError(date, message) {
  rows.forEach((row) => row.classList.remove("row-error"));
  const row = findRow(date);
  if (row) {
    row.classList.add("row-error");
    row.scrollIntoView({ behavior: "smooth", block: "center" });
  }
  showSaveMessage(message, "error");
}

/** 再読み込みの後に、保存結果とスクロール位置を引き継ぐためのキー */
const FLASH_KEY = "kintaiSaveFlash";

function saveAttendance(isTemporary = false) {
  let records = collectAttendanceRecords(staffId);

  if (!isTemporary) {
    for (const r of records) {
      const day = Number(r.kintaidate.slice(8)) + "日";
      if (r.kintaifrom && r.kintaito) {
        const s = getMinutes(r.kintaifrom);
        const e = getMinutes(r.kintaito);
        if (e < s) {
          showRowError(r.kintaidate, day + "：退勤時刻が出勤時刻より前になっています");
          return;
        }
        if (e - s > 24 * 60) {
          showRowError(r.kintaidate, day + "：実働時間が24時間を超えています");
          return;
        }
      }
      const paidLeaveObj = workTypes.find(w => w.name === '有給');
      const paidLeaveId = paidLeaveObj ? paidLeaveObj.id : undefined;
      if (r.abstractId === paidLeaveId && (r.kintaifrom || r.kintaito)) {
        showRowError(r.kintaidate, day + "：有給の日に出勤・退勤時刻が入力されています");
        return;
      }
    }
  }
  rows.forEach((row) => row.classList.remove("row-error"));

  // add isTemporary to records
  records = records.map(r => Object.assign({}, r, { isTemporary: isTemporary }));

  showSaveMessage("保存しています…", "");
  postRecords(records)
    .then(async (res) => {
      if (res.ok) {
        dirtyDates.clear();
        updateDirtyState();
        // 有給残日数や承認状態を最新にするため読み込み直す。結果の表示と位置は引き継ぐ
        try {
          sessionStorage.setItem(FLASH_KEY, JSON.stringify({
            message: isTemporary ? "一時保存しました。" : "保存（提出）しました。",
            scrollY: window.scrollY
          }));
        } catch (e) {
          // 保存できない環境では、引き継がずに読み込み直す
        }
        location.reload();
        return;
      }
      // 400 などは、サーバーが返した理由をそのまま表示する
      const text = (await res.text().catch(() => "")).trim();
      const reason = text && text.length < 200 && !text.startsWith("<") ? text : "保存できませんでした。";
      showSaveMessage(reason, "error");
    })
    .catch(() => {
      showSaveMessage("保存できませんでした。通信状態を確かめて、もう一度保存してください。", "error");
    });
}

/** 保存して読み込み直した後に、結果の表示とスクロール位置を戻す。 */
function restoreAfterSave() {
  let flash = null;
  try {
    flash = JSON.parse(sessionStorage.getItem(FLASH_KEY) || "null");
    sessionStorage.removeItem(FLASH_KEY);
  } catch (e) {
    flash = null;
  }
  if (!flash) return;
  showSaveMessage(flash.message, "success");
  if (typeof flash.scrollY === "number") {
    window.scrollTo(0, flash.scrollY);
  }
}

// Make it globally accessible from the JSP
window.saveAttendance = saveAttendance;
window.recordAttendance = recordAttendance;
window.insertTemplate = insertTemplate;
window.toggleAllRows = toggleAllRows;
window.applyBulkInput = applyBulkInput;
window.toggleRowDetails = toggleRowDetails;
window.scrollToToday = scrollToToday;
window.selectWeekdays = selectWeekdays;

const rows = document.querySelectorAll("#calendar-log tbody tr");

// Holidays - this should ideally come from the server as well
const holidays = ["2025-09-15", "2025-09-23"];

// -------- Functions --------

function findRow(date) {
  return Array.from(rows).find((row) => row.dataset.date === date);
}

function markDirty(row) {
  if (row && row.dataset.date) {
    dirtyDates.add(row.dataset.date);
    updateDirtyState();
  }
}

/** 保存していない変更があることを、下の保存バーで知らせる。 */
function updateDirtyState() {
  const bar = document.getElementById("save-bar");
  const indicator = document.getElementById("dirty-indicator");
  const dirty = dirtyDates.size > 0;
  if (bar) bar.classList.toggle("dirty", dirty);
  if (indicator) {
    indicator.textContent = dirty ? "未保存の変更があります（" + dirtyDates.size + "日分）" : "変更はすべて保存済みです";
  }
}

function toggleRowDetails(btn) {
  const tr = btn.closest('tr');
  if (tr) {
    tr.classList.toggle('expanded');
    if (tr.classList.contains('expanded')) {
      btn.textContent = '詳細を閉じる ∧';
    } else {
      btn.textContent = '詳細を開く ∨';
    }
  }
}

function toggleAllRows(checkbox) {
  const checkboxes = document.querySelectorAll(".row-checkbox");
  checkboxes.forEach((cb) => (cb.checked = checkbox.checked));
  updateSelectedCount();
}

/** 土日・祝日以外の日だけを選ぶ。 */
function selectWeekdays() {
  rows.forEach((row) => {
    const cb = row.querySelector(".row-checkbox");
    const weekend = row.classList.contains("saturday") || row.classList.contains("sunday") ||
      row.classList.contains("holiday");
    if (cb) cb.checked = !weekend;
  });
  updateSelectedCount();
}

/** 選んだ日数を表示し、「すべて選択」のチェックを実際の選択に合わせる。 */
function updateSelectedCount() {
  const checkboxes = Array.from(document.querySelectorAll(".row-checkbox"));
  const selected = checkboxes.filter((cb) => cb.checked).length;
  const allChecked = checkboxes.length > 0 && selected === checkboxes.length;
  const count = document.getElementById("bulk-selected-count");
  if (count) count.textContent = selected + "日選択中";
  ["bulk-select-all", "bulk-check-all"].forEach((id) => {
    const el = document.getElementById(id);
    if (el) el.checked = allChecked;
  });
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
      if (bulkStart) getTimeInput(row, "kintaifrom").value = bulkStart;
      if (bulkEnd) getTimeInput(row, "kintaito").value = bulkEnd;
      if (bulkStatus !== "") {
        const select = row.querySelector("select[name='status']");
        if (select) select.value = bulkStatus;
      }
      markDirty(row);
    }
  });

  const message = document.getElementById("bulk-message");
  if (updated) {
    updateButtonState();
    if (message) {
      message.className = "inline-message success";
      message.textContent = "選んだ日に反映しました。下の「保存」で保存してください。";
    }
  } else if (message) {
    message.className = "inline-message error";
    message.textContent = "反映する日を選んでください（「すべての日を選択」「平日だけ選択」または各日のチェック）。";
  }
}

function insertTemplate(btnEl) {
  const input = btnEl.previousElementSibling;
  if (input) {
    const templateText = "【所感】\n本日も滞りなく業務を遂行しました。\n";
    if (input.value.indexOf("【所感】") === -1) {
      input.value = (input.value + " " + templateText).trim();
      markDirty(input.closest("tr"));
    }
  }
}

function getRowCorrectionMinutes(row) {
  const us = row.querySelector("input[name='correctionUsTime']");
  const mid = row.querySelector("input[name='correctionMidTime']");
  const usVal = us && us.value !== "" ? getMinutes(us.value) : 0;
  const midVal = mid && mid.value !== "" ? getMinutes(mid.value) : 0;
  return (isNaN(usVal) ? 0 : usVal) + (isNaN(midVal) ? 0 : midVal);
}

/** 補正CDの選択に応じて、登録済みの補正(通)・補正(深)をその行に反映する。 */
function applyCorrectionMasterToRow(select) {
  const row = select.closest("tr");
  if (!row) return;
  const option = select.options[select.selectedIndex];
  const usInput = row.querySelector("input[name='correctionUsTime']");
  const midInput = row.querySelector("input[name='correctionMidTime']");

  if (option && option.value !== "") {
    if (usInput) usInput.value = option.dataset.us || "00:00";
    if (midInput) midInput.value = option.dataset.mid || "00:00";
  }
}

function updateOvertimePerRow() {
  rows.forEach((row) => {
    const start = getTimeInput(row, "kintaifrom").value;
    const end = getTimeInput(row, "kintaito").value;
    const workTimeCell = row.cells[5]; // 実働時間（補正後）を表示するセル

    if (start && end) {
      const correctionMinutes = getRowCorrectionMinutes(row);
      const totalMinutes = calculateTotalWorkMinutes(start, end, correctionMinutes);
      const hours = Math.floor(totalMinutes / 60);
      const minutes = totalMinutes % 60;
      workTimeCell.textContent =
        String(hours).padStart(2, "0") + ":" + String(minutes).padStart(2, "0");
    } else {
      workTimeCell.textContent = "";
    }
  });
}

/** 出勤・退勤ボタンと「今日」欄を、今日の行の入力内容に合わせる。 */
function updateButtonState() {
  const todayRow = findRow(localDateString());

  const clockInBtn = document.getElementById("clock-in-btn");
  const clockOutBtn = document.getElementById("clock-out-btn");
  const jumpBtn = document.getElementById("today-jump-btn");
  const todayStart = document.getElementById("today-start");
  const todayEnd = document.getElementById("today-end");

  if (!todayRow) {
    // 表示中の月に今日が含まれない
    if (clockInBtn) clockInBtn.disabled = true;
    if (clockOutBtn) clockOutBtn.disabled = true;
    if (jumpBtn) jumpBtn.hidden = true;
    const panel = document.getElementById("today-times");
    if (panel) panel.textContent = "表示中の月に今日は含まれません（打刻は今月の画面で行ってください）";
    updateOvertimePerRow();
    return;
  }

  const start = getTimeInput(todayRow, "kintaifrom").value;
  const end = getTimeInput(todayRow, "kintaito").value;
  if (todayStart) todayStart.textContent = start || "--:--";
  if (todayEnd) todayEnd.textContent = end || "--:--";
  if (clockInBtn) clockInBtn.disabled = !!start;
  if (clockOutBtn) clockOutBtn.disabled = !start || !!end;

  updateOvertimePerRow();
}

function showTodayMessage(text, isError) {
  const el = document.getElementById("today-message");
  if (!el) return;
  el.textContent = text;
  el.classList.toggle("error", !!isError);
}

/**
 * 出勤・退勤ボタン。今日の行に現在時刻を入れ、その日の分だけすぐに一時保存する。
 * （月全体の「保存（提出）」は別に行う）
 */
function recordAttendance(type) {
  const now = new Date();
  const row = findRow(localDateString(now));
  if (!row) {
    alert("表示中の月に今日の行がありません。今月の画面で打刻してください。");
    return;
  }
  const timeStr = currentTimeString(now);
  const input = getTimeInput(row, type === "出勤" ? "kintaifrom" : "kintaito");
  const previous = input.value;
  input.value = timeStr;
  updateButtonState();

  const record = Object.assign({}, collectRowRecord(row, staffId), { isTemporary: true });
  showTodayMessage("保存しています…", false);
  postRecords([record])
    .then((res) => {
      if (!res.ok) throw new Error("save failed");
      dirtyDates.delete(row.dataset.date);
      updateDirtyState();
      showTodayMessage(type + " " + timeStr + " を記録しました（一時保存）", false);
    })
    .catch(() => {
      // 保存できなかったときは画面にだけ残し、下の保存ボタンで保存してもらう
      markDirty(row);
      showTodayMessage(type + "の記録を保存できませんでした。通信状態を確かめて「一時保存」を押してください。", true);
      if (!previous) input.focus();
    });
}

function scrollToToday() {
  const row = findRow(localDateString());
  if (row) {
    row.scrollIntoView({ behavior: "smooth", block: "start" });
  }
}

// -------- Event Listeners --------

document.addEventListener("DOMContentLoaded", () => {
  const today = localDateString();

  const todayLabel = document.getElementById("today-date");
  if (todayLabel) {
    const d = new Date();
    todayLabel.textContent = (d.getMonth() + 1) + "/" + d.getDate() + "（" + "日月火水木金土"[d.getDay()] + "）";
  }

  rows.forEach((row) => {
    const date = row.dataset.date;
    const weekText = row.dataset.week;
    const select = row.querySelector("select[name='status']");

    // Apply styles for weekends and holidays
    if (date === today) row.classList.add("today");
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

  const table = document.getElementById("calendar-log");
  if (table) {
    // 入力したら、その日を「未保存」にし、時間を計算し直す
    table.addEventListener("input", (e) => {
      if (e.target.matches(".row-checkbox")) return;
      markDirty(e.target.closest("tr"));
      if (e.target.matches("input[name='kintaifrom'], input[name='kintaito'], input[name='correctionUsTime'], input[name='correctionMidTime']")) {
        updateButtonState();
      }
    });

    table.addEventListener("change", (e) => {
      if (e.target.matches(".row-checkbox")) {
        updateSelectedCount();
        return;
      }
      markDirty(e.target.closest("tr"));
      // 補正CDを選択したら、マスタ登録済みの補正(通)・補正(深)を自動反映
      if (e.target.matches("select[name='correctionId']")) {
        applyCorrectionMasterToRow(e.target);
        updateOvertimePerRow();
      }
    });
  }

  // 保存していない変更があるまま画面を離れようとしたら確認する
  window.addEventListener("beforeunload", (e) => {
    if (dirtyDates.size > 0) {
      e.preventDefault();
      e.returnValue = "";
    }
  });

  // Initial state update
  updateButtonState();
  updateDirtyState();
  updateSelectedCount();
  restoreAfterSave();
});
