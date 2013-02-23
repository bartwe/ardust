package ardust.packets;

import java.nio.ByteBuffer;

public class WindowPacket extends Packet {
    public int x;
    public int y;

    public WindowPacket(int x, int y) {
        this.x = x;
        this.y = y;

    }

    public WindowPacket(ByteBuffer buffer) {
        x = buffer.getInt();
        y = buffer.getInt();
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.put(packetId());
        buffer.putInt(x);
        buffer.putInt(y);
    }

    @Override
    public byte packetId() {
        return Packet.ID_WINDOW_PACKET;
    }
}
