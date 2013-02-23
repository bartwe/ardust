package ardust.server;

import ardust.entities.Entity;
import ardust.packets.DwarfRequestPacket;
import ardust.shared.Constants;
import ardust.shared.Point3;

public class Dwarves {
    public static void handle(Entity entity, DwarfRequestPacket packet, ServerWorld world) {
        if (entity.kind != Entity.Kind.DWARF)
            throw new RuntimeException();

        if (entity.mode != Entity.Mode.IDLE)
            return;

        entity.orientation = packet.orientation;

        //pick mode
        switch (packet.request) {
            case Walk:

                Point3 nextPosition = getPositionAfterMovement(entity);
                if (world.readDirect(nextPosition.x, nextPosition.y, nextPosition.z) == 0) {
                    entity.mode = Entity.Mode.WALKING;
                    entity.countdown = Constants.WALKING_COUNTDOWN;
                }

                break;

            case Mine:

                nextPosition = getPositionAfterMovement(entity);
                int mineable = Constants.isWorldPieceMineable(world.readDirect(nextPosition.x, nextPosition.y, nextPosition.z));
                if (mineable > 0) {
                    entity.mode = Entity.Mode.MINING;
                    entity.countdown = mineable;
                }

                break;

            default:
                entity.mode = Entity.Mode.IDLE;
                break;
        }
    }

    public static Point3 getPositionAfterMovement(Entity entity) {
        switch (entity.orientation) {
            case NORTH:
                return new Point3(entity.position.x, entity.position.y - 1, entity.position.z);
            case EAST:
                return new Point3(entity.position.x + 1, entity.position.y, entity.position.z);
            case SOUTH:
                return new Point3(entity.position.x, entity.position.y + 1, entity.position.z);
            case WEST:
                return new Point3(entity.position.x - 1, entity.position.y, entity.position.z);
        }
        throw new RuntimeException();
    }

    public static void tick(int deltaT, Entity dwarf, ServerWorld world) {
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
            case MINING:
                Point3 position = getPositionAfterMovement(dwarf);
                byte which = world.readDirect(position.x, position.y, position.z);
                world.writeDirect(position.x, position.y, position.z, (byte) 0);
                switch (which) {
                    case Constants.STONE:
                        dwarf.mode = Entity.Mode.RESOURCE_STONE;
                        dwarf.countdown = 100;
                        break;
                    case Constants.IRON:
                        dwarf.mode = Entity.Mode.RESOURCE_IRON;
                        dwarf.countdown = 100;
                        break;
                    case Constants.GOLD:
                        dwarf.mode = Entity.Mode.RESOURCE_GOLD;
                        dwarf.countdown = 100;
                        break;
                    default:
                        dwarf.mode = Entity.Mode.IDLE;
                        break;
                }
                break;

            case RESOURCE_STONE:
            case RESOURCE_IRON:
            case RESOURCE_GOLD:
            case COOLDOWN:
                dwarf.mode = Entity.Mode.IDLE;
                break;
            default:
                throw new RuntimeException();
        }

    }
}
