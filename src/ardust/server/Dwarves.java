package ardust.server;

import ardust.entities.Entity;
import ardust.packets.DwarfRequestPacket;
import ardust.shared.Constants;

public class Dwarves {
    public static void handle(Entity entity, DwarfRequestPacket packet) {
        if (entity.kind != Entity.Kind.DWARF)
            throw new RuntimeException();

        if (entity.mode != Entity.Mode.IDLE)
            return;

        entity.orientation = packet.orientation;

        //pick mode
        switch (packet.request) {
            case Walk:
                entity.mode = Entity.Mode.WALKING;
                entity.countdown = Constants.WALKING_COUNTDOWN;
                break;
            default:
                entity.mode = Entity.Mode.IDLE;
                break;
        }
    }

    public static void tick(int deltaT, Entity dwarf) {
        if (dwarf.kind != Entity.Kind.DWARF)
            throw new RuntimeException();

        if (dwarf.mode == Entity.Mode.IDLE)
            return;

        dwarf.countdown -= deltaT;
        if (dwarf.countdown < 0)
            dwarf.countdown = 0;

        if (dwarf.countdown != 0)
            return;

        switch (dwarf.mode) {
            case WALKING:
                switch (dwarf.orientation) {
                    case NORTH:
                        dwarf.position.y -= 1;
                        break;
                    case EAST:
                        dwarf.position.x += 1;
                        break;
                    case SOUTH:
                        dwarf.position.y += 1;
                        break;
                    case WEST:
                        dwarf.position.x -= 1;
                        break;
                }
                dwarf.mode = Entity.Mode.IDLE;
                break;
            default:
                throw new RuntimeException();
        }

    }
}
