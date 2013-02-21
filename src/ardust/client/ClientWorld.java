package ardust.client;

import ardust.shared.Constants;
import ardust.shared.Tile;

public class ClientWorld {
    byte[] world;
    Tile[] tiles;

    // z, lower is down

    ClientWorld() {
        world = new byte[Constants.WORLD_LENGTH * Constants.WORLD_LENGTH * Constants.WORLD_DEPTH];
        tiles = new Tile[256];
        for (int i = 0; i < tiles.length; i++)
            tiles[i] = new Tile((byte) i);
    }

    public Tile read(int x, int y, int z) {
        if ((z < 0) || (z >= Constants.WORLD_DEPTH))
            return tiles[0];
        x = normalizeAxis(x);
        y = normalizeAxis(y);

        return tiles[((int) world[x + (y + z * Constants.WORLD_LENGTH) * Constants.WORLD_LENGTH]) & 0xff];
    }


    public byte readDirect(int x, int y, int z) {
        if ((z < 0) || (z >= Constants.WORLD_DEPTH))
            return 0;
        x = normalizeAxis(x);
        y = normalizeAxis(y);

        return world[x + (y + z * Constants.WORLD_LENGTH) * Constants.WORLD_LENGTH];
    }

    private int normalizeAxis(int axis) {
        //sure, modulo should do the job too
        while (axis < 0)
            axis += Constants.WORLD_LENGTH;
        while (axis >= Constants.WORLD_LENGTH)
            axis -= Constants.WORLD_LENGTH;
        return axis;
    }


    public void writeTiles(int[] locations, byte[] tiles) {
        for (int i = 0; i < locations.length; i++)
            world[locations[i]] = tiles[i];
    }
}
