package ardust.server;

import ardust.shared.NetworkConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;

public class NetworkServer {

    private Thread worker;
    ServerSocket acceptSocket;

    Deque<NetworkConnection> newConnections = new ArrayDeque<NetworkConnection>();
    private boolean stopped;

    NetworkServer(int port) {
        try {
            acceptSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        worker = new Thread() {
            public void run() {
                while (true) {
                    if (acceptSocket.isClosed())
                        break;
                    acceptConnection();
                }
            }
        };
        worker.setDaemon(true);
        worker.start();
    }

    public void stop() {
        stopped = true;
        try {
            acceptSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (worker != null)
                worker.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void acceptConnection() {
        try {
            Socket socket = acceptSocket.accept();
            NetworkConnection connection = new NetworkConnection(socket);
            connection.start();
            synchronized (this) {
                newConnections.addLast(connection);
            }
        } catch (Exception e) {
            if (!stopped)
                e.printStackTrace();
        }
    }

    public boolean hasNewConnection() {
        synchronized (this) {
            return !newConnections.isEmpty();
        }
    }

    public NetworkConnection nextNewConnection() {
        synchronized (this) {
            return newConnections.removeFirst();
        }
    }
}
