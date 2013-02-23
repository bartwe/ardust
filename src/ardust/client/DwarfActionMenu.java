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


    public enum Mode {
        Normal,
        Crafting,
        Fight
    }

    Point2 location = new Point2();
    ArrayList<Rectangle> buttons;
    Mode mode;

    public DwarfActionMenu(Point2 location, Mode mode) {
        this.location.set(location);
        this.mode = mode;

        buttons = new ArrayList<Rectangle>();

        buttons.add(new Rectangle(32, 12, 32, 32)); //0 -- WALK/ARMOR
        buttons.add(new Rectangle(0, 46, 32, 32));  //1 -- HALT/SWORD
        buttons.add(new Rectangle(64, 46, 32, 32)); //2  -- MINE/ATTACK/GOLDSWORD
        buttons.add(new Rectangle(32, 80, 32, 32)); //3  -- USE/BUILD
    }

    public GameCore.UserInputState isButtonHere(int x, int y, Point viewportLocation) {
        Point p = getDrawPoint(viewportLocation);
        int localX = (x - p.x * Constants.PIXEL_SCALE) / Constants.PIXEL_SCALE;
        int localY = (y - p.y * Constants.PIXEL_SCALE) / Constants.PIXEL_SCALE;

        if (buttons.get(WALK).contains(localX, localY))
            return (mode == Mode.Crafting) ? GameCore.UserInputState.ATTEMPTING_ARMOR_PURCHASE : GameCore.UserInputState.WALK;
        if (buttons.get(HALT).contains(localX, localY))
            return (mode == Mode.Crafting) ? GameCore.UserInputState.ATTEMPTING_SWORD_PURCHASE : GameCore.UserInputState.HALT;
        if (buttons.get(MINE).contains(localX, localY)) {
            switch (mode) {
                case Normal:
                    return GameCore.UserInputState.MINE;
                case Crafting:
                    return GameCore.UserInputState.ATTEMPTING_GOLD_SWORD_PURCHASE;
                case Fight:
                    return GameCore.UserInputState.FIGHT;
            }
        }
        if ((mode != Mode.Crafting) && buttons.get(USE).contains(localX, localY)) {
            return GameCore.UserInputState.BUILD;
        }

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

        int tx = 0;
        int ty = 240;
        if (mode == Mode.Crafting)
            tx = 96;
        if (mode == Mode.Fight) {
            tx = 0;
            ty = 512;
        }

        p.draw(drawPoint.x, drawPoint.y, tx, ty, 96, 112, false);

        if (mode == Mode.Crafting)
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
