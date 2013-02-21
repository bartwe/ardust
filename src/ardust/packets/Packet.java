package ardust.packets;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

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



    static Charset Utf8Charset = Charset.forName("UTF-8");

    public static String readString(ByteBuffer buffer) {
        int size = buffer.getShort();
        byte[] bytes = new byte[size];
        buffer.get(bytes);
        return new String(bytes, Utf8Charset);
    }

    public static void writeString(ByteBuffer buffer, String text) {
        byte[] nameBytes = text.getBytes(Utf8Charset);
        buffer.putShort((short) nameBytes.length);
        buffer.put(nameBytes);
    }
}
