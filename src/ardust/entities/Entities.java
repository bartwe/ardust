package ardust.entities;

import ardust.shared.ByteBufferBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class Entities {
    Integer nextId = 0;
    public HashMap<Integer, Entity> entities = new HashMap<Integer, Entity>();
    public ArrayList<Integer> inserted = new ArrayList<Integer>();
    public ArrayList<Integer> deleted = new ArrayList<Integer>();

    public boolean write(ByteBuffer buffer, boolean checkpoint, boolean tick) {
        int mode = 0;
        int modePosition = buffer.position();
        buffer.put((byte) 0);
        if (!deleted.isEmpty() && !checkpoint) {
            mode |= 1;
            buffer.putShort((short) deleted.size());
            for (Integer id : deleted)
                buffer.putInt(id);
            deleted.clear();
        }
        if (!inserted.isEmpty() && !checkpoint) {
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
            if (entity.write(buffer, checkpoint))
                size++;
            else
                buffer.position(idPosition);
            if (tick)
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

    public void read(ByteBuffer buffer, boolean checkpoint) {
        int mode = buffer.get();

        if ((mode & 0x1) != 0) { // deleted
            if (checkpoint)
                throw new RuntimeException();
            int count = buffer.getShort();
            while (count > 0) {
                Integer id = buffer.getInt();
                if (entities.containsKey(id)) {
                    entities.remove(id);
                    deleted.add(id);
                }
                count--;
            }
        }
        if ((mode & 0x2) != 0) { // inserts
            if (checkpoint)
                throw new RuntimeException();
            int count = buffer.getShort();
            while (count > 0) {
                Integer id = buffer.getInt();
                if (!entities.containsKey(id)) {
                    entities.put(id, new Entity(id));
                    inserted.add(id);
                }
                count--;
            }
        }
        if ((mode & 0x4) != 0) {
            int count = buffer.getShort();
            if (checkpoint) {
                HashMap<Integer, Entity> newEntities = new HashMap<Integer, Entity>();
                while (count > 0) {
                    Integer id = buffer.getInt();
                    Entity entity = entities.get(id);
                    if (entity == null) {
                        entity = new Entity(id);
                        inserted.add(id);
                    }
                    newEntities.put(id, entity);
                    entity.read(buffer);
                    count--;
                }
                for (Integer id : entities.keySet()) {
                    if (!newEntities.containsKey(id))
                        deleted.add(id);
                }
                entities = newEntities;
            } else {
                while (count > 0) {
                    Integer id = buffer.getInt();
                    Entity entity = entities.get(id);
                    if (entity != null)
                        entity.read(buffer);
                    else
                        Entity.dropRead(buffer); //updates to entities not yet received
                    count--;
                }
            }
        }
    }

    public void load() {
        File file = new File("entities.dat");
        if (!file.canRead())
            return;
        FileInputStream in;
        try {
            in = new FileInputStream(file);
            int rem = (int) file.length();
            ByteBuffer buffer = ByteBufferBuffer.alloc(rem);
            buffer.limit(rem);
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
            read(buffer, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        File file = new File("entities.dat");
        FileOutputStream out;
        try {
            ByteBuffer buffer = ByteBufferBuffer.alloc(4 * 1024 * 1024);
            write(buffer, true, false);
            buffer.flip();
            out = new FileOutputStream(file);
            out.write(buffer.array(), buffer.arrayOffset(), buffer.remaining());
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addEntity(Entity entity) {
        while (entities.containsKey(nextId))
            nextId++;
        entity.id = nextId++;
        entities.put(entity.id, entity);
        inserted.add(entity.id);
    }

    public void clearDelta() {
        deleted.clear();
        inserted.clear();
    }

    public Entity getEntity(Integer id) {
        return entities.get(id);
    }

    public void getDwarves(ArrayList<Entity> dwarves) {
        for (Entity entity : entities.values())
            if (entity.kind == Entity.Kind.DWARF)
                dwarves.add(entity);
    }
}
