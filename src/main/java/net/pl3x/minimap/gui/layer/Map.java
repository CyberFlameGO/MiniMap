package net.pl3x.minimap.gui.layer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.GL;
import net.pl3x.minimap.gui.texture.Drawable;
import net.pl3x.minimap.manager.ThreadManager;
import net.pl3x.minimap.manager.TileManager;
import net.pl3x.minimap.tile.Tile;
import net.pl3x.minimap.util.Mathf;
import net.pl3x.minimap.util.Numbers;
import org.lwjgl.opengl.GL11;

public class Map extends Layer {
    private final AsyncUpdate asyncUpdateTask;

    private final Identifier mapId = new Identifier(MiniMap.MODID, "map_layer");
    private NativeImageBackedTexture map;

    public Map() {
        super();
        this.asyncUpdateTask = new AsyncUpdate(this.mm);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (this.map == null || this.map.getImage() == null) {
            return;
        }

        float halfSize = mm.getSize() / 2F;
        float scale = mm.getSize() / mm.getDeltaZoom();
        float scale2 = scale * 2F;

        float x0 = mm.getCenterX() - halfSize + scale;
        float x1 = x0 + mm.getSize() - scale2;
        float y0 = mm.getCenterY() - halfSize + scale;
        float y1 = y0 + mm.getSize() - scale2;

        float u = (Tile.SIZE - mm.getDeltaZoom()) / Tile.SIZE / 2F;
        float v = 1F - u;

        // uses blend which only writes where high alpha values exist
        RenderSystem.blendFunc(GL11.GL_DST_ALPHA, GL11.GL_ONE_MINUS_DST_ALPHA);

        matrixStack.push();
        if (!Config.getConfig().northLocked) {
            // rotate map opposite of player angle
            GL.rotateScene(matrixStack, mm.getCenterX(), mm.getCenterY(), -mm.getAngle());
            if (!Config.getConfig().circular) {
                // scale map if square and not north locked to hide missing pixels in corners when rotating
                GL.scaleScene(matrixStack, mm.getCenterX(), mm.getCenterY(), Mathf.SQRT_OF_2);
            }
        }
        Drawable.draw(matrixStack, this.mapId, x0, y0, x1, y1, u, u, v, v);
        matrixStack.pop();
    }

    @Override
    public void update() {
        if (this.asyncUpdateTask.running) {
            // still updating previous run.
            // skip this run to prevent tearing
            return;
        }

        // check if we have a map texture
        if (this.map == null) {
            // load the map texture
            this.map = new NativeImageBackedTexture(Tile.SIZE, Tile.SIZE, true);
            MiniMap.CLIENT.getTextureManager().registerTexture(this.mapId, this.map);
            return;
        }

        NativeImage image = this.map.getImage();
        if (image == null) {
            // image isn't ready yet
            return;
        }

        this.asyncUpdateTask.running = true;
        this.asyncUpdateTask.image = image;

        ThreadManager.INSTANCE.runAsync(
            this.asyncUpdateTask,
            () -> {
                this.asyncUpdateTask.running = false;
                if (!this.asyncUpdateTask.cancelled) {
                    this.map.upload();
                }
            },
            ThreadManager.INSTANCE.getLayerUpdaterExecutor());
    }

    @Override
    public void stop() {
        this.asyncUpdateTask.cancelled = true;
    }

    private static class AsyncUpdate implements Runnable {
        private final MiniMap mm;

        private NativeImage image;
        private boolean running;
        private boolean cancelled;

        Tile tile;
        NativeImage source;
        int blockX, blockZ;
        double playerX, playerZ;

        private AsyncUpdate(MiniMap mm) {
            this.mm = mm;
        }

        @Override
        public void run() {
            if (this.cancelled) {
                return;
            }
            this.playerX = mm.getPlayer().getX() - Tile.SIZE / 2D;
            this.playerZ = mm.getPlayer().getZ() - Tile.SIZE / 2D;

            for (int x = 0; x < Tile.SIZE; x++) {
                for (int z = 0; z < Tile.SIZE; z++) {
                    if (this.cancelled) {
                        return;
                    }

                    this.blockX = (int) Math.round(this.playerX + x);
                    this.blockZ = (int) Math.round(this.playerZ + z);

                    this.tile = TileManager.INSTANCE.getTile(mm.getWorld(), Numbers.blockToRegion(this.blockX), Numbers.blockToRegion(this.blockZ), false);
                    if (this.tile == null || !this.tile.isReady()) {
                        // tile not ready
                        this.image.setColor(x, z, 0);
                        continue;
                    }

                    this.source = this.tile.getTexture().getImage();
                    if (this.source == null) {
                        // source image not ready
                        this.image.setColor(x, z, 0);
                        continue;
                    }

                    this.image.setColor(x, z, this.source.getColor(this.blockX & (Tile.SIZE - 1), this.blockZ & (Tile.SIZE - 1)));
                }
            }
        }
    }
}
