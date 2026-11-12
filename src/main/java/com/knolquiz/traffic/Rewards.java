package com.knolquiz.traffic;

import java.util.Set;

final class Rewards {
    private Rewards() {}

    enum Item {
        REFLECTOR("반짝이 반사판", "빛을 반사하는 가상 안전 아이템", 10, "✦"),
        STICKER("횡단보도 스티커", "가상 안전 스티커", 30, "▤"),
        BAG_TAG("안전 가방고리", "가방에 다는 가상 안전 아이템", 60, "◇");

        final String label;
        final String description;
        final int price;
        final String symbol;

        Item(String label, String description, int price, String symbol) {
            this.label = label;
            this.description = description;
            this.price = price;
            this.symbol = symbol;
        }
    }

    enum Title {
        FIRST_STEP("안전 첫걸음", "안전 알림 체험을 시작했어요.", 10, "✦"),
        CROSSWALK_FRIEND("횡단보도 친구", "안전 알림을 꾸준히 체험했어요.", 30, "▤"),
        SAFETY_GUARDIAN("교통안전 지킴이", "안전 알림 체험 목표를 달성했어요.", 70, "◆");

        final String label;
        final String description;
        final int threshold;
        final String symbol;

        Title(String label, String description, int threshold, String symbol) {
            this.label = label;
            this.description = description;
            this.threshold = threshold;
            this.symbol = symbol;
        }
    }

    static int balance(int total, int spent) {
        return Math.max(0, Math.max(0, total) - Math.max(0, spent));
    }

    static boolean canRedeem(Item item, int total, int spent, Set<String> owned) {
        return !owned.contains(item.name()) && balance(total, spent) >= item.price;
    }

    static Title currentTitle(int total) {
        Title current = null;
        for (Title title : Title.values()) {
            if (total >= title.threshold) current = title;
        }
        return current;
    }
}
