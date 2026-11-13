package com.knolquiz.traffic;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

final class DashboardStyle {
    private final Context context;

    DashboardStyle(Context context) {
        this.context = context;
    }

    int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    int color(int resource) {
        return context.getColor(resource);
    }

    LinearLayout column() {
        LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);
        return view;
    }

    LinearLayout row() {
        LinearLayout view = new LinearLayout(context);
        view.setGravity(Gravity.CENTER_VERTICAL);
        return view;
    }

    TextView text(CharSequence value, int size, int colorResource, boolean bold) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color(colorResource));
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    ImageView brandIcon(int size) {
        ImageView icon = new ImageView(context);
        icon.setImageResource(R.drawable.ic_signal_alert);
        icon.setBackground(surface(R.color.brand_mint, Math.round(size * 0.27f), 0));
        icon.setClipToOutline(true);
        return icon;
    }

    Button button(CharSequence value, boolean primary) {
        Button view = new Button(context);
        view.setText(value);
        view.setTextSize(14);
        view.setAllCaps(false);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setTextColor(color(primary ? R.color.brand_white : R.color.brand_ink));
        view.setMinHeight(dp(48));
        view.setMinimumHeight(dp(48));
        view.setMinWidth(0);
        view.setMinimumWidth(0);
        view.setPadding(dp(12), dp(8), dp(12), dp(8));
        view.setStateListAnimator(null);
        view.setBackground(new RippleDrawable(ColorStateList.valueOf(color(R.color.brand_ripple)),
                surface(primary ? R.color.brand_button : R.color.brand_mint, 14, 0), null));
        return view;
    }

    GradientDrawable surface(int fill, int radius, int stroke) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(color(fill));
        background.setCornerRadius(dp(radius));
        if (stroke != 0) background.setStroke(dp(1), color(stroke));
        return background;
    }

    void space(LinearLayout parent, int size) {
        parent.addView(new View(context), new LinearLayout.LayoutParams(1, dp(size)));
    }

    void padding(View view, int horizontal, int vertical) {
        view.setPadding(dp(horizontal), dp(vertical), dp(horizontal), dp(vertical));
    }
}
