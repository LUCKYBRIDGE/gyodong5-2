package com.knolquiz.traffic;

final class Proximity {
    private Proximity() {}

    static boolean isNear(boolean previouslyNear, int rssi) {
        return rssi >= (previouslyNear ? -75 : -68);
    }
}
