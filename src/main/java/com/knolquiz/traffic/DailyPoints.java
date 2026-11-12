package com.knolquiz.traffic;

record DailyPoints(String day, int count, int total) {
    static final int DAILY_LIMIT = 3;
    static final int POINTS_PER_REWARD = 10;

    int countFor(String today) {
        return today.equals(day) ? count : 0;
    }

    DailyPoints award(String today) {
        int todayCount = countFor(today);
        if (todayCount >= DAILY_LIMIT) return this;
        return new DailyPoints(today, todayCount + 1, total + POINTS_PER_REWARD);
    }
}
