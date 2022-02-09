package net.pl3x.minimap.sound;

import com.mojang.serialization.Lifecycle;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.pl3x.minimap.MiniMap;

import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;

public class Sound {
    private static final Set<Sound> REGISTERED_SOUNDS = new HashSet<>();

    public static final Sound WHOOSH = register(new Sound("whoosh"));

    private static Sound register(Sound sound) {
        REGISTERED_SOUNDS.add(sound);
        return sound;
    }

    public static void initialize() {
        MutableRegistry<SoundEvent> registry = ((MutableRegistry<SoundEvent>) Registry.SOUND_EVENT);
        REGISTERED_SOUNDS.forEach(sound -> {
            RegistryKey<SoundEvent> key = RegistryKey.of(registry.getKey(), sound.identifier);
            int rawId = Registry.SOUND_EVENT.getRawId(sound.soundEvent);
            OptionalInt id = rawId > 0 ? OptionalInt.of(rawId) : OptionalInt.empty();
            sound.soundEvent = registry.replace(id, key, new SoundEvent(sound.identifier), Lifecycle.stable());
            if (sound.soundEvent != null) {
                MiniMap.LOG.info("Loaded sound " + sound.identifier);
            } else {
                MiniMap.LOG.error("Could not load sound " + sound.identifier);
            }
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
        this.instance = PositionedSoundInstance.master(this.soundEvent, 1.0F, 1.0F);

        // player instance
        MiniMap.CLIENT.getSoundManager().play(this.instance);
    }

    public void stop() {
        if (this.soundEvent == null) {
            return;
        }

        // stop current instance
        MiniMap.CLIENT.getSoundManager().stop(this.instance);
    }
}
