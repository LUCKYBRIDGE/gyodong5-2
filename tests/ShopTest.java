package com.knolquiz.traffic;

import java.util.Objects;
import java.util.Set;

public final class ShopTest {
    private static int assertions;

    public static void main(String[] args) {
        testShop();
        testTitles();
        System.out.println("Passed " + assertions + " shop and title checks.");
    }

    private static void testShop() {
        check("prior rewards are available without shop data", 20, Rewards.balance(20, 0));
        check("spending reduces available points", 10, Rewards.balance(20, 10));
        check("balance cannot be negative", 0, Rewards.balance(10, 30));
        check("invalid spending cannot increase balance", 10, Rewards.balance(10, -3));

        for (Rewards.Item item : Rewards.Item.values()) {
            check(item + " is unavailable below price", false,
                    Rewards.canRedeem(item, item.price - 1, 0, Set.of()));
            check(item + " is available at exact price", true,
                    Rewards.canRedeem(item, item.price + 10, 10, Set.of()));
            check(item + " cannot be redeemed twice", false,
                    Rewards.canRedeem(item, 100, 0, Set.of(item.name())));
        }

        check("an owned reward does not block a different reward", true,
                Rewards.canRedeem(Rewards.Item.STICKER, 40, 10, Set.of(Rewards.Item.REFLECTOR.name())));
    }

    private static void testTitles() {
        check("no title before first milestone", null, Rewards.currentTitle(9));
        check("first milestone unlocks first title", Rewards.Title.FIRST_STEP, Rewards.currentTitle(10));
        check("first title stays before second milestone", Rewards.Title.FIRST_STEP, Rewards.currentTitle(29));
        check("second milestone unlocks second title", Rewards.Title.CROSSWALK_FRIEND, Rewards.currentTitle(30));
        check("second title stays before final milestone", Rewards.Title.CROSSWALK_FRIEND, Rewards.currentTitle(69));
        check("final milestone unlocks final title", Rewards.Title.SAFETY_GUARDIAN, Rewards.currentTitle(70));
        check("highest title remains after its milestone", Rewards.Title.SAFETY_GUARDIAN, Rewards.currentTitle(120));
    }

    private static void check(String description, Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(description + ": expected " + expected + ", got " + actual);
        }
        assertions++;
    }
}
