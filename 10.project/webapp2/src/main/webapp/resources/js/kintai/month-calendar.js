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
import { calculateTotalWorkMinutes, calculateOvertime, isNextDayEnd, getMinutes, localDateString, holidayOvertimeByDate, overtimeRequestOf, shortHm } from "./utils.js?v=20261113f";

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

/** 補正(通)＋補正(深)＋間接（分） */
function addMinutesOf(v) {
  return [v.correctionUsTime, v.correctionMidTime, v.indirectTime].reduce((sum, value) => {
    const minutes = value ? getMinutes(value) : 0;
    return sum + (isNaN(minutes) ? 0 : minutes);
  }, 0);
}

/**
 * その日の時間外と、残業事前申請の状態。
 * 時間外は今の入力から計算する（保存前の変更も反映）。申請の状態は表の「状態」欄（サーバーが出す）から読む。
 * 時間外があるのに事前申請の承認・申請中・却下のどれでもなければ「未申請」とする。
 */
function overtimeInfo(row) {
  const v = readRow(row);
  // 休日出勤の日は週40時間の決まりで数える（kintai.js の表の時間外と同じ）
  const weekly = holidayOvertimeByDate();
  const minutes = !(v.kintaifrom && v.kintaito) ? 0
    : weekly.has(row.dataset.date) ? weekly.get(row.dataset.date)
    : calculateOvertime(v.kintaifrom, v.kintaito, addMinutesOf(v));
  if (minutes <= 0) return { minutes: 0, state: null, label: "" };
  // 残業事前申請の状態は、行に持たせた申請（data-ot-status）を先に見る
  const req = overtimeRequestOf(row, minutes);
  if (req) return { minutes, state: req.cls, label: req.label };
  const cell = row.cells[9];
  const text = cell ? cell.textContent.replace(/\s+/g, "") : "";
  if (/事前申請承認済/.test(text)) return { minutes, state: "ok", label: "承認済" };
  if (/事前申請申請中/.test(text)) return { minutes, state: "wait", label: "申請中" };
  if (/却下/.test(text)) return { minutes, state: "ng", label: "却下" };
  if (/勤怠承認済/.test(text)) return { minutes, state: "ok", label: "承認済" };
  return { minutes, state: "ng", label: "未申請" };
}

/**
 * 残業事前申請の画面へのリンク（その日を入れた状態で開く）。
 * 状態欄にすでにリンクがある日と、上長が代わりに見ている画面（申請は本人が行う）では出さない。
 */
function requestLinkHtml(row, statusCell) {
  if (root && root.dataset.proxyMode === "true") return "";
  if (statusCell && statusCell.querySelector("a")) return "";
  const contextPath = document.body.dataset.contextPath || "";
  const url = contextPath + "/overtimeRequestMenu.do?targetDate=" + encodeURIComponent(row.dataset.date);
  return " <a href='" + escapeHtml(url) + "'>申請する ›</a>";
}

