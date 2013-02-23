package ardust.packets;

import ardust.shared.ByteBufferBuffer;

import java.nio.ByteBuffer;

public class EntitiesPacket extends Packet {
    public boolean checkpoint;
    public ByteBuffer data;

    public EntitiesPacket(ByteBuffer buffer, boolean tosend, boolean checkpoint) {
        this.checkpoint = checkpoint;
        if (buffer.remaining() > 20000)
            throw new RuntimeException("Too long of entity delta");
        if (buffer.remaining() == 0)
            throw new RuntimeException("Whuh?");
        int size = buffer.remaining();
        data = ByteBufferBuffer.alloc(size);
        buffer.get(data.array(), data.arrayOffset(), size);
    }

    public EntitiesPacket(ByteBuffer buffer) {
        checkpoint = buffer.get() != 0;
        int size = buffer.getShort();
        data = ByteBufferBuffer.alloc(size);
        buffer.get(data.array(), data.arrayOffset(), size);
        data.limit(size);
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.put(packetId());
        buffer.put((byte)(checkpoint?1:0));
        buffer.putShort((short)data.remaining());
        buffer.put(data.array(), data.arrayOffset()+ data.position(), data.remaining());
    }

    @Override
    public byte packetId() {
        return Packet.ID_ENTITIES_PACKET;
    }
}