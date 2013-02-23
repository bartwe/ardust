package ardust.client;

import ardust.shared.ByteBufferBuffer;
import ardust.shared.Loader;
import com.jcraft.oggdecoder.OggData;
import com.jcraft.oggdecoder.OggDecoder;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

public class SoundSystem {

    enum SoundType {
        Effect,
        Music
    }

    private class SoundObject {
        public String filename;
        public SoundType type;

        public SoundObject(String filename, SoundType type) {
            this.filename = filename;
            this.type = type;
        }
    }

    /**
     * Buffers hold sound data.
     */
    private IntBuffer buffer;

    /**
     * Sources are points emitting sound.
     */
    private IntBuffer source;

    private ArrayList<SoundObject> soundBank;
    private boolean disabled;

    public SoundSystem() {
        try {
            AL.create();
        } catch (LWJGLException e) {
            disabled = true;
            e.printStackTrace();
        }
        soundBank = new ArrayList<SoundObject>();
        buffer = BufferUtils.createIntBuffer(5);
        source = BufferUtils.createIntBuffer(5);
        if (!disabled) {
            AL10.alGenBuffers(buffer);
            AL10.alGenSources(source);
        }
    }

    public int registerFile(String filename, SoundType type) {
        soundBank.add(new SoundObject(filename, type));

        return soundBank.size() - 1;
    }

    public void updateSource(int i, String filename) {
        soundBank.get(i).filename = filename;
    }

    public void play(int i) {
        if (disabled)
            return;

        SoundObject obj = soundBank.get(i);
        OggDecoder oggDecoder = new OggDecoder();

        ByteBuffer databuffer = ByteBufferBuffer.alloc(1024 * 1024);
        try {
            InputStream f = Loader.getRequiredResourceAsStream(obj.filename);
            int offset = 0;
            int remaining = databuffer.remaining();
            while (true) {
                if (remaining <=0)
                    throw new RuntimeException();
                int len = f.read(databuffer.array(), databuffer.arrayOffset()+offset, remaining);
                if (len < 0)
                    break;
                remaining -= len;
                offset += len;
            }
            databuffer.position(offset);
            databuffer.flip();
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        byte[] data = new byte[databuffer.remaining()];
        databuffer.get(data);
        databuffer = null;

        // Decode OGG into PCM
        InputStream inputStream = new ByteArrayInputStream(data);

        OggData oggData;
        try {
            oggData = oggDecoder.getData(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        int sourceID = source.get(i);
        int bufferID = buffer.get(i);

        // Load PCM data into buffer
        AL10.alBufferData(
                bufferID,
                oggData.channels > 1 ?
                        AL10.AL_FORMAT_STEREO16 : AL10.AL_FORMAT_MONO16,
                oggData.data,
                oggData.rate);

        int error = AL10.alGetError();
        if (error != AL10.AL_NO_ERROR) {
            System.err.println("AL error. "+error);
            return;
        }

        AL10.alSourcei(sourceID, AL10.AL_BUFFER, bufferID);
        AL10.alSourcef(sourceID, AL10.AL_PITCH, 1.0f);
        AL10.alSourcef(sourceID, AL10.AL_GAIN, 1.0f);

        if (obj.type == SoundType.Music) {
            AL10.alSourcei(sourceID, AL10.AL_LOOPING, AL10.AL_TRUE);
        } else {
            AL10.alSourcei(sourceID, AL10.AL_LOOPING, AL10.AL_FALSE);
        }

        AL10.alSourcePlay(sourceID);
    }

    public void killAL() {
        if (disabled)
            return;

        AL10.alDeleteBuffers(buffer);
        AL10.alDeleteSources(source);
        AL.destroy();
    }
}
