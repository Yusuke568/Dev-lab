
export function getMinutes(t) {
  const [h, m] = t.split(':').map(Number);
  return h * 60 + m;
}

const STANDARD_WORK_MINUTES = 480; // 8時間
const BREAK_START = "12:00";
const BREAK_END = "13:00";

/** 出退勤時刻から休憩控除のみを行った実働時間（分）。補正は含まない。 */
function calculateRawWorkMinutes(start, end) {
  const s = getMinutes(start);
  const e = getMinutes(end);
  if (isNaN(s) || isNaN(e) || s >= e) return null;

  let total = e - s;

  const breakStart = getMinutes(BREAK_START);
  const breakEnd = getMinutes(BREAK_END);
  const overlapStart = Math.max(s, breakStart);
  const overlapEnd = Math.min(e, breakEnd);
  if (overlapStart < overlapEnd) {
    total -= (overlapEnd - overlapStart);
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
  const records = [];

  rows.forEach(row => {
    const kintaidate = row.dataset.date;
    const week = row.querySelector("td:nth-child(3)").textContent.trim();
    const kintaifrom = row.querySelector("td:nth-child(4)").textContent.trim();
    const kintaito = row.querySelector("td:nth-child(5)").textContent.trim();
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

    records.push({
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
    });
  });

  return records;
}
