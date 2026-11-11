/*
 * 勤怠承認画面：残業事前申請ごとに、申請者のその月の時間外の累計を折れ線グラフで示す。
 *   実績   … 勤怠に保存されている時間外の累計（実線。今日まで、過去の月は月末まで）
 *   見込み … 実績に、承認済の予定・ほかの申請中・この申請を、その日に足した累計（点線。月末の値が見込みの合計）
 *   予定の日には点を打つ（承認済の予定・ほかの申請中・この申請で色を分ける）
 *   横の基準線：承認ルートの「当月の時間外が○時間以上」の段と、労働基準法の原則の上限（月45時間）
 * データはサーバーが <script type="application/json" id="overtime-status"> に入れる（申請ID → 状況）。
 * 文字はすべて textContent で入れる（HTML として解釈しない）。
 */
(function () {
  "use strict";

  const dataEl = document.getElementById("overtime-status");
  if (!dataEl) return;
  let data = {};
  try { data = JSON.parse(dataEl.textContent || "{}"); } catch (e) { return; }

  const SVG = "http://www.w3.org/2000/svg";
  const KIND = {
    APPROVED: { label: "承認済の予定", cls: "k-approved" },
    PENDING: { label: "ほかの申請中", cls: "k-pending" },
    THIS: { label: "この申請", cls: "k-this" }
  };

  function hm(minutes) {
    const m = Math.max(0, Math.round(minutes));
    return Math.floor(m / 60) + ":" + String(m % 60).padStart(2, "0");
  }
  function hoursLabel(minutes) {
    const h = minutes / 60;
    return (Number.isInteger(h) ? String(h) : String(Math.round(h * 10) / 10)) + "h";
  }
  function el(tag, cls, text) {
    const e = document.createElement(tag);
    if (cls) e.className = cls;
    if (text !== undefined) e.textContent = text;
    return e;
  }
  function svg(tag, attrs, text) {
    const e = document.createElementNS(SVG, tag);
    Object.entries(attrs || {}).forEach(([k, v]) => e.setAttribute(k, v));
    if (text !== undefined) e.textContent = text;
    return e;
  }

  // ツールチップ（1つを使い回す）
  const tip = el("div", "otc-tip");
  tip.setAttribute("role", "tooltip");
  tip.hidden = true;
  document.body.appendChild(tip);
  function showTip(clientX, clientY, lines) {
    tip.innerHTML = "";
    lines.forEach((line, i) => tip.appendChild(el("div", i === 0 ? "otc-tip-title" : "", line)));
    tip.hidden = false;
    const t = tip.getBoundingClientRect();
    let left = clientX + 14 + window.scrollX;
    if (left + t.width > window.scrollX + document.documentElement.clientWidth - 8) left = clientX - t.width - 14 + window.scrollX;
    tip.style.left = left + "px";
    tip.style.top = (clientY + window.scrollY - t.height - 10) + "px";
  }
  function hideTip() { tip.hidden = true; }

  /** 実績を描く最後の日：今月なら今日、過去の月は月末、先の月は0日 */
  function lastActualDay(year, month, days) {
    const now = new Date();
    const ym = year * 12 + month;
    const nowYm = now.getFullYear() * 12 + (now.getMonth() + 1);
    if (ym < nowYm) return days;
    if (ym > nowYm) return 0;
    return now.getDate();
  }

  function render(container, s) {
    const days = s.actualByDay.length;
    const plannedByDay = new Array(days).fill(0);
    s.planned.forEach((p) => { plannedByDay[p.day - 1] += p.minutes; });
    // 累計（添字 d が d日の終わりの値、0 は月初め）
    const actualCum = [0];
    const projectedCum = [0];
    for (let d = 1; d <= days; d++) {
      actualCum.push(actualCum[d - 1] + s.actualByDay[d - 1]);
      projectedCum.push(projectedCum[d - 1] + s.actualByDay[d - 1] + plannedByDay[d - 1]);
    }
    const total = projectedCum[days];
    const lastDay = Math.max(lastActualDay(s.year, s.monthValue, days), 0);

    const root = el("div", "otc");
    root.setAttribute("aria-label", (s.name || "") + "さんの" + s.month + "の時間外の累計");

    // 見出し：見込みの合計と、いちばん近い基準まで
    const head = el("div", "otc-head");
    head.appendChild(el("strong", "", s.month + "の時間外の見込み（この申請を含む）：" + hm(total)));
    const next = s.thresholds.find((t) => total < t.minutes);
    const crossed = s.thresholds.filter((t) => total >= t.minutes);
    if (crossed.length) {
      const last = crossed[crossed.length - 1];
      head.appendChild(el("span", "otc-flag " + (last.law ? "is-law" : "is-route"),
        (last.law ? "⚠ " : "▲ ") + hoursLabel(last.minutes) + "（" + (last.law ? "労基法の上限" : "追加の承認") + "）を " + hm(total - last.minutes) + " 超えます"));
    }
    if (next) head.appendChild(el("span", "otc-next", hoursLabel(next.minutes) + " まで あと " + hm(next.minutes - total)));
    root.appendChild(head);

    // ---- グラフ（SVG） ----
    const W = 720, H = 200, ml = 40, mr = 118, mt = 10, mb = 26;
    const pw = W - ml - mr, ph = H - mt - mb;
    const maxThreshold = Math.max(0, ...s.thresholds.map((t) => t.minutes));
    const yMax = Math.ceil(Math.max(maxThreshold * 1.1, total * 1.08, 10 * 60) / 600) * 600; // 10時間刻み
    const x = (d) => ml + (d / days) * pw;
    const y = (m) => mt + ph - (m / yMax) * ph;

    const chart = svg("svg", { viewBox: "0 0 " + W + " " + H, class: "otc-svg", role: "img",
      "aria-label": s.month + "の時間外の累計。見込み " + hm(total) });
    // 横の目盛り線と目盛り
    for (let m = 0; m <= yMax; m += 600) {
      chart.appendChild(svg("line", { x1: ml, x2: ml + pw, y1: y(m), y2: y(m), class: "otc-grid" }));
      chart.appendChild(svg("text", { x: ml - 6, y: y(m) + 4, class: "otc-tick", "text-anchor": "end" }, hoursLabel(m)));
    }
    [1, 5, 10, 15, 20, 25, days].filter((d, i, a) => d <= days && a.indexOf(d) === i && !(d !== days && days - d < 3)).forEach((d) => {
      chart.appendChild(svg("text", { x: x(d), y: H - 8, class: "otc-tick", "text-anchor": "middle" }, d + "日"));
    });
    // 基準線（右に名前）
    s.thresholds.forEach((t) => {
      chart.appendChild(svg("line", { x1: ml, x2: ml + pw, y1: y(t.minutes), y2: y(t.minutes),
        class: "otc-ref" + (t.law ? " is-law" : "") }));
      chart.appendChild(svg("text", { x: ml + pw + 6, y: y(t.minutes) + 4, class: "otc-ref-label" + (t.law ? " is-law" : "") },
        hoursLabel(t.minutes) + " " + (t.law ? "労基法の上限" : "追加の承認")));
    });
    // 見込み（点線、月末まで）
    const proj = projectedCum.map((v, d) => (d === 0 ? "M" : "L") + x(d).toFixed(1) + "," + y(v).toFixed(1)).join(" ");
    chart.appendChild(svg("path", { d: proj, class: "otc-proj" }));
    // 実績（実線、lastDay まで）
    if (lastDay > 0) {
      const act = actualCum.slice(0, lastDay + 1).map((v, d) => (d === 0 ? "M" : "L") + x(d).toFixed(1) + "," + y(v).toFixed(1)).join(" ");
      chart.appendChild(svg("path", { d: act, class: "otc-actual" }));
      chart.appendChild(svg("circle", { cx: x(lastDay), cy: y(actualCum[lastDay]), r: 4, class: "otc-dot k-actual" }));
    }
    // 予定の点（見込みの線の上）
    s.planned.forEach((p) => {
      const k = KIND[p.kind] || KIND.PENDING;
      chart.appendChild(svg("circle", { cx: x(p.day), cy: y(projectedCum[p.day]), r: p.kind === "THIS" ? 6 : 4.5, class: "otc-dot " + k.cls }));
      if (p.kind === "THIS") {
        chart.appendChild(svg("text", { x: x(p.day), y: y(projectedCum[p.day]) - 10, class: "otc-this-label", "text-anchor": "middle" },
          "この申請 +" + hm(p.minutes)));
      }
    });
    // 見込みの終わりの値
    chart.appendChild(svg("text", { x: x(days) - 4, y: y(total) - 8, class: "otc-end-label", "text-anchor": "end" }, "見込み " + hm(total)));

    // マウスを乗せた日の値（縦線＋ツールチップ）
    const cross = svg("line", { y1: mt, y2: mt + ph, class: "otc-cross", visibility: "hidden" });
    chart.appendChild(cross);
    const hit = svg("rect", { x: ml, y: mt, width: pw, height: ph, class: "otc-hit" });
    hit.addEventListener("mousemove", (e) => {
      const r = chart.getBoundingClientRect();
      const vx = ((e.clientX - r.left) / r.width) * W;
      const d = Math.min(days, Math.max(1, Math.round(((vx - ml) / pw) * days)));
      cross.setAttribute("x1", x(d));
      cross.setAttribute("x2", x(d));
      cross.setAttribute("visibility", "visible");
      const lines = [s.monthValue + "/" + d];
      if (d <= lastDay) lines.push("実績の累計 " + hm(actualCum[d]) + (s.actualByDay[d - 1] ? "（この日 +" + hm(s.actualByDay[d - 1]) + "）" : ""));
      lines.push("見込みの累計 " + hm(projectedCum[d]));
      s.planned.filter((p) => p.day === d).forEach((p) => lines.push((KIND[p.kind] || KIND.PENDING).label + " +" + hm(p.minutes)));
      showTip(e.clientX, e.clientY, lines);
    });
    hit.addEventListener("mouseleave", () => { cross.setAttribute("visibility", "hidden"); hideTip(); });
    chart.appendChild(hit);
    root.appendChild(chart);

    // 凡例と数値（色だけに頼らず数値も文字で出す）
    const legend = el("div", "otc-legend");
    const item = (cls, label) => {
      const i = el("span", "otc-legend-item");
      i.appendChild(el("i", "otc-swatch " + cls));
      i.appendChild(el("span", "", label));
      legend.appendChild(i);
    };
    item("line-actual", "実績 " + hm(s.actual));
    item("line-proj", "見込み " + hm(total));
    item("k-approved", "承認済の予定 " + hm(s.approved));
    item("k-pending", "ほかの申請中 " + hm(s.pending));
    item("k-this", "この申請 " + hm(s.thisRequest));
    root.appendChild(legend);
    container.appendChild(root);
  }

  document.querySelectorAll(".ot-chart[data-request-id]").forEach((container) => {
    const s = data[container.dataset.requestId];
    if (s && Array.isArray(s.actualByDay)) render(container, s);
    else container.closest("tr")?.remove();
  });
})();
