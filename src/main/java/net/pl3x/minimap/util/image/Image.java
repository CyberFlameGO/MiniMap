package net.pl3x.minimap.util.image;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.gui.Tile;
import net.pl3x.minimap.manager.ChunkScanner;
import net.pl3x.minimap.util.Colors;
import net.pl3x.minimap.util.Mathf;

public class Image {
    private final Identifier identifier;
    private final Identifier lightIdentifier;

    private NativeImageBackedTexture texture;
    private NativeImageBackedTexture lightTexture;

    public Image(Identifier identifier) {
        this(identifier, false);
    }

    public Image(Identifier identifier, boolean light) {
        this.identifier = identifier;
        this.lightIdentifier = light ? new Identifier(MiniMap.MODID, identifier.getPath() + "_light") : null;
    }

    public void initialize() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::initialize);
            return;
        }
        if (this.texture == null) {
            this.texture = new NativeImageBackedTexture(MiniMap.TILE_SIZE, MiniMap.TILE_SIZE, true);
            MiniMap.CLIENT.getTextureManager().registerTexture(this.identifier, this.texture);
        }
        if (this.lightIdentifier != null && this.lightTexture == null) {
            this.lightTexture = new NativeImageBackedTexture(MiniMap.TILE_SIZE, MiniMap.TILE_SIZE, true);
            MiniMap.CLIENT.getTextureManager().registerTexture(this.lightIdentifier, this.lightTexture);
        }
    }

    public Identifier identifier() {
        return this.identifier;
    }

    public NativeImage getImage() {
        if (this.texture == null) {
            return null;
        }
        return this.texture.getImage();
    }

    public void upload(Tile tile) {
        if (this.texture == null) {
            return;
        }

        if (this.lightTexture != null) {
            updateLight(tile);
        } else {
            this.texture.upload();
        }
    }

    public void draw(MatrixStack matrixStack, float x0, float y0, float x1, float y1) {
        draw(matrixStack, this.lightTexture != null ? this.lightIdentifier : this.identifier, x0, y0, x1, y1);
    }

    private void draw(MatrixStack matrixStack, Identifier identifier, float x0, float y0, float x1, float y1) {
        if (this.texture == null) {
            return;
        }
        if (this.texture.getImage() == null) {
            return;
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, identifier);
        Matrix4f model = matrixStack.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(model, x0, y0, 0F).texture(0, 0).next();
        bufferBuilder.vertex(model, x0, y1, 0F).texture(0, 1).next();
        bufferBuilder.vertex(model, x1, y1, 0F).texture(1, 1).next();
        bufferBuilder.vertex(model, x1, y0, 0F).texture(1, 0).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
    }

    private void updateLight(Tile tile) {
        if (this.texture == null) {
            return;
        }
        if (this.lightTexture == null) {
            return;
        }
        NativeImage image = this.texture.getImage();
        if (image == null) {
            return;
        }
        NativeImage lightImage = this.lightTexture.getImage();
        if (lightImage == null) {
            return;
        }
        NativeImage baseImage = tile.imageBase().getImage();
        if (baseImage == null) {
            return;
        }

        float skylight = ChunkScanner.INSTANCE.skyLight;

        for (int x = 0; x < MiniMap.TILE_SIZE; x++) {
            for (int z = 0; z < MiniMap.TILE_SIZE; z++) {
                if (baseImage.getColor(x, z) == 0) {
                    lightImage.setColor(x, z, 0);
                    continue;
                }
                lightImage.setColor(x, z, (int) Mathf.clamp(0, 0xFF, (0xFF * Mathf.inverseLerp(0, 15, 15 - skylight) - Colors.alpha(image.getColor(x, z))) / 1.2F) << 24);
            }
        }

        this.lightTexture.upload();
    }
}
