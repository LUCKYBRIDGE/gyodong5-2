package com.knolquiz.traffic;

import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

final class BeaconGuide {
    private static final int INK = R.color.brand_ink;
    private static final int MUTED = R.color.brand_muted;

    private BeaconGuide() {}

    static void render(LinearLayout content, DashboardStyle ui) {
        content.addView(ui.text("사용 방법과 기술 설명", 28, INK, true));
        ui.space(content, 12);
        content.addView(demoCard(ui));
        ui.space(content, 18);
        content.addView(ui.text("실제 비콘 연동 계획", 20, INK, true));
        ui.space(content, 8);
        addStep(content, ui, 1, "횡단보도 앞에 비콘 설치",
                "차도에 들어가기 전인 인도 대기 구역에 소형 블루투스 송신기(BLE 비콘)를 설치해요.");
        addStep(content, ui, 2, "휴대폰이 비콘 신호 감지",
                "앱이 등록된 비콘의 식별 신호를 찾아요. 페어링 없이 신호를 받고 세기를 기준으로 가까워졌는지 판단해요.");
        addStep(content, ui, 3, "다른 앱 위에 경고 표시",
                "안드로이드에서 ‘다른 앱 위에 표시’를 허용하면 화면 위에 “횡단보도 앞입니다. 주변을 봐주세요.”가 떠요.");
        addStep(content, ui, 4, "화면을 끄거나 경고창을 유지",
                "감지 범위 안에서 화면을 꺼 두거나 경고를 닫지 않은 상태로 "
                        + RewardSession.HOLD_MILLIS / 1_000
                        + "초가 지나면 " + DailyPoints.POINTS_PER_REWARD + "점씩 하루 최대 "
                        + DailyPoints.DAILY_LIMIT + "번 적립해요. 적립한 기록은 하슬랑의 성장에도 반영돼요.");
        content.addView(ui.text("비콘 신호 세기(RSSI)는 장애물과 휴대폰 방향에 따라 달라져요. 설치 후 양쪽 인도에서 감지 범위와 경고 시점을 확인하고 조정해요.",
                13, MUTED, false));
        ui.space(content, 8);
        content.addView(ui.text("픽셀 아바타는 강릉의 디지털 관광 캐릭터 하슬랑을 바탕으로 만든 제안 시안입니다.",
                12, MUTED, false));
    }

    private static LinearLayout demoCard(DashboardStyle ui) {
        LinearLayout card = ui.column();
        card.setBackground(ui.surface(R.color.brand_mint, 18, 0));
        ui.padding(card, 14, 12);
        card.addView(ui.text("지금 해보기 · 가상 비콘 체험", 17, INK, true));
        ui.space(card, 4);
        card.addView(ui.text("‘다른 앱 위에 표시’를 허용하고 ‘가상 접근 시작’을 눌러요. 7초 안에 화면을 끄거나 다른 앱을 열어 보세요.",
                13, INK, false));
        return card;
    }

    private static void addStep(LinearLayout content, DashboardStyle ui, int number,
                                String heading, String description) {
        LinearLayout card = ui.row();
        card.setGravity(Gravity.TOP);
        card.setBackground(ui.surface(R.color.brand_white, 16, R.color.brand_line));
        ui.padding(card, 12, 10);

        TextView marker = ui.text(Integer.toString(number), 14, INK, true);
        marker.setGravity(Gravity.CENTER);
        marker.setBackground(ui.surface(R.color.brand_mint, 9, 0));
        marker.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams markerSize = new LinearLayout.LayoutParams(ui.dp(28), ui.dp(28));
        markerSize.rightMargin = ui.dp(10);
        card.addView(marker, markerSize);

        LinearLayout copy = ui.column();
        copy.addView(ui.text(heading, 15, INK, true));
        copy.addView(ui.text(description, 13, MUTED, false));
        card.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        content.addView(card);
        ui.space(content, 7);
    }
}
