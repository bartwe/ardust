package ardust.entities;

import ardust.shared.Constants;
import ardust.shared.Orientation;
import ardust.shared.Point3;
import ardust.shared.Values;

import java.nio.ByteBuffer;

public class Entity {
    // not serialized
    public int countdown;

    public enum Kind {
        DWARF
    }

    public enum Mode {
        IDLE,
        COOLDOWN, // like idle but non interruptable
        WALKING,
        MINING
    }

    public enum Armor
    {
        NONE,
        IRON
    }


    public enum Weapon
    {
       NONE,
       IRON_SWORD,
       GOLD_SWORD
    }

    public Integer id;
    public Integer playerId;
    public Point3 position = new Point3();
    public Kind kind = Kind.DWARF;
    public Orientation orientation = Orientation.SOUTH;
    public Mode mode = Mode.IDLE;
    public int health = 3;
    public Armor armor = Armor.NONE;
    public Weapon weapon = Weapon.NONE;
    Values values;

    public Entity(Integer id) {
        this.id = id;
        values = new Values(Constants.V_ENTITY_VALUES_SIZE);
    }

    public Entity(Integer playerId, Kind kind, int x, int y, int z) {
        values = new Values(Constants.V_ENTITY_VALUES_SIZE);
        this.playerId = playerId;
        this.kind = kind;
        position.set(x, y, z);
    }

    public boolean write(ByteBuffer buffer, boolean all) {
        values.set(Constants.V_ENTITY_POS_X, position.x);
        values.set(Constants.V_ENTITY_POS_Y, position.y);
        values.set(Constants.V_ENTITY_POS_Z, position.z);
        values.set(Constants.V_ENTITY_KIND, kind.ordinal());
        values.set(Constants.V_ENTITY_ORIENTATION, orientation.ordinal());
        values.set(Constants.V_ENTITY_MODE, mode.ordinal());
        values.set(Constants.V_ENTITY_HEALTH, health);
        values.set(Constants.V_ENTITY_PLAYER, playerId);
        values.set(Constants.V_ENTITY_ARMOR, armor.ordinal());
        values.set(Constants.V_ENTITY_SWORD, weapon.ordinal());
        return values.write(buffer, all);
    }

    public void read(ByteBuffer buffer) {
        values.read(buffer);
        position.set(values.get(Constants.V_ENTITY_POS_X), values.get(Constants.V_ENTITY_POS_Y), values.get(Constants.V_ENTITY_POS_Z));
        kind = Kind.values()[values.get(Constants.V_ENTITY_KIND)];
        orientation = Orientation.values()[values.get(Constants.V_ENTITY_ORIENTATION)];
        mode = Mode.values()[values.get(Constants.V_ENTITY_MODE)];
        health = values.get(Constants.V_ENTITY_HEALTH);
        playerId = values.get(Constants.V_ENTITY_PLAYER);
        armor = Armor.values()[values.get(Constants.V_ENTITY_ARMOR)];
        weapon = Weapon.values()[values.get(Constants.V_ENTITY_SWORD)];
    }

    public static void dropRead(ByteBuffer buffer) {
        Values.dropRead(buffer, Constants.V_ENTITY_VALUES_SIZE);
    }

    public void postWrite() {
        values.nextTick();
    }

}
