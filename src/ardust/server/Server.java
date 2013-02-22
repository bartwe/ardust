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
import java.util.Random;

public class Server {

    ServerWorld world;
    Entities entities;
    NetworkServer networkServer;
    ByteBuffer tempBuffer = ByteBufferBuffer.alloc(1024 * 1024);
    private Thread workerThread;
    private boolean running;
    private ArrayList<Player> players = new ArrayList<Player>();
    private long saveDeadline;
    private ArrayList<Player> playersTemp = new ArrayList<Player>();
    long prevT;

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
        int deltaT = (int)(currentT - prevT);
        prevT += deltaT;

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

    ArrayList<Entity> entitiesTemp = new ArrayList<Entity>();

    private void evaluateDwarves(int deltaT) {
        entities.getDwarves(entitiesTemp);
        for (Entity dwarf : entitiesTemp)
            Dwarves.tick(deltaT, dwarf);
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
        if (entities.write(tempBuffer, false)) {
            // could be too large...
            tempBuffer.flip();
            EntitiesPacket entitiesPacket = new EntitiesPacket(tempBuffer, true);
            packets.add(entitiesPacket);
        }

        //TODO: sends updates outside of the players world range, perf thingy
        for (Player player : players) {
            for (Packet packet : packets)
                player.sendPacket(packet);
        }
    }

    private void executeCommands() {
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
        } else if (packet instanceof DebugChangeTilePacket) {
            DebugChangeTilePacket wp = (DebugChangeTilePacket) packet;
            byte tile = world.readDirect(wp.x, wp.y, wp.z);
            tile += 1;
            if (tile >= 3)
                tile = 0;
            System.err.println("changetile: " + wp.x + "," + wp.y + "," + wp.z + "  " + tile);
            world.writeDirect(wp.x, wp.y, wp.z, tile);
        } else
            throw new RuntimeException("Unknown packet: " + packet.packetId());
    }

    private void handleDwarfRequest(Player player, DwarfRequestPacket packet) {
        Entity entity = entities.getEntity(packet.id);
        if (entity == null)
            return; // command can be late, ignore
        if (entity.kind != Entity.Kind.DWARF)
            return;
        // todo: ownership check
        Dwarves.handle(entity, packet);
    }

    private void handleWindowChange(Player player, WindowPacket packet) {
        WindowPacket wp = (WindowPacket) packet;
        int oldX = player.getX();
        int oldY = player.getY();
        int oldZ = player.getZ();
        player.setXYZ(wp.x, wp.y, wp.z);
        sendWorldRegion(player, oldX, oldY, oldZ, wp.x, wp.y, wp.z);
    }

    private void handleHello(Player player, HelloPacket packet) {
        HelloPacket hp = (HelloPacket) packet;
        player.setName(hp.getName());
        int x = Constants.START_OFFSET;
        int y = Constants.START_OFFSET;
        int z = Constants.DEFAULT_Z;
        Random random = new Random();
        x += random.nextInt(Constants.WORLD_LENGTH);
        y += random.nextInt(Constants.WORLD_LENGTH);
        player.setXYZ(x, y, z);
        player.spawnSetup(entities, world);
        player.sendPacket(new WindowPacket(player.getX(), player.getY(), player.getZ()));
    }

    private void sendWorldRegion(Player player, int oldX, int oldY, int oldZ, int x, int y, int z) {
        WorldRegionPacket wrp = new WorldRegionPacket(world, oldX, oldY, oldZ, x, y, z);
        player.sendPacket(wrp);
    }

    private void fetchClientCommands() {
        while (networkServer.hasNewConnection()) {
            Player player = new Player(networkServer.nextNewConnection());
            players.add(player);
        }
        ArrayList<Player> players = this.players;
        Collections.shuffle(players, new Random());
        this.players = playersTemp;
        this.players.clear();
        playersTemp = players;
        for (Player player : players) {
            if (player.isValid())
                this.players.add(player);
        }
    }
}
