package ardust.packets;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

abstract public class Packet {
    public static final byte ID_HELLO_PACKET = 1;
    public static final byte ID_WORLD_UPDATE_PACKET = 2;
    public static final byte ID_WINDOW_PACKET = 3;
    public static final byte ID_WORLD_REGION_PACKET = 4;
    public static final byte ID_DEBUG_CHANGE_TILE_PACKET = 5;

    public static Packet read(ByteBuffer buffer) {
        byte packetId = buffer.get();
        if (packetId == ID_HELLO_PACKET)
            return new HelloPacket(buffer);
        if (packetId == ID_WORLD_UPDATE_PACKET)
            return new WorldUpdatesPacket(buffer);
        if (packetId == ID_WINDOW_PACKET)
            return new WindowPacket(buffer);
        if (packetId == ID_WORLD_REGION_PACKET)
            return new WorldRegionPacket(buffer);
        if (packetId == ID_DEBUG_CHANGE_TILE_PACKET)
            return new DebugChangeTilePacket(buffer);
        throw new RuntimeException("Unknown packet id: "+packetId);
    }

    public abstract void write(ByteBuffer buffer);

    public abstract byte packetId();

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
