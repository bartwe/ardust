package ardust.client;

// is it static or isn't it ?

public class SoundBank {

    public static int buttonSound;
    public static int pickaxeSound;

    public SoundSystem system;

    public SoundBank(SoundSystem system) {
        system = system;
        registerSoundEffects();
    }

    public void registerSoundEffects() {
        pickaxeSound = system.registerFile("resources/audio/effects/pick.ogg", SoundSystem.SoundType.Effect);
    }

    public void playSound(int which) {
        system.play(which);
    }


}
