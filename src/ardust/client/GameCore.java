package ardust.client;

import ardust.packets.*;
import ardust.shared.*;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class GameCore {
    private final NetworkConnection network;
    private final Input input;
    private Painter painter;
    private final World world;

    Character selectedDwarf;
    String name;
    private final GameLoop parent;
    int zLayer;
    DwarfActionMenu currentActionMenu;
    UserInputState currentInputState = UserInputState.NO_DWARF_SELECTED;
    int soundID;
    int stone, iron, gold;


    public enum UserInputState {
        NO_DWARF_SELECTED,
        DWARF_SELECTED,
        WALK,
        HALT,
        MINE
    }

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
        name = NameGenerator.next();
    }

    public void start() {
        network.send(new HelloPacket(name));
    }

    public void stop() {

    }

    public void tick(int deltaT) {
        processNetwork();

        world.tick(deltaT, network, this);

        mousePan();
    }

    Point temp = new Point(); //javawut

    private void processNetwork() {
        if (!network.isValid())
            parent.fail("Network lost");

        while (network.hasInboundPackets()) {
            Packet packet = network.nextInboundPacket();
            if (packet instanceof WindowPacket) {
                WindowPacket spp = (WindowPacket) packet;
                temp.setLocation(spp.x, spp.y);
                zLayer = spp.z;
                world.worldCoordToScreenCoord(temp, temp);
                System.err.println("Force viewport " + spp.x + ":" + spp.y + ":" + spp.z + " " + temp);
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
                world.updateEntities(ep.data, ep.checkpoint);
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
        } else if (currentInputState == UserInputState.NO_DWARF_SELECTED)
            parent.setCurrentMouseCursor(Constants.DEFAULT_CURSOR);

        if (input.isKeyDown(Keyboard.KEY_TAB, true)) {
            selectedDwarf = world.nextCharacter(selectedDwarf);
        }

        if (input.isMouseButtonDown(0, true)) {
            parent.setCurrentMouseCursor(Constants.DEFAULT_CURSOR);
            World.localCoordToGlobalTile(input.getX(), input.getY(), parent.getViewportLocation(), temp);
            if (selectedDwarf == null || world.getCharacterAtTile(temp.x, temp.y, zLayer) != null) {
                selectedDwarf = world.getCharacterAtTile(temp.x, temp.y, zLayer);
                if (selectedDwarf != null) {
                    currentActionMenu = new DwarfActionMenu(selectedDwarf);
                    currentInputState = UserInputState.DWARF_SELECTED;
                } else {
                    currentActionMenu = null;
                    currentInputState = UserInputState.NO_DWARF_SELECTED;
                }
            } else {
                // dwarf interaction stuffs
                switch (currentInputState) {
                    case DWARF_SELECTED:

                        if (currentActionMenu != null)
                            currentInputState = currentActionMenu.isButtonHere(input.getX(), input.getY(), parent.getViewportLocation());

                        if (currentInputState == UserInputState.NO_DWARF_SELECTED)
                        {
                            selectedDwarf = null;
                            currentActionMenu = null;
                        } else if (currentInputState == UserInputState.HALT) {
                            selectedDwarf.halt();
                            deselectCurrentDwarf();
                            parent.getSoundSys().play(GameMenu.buttonSoundID);
                        } else {
                            parent.setCurrentMouseCursor(Constants.ACTION_CURSOR);
                            parent.getSoundSys().play(GameMenu.buttonSoundID);
                        }

                        break;

                    case WALK:

                        sendNetworkRequestInMouseClickDirection(DwarfRequest.Walk);

                        break;

                    case MINE:

                        sendNetworkRequestInMouseClickDirection(DwarfRequest.Mine);

                        break;
                }
            }
        }

        if (selectedDwarf != null) {
            if (input.isKeyDown(Keyboard.KEY_W, false) || input.isKeyDown(Keyboard.KEY_UP, false))    {
                network.send(new DwarfRequestPacket(selectedDwarf.id(), DwarfRequest.Walk, Orientation.NORTH));
                deselectCurrentDwarf();
            }
            if (input.isKeyDown(Keyboard.KEY_D, false) || input.isKeyDown(Keyboard.KEY_RIGHT, false))        {
                network.send(new DwarfRequestPacket(selectedDwarf.id(), DwarfRequest.Walk, Orientation.EAST));
                deselectCurrentDwarf();
            }
            if (input.isKeyDown(Keyboard.KEY_S, false) || input.isKeyDown(Keyboard.KEY_DOWN, false))          {
                network.send(new DwarfRequestPacket(selectedDwarf.id(), DwarfRequest.Walk, Orientation.SOUTH));
                 deselectCurrentDwarf();
            }
            if (input.isKeyDown(Keyboard.KEY_A, false) || input.isKeyDown(Keyboard.KEY_LEFT, false))           {
                network.send(new DwarfRequestPacket(selectedDwarf.id(), DwarfRequest.Walk, Orientation.WEST));
                deselectCurrentDwarf();
            }
        }
    }

    public void deselectCurrentDwarf()
    {
        selectedDwarf = null;
        currentActionMenu = null;
        currentInputState = UserInputState.NO_DWARF_SELECTED;
        parent.setCurrentMouseCursor(Constants.DEFAULT_CURSOR);
    }

    public void sendNetworkRequestInMouseClickDirection(DwarfRequest request)
    {
        Point tile = new Point(0,0);
        World.localCoordToGlobalTile(input.getX(), input.getY(), parent.getViewportLocation(), tile);

        if (tile.x > selectedDwarf.location.x && Math.abs(tile.x - selectedDwarf.location.x) >Math.abs(tile.y - selectedDwarf.location.y)) {
            network.send(new DwarfRequestPacket(selectedDwarf.id(), request, Orientation.EAST));
        } else if (tile.x < selectedDwarf.location.x && Math.abs(tile.x - selectedDwarf.location.x) > Math.abs(tile.y - selectedDwarf.location.y)) {
            network.send(new DwarfRequestPacket(selectedDwarf.id(), request, Orientation.WEST));
        }  else if (tile.y < selectedDwarf.location.y) {
            network.send(new DwarfRequestPacket(selectedDwarf.id(), request, Orientation.NORTH));
        } else {
            network.send(new DwarfRequestPacket(selectedDwarf.id(), request, Orientation.SOUTH));
        }
    }


    public void render() {
        World.localCoordToGlobalTile(input.getX(), input.getY(), parent.getViewportLocation(), temp);
        world.draw(painter, parent.getViewportLocation(), zLayer, painter.getDrawableWidth(), painter.getDrawableHeight(), selectedDwarf, temp.x, temp.y, zLayer);
        if (currentActionMenu != null && currentInputState == UserInputState.DWARF_SELECTED) currentActionMenu.draw(painter, parent.getViewportLocation());

    }
}
