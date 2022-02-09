package net.pl3x.minimap.mixin;

import net.minecraft.client.render.RenderPhase;
import net.pl3x.minimap.gui.font.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPhase.class)
public class RenderPhaseMixin {

    // This is a dirty hack to prevent Mojang's TextRenderer from disabling
    // GL blending mode after drawing lines of text, so we don't have to keep
    // re-enabling it all the time. It's ugly, but works..

    @Inject(method = "endDrawing", at = @At("HEAD"), cancellable = true)
    private void endDrawing(CallbackInfo ci) {
        if (!Font.ALLOW_DISABLE_BLENDING_AFTER_DRAWING_TEXT) {
            ci.cancel();
        }
    }
}