function formatShortHm(minutes) {
  return Math.floor(minutes / 60) + ":" + String(minutes % 60).padStart(2, "0");
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
  // 残業の事前申請がまだの日（目立たせる）
  const alert = document.createElement("div");
  alert.className = "mc-ot-alert";
  alert.id = "mc-ot-alert";
  alert.setAttribute("role", "status");
  container.appendChild(alert);
  container.appendChild(head);

  grid = document.createElement("div");
  grid.className = "mc-grid";
  // 表の行の並びに関係なく、日付の順に並べる（DB から日付順で来なくてもカレンダーが崩れないように）
  const days = rows.slice().sort((a, b) => a.dataset.date.localeCompare(b.dataset.date));
  if (days.length > 0) {
    for (let i = 0; i < dateParts(days[0]).dow; i++) {
      const blank = document.createElement("div");
      blank.className = "mc-blank";
      blank.setAttribute("aria-hidden", "true");
      grid.appendChild(blank);
    }
  }
  days.forEach((row) => {
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
    '<span><b class="mc-ot-key ng">⚠️未申請</b>残業の事前申請がまだ（✖却下）</span>' +
    '<span><b class="mc-ot-key wait">⏳申請中</b>承認待ち</span>' +
    '<span><b class="mc-ot-key ok">✅承認済</b>残業 承認済み</span>' +
    '<span><b class="mc-ot-key plan">✅予定2:00</b>点線はまだ実績のない日の申請</span>' +
    '<span><b class="mc-ot-key over">⚠️超過+0:30</b>実績が申請の予定と違う（⚠️少ない−2:00）。日付を開いて申請し直す</span>' +
    '<span><i class="mc-dot wait"></i>有給などの承認待ち・却下</span>' +
    '<span class="mc-legend-hint">日付をタップすると入力できます</span>';
  container.appendChild(legend);

  // 一括入力の結果
  const message = document.createElement("div");
  message.className = "inline-message";
  message.id = "mc-message";
  message.setAttribute("role", "status");
  container.appendChild(message);
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
  const ot = overtimeInfo(row);

  cell.className = "mc-day"
    + (ot.state ? " ot-" + ot.state : "")
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
  else if (!ot.state && mark && mark.cls !== "ok") html += '<i class="mc-dot wait" aria-hidden="true"></i>';
  if (type) html += '<span class="mc-type">' + escapeHtml(type) + "</span>";
  else if (holiday) html += '<span class="mc-holiday">' + escapeHtml(holiday) + "</span>";
  if (v.kintaifrom || v.kintaito) {
    html += '<span class="mc-time">' + (shortTime(v.kintaifrom) || "--:--") + "</span>";
    html += '<span class="mc-time">' + (v.kintaito ? (nextDay ? "翌" : "") + shortTime(v.kintaito) : "--:--") + "</span>";
  }
  const req = overtimeRequestOf(row, ot.minutes, !!(v.kintaifrom && v.kintaito));
  const diffHtml = !req ? "" : req.over > 0
    ? '<span class="mc-ot-over" title="時間外の実績が、残業事前申請の予定より多くなっています。申請し直してください">⚠️<span class="mc-ot-text">超過</span>+' + shortHm(req.over) + "</span>"
    : req.under > 0
      ? '<span class="mc-ot-over" title="時間外の実績が、残業事前申請の予定より少なくなっています。申請し直してください">⚠️<span class="mc-ot-text">少ない</span>−' + shortHm(req.under) + "</span>"
      : "";
  if (ot.state) {
    // スマホの狭いマスでも切れないよう、2行目は時間だけにする（「未申請」の下に「2:00」）
    html += '<span class="mc-ot ' + ot.state + '" title="時間外 ' + formatShortHm(ot.minutes)
      + (req ? "\n" + escapeHtml(req.title) : "") + '"><b>' + (OT_ICON[ot.label] || "") + '<span class="mc-ot-text">' + ot.label + "</span></b>"
      + formatShortHm(ot.minutes) + "</span>";
    html += diffHtml;
  } else if (req) {
    // まだ時間外の実績がない日（これから残業する予定の日など）も、申請があれば出す
    html += '<span class="mc-ot-plan ' + req.cls + '" title="' + escapeHtml(req.title) + '"><b>' + req.icon + '<span class="mc-ot-text">' + req.label + "</span></b>"
      + (req.cls !== "ng" ? '<span class="mc-ot-text">予定</span>' + shortHm(req.planned) : "") + "</span>";
    html += diffHtml; // 出退勤を入れたのに時間外が予定より少ない日
  }
  cell.innerHTML = html;

  // 読み上げ用の説明
  const parts = [m + "月" + d + "日（" + WEEK_LABELS[dow] + "）"];
  if (holiday) parts.push(holiday);
  if (type) parts.push(type);
  if (v.kintaifrom) parts.push("出勤 " + v.kintaifrom);
  if (v.kintaito) parts.push("退勤 " + (nextDay ? "翌日 " : "") + v.kintaito);
  if (ot.state) parts.push("時間外 " + formatShortHm(ot.minutes) + "、残業の事前申請 " + ot.label);
  else if (req) parts.push("残業の事前申請 " + req.label + (req.cls !== "ng" ? "、予定 " + shortHm(req.planned) : ""));
  else if (mark) parts.push(mark.label);
  if (req && req.over > 0) parts.push("予定より " + shortHm(req.over) + " 多い。申請し直してください");
  if (req && req.under > 0) parts.push("予定より " + shortHm(req.under) + " 少ない。申請し直してください");
  if (dirty) parts.push("未保存");
  cell.setAttribute("aria-label", parts.join("、"));
}

/** カレンダーの申請の印のアイコン（色だけに頼らない） */
const OT_ICON = { "承認済": "✅", "申請中": "⏳", "却下": "✖", "未申請": "⚠️" };

function renderAll() {
  rows.forEach(renderDay);
  renderOvertimeAlert();
}

/** カレンダーの上に、残業の事前申請がまだ（未申請・却下）の日を並べる。押すとその日を開く */
function renderOvertimeAlert() {
  const box = $("mc-ot-alert");
  if (!box) return;
  const pending = rows.map((row) => ({ row, ot: overtimeInfo(row) })).filter((x) => x.ot.state === "ng");
  box.innerHTML = "";
  box.hidden = pending.length === 0;
  if (pending.length === 0) return;
  const title = document.createElement("div");
  title.className = "mc-ot-alert-title";
  title.textContent = "⚠️ 残業の事前申請が必要な日があります（" + pending.length + "日）";
  box.appendChild(title);
  const list = document.createElement("div");
  list.className = "mc-ot-alert-list";
  pending.forEach(({ row, ot }) => {
    const { m, d } = dateParts(row);
    const btn = document.createElement("button");
    btn.type = "button";
    btn.textContent = m + "/" + d + " 残" + formatShortHm(ot.minutes) + (ot.label === "却下" ? "（却下）" : "");
    btn.addEventListener("click", () => openDay(row.dataset.date));
    list.appendChild(btn);
  });
  box.appendChild(list);
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

/**
 * 入力画面を開いたとき：勤務区分が未設定で補正CDも空なら、「いつもの補正CD」（と登録済みの補正(通)・補正(深)）を入れる。
 * 決定しなければ勤怠表には入らない。入れたら true
 */
/** 開いたときに入れた「いつもの補正CD」（入れていなければ空） */
let prefilledCorrection = "";

function prefillDefaultCorrection() {
  const defaultCorrection = (root && root.dataset.defaultCorrectionId) || "";
  const status = $("dd-status-select");
  const correction = $("dd-correction-select");
  if (!defaultCorrection || !status || status.value !== "" || !correction || correction.value !== "") return false;
  if (!Array.from(correction.options).some((o) => o.value === defaultCorrection)) return false;
  correction.value = defaultCorrection;
  correction.dispatchEvent(new Event("change")); // 補正(通)・補正(深)も入れる
  return true;
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
  // 勤務区分が未設定で補正CDも空の日は、開いたときに「いつもの補正CD」（と補正(通)・補正(深)）を入れておく
  const prefilled = prefillDefaultCorrection();
  prefilledCorrection = prefilled ? $("dd-correction-select").value : "";
  // 補正・備考に入力がある日は最初から開いておく
  $("dd-more").open = prefilled || !!(v.correctionId || v.correctionUsTime || v.correctionMidTime || v.indirectTime || v.workDescription);

  // 承認・申請の状態（リンクを含む）は表の「状態」欄をそのまま見せる
  const statusCell = row.cells[9];
  const statusText = statusCell ? statusCell.textContent.replace(/\s+/g, "") : "";
  const ot = overtimeInfo(row);
  let statusHtml = statusCell && statusText !== "ー" ? "<span class='dd-status-label'>状態</span>" + statusCell.innerHTML : "";
  if (ot.state === "ng") {
    statusHtml = "<div class='dd-ot-alert'>⚠️ 時間外 " + formatShortHm(ot.minutes) + " があります。残業の事前申請が"
      + (ot.label === "却下" ? "却下されています。内容を見直して再申請してください。" : "まだです。")
      + requestLinkHtml(row, statusCell)
      + "</div>" + statusHtml;
  }
  $("dd-status").innerHTML = statusHtml;

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
  // 開いたときに入れた「いつもの補正CD」だけで、ほかに何も入れずに決定したら、補正は入れない（空の日に補正だけ残さない）
  const statusSel = $("dd-status-select");
  if (prefilledCorrection && !$("dd-from").value && !$("dd-to").value && statusSel && statusSel.value === ""
      && !$("dd-indirect").value && !$("dd-memo").value) {
    const corr = $("dd-correction-select");
    if (corr && corr.value === prefilledCorrection) {
      corr.value = "";
      $("dd-us").value = "";
      $("dd-mid").value = "";
    }
  }
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
  renderOvertimeAlert();
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

// -------- 一括入力（カレンダー表示用） --------
//
// 日付をなぞって（ドラッグ・スワイプ）まとめて選び、出勤・退勤・勤務区分・補正CDを一度に入れる画面。
// 選んだ日は表の各行のチェック（.row-checkbox）に移し、入力内容は一覧表示の一括入力欄に入れて
// kintai.js の applyBulkInput() を呼ぶ。反映のしかたは一覧表示の一括入力と同じになる。

let bulkDialog;
const bulkSelected = new Set();
const bulkCellByDate = new Map();

function isWeekendRow(row) {
  return row.classList.contains("saturday") || row.classList.contains("sunday") || row.classList.contains("holiday");
}

function buildBulkDialog() {
  bulkDialog = document.createElement("dialog");
  bulkDialog.className = "day-dialog bulk-dialog";
  bulkDialog.setAttribute("aria-labelledby", "bk-title");
  bulkDialog.innerHTML = `
    <form method="dialog" class="dd-form">
      <div class="dd-header">
        <div class="dd-title-wrap">
          <h3 id="bk-title" class="dd-title">一括入力</h3>
          <div class="bk-hint" id="bk-hint">日付を指でなぞると、まとめて選べます（タップで1日ずつ）</div>
        </div>
      </div>
      <div class="dd-body">
        <div class="bk-select-bar">
          <button type="button" class="btn btn-secondary" data-select="weekdays">平日</button>
          <button type="button" class="btn btn-secondary" data-select="all">すべて</button>
          <button type="button" class="btn btn-secondary" data-select="none">解除</button>
          <span class="bk-count" id="bk-count" role="status">0日選択中</span>
        </div>
        <div class="mc-week mc-head bk-head" aria-hidden="true"></div>
        <div class="mc-grid bk-grid" id="bk-grid" role="group" aria-label="一括入力する日" aria-describedby="bk-hint"></div>
        <div class="dd-row dd-times bk-fields">
          <label>出勤<input type="time" id="bk-start" class="form-input"></label>
          <label>退勤<input type="time" id="bk-end" class="form-input"></label>
        </div>
        <div class="dd-quick">
          <button type="button" class="btn btn-secondary" data-fill="09:00-18:00">9:00〜18:00</button>
        </div>
        <label class="dd-label">勤務区分<span id="bk-status-slot"></span></label>
        <label class="dd-label">補正CD<span id="bk-correction-slot"></span></label>
        <div class="inline-message" id="bk-message" role="alert"></div>
      </div>
      <div class="dd-actions">
        <button type="button" class="btn btn-secondary" data-action="cancel">キャンセル</button>
        <button type="button" class="btn btn-primary" data-action="apply">選んだ日に反映</button>
      </div>
      <p class="dd-note">空欄の項目は変更しません。保存は画面下の「一時保存」「保存（提出）」で行ってください。</p>
    </form>`;
  document.body.appendChild(bulkDialog);

  const head = bulkDialog.querySelector(".bk-head");
  WEEK_LABELS.forEach((label, i) => {
    const el = document.createElement("div");
    el.className = "mc-dow" + (i === 0 ? " sunday" : i === 6 ? " saturday" : "");
    el.textContent = label;
    head.appendChild(el);
  });

  const grid = $("bk-grid");
  const days = rows.slice().sort((a, b) => a.dataset.date.localeCompare(b.dataset.date));
  for (let i = 0; i < dateParts(days[0]).dow; i++) {
    const blank = document.createElement("div");
    blank.className = "mc-blank";
    grid.appendChild(blank);
  }
  days.forEach((row) => {
    const { d } = dateParts(row);
    const cell = document.createElement("button");
    cell.type = "button";
    cell.className = "bk-day" + (isWeekendRow(row) ? (row.classList.contains("saturday") ? " saturday" : " sunday") : "");
    cell.dataset.date = row.dataset.date;
    cell.textContent = d;
    // キーボード（Enter・スペース）では1日ずつ切り替える。指・マウスは下の pointer イベントで扱う
    cell.addEventListener("click", (e) => {
      if (e.detail === 0) toggleBulkDate(row.dataset.date, !bulkSelected.has(row.dataset.date));
    });
    grid.appendChild(cell);
    bulkCellByDate.set(row.dataset.date, cell);
  });
  setupDragSelect(grid);

  bulkDialog.querySelectorAll("[data-select]").forEach((btn) => btn.addEventListener("click", () => {
    rows.forEach((row) => {
      const mode = btn.dataset.select;
      toggleBulkDate(row.dataset.date, mode === "all" || (mode === "weekdays" && !isWeekendRow(row)));
    });
  }));
  bulkDialog.querySelector("[data-fill]").addEventListener("click", () => {
    $("bk-start").value = "09:00";
    $("bk-end").value = "18:00";
  });
  bulkDialog.querySelector("[data-action='cancel']").addEventListener("click", () => bulkDialog.close());
  bulkDialog.querySelector("[data-action='apply']").addEventListener("click", applyBulkDialog);
  bulkDialog.addEventListener("click", (e) => {
    if (e.target === bulkDialog) bulkDialog.close();
  });
}

/**
 * なぞって選ぶ。最初に触れた日が未選択なら「選ぶ」、選択済みなら「外す」を、指が通った日すべてに行う。
 * なぞっている間は画面がスクロールしないよう、グリッドに touch-action: none を付けている（CSS）。
 */
function setupDragSelect(grid) {
  let mode = null;
  let last = null;
  const dayAt = (x, y) => {
    const el = document.elementFromPoint(x, y);
    const cell = el ? el.closest(".bk-day") : null;
    return cell && grid.contains(cell) ? cell : null;
  };
  /** 前の位置から今の位置までを細かくたどり、途中のマスも選ぶ（速くなぞっても飛ばさない） */
  const sweep = (x, y) => {
    const from = last || { x, y };
    const steps = Math.max(1, Math.ceil(Math.hypot(x - from.x, y - from.y) / 6));
    for (let i = 1; i <= steps; i++) {
      const cell = dayAt(from.x + (x - from.x) * i / steps, from.y + (y - from.y) * i / steps);
      if (cell) toggleBulkDate(cell.dataset.date, mode);
    }
    last = { x, y };
  };
  grid.addEventListener("pointerdown", (e) => {
    const cell = e.target.closest(".bk-day");
    if (!cell) return;
    e.preventDefault();
    mode = !bulkSelected.has(cell.dataset.date);
    last = { x: e.clientX, y: e.clientY };
    toggleBulkDate(cell.dataset.date, mode);
    // 指が別のマスへ動いても、このグリッドでイベントを受け取り続ける
    try {
      grid.setPointerCapture(e.pointerId);
    } catch (err) {
      // 受け取れなくても、グリッドの上をなぞっている間は選べる
    }
  });
  grid.addEventListener("pointermove", (e) => {
    if (mode === null) return;
    // 指の動きがまとめて届くブラウザでは、途中の位置も使う
    const events = typeof e.getCoalescedEvents === "function" ? e.getCoalescedEvents() : [];
    (events.length > 0 ? events : [e]).forEach((ev) => sweep(ev.clientX, ev.clientY));
  });
  const end = () => { mode = null; last = null; };
  // 指を離した位置までの間も選ぶ（最後の move から離すまでに動いた分）
  grid.addEventListener("pointerup", (e) => {
    if (mode !== null) sweep(e.clientX, e.clientY);
    end();
  });
  grid.addEventListener("pointercancel", end);
  grid.addEventListener("lostpointercapture", end);
}

function toggleBulkDate(date, selected) {
  if (selected) bulkSelected.add(date);
  else bulkSelected.delete(date);
  const cell = bulkCellByDate.get(date);
  if (cell) {
    cell.classList.toggle("selected", selected);
    cell.setAttribute("aria-pressed", String(selected));
    const row = rowOf(date);
    const { m, d, dow } = dateParts(row);
    cell.setAttribute("aria-label", m + "月" + d + "日（" + WEEK_LABELS[dow] + "）" + (selected ? "、選択中" : ""));
  }
  $("bk-count").textContent = bulkSelected.size + "日選択中";
}

/** 一覧表示の一括入力欄の select を複製する（選択肢を同じにする） */
function cloneBulkSelect(sourceId, id, slotId) {
  const original = $(sourceId);
  const slot = $(slotId);
  slot.innerHTML = "";
  if (!original) return;
  const copy = original.cloneNode(true);
  copy.id = id;
  copy.value = original.options[0] ? original.options[0].value : "";
  slot.appendChild(copy);
}

function openBulk() {
  // 表でチェック済みの日を引き継ぐ
  rows.forEach((row) => {
    const cb = row.querySelector(".row-checkbox");
    toggleBulkDate(row.dataset.date, !!(cb && cb.checked));
  });
  $("bk-start").value = "";
  $("bk-end").value = "";
  cloneBulkSelect("bulk-status", "bk-status", "bk-status-slot");
  cloneBulkSelect("bulk-correction", "bk-correction", "bk-correction-slot");
  $("bk-message").textContent = "";
  $("bk-message").className = "inline-message";
  if (typeof bulkDialog.showModal === "function") bulkDialog.showModal();
  else bulkDialog.setAttribute("open", "");
}

function applyBulkDialog() {
  const message = $("bk-message");
  if (bulkSelected.size === 0) {
    message.className = "inline-message error";
    message.textContent = "反映する日を選んでください。";
    return;
  }
  const status = $("bk-status");
  const correction = $("bk-correction");
  const nothing = !$("bk-start").value && !$("bk-end").value
    && (!status || status.value === "") && (!correction || correction.value === "keep");
  if (nothing) {
    message.className = "inline-message error";
    message.textContent = "出勤・退勤・勤務区分・補正CDのどれかを入力してください。";
    return;
  }

  // 選んだ日を表のチェックに移す
  rows.forEach((row) => {
    const cb = row.querySelector(".row-checkbox");
    const selected = bulkSelected.has(row.dataset.date);
    if (cb && cb.checked !== selected) {
      cb.checked = selected;
      cb.dispatchEvent(new Event("change", { bubbles: true }));
    }
  });
  // 一覧表示の一括入力欄に入れて、同じ処理で反映する
  const setValue = (id, value) => {
    const el = $(id);
    if (el) el.value = value;
  };
  setValue("bulk-start", $("bk-start").value);
  setValue("bulk-end", $("bk-end").value);
  setValue("bulk-status", status ? status.value : "");
  setValue("bulk-correction", correction ? correction.value : "keep");
  const count = bulkSelected.size;
  window.applyBulkInput();
  // 一覧表示の一括入力欄は、次に一覧で使うときのために空に戻す
  setValue("bulk-start", "");
  setValue("bulk-end", "");
  setValue("bulk-status", "");
  setValue("bulk-correction", "keep");

  bulkDialog.close();
  const result = $("mc-message");
  if (result) {
    result.className = "inline-message success";
    result.textContent = count + "日に反映しました。画面下の「保存」で保存してください。";
  }
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
  const toolbar = document.createElement("div");
  toolbar.className = "mc-toolbar";
  toolbar.appendChild(buildSwitch());
  const bulkButton = document.createElement("button");
  bulkButton.type = "button";
  bulkButton.className = "btn btn-secondary mc-bulk-btn";
  bulkButton.textContent = "📝 一括入力";
  bulkButton.addEventListener("click", openBulk);
  toolbar.appendChild(bulkButton);
  wrapper.parentNode.insertBefore(toolbar, wrapper);
  wrapper.parentNode.insertBefore(buildCalendar(), wrapper);
  buildDialog();
  buildBulkDialog();

  // 表で直接入力したときも、カレンダーに反映する
  ["input", "change"].forEach((type) => table.addEventListener(type, (e) => {
    const row = e.target.closest("tr");
    if (row) {
      renderDay(row);
      renderOvertimeAlert();
    }
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
