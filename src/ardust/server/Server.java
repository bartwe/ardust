package ardust.server;

import ardust.entities.Entities;
import ardust.entities.Entity;
import ardust.packets.*;
import ardust.shared.ByteBufferBuffer;
import ardust.shared.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class Server {

    ServerWorld world;
    Entities entities;
    NetworkServer networkServer;
    ByteBuffer tempBuffer = ByteBufferBuffer.alloc(1024 * 1024);
    private Thread workerThread;
    private boolean running;
    private ArrayList<Player> pendingPlayers = new ArrayList<Player>();
    private HashMap<Integer, Player> players = new HashMap<Integer, Player>();
    private long saveDeadline;
    long prevT;
    PositionalMap positionalMap = new PositionalMap();
    int playerCount = 0;
    int ringSequence = 0;

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
        try {
            System.out.println(System.in.read());
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.stop();
    }

    public void stop() {
        System.err.println("Stopping server");
        if (entities != null)
            entities.save();
        if (world != null)
            world.save();
        if (networkServer != null)
            networkServer.stop();
        running = false;
        if (workerThread != null) {
            try {
                workerThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.err.println("Stopped server");
    }

    public void start() {
        System.err.println("Starting server");
        entities = new Entities();
        entities.load();
        world = new ServerWorld();
        world.load();
        networkServer = new NetworkServer(Constants.PORT);
        workerThread = new Thread() {
            public void run() {
                long deadline = System.currentTimeMillis();
                while (running) {
                    step();
                    long newDeadline = System.currentTimeMillis() + Constants.MILLIS_PER_SERVER_TICK;
                    while (true) {
                        if (deadline <= System.currentTimeMillis())
                            break;
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    deadline = newDeadline;
                }
            }
        };
        running = true;
        workerThread.start();
        networkServer.start();
        System.err.println("Started server");
    }

    private void step() {
        long currentT = System.currentTimeMillis();
        if (prevT == 0)
            prevT = currentT;
        int deltaT = (int) (currentT - prevT);
        prevT += deltaT;

        updatePositionalMap();
        fetchClientCommands();
        evaluateDwarves(deltaT);
        executeCommands();
        sendUpdates();


        if (System.currentTimeMillis() > saveDeadline) {
            saveDeadline = System.currentTimeMillis() + 15 * 60 * 1000;
            if (entities != null)
                entities.save();
            if (world != null)
                world.save();
        }
    }

    private void updatePositionalMap() {
        positionalMap.updateEntities(entities);
    }

    ArrayList<Entity> entitiesTemp = new ArrayList<Entity>();

    private void evaluateDwarves(int deltaT) {
        entities.getDwarves(entitiesTemp);
        for (Entity dwarf : entitiesTemp)
            Dwarves.tick(deltaT, players.get(dwarf.playerId), dwarf, world, positionalMap);
        entitiesTemp.clear();
    }

    private void sendUpdates() {
        int[] updates = world.getUpdatesBufferArray();
        int index = 0;
        int count = world.getUpdatesCount();

        ArrayList<Packet> packets = new ArrayList<Packet>();
        while (count > index) {
            int len = Math.min(count, 1000);    // 12 bytes a piece, so don't do more than 1000 updates in a packet
            WorldUpdatesPacket updatePacket = new WorldUpdatesPacket(world, updates, index, len);
            index += len;
            packets.add(updatePacket);
        }
        tempBuffer.clear();
        if (entities.write(tempBuffer, false, true)) {
            // could be too large...
            tempBuffer.flip();
            EntitiesPacket entitiesPacket = new EntitiesPacket(tempBuffer, true, false);
            packets.add(entitiesPacket);
        }

        //TODO: sends updates outside of the players world range, perf thingy
        for (Player player : players.values()) {
            player.sendUpdates();
            for (Packet packet : packets)
                player.sendPacket(packet);
        }
    }

    private void executeCommands() {
        ArrayList<Player> players = new ArrayList<Player>(pendingPlayers);
        players.addAll(this.players.values());
        Collections.shuffle(players, new Random());
        for (Player player : players) {
            while (player.hasPacket())
                executeCommand(player, player.nextPacket());
        }
    }

    private void executeCommand(Player player, Packet packet) {
        if (packet instanceof HelloPacket) {
            handleHello(player, (HelloPacket) packet);
        } else if (packet instanceof WindowPacket) {
            handleWindowChange(player, (WindowPacket) packet);
        } else if (packet instanceof DwarfRequestPacket) {
            handleDwarfRequest(player, (DwarfRequestPacket) packet);
        } else
            throw new RuntimeException("Unknown packet: " + packet.packetId());
    }

    private void handleDwarfRequest(Player player, DwarfRequestPacket packet) {
        if (player.id == 0)
            return;
        Entity entity = entities.getEntity(packet.id);
        if (entity == null)
            return; // command can be late, ignore
        if (entity.kind != Entity.Kind.DWARF)
            return;
        if (player.id != entity.playerId.intValue())
            return;
        Dwarves.handle(entity, packet, world, positionalMap, player);
    }

    private void handleWindowChange(Player player, WindowPacket packet) {
        if (player.id == 0)
            return;
        int oldX = player.getX();
        int oldY = player.getY();
        player.setXY(packet.x, packet.y);
        sendWorldRegion(player, oldX, oldY, packet.x, packet.y);
    }

    private void handleHello(Player player, HelloPacket packet) {
        player.setName(packet.getName());

        if (players.containsKey(player.id)) {
            player.disconnect();
            return;
        }
        players.put(player.id, player);
        pendingPlayers.remove(player);

        int x = Constants.START_OFFSET;
        int y = Constants.START_OFFSET;
        Random random = new Random();
//        x += random.nextInt(Constants.WORLD_LENGTH);
//        y += random.nextInt(Constants.WORLD_LENGTH);
        //player.setXY(x, y);
        boolean newRing = player.setLocation(playerCount,20,ringSequence);

        if (newRing)
            ringSequence++;

        player.spawnSetup(entities, world, positionalMap);
        player.sendPacket(new WindowPacket(player.getX(), player.getY()));

        this.playerCount++;

        tempBuffer.clear();
        if (entities.write(tempBuffer, true, false)) {
            tempBuffer.flip();
            player.sendPacket(new EntitiesPacket(tempBuffer, true, true));
        }

        sendWorldRegion(player, Constants.BAD_AXIS, Constants.BAD_AXIS, x, y);
    }

    private void sendWorldRegion(Player player, int oldX, int oldY, int x, int y) {
        WorldRegionPacket wrp = new WorldRegionPacket(world, oldX, oldY, x, y);
        player.sendPacket(wrp);
    }

    private void fetchClientCommands() {
        while (networkServer.hasNewConnection()) {
            Player player = new Player(networkServer.nextNewConnection());
            pendingPlayers.add(player);
        }
        ArrayList<Player> players = new ArrayList<Player>(this.players.values());
        for (Player player : players) {
            if (!player.isValid()) {
                player.disconnect();
                this.players.remove(player);
            }
        }
        players.clear();
        players.addAll(pendingPlayers);
        for (Player player : players) {
            if (!player.isValid()) {
                player.disconnect();
                pendingPlayers.remove(player);
            }
        }
    }
}
