package ardust.server;

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
}
