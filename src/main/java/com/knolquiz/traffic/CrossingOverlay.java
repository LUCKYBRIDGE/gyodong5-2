package com.knolquiz.traffic;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

final class CrossingOverlay {
    private final Context context;
    private final WindowManager windows;
    private View view;

    CrossingOverlay(Context context) {
        this.context = windowContext(context);
        this.windows = this.context.getSystemService(WindowManager.class);
    }

    boolean showWarning(Runnable onClose) {
        return show(true, "횡단보도 앞입니다.\n주변을 봐주세요.", "", onClose);
    }

    void showResult(boolean earned, int count, Runnable onClose) {
        String title = earned ? "+" + DailyPoints.POINTS_PER_REWARD + " 포인트 적립" : "오늘 적립 한도에 도달했어요";
        show(false, title, "오늘 " + count + " / " + DailyPoints.DAILY_LIMIT + "회 적립", onClose);
    }

    void showDismissal() {
        remove();
        TextView message = label(context.getString(R.string.warning_dismissed), 16,
                context.getColor(R.color.brand_white));
        message.setPadding(dp(18), dp(12), dp(18), dp(12));
        message.setMaxWidth(dp(300));
        message.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        GradientDrawable background = new GradientDrawable();
        background.setColor(context.getColor(R.color.brand_overlay));
        background.setCornerRadius(dp(16));
        message.setBackground(background);
        WindowManager.LayoutParams layout = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        layout.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        layout.y = dp(76);
        layout.alpha = 0.8f;
        layout.setTitle("경고 닫기 안내");
        attach(message, layout);
    }

    private boolean show(boolean warning, String message, String detail, Runnable onClose) {
        remove();

        ScrollView panel = new ScrollView(context);
        panel.setFillViewport(true);
        panel.setBackgroundColor(context.getColor(warning ? R.color.brand_overlay : R.color.brand_overlay_result));
        FrameLayout center = new FrameLayout(context);
        center.setPadding(dp(24), dp(16), dp(24), dp(16));
        panel.addView(center);

        LinearLayout contents = new LinearLayout(context);
        contents.setOrientation(LinearLayout.VERTICAL);
        contents.setGravity(Gravity.CENTER);
        center.addView(contents, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));

        if (warning) {
            ImageView icon = new ImageView(context);
            icon.setImageResource(R.drawable.ic_signal_alert);
            icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            contents.addView(icon, new LinearLayout.LayoutParams(dp(64), dp(64)));
        }
        int headlineColor = context.getColor(warning ? R.color.brand_warning : R.color.brand_reward_text);
        TextView headline = label(message, warning ? 30 : 26, headlineColor);
        headline.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        headline.setPadding(0, dp(16), 0, dp(12));
        contents.addView(headline);
        int white = context.getColor(R.color.brand_white);
        if (!detail.isEmpty()) contents.addView(label(detail, 16, white));

        Button close = new Button(context);
        close.setText(warning ? "경고 닫기" : "닫기");
        close.setTextColor(white);
        GradientDrawable outline = new GradientDrawable();
        outline.setColor(context.getColor(R.color.brand_overlay));
        outline.setStroke(dp(1), context.getColor(R.color.brand_overlay_border));
        outline.setCornerRadius(dp(24));
        close.setBackground(outline);
        close.setPadding(dp(20), 0, dp(20), 0);
        close.setOnClickListener(ignored -> onClose.run());
        LinearLayout.LayoutParams closeLayout = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dp(48));
        closeLayout.topMargin = dp(20);
        contents.addView(close, closeLayout);

        WindowManager.LayoutParams layout = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                warning ? WindowManager.LayoutParams.MATCH_PARENT : WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        layout.gravity = Gravity.CENTER;
        layout.setTitle(warning ? "횡단보도 경고" : "포인트 결과");
        return attach(panel, layout);
    }

    private boolean attach(View content, WindowManager.LayoutParams layout) {
        try {
            windows.addView(content, layout);
            view = content;
            return true;
        } catch (WindowManager.BadTokenException | SecurityException ignored) {
            return false;
        }
    }

    boolean isVisible() {
        return view != null && view.isAttachedToWindow() && view.isShown()
                && view.getWindowVisibility() == View.VISIBLE;
    }

    void remove() {
        if (view != null) {
            windows.removeViewImmediate(view);
            view = null;
        }
    }

    private TextView label(String text, int size, int color) {
        TextView label = new TextView(context);
        label.setText(text);
        label.setTextSize(size);
        label.setTextColor(color);
        label.setGravity(Gravity.CENTER);
        return label;
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private static Context windowContext(Context context) {
        if (Build.VERSION.SDK_INT < 30) return context;
        Display display = context.getSystemService(DisplayManager.class).getDisplay(Display.DEFAULT_DISPLAY);
        if (display == null) return context;
        return context.createDisplayContext(display)
                .createWindowContext(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, null);
    }
}
