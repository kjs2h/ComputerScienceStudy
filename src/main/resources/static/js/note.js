(function () {
  "use strict";

  var LS_THEME = "cs_notes_theme_v1";
  var LS_PROGRESS = "cs_notes_progress_v1";
  var root = document.documentElement;
  var $ = function (sel, el) { return (el || document).querySelector(sel); };
  var $$ = function (sel, el) { return Array.prototype.slice.call((el || document).querySelectorAll(sel)); };

  function store(key, value) {
    try {
      if (value === undefined) return localStorage.getItem(key);
      localStorage.setItem(key, value);
    } catch (e) { return null; }
  }

  function initTheme() {
    var btn = $("#theme-toggle");
    var dark = function () {
      var t = root.getAttribute("data-theme");
      return t ? t === "dark" : window.matchMedia("(prefers-color-scheme: dark)").matches;
    };
    var paint = function () { btn.textContent = dark() ? "🌙" : "☀️"; };
    paint();
    btn.addEventListener("click", function () {
      var next = dark() ? "light" : "dark";
      root.setAttribute("data-theme", next);
      store(LS_THEME, next);
      paint();
    });
  }

  function initProgress() {
    var progress = {};
    try { progress = JSON.parse(store(LS_PROGRESS) || "{}"); } catch (e) {}
    var checks = $$("#sidebar .progress-check");

    function paint() {
      checks.forEach(function (cb) {
        var id = cb.getAttribute("data-id");
        var done = !!progress[id];
        cb.checked = done;
        cb.parentNode.classList.toggle("done", done);
        var heading = document.getElementById(id);
        if (heading) heading.classList.toggle("section-done", done);
      });
      $$("details.nav-part").forEach(function (part) {
        var own = $$(".progress-check", part);
        var done = own.filter(function (cb) { return cb.checked; }).length;
        $(".part-progress", part).textContent = own.length ? done + "/" + own.length : "";
      });
      var total = checks.length;
      var doneAll = checks.filter(function (cb) { return cb.checked; }).length;
      var pct = total ? Math.round((doneAll / total) * 100) : 0;
      $("#progress-pill").innerHTML = '<span class="pp-label">학습 진행률</span>' + doneAll + "/" + total + " (" + pct + "%)";
    }

    $("#sidebar").addEventListener("change", function (e) {
      if (!e.target.classList.contains("progress-check")) return;
      progress[e.target.getAttribute("data-id")] = e.target.checked;
      store(LS_PROGRESS, JSON.stringify(progress));
      paint();
    });
    paint();

    $("#random-review").addEventListener("click", function () {
      var ids = checks.map(function (cb) { return cb.getAttribute("data-id"); });
      var undone = ids.filter(function (id) { return !progress[id]; });
      var pool = undone.length ? undone : ids;
      if (pool.length) jump(pool[Math.floor(Math.random() * pool.length)], true);
    });
  }

  function escapeHtml(s) {
    return String(s).replace(/[&<>"]/g, function (c) {
      return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c];
    });
  }

  function initSearch() {
    var input = $("#sidebar-search-input");
    var box = $("#sidebar-search-results");
    var tree = $("#sidebar-nav-tree");
    var timer = null;
    var seq = 0;

    function render(hits) {
      box.innerHTML = hits.length ? hits.map(function (h) {
        return '<a class="search-result-item" href="' + escapeHtml(h.url) + '">' +
          '<span class="sr-part">' + escapeHtml(h.part) + "</span>" +
          '<span class="sr-text">' + escapeHtml(h.title) + "</span>" +
          (h.snippet ? '<span class="sr-snippet">' + escapeHtml(h.snippet) + "</span>" : "") + "</a>";
      }).join("") : '<div class="search-empty">일치하는 항목이 없습니다.</div>';
    }

    input.addEventListener("input", function () {
      var q = input.value.trim();
      clearTimeout(timer);
      if (!q) { box.hidden = true; tree.hidden = false; return; }
      timer = setTimeout(function () {
        var mine = ++seq;
        fetch("/api/search?q=" + encodeURIComponent(q))
          .then(function (r) { return r.json(); })
          .then(function (hits) {
            if (mine !== seq) return;
            render(hits);
            box.hidden = false;
            tree.hidden = true;
          });
      }, 150);
    });
  }

  function initScrollSpy() {
    var items = {};
    $$("li.nav-item").forEach(function (li) { items[li.getAttribute("data-navid")] = li; });
    var headings = $$("main h2[id]").filter(function (h) { return items[h.id]; });
    if (!headings.length || !("IntersectionObserver" in window)) return;
    var current = null;
    var observer = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (!entry.isIntersecting) return;
        if (current) items[current].classList.remove("active");
        current = entry.target.id;
        items[current].classList.add("active");
        items[current].scrollIntoView({ block: "nearest" });
      });
    }, { rootMargin: "-15% 0px -75% 0px", threshold: 0 });
    headings.forEach(function (h) { observer.observe(h); });
  }

  function initChrome() {
    var bar = $("#reading-progress");
    var top = $("#back-to-top");
    var sidebar = $("#sidebar");
    var toggle = $("#sidebar-toggle-btn");

    document.addEventListener("scroll", function () {
      var max = root.scrollHeight - root.clientHeight;
      bar.style.width = (max > 0 ? (root.scrollTop / max) * 100 : 0) + "%";
      top.classList.toggle("show", window.scrollY > 600);
    }, { passive: true });
    top.addEventListener("click", function () { window.scrollTo({ top: 0, behavior: "smooth" }); });
    $("#print-btn").addEventListener("click", function () { window.print(); });
    window.addEventListener("beforeprint", function () {
      $$("main details").forEach(function (d) { d.open = true; });
    });

    function setNav(open) {
      sidebar.classList.toggle("open", open);
      document.body.classList.toggle("nav-open", open);
    }
    toggle.addEventListener("click", function () { setNav(!sidebar.classList.contains("open")); });
    $("#sidebar-backdrop").addEventListener("click", function () { setNav(false); });
    sidebar.addEventListener("click", function (e) { if (e.target.closest("a")) setNav(false); });
    document.addEventListener("keydown", function (e) { if (e.key === "Escape") setNav(false); });
    window.addEventListener("resize", function () { if (window.innerWidth > 980) setNav(false); });

    var active = $("#page-tabs a.active");
    if (active) active.parentNode.scrollLeft = active.offsetLeft - active.parentNode.clientWidth / 2 + active.clientWidth / 2;
  }

  var stack = [];

  function flash(el) {
    el.classList.remove("section-flash");
    void el.offsetWidth;
    el.classList.add("section-flash");
  }

  function reveal(el, withFlash) {
    var d = el.closest("main details");
    if (d) d.open = true;
    el.scrollIntoView({ block: "start" });
    if (withFlash) flash(el);
  }

  function jump(id, withFlash) {
    var el = document.getElementById(id);
    if (!el) { location.href = "/go/" + encodeURIComponent(id); return; }
    history.pushState(null, "", "#" + id);
    reveal(el, withFlash);
  }

  function initLinks() {
    var back = $("#back-btn");
    function paintBack() {
      back.hidden = stack.length === 0;
      back.innerHTML = "← 돌아가기" + (stack.length > 1 ? '<span class="cnt">' + stack.length + "</span>" : "");
    }

    document.addEventListener("click", function (e) {
      var a = e.target.closest ? e.target.closest('a[href^="#"]') : null;
      if (!a) return;
      var id = decodeURIComponent(a.getAttribute("href").slice(1));
      if (!id) return;
      e.preventDefault();
      if (document.getElementById(id)) {
        stack.push(window.pageYOffset);
        if (stack.length > 50) stack.shift();
        paintBack();
      }
      jump(id, true);
    });

    back.addEventListener("click", function () {
      var y = stack.pop();
      paintBack();
      if (y !== undefined) window.scrollTo({ top: y, behavior: "instant" });
    });

    var hash = decodeURIComponent(location.hash.slice(1));
    var target = hash && document.getElementById(hash);
    if (target) reveal(target, target.tagName !== "H1");
  }

  function initToc() {
    var box = $("#page-toc");
    var heads = $$("main h1[id], main h2[id], main h3[id]").filter(function (h) { return h.textContent.trim(); });
    var multiPart = $$("main h1[id]").length > 1;
    var root = document.createElement("ul");
    var entries = [];
    var parent = null;

    heads.forEach(function (h) {
      if (h.tagName === "H1" && !multiPart) return;
      var li = document.createElement("li");
      var a = document.createElement("a");
      a.href = "#" + h.id;
      a.textContent = h.textContent.trim();
      a.title = a.textContent;
      li.appendChild(a);
      if (h.tagName === "H3" && parent) {
        var sub = parent.querySelector("ul") || parent.appendChild(document.createElement("ul"));
        sub.appendChild(li);
        entries.push({ h: h, li: li, top: parent });
      } else {
        if (h.tagName === "H1") li.className = "toc-part";
        else parent = li;
        root.appendChild(li);
        entries.push({ h: h, li: li, top: li });
      }
    });
    if (entries.length < 2) return;

    var title = document.createElement("p");
    title.className = "toc-title";
    title.textContent = "이 페이지";
    box.appendChild(title);
    box.appendChild(root);
    document.body.classList.add("has-toc");

    var ticking = false;
    function spy() {
      ticking = false;
      var now = entries[0];
      for (var i = 0; i < entries.length; i++) {
        if (entries[i].h.getBoundingClientRect().top <= 140) now = entries[i];
        else break;
      }
      entries.forEach(function (e) {
        e.li.classList.toggle("active", e === now);
        e.li.classList.toggle("current", e.li === now.top && !e.li.classList.contains("toc-part"));
      });
      if (box.offsetParent) box.scrollTop = Math.max(0, now.top.offsetTop - box.clientHeight / 3);
    }
    document.addEventListener("scroll", function () {
      if (!ticking) { ticking = true; requestAnimationFrame(spy); }
    }, { passive: true });
    spy();
  }

  document.addEventListener("DOMContentLoaded", function () {
    initTheme();
    initProgress();
    initSearch();
    initScrollSpy();
    initChrome();
    initToc();
    initLinks();
  });
})();
