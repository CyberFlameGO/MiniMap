package net.pl3x.minimap.gui.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.pl3x.minimap.util.Colors;

public class Drawable {
    public final Identifier identifier;

    public Drawable(Identifier identifier) {
        this.identifier = identifier;
    }

    public void tint(MatrixStack matrixStack, float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1, int color) {
        RenderSystem.setShaderColor(
                Colors.red(color) / 255F,
                Colors.green(color) / 255F,
                Colors.blue(color) / 255F,
                Colors.alpha(color) / 255F
        );
        draw(matrixStack, x0, y0, x1, y1, u0, v0, u1, v1);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    }

    public void draw(MatrixStack matrixStack, float x, float y, float width, float height) {
        draw(matrixStack, x, y, x + width, y + height, 0F, 0F, 1F, 1F);
    }

    public void draw(MatrixStack matrixStack, float x0, float y0, float x1, float y1, float u, float v) {
        draw(matrixStack, x0, y0, x1, y1, u, u, v, v);
    }

    public void draw(MatrixStack matrixStack, float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.identifier);
        Matrix4f model = matrixStack.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(model, x0, y0, 0F).texture(u0, v0).next();
        bufferBuilder.vertex(model, x0, y1, 0F).texture(u0, v1).next();
        bufferBuilder.vertex(model, x1, y1, 0F).texture(u1, v1).next();
        bufferBuilder.vertex(model, x1, y0, 0F).texture(u1, v0).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
    }
}
