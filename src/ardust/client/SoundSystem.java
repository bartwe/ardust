package ardust.client;

import com.jcraft.oggdecoder.OggData;
import com.jcraft.oggdecoder.OggDecoder;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;

import java.io.*;
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

        File file = new File(System.getProperty("user.dir") + obj.filename);
        byte[] data;

        try {
            RandomAccessFile f = new RandomAccessFile(file, "r");
            data = new byte[(int) f.length()];
            f.read(data);
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

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

        if (AL10.alGetError() != AL10.AL_NO_ERROR) {
            System.err.println("AL error.");
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
