/*
 * 勤怠のカレンダー表示。
 *
 * スマホで1か月分の表を縦にスクロールしなくて済むよう、月のカレンダーに各日の入力内容（出勤・退勤・勤務区分）を
 * 小さく表示し、日をタップすると入力用のポップアップを開く。
 *
 * 入力内容の置き場所は今までどおり表（#calendar-log）の各行の入力欄。ポップアップは行の値を読み込み、
 * 「決定」で行の入力欄に書き戻して input / change イベントを送る。これで未保存の印・時間の再計算・
 * 有給残日数・保存（kintai.js）は表で入力したときと同じように動く。
 */
import { calculateTotalWorkMinutes, calculateOvertime, isNextDayEnd, getMinutes, localDateString } from "./utils.js";

const VIEW_KEY = "kintaiView";
const WEEK_LABELS = ["日", "月", "火", "水", "木", "金", "土"];

const root = document.getElementById("kintai-app-root");
const table = document.getElementById("calendar-log");
const rows = table ? Array.from(table.querySelectorAll("tbody tr")) : [];
const workTypes = root ? JSON.parse(root.dataset.workTypesJson || "[]") : [];

/** 読み込んだ時点の各行の値。今の値と比べて「未保存」の印を付ける */
const initialValues = new Map();

// -------- 行の値の読み書き --------

const FIELD_NAMES = ["kintaifrom", "kintaito", "status", "correctionId", "correctionUsTime", "correctionMidTime", "indirectTime", "workDescription"];

function field(row, name) {
  return row.querySelector("[name='" + name + "']");
}

function readRow(row) {
  const values = {};
  FIELD_NAMES.forEach((name) => {
    const el = field(row, name);
    values[name] = el ? el.value : "";
  });
  return values;
}

function isDirty(row) {
  const initial = initialValues.get(row.dataset.date);
  if (!initial) return false;
  const now = readRow(row);
  return FIELD_NAMES.some((name) => now[name] !== initial[name]);
}

function selectedText(select) {
  if (!select || select.value === "") return "";
  const option = select.options[select.selectedIndex];
  return option ? option.textContent.trim() : "";
}

/** 表の「状態」欄の内容から、カレンダーに出す印を決める */
function statusMark(row) {
  const cell = row.cells[9];
  const text = cell ? cell.textContent.replace(/\s+/g, "") : "";
  if (/未申請|却下/.test(text)) return { cls: "ng", label: "要対応" };
  if (/申請中|承認待ち/.test(text)) return { cls: "wait", label: "承認待ち" };
  if (/承認済/.test(text)) return { cls: "ok", label: "承認済" };
  return null;
}

function holidayName(row) {
  const names = Array.from(row.querySelectorAll(".holiday-name")).map((el) => el.textContent.trim()).filter(Boolean);
  return names.length > 0 ? names[0] : "";
}

function shortTime(t) {
  // "09:00" → "9:00"
  return t ? t.replace(/^0(\d)/, "$1") : "";
}

function dateParts(row) {
  const [y, m, d] = row.dataset.date.split("-").map(Number);
  return { y, m, d, dow: new Date(y, m - 1, d).getDay() };
}

// -------- カレンダー --------

let grid;
const cellByDate = new Map();

function buildCalendar() {
  const container = document.createElement("div");
  container.className = "month-calendar";
  container.id = "month-calendar";

  const head = document.createElement("div");
  head.className = "mc-week mc-head";
  WEEK_LABELS.forEach((label, i) => {
    const el = document.createElement("div");
    el.className = "mc-dow" + (i === 0 ? " sunday" : i === 6 ? " saturday" : "");
    el.textContent = label;
    head.appendChild(el);
  });
  container.appendChild(head);

  grid = document.createElement("div");
  grid.className = "mc-grid";
  if (rows.length > 0) {
    for (let i = 0; i < dateParts(rows[0]).dow; i++) {
      const blank = document.createElement("div");
      blank.className = "mc-blank";
      blank.setAttribute("aria-hidden", "true");
      grid.appendChild(blank);
    }
  }
  rows.forEach((row) => {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "mc-day";
    button.dataset.date = row.dataset.date;
    button.addEventListener("click", () => openDay(row.dataset.date));
    grid.appendChild(button);
    cellByDate.set(row.dataset.date, button);
  });
  container.appendChild(grid);

  const legend = document.createElement("div");
  legend.className = "mc-legend";
  legend.innerHTML =
    '<span><i class="mc-dot dirty"></i>未保存</span>' +
    '<span><i class="mc-dot ng"></i>要対応（未申請・却下）</span>' +
    '<span><i class="mc-dot wait"></i>承認待ち</span>' +
    '<span class="mc-legend-hint">日付をタップすると入力できます</span>';
  container.appendChild(legend);
  return container;
}

