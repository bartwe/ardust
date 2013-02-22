package ardust.packets;

import java.nio.ByteBuffer;

public class HelloPacket extends Packet {
    String name;

    public HelloPacket(String name) {
        this.name = name;
        if (name.length() > 255)
            throw new RuntimeException("Too long username");
    }

    public HelloPacket(ByteBuffer buffer) {
        name = readString(buffer);
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.put(packetId());
        writeString(buffer, name);
    }

    @Override
    public byte packetId() {
        return Packet.ID_HELLO_PACKET;
    }

    public String getName() {
        return name;
    }
}
