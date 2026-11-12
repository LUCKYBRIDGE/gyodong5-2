package com.knolquiz.traffic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class PixelAvatarView extends View {
    private static final int SKY = 0xffffe6bf;
    private static final int SKY_LIGHT = 0xfffff1d5;
    private static final int HORIZON = 0xffc9dbad;
    private static final int CURB = 0xffa1b58c;
    private static final int ROAD = 0xff718e80;
    private static final int STRIPES = 0xffdce6cc;
    private static final int OUTLINE = 0xff513423;
    private static final int TIGER = 0xffeea248;
    private static final int TIGER_LIGHT = 0xffffb85a;
    private static final int TIGER_SHADE = 0xffcb7431;
    private static final int CREAM = 0xffffebcf;
    private static final int CREAM_SHADE = 0xffe5c8a7;
    private static final int SEA = 0xff78b6bc;
    private static final int WAVE = 0xffc4e1d4;
    private static final int SUN = 0xfff1a156;
    private static final int BLUSH = 0xffdf8175;
    private static final int WHITE = 0xfffff4cb;
    private static final int DENIM = 0xff42667b;
    private static final int GOLD = 0xfff4c75c;
    private static final int MINT = 0xffbde778;
    private static final int GREEN = 0xff467b57;
    private static final int PINE_DARK = 0xff325942;

    private final Rewards.Title title;
    private final boolean reflector;
    private final boolean sticker;
    private final boolean bagTag;
    private final Paint paint = new Paint();

    PixelAvatarView(Context context, Rewards.Title title, Set<String> equipped) {
        super(context);
        this.title = title;
        reflector = equipped.contains(Rewards.Item.REFLECTOR.name());
        sticker = equipped.contains(Rewards.Item.STICKER.name());
        bagTag = equipped.contains(Rewards.Item.BAG_TAG.name());
        paint.setAntiAlias(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        setContentDescription(avatarDescription());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(SKY);
        int pixel = Math.max(1, Math.min(getWidth() / 48, getHeight() / 34));
        int columns = getWidth() / pixel / 2 + 2;
        int top = (getHeight() - 34 * pixel) / 2;
        canvas.save();
        canvas.translate(getWidth() / 2, top);
        canvas.scale(pixel, pixel);

        drawStreet(canvas, columns);
        canvas.restore();

        int growth = title == null ? 78 : switch (title) {
            case FIRST_STEP -> 86;
            case CROSSWALK_FRIEND -> 94;
            case SAFETY_GUARDIAN -> 100;
        };
        int characterPixel = Math.max(1, pixel * growth / 100);
        canvas.save();
        canvas.translate(getWidth() / 2, top + 31 * pixel - 31 * characterPixel);
        canvas.scale(characterPixel, characterPixel);
        drawTail(canvas);
        if (bagTag) drawBag(canvas);
        drawBody(canvas);
        drawHead(canvas);
        drawSafetyGear(canvas);
        drawEquipment(canvas);
        canvas.restore();
    }

    private void drawStreet(Canvas canvas, int edge) {
        block(canvas, SKY_LIGHT, -18, 5, 9, 1);
        block(canvas, SKY_LIGHT, -16, 4, 5, 1);

        block(canvas, SUN, 14, 12, 8, 5);
        block(canvas, SUN, 15, 10, 6, 8);
        block(canvas, SUN, 17, 9, 2, 9);
        block(canvas, GOLD, 15, 13, 6, 2);

        block(canvas, SEA, -edge, 17, edge * 2, 9);
        block(canvas, WAVE, 13, 18, 9, 1);
        block(canvas, WAVE, 17, 20, 8, 1);
        block(canvas, WAVE, 12, 22, 7, 1);
        block(canvas, WAVE, -20, 19, 8, 1);
        block(canvas, WAVE, -13, 22, 5, 1);
        block(canvas, HORIZON, -edge, 25, edge * 2, 5);
        block(canvas, CURB, -edge, 29, edge * 2, 1);
        block(canvas, ROAD, -edge, 30, edge * 2, 20);
        for (int left = -edge; left < edge; left += 7) {
            block(canvas, STRIPES, left, 31, 4, 2);
        }
        block(canvas, CURB, -7, 28, 14, 1);

        drawPine(canvas);
    }

    private void drawPine(Canvas canvas) {
        block(canvas, OUTLINE, -18, 14, 2, 13);
        block(canvas, GREEN, -20, 11, 6, 3);
        block(canvas, GREEN, -22, 13, 10, 2);
        block(canvas, GREEN, -19, 10, 4, 1);
        block(canvas, PINE_DARK, -21, 16, 10, 2);
        block(canvas, GREEN, -23, 18, 11, 2);
        block(canvas, GREEN, -22, 17, 8, 1);
        block(canvas, CURB, -22, 26, 10, 1);
    }

    private void drawTail(Canvas canvas) {
        block(canvas, OUTLINE, 5, 23, 11, 5);
        block(canvas, OUTLINE, 13, 20, 4, 7);
        block(canvas, OUTLINE, 14, 18, 4, 4);
        block(canvas, TIGER_SHADE, 6, 24, 9, 3);
        block(canvas, TIGER, 13, 21, 3, 4);
        block(canvas, CREAM, 15, 19, 2, 3);
        block(canvas, OUTLINE, 11, 24, 1, 3);
        block(canvas, OUTLINE, 14, 22, 2, 1);
    }

    private void drawBody(Canvas canvas) {
        block(canvas, OUTLINE, -8, 16, 16, 12);
        block(canvas, OUTLINE, -7, 27, 5, 4);
        block(canvas, OUTLINE, 2, 27, 5, 4);
        block(canvas, TIGER, -7, 17, 14, 10);
        block(canvas, TIGER_SHADE, -6, 26, 12, 1);
        block(canvas, TIGER, -6, 27, 3, 2);
        block(canvas, TIGER, 3, 27, 3, 2);
        block(canvas, CREAM, -6, 29, 3, 1);
        block(canvas, CREAM, 3, 29, 3, 1);
        block(canvas, OUTLINE, -6, 27, 3, 1);
        block(canvas, OUTLINE, 3, 28, 3, 1);
        block(canvas, CREAM_SHADE, -4, 20, 8, 6);
        block(canvas, CREAM, -3, 19, 6, 6);

        block(canvas, OUTLINE, -13, 12, 6, 7);
        block(canvas, OUTLINE, -11, 17, 5, 4);
        block(canvas, TIGER, -12, 13, 4, 5);
        block(canvas, TIGER, -10, 17, 3, 3);
        block(canvas, OUTLINE, -12, 15, 4, 1);
        block(canvas, OUTLINE, -10, 18, 3, 1);
        drawPaw(canvas, -16, 9);

        block(canvas, OUTLINE, 7, 16, 7, 6);
        block(canvas, TIGER, 8, 17, 5, 4);
        block(canvas, OUTLINE, 10, 17, 1, 4);
        drawPaw(canvas, 12, 15);
    }

    private void drawPaw(Canvas canvas, int x, int y) {
        block(canvas, OUTLINE, x + 1, y, 4, 6);
        block(canvas, OUTLINE, x, y + 1, 6, 4);
        block(canvas, CREAM, x + 1, y + 1, 4, 4);
        block(canvas, BLUSH, x + 2, y + 2, 2, 2);
    }

    private void drawHead(Canvas canvas) {
        block(canvas, OUTLINE, -9, 1, 5, 6);
        block(canvas, OUTLINE, 4, 1, 5, 6);
        block(canvas, OUTLINE, -8, 0, 3, 1);
        block(canvas, OUTLINE, 5, 0, 3, 1);
        block(canvas, TIGER_LIGHT, -8, 1, 3, 4);
        block(canvas, TIGER_LIGHT, 5, 1, 3, 4);
        block(canvas, TIGER_SHADE, -7, 2, 1, 3);
        block(canvas, TIGER_SHADE, 6, 2, 1, 3);

        block(canvas, OUTLINE, -8, 3, 16, 15);
        block(canvas, OUTLINE, -10, 5, 20, 11);
        block(canvas, OUTLINE, -11, 7, 22, 6);
        block(canvas, TIGER, -9, 5, 18, 10);
        block(canvas, TIGER, -10, 7, 20, 6);
        block(canvas, TIGER_LIGHT, -7, 4, 14, 2);
        block(canvas, TIGER_SHADE, -7, 15, 14, 2);
        block(canvas, OUTLINE, -2, 4, 5, 1);
        block(canvas, OUTLINE, 0, 4, 1, 2);
        block(canvas, OUTLINE, -10, 8, 2, 1);
        block(canvas, OUTLINE, -10, 11, 2, 1);
        block(canvas, OUTLINE, 8, 9, 2, 1);
        block(canvas, OUTLINE, 8, 12, 2, 1);

        block(canvas, CREAM_SHADE, -7, 7, 14, 8);
        block(canvas, CREAM, -6, 7, 12, 7);
        block(canvas, CREAM, -5, 6, 10, 9);
        block(canvas, CREAM, -4, 15, 8, 1);
        block(canvas, TIGER, 4, 7, 2, 5);
        block(canvas, TIGER, 3, 8, 3, 4);
        block(canvas, TIGER_LIGHT, 5, 8, 2, 4);
        block(canvas, OUTLINE, -4, 9, 1, 2);
        block(canvas, OUTLINE, 4, 10, 1, 2);
        block(canvas, OUTLINE, -1, 11, 2, 1);
        block(canvas, OUTLINE, -2, 12, 5, 1);
        block(canvas, OUTLINE, -1, 13, 3, 1);
        block(canvas, BLUSH, 0, 13, 1, 1);
        block(canvas, BLUSH, -6, 11, 1, 1);
        block(canvas, BLUSH, 6, 12, 1, 1);
    }

    private void drawSafetyGear(Canvas canvas) {
        if (title == null) return;
        int color = title == Rewards.Title.FIRST_STEP ? MINT : GREEN;
        block(canvas, color, -7, 18, 2, 7);
        block(canvas, color, 5, 18, 2, 7);
        block(canvas, WHITE, -7, 21, 2, 1);
        block(canvas, WHITE, 5, 21, 2, 1);
        if (title == Rewards.Title.FIRST_STEP) return;

        block(canvas, OUTLINE, -2, 19, 4, 4);
        block(canvas, GOLD, -1, 20, 2, 2);
        if (title == Rewards.Title.SAFETY_GUARDIAN) {
            block(canvas, MINT, -7, 23, 2, 2);
            block(canvas, MINT, 5, 23, 2, 2);
            block(canvas, GOLD, -4, 25, 8, 1);
            block(canvas, WHITE, -2, 25, 4, 1);
        }
    }

    private void drawBag(Canvas canvas) {
        block(canvas, OUTLINE, 5, 19, 7, 8);
        block(canvas, DENIM, 6, 20, 5, 6);
        block(canvas, MINT, 8, 22, 3, 2);
    }

    private void drawEquipment(Canvas canvas) {
        if (reflector) {
            block(canvas, OUTLINE, -14, 18, 5, 6);
            block(canvas, OUTLINE, -15, 20, 7, 2);
            block(canvas, GOLD, -13, 19, 3, 4);
            block(canvas, GOLD, -14, 20, 5, 2);
            block(canvas, WHITE, -12, 20, 1, 2);
        }
        if (sticker) {
            block(canvas, OUTLINE, -4, 23, 4, 3);
            block(canvas, WHITE, -3, 23, 2, 2);
            block(canvas, DENIM, -3, 24, 2, 1);
        }
        if (bagTag) {
            block(canvas, WHITE, 9, 23, 1, 2);
            block(canvas, TIGER_LIGHT, 8, 25, 3, 2);
        }
    }

    private void block(Canvas canvas, int color, int left, int top, int width, int height) {
        paint.setColor(color);
        canvas.drawRect(left, top, left + width, top + height, paint);
    }

    private String avatarDescription() {
        List<String> items = new ArrayList<>();
        if (reflector) items.add(Rewards.Item.REFLECTOR.label);
        if (sticker) items.add(Rewards.Item.STICKER.label);
        if (bagTag) items.add(Rewards.Item.BAG_TAG.label);
        String stage = title == null ? "기본" : title.label;
        if (items.isEmpty()) return stage + " 하슬랑 픽셀 아바타";
        return stage + " 하슬랑 픽셀 아바타, 착용 중인 아이템 " + String.join(", ", items);
    }
}
