package ardust.shared;

public class Constants {
    public static final boolean DEVELOPER = true;

    public static final int PORT = 0xba57;
    public static final long MILLIS_PER_SERVER_TICK = 200;

    public static final int WORLD_DEPTH = 16;
    public static final int WORLD_LENGTH = 1024;

    public static final int DWARF_DEFAULT_SPEED = 2;
    public static final int DWARF_ANIMATION_SPEED = 30;
    public static final int DWARF_OFFSET_ON_TILE = 8;

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

    public static final int DUMMY_Z = 5;
}
