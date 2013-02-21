package ardust.shared;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.ByteBuffer;

abstract public class Packet {
    static Packet read(ByteBuffer buffer) {
        throw new NotImplementedException();
    }
    abstract void write(ByteBuffer buffer);

}
