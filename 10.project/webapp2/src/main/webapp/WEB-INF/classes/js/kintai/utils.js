
export function getMinutes(t) {
  const [h, m] = t.split(':').map(Number);
  return h * 60 + m;
}

export function collectAttendanceRecords() {
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
    const correctionId = row.querySelector("input[name='correctionId']")?.value;
    const correctionUsTime = row.querySelector("input[name='correctionUsTime']")?.value;
    const correctionMidTime = row.querySelector("input[name='correctionMidTime']")?.value;
    const indirectTime = row.querySelector("input[name='indirectTime']")?.value;
    const totalWorkTime = row.querySelector("input[name='totalWorkTime']")?.value;
    const totalDirectWorkTime = row.querySelector("input[name='totalDirectWorkTime']")?.value;


    let jikangai = 0;
    if (jikangaiStr && jikangaiStr !== '') {
      const [h, m] = jikangaiStr.split(':').map(Number);
      jikangai = h * 60 + m;
    }

    records.push({
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
