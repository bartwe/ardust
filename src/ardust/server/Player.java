package ardust.server;

import ardust.entities.Entities;
import ardust.entities.Entity;
import ardust.packets.Packet;
import ardust.shared.NetworkConnection;

public class Player {
    String name;
    NetworkConnection connection;
    private int x;
    private int y;
    private int z;

    public Player(NetworkConnection networkConnection) {
        connection = networkConnection;
        name = "Unknown-" + hashCode();
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

        entities.addEntity(new Entity(Entity.Kind.DWARF, x + 2, y + -2));
        entities.addEntity(new Entity(Entity.Kind.DWARF, x + 2, y + 2));
        entities.addEntity(new Entity(Entity.Kind.DWARF, x + -2, y + 2));
        entities.addEntity(new Entity(Entity.Kind.DWARF, x + -2, y + -2));
    }
}
