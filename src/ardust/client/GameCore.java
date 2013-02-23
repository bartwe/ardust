package ardust.client;

import ardust.packets.*;
import ardust.shared.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;

public class GameCore {
    private final NetworkConnection network;
    private final Input input;
    private Painter painter;
    private final World world;
    private boolean musicOn = true;

    Character selectedDwarf;
    String name;
    private final GameLoop parent;
    DwarfActionMenu currentActionMenu;

    Values values = new Values(Constants.V_PLAYER_VALUES_SIZE);
    int stone;
    int iron;
    int gold;
    private int playerId;


    public enum UserInputState {
        NONE,
        WALK,
        HALT,
        MINE,
        ATTEMPTING_SWORD_PURCHASE,
        ATTEMPTING_ARMOR_PURCHASE,
        ATTEMPTING_GOLD_SWORD_PURCHASE,
        FIGHT
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

    Point temp = new Point();
    Point2 temp3 = new Point2();

    private void processNetwork() {
        if (!network.isValid())
            parent.fail("Network lost");

        while (network.hasInboundPackets()) {
            Packet packet = network.nextInboundPacket();
            if (packet instanceof WindowPacket) {
                WindowPacket spp = (WindowPacket) packet;
                temp.setLocation(spp.x, spp.y);
                World.worldCoordToScreenCoord(temp, temp);
                System.err.println("Force viewport " + spp.x + ":" + spp.y + " " + temp);
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
            } else if (packet instanceof PlayerUpdatePacket) {
                PlayerUpdatePacket pup = (PlayerUpdatePacket) packet;
                values.read(pup.data);

                stone = values.get(Constants.V_PLAYER_STONES);
                iron = values.get(Constants.V_PLAYER_IRON);
                gold = values.get(Constants.V_PLAYER_GOLD);
                playerId = values.get(Constants.V_PLAYER_ID);
            } else
                throw new RuntimeException("Unknown packet: " + packet.packetId());
        }

        World.screenCoordToWorldCoord(parent.getViewportLocation(), temp);
        WindowPacket wp = new WindowPacket((int) temp.getX(), (int) temp.getY());
        network.send(wp);
    }

