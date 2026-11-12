package com.knolquiz.traffic;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;

import java.time.LocalDate;

public final class CrossingService extends Service {
    private static final String CHANNEL = "crossing_demo";
    private static final String STOP = "com.knolquiz.traffic.STOP_DEMO";
    private static final long CHECK_INTERVAL = 200;
    private static final long RESULT_DURATION = 3_000;
    private static final long DISMISS_DURATION = 2_000;
    private static final long DEMO_WAKE_LIMIT = 35_000;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final BeaconSource beacon = new BeaconSource.Demo();
    private CrossingOverlay overlay;
    private RewardSession session;
    private SharedPreferences points;
    private PowerManager.WakeLock demoWakeLock;
    private boolean near;
    private boolean warningShown;

    private final BroadcastReceiver screenOff = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            hideOverlay();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        overlay = new CrossingOverlay(this);
        points = getSharedPreferences("traffic", MODE_PRIVATE);
        demoWakeLock = getSystemService(PowerManager.class)
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getPackageName() + ":BeaconDemo");
        demoWakeLock.setReferenceCounted(false);
        getSystemService(NotificationManager.class).createNotificationChannel(
                new NotificationChannel(CHANNEL, "횡단보도 알림 체험", NotificationManager.IMPORTANCE_LOW));
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(screenOff, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(screenOff, filter);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || STOP.equals(intent.getAction()) || !Settings.canDrawOverlays(this)) {
            stopSelf();
            return START_NOT_STICKY;
        }

        startForeground(1, notification());
        clearOverlay();
        near = false;
        demoWakeLock.acquire(DEMO_WAKE_LIMIT);
        beacon.start(this::onProximityChanged);
        return START_NOT_STICKY;
    }

    private void onProximityChanged(boolean inRange) {
        near = inRange;
        if (!near) {
            clearOverlay();
            stopSelf();
            return;
        }

        checkSession();
    }

    private void checkSession() {
        if (!near || !Settings.canDrawOverlays(this)) {
            clearOverlay();
            return;
        }
        boolean displayOff = !getSystemService(PowerManager.class).isInteractive();
        if (displayOff) hideOverlay();
        if (!displayOff && getSystemService(KeyguardManager.class).isKeyguardLocked()) {
            hideOverlay();
            session = null;
            scheduleCheck();
            return;
        }
        if (!displayOff && !warningShown) {
            warningShown = overlay.showWarning(this::dismissWarning);
            if (warningShown) scheduleCheck();
            return;
        }
        long now = SystemClock.elapsedRealtime();
        if (session == null) session = RewardSession.start(now);
        RewardSession.Status status = session.status(
                now, near, warningShown && overlay.isVisible(), displayOff);
        switch (status) {
            case WAITING -> scheduleCheck();
            case CANCELLED -> clearOverlay();
            case EARNED -> finishReward(!displayOff);
        }
    }

    private void scheduleCheck() {
        handler.postDelayed(this::checkSession, CHECK_INTERVAL);
    }

    private void finishReward(boolean showConfirmation) {
        session = null;
        String today = LocalDate.now().toString();
        DailyPoints previous = new DailyPoints(points.getString("day", ""),
                points.getInt("count", 0), points.getInt("total", 0));
        DailyPoints next = previous.award(today);
        boolean earned = next != previous;
        if (earned) {
            points.edit().putString("day", next.day()).putInt("count", next.count())
                    .putInt("total", next.total()).apply();
        }
        if (showConfirmation) {
            overlay.showResult(earned, next.countFor(today), this::clearOverlay);
            handler.postDelayed(this::clearOverlay, RESULT_DURATION);
        }
    }

    private void clearOverlay() {
        session = null;
        handler.removeCallbacksAndMessages(null);
        hideOverlay();
    }

    private void hideOverlay() {
        warningShown = false;
        overlay.remove();
    }

    private void dismissWarning() {
        clearOverlay();
        overlay.showDismissal();
        handler.postDelayed(this::hideOverlay, DISMISS_DURATION);
    }

    private Notification notification() {
        PendingIntent open = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        PendingIntent stop = PendingIntent.getService(this, 1,
                new Intent(this, CrossingService.class).setAction(STOP), PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(this, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("가상 비콘 체험 중")
                .setContentText("다른 앱 위에 횡단보도 알림을 표시합니다.")
                .setContentIntent(open)
                .addAction(new Notification.Action.Builder(null, "체험 종료", stop).build())
                .setOngoing(true)
                .build();
    }

    @Override
    public void onDestroy() {
        beacon.stop();
        clearOverlay();
        unregisterReceiver(screenOff);
        if (demoWakeLock.isHeld()) demoWakeLock.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
