package net.pl3x.minimap.tile;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.texture.Drawable;
import net.pl3x.minimap.manager.FileManager;
import net.pl3x.minimap.manager.ThreadManager;
import net.pl3x.minimap.queue.DiskIOQueue;
import net.pl3x.minimap.queue.ReadQueue;
import net.pl3x.minimap.queue.WriteQueue;
import net.pl3x.minimap.scheduler.Scheduler;
import net.pl3x.minimap.util.Colors;
import net.pl3x.minimap.util.Mathf;

import java.nio.file.Path;

public class Tile {
    private final Identifier identifier;

    private final ClientWorld world;
    private final int regionX;
    private final int regionZ;

    private final Image base;
    private final Image biomes;
    private final Image height;
    private final Image water;
    private final Image light;

    private NativeImageBackedTexture texture;

    private boolean ready;
    private long lastUsed;
    private long lastSaved;
    private long lastUploaded;

    public Tile(ClientWorld world, int regionX, int regionZ) {
        this.identifier = new Identifier(MiniMap.MODID, String.format("tile_%s_%d_%d", world.getRegistryKey().getValue().toString().replace(":", "-"), regionX, regionZ));

        this.world = world;
        this.regionX = regionX;
        this.regionZ = regionZ;

        Path dir = FileManager.INSTANCE.getWorldDir(world);
        this.base = new Image(dir.resolve(String.format("base/%d_%d.png", regionX, regionZ)));
        this.biomes = new Image(dir.resolve(String.format("biomes/%d_%d.png", regionX, regionZ)));
        this.height = new Image(dir.resolve(String.format("height/%d_%d.png", regionX, regionZ)));
        this.water = new Image(dir.resolve(String.format("water/%d_%d.png", regionX, regionZ)));
        this.light = new Image(dir.resolve(String.format("light/%d_%d.png", regionX, regionZ)));
    }

    public ClientWorld world() {
        return this.world;
    }

    public int getRegionX() {
        return this.regionX;
    }

    public int getRegionZ() {
        return this.regionZ;
    }

    public void load() {
        DiskIOQueue.INSTANCE.read(new ReadQueue(this));
    }

    public void save() {
        this.lastSaved = Scheduler.INSTANCE.getCurrentTick();
        if (this.ready) {
            // only save if tile is ready to avoid saving blank png before we loaded
            DiskIOQueue.INSTANCE.write(new WriteQueue(this));
        }
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public NativeImageBackedTexture getTexture() {
        return this.texture;
    }

    public Image getBase() {
        return this.base;
    }

    public Image getBiomes() {
        return this.biomes;
    }

    public Image getHeight() {
        return this.height;
    }

    public Image getFluids() {
        return this.water;
    }

    public Image getLight() {
        return this.light;
    }

    public void use() {
        this.lastUsed = Scheduler.INSTANCE.getCurrentTick();
    }

    public long getLastUsed() {
        return this.lastUsed;
    }

    public long getLastSaved() {
        return this.lastSaved;
    }

    public long getLastUploaded() {
        return this.lastUploaded;
    }

    public boolean isReady() {
        return this.ready && getTexture() != null;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public void upload() {
        // first upload, create and register texture
        if (this.texture == null) {
            // can only register on render thread
            if (!RenderSystem.isOnRenderThread()) {
                RenderSystem.recordRenderCall(this::upload);
                return;
            }
            // register it
            this.texture = new NativeImageBackedTexture(MiniMap.TILE_SIZE, MiniMap.TILE_SIZE, true);
            MiniMap.CLIENT.getTextureManager().registerTexture(this.identifier, this.texture);
        }

        NativeImage image = this.texture.getImage();
        if (image == null) {
            // not ready, skip
            return;
        }

        ThreadManager.INSTANCE.runAsync(
                () -> {
                    int color = 0xFF << 24;
                    float skylight = this.world.getStarBrightness(1F) * 15;
                    for (int x = 0; x < MiniMap.TILE_SIZE; x++) {
                        for (int z = 0; z < MiniMap.TILE_SIZE; z++) {
                            if (Config.getConfig().layers.base) {
                                color = getBase().getPixel(x, z);
                            }
                            if (Config.getConfig().layers.biomes) {
                                color = getBiomes().getPixel(x, z);
                            }
                            if (Config.getConfig().layers.heightmap) {
                                color = Colors.mix(color, getHeight().getPixel(x, z));
                            }
                            if (Config.getConfig().layers.fluids) {
                                color = Colors.mix(color, getFluids().getPixel(x, z));
                            }
                            if (Config.getConfig().layers.lightmap) {
                                color = Colors.mix(color, (int) Mathf.clamp(0, 0xFF, (0xFF * Mathf.inverseLerp(0, 15, 15 - (this.world.getDimension().hasCeiling() ? 5 : skylight)) - Colors.alpha(getLight().getPixel(x, z))) / 1.2F) << 24);
                            }
                            image.setColor(x, z, color);
                        }
                    }
                },
                () -> {
                    this.lastUploaded = Scheduler.INSTANCE.getCurrentTick();
                    this.texture.upload();
                },
                ThreadManager.INSTANCE.getTileUpdaterExecutor()
        );
    }

    public void draw(MatrixStack matrixStack, float x, float z) {
        Drawable.draw(matrixStack, this.getIdentifier(), x, z, x + MiniMap.TILE_SIZE, z + MiniMap.TILE_SIZE, 0, 0, 1, 1);
    }
}
