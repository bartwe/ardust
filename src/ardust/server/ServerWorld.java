package ardust.server;

import ardust.shared.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class ServerWorld {
    byte[] world;
    int[] updates = new int[1024];
    int updatesIndex;

    // z, lower is down

    ServerWorld() {
        world = new byte[Constants.WORLD_LENGTH * Constants.WORLD_LENGTH * Constants.WORLD_DEPTH];
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
            int rem = Constants.WORLD_LENGTH * Constants.WORLD_LENGTH * Constants.WORLD_DEPTH;
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

        for (int z = 0; z < Constants.WORLD_DEPTH; z++)
            for (int y = 0; y < Constants.WORLD_LENGTH; y++)
                for (int x = 0; x < Constants.WORLD_LENGTH; x++)
                    write(x, y, z, (byte)(2+random.nextInt(3)));
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

    private void appendUpdate(int x, int y, int z) {
        int l = x + (y + z * Constants.WORLD_LENGTH) * Constants.WORLD_LENGTH;
        if (updates.length == updatesIndex) {
            updates = Arrays.copyOf(updates, updates.length * 2);
        }
        updates[updatesIndex] = l;
        updatesIndex++;
    }

    public void clearPendingUpdates() {
        updatesIndex = 0;
    }

    public static int normalizeAxis(int axis) {
        //sure, modulo should do the job too
        while (axis < 0)
            axis += Constants.WORLD_LENGTH;
        while (axis >= Constants.WORLD_LENGTH)
            axis -= Constants.WORLD_LENGTH;
        return axis;
    }

    public int[] getUpdatesBufferArray() {
        return updates;
    }

    public int getUpdatesCount() {
        return updatesIndex;
    }

    public void readTiles(byte[] tiles, int[] locations) {
        for (int i = 0; i < locations.length; i++)
            tiles[i] = world[locations[i]];
    }

    public byte read(int x, int y, int z) {
        if ((z < 0) || (z >= Constants.WORLD_DEPTH))
            return 0;
        x = normalizeAxis(x);
        y = normalizeAxis(y);

        return world[x + (y + z * Constants.WORLD_LENGTH) * Constants.WORLD_LENGTH];
    }

    public void write(int x, int y, int z, byte tile) {
        if ((z < 0) || (z >= Constants.WORLD_DEPTH)) {
            // return;
            throw new RuntimeException("writing outside of the world ?");
        }
        x = normalizeAxis(x);
        y = normalizeAxis(y);

        byte current = world[x + (y + z * Constants.WORLD_LENGTH) * Constants.WORLD_LENGTH];
        if (current != tile) {
            world[x + (y + z * Constants.WORLD_LENGTH) * Constants.WORLD_LENGTH] = tile;
            appendUpdate(x, y, z);
        }
    }
}
