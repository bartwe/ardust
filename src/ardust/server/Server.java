package ardust.server;

import ardust.packets.*;
import ardust.shared.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Server {

    ServerWorld world;
    NetworkServer networkServer;
    private Thread workerThread;
    private boolean running;
    private ArrayList<Player> players = new ArrayList<Player>();

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
        world.save();
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
        world = new ServerWorld();
        world.load();
        networkServer = new NetworkServer(53421);
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
        fetchClientCommands();
        executeCommands();
        sendUpdates();
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
        //TODO: sends updates outside of the players world range, perf thingy

        for (Player player : players) {
            for (Packet packet: packets)
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
            HelloPacket hp = (HelloPacket)packet;
            player.setName(hp.getName());
        }
        else if (packet instanceof WindowPacket) {
            WindowPacket wp = (WindowPacket)packet;
            int oldX = player.getX();
            int oldY = player.getY();
            int oldZ = player.getZ();
            player.setXYZ(wp.x, wp.y, wp.z);
            sendWorldRegion(player, oldX, oldY, oldZ, wp.x, wp.y, wp.z);
        }
        else if (packet instanceof DebugChangeTilePacket) {
            DebugChangeTilePacket wp = (DebugChangeTilePacket)packet;
            byte tile = world.readDirect(wp.x, wp.y, wp.z);
            tile += 1;
            if (tile >= 3)
                tile = 0;
            System.err.println("changetile: "+wp.x+","+wp.y+","+wp.z+"  "+tile);
            world.writeDirect(wp.x, wp.y, wp.z, tile);
        }
        else
            throw new RuntimeException("Unknown packet: "+packet.packetId());
    }

    private void sendWorldRegion(Player player, int oldX, int oldY, int oldZ, int x, int y, int z) {
        WorldRegionPacket wrp = new WorldRegionPacket(world, oldX, oldY, oldZ, x, y, z);
        player.sendPacket(wrp);
    }

    private ArrayList<Player> playersTemp = new ArrayList<Player>();

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
