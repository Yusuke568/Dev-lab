document.addEventListener('DOMContentLoaded', () => {
  const rows = document.querySelectorAll("#calendar-log tbody tr");
  const today = new Date().toISOString().slice(0, 10);

  // 祝日リスト（必要ならサーバーから渡してもOK！）
  const holidays = ["2025-09-15", "2025-09-23"];

  rows.forEach(row => {
    const date = row.dataset.date;
    const weekText = row.cells[1].innerText.trim();

    if (date === today) {
      row.classList.add('today');
    }

    if (weekText === "SATURDAY") {
      row.classList.add('saturday');
    } else if (weekText === "SUNDAY") {
      row.classList.add('sunday');
    }

    if (holidays.includes(date)) {
      row.classList.add('holiday');
    }
  });
});
