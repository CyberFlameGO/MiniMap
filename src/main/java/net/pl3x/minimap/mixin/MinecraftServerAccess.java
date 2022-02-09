package net.pl3x.minimap.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccess {

    // This is a dirty hack to grab the integrated server session in order to
    // get the single player world save name. Beats having to set up server side
    // just to send a packet for that information. I hope..

    @Accessor("session")
    LevelStorage.Session getSession();
}
