package ardust.client;

import ardust.shared.Constants;

public class ClientWorld {
    byte[] world;

    ClientWorld() {
        world = new byte[Constants.WORLD_LENGTH * Constants.WORLD_LENGTH];
    }

    public byte read(int x, int y) {
        x = normalizeAxis(x);
        y = normalizeAxis(y);

        return world[x + y * Constants.WORLD_LENGTH];
    }

    public int normalizeAxis(int axis) {
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
