package net.pl3x.minimap.tile;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.animation.Easing;
import net.pl3x.minimap.gui.texture.Drawable;
import net.pl3x.minimap.manager.ChunkScanner;
import net.pl3x.minimap.manager.FileManager;
import net.pl3x.minimap.manager.ThreadManager;
import net.pl3x.minimap.queue.DiskIOQueue;
import net.pl3x.minimap.queue.ReadQueue;
import net.pl3x.minimap.queue.WriteQueue;
import net.pl3x.minimap.scheduler.Scheduler;
import net.pl3x.minimap.util.Biomes;
import net.pl3x.minimap.util.Colors;
import net.pl3x.minimap.util.Mathf;
import net.pl3x.minimap.util.Numbers;

import java.nio.file.Path;

public class Tile {
    public static final int SIZE = 512;

    private final Identifier identifier;

    private final ClientWorld world;
    private final int regionX;
    private final int regionZ;

    private final Image baseImage;
    private final Image biomesImage;
    private final Image heightmapImage;
    private final Image fluidsImage;
    private final Image lightmapImage;

    private NativeImageBackedTexture texture;

    private boolean ready;
    private boolean needsSave;
    private long lastUsed;
    private long lastSaved;
    private long lastUploaded;

    public Tile(ClientWorld world, int regionX, int regionZ) {
        this.identifier = new Identifier(MiniMap.MODID, String.format("tile_%s_%d_%d", world.getRegistryKey().getValue().toString().replace(":", "-"), regionX, regionZ));

        this.world = world;
        this.regionX = regionX;
        this.regionZ = regionZ;

        Path dir = FileManager.INSTANCE.getWorldDir(world);
        this.baseImage = new Image(dir.resolve(String.format("base/%d_%d.png", regionX, regionZ)));
        this.biomesImage = new Image(dir.resolve(String.format("biomes/%d_%d.png", regionX, regionZ)));
        this.heightmapImage = new Image(dir.resolve(String.format("height/%d_%d.png", regionX, regionZ)));
        this.fluidsImage = new Image(dir.resolve(String.format("water/%d_%d.png", regionX, regionZ)));
        this.lightmapImage = new Image(dir.resolve(String.format("light/%d_%d.png", regionX, regionZ)));
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public ClientWorld getWorld() {
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
        if (this.needsSave && this.ready) {
            // only save if tile is ready to avoid saving blank png before we loaded
            this.needsSave = false;
            setLastSaved(Scheduler.INSTANCE.getCurrentTick());
            DiskIOQueue.INSTANCE.write(new WriteQueue(this));
        }
    }

    public NativeImageBackedTexture getTexture() {
        return this.texture;
    }

    public Image getBaseImage() {
        return this.baseImage;
    }

    public Image getBiomesImage() {
        return this.biomesImage;
    }

    public Image getHeightmapImage() {
        return this.heightmapImage;
    }

    public Image getFluidsImage() {
        return this.fluidsImage;
    }

    public Image getLightmapImage() {
        return this.lightmapImage;
    }

    public boolean isReady() {
        return this.ready && getTexture() != null;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public void markToSave() {
        this.needsSave = true;
    }

    public long getLastUsed() {
        return this.lastUsed;
    }

    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }

    public long getLastSaved() {
        return this.lastSaved;
    }

    public void setLastSaved(long lastSaved) {
        this.lastSaved = lastSaved;
    }

    public long getLastUploaded() {
        return this.lastUploaded;
    }

    public void setLastUploaded(long lastUploaded) {
        this.lastUploaded = lastUploaded;
    }

    public void upload() {
        // on first upload, create and register texture
        if (this.texture == null) {
            // can only register on render thread
            if (!RenderSystem.isOnRenderThread()) {
                RenderSystem.recordRenderCall(this::upload);
                return;
            }
            // register it
            this.texture = new NativeImageBackedTexture(Tile.SIZE, Tile.SIZE, true);
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
                for (int x = 0; x < Tile.SIZE; x++) {
                    for (int z = 0; z < Tile.SIZE; z++) {
                        if (Config.getConfig().layers.base) {
                            color = getBaseImage().getPixel(x, z);
                        }
                        if (Config.getConfig().layers.biomes) {
                            color = getBiomesImage().getPixel(x, z);
                        }
                        if (Config.getConfig().layers.heightmap) {
                            color = Colors.mix(color, getHeightmapImage().getPixel(x, z));
                        }
                        if (Config.getConfig().layers.fluids) {
                            color = Colors.mix(color, getFluidsImage().getPixel(x, z));
                        }
                        if (Config.getConfig().layers.lightmap) {
                            color = Colors.mix(color, (int) Mathf.clamp(0, 0xFF, (0xFF * Mathf.inverseLerp(0, 15, 15 - (this.world.getDimension().hasCeiling() ? 5 : skylight)) - Colors.alpha(getLightmapImage().getPixel(x, z))) / 1.2F) << 24);
                        }
                        image.setColor(x, z, color);
                    }
                }
            },
            () -> {
                this.texture.upload();
                setLastUploaded(Scheduler.INSTANCE.getCurrentTick());
            },
            ThreadManager.INSTANCE.getTileUpdaterExecutor()
        );
    }

