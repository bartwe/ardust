package ardust.client;

import ardust.shared.Tile;

public class ClientWorld {
    private static final int WORLD_DEPTH = 16;
    private static final int WORLD_LENGTH = 1024;
    byte[] world;
    Tile[] tiles;
    int[] updates = new int[1024];
    int updatesIndex;

    // z, lower is down

    ClientWorld() {
        world = new byte[WORLD_LENGTH * WORLD_LENGTH * WORLD_DEPTH];
        tiles = new Tile[256];
        for (int i = 0; i < tiles.length; i++)
            tiles[i] = new Tile((byte) i);
    }

    public int xSize() {
        return WORLD_LENGTH;
    }

    public int ySize() {
        return WORLD_LENGTH;
    }

    public int zSize() {
        return WORLD_DEPTH;
    }

    public Tile read(int x, int y, int z) {
        if ((z < 0) || (z >= WORLD_DEPTH))
            return tiles[0];
        x = normalizeAxis(x);
        y = normalizeAxis(y);

        return tiles[((int) world[x + (y + z * WORLD_LENGTH) * WORLD_LENGTH]) & 0xff];
    }


    public byte readDirect(int x, int y, int z) {
        if ((z < 0) || (z >= WORLD_DEPTH))
            return 0;
        x = normalizeAxis(x);
        y = normalizeAxis(y);

        return world[x + (y + z * WORLD_LENGTH) * WORLD_LENGTH];
    }

    private int normalizeAxis(int axis) {
        //sure, modulo should do the job too
        while (axis < 0)
            axis += WORLD_LENGTH;
        while (axis >= WORLD_LENGTH)
            axis -= WORLD_LENGTH;
        return axis;
    }

}
