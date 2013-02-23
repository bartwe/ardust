package ardust.server;

import ardust.entities.Entities;
import ardust.entities.Entity;
import ardust.shared.Point3;

import java.util.HashMap;

public class PositionalMap {
    HashMap<Point3, Entity> occupiedSpace = new HashMap<Point3, Entity>();

    public void updateEntities(Entities entities) {
        occupiedSpace.clear();
        for (Entity entity : entities.entities.values())
            addEntity(entity);
    }

    public boolean isOccupied(Point3 position, Entity entity) {
        Entity at = occupiedSpace.get(position);
        return (at != null) && (at != entity);
    }

    public void updateEntity(Entity entity) {
        // too many spaces may be marked, probably don't care
        addEntity(entity);
    }

    public void addEntity(Entity entity) {
        Point3 current = new Point3();
        current.set(entity.position);
        occupiedSpace.put(current, entity);
        switch (entity.mode) {
            case WALKING:
            case MINING:
                Point3 next = new Point3();
                next.set(entity.position);
                next.move(entity.orientation);
                occupiedSpace.put(current, entity);
            default:
                break;
        }
    }
}