    public void draw(MatrixStack matrixStack, float x, float z) {
        Drawable.draw(matrixStack, getIdentifier(), x, z, x + Tile.SIZE, z + Tile.SIZE, 0, 0, 1, 1);
    }

    public void scanChunk(Chunk chunk, ChunkScanner.State scannerState) {
        BlockPos.Mutable pos = new BlockPos.Mutable();
        BlockPos.Mutable pos2 = new BlockPos.Mutable();

        int blockX = Numbers.chunkToBlock(chunk.getPos().x);
        int blockZ = Numbers.chunkToBlock(chunk.getPos().z);

        // iterate each block in this chunk
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (scannerState.isCancelled()) {
                    return;
                }

                // current block position in the world at the highest Y coordinate
                pos.set(blockX + x, 0, blockZ + z);
                pos.setY(chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, pos.getX(), pos.getZ()) + 1);
                getStartY(chunk, pos);

                // current pixel in the tile image
                int pixelX = pos.getX() & Tile.SIZE - 1;
                int pixelZ = pos.getZ() & Tile.SIZE - 1;

                // reset fluid tracker
                BlockPos fluidPos = null;

                // base layer
                {
                    BlockState state;
                    int color;
                    do {
                        pos.move(Direction.DOWN);
                        state = chunk.getBlockState(pos);
                        color = state.getMapColor(getWorld(), pos).color;
                        if (fluidPos == null) {
                            // only track the first time we see a liquid
                            fluidPos = !state.getFluidState().isEmpty() ? pos.mutableCopy() : null;
                        }
                    } while (pos.getY() > getWorld().getBottomY() && (color == 0 || !state.getFluidState().isEmpty() || Colors.isInvisible(state)));
                    getBaseImage().setPixel(pixelX, pixelZ, (0xFF << 24) | color);
                }

                if (scannerState.isCancelled()) {
                    return;
                }

                // biomes layer
                {
                    // get the biome of the current block
                    // see ClientWorldMixin for a hack that
                    // allows this method to return null
                    Biome biome = Biomes.INSTANCE.getBiome(getWorld(), pos);
                    // only plot the pixel if a biome was found
                    // edge of view distance has a lot of blocks
                    // with missing biomes, so this is a needed check
                    if (biome != null) {
                        getBiomesImage().setPixel(pixelX, pixelZ, Biomes.Color.get(getWorld(), biome));
                    }
                }

                if (scannerState.isCancelled()) {
                    return;
                }

                // height layer
                {
                    int height = 0x22;
                    height = getHeightColor(chunk, pos, pos2.set(pos.getX() - 1, 0, pos.getZ()), height, 0x00);
                    height = getHeightColor(chunk, pos, pos2.set(pos.getX() + 1, 0, pos.getZ()), height, 0x44);
                    height = getHeightColor(chunk, pos, pos2.set(pos.getX(), 0, pos.getZ() - 1), height, 0x00);
                    height = getHeightColor(chunk, pos, pos2.set(pos.getX(), 0, pos.getZ() + 1), height, 0x44);
                    if (height >= 0) {
                        getHeightmapImage().setPixel(pixelX, pixelZ, height << 24);
                    }
                }

