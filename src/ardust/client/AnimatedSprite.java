package ardust.client;

import java.awt.*;

public class AnimatedSprite {

    int currentFrame, accumulator;

    public AnimatedSprite() {

    }

    public void animate(int startingFrame, int framesInAnimation, int ticksPerFrame) {
        accumulator = (accumulator + 1) % (ticksPerFrame * framesInAnimation);
        currentFrame = startingFrame + accumulator / ticksPerFrame;
    }

    public void draw(Painter p, int x, int y, boolean flip) {

        Rectangle tileSheetRect = new Rectangle();
        p.getSourceRectFromTileSheetIndex(currentFrame, tileSheetRect);

        p.draw(x, y, tileSheetRect.x, tileSheetRect.y, tileSheetRect.width, tileSheetRect.height, flip);
    }

}
