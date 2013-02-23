package ardust.client;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: Eric
 * Date: 2/23/13
 * Time: 12:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class SolitaryAnimatedSprite extends AnimatedSprite {
    public int xLocation, yLocation;
    int startingFrame;
    int framesInAnimation;
    int ticksPerFrame;

    public SolitaryAnimatedSprite(int startingFrame, int framesInAnimation, int ticksPerFrame, int xLocation, int yLocation)
    {
        this.startingFrame = startingFrame;
        this.framesInAnimation = framesInAnimation;
        this.ticksPerFrame = ticksPerFrame;
        this.xLocation = xLocation;
        this.yLocation = yLocation;
    }

    public boolean animate() {
        int tmp = accumulator;
        accumulator = (accumulator + 1) % (ticksPerFrame * framesInAnimation);

        currentFrame = startingFrame + (accumulator / ticksPerFrame);
        return accumulator < tmp;
    }

    Point point = new Point();
    Rectangle tileSheetRect = new Rectangle();

    public void draw(Painter p, Point viewportLocation)
    {
        p.getSourceRectFromTileSheetIndex(currentFrame, tileSheetRect);

        World.globalNonTileCoordToScreenCoord(xLocation, yLocation, point, viewportLocation);

        p.draw(point.x, point.y, tileSheetRect.x, tileSheetRect.y, tileSheetRect.width, tileSheetRect.height, false);
    }
}