function renderDay(row) {
  const cell = cellByDate.get(row.dataset.date);
  if (!cell) return;
  const { m, d, dow } = dateParts(row);
  const v = readRow(row);
  const type = selectedText(field(row, "status"));
  const holiday = holidayName(row);
  const mark = statusMark(row);
  const dirty = isDirty(row);
  const nextDay = isNextDayEnd(v.kintaifrom, v.kintaito);

  cell.className = "mc-day"
    + (row.classList.contains("sunday") || dow === 0 ? " sunday" : "")
    + (row.classList.contains("saturday") || dow === 6 ? " saturday" : "")
    + (row.classList.contains("holiday") ? " holiday" : "")
    + (row.dataset.date === localDateString() ? " today" : "")
    + (row.classList.contains("row-error") ? " error" : "")
    + (dirty ? " dirty" : "")
    + (field(row, "kintaifrom")?.value || type ? " filled" : "");

  // 備考がある日は日付の横に ✎ を付ける
  let html = '<span class="mc-date">' + d
    + (v.workDescription ? '<span class="mc-memo" aria-hidden="true">✎</span>' : "") + "</span>";
  if (dirty) html += '<i class="mc-dot dirty" aria-hidden="true"></i>';
  else if (mark && mark.cls !== "ok") html += '<i class="mc-dot ' + mark.cls + '" aria-hidden="true"></i>';
  if (type) html += '<span class="mc-type">' + escapeHtml(type) + "</span>";
  else if (holiday) html += '<span class="mc-holiday">' + escapeHtml(holiday) + "</span>";
  if (v.kintaifrom || v.kintaito) {
    html += '<span class="mc-time">' + (shortTime(v.kintaifrom) || "--:--") + "</span>";
    html += '<span class="mc-time">' + (v.kintaito ? (nextDay ? "翌" : "") + shortTime(v.kintaito) : "--:--") + "</span>";
  }
  cell.innerHTML = html;

  // 読み上げ用の説明
  const parts = [m + "月" + d + "日（" + WEEK_LABELS[dow] + "）"];
  if (holiday) parts.push(holiday);
  if (type) parts.push(type);
  if (v.kintaifrom) parts.push("出勤 " + v.kintaifrom);
  if (v.kintaito) parts.push("退勤 " + (nextDay ? "翌日 " : "") + v.kintaito);
  if (mark) parts.push(mark.label);
  if (dirty) parts.push("未保存");
  cell.setAttribute("aria-label", parts.join("、"));
}

function renderAll() {
  rows.forEach(renderDay);
}

