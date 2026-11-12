import { drawAvatar } from "./avatar.js";
import {
  REWARD, DAILY_LIMIT, HOLD_TIME, TITLES, ITEMS, today, initialState,
  normalizeState, award, redeem, toggleEquipment, currentTitle,
} from "./rewards.js";

const STORAGE_KEY = "gyodong5-2-web-prototype-v1";
const content = document.querySelector("#phone-content");
const navigation = document.querySelector("#phone-nav");
const status = document.querySelector("#demo-status");
const pages = [["home", "홈"], ["shop", "상점"], ["titles", "칭호"], ["avatar", "하슬랑"], ["guide", "안내"]];
let stored;
try { stored = JSON.parse(localStorage.getItem(STORAGE_KEY)); } catch { stored = null; }
let state = normalizeState(stored);
let page = "home";
let trial = null;
let timer = null;
let overlay = null;
let noticeTimer = null;
let previewLevel = null;
let storageAvailable = true;

function save(next) {
  state = next;
  try { localStorage.setItem(STORAGE_KEY, JSON.stringify(state)); }
  catch {
    storageAvailable = false;
    status.textContent = "이 브라우저에서는 기록이 저장되지 않아, 페이지를 닫으면 체험 기록이 사라져요.";
  }
}
function money(value) { return new Intl.NumberFormat("ko-KR").format(value); }
function canvasMarkup(className = "") {
  return `<canvas class="pixel-avatar ${className}" width="576" height="408" role="img" aria-label="픽셀 하슬랑"></canvas>`;
}
function titleHeading(heading, description) {
  return `<div class="app-heading"><h3>${heading}</h3><p>${description}</p></div>`;
}
function home() {
  const title = currentTitle(state.total);
  return `<section class="points-hero">
    <div class="wallet"><p>사용 가능한 포인트</p><strong data-balance>${money(state.total - state.spent)}<small>점</small></strong></div>
    <button class="avatar-shortcut" data-page="avatar" aria-label="하슬랑 키우기 열기">${canvasMarkup()}<span>하슬랑 키우기 ↗</span></button>
    <div class="wallet-metrics"><span>누적 적립<b>${money(state.total)}점</b></span><span>오늘 적립<b>${state.count} / ${DAILY_LIMIT}회</b></span></div>
  </section>
  <button class="app-card shortcut" data-page="titles"><span><b>교통안전 칭호</b><small>${title.label}</small></span><span class="text-action">칭호 보기 ↗</span></button>
  <button class="app-card shortcut" data-page="shop"><span><b>포인트 상점</b><small>교환권 예시와 하슬랑 꾸미기</small></span><span class="text-action">상점 보기 ↗</span></button>
  <section class="app-card trial-card"><h4>횡단보도 앞에 도착했어요</h4><p>경고창을 닫지 않고 10초 기다려 보세요.<br>화면을 꺼 둔 상황도 체험할 수 있어요.</p>
    <button class="app-button" data-start="warning">비콘 접근 체험</button>
    <button class="app-button secondary" data-start="off">화면 끄기 체험</button>
  </section>`;
}
function shop() {
  return `${titleHeading("포인트 상점", "웹에서 모은 포인트로 꾸미기를 체험해요.")}
  <section class="app-card balance-card"><p>사용 가능한 포인트</p><strong>${money(state.total - state.spent)}점</strong></section>
  <section class="app-card coupon-example"><span class="app-eyebrow">모바일 교환권 · 상품 예시</span><h4>편의점 기프티콘</h4><p>실제 발급은 운영 예산과 발송 서비스 연동 후 제공할 예정이에요.</p></section>
  <h4 class="app-section-title">하슬랑 꾸미기</h4>
  ${ITEMS.map((item) => {
    const owned = state.owned.includes(item.id);
    const affordable = state.total - state.spent >= item.price;
    return `<div class="app-card item-row"><span class="item-symbol" aria-hidden="true">${item.symbol}</span><div><b>${item.label}</b><small>${item.price}점 · 가상 아이템</small></div><button class="small-button" data-redeem="${item.id}" ${owned || !affordable ? "disabled" : ""}>${owned ? "보유 중" : affordable ? "교환" : "포인트 부족"}</button></div>`;
  }).join("")}`;
}
function titles() {
  const title = currentTitle(state.total);
  return `${titleHeading("교통안전 칭호", "완료한 알림 기록이 하슬랑의 성장이 돼요.")}
  ${TITLES.slice(1).map((entry) => `<section class="app-card title-card"><div class="title-line"><span class="item-symbol" aria-hidden="true">${["", "✦", "▤", "◆"][entry.level]}</span><h4>${entry.label}</h4><span>${title.level === entry.level ? "현재 칭호" : title.level > entry.level ? "획득" : "잠김"}</span></div><progress value="${Math.min(state.total, entry.threshold)}" max="${entry.threshold}" aria-label="${entry.label} 성장 진행"></progress><small>완료한 알림 ${Math.min(state.total, entry.threshold) / REWARD} / ${entry.threshold / REWARD}회</small></section>`).join("")}`;
}
function avatar() {
  const actual = currentTitle(state.total);
  const displayed = previewLevel === null ? actual : TITLES[previewLevel];
  const next = TITLES.find((entry) => entry.threshold > state.total);
  return `${titleHeading("하슬랑 키우기", "횡단보도 알림을 잘 이용할 때마다 자라요.")}
  <section class="app-card avatar-stage">${canvasMarkup("large")}<h4>${displayed.label}</h4><p>${previewLevel === null ? next ? `다음 칭호까지 알림 ${Math.ceil((next.threshold - state.total) / REWARD)}회` : "가장 높은 칭호에 도달했어요." : "성장 모습 미리보기"}</p></section>
  <fieldset class="growth-preview"><legend>성장 모습 미리보기</legend><div>${TITLES.map((entry) => `<button data-preview="${entry.level}" aria-pressed="${displayed.level === entry.level}">${entry.level + 1}단계</button>`).join("")}</div><button class="text-button" data-preview="actual">내 하슬랑 보기</button></fieldset>
  <h4 class="app-section-title">하슬랑 꾸미기</h4>
  ${ITEMS.map((item) => {
    const owned = state.owned.includes(item.id);
    const worn = state.equipped.includes(item.id);
    return `<div class="app-card item-row"><span class="item-symbol" aria-hidden="true">${item.symbol}</span><div><b>${item.label}</b><small>${worn ? "착용 중" : owned ? "보유 중" : "상점에서 교환할 수 있어요."}</small></div><button class="small-button" ${owned ? `data-equip="${item.id}"` : 'data-page="shop"'}>${owned ? worn ? "벗기" : "착용" : "상점"}</button></div>`;
  }).join("")}`;
}
function guide() {
  return `${titleHeading("어떻게 작동하나요?", "횡단보도 앞에서 휴대폰이 신호를 받아요.")}
  <section class="app-card"><h4>지금은 가상 비콘 체험이에요</h4><p>웹에서는 이 휴대폰 모양 안에서 경고와 적립을 재현해요. 실제 안드로이드 앱은 다른 앱 위에 경고를 띄울 수 있어요.</p></section>
  <ol class="guide-steps"><li><b>횡단보도 앞 인도에 비콘 설치</b><p>차도에 들어가기 전 대기 구역에 블루투스 비콘을 설치해요.</p></li><li><b>앱이 등록된 비콘 신호 감지</b><p>신호 세기를 바탕으로 가까워졌는지 판단해요. 실제 감지 범위는 현장에서 조정해요.</p></li><li><b>화면을 끄거나 경고창을 유지</b><p>가까이에서 화면을 끄고 있거나 경고를 닫지 않은 채 10초가 지나면 10점씩 하루 최대 3회 적립해요.</p></li><li><b>하슬랑과 함께 습관 만들기</b><p>누적 기록에 따라 칭호가 열리고 하슬랑이 자라요. 웹 체험 기록은 이 브라우저에 저장돼요.</p></li></ol>`;
}