    private void mousePan() {

        if ((selectedDwarf != null) && !world.characters.containsId(selectedDwarf.id()))
            deselectCurrentDwarf();

        //Panning around on the map
        if (input.isKeyDown(Keyboard.KEY_TAB, true)) {
            selectedDwarf = world.nextCharacter(playerId, selectedDwarf);
            currentActionMenu = null;
        }

        if (input.isKeyDown(Keyboard.KEY_M, true)) {
            if (musicOn)
                parent.soundBank.stopSound(parent.soundBank.mainMusic);
            else
                parent.soundBank.playSound(parent.soundBank.mainMusic, false);

            musicOn = !musicOn;
        }

        if (selectedDwarf == null)
            currentActionMenu = null;


        int dWheel = Mouse.getDWheel();
        if (dWheel > 0)
        {
           int tmp = Constants.PIXEL_SCALE;
           painter.setScale(Constants.PIXEL_SCALE + 1);
           if (Constants.PIXEL_SCALE != tmp)
           {
               //parent.centerViewportAroundMouse();
           }
        } else if (dWheel < 0)
        {
            int tmp = Constants.PIXEL_SCALE;
            painter.setScale(Constants.PIXEL_SCALE - 1);
            if (Constants.PIXEL_SCALE != tmp)
            {
               // parent.centerViewportAroundMouse();
            }
        }

        if (input.isMouseButtonDown(1, false) || input.isMouseButtonDown(2, false)) {
            currentActionMenu = null;
            int refKey = (input.isMouseButtonDown(2, false)) ? 2 : 1;
            parent.setCurrentMouseCursor(Constants.PANNING_CURSOR);
            int xPan = (int) Math.max(-Constants.MAP_PAN_MAX_SPEED / Constants.PIXEL_SCALE, Math.min(((input.getX() - input.getMostRecentClick(refKey).x) / (double) Constants.MAP_PAN_SENSITIVITY) * Constants.MAP_PAN_MAX_SPEED, Constants.MAP_PAN_MAX_SPEED/ Constants.PIXEL_SCALE));
            int yPan = (int) Math.max(-Constants.MAP_PAN_MAX_SPEED/ Constants.PIXEL_SCALE, Math.min(((input.getY() - input.getMostRecentClick(refKey).y) / (double) Constants.MAP_PAN_SENSITIVITY) * Constants.MAP_PAN_MAX_SPEED, Constants.MAP_PAN_MAX_SPEED)/ Constants.PIXEL_SCALE);

            parent.setViewportLocation(new Point(parent.getViewportLocation().x + xPan, parent.getViewportLocation().y + yPan));
        } else {
            parent.setCurrentMouseCursor(Constants.DEFAULT_CURSOR);

            if (input.isMouseButtonDown(0, false)) {
                boolean consumeEvent = false;

                parent.setCurrentMouseCursor(Constants.DEFAULT_CURSOR);
                World.localCoordToGlobalTile(input.getX(), input.getY(), parent.getViewportLocation(), temp);

                if (!consumeEvent && (currentActionMenu != null)) {
                    GameCore.UserInputState menuState = currentActionMenu.isButtonHere(input.getX(), input.getY(), parent.getViewportLocation());
                    switch (menuState) {
                        case WALK:
                            selectedDwarf.walkTo(currentActionMenu.location);
                            break;
                        case HALT:
                            selectedDwarf.halt();
                            break;
                        case MINE:
                            selectedDwarf.mineTo(currentActionMenu.location);
                            break;
                        case FIGHT:
                            selectedDwarf.fightTo(currentActionMenu.location);
                            break;
                        case ATTEMPTING_ARMOR_PURCHASE:
                            network.send(new DwarfRequestPacket(selectedDwarf.id(), DwarfRequest.CraftArmor, Orientation.NORTH));
                            break;
                        case ATTEMPTING_SWORD_PURCHASE:
                            network.send(new DwarfRequestPacket(selectedDwarf.id(), DwarfRequest.CraftSword, Orientation.NORTH));
                            break;
                        case ATTEMPTING_GOLD_SWORD_PURCHASE:
                            network.send(new DwarfRequestPacket(selectedDwarf.id(), DwarfRequest.CraftGoldSword, Orientation.NORTH));
                            break;
                        default:
                            break;
                    }
                    currentActionMenu = null;
                    consumeEvent = true;
                }

                if (!consumeEvent && ((selectedDwarf == null) || world.getCharacterAtTile(temp.x, temp.y) != null)) {
                    Character option = world.getCharacterAtTile(temp.x, temp.y);
                    if ((option != null) && (option.playerId() == playerId)) {
                        selectedDwarf = option;
                        consumeEvent = true;
                    }
                }


                if (!consumeEvent && (selectedDwarf != null) && (currentActionMenu == null)) {
                    temp3.set(temp.x, temp.y);

                    DwarfActionMenu.Mode mode = DwarfActionMenu.Mode.Normal;

                    boolean crafting = Constants.hasCraftingInteraction(world.clientWorld.read(temp.x, temp.y));
                    if (crafting)
                        mode = DwarfActionMenu.Mode.Crafting;

                    Character enemy = world.getCharacterAtTile(temp.x, temp.y);
                    if ((enemy != null) && (enemy.playerId() == playerId))
                        enemy = null;
                    if (enemy != null)
                        mode = DwarfActionMenu.Mode.Fight;

                    currentActionMenu = new DwarfActionMenu(temp3, mode);
                    consumeEvent = true;
                }

                if (consumeEvent)
                    input.isMouseButtonDown(0, true);
            }

            if (currentActionMenu != null) {
                GameCore.UserInputState menuState = currentActionMenu.isButtonHere(input.getX(), input.getY(), parent.getViewportLocation());
                if (menuState != UserInputState.NONE)
                    parent.setCurrentMouseCursor(Constants.ACTION_CURSOR);
            }
        }

        if (selectedDwarf != null) {
            if (input.isKeyDown(Keyboard.KEY_W, false) || input.isKeyDown(Keyboard.KEY_UP, false))
                network.send(new DwarfRequestPacket(selectedDwarf.id(), DwarfRequest.Walk, Orientation.NORTH));
            if (input.isKeyDown(Keyboard.KEY_D, false) || input.isKeyDown(Keyboard.KEY_RIGHT, false))
                network.send(new DwarfRequestPacket(selectedDwarf.id(), DwarfRequest.Walk, Orientation.EAST));
            if (input.isKeyDown(Keyboard.KEY_S, false) || input.isKeyDown(Keyboard.KEY_DOWN, false))
                network.send(new DwarfRequestPacket(selectedDwarf.id(), DwarfRequest.Walk, Orientation.SOUTH));
            if (input.isKeyDown(Keyboard.KEY_A, false) || input.isKeyDown(Keyboard.KEY_LEFT, false))
                network.send(new DwarfRequestPacket(selectedDwarf.id(), DwarfRequest.Walk, Orientation.WEST));

            if (input.isKeyDown(Keyboard.KEY_ESCAPE, false))
                deselectCurrentDwarf();
        }

        if (selectedDwarf == null)
            currentActionMenu = null;
    }

    public void deselectCurrentDwarf() {
        selectedDwarf = null;
        currentActionMenu = null;
        parent.setCurrentMouseCursor(Constants.DEFAULT_CURSOR);
    }

    public void render() {
        World.localCoordToGlobalTile(input.getX(), input.getY(), parent.getViewportLocation(), temp);
        world.draw(painter, parent.getViewportLocation(), painter.getDrawableWidth(), painter.getDrawableHeight(), selectedDwarf, temp.x, temp.y);
        if (currentActionMenu != null)
            currentActionMenu.draw(painter, parent.getViewportLocation());

    }
}
