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
        world = new byte[Constants.WORLD_LENGTH * Constants.WORLD_LENGTH];
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
            int rem = Constants.WORLD_LENGTH * Constants.WORLD_LENGTH;
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

            for (int y = 0; y < Constants.WORLD_LENGTH; y++)
                for (int x = 0; x < Constants.WORLD_LENGTH; x++)
                    write(x, y, (byte) (1 + random.nextInt(4)));
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

    private void appendUpdate(int x, int y) {
        int l = x + y * Constants.WORLD_LENGTH;
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

    public byte read(int x, int y) {
        x = normalizeAxis(x);
        y = normalizeAxis(y);

        return world[x + y * Constants.WORLD_LENGTH];
    }

    public void write(int x, int y, byte tile) {
        x = normalizeAxis(x);
        y = normalizeAxis(y);

        byte current = world[x + y * Constants.WORLD_LENGTH];
        if (current != tile) {
            world[x + y  * Constants.WORLD_LENGTH] = tile;
            appendUpdate(x, y);
        }
    }
}
