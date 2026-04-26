
export function getMinutes(t) {
  const [h, m] = t.split(':').map(Number);
  return h * 60 + m;
}

export function collectAttendanceRecords(staffId) {
  const rows = document.querySelectorAll("#calendar-log tbody tr");
  const records = [];

  rows.forEach(row => {
    const kintaidate = row.dataset.date;
    const week = row.querySelector("td:nth-child(3)").textContent.trim();
    const kintaifrom = row.querySelector("td:nth-child(4)").textContent.trim();
    const kintaito = row.querySelector("td:nth-child(5)").textContent.trim();
    const jikangaiStr = row.querySelector("td:nth-child(6)").textContent.trim();
    const statusSelect = row.querySelector("select[name='status']");
    const abstractId = statusSelect && statusSelect.value ? parseInt(statusSelect.value, 10) : null;
    
    // New fields
    const getInputValue = (name) => {
      const el = row.querySelector(`input[name='${name}'], textarea[name='${name}']`);
      return el ? el.value : null;
    };
    const memo = getInputValue('workDescription') || '';
    const correctionId = getInputValue('correctionId');
    const correctionUsTime = getInputValue('correctionUsTime');
    const correctionMidTime = getInputValue('correctionMidTime');
    const indirectTime = getInputValue('indirectTime');
    const totalWorkTime = getInputValue('totalWorkTime');
    const totalDirectWorkTime = getInputValue('totalDirectWorkTime');

    const parseNum = (val) => (val !== null && val !== '') ? Number(val) : null;

    let jikangai = 0;
    if (jikangaiStr && jikangaiStr !== '') {
      const [h, m] = jikangaiStr.split(':').map(Number);
      jikangai = h * 60 + m;
    }

    records.push({
      id: staffId,
      kintaidate,
      week,
      kintaifrom,
      kintaito,
      jikangai,
      abstractId, // Renamed from tekiyoukbn
      memo,
      correctionId: parseNum(correctionId),
      correctionUsTime: parseNum(correctionUsTime),
      correctionMidTime: parseNum(correctionMidTime),
      indirectTime: parseNum(indirectTime),
      totalWorkTime: parseNum(totalWorkTime),
      totalDirectWorkTime: parseNum(totalDirectWorkTime)
    });
  });

  return records;
}
