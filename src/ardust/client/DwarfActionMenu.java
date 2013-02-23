package ardust.client;


import ardust.shared.Constants;

import java.awt.*;
import java.util.ArrayList;

public class DwarfActionMenu {
    private final int WALK = 0;
    private final int HALT = 1;
    private final int MINE = 2;
    private final int USE = 3;

    Character dwarf;
    ArrayList<Rectangle> buttons;
    boolean craftingMenu;

    public DwarfActionMenu(Character dwarf, boolean craftingMenu) {
        this.dwarf = dwarf;
        this.craftingMenu = craftingMenu;

        buttons = new ArrayList<Rectangle>();
        buttons.add(new Rectangle(32, 12, 32, 32)); //0 -- WALK
        buttons.add(new Rectangle(0, 46, 32, 32));  //1 -- HALT
        buttons.add(new Rectangle(64, 46, 32, 32)); //2  -- MINE (Yes it's ugly but we're short on time)
        if (!craftingMenu)buttons.add(new Rectangle(32, 80, 32, 32)); // 3 -- USE
    }

    public GameCore.UserInputState isButtonHere(int x, int y, Point viewportLocation) {
        Point p = getDrawPoint(viewportLocation);
        int localX = (x - p.x * Constants.PIXEL_SCALE) / Constants.PIXEL_SCALE;
        int localY = (y - p.y * Constants.PIXEL_SCALE) / Constants.PIXEL_SCALE;


        System.out.println("Clicked @ :" + x + ", " + y + "   Local: " + localX + " , " + localY + "DrawPoint: " + p.toString());

        if (buttons.get(WALK).contains(localX, localY)) return craftingMenu? GameCore.UserInputState.ATTEMPTING_ARMOR_PURCHASE : GameCore.UserInputState.WALK;
        if (buttons.get(HALT).contains(localX, localY)) return craftingMenu? GameCore.UserInputState.ATTEMPTING_SWORD_PURCHASE : GameCore.UserInputState.HALT;
        if (buttons.get(MINE).contains(localX, localY)) return craftingMenu? GameCore.UserInputState.ATTEMPTING_GOLD_SWORD_PURCHASE  : GameCore.UserInputState.MINE;
        if (!craftingMenu && buttons.get(USE).contains(localX, localY)) return GameCore.UserInputState.USE;

        return GameCore.UserInputState.NO_DWARF_SELECTED;
    }

    public Point getDrawPoint(Point viewportLocation) {
        Point localCharacterPoint = dwarf.getLocalDrawPoint(viewportLocation);
        return new Point(localCharacterPoint.x - 32, localCharacterPoint.y - Constants.TILE_DRAW_HEIGHT - 24);
    }

    public void draw(Painter p, Point viewportLocation) {
        p.start();
        Point drawPoint = getDrawPoint(viewportLocation);

        p.draw(drawPoint.x, drawPoint.y,(craftingMenu?96 :0), 240 , 96, 112, false);
        p.flush();
    }

}
