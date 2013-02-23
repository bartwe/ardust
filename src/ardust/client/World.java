package ardust.client;

import ardust.entities.Entities;
import ardust.entities.Entity;
import ardust.shared.Constants;
import ardust.shared.NetworkConnection;
import ardust.shared.Point2;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class World {
    public static final int tilesBeyondViewportToRender = 3;

    ClientWorld clientWorld;
    Entities entities;
    Characters characters;
    ArrayList<SolitaryAnimatedSprite> temporaryAnimatedSprites = new ArrayList<SolitaryAnimatedSprite>();


    public World() {
        clientWorld = new ClientWorld();
        entities = new Entities();
        characters = new Characters(entities);
    }

    public static void globalTileToLocalCoord(int tileX, int tileY, Point viewportLocation, Point result) {
        result.setLocation(tileX * Constants.TILE_BASE_WIDTH - viewportLocation.x, tileY * Constants.TILE_BASE_HEIGHT - viewportLocation.y);
    }

    public void tick(int deltaT, NetworkConnection network, GameCore core) {
        characters.tick(deltaT, this, network, core);
        for (int i = temporaryAnimatedSprites.size() - 1; i >= 0; i--)
        {
            if (temporaryAnimatedSprites.get(i).animate())
            {
                temporaryAnimatedSprites.remove(i);
            }
        }
    }

    Point toDrawCoord = new Point();
    Rectangle tileSheetFloorRect = new Rectangle();
    Rectangle tileSheetRect = new Rectangle();
    Point2 tilePoint = new Point2();

    public void draw(Painter p, Point viewportLocation, int screenWidth, int screenHeight, Character selectedDwarf, int cursorX, int cursorY) {

        int tileRectX = viewportLocation.x / Constants.TILE_BASE_WIDTH - tilesBeyondViewportToRender;
        int tileRectY = viewportLocation.y / Constants.TILE_BASE_HEIGHT - tilesBeyondViewportToRender;
        int tileRectWidth = screenWidth / Constants.TILE_BASE_WIDTH + 2 * tilesBeyondViewportToRender;
        int tileRectHeight = screenHeight / Constants.TILE_BASE_HEIGHT + 2 * tilesBeyondViewportToRender;

        HashMap<Point2, Character> charactersByPosition = characters.charactersByPosition();

        double t = (System.currentTimeMillis() / 1000d * 2d * 3.14d) / 5;
        float r = (float) Math.abs(Math.sin(t));
        float g = (float) Math.abs(Math.sin(t + 2));
        float b = (float) Math.abs(Math.sin(t + 4));
        float a = 0.5f + 0.5f * (float) Math.abs(Math.sin(t * 3));

        p.start();
        for (int y = tileRectY; y < tileRectY + tileRectHeight; y++) {
            for (int x = tileRectX; x < tileRectX + tileRectWidth; x++) {

                globalTileToLocalCoord(x, y, viewportLocation, toDrawCoord);

                //Draw Floor
                p.getSourceRectFromTileSheetIndex(0, tileSheetFloorRect);
                p.draw(toDrawCoord.x, toDrawCoord.y - (Constants.TILE_DRAW_HEIGHT - Constants.TILE_BASE_HEIGHT) + Constants.FLOOR_TILE_THICKNESS,
                        tileSheetFloorRect.x, tileSheetFloorRect.y, tileSheetFloorRect.width, tileSheetFloorRect.height, false);

                //Draw Terrain Item
                byte whatItem = clientWorld.read(x, y);
                int baseBlock = Constants.convertIndexToBaseBlockIndex(whatItem);
                int blockMod = Constants.getBlockModIndex(whatItem);
                if (whatItem != 0) {
                    if ((x == cursorX) && (y == cursorY))
                        GL11.glColor4f(r, g, b, 1);
                    p.getSourceRectFromTileSheetIndex(baseBlock, tileSheetRect);
                    p.draw(toDrawCoord.x, toDrawCoord.y - (Constants.TILE_DRAW_HEIGHT - Constants.TILE_BASE_HEIGHT),
                            tileSheetRect.x, tileSheetRect.y, tileSheetRect.width, tileSheetRect.height, false);
                    if ((x == cursorX) && (y == cursorY))
                        GL11.glColor4f(1, 1, 1, 1);

                    if (blockMod != -1) {
                        p.getSourceRectFromTileSheetIndex(blockMod, tileSheetRect);
                        p.draw(toDrawCoord.x, toDrawCoord.y - (Constants.TILE_DRAW_HEIGHT - Constants.TILE_BASE_HEIGHT),
                                tileSheetRect.x, tileSheetRect.y, tileSheetRect.width, tileSheetRect.height, false);
                    }
                }

                //Draw Animations
                for (SolitaryAnimatedSprite sprite : temporaryAnimatedSprites)
                {

                    sprite.draw(p, viewportLocation);
                }

                //Draw Character
                tilePoint.set(x, y);
                Character character = charactersByPosition.get(tilePoint);
                if (character != null) {
                    character.draw(p, viewportLocation, character.equals(selectedDwarf));
                }
            }
        }

        //cursor
        GL11.glColor4f(r, g, b, a);
        globalTileToLocalCoord(cursorX, cursorY, viewportLocation, toDrawCoord);

        p.getSourceRectFromTileSheetIndex(Constants.CURSOR_TILE_NORMAL, tileSheetRect);
        p.draw(toDrawCoord.x, toDrawCoord.y - (Constants.TILE_DRAW_HEIGHT - Constants.TILE_BASE_HEIGHT),
                tileSheetRect.x, tileSheetRect.y, tileSheetRect.width, tileSheetRect.height, false);

        p.flush();
    }

    public static void screenCoordToWorldCoord(Point viewportLocation, Point result) {
        int x = viewportLocation.x / Constants.TILE_BASE_WIDTH;
        int y = viewportLocation.y / Constants.TILE_BASE_HEIGHT;
        result.setLocation(x, y);
    }

    public static void worldCoordToScreenCoord(Point worldLocation, Point result) {
        int x = worldLocation.x * Constants.TILE_BASE_WIDTH;
        int y = worldLocation.y * Constants.TILE_BASE_HEIGHT;
        result.setLocation(x, y);
    }

    public static void globalNonTileCoordToScreenCoord(int x, int y, Point result, Point viewportLocation)
    {
      result.setLocation(x - viewportLocation.x, y - viewportLocation.y);

    }

    public static void localCoordToGlobalTile(int x, int y, Point viewportLocation, Point result) {
        result.setLocation((viewportLocation.x + x / Constants.PIXEL_SCALE) / Constants.TILE_BASE_WIDTH, (viewportLocation.y + y / Constants.PIXEL_SCALE + Constants.MOUSE_TO_TILE_YSHIFT) / Constants.TILE_BASE_HEIGHT);
    }

    public void writeTiles(int[] locations, byte[] tiles) {
        clientWorld.writeTiles(locations, tiles);
    }

    public boolean isTileOccupied(int x, int y, Entity entity) {
        if (!Constants.isWalkable(clientWorld.read(x, y)))
            return true;
        Character at = getCharacterAtTile(x, y);
        if ((at != null) && (at.id().equals(entity.id)))
            return true;
        return false;
    }

    public Character getCharacterAtTile(int x, int y) {
        return characters.getCharacterAtTile(x, y);
    }

    public void updateEntities(ByteBuffer data, boolean checkpoint) {
        entities.read(data, checkpoint);
    }

    public Character nextCharacter(int playerId, Character selectedDwarf) {
        return characters.nextCharacter(playerId, selectedDwarf);
    }

    public boolean isTileOccupied(Point2 point, Entity entity) {
        return isTileOccupied(point.x, point.y,  entity);
    }

    public boolean isTileMineable(Point2 point) {
        return Constants.isMinable(clientWorld.read(point.x, point.y));
    }
}
