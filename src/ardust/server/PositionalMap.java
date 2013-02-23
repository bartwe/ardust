package ardust.server;

import ardust.entities.Entities;
import ardust.entities.Entity;
import ardust.shared.Point2;

import java.util.HashMap;

public class PositionalMap {
    HashMap<Point2, Entity> occupiedSpace = new HashMap<Point2, Entity>();

    public void updateEntities(Entities entities) {
        occupiedSpace.clear();
        for (Entity entity : entities.entities.values())
            addEntity(entity);
    }

    public boolean isOccupied(Point2 position, Entity entity) {
        Entity at = occupiedSpace.get(position);
        return (at != null) && (at != entity);
    }

    public void updateEntity(Entity entity) {
        // too many spaces may be marked, probably don't care
        addEntity(entity);
    }

    public void addEntity(Entity entity) {
        Point2 current = new Point2();
        current.set(entity.position);
        occupiedSpace.put(current, entity);
        switch (entity.mode) {
            case WALKING:
            case MINING:
                Point2 next = new Point2();
                next.set(entity.position);
                next.move(entity.orientation);
                occupiedSpace.put(current, entity);
            default:
                break;
        }
    }

    public Entity getEntity(Point2 nextPosition) {
        return occupiedSpace.get(nextPosition);
    }
}
