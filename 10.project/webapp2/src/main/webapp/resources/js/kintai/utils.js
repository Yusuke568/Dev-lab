
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
const BREAK_START = "12:00";
const BREAK_END = "13:00";

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

  // 当日と翌日の昼休み（12:00〜13:00）のうち、勤務時間に重なる分を引く
  const breakStart = getMinutes(BREAK_START);
  const breakEnd = getMinutes(BREAK_END);
  for (const offset of [0, 24 * 60]) {
    const overlapStart = Math.max(s, breakStart + offset);
    const overlapEnd = Math.min(e, breakEnd + offset);
    if (overlapStart < overlapEnd) {
      total -= (overlapEnd - overlapStart);
    }
  }

  return total;
}

/**
 * 出退勤時刻から実働時間（分）を計算し、補正(通)+補正(深)の分数を加算する。
 * 派遣先ごとに勤務時間の計上ルールが異なるため、実績（出退勤時刻）に
 * 補正CDで登録された補正時間を加えて自社基準の勤務時間とする仕様。
 * 給与計上用（TOTAL_WORK_TIME）の値であり、時間外判定には使わない。
 */
export function calculateTotalWorkMinutes(start, end, correctionMinutes = 0) {
  const raw = calculateRawWorkMinutes(start, end);
  if (raw === null) return 0;
  return raw + (correctionMinutes || 0);
}

/**
 * 休憩控除後の実働時間（補正を含まない）のうち、所定労働時間（8時間）を超えた分を時間外として返す。
 * 補正は給与計算上の勤務時間の調整であって実際に働いた時間の長さとは無関係のため、
 * 時間外（要事前承認）の判定に補正を含めると、定時退勤日でも補正分だけ残業扱いになってしまう。
 */
export function calculateOvertime(start, end) {
  const raw = calculateRawWorkMinutes(start, end);
  if (raw === null) return 0;
  return raw > STANDARD_WORK_MINUTES ? raw - STANDARD_WORK_MINUTES : 0;
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

    const hasTime = !!(kintaifrom && kintaito);
    const jikangai = hasTime ? calculateOvertime(kintaifrom, kintaito) : 0;
    const totalWorkTime = hasTime ? calculateTotalWorkMinutes(kintaifrom, kintaito, correctionMinutes) : null;
    const indirectTimeNum = parseNum(indirectTime);
    const totalDirectWorkTime = (totalWorkTime !== null) ? totalWorkTime - (indirectTimeNum || 0) : null;

    return {
      id: staffId,
      kintaidate,
      week,
      kintaifrom,
      kintaito,
      jikangai, // 実働時間（補正を含まない）のうち8時間を超えた分（時間外）
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
