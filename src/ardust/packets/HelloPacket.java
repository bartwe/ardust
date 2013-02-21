package ardust.packets;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class HelloPacket extends Packet {
    String name;


    static Charset Utf8Charset = Charset.forName("UTF-8");

    public HelloPacket(String name) {
        this.name = name;
        if (name.length() > 255)
            throw new RuntimeException("Too long username");
    }

    public HelloPacket(ByteBuffer buffer) {
        int size = buffer.getShort();
        byte[] nameBytes = new byte[size];
        buffer.get(nameBytes);
        name = new String(nameBytes, Utf8Charset);
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.put(packetId());
        byte[] nameBytes = name.getBytes(Utf8Charset);
        buffer.putShort((short) nameBytes.length);
        buffer.put(nameBytes);
    }

    @Override
    byte packetId() {
        return Packet.ID_HELLO_PACKET;
    }
}
