package ardust.client;

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

    String name;

    public GameCore(NetworkConnection network, Input input, Painter painter) {
        this.network = network;
        this.input = input;
        this.painter = painter;
        this.world = new World();
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

    }

    Point temp = new Point(); //javawut

    private void processNetwork() {
        while (network.hasInboundPackets()) {
            Packet packet = network.nextInboundPacket();
            if (packet instanceof WorldRegionPacket) {
                WorldRegionPacket wrp = (WorldRegionPacket)packet;
                int entries = wrp.entries();
                if (entries > 0) {
                    int[] locations = new int[entries];
                    byte[] tiles = new byte[entries];
                    wrp.readUpdates(locations, tiles);
                    world.writeTiles(locations, tiles);
                }
            }
            else
            if (packet instanceof WorldUpdatesPacket) {
                WorldUpdatesPacket wup = (WorldUpdatesPacket)packet;
                world.writeTiles(wup.locations, wup.tiles);
            }
            else
                throw new RuntimeException("Unknown packet: "+ packet.packetId());
        }

        world.screenCoordToWorldCoord(GameLoop.getViewportLocation(), temp);
        System.err.println(temp);
        WindowPacket wp = new WindowPacket((int)temp.getX(), (int)temp.getY(), Constants.DUMMY_Z);
        network.send(wp);
    }

    private void mousePan() {
        //Panning around on the map
        if (input.isMouseButtonDown(1, false)) {
            GameLoop.setCurrentMouseCursor(Constants.PANNING_CURSOR);
            int xPan = (int) Math.max(-Constants.MAP_PAN_MAX_SPEED, Math.min(((input.getX() - input.getMostRecentClick(1).x) / (double) Constants.MAP_PAN_SENSITIVITY) * Constants.MAP_PAN_MAX_SPEED, Constants.MAP_PAN_MAX_SPEED));
            int yPan = (int) Math.max(-Constants.MAP_PAN_MAX_SPEED, Math.min(((input.getY() - input.getMostRecentClick(1).y) / (double) Constants.MAP_PAN_SENSITIVITY) * Constants.MAP_PAN_MAX_SPEED, Constants.MAP_PAN_MAX_SPEED));

            GameLoop.setViewportLocation(new Point(GameLoop.getViewportLocation().x + xPan,
                    GameLoop.getViewportLocation().y + yPan));
        }
        else
            GameLoop.setCurrentMouseCursor(Constants.DEFAULT_CURSOR);

    }

    public void render() {

        world.draw(painter, GameLoop.getViewportLocation(), GameLoop.getWidth(), GameLoop.getHeight());
    }
}
