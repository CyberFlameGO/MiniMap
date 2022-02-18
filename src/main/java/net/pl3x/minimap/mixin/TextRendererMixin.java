package net.pl3x.minimap.mixin;

import net.minecraft.client.font.TextRenderer;
import net.pl3x.minimap.gui.font.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextRenderer.class)
public class TextRendererMixin {

    // This is a dirty hack to prevent Mojang's TextRenderer from
    // removing alpha from text color if it's 0x03 or below

    @Inject(method = "tweakTransparency", at = @At("HEAD"), cancellable = true)
    private static void tweakTransparency(int argb, CallbackInfoReturnable<Integer> cir) {
        if (Font.FIX_MOJANGS_TEXT_RENDERER_CRAP) {
            cir.setReturnValue(argb);
            cir.cancel();
        }
    }
}
