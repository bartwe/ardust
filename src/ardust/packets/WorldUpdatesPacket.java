package ardust.packets;

import ardust.server.ServerWorld;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class WorldUpdatesPacket extends Packet {
    public int[] locations;
    public byte[] tiles;

    public WorldUpdatesPacket(ServerWorld world, int[] updates, int index, int length) {
        locations = Arrays.copyOfRange(updates, index, index + length);
        tiles = new byte[locations.length];
        world.readTiles(tiles, locations);
    }

    public WorldUpdatesPacket(ByteBuffer buffer) {
        int size = buffer.getShort();
        locations = new int[size];
        tiles = new byte[size];
        for (int i = 0; i < locations.length; i++)
            locations[i] = buffer.getInt();
        buffer.get(tiles);
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.put(packetId());
        buffer.putShort((short) locations.length);
        for (int i = 0; i < locations.length; i++)
            buffer.putInt(locations[i]);
        buffer.put(tiles);
    }

    @Override
    public byte packetId() {
        return Packet.ID_WORLD_UPDATE_PACKET;
    }
}
