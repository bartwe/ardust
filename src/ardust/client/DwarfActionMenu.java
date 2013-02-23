package ardust.client;


import ardust.shared.Constants;
import ardust.shared.Point2;

import java.awt.*;
import java.util.ArrayList;

public class DwarfActionMenu {
    private static final int WALK = 0;
    private static final int HALT = 1;
    private static final int MINE = 2;
    private static final int USE = 3;

    Point2 location = new Point2();
    ArrayList<Rectangle> buttons;
    boolean craftingMenu;

    public DwarfActionMenu(Point2 location, boolean craftingMenu) {
        this.location.set(location);
        this.craftingMenu = craftingMenu;

        buttons = new ArrayList<Rectangle>();
        buttons.add(new Rectangle(32, 12, 32, 32)); //0 -- WALK
        buttons.add(new Rectangle(0, 46, 32, 32));  //1 -- HALT
        buttons.add(new Rectangle(64, 46, 32, 32)); //2  -- MINE (Yes it's ugly but we're short on time)
        if (!craftingMenu) buttons.add(new Rectangle(32, 80, 32, 32)); // 3 -- USE
    }

    public GameCore.UserInputState isButtonHere(int x, int y, Point viewportLocation) {
        Point p = getDrawPoint(viewportLocation);
        int localX = (x - p.x * Constants.PIXEL_SCALE) / Constants.PIXEL_SCALE;
        int localY = (y - p.y * Constants.PIXEL_SCALE) / Constants.PIXEL_SCALE;

        if (buttons.get(WALK).contains(localX, localY))
            return craftingMenu ? GameCore.UserInputState.ATTEMPTING_ARMOR_PURCHASE : GameCore.UserInputState.WALK;
        if (buttons.get(HALT).contains(localX, localY))
            return craftingMenu ? GameCore.UserInputState.ATTEMPTING_SWORD_PURCHASE : GameCore.UserInputState.HALT;
        if (buttons.get(MINE).contains(localX, localY))
            return craftingMenu ? GameCore.UserInputState.ATTEMPTING_GOLD_SWORD_PURCHASE : GameCore.UserInputState.MINE;
        if (!craftingMenu && buttons.get(USE).contains(localX, localY)) return GameCore.UserInputState.BUILD;

        return GameCore.UserInputState.NONE;
    }

    Point tempPoint = new Point();

    public Point getDrawPoint(Point viewportLocation) {
        World.globalTileToLocalCoord(location.x, location.y, viewportLocation, tempPoint);
        return new Point(tempPoint.x - 32, tempPoint.y - Constants.TILE_DRAW_HEIGHT - 24);
    }

    public void draw(Painter p, Point viewportLocation) {
        p.start();
        Point drawPoint = getDrawPoint(viewportLocation);

        p.draw(drawPoint.x, drawPoint.y, (craftingMenu ? 96 : 0), 240, 96, 112, false);

        if (craftingMenu)
        {
             p.draw(drawPoint.x + 46, drawPoint.y - 10, 96, 56, 16, 16, false);
             SpriteNumber.drawNumber(5, drawPoint.x + 36, drawPoint.y - 8, p);
             p.draw(drawPoint.x - 16, drawPoint.y  + 50, 96, 56, 16, 16, false);
             SpriteNumber.drawNumber(5, drawPoint.x  - 26, drawPoint.y  + 52, p);
             p.draw(drawPoint.x + 108, drawPoint.y + 50, 112, 56, 16, 16, false);
             SpriteNumber.drawNumber(5, drawPoint.x + 98, drawPoint.y + 52, p);
             p.draw(drawPoint.x + 48, drawPoint.y + 114, 112, 40, 16, 16, false);
             SpriteNumber.drawNumber(20, drawPoint.x + 38, drawPoint.y + 116, p);
        }


        p.flush();
    }

}
