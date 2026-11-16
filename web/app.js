import { drawAvatar } from "./avatar.js";
import {
  REWARD, DAILY_LIMIT, HOLD_TIME, TITLES, ITEMS, initialState,
  normalizeState, award, redeem, toggleEquipment, currentTitle,
} from "./rewards.js";

const RESULT_DURATION = 3000;
const phone = document.querySelector("#phone-demo .phone-screen");
const homeBar = document.querySelector("[data-device-home]");
const STORAGE_KEY = "gyodong5-2-web-prototype-v1";
const content = document.querySelector("#phone-content");
const navigation = document.querySelector("#phone-nav");
const status = document.querySelector("#demo-status");
const preview = document.querySelector("#app-preview");
const previewContent = document.querySelector("#preview-content");
const previewNavigation = document.querySelector("#preview-nav");
const previewStatus = document.querySelector("#preview-status");
const pages = [["home", "홈"], ["shop", "상점"], ["titles", "칭호"], ["avatar", "하슬랑"], ["guide", "안내"], ["design", "디자인"]];
let stored;
try { stored = JSON.parse(localStorage.getItem(STORAGE_KEY)); } catch { stored = null; }
let state = normalizeState(stored);
let page = "home";
let surface = "reading";
let media = "video";
let playing = true;
let previewPage = "home";
let standaloneGrowth = null;
let locationStage = "away";
let deviceOff = false;
let trial = null;
let timer = null;
let arrivalTimer = null;
let resultTimer = null;
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
function home(standalone) {
  const title = currentTitle(state.total);
  return `<section class="points-hero">
    <div class="wallet"><p>사용 가능한 포인트</p><strong data-balance>${money(state.total - state.spent)}<small>점</small></strong></div>
    <button class="avatar-shortcut" data-page="avatar" aria-label="하슬랑 키우기 열기">${canvasMarkup()}<span>하슬랑 키우기 ↗</span></button>
    <div class="wallet-metrics"><span>누적 적립<b>${money(state.total)}점</b></span><span>오늘 적립<b>${state.count} / ${DAILY_LIMIT}회</b></span></div>
  </section>
  <button class="app-card shortcut" data-page="titles"><span><b>교통안전 칭호</b><small>${title.label}</small></span><span class="text-action">칭호 보기 ↗</span></button>
  <button class="app-card shortcut" data-page="shop"><span><b>포인트 상점</b><small>교환권 예시와 하슬랑 꾸미기</small></span><span class="text-action">상점 보기 ↗</span></button>
  ${standalone ? '<a class="app-button preview-walk" href="#experience">횡단보도 체험으로 이동</a>' : `<section class="app-card trial-card"><h4>가상 비콘 체험</h4><p>다른 화면을 보며 걷는 상황을 체험해 보세요. 비콘 접근 후 화면을 끄거나 경고를 10초 유지하면 10점이 쌓여요.</p>
    <button class="app-button" data-start>가상 접근 시작</button>
  </section>`}`;
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
function avatar(growth) {
  const actual = currentTitle(state.total);
  const displayed = growth === null ? actual : TITLES[growth];
  const next = TITLES.find((entry) => entry.threshold > state.total);
  return `${titleHeading("하슬랑 키우기", "신호등 알리미를 잘 이용할 때마다 자라요.")}
  <section class="app-card avatar-stage">${canvasMarkup("large")}<h4>${displayed.label}</h4><p>${growth === null ? next ? `다음 칭호까지 알림 ${Math.ceil((next.threshold - state.total) / REWARD)}회` : "가장 높은 칭호에 도달했어요." : "성장 모습 미리보기"}</p></section>
  <fieldset class="growth-preview"><legend>성장 모습 미리보기</legend><div>${TITLES.map((entry) => `<button data-preview="${entry.level}" aria-pressed="${displayed.level === entry.level}">${entry.level + 1}단계</button>`).join("")}</div><button class="text-button" data-preview="actual">내 하슬랑 보기</button></fieldset>
  <h4 class="app-section-title">하슬랑 꾸미기</h4>
  ${ITEMS.map((item) => {
    const owned = state.owned.includes(item.id);
    const worn = state.equipped.includes(item.id);
    return `<div class="app-card item-row"><span class="item-symbol" aria-hidden="true">${item.symbol}</span><div><b>${item.label}</b><small>${worn ? "착용 중" : owned ? "보유 중" : "상점에서 교환할 수 있어요."}</small></div><button class="small-button" ${owned ? `data-equip="${item.id}"` : 'data-page="shop"'}>${owned ? worn ? "벗기" : "착용" : "상점"}</button></div>`;
  }).join("")}`;
}
function guide() {
  return `${titleHeading("어떻게 작동하나요?", "강릉시 시민제안 제출용 시제품입니다. 정식 출시 앱이 아닙니다.")}
  <section class="app-card"><h4>지금은 가상 비콘 체험이에요</h4><p>웹에서는 이 휴대폰 모양 안에서 경고와 적립을 재현해요. 안드로이드 시제품은 다른 앱 위에 경고를 띄울 수 있어요.</p></section>
  <ol class="guide-steps"><li><b>횡단보도 앞 인도에 비콘 설치</b><p>차도에 들어가기 전 대기 구역에 블루투스 비콘을 설치해요.</p></li><li><b>앱이 등록된 비콘 신호 감지</b><p>신호 세기를 바탕으로 가까워졌는지 판단해요. 실제 감지 범위는 현장에서 조정해요.</p></li><li><b>화면을 끄거나 경고창을 유지</b><p>비콘 접근 후 화면을 끄고 있거나 경고를 닫지 않은 채 10초가 지나면 10점씩 하루 최대 3회 적립해요.</p></li><li><b>하슬랑과 함께 습관 만들기</b><p>누적 기록에 따라 칭호가 열리고 하슬랑이 자라요. 웹 체험 기록은 이 브라우저에 저장돼요.</p></li></ol>`;
}

function launcher() {
  return `<div class="device-launcher">
    <div class="launcher-clock" data-phone-clock aria-hidden="true">9:41</div>
    <p class="launcher-hint" id="launcher-hint">‘신호등 알리미’ 아이콘을 눌러<br>모은 포인트를 확인해 보세요.</p>
    <div class="launcher-apps" role="group" aria-label="가상 휴대폰의 앱 아이콘">
      <button type="button" data-media="chat" class="launcher-icon"><span class="launcher-glyph icon-talk" aria-hidden="true"><i>TALK</i></span><b>카카오톡</b></button>
      <button type="button" data-media="camera" class="launcher-icon"><span class="launcher-glyph icon-camera" aria-hidden="true"><i></i></span><b>카메라</b></button>
      <button type="button" data-media="browser" class="launcher-icon"><span class="launcher-glyph icon-chrome" aria-hidden="true"><i></i></span><b>크롬</b></button>
      <button type="button" data-media="video" class="launcher-icon"><span class="launcher-glyph icon-video" aria-hidden="true"><i>▶</i></span><b>영상</b></button>
      <button type="button" data-open-app class="launcher-icon launcher-featured" aria-describedby="launcher-hint"><span class="launcher-glyph launcher-crossing" aria-hidden="true"><img src="assets/signal-alert-app-icon.png" alt="" width="47" height="47"></span><b>신호등 알리미</b></button>
      <button type="button" data-media="photos" class="launcher-icon"><span class="launcher-glyph icon-photos" aria-hidden="true"><i></i></span><b>사진</b></button>
    </div>
  </div>`;
}
function reading() {
  if (media === "camera" || media === "photos") {
    const label = media === "camera" ? "카메라 미리보기" : "사진";
    return `<div class="phone-media phone-media--gallery">
      <h3>${label}</h3>
      <figure><img src="assets/gangneung-crosswalk-open-scene.jpg" alt="강릉역 앞 가상 거리"><figcaption>강릉역 앞 · 가상 체험 장면</figcaption></figure>
    </div>`;
  }
  if (media === "browser") {
    return `<div class="phone-media phone-media--browser">
      <div class="browser-address">강릉 산책 노트</div>
      <h3>강릉역 광장 둘러보기</h3>
      <figure><img src="assets/gangneung-crosswalk-open-scene.jpg" alt="강릉역 앞 가상 거리"></figure>
      <p class="media-caption">강릉역에서 시작하는 가상 산책</p>
    </div>`;
  }
  return `<div class="phone-media phone-media--${media}">
    <div class="media-switch" role="group" aria-label="가상 휴대폰에서 사용할 앱">
      <button type="button" data-media="video" aria-pressed="${media === "video"}">영상</button>
      <button type="button" data-media="chat" aria-pressed="${media === "chat"}">메신저</button>
    </div>
    ${media === "video" ? `<div class="video-header"><span class="video-mark" aria-hidden="true">▶</span><b>영상</b></div>
    <div class="sample-player ${playing ? "is-playing" : ""}">
      <div class="sample-film" aria-hidden="true"></div>
      <button class="sample-play" type="button" data-play aria-label="${playing ? "영상 일시정지" : "영상 재생"}">${playing ? "Ⅱ" : "▶"}</button>
      <span class="sample-progress" aria-hidden="true"></span>
    </div>
    <h3>강릉역에서 시작하는 산책</h3>
    <p class="media-caption">가상 영상 · 강릉 산책 노트</p>
    <div class="media-author"><span aria-hidden="true">산</span><b>강릉 산책 노트</b></div>`
    : `<div class="chat-header"><b>친구와의 대화</b><span>가상 메신저</span></div>
    <div class="chat-thread">
      <p>강릉역 앞에 도착했어?</p>
      <p class="chat-mine">응, 광장 쪽으로 가고 있어.</p>
      <p>그럼 역 입구에서 만나자!</p>
    </div>`}
  </div>`;
}
function updateJourney() {
  document.querySelector("#street-viewport").dataset.stage = locationStage;
  document.querySelector("#journey-place").textContent = {
    away: "멀리 횡단보도가 보여요", approaching: "횡단보도가 가까워졌어요",
    near: "횡단보도 앞에 도착했어요", passed: "횡단보도에서 멀어졌어요",
  }[locationStage];
  document.querySelector("#journey-distance").textContent = {
    away: "오른쪽 화살표를 눌러 가까이 가 보세요", approaching: "한 번 더 이동하면 횡단보도 앞이에요",
    near: "휴대폰을 내려놓고 주변을 살펴요", passed: "안전한 곳에서 앱을 열어 포인트를 확인해요",
  }[locationStage];
  document.querySelector("#walk-instruction").textContent = {
    away: "횡단보도로 가까이", approaching: "한 걸음 더",
    near: "횡단보도에서 멀리", passed: "산책 다시 시작",
  }[locationStage];
  const forward = document.querySelector("#start-demo");
  document.querySelector("#walk-back").disabled = locationStage === "away";
  forward.setAttribute("aria-label", {
    away: "횡단보도로 가까이 이동", approaching: "횡단보도 바로 앞으로 이동",
    near: "횡단보도에서 벗어나기", passed: "산책 다시 시작",
  }[locationStage]);
  const power = document.querySelector("#screen-off-toggle");
  const powerLabel = deviceOff ? "화면 켜기" : "화면 끄기";
  power.title = powerLabel;
  power.setAttribute("aria-label", "가상 휴대폰 " + powerLabel);
  phone.classList.toggle("is-off", deviceOff);
}

function design() {
  return `${titleHeading("디자인 소개", "5난초가 정한 이름과 로고")}
  <section class="app-card design-card">
    <img src="assets/signal-alert-app-icon.png" alt="둥근 사각형 사이렌 아이콘" width="192" height="192">
    <h4>신호등 알리미</h4>
    <p>5난초 학생들이 만든 이름과 아이콘 중에서 학급 투표로 ‘신호등 알리미’와 이 사이렌 배지를 골랐어요.</p>
  </section>`;
}

function paintAvatars(target, currentPage, growth) {
  const level = currentPage === "avatar" && growth !== null ? growth : currentTitle(state.total).level;
  target.querySelectorAll("canvas").forEach((canvas) => {
    drawAvatar(canvas, level, state.equipped);
    canvas.setAttribute("aria-label", `${TITLES[level].label} 하슬랑 픽셀 캐릭터`);
  });
}
function renderApp(target, menu, currentPage, growth, standalone = false) {
  const renderPage = {
    home: () => home(standalone), shop, titles, avatar: () => avatar(growth), guide, design,
  };
  target.innerHTML = renderPage[currentPage]();
  if (!menu.childElementCount) {
    menu.innerHTML = pages.map(([id, label]) => `<button type="button" data-page="${id}">${label}</button>`).join("");
  }
  for (const button of menu.children) {
    if (button.dataset.page === currentPage) button.setAttribute("aria-current", "page");
    else button.removeAttribute("aria-current");
  }
  paintAvatars(target, currentPage, growth);
}
function renderPreview() {
  renderApp(previewContent, previewNavigation, previewPage, standaloneGrowth, true);
}
function render({ refreshMedia = false } = {}) {
  state = normalizeState(state);
  const appOpen = surface === "app";
  phone.dataset.surface = surface === "reading" ? media : surface;
  document.querySelector("#phone-demo .phone-brand").hidden = !appOpen;
  navigation.hidden = !appOpen;
  content.classList.toggle("device-content", !appOpen);
  if (appOpen) renderApp(content, navigation, page, previewLevel);
  else if (surface === "launcher") {
    content.innerHTML = launcher();
  } else if (refreshMedia || !content.querySelector(".phone-media")) {
    content.innerHTML = reading();
  }
  updateJourney();
  renderPreview();
  updatePhoneClock();
}
function openPage(destination) {
  if (!pages.some(([id]) => id === destination)) return;
  surface = "app";
  page = destination;
  previewLevel = null;
  render();
  content.scrollTop = 0;
}
function removeOverlay() {
  clearTimeout(resultTimer);
  resultTimer = null;
  overlay?.remove();
  overlay = null;
  content.inert = false;
  navigation.inert = false;
  homeBar.disabled = false;
}
function clearTrial() {
  clearTimeout(arrivalTimer);
  arrivalTimer = null;
  clearInterval(timer);
  timer = null;
  trial = null;
  removeOverlay();
  updateJourney();
}
function announce(message) {
  status.textContent = message;
}
function showOverlay(html, className = "") {
  removeOverlay();
  overlay = document.createElement("div");
  overlay.className = `phone-overlay ${className}`;
  overlay.dataset.demoPanel = "true";
  overlay.innerHTML = html;
  phone.append(overlay);
  homeBar.disabled = true;
  content.inert = true;
  navigation.inert = true;
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
  locationStage = "passed";
  updateJourney();
  if (!trial && arrivalTimer === null) {
    announce("횡단보도에서 벗어났어요. 신호등 알리미 앱에서 포인트와 하슬랑을 확인해 보세요.");
  }
}
function finishTrial() {
  clearTrial();
  const outcome = award(state);
  save(outcome.state);
  render();
  const heading = outcome.earned ? "+10 포인트 적립" : "오늘 적립 한도에 도달했어요";
  announce(`${heading}. 오늘 ${state.count} / ${DAILY_LIMIT}회 적립`);
  if (deviceOff) {
    showScreenOff();
    announce(heading + " · 화면을 켜지 않고 기록했어요. 안전한 곳에서 앱을 열어 확인해 보세요.");
    return;
  }
  showOverlay(`<div class="result-panel" role="status"><h3>${heading}</h3><p>오늘 ${state.count} / ${DAILY_LIMIT}회 적립</p><button type="button" class="result-dismiss" data-close-result>닫기</button></div>`, "result-overlay");
  resultTimer = setTimeout(closeResult, RESULT_DURATION);
}
function closeResult() {
  if (!overlay?.classList.contains("result-overlay")) return;
  const focusWasInResult = overlay.contains(document.activeElement);
  removeOverlay();
  if (focusWasInResult) homeBar.focus({ preventScroll: true });
}
function showScreenOff() {
  showOverlay('<div role="status" aria-label="화면 꺼짐"></div>', "screen-off-overlay");
}
function toggleScreen() {
  closeResult();
  deviceOff = !deviceOff;
  if (deviceOff) {
    showScreenOff();
  } else {
    removeOverlay();
    if (trial) {
      showWarning();
      tick();
    } else render();
  }
  updateJourney();
}
function showWarning() {
  const warning = showOverlay(`<div class="warning-panel" role="dialog" aria-label="횡단보도 경고"><img class="warning-brand-logo" src="assets/signal-alert-app-icon.png" alt="" width="56" height="56"><h3>횡단보도 앞입니다.<br>주변을 봐주세요.</h3><div class="warning-clock"><span data-countdown role="timer" aria-label="적립까지 남은 초">${Math.ceil(HOLD_TIME / 1000)}</span><small>초 뒤 적립</small></div><button data-dismiss-warning>경고 닫기</button></div>`, "warning-overlay");
  warning.querySelector("button").focus({ preventScroll: true });
}
function tick() {
  if (!trial) return;
  const elapsed = performance.now() - trial.startedAt;
  if (elapsed >= HOLD_TIME) {
    finishTrial();
    return;
  }
  const left = Math.ceil((HOLD_TIME - elapsed) / 1000);
  const countdown = overlay?.querySelector("[data-countdown]");
  if (countdown && countdown.textContent !== String(left)) countdown.textContent = left;
  const message = `${left}초 뒤 적립 · ${deviceOff ? "화면 꺼짐" : "경고 유지 중"}`;
  if (status.textContent !== message) announce(message);
}
function startTrial() {
  if (locationStage === "near") return;
  locationStage = "approaching";
  updateJourney();
  announce("같은 횡단보도가 조금 더 가까워졌어요. 오른쪽 화살표를 한 번 더 눌러 보세요.");
}
function arrive() {
  locationStage = "near";
  updateJourney();
  if (trial || arrivalTimer !== null) return;
  announce("횡단보도 바로 앞에 도착했어요.");
  // Wait for the last movement to settle before showing the phone warning.
  const delay = matchMedia("(prefers-reduced-motion: reduce)").matches ? 0 : 780;
  arrivalTimer = setTimeout(() => {
    arrivalTimer = null;
    if (!deviceOff) showWarning();
    trial = { startedAt: performance.now() };
    tick();
    timer = setInterval(tick, 100);
  }, delay);
}
function updatePhoneClock() {
  const time = new Intl.DateTimeFormat("en-GB", { hour: "numeric", minute: "2-digit", timeZone: "Asia/Seoul" }).format(new Date());
  for (const clock of document.querySelectorAll("[data-phone-clock]")) {
    if (clock.textContent !== time) clock.textContent = time;
  }
}
function showDeviceHome() {
  if (overlay || surface === "launcher") return;
  surface = "launcher";
  render();
  content.scrollTop = 0;
}
let homeSwipeStart = null;
homeBar.addEventListener("pointerdown", (event) => {
  if (!event.isPrimary || event.button !== 0 || homeBar.disabled) return;
  homeSwipeStart = event.clientY;
  homeBar.setPointerCapture(event.pointerId);
});
homeBar.addEventListener("pointerup", (event) => {
  if (homeSwipeStart !== null && homeSwipeStart - event.clientY >= 20) showDeviceHome();
  homeSwipeStart = null;
});
homeBar.addEventListener("lostpointercapture", () => { homeSwipeStart = null; });

document.addEventListener("click", (event) => {
  const button = event.target.closest("button");
  if (!button) return;
  const inPreview = preview.contains(button);
  if (button.dataset.page) {
    if (inPreview) {
      previewPage = button.dataset.page;
      standaloneGrowth = null;
      renderPreview();
      previewContent.scrollTop = 0;
    } else openPage(button.dataset.page);
  }
  if (button.dataset.media) {
    media = button.dataset.media;
    surface = "reading";
    render({ refreshMedia: true });
    (content.querySelector(`[data-media="${media}"]`) || content).focus({ preventScroll: true });
  }
  if (button.hasAttribute("data-play")) {
    playing = !playing;
    content.querySelector(".sample-player").classList.toggle("is-playing", playing);
    button.setAttribute("aria-label", playing ? "영상 일시정지" : "영상 재생");
    button.textContent = playing ? "Ⅱ" : "▶";
  }
  if (button.hasAttribute("data-open-app")) {
    if (trial || locationStage === "near") {
      showNotice("횡단보도에서 벗어난 뒤 앱을 열어 보세요.");
      return;
    }
    openPage("home");
  }
  if (button.hasAttribute("data-device-home")) showDeviceHome();
  if (button.hasAttribute("data-start")) {
    if (!trial && arrivalTimer === null && locationStage !== "near") {
      surface = "reading";
      render();
      content.scrollTop = 0;
    }
    startTrial();
    document.querySelector("#experience").scrollIntoView({ block: "start" });
  }
  if (button.hasAttribute("data-dismiss-warning")) dismissWarning();
  if (button.hasAttribute("data-close-result")) closeResult();
  if (button.dataset.redeem) {
    const next = redeem(state, button.dataset.redeem);
    if (next !== state) {
      save(next);
      render();
      if (inPreview) previewStatus.textContent = "가상 아이템을 모았어요. 하슬랑에게 입혀 보세요.";
      else showNotice("가상 아이템을 모았어요. 하슬랑에게 입혀 보세요.");
    }
  }
  if (button.dataset.equip) { save(toggleEquipment(state, button.dataset.equip)); render(); }
  if (button.dataset.preview) {
    const level = button.dataset.preview === "actual" ? null : Number(button.dataset.preview);
    if (inPreview) {
      standaloneGrowth = level;
      renderPreview();
    } else {
      previewLevel = level;
      render();
    }
  }
});
document.querySelector("#start-demo").addEventListener("click", () => {
  if (locationStage === "away") startTrial();
  else if (locationStage === "approaching") arrive();
  else if (locationStage === "near") depart();
  else if (locationStage === "passed") {
    locationStage = "away";
    updateJourney();
    announce("다시 산책을 시작할 수 있어요.");
  }
});
document.querySelector("#walk-back").addEventListener("click", () => {
  locationStage = "away";
  updateJourney();
  if (!trial && arrivalTimer === null) announce("강릉역 앞 산책 시작 지점으로 돌아왔어요.");
});
document.querySelector("#screen-off-toggle").addEventListener("click", toggleScreen);

document.querySelector("#reset-demo").addEventListener("click", () => {
  clearTrial();
  save(initialState());
  page = "home";
  surface = "reading";
  locationStage = "away";
  media = "video";
  playing = true;
  deviceOff = false;
  previewLevel = null;
  standaloneGrowth = null;
  previewStatus.textContent = "";
  render({ refreshMedia: true });
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
  if (document.hidden && (trial || arrivalTimer !== null)) {
    clearTrial();
    locationStage = "approaching";
    render();
    if (deviceOff) showScreenOff();
    announce("탭을 벗어나 적립을 멈췄어요. 다시 횡단보도 앞으로 이동해 보세요.");
  }
});
window.addEventListener("storage", (event) => {
  if (event.key !== STORAGE_KEY) return;
  if (trial || arrivalTimer !== null) locationStage = "approaching";
  clearTrial();
  let incoming;
  try { incoming = JSON.parse(event.newValue); } catch { incoming = null; }
  state = normalizeState(incoming);
  render();
  if (deviceOff) showScreenOff();
  announce("다른 창에서 바뀐 웹 체험 기록을 반영했어요.");
});
render();
setInterval(updatePhoneClock, 15_000);
announce(storageAvailable ? "오른쪽 화살표로 가까이 가 보세요. 휴대폰 옆 버튼으로 화면을 끌 수도 있어요." : "이 창에서 체험할 수 있어요.");
