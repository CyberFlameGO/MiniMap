package net.pl3x.minimap.gui.layer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.pl3x.minimap.gui.texture.Texture;
import org.lwjgl.opengl.GL11;

public class Background extends Layer {
    private Texture background = Texture.SKY_OVERWORLD;

    @Override
    public void render(MatrixStack matrixStack) {

        float x = mm.centerX - (mm.size / 2F);
        float y = mm.centerY - (mm.size / 2F);

        // uses blend which only writes where high alpha values exist
        RenderSystem.blendFunc(GL11.GL_DST_ALPHA, GL11.GL_ONE_MINUS_DST_ALPHA);

        this.background.draw(matrixStack, x, y, mm.size, mm.size);
    }

    @Override
    public void update() {
        RegistryKey<World> key = mm.player.world.getRegistryKey();
        if (key == World.OVERWORLD) {
            this.background = Texture.SKY_OVERWORLD;
        } else if (key == World.NETHER) {
            this.background = Texture.SKY_THE_NETHER;
        } else if (key == World.END) {
            this.background = Texture.SKY_THE_END;
        } else {
            this.background = Texture.SKY_OVERWORLD;
        }
    }
}
