
export function getMinutes(t) {
  const [h, m] = t.split(':').map(Number);
  return h * 60 + m;
}

export function collectAttendanceRecords(staffId) {
  const rows = document.querySelectorAll("#calendar-log tbody tr");
  const records = [];

  rows.forEach(row => {
    const kintaidate = row.dataset.date;
    const week = row.querySelector("td:nth-child(2)").textContent.trim();
    const kintaifrom = row.querySelector("td:nth-child(3)").innerText.trim();
    const kintaito = row.querySelector("td:nth-child(4)").innerText.trim();
    const jikangaiStr = row.querySelector("td:nth-child(5)").innerText.trim();
    const abstractId = parseInt(row.querySelector("select[name='status']").value, 10); // Capture as int
    const memo = row.querySelector("td:nth-child(7)").textContent.trim();
    // New fields
    const getInputValue = (name) => {
      const el = row.querySelector(`input[name='${name}']`);
      return el ? el.value : null;
    };
    const correctionId = getInputValue('correctionId');
    const correctionUsTime = getInputValue('correctionUsTime');
    const correctionMidTime = getInputValue('correctionMidTime');
    const indirectTime = getInputValue('indirectTime');
    const totalWorkTime = getInputValue('totalWorkTime');
    const totalDirectWorkTime = getInputValue('totalDirectWorkTime');


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
      correctionId: correctionId === '' ? null : Number(correctionId),
      correctionUsTime: correctionUsTime === '' ? null : Number(correctionUsTime),
      correctionMidTime: correctionMidTime === '' ? null : Number(correctionMidTime),
      indirectTime: indirectTime === '' ? null : Number(indirectTime),
      totalWorkTime: totalWorkTime === '' ? null : Number(totalWorkTime),
      totalDirectWorkTime: totalDirectWorkTime === '' ? null : Number(totalDirectWorkTime)
    });
  });

  return records;
}
