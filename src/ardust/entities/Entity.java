package ardust.entities;

import ardust.shared.Constants;
import ardust.shared.Orientation;
import ardust.shared.Point2;
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
        MINING,
        ATTACK,
        DEAD
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
    public Point2 position = new Point2();
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

    public Entity(Integer playerId, Kind kind, int x, int y) {
        values = new Values(Constants.V_ENTITY_VALUES_SIZE);
        this.playerId = playerId;
        this.kind = kind;
        position.set(x, y);
    }

    public boolean write(ByteBuffer buffer, boolean all) {
        values.set(Constants.V_ENTITY_POS_X, position.x);
        values.set(Constants.V_ENTITY_POS_Y, position.y);
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
        position.set(values.get(Constants.V_ENTITY_POS_X), values.get(Constants.V_ENTITY_POS_Y));
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

    public void takeDamage(int damage) {
        if (armor == Armor.IRON)
            damage /= 2;
        health -= damage;
        if (health < 0)
            health = 0;
        mode = Mode.DEAD;
        countdown = Constants.DEAD_COUNTDOWN;
    }
}
