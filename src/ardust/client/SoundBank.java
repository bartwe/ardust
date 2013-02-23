package ardust.client;

public class SoundBank {

    public static int buttonSound;
    public static int pickaxeSound;

    public static SoundSystem system;

    public SoundBank(SoundSystem system)
    {
     this.system = system;
        registerSoundEffects();
    }

    public void registerSoundEffects()
    {
        pickaxeSound = system.registerFile("/resources/audio/effects/pick.ogg",SoundSystem.SoundType.Effect);
    }

    public static void playSound(int which)
    {
        system.play(which);
    }


}
