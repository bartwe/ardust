package ardust.entities;

import ardust.shared.Constants;
import ardust.shared.Values;

import java.awt.*;
import java.nio.ByteBuffer;

public class Entity {
    public Integer id;
    public Point position = new Point();
    public Kind kind;
    Values values;

    public Entity(Integer id) {
        this.id = id;
        values = new Values(Constants.V_ENTITY_VALUES_SIZE);
    }

    public Entity(Kind kind, int x, int y) {
        values = new Values(Constants.V_ENTITY_VALUES_SIZE);
        this.kind = kind;
        position.setLocation(x, y);
    }

    public boolean write(ByteBuffer buffer, boolean all) {
        values.set(Constants.V_ENTITY_POS_X, (int) position.getX());
        values.set(Constants.V_ENTITY_POS_Y, (int) position.getY());
        values.set(Constants.V_ENTITY_KIND, kind.ordinal());

        return values.write(buffer, all);
    }

    public void read(ByteBuffer buffer) {
        values.read(buffer);
        position.setLocation(values.get(Constants.V_ENTITY_POS_X), values.get(Constants.V_ENTITY_POS_Y));
        kind = Kind.values()[values.get(Constants.V_ENTITY_KIND)];
    }

    public void postWrite() {
        values.nextTick();
    }

    public enum Kind {
        DWARF
    }
}
