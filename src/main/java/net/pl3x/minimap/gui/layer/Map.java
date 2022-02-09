package net.pl3x.minimap.gui.layer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.texture.Texture;
import net.pl3x.minimap.gui.GL;
import org.lwjgl.opengl.GL11;

public class Map extends Layer {
    @Override
    public void render(MatrixStack matrixStack) {
        float halfSize = mm.size / 2.0F;
        float scale = mm.size / mm.deltaZoom;
        float scale2 = scale * 2.0F;

        float x0 = mm.centerX - halfSize + scale;
        float x1 = x0 + mm.size - scale2;
        float y0 = mm.centerY - halfSize + scale;
        float y1 = y0 + mm.size - scale2;

        float u = (MiniMap.TILE_SIZE / 2F - mm.deltaZoom / 2F) / MiniMap.TILE_SIZE; // resizes with zoom _and_ size
        float v = 1.0F - u;

        // uses blend which only writes where high alpha values exist from above
        RenderSystem.blendFunc(GL11.GL_DST_ALPHA, GL11.GL_ONE_MINUS_DST_ALPHA);

        matrixStack.push();
        if (!Config.getConfig().northLocked) {
            // rotate map opposite of player angle
            GL.rotateScene(matrixStack, mm.centerX, mm.centerY, -mm.angle);
            if (!Config.getConfig().circular) {
                // scale map if square and not north locked to hide missing pixels in corners when rotating
                GL.scaleScene(matrixStack, mm.centerX, mm.centerY, 1.41421356237F);
            }
        }
        Texture.MINIMAP.draw(matrixStack, x0, y0, x1, y1, u, v);
        matrixStack.pop();
    }
}
