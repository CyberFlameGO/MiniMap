package net.pl3x.minimap.gui.layer;

import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.font.Font;
import net.pl3x.minimap.util.Mathf;

public class Directions extends Layer {
    private float x;
    private float y;

    @Override
    public void render(MatrixStack matrixStack) {
        if (!Config.getConfig().showDirections) {
            return;
        }

        float scale = 0.5F / mm.scaleFactor;

        this.x = mm.centerX / scale;
        this.y = mm.centerY / scale + 1F;

        float angle = Config.getConfig().northLocked ? 0 : mm.angle;
        float distance = mm.size / 2F / scale + Font.DEFAULT.height() / mm.scaleFactor;
        if (!Config.getConfig().circular && !Config.getConfig().northLocked && angle != 0F) {
            distance /= Mathf.cosRads(45F - Math.abs(45F + (-Math.abs(angle) % 90F)));
        }

        // use a blend that supports translucent pixels for all the remaining textures
        //RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

        matrixStack.push();
        matrixStack.scale(scale, scale, scale);
        draw(matrixStack, "N", distance * Mathf.sinRads(angle + 180F), distance * Mathf.cosRads(angle + 180F));
        draw(matrixStack, "E", distance * Mathf.sinRads(angle + 90F), distance * Mathf.cosRads(angle + 90F));
        draw(matrixStack, "S", distance * Mathf.sinRads(angle), distance * Mathf.cosRads(angle));
        draw(matrixStack, "W", distance * Mathf.sinRads(angle - 90F), distance * Mathf.cosRads(angle - 90F));
        matrixStack.pop();
    }

    private void draw(MatrixStack matrixStack, String direction, float x, float y) {
        matrixStack.push();
        matrixStack.translate(x, y, 0D);
        Font.DEFAULT.drawCenteredWithShadow(matrixStack, direction, this.x, this.y);
        matrixStack.pop();
    }
}
