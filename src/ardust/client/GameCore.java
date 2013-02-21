package ardust.client;

import ardust.packets.HelloPacket;
import ardust.shared.NameGenerator;
import ardust.shared.NetworkConnection;

public class GameCore {
    private final NetworkConnection network;
    private final Input input;
    private final Painter painter;

    String name;

    public GameCore(NetworkConnection network, Input input, Painter painter) {
        this.network = network;
        this.input = input;
        this.painter = painter;

        name = NameGenerator.next();
    }

    public void start() {
        network.send(new HelloPacket(name));
    }

    public void stop() {

    }

    public void tick() {

    }

    public void render() {

    }
}
