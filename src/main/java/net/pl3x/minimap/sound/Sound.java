package net.pl3x.minimap.sound;

import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.pl3x.minimap.MiniMap;

import java.util.HashSet;
import java.util.Set;

public class Sound {
    private static final Set<Sound> REGISTERED_SOUNDS = new HashSet<>();

    public static final Sound WHOOSH = register(new Sound("whoosh"));

    private static Sound register(Sound sound) {
        REGISTERED_SOUNDS.add(sound);
        return sound;
    }

    public static void initialize() {
        REGISTERED_SOUNDS.forEach(sound -> {
            sound.soundEvent = new SoundEvent(sound.identifier);
            MiniMap.LOG.info("Loaded sound " + sound.identifier);
        });
    }

    public final Identifier identifier;
    private SoundEvent soundEvent;
    private SoundInstance instance;

    public Sound(String name) {
        this.identifier = new Identifier(MiniMap.MODID, name);
    }

    public void play() {
        if (this.soundEvent == null) {
            return;
        }

        // prevent overlapping
        stop();

        // create new instance
        this.instance = PositionedSoundInstance.master(this.soundEvent, 1F, 1F);

        // player instance
        MiniMap.getClient().getSoundManager().play(this.instance);
    }

    public void stop() {
        if (this.soundEvent == null) {
            return;
        }

        // stop current instance
        MiniMap.getClient().getSoundManager().stop(this.instance);
    }
}
