package ardust.packets;

import java.nio.ByteBuffer;

public class WindowPacket extends Packet {
    public int x;
    public int y;
    public int z;

    public WindowPacket(ByteBuffer buffer) {
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
        return Packet.ID_WINDOW_PACKET;
    }
}
