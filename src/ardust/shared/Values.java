package ardust.shared;

import java.nio.ByteBuffer;

public class Values {
    long[] revision;
    int[] entries;
    long currentTick;

    public Values(int size) {
        revision = new long[size];
        entries = new int[size];
    }

    public void set(int index, int value) {
        if (entries[index] != value) {
            revision[index] = currentTick;
            entries[index] = value;
        }
    }

    public int get(int index) {
        return entries[index];
    }

    public boolean write(ByteBuffer buffer, boolean all) {
        int count = 0;
        if (all)
            count = entries.length;
        else {
            for (int i = 0; i < entries.length; i++) {
                if (revision[i] == currentTick)
                    count++;
            }
        }
        if ((count == 0) && !all)
            return false;
        buffer.put((byte) count);
        if (count == entries.length) {
            for (int i = 0; i < entries.length; i++)
                buffer.putInt(entries[i]);
        } else {
            for (int i = 0; i < entries.length; i++) {
                if (revision[i] == currentTick) {
                    buffer.put((byte) i);
                    buffer.putInt(entries[i]);
                }
            }
        }
        return true;
    }

    public void nextTick() {
        currentTick++;
    }

    public void read(ByteBuffer buffer) {
        int count = buffer.get();
        if (count == entries.length) {
            for (int i = 0; i < entries.length; i++)
                entries[i] = buffer.getInt();
        } else {
            while (count > 0) {
                int index = buffer.get();
                entries[index] = buffer.getInt();
                count--;
            }
        }
    }

    public static void dropRead(ByteBuffer buffer, int size) {
        int count = buffer.get();
        if (count == size) {
            buffer.position(buffer.position() + 4*count);
            for (int i = 0; i < size; i++)
                buffer.getInt();
        } else {
            buffer.position(buffer.position() + 5*count);
        }
    }
}
