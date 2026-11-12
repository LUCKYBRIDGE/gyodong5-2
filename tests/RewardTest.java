package com.knolquiz.traffic;

public final class RewardTest {
    private static int assertions;

    public static void main(String[] args) {
        testRewardSession();
        testProximity();
        testDailyPoints();
        System.out.println("Passed " + assertions + " reward and proximity checks.");
    }

    private static void testRewardSession() {
        long start = 12_345;
        RewardSession session = RewardSession.start(start);
        check("starts waiting", "WAITING", session.status(start, true, true, false).name());
        check("waits for the whole duration", "WAITING",
                session.status(start + 9_999, true, true, false).name());
        check("earns at exactly ten seconds", "EARNED",
                session.status(start + 10_000, true, true, false).name());
        check("earns after ten seconds", "EARNED",
                session.status(start + 10_001, true, true, false).name());
        check("leaving range cancels", "CANCELLED",
                session.status(start + 5_000, false, true, false).name());
        check("dismissing the popup cancels", "CANCELLED",
                session.status(start + 5_000, true, false, false).name());
        check("leaving range at the deadline cancels", "CANCELLED",
                session.status(start + 10_000, false, true, false).name());
        check("dismissing the popup at the deadline cancels", "CANCELLED",
                session.status(start + 10_000, true, false, false).name());
        check("screen off inside range can earn without a popup", "EARNED",
                session.status(start + 10_000, true, false, true).name());
        check("screen off still requires the whole duration", "WAITING",
                session.status(start + 9_999, true, false, true).name());
        check("screen off outside range cannot earn", "CANCELLED",
                session.status(start + 10_000, false, false, true).name());
        check("waiting with the warning before turning off the screen", "WAITING",
                session.status(start + 5_000, true, true, false).name());
        check("switching from the warning to screen off preserves safe duration", "EARNED",
                session.status(start + 10_000, true, false, true).name());
    }

    private static void testProximity() {
        check("enters at threshold", true, Proximity.isNear(false, -68));
        check("enters on stronger signal", true, Proximity.isNear(false, -55));
        check("stays out just below entry threshold", false, Proximity.isNear(false, -69));
        check("stays near within hysteresis", true, Proximity.isNear(true, -72));
        check("stays near at exit threshold", true, Proximity.isNear(true, -75));
        check("exits just below threshold", false, Proximity.isNear(true, -76));
        check("does not reenter on a weak signal", false, Proximity.isNear(false, -75));
    }

    private static void testDailyPoints() {
        String firstDay = "2026-11-13";
        String nextDay = "2026-11-14";
        DailyPoints initial = new DailyPoints(firstDay, 0, 0);

        DailyPoints first = initial.award(firstDay);
        check("first reward adds ten points", 10, first.total());
        check("first reward adds one use", 1, first.countFor(firstDay));
        check("awarding preserves original state", 0, initial.total());
        DailyPoints third = first.award(firstDay).award(firstDay);
        check("three rewards add thirty points", 30, third.total());
        check("counts three rewards", 3, third.countFor(firstDay));
        check("a fourth reward is capped", third, third.award(firstDay));

        check("yesterday's count does not carry over", 0, third.countFor(nextDay));
        DailyPoints next = third.award(nextDay);
        check("next day starts with one use", 1, next.countFor(nextDay));
        check("next day's reward adds to the lifetime total", 40, next.total());
        check("next day's state has the correct date", nextDay, next.day());
    }

    private static void check(String description, Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError(description + ": expected " + expected + ", got " + actual);
        }
        assertions++;
    }
}
