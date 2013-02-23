package ardust.shared;

public class Constants {
    public static final boolean DEVELOPER = true;

    public static final int PORT = 0xba57;
    public static final long MILLIS_PER_SERVER_TICK = 200;

    public static final int WORLD_DEPTH = 16;
    public static final int WORLD_LENGTH = 1024;

    public static final int DWARF_ANIMATION_SPEED = 10;
    public static final int DWARF_OFFSET_ON_TILE = 8;

    public static final int BUTTON_SIZE = 32;

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
    public static final int ACTION_CURSOR = 2;

    public static final int DEFAULT_Z = 5;

    public static final int V_ENTITY_POS_X = 0;
    public static final int V_ENTITY_POS_Y = 1;
    public static final int V_ENTITY_POS_Z = 2;
    public static final int V_ENTITY_KIND = 3;
    public static final int V_ENTITY_ORIENTATION = 4;
    public static final int V_ENTITY_MODE = 5;
    public static final int V_ENTITY_HEALTH = 6;
    public static final int V_ENTITY_VALUES_SIZE = 7;

    public static final int CURSOR_TILE_NORMAL = 4;
    public static final int CURSOR_TILE_PROBLEM = 5;

    public static final int MOUSE_TO_TILE_YSHIFT = 8;

    // start away from 0,0, bugs near negative numbers
    public static final int START_OFFSET = 1000000;

    public static final int WALKING_COUNTDOWN = 500;

    public static final int DWARF_HEART_CENTER_OFFSET = 11;

    public static final int STONE = 1;
    public static final int GOLD = 3;
    public static final int IRON = 4;
    public static final int ANVIL = 8;

    public static int isWorldPieceMineable(byte index) {
        switch (index) {
            case 0:
            case 2:
                return 500;
            case STONE:
                return 1500;
            case GOLD:
                return 7000;
            case IRON:
                return 3000;
            default:
                return 0;
        }
    }

    public static int getBlockModIndex(byte index) {
        switch (index) {
            case GOLD:
                return 13;
            case IRON:
                return 14;
            default:
                return -1;
        }
    }

    public static int convertIndexToBaseBlockIndex(byte index) {
        switch (index) {
            case GOLD:
            case IRON:
                return 1;
            default:
                return index;
        }
    }


}
