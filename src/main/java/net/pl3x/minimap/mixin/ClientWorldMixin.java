package net.pl3x.minimap.mixin;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.biome.Biome;
import net.pl3x.minimap.util.Biomes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {

    // This is a dirty hack to prevent Mojang from giving us a PLAINS
    // biome when none is found due to the edge of the view distance

    @Inject(method = "getGeneratorStoredBiome", at = @At("HEAD"), cancellable = true)
    private void getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ, CallbackInfoReturnable<Biome> cir) {
        if (Biomes.ALLOW_NULL_BIOMES) {
            cir.setReturnValue(null);
            cir.cancel();
        }
    }
}
