package ardust.client;

import java.awt.*;
import java.util.Random;

/* I chose to store the "terrainItems" (any stationary object that exists in the world... like stones, structures, etc.)  in a byte array to save on memory (the bytes would presumably
correspond to an index in the tile sheet).
This might be a bad choice, but I figured there wouldn't be too many possible types in a game of this size, and they wouldn't really need to store any data about themselves.
I'm also not sure how complex the interactions will be with these structures, but I figured that we might able to do something like Minecraft,
so each dwarf can really only affect one block at a time (if you are mining a block and then switch to another block
your "damage" to the initial block just goes away). So we could just have each dwarf keep track of the terrainItem he is currently
working with, and pull info (health, effect on adjacent dwarves) from a couple of switch statements somewhere.
I dunno.. it's something to discuss.


*/
public class World {
    public static final int worldWidth = 64;
    public static final int worldHeight = 64;
    public static final byte emptySpot = 127;
    public static final int tilesBeyondViewportToRender = 1;

    private byte[][] terrainItems = new byte[worldWidth][worldHeight];

    public World() {
       //Populate with random stuff for now
        Random r = new Random();

          for (int i = 0; i < worldWidth; i++) {
            for (int j = 0; j < worldHeight; j++) {
               terrainItems[i][j] = r.nextDouble () < .5 ? (byte)(r.nextInt(2) * 2 + 1) : emptySpot;
            }
        }
    }


    public void tick() {

    }

    public byte getTerrainItemAt(int x, int y) {
        if (x >= 0 && x < worldWidth && y >= 0 && y < worldHeight)
        return terrainItems[x][y];

        return -1;
    }

    public void setTerrainItemAt(int x, int y, byte whichItem) {
        if (x >= 0 && x < worldWidth && y >= 0 && y < worldHeight)
        terrainItems[x][y] = whichItem;
    }

    public static Point globalTileToLocalCoord(int tileX, int tileY, Point viewportLocation) {

        return new Point(tileX * GameLoop.TILE_BASE_WIDTH - viewportLocation.x, tileY * GameLoop.TILE_BASE_HEIGHT - viewportLocation.y);
    }

    public static Point localPointToGlobalTile(int x, int y, Point viewportLocation)
    {
        return new Point(viewportLocation.x + x, viewportLocation.y + y);
    }

    public boolean isGlobalPointOnMap(int x, int y)
    {
        return (x >= 0 && x < worldWidth * GameLoop.TILE_BASE_WIDTH && y >= 0 && y < worldHeight * GameLoop.TILE_BASE_HEIGHT);
    }

    public boolean isLocalPointOnMap(int x, int y, Point viewportLocation)
    {
       Point p = localPointToGlobalTile(x, y, viewportLocation);
        return (p.x >= 0 && p.x < worldWidth && p.y >= 0 && p.y < worldHeight);
    }

    public void constrainViewport(){
        if (GameLoop.getViewportLocation().x < 0)  GameLoop.setViewportX(0);
        else if (GameLoop.getViewportLocation().x > World.worldWidth * GameLoop.TILE_BASE_WIDTH - GameLoop.getWidth()) {
            GameLoop.setViewportX(World.worldWidth* GameLoop.TILE_BASE_HEIGHT - GameLoop.getWidth());
        }
        if (GameLoop.getViewportLocation().y < -GameLoop.TILE_DRAW_HEIGHT ) GameLoop.setViewportY(-GameLoop.TILE_DRAW_HEIGHT);
        else if (GameLoop.getViewportLocation().y > World.worldHeight * GameLoop.TILE_BASE_HEIGHT - GameLoop.getHeight())  {
            GameLoop.setViewportY(World.worldHeight* GameLoop.TILE_BASE_HEIGHT - GameLoop.getHeight());
        }
    }



    public void draw(Painter p, Point viewportLocation,int screenWidth, int screenHeight ) {

        int tileRectX = viewportLocation.x / GameLoop.TILE_BASE_WIDTH - tilesBeyondViewportToRender;
        int tileRectY = viewportLocation.y / GameLoop.TILE_BASE_HEIGHT - tilesBeyondViewportToRender;
        int tileRectWidth = screenWidth / GameLoop.TILE_BASE_WIDTH + 2 * tilesBeyondViewportToRender;
        int tileRectHeight = screenHeight / GameLoop.TILE_BASE_HEIGHT + 2 * tilesBeyondViewportToRender;

        p.start();
        for (int x = tileRectX; x < tileRectX + tileRectWidth; x++) {
            for (int y = tileRectY; y < tileRectY + tileRectHeight; y++) {

                Point toDrawCoord = globalTileToLocalCoord(x, y, viewportLocation);

                //Draw Floor
                Rectangle tileSheetFloorRect = p.getSourceRectFromTileSheetIndex(0);
                 p.draw(toDrawCoord.x, toDrawCoord.y - (GameLoop.TILE_DRAW_HEIGHT - GameLoop.TILE_BASE_HEIGHT) + GameLoop.FLOOR_TILE_THICKNESS,
                         tileSheetFloorRect.x, tileSheetFloorRect.y, tileSheetFloorRect.width, tileSheetFloorRect.height);

                //Draw Terrain Item
                byte whatItem = getTerrainItemAt(x, y);
                if (whatItem != emptySpot) {
                    Rectangle tileSheetRect = p.getSourceRectFromTileSheetIndex(whatItem);
                    p.draw(toDrawCoord.x, toDrawCoord.y - (GameLoop.TILE_DRAW_HEIGHT - GameLoop.TILE_BASE_HEIGHT),
                            tileSheetRect.x, tileSheetRect.y, tileSheetRect.width, tileSheetRect.height);
                }
            }
        }
        p.flush();

    }



}
