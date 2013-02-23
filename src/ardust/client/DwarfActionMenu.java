package ardust.client;


import ardust.shared.Constants;
import ardust.shared.Point3;

import java.awt.*;
import java.util.ArrayList;

public class DwarfActionMenu {
    private final int WALK = 0;
    private final int HALT = 1;
    private final int MINE = 2;

    Point3 location = new Point3();
    ArrayList<Rectangle> buttons;

    public DwarfActionMenu(Point3 location) {
        this.location.set(location);
        buttons = new ArrayList<Rectangle>();
        buttons.add(new Rectangle(32, 12, 32, 32)); //0 -- WALK
        buttons.add(new Rectangle(0, 46, 32, 32));  //1 -- HALT
        buttons.add(new Rectangle(64, 46, 32, 32)); //2  -- MINE (Yes it's ugly but we're short on time)
    }

    public GameCore.UserInputState isButtonHere(int x, int y, Point viewportLocation) {
        Point p = getDrawPoint(viewportLocation);
        int localX = (x - p.x * Constants.PIXEL_SCALE) / Constants.PIXEL_SCALE;
        int localY = (y - p.y * Constants.PIXEL_SCALE) / Constants.PIXEL_SCALE;

        if (buttons.get(WALK).contains(localX, localY)) return GameCore.UserInputState.WALK;
        if (buttons.get(HALT).contains(localX, localY)) return GameCore.UserInputState.HALT;
        if (buttons.get(MINE).contains(localX, localY)) return GameCore.UserInputState.MINE;

        return GameCore.UserInputState.NONE;
    }

    Point tempPoint = new Point();

    public Point getDrawPoint(Point viewportLocation) {
        World.globalTileToLocalCoord(location.x, location.y, location.z, viewportLocation, tempPoint);
        return new Point(tempPoint.x - 32, tempPoint.y - Constants.TILE_DRAW_HEIGHT - 24);
    }

    public void draw(Painter p, Point viewportLocation) {
        p.start();
        Point drawPoint = getDrawPoint(viewportLocation);

        p.draw(drawPoint.x, drawPoint.y, 0, 240, 96, 80, false);
        p.flush();
    }

}
