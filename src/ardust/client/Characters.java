package ardust.client;

import ardust.entities.Entities;
import ardust.shared.NetworkConnection;
import ardust.shared.Point3;

import java.util.*;

public class Characters {
    HashMap<Integer, Character> mapping = new HashMap<Integer, Character>();
    private final Entities entities;
    HashMap<Point3, Character> positional = new HashMap<Point3, Character>();

    public Characters(Entities entities) {
        this.entities = entities;
    }

    public void tick(int deltaT, World world, NetworkConnection network, GameCore core) {
        for (Integer id : entities.deleted)
            mapping.remove(id);
        for (Integer id : entities.inserted)
            mapping.put(id, new Character(entities.getEntity(id)));
        entities.clearDelta();
        positional.clear();
        for (Character character : mapping.values()) {
            character.tick(deltaT, world, network, core);
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

    public Character nextCharacter(int playerId, Character selectedDwarf) {
        if (mapping.isEmpty())
            return null;

        ArrayList<Character> options = new ArrayList<Character>();

        for (Character character: mapping.values())
            if (character.playerId() == playerId)
                options.add(character);

        if (options.size() == 0)
            return null;

        if ((selectedDwarf != null) && (!mapping.containsKey(selectedDwarf.id())))
            selectedDwarf = null;

        if (selectedDwarf == null) {
            Random random = new Random();
            return options.get(random.nextInt(options.size()));
        }

        Collections.sort(options, new Comparator<Character>() {
            public int compare(Character o1, Character o2) {
                return o1.id().compareTo(o2.id());
            }
        });

        int idx = 0;

        for (int i = 0; i < options.size(); ++i) {
            if (options.get(i).id().equals(selectedDwarf.id()))
                idx = i;
        }
        idx = (idx + 1) % options.size();

        return options.get(idx);
    }

    public boolean containsId(Integer id) {
        return mapping.containsKey(id);
    }
}
