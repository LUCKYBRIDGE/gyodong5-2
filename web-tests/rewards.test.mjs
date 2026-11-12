import assert from "node:assert/strict";
import { test } from "node:test";
import {
  initialState, normalizeState, award, redeem, toggleEquipment, currentTitle,
} from "../web/rewards.js";

const day = "2026-11-13";
test("three rewards per day and rollover preserve lifetime growth", () => {
  let state = initialState(day);
  for (let i = 0; i < 3; i++) state = award(state, day).state;
  assert.equal(state.total, 30);
  assert.equal(state.count, 3);
  assert.equal(award(state, day).earned, false);
  const next = award(state, "2026-11-14");
  assert.equal(next.state.total, 40);
  assert.equal(next.state.count, 1);
});
test("purchases spend balance once while lifetime growth remains", () => {
  const state = { ...initialState(day), total: 30 };
  const bought = redeem(state, "REFLECTOR");
  assert.equal(bought.spent, 10);
  assert.equal(bought.total, 30);
  assert.equal(currentTitle(bought.total).label, "횡단보도 친구");
  assert.equal(redeem(bought, "REFLECTOR"), bought);
  assert.equal(redeem(bought, "BAG_TAG"), bought);
  assert.deepEqual(state.owned, []);
});
test("only owned items can be equipped and toggled", () => {
  const state = initialState(day);
  assert.equal(toggleEquipment(state, "REFLECTOR"), state);
  const bought = redeem({ ...state, total: 10 }, "REFLECTOR");
  const equipped = toggleEquipment(bought, "REFLECTOR");
  assert.deepEqual(equipped.equipped, ["REFLECTOR"]);
  assert.deepEqual(toggleEquipment(equipped, "REFLECTOR").equipped, []);
});
test("corrupt saved records recover and unknown cosmetics are removed", () => {
  assert.deepEqual(normalizeState({ total: -10 }, day), initialState(day));
  assert.deepEqual(normalizeState({ total: 10, spent: 20, count: 1 }, day), initialState(day));
  assert.deepEqual(normalizeState({ ...initialState(day), owned: {}, equipped: {} }, day), initialState(day));
  assert.deepEqual(normalizeState({ ...initialState(day), owned: ["FAKE"], equipped: ["FAKE"] }, day), initialState(day));
});
