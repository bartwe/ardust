package ardust.server;

import ardust.shared.ByteBufferBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class Entities {
    HashMap<Integer, Entity> entities;
    ArrayList<Integer> inserted = new ArrayList<Integer>();
    ArrayList<Integer> deleted = new ArrayList<Integer>();

    public boolean write(ByteBuffer buffer, boolean all) {
        int mode = 0;
        int modePosition = buffer.position();
        buffer.put((byte) 0);
        if (!deleted.isEmpty() && !all) {
            mode |= 1;
            buffer.putShort((short) deleted.size());
            for (Integer id : deleted)
                buffer.putInt(id);
            deleted.clear();
        }
        if (!inserted.isEmpty() && !all) {
            mode |= 2;
            buffer.putShort((short) inserted.size());
            for (Integer id : inserted)
                buffer.putInt(id);
            inserted.clear();
        }
        int sizePosition = buffer.position();
        buffer.putShort((short) 0);
        int size = 0;
        for (Entity entity : entities.values()) {
            int idPosition = buffer.position();
            buffer.putInt(entity.id);
            if (entity.write(buffer, all))
                size++;
            else
                buffer.position(idPosition);
            entity.postWrite();
        }
        if (size > 0) {
            buffer.putShort(sizePosition, (short) size);
            mode |= 4;
        } else {
            buffer.position(sizePosition);
        }
        buffer.put(modePosition, (byte) mode);
        return mode != 0;
    }

    public void read(ByteBuffer buffer) {
        int mode = buffer.get();
        if ((mode & 0x1) != 0) { // deleted
            int count = buffer.getShort();
            while (count > 0) {
                entities.remove(buffer.getInt());
                count--;
            }
        }
        if ((mode & 0x2) != 0) { // inserts
            int count = buffer.getShort();
            while (count > 0) {
                Integer id = buffer.getInt();
                entities.put(id, new Entity(id));
                count--;
            }
        }
        if ((mode & 0x4) != 0) {
            int count = buffer.getShort();
            while (count > 0) {
                Integer id = buffer.getInt();
                entities.get(id).read(buffer);
                count--;
            }
        }
    }

    public void load() {
        File file = new File("entities.dat");
        if (!file.canRead())
            return;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            int rem = (int) file.length();
            ByteBuffer buffer = ByteBufferBuffer.alloc(rem);
            int off = buffer.arrayOffset();
            while (rem > 0) {
                int len = in.read(buffer.array(), off, rem);
                if (len == -1)
                    throw new RuntimeException("Failed to load world from disk.");
                off += len;
                rem -= len;
            }
            in.close();
            entities.clear();
            inserted.clear();
            deleted.clear();
            read(buffer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        File file = new File("entities.dat");
        FileOutputStream out = null;
        try {
            ByteBuffer buffer = ByteBufferBuffer.alloc(4 * 1024 * 1024);
            write(buffer, true);
            buffer.flip();
            out = new FileOutputStream(file);
            out.write(buffer.array(), buffer.arrayOffset(), buffer.remaining());
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
