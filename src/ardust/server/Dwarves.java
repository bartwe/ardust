package ardust.server;

import ardust.entities.Entities;
import ardust.entities.Entity;
import ardust.packets.DwarfRequestPacket;
import ardust.shared.Constants;
import ardust.shared.Point2;

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

                Point2 nextPosition = getPositionAfterMovement(entity);
                if (!positionalMap.isOccupied(nextPosition, null) &&
                        Constants.isWalkable(world.read(nextPosition.x, nextPosition.y))) {
                    entity.mode = Entity.Mode.WALKING;
                    entity.countdown = Constants.WALKING_COUNTDOWN;
                }

                break;

            case Mine:

                nextPosition = getPositionAfterMovement(entity);
                int mineable = Constants.isWorldPieceMineable(world.read(nextPosition.x, nextPosition.y));
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
            case Build:
                nextPosition = getPositionAfterMovement(entity);
                if (player.getStone() >= 1 && world.read(nextPosition.x, nextPosition.y) == 0) {
                     player.addStone(-1);
                    world.write(nextPosition.x, nextPosition.y,Constants.STONE_WALL);
                }

                break;

            case Attack:

                System.err.println("Attack mode engaged.");

                nextPosition = getPositionAfterMovement(entity);
                Entity targetEntity = positionalMap.getEntity(nextPosition);
                if (targetEntity != null) {
                    if (targetEntity.playerId != entity.playerId) {
                        entity.mode = Entity.Mode.ATTACK;
                        entity.countdown = Constants.FIGHT_COUNTDOWN;
                    }
                }

                break;

            default:
                entity.mode = Entity.Mode.IDLE;
                break;
        }
    }

    public static Point2 getPositionAfterMovement(Entity entity) {
        switch (entity.orientation) {
            case NORTH:
                return new Point2(entity.position.x, entity.position.y - 1);
            case EAST:
                return new Point2(entity.position.x + 1, entity.position.y);
            case SOUTH:
                return new Point2(entity.position.x, entity.position.y + 1);
            case WEST:
                return new Point2(entity.position.x - 1, entity.position.y);
            default:
                return new Point2(entity.position.x, entity.position.y);
        }
    }

    static Point2 tempPosition = new Point2();

    public static void tick(int deltaT, Player player, Entity dwarf, ServerWorld world, PositionalMap positionalMap, Entities entities) {
        if (dwarf.kind != Entity.Kind.DWARF)
            throw new RuntimeException();

        if (player == null)
            dwarf.mode = Entity.Mode.DEAD;

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
                    if (Constants.isWalkable(world.read(tempPosition.x, tempPosition.y)))
                        dwarf.position.move(dwarf.orientation);
                dwarf.mode = Entity.Mode.IDLE;
                break;
            case MINING:
                Point2 position = getPositionAfterMovement(dwarf);
                byte which = world.read(position.x, position.y);
                world.write(position.x, position.y, (byte) 0);
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

            case ATTACK:
                position = getPositionAfterMovement(dwarf);
                Entity targetEntity = positionalMap.getEntity(position);
                if (targetEntity != null) {
                    if (targetEntity.playerId != dwarf.playerId) {
                        targetEntity.takeDamage(Constants.damageForWeapon(dwarf.weapon));
                    }
                }

                dwarf.mode = Entity.Mode.IDLE;
                break;

            case DEAD:
                entities.removeEntity(dwarf);
                break;

            case COOLDOWN:
                dwarf.mode = Entity.Mode.IDLE;
                break;
            default:
                throw new RuntimeException();
        }

    }
}
