import {
  collectAttendanceRecords, collectRowRecord, getMinutes, calculateTotalWorkMinutes, calculateOvertime,
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

/** 勤務区分ごとの有給消費日数（適用マスタの設定。有給 1、半休 0.5、それ以外 0） */
const paidLeaveUse = new Map(workTypes.map((w) => [String(w.id), Number(w.paidLeaveDays) || 0]));

/** 12 → "12"、11.5 → "11.5" */
function formatDays(days) {
  return String(Math.round(days * 10) / 10);
}

/**
 * 有給残日数の表示を、保存していない勤務区分の変更に合わせる。
 * 保存すると、前の区分で使っていた日数が戻り、新しい区分の日数が引かれる（サーバーと同じ考え方）。
 */
function updatePaidLeavePreview() {
  const card = document.getElementById("paid-leave-card");
  if (!card) return;
  const saved = Number(card.dataset.savedDays);
  let delta = 0;
  rows.forEach((row) => {
    const select = row.querySelector("select[name='status']");
    if (!select) return;
    delta += (paidLeaveUse.get(select.dataset.initial) || 0) - (paidLeaveUse.get(select.value) || 0);
  });
  const after = saved + delta;
  const note = document.getElementById("paid-leave-note");
  if (note) {
    note.textContent = delta === 0 ? "" : "保存すると " + formatDays(after) + "日（未保存の有給 "
      + (delta < 0 ? formatDays(-delta) + "日" : "取消 " + formatDays(delta) + "日") + "）";
  }
  card.classList.toggle("short", after < 0);
  if (after < 0 && note) note.textContent += " 残日数が足りません";
}

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

/**
 * 勤怠を保存する。
 * @param {boolean} isTemporary 一時保存なら true
 * @param {{noReload?: boolean}} opts noReload が true なら保存後に読み込み直さない（別の画面へ移る前の保存）
 * @returns {Promise<boolean>} 保存できたら true
 */
function saveAttendance(isTemporary = false, opts = {}) {
  let records = collectAttendanceRecords(staffId);

  if (!isTemporary) {
    for (const r of records) {
      const day = Number(r.kintaidate.slice(8)) + "日";
      if (r.kintaifrom && r.kintaito && r.kintaifrom === r.kintaito) {
        showRowError(r.kintaidate, day + "：出勤と退勤が同じ時刻です");
        return Promise.resolve(false);
      }
      const paidLeaveObj = workTypes.find(w => w.name === '有給');
      const paidLeaveId = paidLeaveObj ? paidLeaveObj.id : undefined;
      if (r.abstractId === paidLeaveId && (r.kintaifrom || r.kintaito)) {
        showRowError(r.kintaidate, day + "：有給の日に出勤・退勤時刻が入力されています");
        return Promise.resolve(false);
      }
    }
  }
  rows.forEach((row) => row.classList.remove("row-error"));

  // add isTemporary to records
  records = records.map(r => Object.assign({}, r, { isTemporary: isTemporary }));

  showSaveMessage("保存しています…", "");
  return postRecords(records)
    .then(async (res) => {
      if (res.ok) {
        dirtyDates.clear();
        updateDirtyState();
        if (opts.noReload) {
          showSaveMessage(isTemporary ? "一時保存しました。" : "保存（提出）しました。", "success");
          return true;
        }
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
        return true;
      }
      // 400 などは、サーバーが返した理由をそのまま表示する
      const text = (await res.text().catch(() => "")).trim();
      const reason = text && text.length < 200 && !text.startsWith("<") ? text : "保存できませんでした。";
      showSaveMessage(reason, "error");
      return false;
    })
    .catch(() => {
      showSaveMessage("保存できませんでした。通信状態を確かめて、もう一度保存してください。", "error");
      return false;
    });
}

// ------------------------------------------------------------ 保存していないまま別の画面へ移るとき

/** 移動を確かめ済み（このあとの beforeunload では聞かない） */
let leavingConfirmed = false;

/** 週次作業報告に保存していない変更があるか（kintai.jsp の週次報告のスクリプトが用意する） */
function weeklyReportDirty() {
  return typeof window.isWeeklyReportDirty === "function" && window.isWeeklyReportDirty();
}

function hasUnsavedChanges() {
  return dirtyDates.size > 0 || weeklyReportDirty();
}

/** 保存していない内容の説明（例：「勤怠 3日分・週次作業報告」） */
function unsavedSummary() {
  const parts = [];
  if (dirtyDates.size > 0) parts.push("勤怠 " + dirtyDates.size + "日分");
  if (weeklyReportDirty()) parts.push("週次作業報告");
  return parts.join("・");
}

/**
 * 「保存して移動」「保存しないで移動」「キャンセル」を聞く。
 * @returns {Promise<"save"|"discard"|"cancel">}
 */
function askLeave() {
  return new Promise((resolve) => {
    const dialog = document.createElement("dialog");
    dialog.className = "leave-dialog";
    dialog.setAttribute("aria-labelledby", "leave-dialog-title");
    dialog.innerHTML =
      '<h3 id="leave-dialog-title">保存していない変更があります</h3>' +
      '<p class="leave-dialog-what"></p>' +
      '<p>保存してから移動しますか？（勤怠は「一時保存」します。提出はしません）</p>' +
      '<div class="leave-dialog-buttons">' +
      '<button type="button" class="btn btn-success" data-choice="save">💾 保存して移動</button>' +
      '<button type="button" class="btn btn-danger" data-choice="discard">保存しないで移動</button>' +
      '<button type="button" class="btn btn-secondary" data-choice="cancel">キャンセル</button>' +
      '</div>';
    dialog.querySelector(".leave-dialog-what").textContent = "未保存：" + unsavedSummary();
    let choice = "cancel";
    dialog.addEventListener("click", (e) => {
      const btn = e.target.closest("button[data-choice]");
      if (btn) { choice = btn.dataset.choice; dialog.close(); }
      else if (e.target === dialog) dialog.close();
    });
    dialog.addEventListener("close", () => { dialog.remove(); resolve(choice); });
    document.body.appendChild(dialog);
    if (typeof dialog.showModal === "function") dialog.showModal();
    else { dialog.setAttribute("open", ""); }
    dialog.querySelector('[data-choice="save"]').focus();
  });
}

/** 保存していない勤怠・週次作業報告を保存する。どちらかが保存できなければ false */
async function saveAllBeforeLeave() {
  if (dirtyDates.size > 0 && !(await saveAttendance(true, { noReload: true }))) return false;
  if (weeklyReportDirty() && typeof window.saveWeeklyWorkReports === "function") {
    if (!(await window.saveWeeklyWorkReports())) return false;
  }
  return true;
}

/** 移動の前に聞く。移動してよければ go() を呼ぶ */
async function confirmLeave(go) {
  const choice = await askLeave();
  if (choice === "cancel") return;
  if (choice === "save" && !(await saveAllBeforeLeave())) {
    // 保存できなかった：理由は保存バー・週次報告の欄に出ている。移動しない
    showSaveMessage((document.getElementById("save-message")?.textContent || "保存できませんでした。") + "（移動をやめました）", "error");
    return;
  }
  leavingConfirmed = true;
  go();
}

function installLeaveGuard() {
  // リンク：同じタブで別の画面へ移るものだけ止める
  document.addEventListener("click", (e) => {
    if (!hasUnsavedChanges() || e.defaultPrevented) return;
    if (e.button !== 0 || e.ctrlKey || e.metaKey || e.shiftKey || e.altKey) return;
    const a = e.target.closest("a[href]");
    if (!a || a.target === "_blank" || a.hasAttribute("download")) return;
    const href = a.getAttribute("href");
    if (!href || href.startsWith("#") || href.startsWith("javascript:")) return;
    // 作業報告書（Excel）の出力はダウンロードで画面は変わらない
    if (/workReportExport\.do/.test(href)) return;
    e.preventDefault();
    confirmLeave(() => { location.href = a.href; });
  }, true);

  // フォームの送信（作業報告書の取り込み・ログアウトなど）
  document.addEventListener("submit", (e) => {
    const form = e.target;
    if (!hasUnsavedChanges() || leavingConfirmed || form.closest("dialog")) return;
    e.preventDefault();
    confirmLeave(() => form.submit());
  }, true);

  // ブラウザの戻る・再読み込み・タブを閉じるときは、ブラウザの確認だけ出せる（保存はできない）
  window.addEventListener("beforeunload", (e) => {
    if (!leavingConfirmed && hasUnsavedChanges()) {
      e.preventDefault();
      e.returnValue = "";
    }
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
    updatePaidLeavePreview();
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

/** 実働時間に足す分（補正(通)＋補正(深)＋勤務時間(間接)） */
function getRowAddMinutes(row) {
  return ["correctionUsTime", "correctionMidTime", "indirectTime"].reduce((sum, name) => {
    const input = row.querySelector("input[name='" + name + "']");
    const value = input && input.value !== "" ? getMinutes(input.value) : 0;
    return sum + (isNaN(value) ? 0 : value);
  }, 0);
}

function formatHm(totalMinutes) {
  return String(Math.floor(totalMinutes / 60)).padStart(2, "0") + ":" + String(totalMinutes % 60).padStart(2, "0");
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
    const workTimeCell = row.cells[5]; // 総勤務時間（補正・間接を含む）を表示するセル
    const overtimeCell = row.cells[6]; // 時間外

    const nextDayBadge = row.querySelector(".next-day-badge");
    if (nextDayBadge) nextDayBadge.hidden = !isNextDayEnd(start, end);

    if (start && end) {
      const addMinutes = getRowAddMinutes(row);
      workTimeCell.textContent = formatHm(calculateTotalWorkMinutes(start, end, addMinutes));
      const overtime = calculateOvertime(start, end, addMinutes);
      overtimeCell.textContent = overtime > 0 ? formatHm(overtime) : "ー";
    } else {
      workTimeCell.textContent = "";
      overtimeCell.textContent = "ー";
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
      if (e.target.matches("input[name='kintaifrom'], input[name='kintaito'], input[name='correctionUsTime'], input[name='correctionMidTime'], input[name='indirectTime']")) {
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

  // 保存していない変更があるまま画面を離れようとしたら、保存してから移るか聞く
  installLeaveGuard();

  // 有給残日数：読み込んだ時点の勤務区分を覚えておき、変更に合わせて保存後の日数を出す
  rows.forEach((row) => {
    const select = row.querySelector("select[name='status']");
    if (select) select.dataset.initial = select.value;
  });
  document.getElementById("calendar-log")?.addEventListener("change", (e) => {
    if (e.target.matches("select[name='status']")) updatePaidLeavePreview();
  });

  // Initial state update
  updateButtonState();
  updateDirtyState();
  updateSelectedCount();
  restoreAfterSave();
});