function escapeHtml(s) {
  return String(s).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

// -------- 入力ポップアップ --------

let dialog;
let currentDate = null;

function buildDialog() {
  dialog = document.createElement("dialog");
  dialog.className = "day-dialog";
  dialog.setAttribute("aria-labelledby", "dd-title");
  dialog.innerHTML = `
    <form method="dialog" class="dd-form">
      <div class="dd-header">
        <button type="button" class="dd-nav" data-move="-1" aria-label="前の日">‹</button>
        <div class="dd-title-wrap">
          <h3 id="dd-title" class="dd-title"></h3>
          <div class="dd-holiday" id="dd-holiday"></div>
        </div>
        <button type="button" class="dd-nav" data-move="1" aria-label="次の日">›</button>
      </div>
      <div class="dd-body">
        <div class="dd-row dd-times">
          <label>出勤<input type="time" id="dd-from" class="form-input"></label>
          <label>退勤<span class="next-day-badge" id="dd-nextday" hidden>翌日</span><input type="time" id="dd-to" class="form-input"></label>
        </div>
        <div class="dd-quick">
          <button type="button" class="btn btn-secondary" data-fill="09:00-18:00">9:00〜18:00</button>
          <button type="button" class="btn btn-secondary" data-fill="clear">時刻を消す</button>
        </div>
        <div class="dd-worktime" id="dd-worktime" aria-live="polite"></div>
        <label class="dd-label">勤務区分<span id="dd-status-slot"></span></label>
        <details class="dd-more" id="dd-more">
          <summary>補正・間接・備考</summary>
          <label class="dd-label">補正CD<span id="dd-correction-slot"></span></label>
          <div class="dd-row">
            <label>補正(通)<input type="time" id="dd-us" class="form-input"></label>
            <label>補正(深)<input type="time" id="dd-mid" class="form-input"></label>
          </div>
          <div class="dd-row">
            <label>間接<input type="time" id="dd-indirect" class="form-input" aria-describedby="dd-indirect-note"></label>
            <p class="dd-field-note" id="dd-indirect-note">出退勤時刻の外で行った社内会議・教育などの時間</p>
          </div>
          <label class="dd-label">備考</label>
          <div class="dd-memo-row">
            <input type="text" id="dd-memo" class="form-input" maxlength="500">
            <button type="button" class="btn btn-secondary dd-template" onclick="insertTemplate(this)">定型</button>
          </div>
        </details>
        <div class="dd-status" id="dd-status"></div>
        <label class="dd-check"><input type="checkbox" id="dd-select"> 一括入力の対象にする</label>
      </div>
      <div class="dd-actions">
        <button type="button" class="btn btn-secondary" data-action="cancel">キャンセル</button>
        <button type="button" class="btn btn-primary" data-action="apply">決定</button>
      </div>
      <p class="dd-note">「決定」で表に反映します。保存は画面下の「一時保存」「保存（提出）」で行ってください。</p>
    </form>`;
  document.body.appendChild(dialog);

  dialog.querySelector("[data-action='cancel']").addEventListener("click", () => dialog.close());
  dialog.querySelector("[data-action='apply']").addEventListener("click", () => {
    applyDialog();
    dialog.close();
  });
  dialog.querySelectorAll(".dd-nav").forEach((btn) => btn.addEventListener("click", () => {
    applyDialog();
    const index = rows.findIndex((r) => r.dataset.date === currentDate) + Number(btn.dataset.move);
    if (index >= 0 && index < rows.length) loadDay(rows[index].dataset.date);
  }));
  // 背景をタップしたら閉じる（入力内容は反映しない）
  dialog.addEventListener("click", (e) => {
    if (e.target === dialog) dialog.close();
  });
  dialog.addEventListener("close", () => {
    const cell = cellByDate.get(currentDate);
    if (cell) cell.focus();
  });

  dialog.querySelectorAll("[data-fill]").forEach((btn) => btn.addEventListener("click", () => {
    if (btn.dataset.fill === "clear") {
      $("dd-from").value = "";
      $("dd-to").value = "";
    } else {
      const [from, to] = btn.dataset.fill.split("-");
      $("dd-from").value = from;
      $("dd-to").value = to;
      setWorkTypeIfUnset();
    }
    updateDialogPreview();
  }));
  ["dd-from", "dd-to"].forEach((id) => $(id).addEventListener("input", () => {
    if ($(id).value) setWorkTypeIfUnset();
    updateDialogPreview();
  }));
  ["dd-us", "dd-mid", "dd-indirect"].forEach((id) => $(id).addEventListener("input", updateDialogPreview));
}

function $(id) {
  return document.getElementById(id);
}

function rowOf(date) {
  return rows.find((r) => r.dataset.date === date);
}

/** 勤務区分が未設定のまま時刻を入れたら「出勤」にする（表の入力と同じ動き） */
function setWorkTypeIfUnset() {
  const select = $("dd-status-select");
  if (!select || select.value !== "") return;
  const work = workTypes.find((w) => w.name === "出勤");
  if (work) select.value = String(work.id);
}

/** 分 → "13時間00分" */
function hm(minutes) {
  return Math.floor(minutes / 60) + "時間" + String(minutes % 60).padStart(2, "0") + "分";
}

function updateDialogPreview() {
  const from = $("dd-from").value;
  const to = $("dd-to").value;
  $("dd-nextday").hidden = !isNextDayEnd(from, to);
  const out = $("dd-worktime");
  if (from && to) {
    const add = ["dd-us", "dd-mid", "dd-indirect"].reduce((sum, id) => {
      const value = $(id).value ? getMinutes($(id).value) : 0;
      return sum + (isNaN(value) ? 0 : value);
    }, 0);
    const total = calculateTotalWorkMinutes(from, to, add);
    const overtime = calculateOvertime(from, to, add);
    out.textContent = "総勤務 " + hm(total) + "（休憩を除く・補正と間接を含む）" + (overtime > 0 ? "　時間外 " + hm(overtime) : "");
  } else {
    out.textContent = "";
  }
}

/** 行の select を複製して、ポップアップの選択欄にする（選択肢は表と同じ） */
function cloneSelect(row, name, id, slotId) {
  const original = field(row, name);
  const slot = $(slotId);
  slot.innerHTML = "";
  if (!original) return null;
  const copy = original.cloneNode(true);
  copy.removeAttribute("name");
  copy.id = id;
  copy.value = original.value;
  slot.appendChild(copy);
  return copy;
}

function loadDay(date) {
  const row = rowOf(date);
  if (!row) return;
  currentDate = date;
  const { m, d, dow } = dateParts(row);
  const v = readRow(row);

  $("dd-title").textContent = m + "月" + d + "日（" + WEEK_LABELS[dow] + "）";
  $("dd-title").className = "dd-title" + (dow === 0 || row.classList.contains("holiday") ? " dd-sun" : dow === 6 ? " dd-sat" : "");
  $("dd-holiday").textContent = holidayName(row);
  $("dd-from").value = v.kintaifrom;
  $("dd-to").value = v.kintaito;
  cloneSelect(row, "status", "dd-status-select", "dd-status-slot");
  const correction = cloneSelect(row, "correctionId", "dd-correction-select", "dd-correction-slot");
  if (correction) {
    correction.addEventListener("change", () => {
      const option = correction.options[correction.selectedIndex];
      if (option && option.value !== "") {
        $("dd-us").value = option.dataset.us || "00:00";
        $("dd-mid").value = option.dataset.mid || "00:00";
      } else {
        $("dd-us").value = "";
        $("dd-mid").value = "";
      }
      updateDialogPreview();
    });
  }
  $("dd-us").value = v.correctionUsTime;
  $("dd-mid").value = v.correctionMidTime;
  $("dd-indirect").value = v.indirectTime;
  $("dd-memo").value = v.workDescription;
  // 補正・備考に入力がある日は最初から開いておく
  $("dd-more").open = !!(v.correctionId || v.correctionUsTime || v.correctionMidTime || v.indirectTime || v.workDescription);

  // 承認・申請の状態（リンクを含む）は表の「状態」欄をそのまま見せる
  const statusCell = row.cells[9];
  const statusText = statusCell ? statusCell.textContent.replace(/\s+/g, "") : "";
  $("dd-status").innerHTML = statusCell && statusText !== "ー" ? "<span class='dd-status-label'>状態</span>" + statusCell.innerHTML : "";

  const checkbox = row.querySelector(".row-checkbox");
  $("dd-select").checked = !!(checkbox && checkbox.checked);

  const index = rows.indexOf(row);
  dialog.querySelector("[data-move='-1']").disabled = index <= 0;
  dialog.querySelector("[data-move='1']").disabled = index >= rows.length - 1;
  updateDialogPreview();
}

/** 行の入力欄に値を入れ、表で入力したときと同じイベントを送る */
function setRowValue(row, name, value, eventType) {
  const el = field(row, name);
  if (!el || el.value === value) return;
  el.value = value;
  el.dispatchEvent(new Event(eventType, { bubbles: true }));
}

function applyDialog() {
  const row = rowOf(currentDate);
  if (!row) return;
  setRowValue(row, "kintaifrom", $("dd-from").value, "input");
  setRowValue(row, "kintaito", $("dd-to").value, "input");
  // 時刻を入れると表側でも勤務区分が「出勤」になることがあるので、ポップアップの選択で上書きする
  const status = $("dd-status-select");
  if (status) setRowValue(row, "status", status.value, "change");
  const correction = $("dd-correction-select");
  if (correction) setRowValue(row, "correctionId", correction.value, "change");
  // 補正CDの変更で表側が登録済みの補正時間を入れるので、そのあとでポップアップの値を入れる
  setRowValue(row, "correctionUsTime", $("dd-us").value, "input");
  setRowValue(row, "correctionMidTime", $("dd-mid").value, "input");
  setRowValue(row, "indirectTime", $("dd-indirect").value, "input");
  setRowValue(row, "workDescription", $("dd-memo").value, "input");

  const checkbox = row.querySelector(".row-checkbox");
  if (checkbox && checkbox.checked !== $("dd-select").checked) {
    checkbox.checked = $("dd-select").checked;
    checkbox.dispatchEvent(new Event("change", { bubbles: true }));
  }
  row.classList.remove("row-error");
  renderDay(row);
}

function openDay(date) {
  loadDay(date);
  if (typeof dialog.showModal === "function") dialog.showModal();
  else dialog.setAttribute("open", "");
}

// -------- 表示の切り替え（カレンダー／一覧） --------

function currentView() {
  try {
    const saved = localStorage.getItem(VIEW_KEY);
    if (saved === "calendar" || saved === "list") return saved;
  } catch (e) {
    // 保存できない環境では画面幅で決める
  }
  return window.matchMedia("(max-width: 768px)").matches ? "calendar" : "list";
}

function setView(view, remember) {
  document.body.classList.toggle("kintai-calendar-view", view === "calendar");
  document.querySelectorAll(".view-switch button").forEach((btn) => {
    btn.setAttribute("aria-pressed", String(btn.dataset.view === view));
  });
  if (view === "calendar") renderAll();
  if (remember) {
    try {
      localStorage.setItem(VIEW_KEY, view);
    } catch (e) {
      // 覚えられなくても切り替えはできる
    }
  }
}

function buildSwitch() {
  const wrap = document.createElement("div");
  wrap.className = "view-switch";
  wrap.setAttribute("role", "group");
  wrap.setAttribute("aria-label", "勤怠の表示方法");
  wrap.innerHTML =
    '<button type="button" data-view="calendar">📅 カレンダー</button>' +
    '<button type="button" data-view="list">📋 一覧</button>';
  wrap.querySelectorAll("button").forEach((btn) => btn.addEventListener("click", () => setView(btn.dataset.view, true)));
  return wrap;
}

// -------- 初期化 --------

/** 表の値を書き換える関数（打刻・一括入力・保存）の後で、カレンダーも描き直す */
function wrapGlobal(name) {
  const original = window[name];
  if (typeof original !== "function") return;
  window[name] = function (...args) {
    const result = original.apply(this, args);
    renderAll();
    return result;
  };
}

function init() {
  if (!table || rows.length === 0) return;
  rows.forEach((row) => initialValues.set(row.dataset.date, readRow(row)));

  const wrapper = table.closest(".table-responsive") || table;
  wrapper.parentNode.insertBefore(buildSwitch(), wrapper);
  wrapper.parentNode.insertBefore(buildCalendar(), wrapper);
  buildDialog();

  // 表で直接入力したときも、カレンダーに反映する
  ["input", "change"].forEach((type) => table.addEventListener(type, (e) => {
    const row = e.target.closest("tr");
    if (row) renderDay(row);
  }));
  ["recordAttendance", "applyBulkInput", "saveAttendance"].forEach(wrapGlobal);

  // 「今日の行へ」はカレンダー表示では今日の入力を開く
  const scrollToToday = window.scrollToToday;
  window.scrollToToday = function () {
    if (document.body.classList.contains("kintai-calendar-view")) {
      const today = localDateString();
      if (rowOf(today)) {
        $("month-calendar").scrollIntoView({ behavior: "smooth", block: "start" });
        openDay(today);
      }
      return;
    }
    if (typeof scrollToToday === "function") scrollToToday();
  };

  setView(currentView(), false);
}

// module は HTML を読み終えてから動くので、表はもうある。kintai.js（先に読み込む module）が
// window に関数を登録した後なので、そのまま初期化できる
init();
