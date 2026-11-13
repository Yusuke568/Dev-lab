/*
 * 残業事前申請：複数日をまとめて申請するカレンダー。
 *   「複数日をまとめて申請」を押すとカレンダーを開き、日をクリックして選ぶ（もう一度押すと外す）。
 *   選んだ日は hidden の targetDates（"2026-11-20,2026-11-21"）に入れる。開いている間は1日の「対象日」は使わない。
 *   申請中・承認済みの日（#ot-applied-dates）は選べない。
 */
(function () {
  "use strict";

  const toggle = document.getElementById("ot-multi-toggle");
  const panel = document.getElementById("ot-multi");
  const cal = document.getElementById("ot-cal");
  const title = document.getElementById("ot-cal-title");
  const hidden = document.getElementById("targetDates");
  const single = document.getElementById("targetDate");
  const singleGroup = document.getElementById("ot-single-date");
  const selectedText = document.getElementById("ot-multi-selected");
  const submit = document.getElementById("ot-submit");
  if (!toggle || !panel || !cal || !hidden || !single) return;

  const MAX = 31;
  const DOW = ["日", "月", "火", "水", "木", "金", "土"];
  let applied = {};
  try {
    applied = JSON.parse((document.getElementById("ot-applied-dates") || {}).textContent || "{}") || {};
  } catch (e) {
    applied = {};
  }

  const pad = (n) => String(n).padStart(2, "0");
  const iso = (y, m, d) => y + "-" + pad(m) + "-" + pad(d);
  const today = new Date();
  const todayIso = iso(today.getFullYear(), today.getMonth() + 1, today.getDate());

  const selected = new Set(hidden.value.split(",").map((s) => s.trim()).filter(Boolean));
  let view = (() => {
    const first = [...selected].sort()[0] || single.value || todayIso;
    const [y, m] = first.split("-").map(Number);
    return { y, m };
  })();

  function label(dateText) {
    const [, m, d] = dateText.split("-").map(Number);
    return m + "/" + d;
  }

  function sync() {
    const dates = [...selected].sort();
    hidden.value = dates.join(",");
    if (!panel.hidden) {
      selectedText.textContent = dates.length
        ? "選んだ日（" + dates.length + "日）：" + dates.map(label).join("・")
        : "申請する日をカレンダーで選んでください。";
      if (submit) submit.textContent = dates.length ? "選んだ " + dates.length + " 日を申請" : "申請";
    }
  }

  function render() {
    cal.textContent = "";
    title.textContent = view.y + "年" + view.m + "月";
    DOW.forEach((w, i) => {
      const h = document.createElement("div");
      h.className = "dow" + (i === 0 ? " sun" : i === 6 ? " sat" : "");
      h.textContent = w;
      cal.appendChild(h);
    });
    const first = new Date(view.y, view.m - 1, 1);
    for (let i = 0; i < first.getDay(); i++) cal.appendChild(document.createElement("div"));
    const days = new Date(view.y, view.m, 0).getDate();
    for (let d = 1; d <= days; d++) {
      const dateText = iso(view.y, view.m, d);
      const dow = new Date(view.y, view.m - 1, d).getDay();
      const btn = document.createElement("button");
      btn.type = "button";
      btn.className = "day" + (dow === 0 ? " sun" : dow === 6 ? " sat" : "")
        + (selected.has(dateText) ? " is-selected" : "") + (dateText === todayIso ? " is-today" : "");
      btn.dataset.date = dateText;
      btn.setAttribute("aria-pressed", selected.has(dateText) ? "true" : "false");
      const num = document.createElement("span");
      num.textContent = d;
      btn.appendChild(num);
      if (applied[dateText]) {
        btn.disabled = true;
        const note = document.createElement("small");
        note.textContent = applied[dateText];
        btn.appendChild(note);
        btn.title = dateText + " は" + applied[dateText] + "です";
      }
      btn.addEventListener("click", () => {
        if (selected.has(dateText)) {
          selected.delete(dateText);
        } else if (selected.size >= MAX) {
          alert("まとめて申請できるのは " + MAX + " 日までです。");
          return;
        } else {
          selected.add(dateText);
        }
        render();
        sync();
      });
      cal.appendChild(btn);
    }
  }

  function open() {
    panel.hidden = false;
    toggle.setAttribute("aria-expanded", "true");
    toggle.textContent = "1日だけ申請する";
    // 1日の対象日を選んでいれば、それもまとめて申請の日に入れる
    if (single.value && !applied[single.value]) selected.add(single.value);
    single.required = false;
    single.disabled = true;
    if (singleGroup) singleGroup.hidden = true;
    render();
    sync();
  }

  function close() {
    panel.hidden = true;
    toggle.setAttribute("aria-expanded", "false");
    toggle.textContent = "📅 複数日をまとめて申請";
    single.disabled = false;
    single.required = true;
    if (singleGroup) singleGroup.hidden = false;
    selected.clear();
    hidden.value = "";
    if (submit) submit.textContent = "申請";
  }

  toggle.addEventListener("click", () => (panel.hidden ? open() : close()));
  document.getElementById("ot-cal-prev").addEventListener("click", () => {
    view = view.m === 1 ? { y: view.y - 1, m: 12 } : { y: view.y, m: view.m - 1 };
    render();
  });
  document.getElementById("ot-cal-next").addEventListener("click", () => {
    view = view.m === 12 ? { y: view.y + 1, m: 1 } : { y: view.y, m: view.m + 1 };
    render();
  });
  // 何も選ばずに申請しようとしたら止める
  panel.closest("form").addEventListener("submit", (e) => {
    if (!panel.hidden && selected.size === 0) {
      e.preventDefault();
      alert("申請する日をカレンダーで選んでください。");
    }
  });

  // 申請できなかった日を選んだまま戻ってきたときは、開いた状態にする
  if (selected.size > 0) open();

  window.otMultiDates = { close };
})();
