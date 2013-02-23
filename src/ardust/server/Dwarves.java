package ardust.server;

import ardust.entities.Entity;
import ardust.packets.DwarfRequestPacket;
import ardust.shared.Constants;
import ardust.shared.Point3;

public class Dwarves {
    public static void handle(Entity entity, DwarfRequestPacket packet, ServerWorld world, PositionalMap positionalMap, Player player) {
        if (entity.kind != Entity.Kind.DWARF)
            throw new RuntimeException();

        if (entity.mode != Entity.Mode.IDLE)
            return;

        entity.orientation = packet.orientation;

        //pick mode
        switch (packet.request) {
            case Walk:

                Point3 nextPosition = getPositionAfterMovement(entity);
                if (!positionalMap.isOccupied(nextPosition, null) &&
                        Constants.isWalkable(world.read(nextPosition.x, nextPosition.y, nextPosition.z))) {
                    entity.mode = Entity.Mode.WALKING;
                    entity.countdown = Constants.WALKING_COUNTDOWN;
                }

                break;

            case Mine:

                nextPosition = getPositionAfterMovement(entity);
                int mineable = Constants.isWorldPieceMineable(world.read(nextPosition.x, nextPosition.y, nextPosition.z));
                if (mineable > 0) {
                    entity.mode = Entity.Mode.MINING;
                    entity.countdown = mineable;
                }

                break;
            case CraftArmor:

                if (player.getIron() >= 5)
                {
                    player.addIron(-5);
                    entity.armor = Entity.Armor.IRON;
                }

                break;
            case CraftSword:

                if (player.getIron() >= 5)
                {
                    player.addIron(-5);
                    entity.weapon = Entity.Weapon.IRON_SWORD;
                }

                break;
            case CraftGoldSword:

                if (player.getGold() >= 5 && entity.weapon == Entity.Weapon.IRON_SWORD)
                {
                    player.addGold(-5);
                    entity.weapon = Entity.Weapon.GOLD_SWORD;
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
            default:
                return new Point3(entity.position.x, entity.position.y, entity.position.z);
        }
    }

    static Point3 tempPosition = new Point3();

    public static void tick(int deltaT, Player player, Entity dwarf, ServerWorld world, PositionalMap positionalMap) {
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
                tempPosition.set(dwarf.position);
                tempPosition.move(dwarf.orientation);
                if (!positionalMap.isOccupied(tempPosition, dwarf))
                    if (Constants.isWalkable(world.read(tempPosition.x, tempPosition.y, tempPosition.z)))
                        dwarf.position.move(dwarf.orientation);
                dwarf.mode = Entity.Mode.IDLE;
                break;
            case MINING:
                Point3 position = getPositionAfterMovement(dwarf);
                byte which = world.read(position.x, position.y, position.z);
                world.write(position.x, position.y, position.z, (byte) 0);
                if (player != null) {
                    switch (which) {
                        case Constants.STONE:
                            player.addStone(1);
                            break;
                        case Constants.IRON:
                            player.addIron(1);
                            break;
                        case Constants.GOLD:
                            player.addGold(1);
                            break;
                        default:
                            break;
                    }
                }
                dwarf.mode = Entity.Mode.IDLE;
                break;

            case COOLDOWN:
                dwarf.mode = Entity.Mode.IDLE;
                break;
            default:
                throw new RuntimeException();
        }

    }
}
