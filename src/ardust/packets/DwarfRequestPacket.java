package ardust.packets;

import ardust.shared.DwarfRequest;
import ardust.shared.Orientation;

import java.nio.ByteBuffer;

public class DwarfRequestPacket extends Packet {
    public int id;
    public DwarfRequest request;
    public Orientation orientation;

    public DwarfRequestPacket(Integer id, DwarfRequest request, Orientation orientation) {
        this.id = id;
        this.request = request;
        this.orientation = orientation;
    }

    public DwarfRequestPacket(ByteBuffer buffer) {
        id = buffer.getInt();
        request = DwarfRequest.values()[buffer.get()];
        orientation = Orientation.values()[buffer.get()];
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.put(packetId());
        buffer.putInt(id);
        buffer.put((byte) request.ordinal());
        buffer.put((byte) orientation.ordinal());
    }

    @Override
    public byte packetId() {
        return Packet.ID_DWARF_REQUEST_PACKET;
    }
}
