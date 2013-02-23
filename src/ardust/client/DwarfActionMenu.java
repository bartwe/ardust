package ardust.client;


import ardust.shared.Constants;

import java.awt.*;

public class DwarfActionMenu {
    private final int TEXTURE_X = 0;
    private final int TEXTURE_Y = 240;
    private final int COLORMAP_X = 96;
    private final int COLORMAP_Y = 240;

    Character dwarf;

    public DwarfActionMenu(Character dwarf) {
        this.dwarf = dwarf;
    }

    public GameCore.UserInputState isButtonHere(int x, int y, Point viewportLocation) {
        int localX = x - getDrawPoint(viewportLocation).x;
        int localY = y - getDrawPoint(viewportLocation).y;
        int colorMapX = localX + COLORMAP_X;
        int colorMapY = localY + COLORMAP_Y;

        Color c = Painter.getColorAt(colorMapX, colorMapY);

        if (c.equals(Color.RED)) return GameCore.UserInputState.HALT;
        if (c.equals(Color.GREEN)) return GameCore.UserInputState.WALK;
        if (c.equals(Color.BLUE)) return GameCore.UserInputState.MINE;

        return GameCore.UserInputState.NO_DWARF_SELECTED;
    }

    public Point getDrawPoint(Point viewportLocation) {
        Point localCharacterPoint = dwarf.getLocalDrawPoint(viewportLocation);
        return new Point(localCharacterPoint.x - 48, localCharacterPoint.y - Constants.TILE_DRAW_HEIGHT);
    }

    public void draw(Painter p, Point viewportLocation) {
        Point drawPoint = getDrawPoint(viewportLocation);

        p.draw(drawPoint.x, drawPoint.y, 0, 240, 96, 80, false);
    }

}