                if (scannerState.isCancelled()) {
                    return;
                }

                // fluids layer
                {
                    int color = 0;
                    if (fluidPos != null) {
                        // setup initial fluid stuff
                        float depth = 0F;
                        boolean lava;
                        if (chunk.getBlockState(pos2.set(fluidPos)).isOf(Blocks.LAVA)) {
                            lava = true;
                            color = 0xFFEA5C0F;
                        } else {
                            lava = false;
                            color = MapColor.WATER_BLUE.color;
                        }
                        // iterate down until we don't find any more fluids
                        BlockState state;
                        do {
                            pos2.move(Direction.DOWN);
                            state = chunk.getBlockState(pos2);
                            depth += 0.025F;
                        } while (pos2.getY() > getWorld().getBottomY() && (!state.getFluidState().isEmpty() || Colors.isInvisible(state)));
                        // let's do some maths to get pretty fluid colors based on depth
                        color = Colors.lerpARGB(color, 0xFF000000, Mathf.clamp(0, lava ? 0.3F : 0.45F, Easing.Cubic.out(depth / 1.5F)));
                        color = Colors.setAlpha(lava ? 0xFF : (int) (Easing.Quintic.out(Mathf.clamp(0, 1, depth * 5F)) * 0xFF), color);
                    }
                    getFluidsImage().setPixel(pixelX, pixelZ, color);
                }

                if (scannerState.isCancelled()) {
                    return;
                }

                // light layer
                {
                    // store light levels as black pixels - we will invert this during rendering
                    int light = getWorld().getLightLevel(LightType.BLOCK, (fluidPos == null ? pos : fluidPos).up());
                    int alpha = (int) (Mathf.inverseLerp(0, 15, light) * 0xFF);
                    getLightmapImage().setPixel(pixelX, pixelZ, Colors.setAlpha(alpha, 0x00000000));
                }
            }
        }
    }

    private int getHeightColor(Chunk chunk, BlockPos.Mutable pos, BlockPos.Mutable pos2, int oldColor, int newColor) {
        if (oldColor < 0 || getWorld().getChunk(pos2) instanceof EmptyChunk) {
            return -1;
        }
        pos2.setY(getWorld().getChunk(pos2).sampleHeightmap(Heightmap.Type.WORLD_SURFACE, pos2.getX(), pos2.getZ()) + 1);
        return iterateDown(getStartY(chunk, pos2)).getY() < pos.getY() ? newColor : oldColor;
    }

    private BlockPos.Mutable getStartY(Chunk chunk, BlockPos.Mutable pos) {
        // todo - add option for seeing through ceiling
        if (getWorld().getDimension().hasCeiling()) {
            int maxY = getWorld().getBottomY() + getWorld().getDimension().getLogicalHeight();
            // todo - possibly add option for bottomup/topdown scanning
            if (false) {
                // start from bottom up until we find air
                pos.setY(getWorld().getBottomY());
                do {
                    pos.move(Direction.UP);
                } while (!chunk.getBlockState(pos).isAir() && pos.getY() < maxY);
            } else {
                // start from top down until we find air
                pos.setY(maxY);
                do {
                    pos.move(Direction.DOWN);
                } while (!chunk.getBlockState(pos).isAir() && pos.getY() > getWorld().getBottomY());
            }
        }
        return pos;
    }

    private BlockPos iterateDown(BlockPos.Mutable pos) {
        BlockState state;
        do {
            pos.move(Direction.DOWN);
            state = getWorld().getBlockState(pos);
        } while (pos.getY() > getWorld().getBottomY() && (!state.getFluidState().isEmpty() || Colors.isInvisible(getWorld(), state, pos)));
        return pos;
    }
}
