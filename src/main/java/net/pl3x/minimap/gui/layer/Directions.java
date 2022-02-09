package net.pl3x.minimap.gui.layer;

import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.font.Font;

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
        this.y = mm.centerY / scale + 1;

        double angle = Config.getConfig().northLocked ? 0.0D : mm.angle;
        double distance = mm.size / 2 / scale + Font.DEFAULT.height() / mm.scaleFactor;
        if (!Config.getConfig().circular && !Config.getConfig().northLocked && angle != 0.0D) {
            distance /= cos(45.0D - Math.abs(45.0D + (-Math.abs(angle) % 90.0D)));
        }

        // use a blend that supports translucent pixels for all the remaining textures
        //RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

        matrixStack.push();
        matrixStack.scale(scale, scale, scale);
        draw(matrixStack, "N", distance * sin(angle + 180.0D), distance * cos(angle + 180.0D));
        draw(matrixStack, "E", distance * sin(angle + 90.0D), distance * cos(angle + 90.0D));
        draw(matrixStack, "S", distance * sin(angle), distance * cos(angle));
        draw(matrixStack, "W", distance * sin(angle - 90.0D), distance * cos(angle - 90.0D));
        matrixStack.pop();
    }

    private void draw(MatrixStack matrixStack, String direction, double x, double y) {
        matrixStack.push();
        matrixStack.translate(x, y, 0);
        Font.DEFAULT.drawCenteredWithShadow(matrixStack, direction, this.x, this.y);
        matrixStack.pop();
    }

    private double cos(double degree) {
        return Math.cos(Math.toRadians(degree));
    }

    private double sin(double degree) {
        return Math.sin(Math.toRadians(degree));
    }
}