function render() {
  state = normalizeState(state);
  content.innerHTML = ({ home, shop, titles, avatar, guide })[page]();
  navigation.innerHTML = pages.map(([id, label]) => `<button data-page="${id}" aria-current="${page === id ? "page" : "false"}">${label}</button>`).join("");
  content.querySelectorAll("canvas").forEach((canvas) => {
    const level = page === "avatar" && previewLevel !== null ? previewLevel : currentTitle(state.total).level;
    drawAvatar(canvas, level, state.equipped);
    canvas.setAttribute("aria-label", `${TITLES[level].label} 하슬랑 픽셀 캐릭터`);
  });
}
function openPage(destination) {
  if (!pages.some(([id]) => id === destination)) return;
  page = destination;
  previewLevel = null;
  render();
  content.scrollTop = 0;
}
function clearTrial() {
  clearInterval(timer);
  timer = null;
  trial = null;
  document.querySelectorAll("[data-demo-panel]").forEach((element) => element.remove());
  overlay = null;
  document.querySelectorAll("#start-demo, #screen-off-demo").forEach((button) => { button.disabled = false; });
}
function announce(message) {
  status.textContent = message;
}
function showOverlay(html, className = "") {
  document.querySelectorAll("[data-demo-panel]").forEach((element) => element.remove());
  overlay = document.createElement("div");
  overlay.className = `phone-overlay ${className}`;
  overlay.dataset.demoPanel = "true";
  overlay.innerHTML = html;
  document.querySelector(".phone-screen").append(overlay);
  return overlay;
}
function showNotice(message) {
  clearTimeout(noticeTimer);
  const notice = document.querySelector("#phone-notice");
  notice.textContent = message;
  notice.hidden = false;
  noticeTimer = setTimeout(() => { notice.hidden = true; }, 2500);
  announce(message);
}
function dismissWarning() {
  if (!trial) return;
  clearTrial();
  render();
  showNotice("앗! 아쉬워요. 주변을 조심하세요.");
}
function depart() {
  if (!trial) return;
  clearTrial();
  render();
  showNotice("비콘 범위를 벗어나 적립이 중단됐어요.");
}
function finishTrial() {
  const mode = trial.mode;
  clearTrial();
  const outcome = award(state);
  save(outcome.state);
  render();
  const heading = outcome.earned ? "+10 포인트 적립" : "오늘 적립 한도에 도달했어요";
  announce(`${heading}. 오늘 ${state.count} / ${DAILY_LIMIT}회 적립`);
  const detail = mode === "off" ? "화면을 꺼 둔 상태로 적립됐어요." : "경고창을 닫지 않고 기다렸어요.";
  showOverlay(`<div class="result-panel" role="dialog" aria-label="${heading}"><span class="reward-mark" aria-hidden="true">✦</span><h3>${heading}</h3><p>${detail}</p><p>오늘 ${state.count} / ${DAILY_LIMIT}회 적립</p><button class="app-button" data-close-result>하슬랑 보러 가기</button></div>`, "result-overlay");
  overlay.querySelector("button").focus({ preventScroll: true });
}
function tick() {
  if (!trial) return;
  const now = performance.now();
  if (trial.phase === "approach" && now >= trial.approachAt) {
    trial = { ...trial, phase: "holding", startedAt: now };
    if (trial.mode === "off") {
      showOverlay(`<div class="screen-off-panel"><span class="off-light"></span><h3>화면을 꺼 둔 상태예요</h3><p>비콘 범위 안에서 조용히 적립해요.</p><button data-screen-on>화면 켜기</button></div>`, "screen-off-overlay");
    } else {
      showOverlay(`<div class="warning-panel" role="dialog" aria-label="횡단보도 경고"><span class="warning-crosswalk" aria-hidden="true">▰ ▰ ▰</span><h3>횡단보도 앞입니다.<br>주변을 봐주세요.</h3><button data-dismiss-warning>경고 닫기</button></div>`, "warning-overlay");
      overlay.querySelector("button").focus({ preventScroll: true });
    }
  }
  if (trial?.phase === "holding") {
    const left = Math.max(0, Math.ceil((HOLD_TIME - (now - trial.startedAt)) / 1000));
    announce(`가상 비콘 범위 안 · ${left}초 뒤 적립 · ${trial.mode === "off" ? "화면 꺼짐" : "경고 유지 중"}`);
    if (now - trial.startedAt >= HOLD_TIME) finishTrial();
  }
}
function startTrial(mode) {
  if (trial) return;
  document.querySelectorAll("[data-demo-panel]").forEach((element) => element.remove());
  document.querySelector("#phone-notice").hidden = true;
  state = normalizeState(state);
  page = "home";
  render();
  trial = { mode, phase: "approach", approachAt: performance.now() + 2000, startedAt: null };
  document.querySelectorAll("#start-demo, #screen-off-demo").forEach((button) => { button.disabled = true; });
  showOverlay(`<div class="approach-panel" role="status"><span class="beacon-pulse"></span><h3>횡단보도에 가까워지고 있어요</h3><p>2초 뒤 가상 비콘을 감지해요.</p></div>`, "approach-overlay");
  announce("2초 뒤 가상 비콘을 감지해요.");
  timer = setInterval(tick, 100);
  document.querySelector("#phone-demo")?.scrollIntoView({ behavior: "smooth", block: "center" });
}
document.addEventListener("click", (event) => {
  const button = event.target.closest("button");
  if (!button) return;
  if (button.dataset.page) openPage(button.dataset.page);
  if (button.dataset.start) startTrial(button.dataset.start);
  if (button.hasAttribute("data-dismiss-warning")) dismissWarning();
  if (button.hasAttribute("data-close-result")) {
    clearTrial();
    openPage("avatar");
    content.querySelector("button")?.focus({ preventScroll: true });
  }
  if (button.hasAttribute("data-screen-on") && trial) {
    const originalStart = trial.startedAt;
    trial = { ...trial, mode: "warning", phase: "approach", approachAt: 0 };
    tick();
    if (trial) trial = { ...trial, startedAt: originalStart };
  }
  if (button.dataset.redeem) {
    const next = redeem(state, button.dataset.redeem);
    if (next !== state) {
      save(next);
      render();
      showNotice("가상 아이템을 모았어요. 하슬랑에게 입혀 보세요.");
    }
  }
  if (button.dataset.equip) { save(toggleEquipment(state, button.dataset.equip)); render(); }
  if (button.dataset.preview) {
    previewLevel = button.dataset.preview === "actual" ? null : Number(button.dataset.preview);
    render();
  }
});
document.querySelector("#start-demo").addEventListener("click", () => startTrial("warning"));
document.querySelector("#screen-off-demo").addEventListener("click", () => startTrial("off"));
document.querySelector("#leave-demo")?.addEventListener("click", depart);
document.querySelector("#reset-demo").addEventListener("click", () => {
  clearTrial();
  save(initialState());
  page = "home";
  previewLevel = null;
  render();
  showNotice("웹 체험 기록을 처음으로 돌렸어요.");
});
const installDialog = document.querySelector("#install-dialog");
document.querySelector("#install-guide").addEventListener("click", () => installDialog.showModal());
installDialog.querySelector("[data-close]").addEventListener("click", () => installDialog.close());
installDialog.addEventListener("click", (event) => {
  if (event.target === installDialog) {
    const bounds = installDialog.getBoundingClientRect();
    if (event.clientX < bounds.left || event.clientX > bounds.right ||
        event.clientY < bounds.top || event.clientY > bounds.bottom) installDialog.close();
  }
});
document.addEventListener("visibilitychange", () => {
  if (document.hidden && trial) {
    clearTrial();
    render();
    announce("웹 체험을 벗어나 적립을 멈췄어요. 다시 시작할 수 있어요.");
  }
});
window.addEventListener("storage", (event) => {
  if (event.key !== STORAGE_KEY) return;
  clearTrial();
  let incoming;
  try { incoming = JSON.parse(event.newValue); } catch { incoming = null; }
  state = normalizeState(incoming);
  render();
  announce("다른 창에서 바뀐 웹 체험 기록을 반영했어요.");
});
render();
announce(storageAvailable ? "비콘 접근 체험을 눌러 시작해 보세요." : "이 창에서 체험할 수 있어요.");
