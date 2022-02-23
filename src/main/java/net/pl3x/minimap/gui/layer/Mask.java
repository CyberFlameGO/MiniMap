package net.pl3x.minimap.gui.layer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.texture.Texture;
import org.lwjgl.opengl.GL11;

public class Mask extends Layer {
    @Override
    public void render(MatrixStack matrixStack) {
        float size2 = mm.getSize() * 2F;
        float x = mm.getCenterX() - mm.getSize();
        float y = mm.getCenterY() - mm.getSize();

        // uses blend which writes to the alpha channel where black pixels exist.
        // the mask is twice as large as the map texture and the black pixels are the map size.
        // this ensures pixels outside the map area won't draw to the screen. this fixes rough edges
        // on circle maps and prevents corners from extending beyond the frame on rotating square maps.
        RenderSystem.blendFuncSeparate(GL11.GL_ZERO, GL11.GL_ONE, GL11.GL_SRC_COLOR, GL11.GL_ZERO);

        Texture texture = Config.getConfig().circular ? Texture.MASK_CIRCLE : Texture.MASK_SQUARE;
        texture.draw(matrixStack, x, y, size2, size2);
    }
}
