package net.pl3x.minimap.gui.layer;

import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.GL;
import net.pl3x.minimap.gui.texture.Texture;

public class Players extends Layer {
    @Override
    public void render(MatrixStack matrixStack) {
        float halfSize = mm.getSize() / 2F;
        //float mapScale = mm.size / mm.deltaZoom;
        //float mapScale2 = mapScale * 2F;
        float offset = 0;//mapScale / mapScale2;

        float x0 = mm.getCenterX() - halfSize;// + mapScale;
        float x1 = x0 + mm.getSize();// - mapScale2;
        float y0 = mm.getCenterY() - halfSize;// + mapScale;
        float y1 = y0 + mm.getSize();// - mapScale2;

        float u = 0.375F;//(MiniMap.TILE_SIZE / 2F - halfSize) / MiniMap.TILE_SIZE; // doesn't resize with size or zoom
        float v = 1F - u;

        // https://crafthead.net/helm/0b54d4f1-8ce9-46b3-a723-4ffdeeae3d7d

        // use a blend that supports translucent pixels for all the remaining textures
        //RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

        matrixStack.push();
        if (Config.getConfig().northLocked) {
            // only rotate if map is northlocked
            GL.rotateScene(matrixStack, mm.getCenterX(), mm.getCenterY(), mm.getAngle());
        }
        Texture.PLAYER.draw(matrixStack, x0 + offset, y0, x1 + offset, y1, u, v);
        matrixStack.pop();
    }
}
