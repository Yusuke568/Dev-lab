/*
 * 承認設定画面（master_approval_settings.jsp）。
 *
 * サーバーが埋め込んだ JSON（#settings-data）から、4つのタブを描く。
 *   ・承認ルート … 段ごとの承認者を決めたルート。部署か社員に割り当てて使う（ふだんの設定はここ）
 *   ・部署ごとの割り当て … 部署に割り当てたルート（上位部署から引き継ぐものも表示）
 *   ・社員ごとの確認 … 全社員の実際の承認者と決まり方。所属・兼任・社員個別のルートをここで変える
 *   ・組織図（割り当てなし用） … ルートを割り当てていない社員は、部署の承認者・副承認者を段数までたどる
 * 保存はふつうのフォーム送信（POST /masterApprovalSettings.do、mode で振り分け）。
 * 文字はすべて textContent で入れる（HTML として解釈しない）。
 */
(function () {
  "use strict";

  const root = document.getElementById("approval-settings");
  const dataEl = document.getElementById("settings-data");
  if (!root || !dataEl) return;
  const D = JSON.parse(dataEl.textContent);

  const ACTION = (D.contextPath || "") + "/masterApprovalSettings.do";
  const TYPES = Object.keys(D.requestTypes); // ["OVERTIME", "LEAVE"]
  const staffById = new Map(D.staff.map((s) => [s.id, s]));
  const deptById = new Map(D.departments.map((d) => [d.id, d]));
  const routeById = new Map(D.routes.map((r) => [r.id, r]));

  // ------------------------------------------------------------ 小さな道具

  /** 要素を作る。attrs の on* はイベント、text は textContent */
  function el(tag, attrs, ...children) {
    const e = document.createElement(tag);
    Object.entries(attrs || {}).forEach(([k, v]) => {
      if (v === null || v === undefined || v === false) return;
      if (k === "text") e.textContent = v;
      else if (k.startsWith("on")) e.addEventListener(k.slice(2), v);
      else if (k === "className") e.className = v;
      else e.setAttribute(k, v === true ? "" : v);
    });
    children.flat().forEach((c) => {
      if (c === null || c === undefined || c === false) return;
      e.appendChild(typeof c === "string" ? document.createTextNode(c) : c);
    });
    return e;
  }

  function staffName(id) {
    const s = staffById.get(id);
    return s ? s.name : "（社員ID " + id + "・社員マスタにいません）";
  }
  function staffLabel(id) {
    const s = staffById.get(id);
    return s ? s.id + " " + s.name : String(id);
  }
  function deptName(id) {
    const d = deptById.get(id);
    return d ? d.name : "（なし）";
  }
  function deptOptionLabel(d) {
    return "　".repeat(d.depth || 0) + (d.depth ? "└ " : "") + d.name;
  }
  /** 段ごとの承認者を「鈴木／高橋」の並びにする */
  function levelText(level) {
    return level.map(staffName).join("／");
  }
  function hidden(name, value) {
    return el("input", { type: "hidden", name: name, value: value === null || value === undefined ? "" : String(value) });
  }

  /** 承認者のうち社員マスタにいない人（退職者など） */
  function missingApprovers(levels) {
    return levels.flat().filter((id) => !staffById.has(id));
  }

  // ------------------------------------------------------------ 社員ごとのルートの状況

  function preview(type, staffId) {
    return (D.previews[type] || {})[String(staffId)] || { levels: [], error: "不明" };
  }
  /** 社員がどちらかの申請でルートが決まらない・社員マスタにいない承認者に回るなら true */
  function staffHasProblem(staffId) {
    return TYPES.some((t) => {
      const p = preview(t, staffId);
      return !!p.error || missingApprovers(p.levels).length > 0;
    });
  }
  /** 社員の申請がどれかの承認ルートで決まっていれば true（組織図で決まる人は false） */
  function usesRoute(staffId) {
    return TYPES.some((t) => {
      const p = preview(t, staffId);
      return p.source && p.source.startsWith("承認ルート");
    });
  }

  // ------------------------------------------------------------ ルートの割り当て

  function appliesTo(r, t) {
    return r.type === null || r.type === t;
  }
  /** 社員・部署に直接割り当てたルート（申請種別を指定したルートを優先。サーバーの決め方と同じ） */
  function directRoute(kind, id, t) {
    const list = D.routes.filter((r) => appliesTo(r, t) && (kind === "dept" ? r.deptIds : r.staffIds).includes(id));
    list.sort((a, b) => ((a.type === null) - (b.type === null)) || (a.id - b.id));
    return list[0] || null;
  }
  /** 部署に効くルート。なければ上位部署の割り当てをたどる → { route, from（割り当てた部署） } か null */
  function deptRoute(deptId, t) {
    const seen = new Set();
    let cur = deptId;
    while (cur !== null && cur !== undefined && !seen.has(cur)) {
      seen.add(cur);
      const r = directRoute("dept", cur, t);
      if (r) return { route: r, from: cur };
      const d = deptById.get(cur);
      cur = d ? d.parentId : null;
    }
    return null;
  }
  /** ルートを使っている社員の数（実際の決まり方から数える） */
  function routeUserCount(r) {
    const key = "承認ルート「" + r.name + "」";
    return D.staff.filter((s) => TYPES.some((t) => (preview(t, s.id).source || "").startsWith(key))).length;
  }
  function routeSteps(r) {
    return r.levels.map(levelText).join(" → ");
  }

  // ------------------------------------------------------------ 画面全体

  const state = {
    tab: ["routes", "depts", "staff", "org"].includes(D.tab) ? D.tab : "routes",
    type: "OVERTIME",
    dept: "",
    q: "",
    onlyProblems: false
  };

  function render() {
    root.innerHTML = "";
    root.appendChild(renderSummary());
    root.appendChild(renderTabs());
    root.appendChild(renderRoutesPanel());
    root.appendChild(renderDeptsPanel());
    root.appendChild(renderStaffPanel());
    root.appendChild(renderOrgPanel());
  }

  function renderSummary() {
    const problems = D.staff.filter((s) => staffHasProblem(s.id)).length;
    const byOrg = D.staff.filter((s) => !usesRoute(s.id)).length;
    const deptsWithoutRoute = D.departments.filter((d) => TYPES.some((t) => !deptRoute(d.id, t))).length;
    const go = (tab, only) => () => {
      state.tab = tab;
      if (only !== undefined) state.onlyProblems = only;
      render();
    };
    return el("div", { className: "as-summary" },
      el("button", { type: "button", className: "as-stat" + (problems ? " ng" : ""), onclick: go("staff", true) },
        el("div", { className: "num", text: String(problems) }),
        el("div", { className: "lbl", text: problems ? "承認者が決まらない・いない人に回る社員（押すと絞り込み）" : "承認者が決まらない社員はいません" })),
      el("button", { type: "button", className: "as-stat", onclick: go("routes") },
        el("div", { className: "num", text: String(D.routes.length) }),
        el("div", { className: "lbl", text: "承認ルート" })),
      el("button", { type: "button", className: "as-stat", onclick: go("depts") },
        el("div", { className: "num", text: String(D.departments.length) }),
        el("div", { className: "lbl", text: "部署" + (deptsWithoutRoute ? "（ルート未割り当て " + deptsWithoutRoute + "部署 → 組織図で決まる）" : "（すべてルート割り当て済み）") })),
      el("button", { type: "button", className: "as-stat", onclick: go("staff", false) },
        el("div", { className: "num", text: String(D.staff.length) }),
        el("div", { className: "lbl", text: "社員" + (byOrg ? "（組織図で決まる " + byOrg + "人）" : "（全員ルートで決まる）") })));
  }

  function renderTabs() {
    const tabs = [["routes", "承認ルート"], ["depts", "部署ごとの割り当て"], ["staff", "社員ごとの確認"], ["org", "組織図（割り当てなし用）"]];
    return el("div", { className: "as-tabs", role: "tablist" },
      tabs.map(([key, label]) => el("button", {
        type: "button", role: "tab", id: "tab-" + key, "aria-controls": "panel-" + key,
        "aria-selected": String(state.tab === key),
        onclick: () => { state.tab = key; render(); }
      }, label)));
  }

  // ------------------------------------------------------------ タブ1：社員ごとの承認ルート

  function renderStaffPanel() {
    const panel = el("section", { className: "as-panel", id: "panel-staff", role: "tabpanel", "aria-labelledby": "tab-staff" });
    panel.hidden = state.tab !== "staff";

    const typeSeg = el("div", { className: "as-seg", role: "group", "aria-label": "申請種別" },
      TYPES.map((t) => el("button", {
        type: "button", "aria-pressed": String(state.type === t),
        onclick: () => { state.type = t; render(); }
      }, D.requestTypes[t])));
    const deptSel = el("select", { className: "form-input", id: "as-filter-dept", onchange: (e) => { state.dept = e.target.value; renderStaffRows(); } },
      el("option", { value: "" }, "すべての部署"),
      D.departments.map((d) => el("option", { value: String(d.id), selected: String(d.id) === state.dept }, deptOptionLabel(d))));
    const search = el("input", { type: "search", className: "form-input", id: "as-filter-q", placeholder: "社員ID・氏名", value: state.q,
      oninput: (e) => { state.q = e.target.value; renderStaffRows(); } });
    const only = el("input", { type: "checkbox", id: "as-filter-ng", checked: state.onlyProblems,
      onchange: (e) => { state.onlyProblems = e.target.checked; renderStaffRows(); } });
    const count = el("span", { className: "as-count", id: "as-staff-count" });

    panel.appendChild(el("div", { className: "as-filters" },
      typeSeg,
      el("div", { className: "form-group" }, el("label", { className: "form-label", for: "as-filter-dept", text: "部署" }), deptSel),
      el("div", { className: "form-group" }, el("label", { className: "form-label", for: "as-filter-q", text: "検索" }), search),
      el("label", { for: "as-filter-ng", style: "display:flex;gap:6px;align-items:center;font-size:0.85rem;" }, only, "問題のある社員だけ"),
      count));

    const table = el("table", { className: "as-table" },
      el("thead", {}, el("tr", {},
        el("th", { text: "社員" }), el("th", { text: "所属" }), el("th", { text: D.requestTypes[state.type] + "の承認者" }),
        el("th", { text: "決まり方" }), el("th", {}))),
      el("tbody", { id: "as-staff-rows" }));
    panel.appendChild(el("div", { style: "overflow-x:auto;" }, table));
    // panel を root に付けたあとで行を描く
    setTimeout(renderStaffRows, 0);
    return panel;
  }

  function staffMatches(s) {
    if (state.dept) {
      const d = Number(state.dept);
      if (s.deptId !== d && !s.concurrent.includes(d)) return false;
    }
    if (state.q) {
      const q = state.q.trim().toLowerCase();
      if (!(String(s.id).includes(q) || (s.name || "").toLowerCase().includes(q))) return false;
    }
    if (state.onlyProblems && !staffHasProblem(s.id)) return false;
    return true;
  }

  function renderStaffRows() {
    const tbody = document.getElementById("as-staff-rows");
    if (!tbody) return;
    tbody.innerHTML = "";
    const rows = D.staff.filter(staffMatches);
    rows.forEach((s) => {
      const p = preview(state.type, s.id);
      const missing = missingApprovers(p.levels);
      const ng = !!p.error || missing.length > 0;

      let routeCell;
      if (p.error) routeCell = el("div", { className: "as-error", text: "⚠ " + p.error });
      else if (p.auto) routeCell = el("span", { className: "as-muted", text: "（承認者なし：本人が最上位のため自動承認）" });
      else routeCell = el("ol", { className: "as-route" }, p.levels.map((lv, i) => el("li", {}, (i + 1) + ". " + levelText(lv))));
      if (missing.length) routeCell = el("div", {}, routeCell, el("div", { className: "as-error", text: "⚠ 社員マスタにいない承認者がいます（社員ID " + missing.join("・") + "）" }));
      // 表示していない申請種別の問題も知らせる（押すとその種別に切り替える）
      const otherProblems = TYPES.filter((t) => t !== state.type).filter((t) => {
        const o = preview(t, s.id);
        return !!o.error || missingApprovers(o.levels).length > 0;
      });
      if (otherProblems.length) {
        routeCell = el("div", {}, routeCell, otherProblems.map((t) => el("button", {
          type: "button", className: "as-tag warn", style: "border:0;cursor:pointer;",
          onclick: () => { state.type = t; render(); }
        }, "⚠ " + D.requestTypes[t] + "に問題あり（切り替える）")));
      }

      let how;
      if (p.error) how = el("span", { className: "as-tag ng", text: "決まらない" });
      else if (p.source && p.source.startsWith("承認ルート")) how = el("span", { className: "as-tag ex", text: p.source.replace("承認ルート", "ルート") });
      else if (p.auto) how = el("span", { className: "as-tag auto", text: "自動承認" });
      else how = el("span", { className: "as-tag org", text: "組織図" });

      tbody.appendChild(el("tr", { className: ng ? "ng" : "" },
        el("td", { "data-label": "社員", className: "as-who" }, el("strong", { text: s.name }), el("div", { className: "as-muted", text: "ID " + s.id })),
        el("td", { "data-label": "所属", className: "as-dept" },
          s.deptId ? el("div", { text: deptName(s.deptId) }) : el("div", { className: "as-error", text: "未所属" }),
          s.concurrent.map((d) => el("span", { className: "as-tag sub", text: "兼 " + deptName(d) }))),
        el("td", { "data-label": D.requestTypes[state.type] + "の承認者" }, routeCell),
        el("td", { "data-label": "決まり方" }, how),
        el("td", { className: "as-actions" },
          D.canDepartment ? el("button", { type: "button", className: "btn btn-secondary", onclick: () => openStaffDialog(s) }, "変更") : null)));
    });
    if (rows.length === 0) {
      tbody.appendChild(el("tr", {}, el("td", { colspan: "5", className: "as-muted", text: "条件に合う社員はいません。" })));
    }
    const count = document.getElementById("as-staff-count");
    if (count) count.textContent = rows.length + " / " + D.staff.length + "人";
  }

  // ------------------------------------------------------------ タブ2：組織図・段数

  function renderOrgPanel() {
    const panel = el("section", { className: "as-panel", id: "panel-org", role: "tabpanel", "aria-labelledby": "tab-org" });
    panel.hidden = state.tab !== "org";

    // 段数
    const levelsForm = el("form", { method: "post", action: ACTION, className: "as-levels-form" },
      hidden("mode", "levels"), hidden("tab", "org"),
      TYPES.map((t) => el("div", {},
        el("label", { for: "lv-" + t, text: D.requestTypes[t] }), el("br"),
        el("input", { type: "number", id: "lv-" + t, name: "maxLevels_" + t, className: "form-input", min: "1", max: "10",
          value: String(D.maxLevels[t] || 1), required: true, disabled: !D.canRoute }), " 段")),
      D.canRoute ? el("button", { type: "submit", className: "btn btn-primary", text: "段数を保存" }) : null);
    panel.appendChild(el("div", { className: "as-box" },
      el("h3", { style: "margin-top:0;", text: "組織図でたどる段数" }),
      el("p", { className: "as-lead", style: "margin-top:0;", text: "承認ルートを割り当てていない社員は、所属部署の承認者を1段目として、上位部署の承認者へこの段数まで回ります（承認者のいない部署・本人は飛ばします）。" }),
      levelsForm));

    // 部署
    panel.appendChild(el("div", { className: "as-toolbar" },
      el("h3", { text: "部署と承認者" }),
      D.canDepartment ? el("button", { type: "button", className: "btn btn-primary", onclick: () => openDeptDialog(null) }, "＋ 部署を追加") : null));
    const tbody = el("tbody", {});
    D.departments.forEach((d) => {
      const missing = [d.approverId, ...d.subIds].filter((id) => id !== null && !staffById.has(id));
      tbody.appendChild(el("tr", { className: d.approverId === null && d.subIds.length === 0 ? "ng" : "" },
        el("td", { "data-label": "部署", className: "as-who" },
          el("span", { className: "as-indent", text: "　".repeat(d.depth || 0) + (d.depth ? "└ " : "") }),
          el("strong", { text: d.name })),
        el("td", { "data-label": "承認者" },
          d.approverId !== null ? staffName(d.approverId) : el("span", { className: "as-error", text: "未設定" })),
        el("td", { "data-label": "副承認者" },
          d.subIds.length ? d.subIds.map((id) => el("span", { className: "as-tag sub", text: staffName(id) })) : el("span", { className: "as-muted", text: "なし" }),
          missing.length ? el("div", { className: "as-error", text: "⚠ 社員マスタにいない人がいます" }) : null),
        el("td", { "data-label": "所属人数", text: d.memberCount + "人" }),
        el("td", { className: "as-actions" },
          D.canDepartment ? el("button", { type: "button", className: "btn btn-secondary", onclick: () => openDeptDialog(d) }, "編集") : null)));
    });
    panel.appendChild(el("div", { style: "overflow-x:auto;" }, el("table", { className: "as-table" },
      el("thead", {}, el("tr", {}, el("th", { text: "部署" }), el("th", { text: "承認者" }),
        el("th", { text: "副承認者（どちらか1人の承認で進む）" }), el("th", { text: "所属人数" }), el("th", {}))),
      tbody)));
    return panel;
  }

  // ------------------------------------------------------------ タブ：部署ごとの割り当て

  function renderDeptsPanel() {
    const panel = el("section", { className: "as-panel", id: "panel-depts", role: "tabpanel", "aria-labelledby": "tab-depts" });
    panel.hidden = state.tab !== "depts";
    panel.appendChild(el("p", { className: "as-lead", text:
      "部署に承認ルートを割り当てると、その部署と下の部署の社員の申請はそのルートで回ります。下の部署に別のルートを割り当てれば、そちらが優先されます。どこにも割り当てがない部署は組織図で決まります。" }));
    const cell = (d, t) => {
      const hit = deptRoute(d.id, t);
      if (!hit) return el("span", { className: "as-tag org", text: "組織図で決まる" });
      const direct = hit.from === d.id;
      return el("div", {},
        direct ? el("strong", { text: hit.route.name }) : el("span", { className: "as-muted", text: "↑ " + hit.route.name + "（" + deptName(hit.from) + "から）" }),
        el("div", { className: "as-muted", style: "font-size:0.8rem;", text: routeSteps(hit.route) }));
    };
    const tbody = el("tbody", {});
    D.departments.forEach((d) => {
      tbody.appendChild(el("tr", {},
        el("td", { "data-label": "部署", className: "as-who" },
          el("span", { className: "as-indent", text: "　".repeat(d.depth || 0) + (d.depth ? "└ " : "") }),
          el("strong", { text: d.name })),
        TYPES.map((t) => el("td", { "data-label": D.requestTypes[t] }, cell(d, t))),
        el("td", { "data-label": "所属人数", text: d.memberCount + "人" }),
        el("td", { className: "as-actions" },
          D.canRoute ? el("button", { type: "button", className: "btn btn-secondary", onclick: () => openDeptRouteDialog(d) }, "ルートを選ぶ") : null)));
    });
    panel.appendChild(el("div", { style: "overflow-x:auto;" }, el("table", { className: "as-table" },
      el("thead", {}, el("tr", {}, el("th", { text: "部署" }), TYPES.map((t) => el("th", { text: D.requestTypes[t] })),
        el("th", { text: "所属人数" }), el("th", {}))),
      tbody)));
    return panel;
  }

  // ------------------------------------------------------------ タブ：承認ルート

  function renderRoutesPanel() {
    const panel = el("section", { className: "as-panel", id: "panel-routes", role: "tabpanel", "aria-labelledby": "tab-routes" });
    panel.hidden = state.tab !== "routes";
    panel.appendChild(el("p", { className: "as-lead", text:
      "段ごとの承認者を決めたルートを作り、部署か社員に割り当てます。ふだんは部署に割り当て、派遣先常駐・代理などイレギュラーな人だけ社員に割り当てます（社員への割り当てが優先）。部署への割り当ては下の部署にも効きます。" }));
    panel.appendChild(el("div", { className: "as-toolbar" },
      el("h3", { text: "承認ルート" }),
      D.canRoute ? el("button", { type: "button", className: "btn btn-primary", onclick: () => openRouteDialog(null) }, "＋ 承認ルートを作る") : null));

    if (D.routes.length === 0) {
      panel.appendChild(el("p", { className: "as-muted", text: "まだありません。全員が組織図で決まります。" }));
      return panel;
    }
    const tbody = el("tbody", {});
    D.routes.forEach((r) => {
      const missing = missingApprovers(r.levels);
      const targets = [
        ...r.deptIds.map((id) => el("span", { className: "as-tag sub", text: "部署 " + deptName(id) })),
        ...r.staffIds.map((id) => el("span", { className: "as-tag sub", text: staffLabel(id) }))
      ];
      tbody.appendChild(el("tr", {},
        el("td", { "data-label": "ルート名" }, el("strong", { text: r.name }), r.note ? el("div", { className: "as-muted", text: r.note }) : null),
        el("td", { "data-label": "申請種別", text: r.type ? D.requestTypes[r.type] : "残業・休暇の両方" }),
        el("td", { "data-label": "承認者" },
          el("ol", { className: "as-route" }, r.levels.map((lv, i) => el("li", {}, (i + 1) + ". " + levelText(lv)))),
          missing.length ? el("div", { className: "as-error", text: "⚠ 社員マスタにいない承認者がいます" }) : null),
        el("td", { "data-label": "割り当て先" }, targets.length ? targets : el("span", { className: "as-error", text: "未割り当て（使われていません）" })),
        el("td", { "data-label": "使っている社員", text: routeUserCount(r) + "人" }),
        el("td", { className: "as-actions" },
          D.canRoute ? [
            el("button", { type: "button", className: "btn btn-secondary", onclick: () => openRouteDialog(r) }, "編集"), " ",
            el("form", { method: "post", action: ACTION, style: "display:inline;",
              onsubmit: (e) => { if (!confirm("承認ルート「" + r.name + "」を削除しますか？\n割り当てていた社員・部署は、上位部署の割り当てか組織図で決まるようになります。")) e.preventDefault(); } },
              hidden("mode", "routeDelete"), hidden("tab", "routes"), hidden("id", r.id),
              el("button", { type: "submit", className: "btn btn-danger" }, "削除"))
          ] : null)));
    });
    panel.appendChild(el("div", { style: "overflow-x:auto;" }, el("table", { className: "as-table" },
      el("thead", {}, el("tr", {}, el("th", { text: "ルート名" }), el("th", { text: "申請種別" }), el("th", { text: "承認者（段ごと）" }),
        el("th", { text: "割り当て先" }), el("th", { text: "使っている社員" }), el("th", {}))),
      tbody)));
    return panel;
  }

  // ------------------------------------------------------------ ダイアログ

  /** 検索つきのチェックリスト（name の値を複数送る） */
  function pickList(name, options, selected, placeholder) {
    const chosen = new Set(selected || []);
    const foot = el("div", { className: "foot" });
    const list = el("div", { className: "list" });
    const update = () => {
      foot.textContent = list.querySelectorAll("input:checked").length + "件選択中";
    };
    // 選んである人・部署を先頭に出す（長いリストでも、今の設定がすぐ分かるように）
    const ordered = options.filter((o) => chosen.has(o.value)).concat(options.filter((o) => !chosen.has(o.value)));
    ordered.forEach((o) => {
      list.appendChild(el("label", { "data-search": (o.search || o.label).toLowerCase() },
        el("input", { type: "checkbox", name: name, value: String(o.value), checked: chosen.has(o.value), onchange: update }),
        o.label));
    });
    const search = el("input", { type: "search", placeholder: placeholder || "絞り込み", "aria-label": "絞り込み",
      oninput: (e) => {
        const q = e.target.value.trim().toLowerCase();
        list.querySelectorAll("label").forEach((l) => { l.hidden = q !== "" && !l.dataset.search.includes(q); });
      } });
    update();
    return el("div", { className: "as-pick" }, search, list, foot);
  }

  const staffOptions = () => D.staff.map((s) => ({ value: s.id, label: s.id + " " + s.name, search: s.id + " " + s.name }));
  const deptOptions = () => D.departments.map((d) => ({ value: d.id, label: deptOptionLabel(d), search: d.name }));

  function selectEl(name, options, selectedValue, id) {
    return el("select", { name: name, id: id, className: "form-input" },
      options.map((o) => el("option", { value: o.value === null ? "" : String(o.value), selected: o.value === selectedValue }, o.label)));
  }

  function field(label, control, hint, forId) {
    return el("div", { className: "as-field" },
      forId ? el("label", { for: forId, text: label }) : el("div", { className: "lbl", text: label }),
      control,
      hint ? el("div", { className: "hint", text: hint }) : null);
  }

  /** ダイアログを開く。body は中身の要素、formAttrs はフォームの hidden 値 */
  function openDialog(title, hiddenValues, body, submitLabel) {
    const dialog = el("dialog", { className: "as-dialog", "aria-label": title });
    const form = el("form", { method: "post", action: ACTION },
      Object.entries(hiddenValues).map(([k, v]) => hidden(k, v)),
      el("header", {}, el("h3", { text: title })),
      el("div", { className: "body" }, body),
      el("footer", {},
        el("button", { type: "button", className: "btn btn-secondary", onclick: () => dialog.close() }, "キャンセル"),
        el("button", { type: "submit", className: "btn btn-primary" }, submitLabel || "保存")));
    dialog.appendChild(form);
    dialog.addEventListener("close", () => dialog.remove());
    dialog.addEventListener("click", (e) => { if (e.target === dialog) dialog.close(); });
    document.body.appendChild(dialog);
    if (typeof dialog.showModal === "function") dialog.showModal(); else dialog.setAttribute("open", "");
    return form;
  }

  /**
   * 残業・休暇それぞれのルートを選ぶ欄。currentFor(t) は今の割り当て。
   * 種別「両方」のルートは割り当て1件で両方の申請に効くので、もう一方が「なし」なら同じルートを入れて見せる。
   */
  function routeSelectFields(label, currentFor, noneLabel) {
    const selects = {};
    const fields = TYPES.map((t) => {
      const usable = D.routes.filter((r) => appliesTo(r, t));
      const cur = currentFor(t);
      const sel = selectEl(t === "OVERTIME" ? "overtimeRouteId" : "leaveRouteId",
        [{ value: null, label: noneLabel }, ...usable.map((r) => ({ value: r.id, label: r.name + (r.type === null ? "（残業・休暇の両方）" : "") }))],
        cur ? cur.id : null, "dlg-route-" + t);
      selects[t] = sel;
      return field(D.requestTypes[t] + "の" + label, sel, null, "dlg-route-" + t);
    });
    TYPES.forEach((t) => selects[t].addEventListener("change", () => {
      const r = routeById.get(Number(selects[t].value));
      if (r && r.type === null) TYPES.filter((o) => o !== t).forEach((o) => { if (selects[o].value === "") selects[o].value = String(r.id); });
    }));
    fields.push(el("div", { className: "hint", style: "font-size:0.75rem;color:#718096;margin:-6px 0 12px;",
      text: "「両方」のルートは、もう一方の申請にも使われます（もう一方で別のルートを選べば、そちらが優先されます）。" }));
    return fields;
  }

  /** 社員：主所属・兼任・社員個別の承認ルート */
  function openStaffDialog(s) {
    const deptChoices = [{ value: null, label: "（未所属）" }, ...D.departments.map((d) => ({ value: d.id, label: deptOptionLabel(d) }))];
    const body = [
      field("所属部署（主）", selectEl("departmentId", deptChoices, s.deptId, "dlg-dept"), null, "dlg-dept"),
      field("兼任部署", pickList("concurrentDepartmentIds", deptOptions(), s.concurrent, "部署名で絞り込み"),
        "兼任している部署を選ぶと、申請のときに「どの部署の業務か」を選べるようになります。")
    ];
    if (D.canRoute) {
      body.push(el("div", { className: "as-field" }, el("div", { className: "lbl", text: "社員個別の承認ルート（部署の割り当てより優先。派遣先常駐・代理などイレギュラーな人だけ）" })));
      body.push(...routeSelectFields("ルート", (t) => directRoute("staff", s.id, t), "なし（部署の割り当て・組織図で決まる）"));
    }
    openDialog(s.name + "（ID " + s.id + "）", { mode: "staff", tab: "staff", staffId: s.id }, body, "保存");
  }

  /** 部署：割り当てる承認ルート */
  function openDeptRouteDialog(d) {
    const parentHit = (t) => {
      const p = d.parentId !== null && d.parentId !== undefined ? deptRoute(d.parentId, t) : null;
      return p ? "上位部署の「" + p.route.name + "」を使う" : "組織図で決める";
    };
    const body = [
      el("p", { className: "as-lead", style: "margin-top:0;", text: "この部署と下の部署の社員の申請に使うルートを選びます。下の部署・社員に別のルートを割り当てていれば、そちらが優先されます。" }),
      ...routeSelectFields("ルート", (t) => directRoute("dept", d.id, t), "なし（" + parentHit("OVERTIME") + "）")
    ];
    // 「なし」の説明は申請種別ごとに違うので、休暇側を直す
    const leaveNone = body.find((n) => n.querySelector && n.querySelector("#dlg-route-LEAVE"));
    if (leaveNone) leaveNone.querySelector("option").textContent = "なし（" + parentHit("LEAVE") + "）";
    openDialog("部署の承認ルート：" + d.name, { mode: "deptRoutes", tab: "depts", departmentId: d.id }, body, "保存");
  }

  /** 部署：名前・上位部署・承認者・副承認者・並び順 */
  function openDeptDialog(d) {
    const parents = [{ value: null, label: "（なし：最上位）" },
      ...D.departments.filter((p) => !d || p.id !== d.id).map((p) => ({ value: p.id, label: deptOptionLabel(p) }))];
    const approvers = [{ value: null, label: "（未設定）" }, ...D.staff.map((s) => ({ value: s.id, label: s.id + " " + s.name }))];
    const body = [
      field("部署名", el("input", { type: "text", name: "name", id: "dlg-dname", className: "form-input", required: true, maxlength: "100", value: d ? d.name : "" }), null, "dlg-dname"),
      field("上位部署", selectEl("parentId", parents, d ? d.parentId : null, "dlg-parent"), null, "dlg-parent"),
      field("承認者", selectEl("approverStaffId", approvers, d ? d.approverId : null, "dlg-approver"), null, "dlg-approver"),
      field("副承認者", pickList("subApproverIds", staffOptions(), d ? d.subIds : [], "社員ID・氏名で絞り込み"),
        "承認者と同じ段の候補です。誰か1人が承認すれば次の段へ進みます（不在時の代理など）。"),
      field("並び順", el("input", { type: "number", name: "sortOrder", id: "dlg-sort", className: "form-input", value: String(d ? d.sortOrder : 0) }), null, "dlg-sort")
    ];
    openDialog(d ? "部署を編集：" + d.name : "部署を追加", { mode: "dept", tab: "org", id: d ? d.id : "" }, body, d ? "保存" : "追加");
  }

  /**
   * 承認者を選ぶグループ（左の列）。全員・職種ごと・部署ごと。
   * 部署は主所属と兼任の両方を含める。
   */
  function staffGroups() {
    const groups = [{ key: "all", label: "全員", kind: "all", ids: D.staff.map((s) => s.id) }];
    const classes = new Map();
    D.staff.forEach((s) => {
      if (s.classId === null || s.classId === undefined) return;
      if (!classes.has(s.classId)) classes.set(s.classId, { key: "c" + s.classId, label: s.className || "職種" + s.classId, kind: "class", ids: [] });
      classes.get(s.classId).ids.push(s.id);
    });
    groups.push(...[...classes.entries()].sort((a, b) => a[0] - b[0]).map((e) => e[1]));
    D.departments.forEach((d) => {
      const ids = D.staff.filter((s) => s.deptId === d.id || (s.concurrent || []).includes(d.id)).map((s) => s.id);
      groups.push({ key: "d" + d.id, label: d.name, indent: d.depth || 0, kind: "dept", ids: ids });
    });
    return groups;
  }

  /**
   * 段ごとの承認者を「選択済 ← 未選択」の2つの欄で選ぶ。
   * 段の並べ替え（▲▼）・削除（✖）・「経路追加」ができる。
   * 送る値：levelCount と、段ごとの level1, level2, …（社員ID を複数）
   */
  function routeLevelsEditor(initial) {
    const groups = staffGroups();
    const groupByKey = new Map(groups.map((g) => [g.key, g]));
    const levels = (initial && initial.length ? initial : [[]]).map((lv) => lv.slice());
    const views = levels.map(() => ({ group: "all", q: "" }));
    const box = el("div", { className: "as-rl" });
    const inputs = el("div", {});

    function syncInputs() {
      inputs.innerHTML = "";
      inputs.appendChild(hidden("levelCount", levels.length));
      levels.forEach((lv, i) => lv.forEach((id) => inputs.appendChild(hidden("level" + (i + 1), id))));
    }

    function person(id, onclick, title) {
      return el("li", {}, el("button", { type: "button", className: "as-dl-item", title: title, onclick: onclick },
        el("span", { className: "as-dl-icon", "aria-hidden": "true", text: "👤" }), staffName(id)));
    }

    function drawLevel(i) {
      const lv = levels[i];
      const view = views[i];
      const redraw = () => { box.children[i].replaceWith(drawLevel(i)); syncInputs(); };

      // 選択済
      // 先頭の人がその段の主承認者（主承認者本人の申請は、この段のほかの人に回さず次の段へ進む）
      const selList = el("ul", { className: "as-dl-list" },
        lv.length ? lv.map((id, k) => {
          const li = person(id, () => { lv.splice(lv.indexOf(id), 1); redraw(); }, "クリックで外す");
          if (lv.length > 1) {
            li.classList.add("as-dl-sel");
            li.appendChild(k === 0 ? el("span", { className: "as-dl-main", title: "この人の申請は、この段のほかの人に回さず次の段へ進みます", text: "主" })
              : el("button", { type: "button", className: "as-dl-link as-dl-tomain", "aria-label": staffName(id) + "を主承認者にする",
                onclick: () => { lv.splice(k, 1); lv.unshift(id); redraw(); } }, "主にする"));
          }
          return li;
        }) : el("li", { className: "as-dl-empty", text: "右の欄から承認者を選んでください" }));
      const selBox = el("div", { className: "as-dl-box" },
        el("div", { className: "as-dl-head" }, el("span", { text: "選択済（" + lv.length + "人）" }),
          el("button", { type: "button", className: "as-dl-link", onclick: () => { lv.length = 0; redraw(); } }, "全て削除")),
        selList);

      // 未選択：グループ ＋ 社員
      const group = groupByKey.get(view.group) || groups[0];
      const q = view.q.trim().toLowerCase();
      const candidates = group.ids.filter((id) => !lv.includes(id))
        .filter((id) => q === "" || (id + " " + staffName(id)).toLowerCase().includes(q));
      const groupList = el("ul", { className: "as-dl-groups" }, groups.map((g) =>
        el("li", {}, el("button", { type: "button", className: "as-dl-group" + (g.kind === "class" ? " cls" : ""),
          "aria-pressed": g.key === group.key ? "true" : "false",
          onclick: () => { view.group = g.key; redraw(); } },
          el("span", { className: "as-dl-icon", "aria-hidden": "true", text: g.kind === "dept" ? "🏢" : "👥" }),
          (g.indent ? "　".repeat(g.indent) : "") + g.label))));
      const unselList = el("ul", { className: "as-dl-list" },
        candidates.length ? candidates.map((id) => person(id, () => { lv.push(id); redraw(); }, "クリックで選択済に入れる"))
          : el("li", { className: "as-dl-empty", text: "該当する社員はいません" }));
      const search = el("input", { type: "search", className: "as-dl-search", placeholder: "社員ID・氏名で検索", "aria-label": (i + 1) + "段目の承認者を検索", value: view.q,
        oninput: (e) => { view.q = e.target.value; const pos = e.target.selectionStart; redraw();
          const s = box.children[i].querySelector(".as-dl-search"); s.focus(); s.setSelectionRange(pos, pos); } });
      const unselBox = el("div", { className: "as-dl-box" },
        el("div", { className: "as-dl-head" }, el("span", { text: "未選択：" + group.label }),
          el("button", { type: "button", className: "as-dl-link", onclick: () => { candidates.forEach((id) => lv.push(id)); redraw(); } }, "全て選択")),
        el("div", { className: "as-dl-split" }, groupList, unselList));

      const move = (d) => {
        const j = i + d;
        if (j < 0 || j >= levels.length) return;
        [levels[i], levels[j]] = [levels[j], levels[i]];
        [views[i], views[j]] = [views[j], views[i]];
        drawAll();
      };
      return el("div", { className: "as-rl-row" },
        el("div", { className: "as-rl-no", text: (i + 1) + "段目" }),
        el("div", { className: "as-rl-body" },
          search,
          el("div", { className: "as-dl" }, selBox, el("div", { className: "as-dl-arrow", "aria-hidden": "true", text: "⇦" }), unselBox)),
        el("div", { className: "as-rl-ctrl" },
          el("button", { type: "button", className: "as-rl-del", title: "この段を削除", "aria-label": (i + 1) + "段目を削除", disabled: levels.length <= 1,
            onclick: () => { levels.splice(i, 1); views.splice(i, 1); drawAll(); } }, "✖"),
          el("button", { type: "button", title: "上へ", "aria-label": (i + 1) + "段目を上へ", disabled: i === 0, onclick: () => move(-1) }, "▲"),
          el("button", { type: "button", title: "下へ", "aria-label": (i + 1) + "段目を下へ", disabled: i === levels.length - 1, onclick: () => move(1) }, "▼")));
    }

    function drawAll() {
      box.innerHTML = "";
      levels.forEach((_, i) => box.appendChild(drawLevel(i)));
      syncInputs();
    }
    drawAll();
    const add = el("button", { type: "button", className: "as-dl-link as-rl-add",
      onclick: () => { levels.push([]); views.push({ group: "all", q: "" }); drawAll(); } }, "＋ 経路追加（段を増やす）");
    return el("div", {}, box, add, inputs);
  }

  /** 承認ルート：名前・種別・段ごとの承認者・割り当て先 */
  function openRouteDialog(r) {
    const levelsEditor = routeLevelsEditor(r ? r.levels : null);

    const typeChoices = [{ value: null, label: "残業・休暇の両方" }, ...TYPES.map((t) => ({ value: t, label: D.requestTypes[t] }))];
    const body = [
      field("ルート名", el("input", { type: "text", name: "name", id: "dlg-rname", className: "form-input", required: true, maxlength: "100",
        placeholder: "例：派遣先A 責任者ルート", value: r ? r.name : "" }), null, "dlg-rname"),
      field("申請種別", selectEl("requestType", typeChoices, r ? r.type : null, "dlg-rtype"), null, "dlg-rtype"),
      field("メモ（任意）", el("input", { type: "text", name: "note", id: "dlg-rnote", className: "form-input", maxlength: "255", value: r && r.note ? r.note : "" }), null, "dlg-rnote"),
      el("div", { className: "as-field" },
        el("div", { className: "lbl", text: "承認者（段ごと）" }),
        el("div", { className: "hint", style: "margin-bottom:6px;", text: "1段目から順に承認します。同じ段に複数選ぶと、誰か1人の承認でその段は終わります。先頭の人（主）本人の申請は、その段のほかの人に回さず次の段へ進みます。右の欄で職種・部署を選ぶと、その社員だけが出ます。" }),
        levelsEditor),
      field("割り当て先：部署", pickList("assignDepartmentIds", deptOptions(), r ? r.deptIds : [], "部署名で絞り込み"),
        "その部署と下の部署の社員に使われます（別のルートを割り当てた部署・社員を除く）。"),
      field("割り当て先：社員", pickList("assignStaffIds", staffOptions(), r ? r.staffIds : [], "社員ID・氏名で絞り込み"),
        "社員への割り当ては、部署への割り当てより優先されます。")
    ];
    const form = openDialog(r ? "承認ルートを編集：" + r.name : "承認ルートを作る", { mode: "route", tab: "routes", id: r ? r.id : "" }, body, r ? "保存" : "作る");
    form.parentElement.classList.add("wide");
  }

  render();
})();
