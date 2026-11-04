import {
  collectAttendanceRecords, collectRowRecord, getMinutes, calculateTotalWorkMinutes,
  localDateString, currentTimeString, getTimeInput, isNextDayEnd
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
      if (r.kintaifrom && r.kintaito && r.kintaifrom === r.kintaito) {
        showRowError(r.kintaidate, day + "：出勤と退勤が同じ時刻です");
        return;
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
  const bulkCorrectionEl = document.getElementById("bulk-correction");
  const bulkCorrection = bulkCorrectionEl ? bulkCorrectionEl.value : "keep";

  let updated = false;
  rows.forEach((row) => {
    const cb = row.querySelector(".row-checkbox");
    if (cb && cb.checked) {
      updated = true;
      if (bulkStart) getTimeInput(row, "kintaifrom").value = bulkStart;
      if (bulkEnd) getTimeInput(row, "kintaito").value = bulkEnd;
      if (bulkStatus !== "") {
        const select = row.querySelector("select[name='status']");
        if (select) select.value = bulkStatus === "unset" ? "" : bulkStatus;
      }
      if (bulkStart || bulkEnd) setWorkTypeIfUnset(row);
      if (bulkCorrection !== "keep") {
        applyBulkCorrectionToRow(row, bulkCorrection);
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

/** 勤務区分が未設定の日に時刻を入れたら、勤務区分を「出勤」にする。 */
function setWorkTypeIfUnset(row) {
  const select = row.querySelector("select[name='status']");
  if (!select || select.value !== "") return;
  const work = workTypes.find((wt) => wt.name === "出勤");
  if (work) select.value = String(work.id);
}

/**
 * 一括入力の補正CDを1日分に反映する。補正CDを選んだら、マスタ登録済みの補正(通)・補正(深)も入れる。
 * 「なし」（空）なら補正CDを外し、補正(通)・補正(深)も空にする。
 */
function applyBulkCorrectionToRow(row, correctionId) {
  const select = row.querySelector("select[name='correctionId']");
  if (!select) return;
  select.value = correctionId;
  if (correctionId === "") {
    const usInput = row.querySelector("input[name='correctionUsTime']");
    const midInput = row.querySelector("input[name='correctionMidTime']");
    if (usInput) usInput.value = "";
    if (midInput) midInput.value = "";
  } else {
    applyCorrectionMasterToRow(select);
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

    const nextDayBadge = row.querySelector(".next-day-badge");
    if (nextDayBadge) nextDayBadge.hidden = !isNextDayEnd(start, end);

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

/**
 * 日付が変わってから退勤するときの、前日の行。
 * 今日の出勤がなく、前日に出勤だけ記録されていて、今の時刻が前日の出勤時刻より前（＝勤務が24時間以内）なら、
 * 前日の勤務の退勤（翌日の退勤）として扱う。前日の退勤を押し忘れただけの日を、翌日夜の退勤で締めないようにする。
 */
function findOpenShiftRow(now = new Date()) {
  const todayRow = findRow(localDateString(now));
  if (todayRow && getTimeInput(todayRow, "kintaifrom").value) return null;
  const yesterday = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 1);
  const row = findRow(localDateString(yesterday));
  if (!row) return null;
  const start = getTimeInput(row, "kintaifrom").value;
  if (start && !getTimeInput(row, "kintaito").value && currentTimeString(now) < start) {
    return row;
  }
  return null;
}

/** 出勤・退勤ボタンと「今日」欄を、今日の行の入力内容に合わせる。 */
function updateButtonState() {
  const openShiftRow = findOpenShiftRow();
  if (openShiftRow) {
    // 前日から続いている勤務：退勤ボタンで前日の行に翌日の退勤を記録する
    const clockIn = document.getElementById("clock-in-btn");
    const clockOut = document.getElementById("clock-out-btn");
    const todayStart = document.getElementById("today-start");
    const todayEnd = document.getElementById("today-end");
    if (todayStart) todayStart.textContent = getTimeInput(openShiftRow, "kintaifrom").value + "（前日）";
    if (todayEnd) todayEnd.textContent = "--:--";
    if (clockIn) clockIn.disabled = false;
    if (clockOut) clockOut.disabled = false;
    updateOvertimePerRow();
    return;
  }
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
  const openShiftRow = type === "退勤" ? findOpenShiftRow(now) : null;
  const row = openShiftRow || findRow(localDateString(now));
  if (!row) {
    alert("表示中の月に今日の行がありません。今月の画面で打刻してください。");
    return;
  }
  const timeStr = currentTimeString(now);
  const input = getTimeInput(row, type === "出勤" ? "kintaifrom" : "kintaito");
  const previous = input.value;
  input.value = timeStr;
  setWorkTypeIfUnset(row);
  updateButtonState();

  const record = Object.assign({}, collectRowRecord(row, staffId), { isTemporary: true });
  showTodayMessage("保存しています…", false);
  postRecords([record])
    .then((res) => {
      if (!res.ok) throw new Error("save failed");
      dirtyDates.delete(row.dataset.date);
      updateDirtyState();
      const nextDay = openShiftRow ? "（前日の勤務の翌日の退勤）" : "";
      showTodayMessage(type + " " + timeStr + nextDay + " を記録しました（一時保存）", false);
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
    // 土日・祝日の色はサーバーが付ける（祝日は JapaneseHolidayCalendar で計算）。ここでは今日の行だけ
    if (row.dataset.date === today) row.classList.add("today");
  });

  const table = document.getElementById("calendar-log");
  if (table) {
    // 入力したら、その日を「未保存」にし、時間を計算し直す
    table.addEventListener("input", (e) => {
      if (e.target.matches(".row-checkbox")) return;
      markDirty(e.target.closest("tr"));
      if (e.target.matches("input[name='kintaifrom'], input[name='kintaito']") && e.target.value) {
        setWorkTypeIfUnset(e.target.closest("tr"));
      }
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
