package ardust.server;

import ardust.shared.Tile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class ServerWorld {
    private static final int WORLD_DEPTH = 16;
    private static final int WORLD_LENGTH = 1024;
    byte[] world;
    Tile[] tiles;
    int[] updates = new int[1024];
    int updatesIndex;

    // z, lower is down

    ServerWorld() {
        world = new byte[WORLD_LENGTH * WORLD_LENGTH * WORLD_DEPTH];
        tiles = new Tile[256];
        for (int i = 0; i < tiles.length; i++)
            tiles[i] = new Tile((byte) i);
    }

    public void load() {
        try {
            FileInputStream in;
            if (new File("world.dat").canRead())
                in = new FileInputStream("world.dat");
            else {
                if (new File("world.dat").canRead())
                    in = new FileInputStream("template.dat");
                else {
                    generateWorld();
                    return;
                }
            }
            int off = 0;
            int rem = WORLD_LENGTH * WORLD_LENGTH * WORLD_DEPTH;
            while (rem > 0) {
                int len = in.read(world, off, rem);
                if (len == -1)
                    throw new RuntimeException("Failed to load world from disk.");
                off += len;
                rem -= len;
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateWorld() {
        Random random = new Random();

        for (int z = 0; z < zSize(); z++)
            for (int y = 0; y < ySize(); y++)
                for (int x = 0; x < xSize(); x++)
                    write(x, y, z, tiles[random.nextInt(3)]);
        clearPendingUpdates();
    }

    public void save() {
        try {
            FileOutputStream out = new FileOutputStream("world.dat");
            out.write(world);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void write(int x, int y, int z, Tile tile) {
        if ((z < 0) || (z >= WORLD_DEPTH)) {
            // return;
            throw new RuntimeException("writing outside of the world ?");
        }
        x = normalizeAxis(x);
        y = normalizeAxis(y);

        byte current = world[x + (y + z * WORLD_LENGTH) * WORLD_LENGTH];
        byte value = tile.value();
        if (current != value) {
            world[x + (y + z * WORLD_LENGTH) * WORLD_LENGTH] = tile.value();
            appendUpdate(x, y, z);
        }
    }

    private void appendUpdate(int x, int y, int z) {
        int l = x + (y + z * WORLD_LENGTH) * WORLD_LENGTH;
        if (updates.length == updatesIndex) {
            updates = Arrays.copyOf(updates, updates.length * 2);
        }
        updates[updatesIndex] = l;
        updatesIndex++;
    }

    public void clearPendingUpdates() {
        updatesIndex = 0;
    }

    private int normalizeAxis(int axis) {
        //sure, modulo should do the job too
        while (axis < 0)
            axis += WORLD_LENGTH;
        while (axis >= WORLD_LENGTH)
            axis -= WORLD_LENGTH;
        return axis;
    }

    public int[] getUpdatesBufferArray() {
        return updates;
    }

    public int getUpdatesCount() {
        return updatesIndex;
    }

    public void readTiles(byte[] tiles, int[] locations) {
        for (int i = 0; i<locations.length; i++)
            tiles[i] = world[locations[i]];
    }

    public byte readDirect(int x, int y, int z) {
        if ((z < 0) || (z >= WORLD_DEPTH))
            return 0;
        x = normalizeAxis(x);
        y = normalizeAxis(y);

        return world[x + (y + z * WORLD_LENGTH) * WORLD_LENGTH];
    }
}
