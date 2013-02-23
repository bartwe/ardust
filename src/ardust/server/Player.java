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
    Integer id;
    String name;
    NetworkConnection connection;
    private int x;
    private int y;
    Values values;
    public HashMap<Integer, Entity> dwarfs = new HashMap<Integer, Entity>();

    public Player(NetworkConnection networkConnection) {
        connection = networkConnection;
        name = "Unknown-" + hashCode();
        id = 0;
        values = new Values(Constants.V_PLAYER_VALUES_SIZE);
        values.set(Constants.V_PLAYER_STONES, 0);
        values.set(Constants.V_PLAYER_IRON, 2);
        values.set(Constants.V_PLAYER_GOLD, 1);
        values.set(Constants.V_PLAYER_ID, id);
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
        id = name.hashCode();  // oh the collisions ! the humanity !
        values.set(Constants.V_PLAYER_ID, id);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void spawnSetup(Entities entities, ServerWorld world, PositionalMap positionalMap) {

        final int b = 5;
        for (int yy = -b; yy <= b; yy++)
            for (int xx = -b; xx <= b; xx++) {
                byte tile = 0;
                if ((yy == b) || (yy == -b) || (xx == b) || (xx == -b))
                    tile = 2;
                world.write(xx + x, yy + y, tile);
            }


        addDwarf(entities, positionalMap, x + 2, y + -2);
        addDwarf(entities, positionalMap, x + 2, y + 2);
        world.write(x, y, (byte) 8); // anvil
        addDwarf(entities, positionalMap, x + -2, y + 2);
        addDwarf(entities, positionalMap, x + -2, y + -2);
    }

    public void addDwarf(Entities entities, PositionalMap positionalMap, int x, int y) {
        Entity entity = new Entity(id, Entity.Kind.DWARF, x, y);
        entities.addEntity(entity);
        positionalMap.addEntity(entity);
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

    public int getStone()
    {
        return values.get(Constants.V_PLAYER_STONES);
    }

    public int getIron()
    {
        return values.get(Constants.V_PLAYER_IRON);
    }

    public int getGold()
    {
        return values.get(Constants.V_PLAYER_GOLD);
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
