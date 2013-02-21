package ardust.packets;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.ByteBuffer;

abstract public class Packet {
    public static final byte ID_HELLO_PACKET = 1;

    public static Packet read(ByteBuffer buffer) {
        byte packetId = buffer.get();
        if (packetId == ID_HELLO_PACKET)
            return new HelloPacket(buffer);
        throw new NotImplementedException();
    }

    public abstract void write(ByteBuffer buffer);

    abstract byte packetId();
}
