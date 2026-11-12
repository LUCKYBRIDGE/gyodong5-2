package com.knolquiz.traffic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class MainActivity extends Activity {
    private enum Page {
        HOME("홈"), SHOP("상점"), TITLES("칭호"), AVATAR("하슬랑"), GUIDE("안내");

        final String label;
        Page(String label) { this.label = label; }
    }

    private static final int INK = R.color.brand_ink;
    private static final int MUTED = R.color.brand_muted;
    private static final int PAPER = R.color.brand_paper;
    private static final int WHITE = R.color.brand_white;
    private static final int MINT = R.color.brand_mint;

    private DashboardStyle ui;
    private SharedPreferences points;
    private LinearLayout content;
    private LinearLayout navigation;
    private ScrollView scroll;
    private Page page = Page.HOME;

    private final Runnable refresh = this::render;
    private final SharedPreferences.OnSharedPreferenceChangeListener pointsListener =
            (prefs, key) -> {
                scroll.removeCallbacks(refresh);
                scroll.post(refresh);
            };

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        ui = new DashboardStyle(this);
        points = getSharedPreferences("traffic", MODE_PRIVATE);
        if (saved != null) page = Page.values()[saved.getInt("page")];
        buildShell();
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putInt("page", page.ordinal());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_MAIN.equals(intent.getAction()) && intent.hasCategory(Intent.CATEGORY_LAUNCHER)) {
            openPage(Page.HOME);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        points.registerOnSharedPreferenceChangeListener(pointsListener);
        render();
    }

    @Override
    protected void onPause() {
        points.unregisterOnSharedPreferenceChangeListener(pointsListener);
        scroll.removeCallbacks(refresh);
        super.onPause();
    }

    private void buildShell() {
        LinearLayout root = ui.column();
        root.setBackgroundColor(ui.color(PAPER));
        root.addView(header());
        scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        content = ui.column();
        ui.padding(content, 20, 12);
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        navigation = ui.row();
        ui.padding(navigation, 12, 6);
        root.addView(navigation);
        setContentView(root);
        configureInsets(root);
    }

    private View header() {
        LinearLayout header = ui.row();
        ui.padding(header, 20, 12);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_crossing);
        logo.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(ui.dp(38), ui.dp(38));
        size.rightMargin = ui.dp(11);
        header.addView(logo, size);
        header.addView(ui.text(getText(R.string.app_name), 20, INK, true));
        return header;
    }

    private void render() {
        DailyPoints earned = new DailyPoints(points.getString("day", ""),
                points.getInt("count", 0), points.getInt("total", 0));
        int balance = Rewards.balance(earned.total(), points.getInt("spent", 0));
        content.removeAllViews();
        switch (page) {
            case HOME -> home(earned, balance);
            case SHOP -> shop(earned.total(), balance);
            case TITLES -> titles(earned.total());
            case AVATAR -> avatar(earned.total());
            case GUIDE -> BeaconGuide.render(content, ui);
        }
        renderNavigation();
    }

    private void renderNavigation() {
        navigation.removeAllViews();
        for (Page option : Page.values()) {
            Button button = ui.button(option.label, page == option);
            ui.padding(button, 2, 8);
            button.setSelected(page == option);
            if (page != option) button.setBackground(ui.surface(PAPER, 14, 0));
            LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(0, -2, 1);
            size.setMargins(ui.dp(2), 0, ui.dp(2), 0);
            navigation.addView(button, size);
            button.setOnClickListener(view -> openPage(option));
        }
    }

    private void openPage(Page option) {
        page = option;
        render();
        scroll.scrollTo(0, 0);
    }

    private void home(DailyPoints earned, int balance) {
        LinearLayout hero = ui.column();
        hero.setBackground(ui.surface(INK, 26, 0));
        ui.padding(hero, 22, 21);
        Rewards.Title current = Rewards.currentTitle(earned.total());
        LinearLayout headline = ui.row();
        LinearLayout wallet = rowText(headline);
        wallet.addView(ui.text("사용 가능한 포인트", 14, MINT, true));
        wallet.addView(ui.text(formatPoints(balance), 42, WHITE, true));
        LinearLayout character = ui.column();
        character.setGravity(Gravity.CENTER);
        PixelAvatarView portrait = new PixelAvatarView(this, current, equippedItems());
        portrait.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        character.addView(portrait, new LinearLayout.LayoutParams(ui.dp(102), ui.dp(88)));
        ui.space(character, 3);
        character.addView(ui.text("하슬랑 키우기", 13, MINT, true));
        character.setContentDescription(portrait.getContentDescription() + ", 하슬랑 키우기 열기");
        character.setOnClickListener(view -> openPage(Page.AVATAR));
        character.setFocusable(true);
        headline.addView(character);
        hero.addView(headline);
        ui.space(hero, 14);
        LinearLayout metrics = ui.row();
        metric(metrics, "누적 적립", formatPoints(earned.total()));
        metric(metrics, "오늘 적립", getString(R.string.today_count,
                earned.countFor(LocalDate.now().toString()), DailyPoints.DAILY_LIMIT));
        hero.addView(metrics);
        content.addView(hero);
        ui.space(content, 16);

        shortcut("교통안전 칭호", current == null ? "10점부터 첫 칭호가 열려요." : current.label,
                "칭호 보기", Page.TITLES);
        ui.space(content, 10);
        shortcut("포인트 상점", "교환권 예시와 하슬랑 꾸미기 아이템", "상점 보기", Page.SHOP);
        ui.space(content, 16);

        LinearLayout trial = card();
        trial.addView(ui.text("가상 비콘 체험", 17, INK, true));
        ui.space(trial, 4);
        trial.addView(ui.text("시작 후 다른 앱을 열거나 화면을 꺼 보세요. 가까이에서 화면을 끄거나 경고를 "
                + RewardSession.HOLD_MILLIS / 1000 + "초 유지하면 "
                + formatPoints(DailyPoints.POINTS_PER_REWARD) + "이 쌓여요.", 13, MUTED, false));
        ui.space(trial, 10);
        boolean allowed = Settings.canDrawOverlays(this);
        Button action = ui.button(allowed ? "가상 접근 시작" : "다른 앱 위에 표시 허용", true);
        action.setOnClickListener(view -> beginOrRequestPermission());
        trial.addView(action);
        if (!allowed) {
            ui.space(trial, 6);
            trial.addView(ui.text(getString(R.string.overlay_permission_hint, getString(R.string.app_name)),
                    13, MUTED, false));
        }
        content.addView(trial);
    }

    private void metric(LinearLayout row, String label, String value) {
        LinearLayout metric = ui.column();
        metric.addView(ui.text(label, 13, MINT, false));
        metric.addView(ui.text(value, 20, WHITE, true));
        row.addView(metric, new LinearLayout.LayoutParams(0, -2, 1));
    }

    private void shortcut(String title, String description, String action, Page destination) {
        LinearLayout card = rowCard();
        LinearLayout words = rowText(card);
        words.addView(ui.text(title, 16, INK, true));
        words.addView(ui.text(description, 13, MUTED, false));
        Button button = ui.button(action, false);
        button.setOnClickListener(view -> openPage(destination));
        card.addView(button);
        content.addView(card);
    }

    private void shop(int total, int balance) {
        section("포인트 상점", "사용 가능한 포인트로 교환해요.");
        LinearLayout funds = card();
        funds.setBackground(ui.surface(MINT, 19, 0));
        funds.addView(ui.text("사용 가능한 포인트", 13, INK, false));
        funds.addView(ui.text(formatPoints(balance), 28, INK, true));
        content.addView(funds);
        ui.space(content, 14);

        LinearLayout voucher = card();
        voucher.addView(ui.text("모바일 교환권 · 상품 예시", 13, MUTED, true));
        ui.space(voucher, 4);
        voucher.addView(ui.text("편의점 기프티콘", 18, INK, true));
        voucher.addView(ui.text("실제 쿠폰 발급은 발송 서비스 연동 후 이용할 수 있어요.", 13, MUTED, false));
        content.addView(voucher);
        ui.space(content, 16);
        content.addView(ui.text("하슬랑 꾸미기 아이템", 17, INK, true));
        ui.space(content, 7);

        Set<String> owned = ownedItems();
        for (Rewards.Item item : Rewards.Item.values()) {
            LinearLayout card = rowCard();
            badge(card, item.symbol, true);
            LinearLayout words = rowText(card);
            words.addView(ui.text(item.label, 16, INK, true));
            words.addView(ui.text(item.description, 13, MUTED, false));
            ui.space(words, 4);
            words.addView(ui.text(formatPoints(item.price), 13, INK, true));

            boolean hasItem = owned.contains(item.name());
            Button button = ui.button(hasItem ? "보유 중" : balance < item.price ? "포인트 부족" : "교환", false);
            boolean canRedeem = Rewards.canRedeem(item, total, points.getInt("spent", 0), owned);
            button.setEnabled(canRedeem);
            if (!canRedeem) button.setBackground(ui.surface(PAPER, 14, 0));
            button.setOnClickListener(view -> confirmRedemption(item));
            card.addView(button);
            content.addView(card);
            ui.space(content, 10);
        }
    }

    private void confirmRedemption(Rewards.Item item) {
        new AlertDialog.Builder(this)
                .setTitle(item.label)
                .setMessage(formatPoints(item.price) + "을 사용해 이 가상 아이템을 모을까요?")
                .setNegativeButton("취소", null)
                .setPositiveButton("교환", (dialog, button) -> redeem(item))
                .show();
    }

    private void redeem(Rewards.Item item) {
        int spent = points.getInt("spent", 0);
        Set<String> owned = ownedItems();
        if (!Rewards.canRedeem(item, points.getInt("total", 0), spent, owned)) return;
        Set<String> updated = new HashSet<>(owned);
        updated.add(item.name());
        points.edit().putInt("spent", spent + item.price).putStringSet("owned", updated).apply();
        Toast.makeText(this, item.label + " 아이템을 모았어요.", Toast.LENGTH_SHORT).show();
    }

    private Set<String> ownedItems() {
        return points.getStringSet("owned", Collections.emptySet());
    }

    private Set<String> equippedItems() {
        Set<String> equipped = new HashSet<>(points.getStringSet("equipped", Collections.emptySet()));
        equipped.retainAll(ownedItems());
        return equipped;
    }

    private void avatar(int total) {
        section("하슬랑 키우기", "횡단보도 알림을 잘 이용할 때마다 하슬랑이 자라요.");
        Rewards.Title current = Rewards.currentTitle(total);
        LinearLayout stage = card();
        stage.addView(new PixelAvatarView(this, current, equippedItems()),
                new LinearLayout.LayoutParams(-1, ui.dp(212)));
        ui.space(stage, 10);
        stage.addView(ui.text(current == null ? "첫 안전 체험을 기다리고 있어요" : current.label, 17, INK, true));
        addGrowthProgress(stage, total);
        content.addView(stage);
        ui.space(content, 16);
        content.addView(ui.text("하슬랑 꾸미기", 18, INK, true));
        ui.space(content, 8);
        Set<String> equipped = equippedItems();
        Set<String> owned = ownedItems();
        for (Rewards.Item item : Rewards.Item.values()) {
            boolean worn = equipped.contains(item.name());
            boolean available = owned.contains(item.name());
            LinearLayout card = rowCard();
            badge(card, item.symbol, worn);
            LinearLayout words = rowText(card);
            words.addView(ui.text(item.label, 16, INK, true));
            words.addView(ui.text(worn ? "착용 중" : available ? "보유 중" : "가상 상점에서 교환할 수 있어요.",
                    13, MUTED, false));
            Button action = ui.button(available ? worn ? "벗기" : "착용" : "상점", false);
            action.setOnClickListener(view -> {
                if (available) toggleEquipment(item);
                else openPage(Page.SHOP);
            });
            card.addView(action);
            content.addView(card);
            ui.space(content, 8);
        }
    }

    private void addGrowthProgress(LinearLayout stage, int total) {
        int step = DailyPoints.POINTS_PER_REWARD;
        Rewards.Title next = null;
        for (Rewards.Title title : Rewards.Title.values()) {
            if (title.threshold > total) {
                next = title;
                break;
            }
        }
        ui.space(stage, 6);
        if (next == null) {
            stage.addView(ui.text("가장 높은 칭호에 도달했어요.", 13, MUTED, false));
            return;
        }
        stage.addView(ui.text("다음 칭호 · " + next.label, 13, MUTED, false));
        ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(next.threshold);
        progress.setProgress(total);
        progress.setContentDescription("하슬랑 성장 진행률");
        stage.addView(progress);
        stage.addView(ui.text("완료한 알림 " + total / step + " / " + next.threshold / step + "회", 13, MUTED, false));
    }

    private void toggleEquipment(Rewards.Item item) {
        if (!ownedItems().contains(item.name())) return;
        Set<String> updated = equippedItems();
        if (updated.contains(item.name())) updated.remove(item.name());
        else updated.add(item.name());
        points.edit().putStringSet("equipped", updated).apply();
    }

    private void titles(int total) {
        section("교통안전 칭호", "횡단보도 알림 체험으로 모은 누적 점수로 열려요.");
        Rewards.Title current = Rewards.currentTitle(total);
        for (Rewards.Title title : Rewards.Title.values()) {
            boolean unlocked = total >= title.threshold;
            LinearLayout card = card();
            LinearLayout top = ui.row();
            badge(top, title.symbol, unlocked);
            LinearLayout words = rowText(top);
            words.addView(ui.text(title.label, 17, INK, true));
            words.addView(ui.text(title.description, 13, MUTED, false));
            String status = title == current ? "현재 칭호" : unlocked ? "획득" : "잠김";
            top.addView(ui.text(status, 12, unlocked ? INK : MUTED, true));
            card.addView(top);
            ui.space(card, 12);
            ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            progress.setMax(title.threshold);
            progress.setProgress(Math.min(total, title.threshold));
            progress.setContentDescription("누적 적립 진행률");
            card.addView(progress);
            card.addView(ui.text("누적 " + formatPoints(Math.min(total, title.threshold))
                    + " / " + formatPoints(title.threshold), 13, MUTED, false));
            content.addView(card);
            ui.space(content, 10);
        }
    }

    private void section(String title, String subtitle) {
        content.addView(ui.text(title, 30, INK, true));
        ui.space(content, 4);
        content.addView(ui.text(subtitle, 14, MUTED, false));
        ui.space(content, 17);
    }

    private LinearLayout card() {
        return styledCard(ui.column());
    }

    private LinearLayout rowCard() {
        return styledCard(ui.row());
    }

    private LinearLayout styledCard(LinearLayout card) {
        ui.padding(card, 15, 13);
        card.setBackground(ui.surface(WHITE, 19, R.color.brand_line));
        return card;
    }

    private LinearLayout rowText(LinearLayout row) {
        LinearLayout words = ui.column();
        LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(0, -2, 1);
        size.rightMargin = ui.dp(8);
        row.addView(words, size);
        return words;
    }

    private void badge(LinearLayout row, String symbol, boolean active) {
        TextView badge = ui.text(symbol, 19, INK, true);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(ui.surface(active ? MINT : PAPER, 14, 0));
        badge.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(ui.dp(44), ui.dp(44));
        size.rightMargin = ui.dp(10);
        row.addView(badge, size);
    }

    private String formatPoints(int value) {
        return getString(R.string.total_points, value);
    }

    private void beginOrRequestPermission() {
        if (!Settings.canDrawOverlays(this)) {
            try {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())));
            } catch (android.content.ActivityNotFoundException unavailable) {
                Toast.makeText(this, "설정에서 다른 앱 위에 표시 권한을 찾아 주세요.", Toast.LENGTH_LONG).show();
            }
            return;
        }

        try {
            startForegroundService(new Intent(this, CrossingService.class));
            Toast.makeText(this, "7초 뒤 경고창이 떠요. 다른 앱을 열어 보세요.", Toast.LENGTH_SHORT).show();
        } catch (IllegalStateException | SecurityException unavailable) {
            Toast.makeText(this, "체험을 시작할 수 없어요. 권한 설정을 확인해 주세요.", Toast.LENGTH_LONG).show();
        }
    }

    private void configureInsets(View root) {
        if (Build.VERSION.SDK_INT < 30) return;
        getWindow().setDecorFitsSystemWindows(false);
        WindowInsetsController controller = getWindow().getInsetsController();
        if (controller != null) {
            int lightBars = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
            controller.setSystemBarsAppearance(lightBars, lightBars);
        }
        root.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            android.graphics.Insets insets = windowInsets.getInsets(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsets.CONSUMED;
        });
        root.requestApplyInsets();
    }
}
