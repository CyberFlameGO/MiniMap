package net.pl3x.minimap.gui.layer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.pl3x.minimap.gui.texture.Texture;
import org.lwjgl.opengl.GL11;

public class Background extends Layer {
    @Override
    public void render(MatrixStack matrixStack) {

        float x = mm.getCenterX() - (mm.getSize() / 2F);
        float y = mm.getCenterY() - (mm.getSize() / 2F);

        // uses blend which only writes where high alpha values exist
        RenderSystem.blendFunc(GL11.GL_DST_ALPHA, GL11.GL_ONE_MINUS_DST_ALPHA);

        mm.getBackground().draw(matrixStack, x, y, mm.getSize(), mm.getSize());
    }

    @Override
    public void update() {
        RegistryKey<World> key = mm.getPlayer().world.getRegistryKey();
        if (key == World.OVERWORLD) {
            mm.setBackground(Texture.SKY_OVERWORLD);
        } else if (key == World.NETHER) {
            mm.setBackground(Texture.SKY_THE_NETHER);
        } else if (key == World.END) {
            mm.setBackground(Texture.SKY_THE_END);
        } else {
            mm.setBackground(Texture.SKY_OVERWORLD);
        }
    }
}
