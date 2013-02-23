package ardust.client;

import java.awt.*;

public class AnimatedSprite {

    int currentFrame, accumulator;

    public AnimatedSprite() {

    }

    public boolean animate(int startingFrame, int framesInAnimation, int ticksPerFrame) {
        int tmp = accumulator;
        accumulator = (accumulator + 1) % (ticksPerFrame * framesInAnimation);

        currentFrame = startingFrame + (accumulator / ticksPerFrame);
        return accumulator < tmp;
    }

    public void draw(Painter p, int x, int y, boolean flip) {

        Rectangle tileSheetRect = new Rectangle();
        p.getSourceRectFromTileSheetIndex(currentFrame, tileSheetRect);

        p.draw(x, y, tileSheetRect.x, tileSheetRect.y, tileSheetRect.width, tileSheetRect.height, flip);
    }

}
