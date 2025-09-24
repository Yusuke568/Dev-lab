
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
    const tekiyoukbn = row.querySelector("select[name='status']").value;
    const memo = row.querySelector("td:nth-child(7)").textContent.trim();

    let jikangai = 0;
    if (jikangaiStr) {
      const [h, m] = jikangaiStr.split(':').map(Number);
      jikangai = h * 60 + m;
    }

    records.push({
      kintaidate,
      week,
      kintaifrom,
      kintaito,
      jikangai,
      tekiyoukbn,
      memo
    });
  });

  return records;
}
