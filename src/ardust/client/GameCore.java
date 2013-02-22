package ardust.client;

import ardust.entities.Entities;
import ardust.packets.*;
import ardust.shared.Constants;
import ardust.shared.NameGenerator;
import ardust.shared.NetworkConnection;

import java.awt.*;

public class GameCore {
    private final NetworkConnection network;
    private final Input input;
    private Painter painter;
    private final World world;
    private Entities entities;

    Character selectedDwarf;
    String name;
    private final GameLoop parent;
    int zLayer;

    public GameCore(GameLoop parent, NetworkConnection network, Input input, Painter painter) {
        if (parent == null)
            throw new IllegalArgumentException("parent");
        if (network == null)
            throw new IllegalArgumentException("network");
        if (input == null)
            throw new IllegalArgumentException("input");
        if (painter == null)
            throw new IllegalArgumentException("painter");
        this.parent = parent;
        this.network = network;
        this.input = input;
        this.painter = painter;
        this.world = new World();
        this.entities = new Entities();
        name = NameGenerator.next();
    }

    public void start() {
        network.send(new HelloPacket(name));
    }

    public void stop() {

    }

    public void tick() {
        processNetwork();

        mousePan();

        world.tick();
    }

    Point temp = new Point(); //javawut

    private void processNetwork() {
        if (!network.isValid())
            parent.fail("Network lost");

        while (network.hasInboundPackets()) {
            Packet packet = network.nextInboundPacket();
            if (packet instanceof WindowPacket) {
                WindowPacket spp = (WindowPacket)packet;
                temp.setLocation(spp.x, spp.y);
                zLayer = spp.z;
                world.worldCoordToScreenCoord(temp, temp);
                System.err.println("Force viewport "+spp.x+":"+spp.y+":"+spp.z+" "+temp);
                parent.setViewportLocation(temp);
            } else if (packet instanceof WorldRegionPacket) {
                WorldRegionPacket wrp = (WorldRegionPacket) packet;
                int entries = wrp.entries();
                if (entries > 0) {
                    int[] locations = new int[entries];
                    byte[] tiles = new byte[entries];
                    wrp.readUpdates(locations, tiles);
                    world.writeTiles(locations, tiles);
                }
            } else if (packet instanceof WorldUpdatesPacket) {
                WorldUpdatesPacket wup = (WorldUpdatesPacket) packet;
                world.writeTiles(wup.locations, wup.tiles);
            } else if (packet instanceof EntitiesPacket) {
                EntitiesPacket ep = (EntitiesPacket) packet;
                entities.read(ep.data, false);
            } else
                throw new RuntimeException("Unknown packet: " + packet.packetId());
        }

        world.screenCoordToWorldCoord(parent.getViewportLocation(), temp);
        WindowPacket wp = new WindowPacket((int) temp.getX(), (int) temp.getY(), zLayer);
        network.send(wp);
    }

    private void mousePan() {
        //Panning around on the map
        if (input.isMouseButtonDown(1, false)) {
            parent.setCurrentMouseCursor(Constants.PANNING_CURSOR);
            int xPan = (int) Math.max(-Constants.MAP_PAN_MAX_SPEED, Math.min(((input.getX() - input.getMostRecentClick(1).x) / (double) Constants.MAP_PAN_SENSITIVITY) * Constants.MAP_PAN_MAX_SPEED, Constants.MAP_PAN_MAX_SPEED));
            int yPan = (int) Math.max(-Constants.MAP_PAN_MAX_SPEED, Math.min(((input.getY() - input.getMostRecentClick(1).y) / (double) Constants.MAP_PAN_SENSITIVITY) * Constants.MAP_PAN_MAX_SPEED, Constants.MAP_PAN_MAX_SPEED));

            parent.setViewportLocation(new Point(parent.getViewportLocation().x + xPan, parent.getViewportLocation().y + yPan));
        } else
            parent.setCurrentMouseCursor(Constants.DEFAULT_CURSOR);

        if (input.isMouseButtonDown(0, true)) {
            World.localCoordToGlobalTile(input.getX(), input.getY(), parent.getViewportLocation(), temp);
            if (selectedDwarf == null || world.getCharacterAtTile(temp.x, temp.y, zLayer) != null) {
                selectedDwarf = world.getCharacterAtTile(temp.x, temp.y, zLayer);
            } else {
                selectedDwarf.setMovingBasedOnTileDifferential(temp.x, temp.y, world);
            }
        }
    }

    public void render() {
        World.localCoordToGlobalTile(input.getX(), input.getY(), parent.getViewportLocation(), temp);
        world.draw(painter, parent.getViewportLocation(), zLayer, painter.getDrawableWidth(), painter.getDrawableHeight(), selectedDwarf, temp.x, temp.y, zLayer);
    }
}
