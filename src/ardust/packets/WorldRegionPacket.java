package ardust.packets;

import ardust.server.ServerWorld;
import ardust.shared.Constants;

import java.nio.ByteBuffer;

public class WorldRegionPacket extends Packet {
    ByteBuffer tileBuffer;
    int oldX, oldY, oldZ, x, y, z;

    public WorldRegionPacket(ServerWorld world, int oldX, int oldY, int oldZ, int x, int y, int z) {
        this.oldX = oldX;
        this.oldY = oldY;
        this.oldZ = oldZ;
        this.x = x;
        this.y = y;
        this.z = z;
        tileBuffer = ByteBuffer.allocate((Constants.ZRADIUS * 2 + 1) * (Constants.RADIUS * 2 + 1) * (Constants.RADIUS * 2 + 1));
        for (int zi = z - Constants.ZRADIUS; zi <= z + Constants.ZRADIUS; zi++)
            for (int yi = y - Constants.RADIUS; yi <= y + Constants.RADIUS; yi++)
                for (int xi = x - Constants.RADIUS; xi <= x + Constants.RADIUS; xi++) {
                    int dx = Math.abs(xi - oldX);
                    int dy = Math.abs(yi - oldY);
                    int dz = Math.abs(zi - oldZ);
                    if ((dx > Constants.RADIUS) || (dy > Constants.RADIUS) || (dz > Constants.ZRADIUS)) {
                        tileBuffer.put(world.readDirect(xi, yi, zi));
                    }
                }
    }

    public WorldRegionPacket(ByteBuffer buffer) {
        oldX = buffer.getInt();
        oldY = buffer.getInt();
        oldZ = buffer.getInt();
        x = buffer.getInt();
        y = buffer.getInt();
        z = buffer.getInt();
        int size = buffer.getShort();
        tileBuffer = ByteBuffer.allocate(size);
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
        buffer.putInt(oldZ);
        buffer.putInt(x);
        buffer.putInt(y);
        buffer.putInt(z);
        buffer.putShort((short) tileBuffer.position());
        buffer.put(tileBuffer.array(), tileBuffer.arrayOffset(), tileBuffer.position());
    }

    public void readUpdates(int[] locations, byte[] tiles) {
        System.arraycopy(tileBuffer.array(), tileBuffer.arrayOffset(), tiles, 0, tileBuffer.position());
        int index = 0;
        for (int zi = z - Constants.ZRADIUS; zi <= z + Constants.ZRADIUS; zi++)
            for (int yi = y - Constants.RADIUS; yi <= y + Constants.RADIUS; yi++)
                for (int xi = x - Constants.RADIUS; xi <= x + Constants.RADIUS; xi++) {
                    int dx = Math.abs(xi - oldX);
                    int dy = Math.abs(yi - oldY);
                    int dz = Math.abs(zi - oldZ);
                    if ((dx > Constants.RADIUS) || (dy > Constants.RADIUS) || (dz > Constants.ZRADIUS)) {
                        locations[index] = xi + (yi + zi * Constants.WORLD_LENGTH) * Constants.WORLD_LENGTH;
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
