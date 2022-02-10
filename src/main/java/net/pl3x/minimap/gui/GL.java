package net.pl3x.minimap.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import net.pl3x.minimap.MiniMap;

public class GL {
    public static void drawSolidRect(MatrixStack matrices, float x0, float y0, float x1, float y1, int color) {
        drawSolidRect(matrices.peek().getPositionMatrix(), x0, y0, x1, y1, color, color, color, color);
    }

    public static void drawSolidRect(MatrixStack matrices, float x0, float y0, float x1, float y1, int colorLeft, int colorRight) {
        drawSolidRect(matrices.peek().getPositionMatrix(), x0, y0, x1, y1, colorLeft, colorRight, colorLeft, colorRight);
    }

    public static void drawSolidRect(MatrixStack matrices, float x0, float y0, float x1, float y1, int colorTopLeft, int colorTopRight, int colorBottomLeft, int colorBottomRight) {
        drawSolidRect(matrices.peek().getPositionMatrix(), x0, y0, x1, y1, colorTopLeft, colorTopRight, colorBottomLeft, colorBottomRight);
    }

    public static void drawSolidRect(Matrix4f matrix, float x0, float y0, float x1, float y1, int color) {
        drawSolidRect(matrix, x0, y0, x1, y1, color, color, color, color);
    }

    public static void drawSolidRect(Matrix4f matrix, float x0, float y0, float x1, float y1, int colorLeft, int colorRight) {
        drawSolidRect(matrix, x0, y0, x1, y1, colorLeft, colorRight, colorLeft, colorRight);
    }

    public static void drawSolidRect(Matrix4f matrix, float x0, float y0, float x1, float y1, int colorTopLeft, int colorTopRight, int colorBottomLeft, int colorBottomRight) {
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buf = Tessellator.getInstance().getBuffer();
        buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        buf.vertex(matrix, x1, y0, 0).color(colorTopRight).next();
        buf.vertex(matrix, x0, y0, 0).color(colorTopLeft).next();
        buf.vertex(matrix, x0, y1, 0).color(colorBottomLeft).next();
        buf.vertex(matrix, x1, y1, 0).color(colorBottomRight).next();

        buf.end();

        BufferRenderer.draw(buf);

        RenderSystem.enableTexture();
    }

    public static float scale() {
        return (float) MiniMap.CLIENT.getWindow().getScaleFactor();
    }

    public static float width() {
        return MiniMap.CLIENT.getWindow().getWidth();
    }

    public static float height() {
        return MiniMap.CLIENT.getWindow().getHeight();
    }

    public static void rotateScene(MatrixStack matrixStack, double x, double y, double degrees) {
        matrixStack.translate(x, y, 0);
        matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion((float) degrees));
        matrixStack.translate(-x, -y, 0);
    }

    public static void scaleScene(MatrixStack matrixStack, double x, double y, float scale) {
        matrixStack.translate(x, y, 0);
        matrixStack.scale(scale, scale, scale);
        matrixStack.translate(-x, -y, 0);
    }
}
