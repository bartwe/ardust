package ardust.entities;

import ardust.shared.Constants;
import ardust.shared.Values;

import java.awt.*;
import java.nio.ByteBuffer;

public class Entity {
    public Integer id;
    public Point position;
    Values values;

    public Entity(Integer id) {
        this.id = id;
        values = new Values(Constants.V_ENTITY_VALUES_SIZE);
    }

    public boolean write(ByteBuffer buffer, boolean all) {
        values.set(Constants.V_ENTITY_POS_X, (int) position.getX());
        values.set(Constants.V_ENTITY_POS_Y, (int) position.getY());

        return values.write(buffer, all);
    }

    public void read(ByteBuffer buffer) {
        values.read(buffer);
        position.setLocation(values.get(Constants.V_ENTITY_POS_X), values.get(Constants.V_ENTITY_POS_Y));
    }

    public void postWrite() {
        values.nextTick();
    }
}
