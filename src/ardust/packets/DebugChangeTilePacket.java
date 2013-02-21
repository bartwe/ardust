package ardust.packets;

import java.nio.ByteBuffer;

public class DebugChangeTilePacket extends Packet {
    public int x;
    public int y;
    public int z;

    public DebugChangeTilePacket(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public DebugChangeTilePacket(ByteBuffer buffer) {
        x = buffer.getInt();
        y = buffer.getInt();
        z = buffer.getInt();
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.put(packetId());
        buffer.putInt(x);
        buffer.putInt(y);
        buffer.putInt(z);
    }

    @Override
    public byte packetId() {
        return Packet.ID_DEBUG_CHANGE_TILE_PACKET;
    }
}
