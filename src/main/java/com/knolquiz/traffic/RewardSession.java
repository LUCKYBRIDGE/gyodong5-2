package com.knolquiz.traffic;

record RewardSession(long startedAtMillis) {
    static final long HOLD_MILLIS = 10_000;

    enum Status { WAITING, EARNED, CANCELLED }

    static RewardSession start(long nowMillis) {
        return new RewardSession(nowMillis);
    }

    Status status(long nowMillis, boolean inRange, boolean warningVisible, boolean screenOff) {
        if (!inRange || !(warningVisible || screenOff)) return Status.CANCELLED;
        return nowMillis - startedAtMillis >= HOLD_MILLIS ? Status.EARNED : Status.WAITING;
    }
}
