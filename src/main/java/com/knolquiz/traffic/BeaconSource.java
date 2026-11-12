package com.knolquiz.traffic;

import android.os.Handler;
import android.os.Looper;

import java.util.function.Consumer;

/** Reports transitions into and out of the configured beacon's range on the main thread. */
interface BeaconSource {
    void start(Consumer<Boolean> listener);

    void stop();

    final class Demo implements BeaconSource {
        private static final long APPROACH_DELAY_MS = 7_000;
        private static final long IN_RANGE_MS = 22_000;

        private final Handler handler = new Handler(Looper.getMainLooper());
        private Runnable approach;
        private Runnable departure;

        @Override
        public void start(Consumer<Boolean> listener) {
            stop();
            approach = () -> listener.accept(true);
            departure = () -> listener.accept(false);
            handler.postDelayed(approach, APPROACH_DELAY_MS);
            handler.postDelayed(departure, APPROACH_DELAY_MS + IN_RANGE_MS);
        }

        @Override
        public void stop() {
            if (approach != null) {
                handler.removeCallbacks(approach);
                approach = null;
            }
            if (departure != null) {
                handler.removeCallbacks(departure);
                departure = null;
            }
        }
    }
}
