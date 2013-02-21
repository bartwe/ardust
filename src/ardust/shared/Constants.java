package ardust.shared;

public class Constants {
    public static final long MILLIS_PER_SERVER_TICK = 200;

    // client get this radius of data around the screen center from the server
    public static final int RADIUS = 32;
    public static final int ZRADIUS = 2;

    public static final int PIXEL_SCALE = 3;
    public static final int TILE_BASE_WIDTH = 32;
    public static final int TILE_BASE_HEIGHT = 16; //the height of an in-game tile.
    public static final int TILE_DRAW_HEIGHT = 40; //the actual height of the graphic assets
    public static final int FLOOR_TILE_THICKNESS = 4;
    public static final int MAP_PAN_MAX_SPEED = 3;
    public static final int MAP_PAN_SENSITIVITY = 256; //distance to drag away from click to reach max panning speed

    //mouse cursors
    public static final int CURSOR_X_IN_TILESHEET = 64;
    public static final int CURSOR_Y_IN_TILESHEET = 40;
    public static final int DEFAULT_CURSOR = 0;
    public static final int PANNING_CURSOR = 1;
}
