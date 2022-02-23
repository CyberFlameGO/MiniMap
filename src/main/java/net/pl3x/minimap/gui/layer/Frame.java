package net.pl3x.minimap.gui.layer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.texture.Texture;
import org.lwjgl.opengl.GL11;

public class Frame extends Layer {
    @Override
    public void render(MatrixStack matrixStack) {
        if (!Config.getConfig().showFrame) {
            return;
        }

        float x = mm.getCenterX() - mm.getSize() / 2F;
        float y = mm.getCenterY() - mm.getSize() / 2F;

        // use a blend that supports translucent pixels for all the remaining textures
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Texture texture = Config.getConfig().circular ? Texture.FRAME_CIRCLE : Texture.FRAME_SQUARE;
        texture.draw(matrixStack, x, y, mm.getSize(), mm.getSize());
    }
}
