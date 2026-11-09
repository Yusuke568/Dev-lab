
/** 端末の時刻での日付（YYYY-MM-DD）。toISOString() は UTC なので、日本時間の朝9時前は前日になってしまう。 */
export function localDateString(date = new Date()) {
  return date.getFullYear() + "-" + String(date.getMonth() + 1).padStart(2, "0") + "-" +
    String(date.getDate()).padStart(2, "0");
}

/** 端末の時刻での現在時刻（HH:mm）。 */
export function currentTimeString(date = new Date()) {
  return String(date.getHours()).padStart(2, "0") + ":" + String(date.getMinutes()).padStart(2, "0");
}

/** 行の出勤・退勤の入力欄。 */
export function getTimeInput(row, name) {
  return row.querySelector(`input[name='${name}']`);
}

export function getMinutes(t) {
  const [h, m] = t.split(':').map(Number);
  return h * 60 + m;
}

const STANDARD_WORK_MINUTES = 480; // 8時間
/**
 * 休憩時間帯。サーバー（BreakSchedule）から勤怠画面の data-break-periods で受け取る。
 * 受け取れないときは同じ値を使う。
 */
const DEFAULT_BREAK_PERIODS = [
  ["05:00", "05:30"], ["08:30", "09:00"], ["12:00", "13:00"],
  ["18:00", "18:30"], ["22:00", "22:30"], ["02:00", "03:00"]
];

function breakPeriods() {
  const root = typeof document !== "undefined" ? document.getElementById("kintai-app-root") : null;
  try {
    const periods = root && root.dataset.breakPeriods ? JSON.parse(root.dataset.breakPeriods) : null;
    if (Array.isArray(periods) && periods.length > 0) return periods;
  } catch (e) {
    // 読めなければ既定の休憩時間帯
  }
  return DEFAULT_BREAK_PERIODS;
}

/** 退勤が出勤より前の時刻なら、日をまたいで翌日に退勤したとみなす（例：9:00〜翌1:00）。 */
export function isNextDayEnd(start, end) {
  if (!start || !end) return false;
  const s = getMinutes(start);
  const e = getMinutes(end);
  return !isNaN(s) && !isNaN(e) && e < s;
}

/** 出退勤時刻から休憩控除のみを行った実働時間（分）。補正は含まない。日またぎにも対応する。 */
function calculateRawWorkMinutes(start, end) {
  const s = getMinutes(start);
  let e = getMinutes(end);
  if (isNaN(s) || isNaN(e) || s === e) return null;
  if (e < s) e += 24 * 60; // 翌日の退勤

  let total = e - s;

  // 当日と翌日の休憩時間帯のうち、勤務時間に重なる分を引く
  for (const offset of [0, 24 * 60]) {
    for (const [breakFrom, breakTo] of breakPeriods()) {
      const overlapStart = Math.max(s, getMinutes(breakFrom) + offset);
      const overlapEnd = Math.min(e, getMinutes(breakTo) + offset);
      if (overlapStart < overlapEnd) {
        total -= (overlapEnd - overlapStart);
      }
    }
  }

  return total;
}

/**
 * 総勤務時間（分）＝ 休憩控除後の実働時間 ＋ addMinutes。
 * addMinutes には補正(通)＋補正(深)＋勤務時間(間接) を渡す（作業報告書の (e) と同じ。サーバーの AttendanceRecord と同じ計算）。
 */
export function calculateTotalWorkMinutes(start, end, addMinutes = 0) {
  const raw = calculateRawWorkMinutes(start, end);
  if (raw === null) return 0;
  return raw + (addMinutes || 0);
}

/**
 * 時間外（分）＝ 総勤務時間のうち所定労働時間（8時間）を超えた分。補正と間接作業の時間も含めて数える。
 * 例：9:00〜23:00、補正(通)+0:30、補正(深)+0:30 → 総勤務 13:00 → 時間外 5:00
 */
export function calculateOvertime(start, end, addMinutes = 0) {
  const raw = calculateRawWorkMinutes(start, end);
  if (raw === null) return 0;
  return Math.max(0, raw + (addMinutes || 0) - STANDARD_WORK_MINUTES);
}

export function collectAttendanceRecords(staffId) {
  const rows = document.querySelectorAll("#calendar-log tbody tr");
  return Array.from(rows).map(row => collectRowRecord(row, staffId));
}

