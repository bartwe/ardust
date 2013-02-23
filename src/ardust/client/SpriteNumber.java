package ardust.client;

public class SpriteNumber {
    private static final int textureX = 128;
    private static final int textureY = 40;
    private static final int digitWidth = 8;
    private static final int digitHeight = 12;

    //draws the number... but screenX is the RIGHT side of the number... didn't want to waste time doing it other way
    public static void drawNumber(int number, int screenX, int screenY, Painter p) {
        int widthOffset = 0;
        while (true) {
            int currentDigit = number % 10;
            number /= 10;

            int textX = textureX + (currentDigit * digitWidth) % textureX;
            int textY = textureY + ((currentDigit * digitWidth) / textureX) * digitHeight;
            p.draw(screenX - widthOffset, screenY, textX, textY, digitWidth, digitHeight, false);
            widthOffset += digitWidth;

            if (number <= 0) return;
        }
    }
}
