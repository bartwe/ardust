package ardust.packets;

import ardust.server.ServerWorld;
import ardust.shared.ByteBufferBuffer;
import ardust.shared.Constants;

import java.nio.ByteBuffer;

public class WorldRegionPacket extends Packet {
    ByteBuffer tileBuffer;
    int oldX, oldY, x, y;

    public WorldRegionPacket(ServerWorld world, int oldX, int oldY, int x, int y) {
        this.oldX = oldX;
        this.oldY = oldY;
        this.x = x;
        this.y = y;
        tileBuffer = ByteBufferBuffer.alloc((Constants.RADIUS * 2 + 1) * (Constants.RADIUS * 2 + 1));
        for (int yi = y - Constants.RADIUS; yi <= y + Constants.RADIUS; yi++)
            for (int xi = x - Constants.RADIUS; xi <= x + Constants.RADIUS; xi++) {
                int dx = Math.abs(xi - oldX);
                int dy = Math.abs(yi - oldY);
                if ((dx > Constants.RADIUS) || (dy > Constants.RADIUS)) {
                    tileBuffer.put(world.read(xi, yi));
                }
            }
    }

    public WorldRegionPacket(ByteBuffer buffer) {
        oldX = buffer.getInt();
        oldY = buffer.getInt();
        x = buffer.getInt();
        y = buffer.getInt();
        int size = buffer.getShort();
        tileBuffer = ByteBufferBuffer.alloc(size);
        tileBuffer.put(buffer);
    }

    //todo, apply phase


    @Override
    public byte packetId() {
        return Packet.ID_WORLD_REGION_PACKET;
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.put(packetId());
        buffer.putInt(oldX);
        buffer.putInt(oldY);
        buffer.putInt(x);
        buffer.putInt(y);
        buffer.putShort((short) tileBuffer.position());
        buffer.put(tileBuffer.array(), tileBuffer.arrayOffset(), tileBuffer.position());
    }

    public void readUpdates(int[] locations, byte[] tiles) {
        System.arraycopy(tileBuffer.array(), tileBuffer.arrayOffset(), tiles, 0, tileBuffer.position());
        int index = 0;
        for (int yi = y - Constants.RADIUS; yi <= y + Constants.RADIUS; yi++)
            for (int xi = x - Constants.RADIUS; xi <= x + Constants.RADIUS; xi++) {
                int dx = Math.abs(xi - oldX);
                int dy = Math.abs(yi - oldY);

                if ((dx > Constants.RADIUS) || (dy > Constants.RADIUS)) {
                    locations[index] = ServerWorld.normalizeAxis(xi) + ServerWorld.normalizeAxis(yi) * Constants.WORLD_LENGTH;
                    index++;
                }

            }
        if (index != tileBuffer.position())
            throw new RuntimeException("wut");
    }

    public int entries() {
        return tileBuffer.position();
    }
}
