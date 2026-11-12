const SKY = "#ffe6bf";
const SKY_LIGHT = "#fff1d5";
const HORIZON = "#c9dbad";
const CURB = "#a1b58c";
const ROAD = "#718e80";
const STRIPES = "#dce6cc";
const OUTLINE = "#513423";
const TIGER = "#eea248";
const TIGER_LIGHT = "#ffb85a";
const TIGER_SHADE = "#cb7431";
const CREAM = "#ffebcf";
const CREAM_SHADE = "#e5c8a7";
const SEA = "#78b6bc";
const WAVE = "#c4e1d4";
const SUN = "#f1a156";
const BLUSH = "#df8175";
const WHITE = "#fff4cb";
const DENIM = "#42667b";
const GOLD = "#f4c75c";
const MINT = "#bde778";
const GREEN = "#467b57";
const PINE_DARK = "#325942";
export function drawAvatar(canvas, level = 0, equipment = []) {
    const context = canvas.getContext("2d");
    const title = level || null;
    const reflector = equipment.includes("REFLECTOR");
    const sticker = equipment.includes("STICKER");
    const bagTag = equipment.includes("BAG_TAG");
    context.imageSmoothingEnabled = false;
    context.fillStyle = SKY;
    context.fillRect(0, 0, canvas.width, canvas.height);
    const pixel = Math.max(1, Math.floor(Math.min(canvas.width / 48, canvas.height / 34)));
    const top = Math.floor((canvas.height - 34 * pixel) / 2);
    context.save();
    context.translate(canvas.width / 2, top);
    context.scale(pixel, pixel);
    drawStreet(context, Math.ceil(canvas.width / pixel / 2) + 2);
    context.restore();
    const growth = [0.78, 0.86, 0.94, 1][level];
    const characterPixel = Math.max(1, Math.floor(pixel * growth));
    context.save();
    context.translate(canvas.width / 2, top + 31 * pixel - 31 * characterPixel);
    context.scale(characterPixel, characterPixel);
    drawTail(context);
    if (bagTag) drawBag(context);
    drawBody(context);
    drawHead(context);
    drawSafetyGear(context);
    drawEquipment(context);
    context.restore();
    function block(canvas, color, left, top, width, height) {
        canvas.fillStyle = color;
        canvas.fillRect(left, top, width, height);
    }
    function drawStreet(canvas, edge) {
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
        for (let left = -edge; left < edge; left += 7) {
            block(canvas, STRIPES, left, 31, 4, 2);
        }
        block(canvas, CURB, -7, 28, 14, 1);

        drawPine(canvas);
    }

    function drawPine(canvas) {
        block(canvas, OUTLINE, -18, 14, 2, 13);
        block(canvas, GREEN, -20, 11, 6, 3);
        block(canvas, GREEN, -22, 13, 10, 2);
        block(canvas, GREEN, -19, 10, 4, 1);
        block(canvas, PINE_DARK, -21, 16, 10, 2);
        block(canvas, GREEN, -23, 18, 11, 2);
        block(canvas, GREEN, -22, 17, 8, 1);
        block(canvas, CURB, -22, 26, 10, 1);
    }

    function drawTail(canvas) {
        block(canvas, OUTLINE, 5, 23, 11, 5);
        block(canvas, OUTLINE, 13, 20, 4, 7);
        block(canvas, OUTLINE, 14, 18, 4, 4);
        block(canvas, TIGER_SHADE, 6, 24, 9, 3);
        block(canvas, TIGER, 13, 21, 3, 4);
        block(canvas, CREAM, 15, 19, 2, 3);
        block(canvas, OUTLINE, 11, 24, 1, 3);
        block(canvas, OUTLINE, 14, 22, 2, 1);
    }

    function drawBody(canvas) {
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

    function drawPaw(canvas, x, y) {
        block(canvas, OUTLINE, x + 1, y, 4, 6);
        block(canvas, OUTLINE, x, y + 1, 6, 4);
        block(canvas, CREAM, x + 1, y + 1, 4, 4);
        block(canvas, BLUSH, x + 2, y + 2, 2, 2);
    }

    function drawHead(canvas) {
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

    function drawSafetyGear(canvas) {
        if (title == null) return;
        const color = title == 1 ? MINT : GREEN;
        block(canvas, color, -7, 18, 2, 7);
        block(canvas, color, 5, 18, 2, 7);
        block(canvas, WHITE, -7, 21, 2, 1);
        block(canvas, WHITE, 5, 21, 2, 1);
        if (title == 1) return;

        block(canvas, OUTLINE, -2, 19, 4, 4);
        block(canvas, GOLD, -1, 20, 2, 2);
        if (title == 3) {
            block(canvas, MINT, -7, 23, 2, 2);
            block(canvas, MINT, 5, 23, 2, 2);
            block(canvas, GOLD, -4, 25, 8, 1);
            block(canvas, WHITE, -2, 25, 4, 1);
        }
    }

    function drawBag(canvas) {
        block(canvas, OUTLINE, 5, 19, 7, 8);
        block(canvas, DENIM, 6, 20, 5, 6);
        block(canvas, MINT, 8, 22, 3, 2);
    }

    function drawEquipment(canvas) {
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


}
