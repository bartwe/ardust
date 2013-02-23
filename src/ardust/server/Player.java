package ardust.server;

import ardust.entities.Entities;
import ardust.entities.Entity;
import ardust.packets.Packet;
import ardust.packets.PlayerUpdatePacket;
import ardust.shared.ByteBufferBuffer;
import ardust.shared.Constants;
import ardust.shared.NetworkConnection;
import ardust.shared.Values;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class Player {
    String name;
    NetworkConnection connection;
    private int x;
    private int y;
    private int z;
    Values values;
    public HashMap<Integer, Entity> dwarfs = new HashMap<Integer, Entity>();

    public Player(NetworkConnection networkConnection) {
        connection = networkConnection;
        name = "Unknown-" + hashCode();
        values = new Values(Constants.V_PLAYER_VALUES_SIZE);
        values.set(Constants.V_PLAYER_STONES, 0);
        values.set(Constants.V_PLAYER_IRON, 2);
        values.set(Constants.V_PLAYER_GOLD, 1);
    }

    public boolean isValid() {
        return connection.isValid();
    }

    public boolean hasPacket() {
        return connection.hasInboundPackets();
    }

    public Packet nextPacket() {
        return connection.nextInboundPacket();
    }

    public void sendPacket(Packet packet) {
        connection.send(packet);
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setXYZ(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getZ() {
        return z;
    }

    public void spawnSetup(Entities entities, ServerWorld world) {

        final int b = 5;
        for (int yy = -b; yy <= b; yy++)
            for (int xx = -b; xx <= b; xx++) {
                byte tile = 0;
                if ((yy == b) || (yy == -b) || (xx == b) || (xx == -b))
                    tile = 2;
                world.writeDirect(xx + x, yy + y, z, tile);
            }



        addDwarf(entities, x + 2, y + -2, z);
        addDwarf(entities, x + 2, y + 2, z);
        world.writeDirect(x, y, z, (byte) 8); // anvil
        addDwarf(entities, x + -2, y + 2, z);
        addDwarf(entities, x + -2, y + -2, z);
    }

    public void addDwarf(Entities entities, int x, int y, int z) {
        Entity entity = new Entity(Entity.Kind.DWARF, x, y, z);
        entities.addEntity(entity);
        dwarfs.put(entity.id, entity);
    }

    public void disconnect() {
        connection.stop();
    }

    public void addStone(int q) {
        values.set(Constants.V_PLAYER_STONES, values.get(Constants.V_PLAYER_STONES) + q);
    }

    public void addIron(int q) {
        values.set(Constants.V_PLAYER_IRON, values.get(Constants.V_PLAYER_IRON) + q);
    }

    public void addGold(int q) {
        values.set(Constants.V_PLAYER_GOLD, values.get(Constants.V_PLAYER_GOLD) + q);
    }


    ByteBuffer buffer = ByteBufferBuffer.alloc(16000);

    public void sendUpdates() {
        buffer.clear();
        if (!values.write(buffer, false))
            return;
        buffer.flip();
        sendPacket(new PlayerUpdatePacket(buffer, true));
    }
}
