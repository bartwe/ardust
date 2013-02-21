package ardust.server;

import java.io.IOException;

public class Server {
    private static final long MILLIS_PER_SERVER_TICK = 200;

    NetworkServer networkServer;
    private Thread workerThread;
    private boolean running;

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.stop();
    }

    private void stop() {
        networkServer.stop();
        running = false;
        if (workerThread != null) {
            try {
                workerThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void start() {
        networkServer = new NetworkServer(53421);
        workerThread = new Thread() {
            public void run() {
                long deadline = System.currentTimeMillis();
                while (running) {
                    step();
                    long newDeadline =  System.currentTimeMillis() + MILLIS_PER_SERVER_TICK;
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
    }

    private void step() {
        fetchClientCommands();
        executeCommands();
        sendUpdates();
    }

    private void sendUpdates() {

    }

    private void executeCommands() {

    }

    private void fetchClientCommands() {

    }
}
