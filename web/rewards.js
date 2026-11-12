export const REWARD = 10;
export const DAILY_LIMIT = 3;
export const HOLD_TIME = 10_000;
export const TITLES = [
  { label: "함께 걷기 시작", threshold: 0, level: 0 },
  { label: "안전 첫걸음", threshold: 10, level: 1 },
  { label: "횡단보도 친구", threshold: 30, level: 2 },
  { label: "교통안전 지킴이", threshold: 70, level: 3 },
];
export const ITEMS = [
  { id: "REFLECTOR", label: "반짝이 반사판", price: 10, symbol: "✦" },
  { id: "STICKER", label: "횡단보도 스티커", price: 30, symbol: "▤" },
  { id: "BAG_TAG", label: "안전 가방고리", price: 60, symbol: "◇" },
];
export function today() {
  return new Intl.DateTimeFormat("en-CA", {
    timeZone: "Asia/Seoul", year: "numeric", month: "2-digit", day: "2-digit",
  }).format(new Date());
}
export function initialState(day = today()) {
  return { day, count: 0, total: 0, spent: 0, owned: [], equipped: [] };
}
export function normalizeState(value, day = today()) {
  if (!value || typeof value !== "object") return initialState(day);
  const validNumber = (number) => Number.isSafeInteger(number) && number >= 0;
  if (!["total", "spent", "count"].every((key) => validNumber(value[key]))
      || value.spent > value.total || value.count > DAILY_LIMIT) return initialState(day);
  const savedOwned = Array.isArray(value.owned) ? value.owned : [];
  const savedEquipment = Array.isArray(value.equipped) ? value.equipped : [];
  const owned = ITEMS.filter((item) => savedOwned.includes(item.id)).map((item) => item.id);
  const equipped = owned.filter((id) => savedEquipment.includes(id));
  return { day, count: value.day === day ? value.count : 0,
    total: value.total, spent: value.spent, owned, equipped };
}
export function award(state, day = today()) {
  const current = normalizeState(state, day);
  if (current.count >= DAILY_LIMIT) return { state: current, earned: false };
  return { state: { ...current, count: current.count + 1, total: current.total + REWARD }, earned: true };
}
export function redeem(state, id) {
  const item = ITEMS.find((entry) => entry.id === id);
  if (!item || state.owned.includes(id) || state.total - state.spent < item.price) return state;
  return { ...state, spent: state.spent + item.price, owned: [...state.owned, id] };
}
export function toggleEquipment(state, id) {
  if (!state.owned.includes(id)) return state;
  return { ...state, equipped: state.equipped.includes(id)
    ? state.equipped.filter((entry) => entry !== id) : [...state.equipped, id] };
}
export function currentTitle(total) {
  return TITLES.reduce((current, title) => total >= title.threshold ? title : current, TITLES[0]);
}
