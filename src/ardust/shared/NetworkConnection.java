package ardust.shared;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Deque;

public class NetworkConnection {
    Socket socket;

    Deque<Packet> outbound; //this synchronized
    EventFlag outboundFlag;
    Thread outboundThread;

    Deque<Packet> inbound;  //this synchronized
    EventFlag inboundFlag;
    Thread inboundThread;

    public NetworkConnection(Socket socket) {
        this.socket = socket;
    }

    public void start() {
        outboundThread = new Thread() {
            public void run() {
                setDaemon(true);
                handleOutbound();
            }
        };
        inboundThread = new Thread() {
            public void run() {
                setDaemon(true);
                handleInbound();
            }
        };
        outboundThread.setDaemon(true);
        inboundThread.setDaemon(true);
        inboundThread.start();
        outboundThread.start();
    }

    public void stop() {
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            if (inboundThread != null)
                inboundThread.join();
            if (outboundThread != null)
                outboundThread.join();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    private void handleOutbound() {
        final ByteBuffer buffer = ByteBufferBuffer.alloc(1024 * 64);
        try {
            OutputStream outStream = socket.getOutputStream();
            while (true) {
                if (socket.isClosed() || !socket.isConnected())
                    break;
                outboundFlag.reset();
                Packet packet = null;
                synchronized (this) {
                    if (!outbound.isEmpty())
                        packet = outbound.removeFirst();
                }
                if (packet == null) {
                    outboundFlag.waitFor();
                } else {
                    buffer.clear();
                    buffer.putShort((short) 0); // size
                    packet.write(buffer);
                    //todo some kind of compression
                    int size = buffer.position() - 2;
                    if ((size <= 0) || (size > 32000))
                        throw new RuntimeException("Bad packet size: " + size);
                    buffer.putShort(0, (short) size);
                    outStream.write(buffer.array(), buffer.arrayOffset(), buffer.position());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (socket != null)
                    socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void ensureBuffer(ByteBuffer buffer, InputStream stream, int goalPosition) throws IOException {
        while (true) {
            int remainder = goalPosition - buffer.position();
            if (remainder <= 0)
                return;
            int len = stream.read(buffer.array(), buffer.arrayOffset() + buffer.position(), remainder);
            if (len == -1)
                throw new IOException("EOF");
            buffer.position(buffer.position() + len);
        }
    }

    private void handleInbound() {
        ByteBuffer buffer = ByteBufferBuffer.alloc(1024 * 64);
        try {
            InputStream inStream = socket.getInputStream();
            while (true) {
                if (socket.isClosed() || !socket.isConnected())
                    break;
                ensureBuffer(buffer, inStream, 2);
                int size = buffer.getShort(0);
                if ((size <= 0) || (size > 32000))
                    throw new RuntimeException("Invalid packet size.");
                buffer.clear();
                ensureBuffer(buffer, inStream, size);
                int position = buffer.position();
                if (position != size)
                    throw new RuntimeException("Bad receive buffer.");
                Packet packet = Packet.read(buffer);
                synchronized (this) {
                    inbound.addLast(packet);
                }
                inboundFlag.set();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (socket != null)
                    socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
