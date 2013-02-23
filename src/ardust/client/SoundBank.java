package ardust.client;

// is it static or isn't it ?

public class SoundBank {

    public static int buttonSound;
    public static int pickaxeSound;
    public static int mainMusic;
    public static int menuMusic;

    public SoundSystem system;

    public SoundBank(SoundSystem system) {
        this.system = system;
        registerSoundEffects();
    }

    public void registerSoundEffects() {
        pickaxeSound = system.registerFile("resources/audio/effects/pick.ogg", SoundSystem.SoundType.Effect);
        buttonSound = system.registerFile("resources/audio/effects/button.ogg", SoundSystem.SoundType.Effect);
        mainMusic = system.registerFile("resources/audio/music/main.ogg", SoundSystem.SoundType.Music);
        menuMusic = system.registerFile("resources/audio/music/menu.ogg", SoundSystem.SoundType.Music);
    }

    public void playSound(int which) {
        system.play(which);
    }

    public void stopSound(int which) {
        system.stop(which);
    }


}
