package ardust.shared;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteBufferBuffer {
    public static ByteBuffer alloc(int size) {
        ByteBuffer bb = ByteBuffer.allocate(size);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb;
    }
}
