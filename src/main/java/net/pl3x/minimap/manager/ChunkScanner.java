package net.pl3x.minimap.manager;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.config.Config;
import net.pl3x.minimap.gui.animation.Easing;
import net.pl3x.minimap.scheduler.Scheduler;
import net.pl3x.minimap.scheduler.Task;
import net.pl3x.minimap.tile.Tile;
import net.pl3x.minimap.util.Biomes;
import net.pl3x.minimap.util.Colors;
import net.pl3x.minimap.util.Mathf;
import net.pl3x.minimap.util.Numbers;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChunkScanner {
    public static final ChunkScanner INSTANCE = new ChunkScanner();

    private final Queue<Chunk> loadedChunks = new ConcurrentLinkedQueue<>();

    private ScanTask tickTask;

    private ChunkScanner() {
        ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            if (MiniMap.INSTANCE.getWorld() != world) {
                this.loadedChunks.clear();
                MiniMap.INSTANCE.setWorld(world);
                TileManager.INSTANCE.tiles.forEach((key, tile) -> tile.unload());
                TileManager.INSTANCE.tiles.clear();
            }
            this.loadedChunks.add(chunk);
        });
        ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> this.loadedChunks.remove(chunk));
    }

    public void start() {
        this.loadedChunks.clear();
        this.tickTask = new ScanTask();
        Scheduler.INSTANCE.addTask(this.tickTask);
    }

    public void stop() {
        if (this.tickTask != null) {
            this.tickTask.asyncScanTask.cancel();
            this.tickTask.cancel();
            this.tickTask = null;
        }
        this.loadedChunks.clear();
    }

    private static class ScanTask extends Task {
        private final AsyncScan asyncScanTask = new AsyncScan();

        private boolean running;

        private ScanTask() {
            super(20, true);
        }

        @Override
        public void run() {
            if (this.running) {
                // haven't finished previous run
                return;
            }

            ClientPlayerEntity player = MiniMap.INSTANCE.getPlayer();
            if (player == null) {
                return;
            }

            this.running = true;

            ThreadManager.INSTANCE.runAsync(this.asyncScanTask, () -> this.running = false);
        }
    }

    private static class AsyncScan implements Runnable {
        private final List<Block> invisibleBlocks = new ArrayList<>();

        private boolean cancelled;

        private AsyncScan() {
            invisibleBlocks.add(Blocks.TALL_GRASS);
            invisibleBlocks.add(Blocks.GRASS);
        }

        public void cancel() {
            this.cancelled = true;
        }

        @Override
        public void run() {
            if (this.cancelled) {
                return;
            }

            ClientWorld world = MiniMap.INSTANCE.getWorld();
            if (world == null) {
                return;
            }

            // reusable variables to minimize allocation churn
            int x, z, alpha, color, height, light, fluidColor, blockX, blockZ, pixelX, pixelZ;
            float depth;
            boolean lava;
            Tile tile;
            Biome biome;
            BlockState state;
            BlockPos fluidPos;
            BlockPos.Mutable pos = new BlockPos.Mutable();
            BlockPos.Mutable pos2 = new BlockPos.Mutable();

            // iterate each chunk to map it
            for (Chunk chunk : ChunkScanner.INSTANCE.loadedChunks) {
                blockX = Numbers.chunkToBlock(chunk.getPos().x);
                blockZ = Numbers.chunkToBlock(chunk.getPos().z);

                // get the tile this chunk belongs to
                tile = TileManager.INSTANCE.getTile(world, Numbers.chunkToRegion(chunk.getPos().x), Numbers.chunkToRegion(chunk.getPos().z), true);
                if (!tile.isReady()) {
                    // tile is not currently ready
                    continue;
                }

                // iterate each block in this chunk
                for (x = 0; x < 16; x++) {
                    for (z = 0; z < 16; z++) {
                        if (this.cancelled) {
                            return;
                        }

                        // current block position in the world at the highest Y coordinate
                        pos.set(blockX + x, chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z) + 1, blockZ + z);

                        // current pixel in the tile image
                        pixelX = pos.getX() & MiniMap.TILE_SIZE - 1;
                        pixelZ = pos.getZ() & MiniMap.TILE_SIZE - 1;

                        // reset fluid tracker
                        fluidPos = null;

                        if (Config.getConfig().layers.base) {
                            if (world.getDimension().hasCeiling()) {
                                // start from bottom up until we find air
                                pos.setY(world.getBottomY());
                                do {
                                    pos.move(Direction.UP);
                                    state = chunk.getBlockState(pos);
                                } while (!state.isAir() && pos.getY() < world.getHeight());
                            }
                            do {
                                pos.move(Direction.DOWN);
                                state = chunk.getBlockState(pos);
                                color = state.getMapColor(world, pos).color;
                                if (fluidPos == null) {
                                    // only track the first time we see a liquid
                                    fluidPos = !state.getFluidState().isEmpty() ? pos.mutableCopy() : null;
                                }
                            } while (pos.getY() > world.getBottomY() && (color == 0 || !state.getFluidState().isEmpty() || this.invisibleBlocks.contains(state.getBlock())));
                            tile.getBase().setPixel(pixelX, pixelZ, (0xFF << 24) | color);
                        }

                        if (this.cancelled) {
                            return;
                        }

                        if (Config.getConfig().layers.biomes) {
                            // get the biome of the current block
                            // see ClientWorldMixin for a hack that
                            // allows this method to return null
                            biome = world.getBiome(pos);
                            // only plot the pixel if a biome was found
                            // edge of view distance has a lot of blocks
                            // with missing biomes, so this is a needed check
                            if (biome != null) {
                                tile.getBiomes().setPixel(pixelX, pixelZ, Biomes.Color.get(world, biome));
                            }
                        }

                        if (this.cancelled) {
                            return;
                        }

                        if (Config.getConfig().layers.height) {
                            height = 0x22 << 24;
                            if (z + 1 < 16 && iterateDown(world, getY(chunk, pos2, blockX, blockZ, x, z + 1)).getY() < pos.getY()) {
                                // south neighbor block is lower, mark this block a darker shade
                                height = 0x44 << 24;
                            }
                            if (x + 1 < 16 && iterateDown(world, getY(chunk, pos2, blockX, blockZ, x + 1, z)).getY() < pos.getY()) {
                                // east neighbor block is lower, mark this block a darker shade
                                height = 0x44 << 24;
                            }
                            if (z - 1 > 0 && iterateDown(world, getY(chunk, pos2, blockX, blockZ, x, z - 1)).getY() < pos.getY()) {
                                // north neighbor block is lower, mark this block a lighter shade
                                height = 0x00;
                            }
                            if (x - 1 > 0 && iterateDown(world, getY(chunk, pos2, blockX, blockZ, x - 1, z)).getY() < pos.getY()) {
                                // west neighbor block is lower, mark this block a lighter shade
                                height = 0x00;
                            }
                            tile.getHeight().setPixel(pixelX, pixelZ, height);
                        }

                        if (this.cancelled) {
                            return;
                        }

                        if (Config.getConfig().layers.fluids) {
                            fluidColor = 0;
                            if (fluidPos != null) {
                                // setup initial fluid stuff
                                depth = 0F;
                                state = chunk.getBlockState(pos2.set(fluidPos));
                                if (state.isOf(Blocks.LAVA)) {
                                    lava = true;
                                    fluidColor = 0xFFEA5C0F;
                                } else {
                                    lava = false;
                                    fluidColor = MapColor.WATER_BLUE.color;
                                }
                                // iterate down until we don't find any more fluids
                                do {
                                    state = chunk.getBlockState(pos2.setY(pos2.getY() - 1));
                                    color = state.getMapColor(world, pos2).color;
                                    depth += 0.025F;
                                } while (pos2.getY() > world.getBottomY() && (color == 0 || !state.getFluidState().isEmpty() || this.invisibleBlocks.contains(state.getBlock())));
                                // let's do some maths to get pretty fluid colors based on depth
                                fluidColor = Colors.lerpARGB(fluidColor, 0xFF000000, Mathf.clamp(0, lava ? 0.3F : 0.45F, Easing.Cubic.out(depth / 1.5F)));
                                fluidColor = Colors.setAlpha(lava ? 0xFF : (int) (Easing.Quintic.out(Mathf.clamp(0, 1, depth * 5F)) * 0xFF), fluidColor);
                            }
                            tile.getFluids().setPixel(pixelX, pixelZ, fluidColor);
                        }

                        if (this.cancelled) {
                            return;
                        }

                        if (Config.getConfig().layers.light) {
                            // store light levels as black pixels - we will invert this during rendering
                            light = world.getLightLevel(LightType.BLOCK, (fluidPos == null ? pos : fluidPos).up());
                            alpha = (int) (Mathf.inverseLerp(0, 15, light) * 0xFF);
                            tile.getLight().setPixel(pixelX, pixelZ, Colors.setAlpha(alpha, 0x00000000));
                        }
                    }
                }

                if (this.cancelled) {
                    return;
                }

                // todo - move this to its own repeating task separate from this
                tile.upload();
            }
        }

        private BlockPos.Mutable getY(Chunk chunk, BlockPos.Mutable pos, int blockX, int blockZ, int offsetX, int offsetZ) {
            return pos.set(blockX + offsetX, chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, offsetX, offsetZ) + 1, blockZ + offsetZ);
        }

        private BlockPos iterateDown(World world, BlockPos.Mutable pos2) {
            BlockState state;
            int color;
            do {
                pos2.move(Direction.DOWN);
                state = world.getBlockState(pos2);
                color = state.getMapColor(world, pos2).color;
            } while (pos2.getY() > world.getBottomY() && (color == 0 || !state.getFluidState().isEmpty() || invisibleBlocks.contains(state.getBlock())));
            return pos2;
        }
    }
}