/** 1日分（表の1行）の入力内容を、保存APIに送る形にまとめる。 */
export function collectRowRecord(row, staffId) {
    const kintaidate = row.dataset.date;
    const week = row.dataset.week;
    const fromInput = getTimeInput(row, "kintaifrom");
    const toInput = getTimeInput(row, "kintaito");
    const kintaifrom = fromInput ? fromInput.value : "";
    const kintaito = toInput ? toInput.value : "";
    const statusSelect = row.querySelector("select[name='status']");
    const abstractId = statusSelect && statusSelect.value ? parseInt(statusSelect.value, 10) : null;

    // New fields
    const getInputValue = (name) => {
      const el = row.querySelector(`input[name='${name}'], textarea[name='${name}'], select[name='${name}']`);
      return el ? el.value : null;
    };
    const memo = getInputValue('workDescription') || '';
    const correctionId = getInputValue('correctionId');
    const correctionUsTime = getInputValue('correctionUsTime'); // "HH:mm" 形式
    const correctionMidTime = getInputValue('correctionMidTime'); // "HH:mm" 形式
    const indirectTime = getInputValue('indirectTime');

    const parseNum = (val) => (val !== null && val !== '') ? Number(val) : null;
    const parseTimeToMinutes = (val) => (val !== null && val !== '') ? getMinutes(val) : null;

    const correctionUsTimeNum = parseTimeToMinutes(correctionUsTime);
    const correctionMidTimeNum = parseTimeToMinutes(correctionMidTime);
    const correctionMinutes = (correctionUsTimeNum || 0) + (correctionMidTimeNum || 0);
    // 勤務時間(間接) は "HH:mm" で入力する
    const indirectTimeNum = parseTimeToMinutes(indirectTime);
    const addMinutes = correctionMinutes + (indirectTimeNum || 0);

    const hasTime = !!(kintaifrom && kintaito);
    const jikangai = hasTime ? calculateOvertime(kintaifrom, kintaito, addMinutes) : 0;
    const totalWorkTime = hasTime ? calculateTotalWorkMinutes(kintaifrom, kintaito, addMinutes) : null;
    const totalDirectWorkTime = (totalWorkTime !== null) ? totalWorkTime - (indirectTimeNum || 0) : null;

    return {
      id: staffId,
      kintaidate,
      week,
      kintaifrom,
      kintaito,
      jikangai, // 総勤務時間（補正・間接を含む）のうち8時間を超えた分（時間外）
      abstractId, // Renamed from tekiyoukbn
      memo,
      correctionId: parseNum(correctionId),
      correctionUsTime: correctionUsTimeNum,
      correctionMidTime: correctionMidTimeNum,
      indirectTime: indirectTimeNum,
      totalWorkTime,
      totalDirectWorkTime
    };
}

// -------- 週40時間（休日出勤の日の時間外） --------

const WEEKLY_MINUTES = 40 * 60;

/** "YYYY-MM-DD" を、その日を含む週の日曜日の "YYYY-MM-DD" にする（週は日曜〜土曜） */
function weekStartOf(dateText) {
  const [y, m, d] = dateText.split("-").map(Number);
  const date = new Date(y, m - 1, d);
  date.setDate(date.getDate() - date.getDay());
  return localDateString(date);
}

function isWeekdayText(dateText) {
  const [y, m, d] = dateText.split("-").map(Number);
  const dow = new Date(y, m - 1, d).getDay();
  return dow !== 0 && dow !== 6;
}

/**
 * 休日出勤の日（土日・祝日・会社休日）の時間外を、週40時間の決まりで計算する（サーバーの WeeklyOvertimeCalculator と同じ）。
 *   月〜金の「所定に数える時間」（その日の総勤務のうち8時間まで。休日の平日・有給は8時間、半休は4時間＋働いた分）を足し、
 *   休日出勤の勤務は日付順に40時間に足りない分を埋め（1日8時間まで）、残りを時間外にする。
 * 表にない前月・翌月の日は、サーバーが #kintai-app-root の data-outside-week-days に入れた分を使う。
 * @returns {Map<string, number>} 休日の日付 → 時間外（分）
 */
export function holidayOvertimeByDate() {
  const root = document.getElementById("kintai-app-root");
  let workTypes = [];
  let outside = [];
  try { workTypes = JSON.parse((root && root.dataset.workTypesJson) || "[]"); } catch (e) { workTypes = []; }
  try { outside = JSON.parse((root && root.dataset.outsideWeekDays) || "[]"); } catch (e) { outside = []; }
  const leaveCredit = new Map(workTypes.map((w) => [String(w.id), Math.round((Number(w.paidLeaveDays) || 0) * STANDARD_WORK_MINUTES)]));

  const days = outside.map((d) => ({ date: d.date, total: Number(d.total) || 0, holiday: !!d.holiday, leave: Number(d.leave) || 0 }));
  document.querySelectorAll("#calendar-log tbody tr").forEach((row) => {
    if (!row.dataset.date) return;
    const rec = collectRowRecord(row, null);
    days.push({
      date: row.dataset.date,
      total: rec.totalWorkTime || 0,
      holiday: row.classList.contains("saturday") || row.classList.contains("sunday") || row.classList.contains("holiday"),
      leave: rec.abstractId != null ? (leaveCredit.get(String(rec.abstractId)) || 0) : 0
    });
  });

  const weeks = new Map();
  days.forEach((d) => {
    const key = weekStartOf(d.date);
    if (!weeks.has(key)) weeks.set(key, []);
    weeks.get(key).push(d);
  });

  const result = new Map();
  weeks.forEach((week) => {
    const baseline = week.reduce((sum, d) => {
      if (!isWeekdayText(d.date)) return sum;
      return sum + (d.holiday ? STANDARD_WORK_MINUTES : Math.min(STANDARD_WORK_MINUTES, d.total + d.leave));
    }, 0);
    let remaining = Math.max(0, WEEKLY_MINUTES - baseline);
    week.filter((d) => d.holiday).sort((a, b) => a.date.localeCompare(b.date)).forEach((d) => {
      const regular = Math.min(d.total, remaining, STANDARD_WORK_MINUTES);
      remaining -= regular;
      result.set(d.date, d.total - regular);
    });
  });
  return result;
}
