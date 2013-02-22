package ardust.client;

import ardust.entities.Entities;
import ardust.shared.Point3;

import java.util.HashMap;

public class Characters {
    HashMap<Integer, Character> mapping = new HashMap<Integer, Character>();
    private final Entities entities;
    HashMap<Point3, Character> positional = new HashMap<Point3, Character>();

    public Characters(Entities entities) {
          this.entities = entities;
    }

    public void tick(int deltaT, ClientWorld world) {
        for (Integer id: entities.deleted)
            mapping.remove(id);
        for (Integer id: entities.inserted)
            mapping.put(id, new Character(entities.getEntity(id)));
        entities.clearDelta();
        positional.clear();
        for (Character character : mapping.values()) {
            character.tick(deltaT, world);
            positional.put(character.location, character);
            if (!character.targetLocation.equals(character.location))
	            positional.put(character.targetLocation, character);
        }
    }

    Point3 tempPoint = new Point3();

    public Character getCharacterAtTile(int x, int y, int z) {
        tempPoint.set(x, y, z);
        return positional.get(tempPoint);
    }

    public HashMap<Point3, Character> charactersByPosition() {
        return positional;
    }

}
