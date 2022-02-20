package net.pl3x.minimap.manager;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.gui.Tile;
import net.pl3x.minimap.gui.animation.Easing;
import net.pl3x.minimap.scheduler.Scheduler;
import net.pl3x.minimap.scheduler.Task;
import net.pl3x.minimap.util.Biomes;
import net.pl3x.minimap.util.Colors;
import net.pl3x.minimap.util.Mathf;
import net.pl3x.minimap.util.Numbers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkScanner {
    public static final ChunkScanner INSTANCE = new ChunkScanner();

    private final Set<MapChunk> loadedChunks = ConcurrentHashMap.newKeySet();
    private final List<Block> invisibleBlocks = new ArrayList<>();

    private Task task;
    public float skyLight;

    private ChunkScanner() {
        ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> loadedChunks.add(new MapChunk(world, chunk)));
        ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> loadedChunks.remove(new MapChunk(world, chunk)));
    }

    public void start() {
        this.loadedChunks.clear();
        this.task = new ScanTask();
        Scheduler.INSTANCE.addTask(this.task);
    }

    public void stop() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
        this.loadedChunks.clear();
    }

    public record MapChunk(World world, Chunk chunk) {
    }

    private class ScanTask extends Task {
        private ScanTask() {
            super(20, true);
        }

        @Override
        public void run() {
            ClientPlayerEntity player = MiniMap.INSTANCE.player;
            if (player == null) {
                return;
            }

            World world = player.world;
            if (world == null) {
                return;
            }

            Identifier identifier = world.getRegistryKey().getValue();
            //System.out.println("Scanning " + loadedChunks.size() + " loaded chunks in " + identifier);

            Set<Tile> dirtyTiles = new HashSet<>();
            invisibleBlocks.add(Blocks.TALL_GRASS);
            invisibleBlocks.add(Blocks.GRASS);

            CompletableFuture.runAsync(() -> {

                skyLight = ((ClientWorld) world).getStarBrightness(1F) * 15;

                BlockPos.Mutable pos = new BlockPos.Mutable();
                BlockPos.Mutable pos2 = new BlockPos.Mutable();
                int color;
                BlockState state;
                BlockPos isWater;

                for (MapChunk mapChunk : loadedChunks) {
                    Chunk chunk = mapChunk.chunk;

                    int chunkX = chunk.getPos().x;
                    int chunkZ = chunk.getPos().z;
                    int regionX = Numbers.chunkToRegion(chunkX);
                    int regionZ = Numbers.chunkToRegion(chunkZ);

                    Tile.Key key = new Tile.Key(identifier, regionX, regionZ);
                    Tile tile = TileManager.INSTANCE.getTile(key);
                    if (tile.initialize()) {
                        continue;
                    }

                    NativeImage imageBase = tile.imageBase().getImage();
                    NativeImage imageBiome = tile.imageBiome().getImage();
                    NativeImage imageHeight = tile.imageHeight().getImage();
                    NativeImage imageWater = tile.imageWater().getImage();
                    NativeImage imageLight = tile.imageLight().getImage();
                    if (imageBase == null || imageBiome == null || imageHeight == null || imageWater == null || imageLight == null) {
                        continue;
                    }

                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            pos.set(
                                    Numbers.chunkToBlock(chunkX) + x,
                                    chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z) + 1,
                                    Numbers.chunkToBlock(chunkZ) + z
                            );
                            isWater = null;

                            int pixelX = pos.getX() & MiniMap.TILE_SIZE - 1;
                            int pixelZ = pos.getZ() & MiniMap.TILE_SIZE - 1;

                            // base layer
                            {
                                do {
                                    pos.move(Direction.DOWN);
                                    state = chunk.getBlockState(pos);
                                    color = state.getMapColor(world, pos).color;
                                    if (isWater == null) {
                                        isWater = color == MapColor.WATER_BLUE.color ? pos.mutableCopy() : null;
                                    }
                                } while (pos.getY() > world.getBottomY() && (color == 0 || color == MapColor.WATER_BLUE.color || invisibleBlocks.contains(state.getBlock())));
                                imageBase.setColor(pixelX, pixelZ, Colors.rgb2bgr((0xFF << 24) | color));
                            }

                            // biome layer
                            {
                                Biomes.ALLOW_NULL_BIOMES = true;
                                Biome biome = world.getBiome(pos);
                                if (biome != null) {
                                    imageBiome.setColor(pixelX, pixelZ, Colors.rgb2bgr(Biomes.Color.get(world, biome)));
                                }
                                Biomes.ALLOW_NULL_BIOMES = false;
                            }

                            // heightmap layer
                            {
                                int height = 0x22000000;
                                if (z + 1 < 16) {
                                    int y = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z + 1) + 1;
                                    iterateDown(world, pos2.set(Numbers.chunkToBlock(chunkX) + x, y, Numbers.chunkToBlock(chunkZ) + z + 1), state);
                                    if (pos2.getY() < pos.getY()) {
                                        height = 0x44000000;
                                    }
                                }
                                if (x + 1 < 16) {
                                    int y = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x + 1, z) + 1;
                                    iterateDown(world, pos2.set(Numbers.chunkToBlock(chunkX) + x + 1, y, Numbers.chunkToBlock(chunkZ) + z), state);
                                    if (pos2.getY() < pos.getY()) {
                                        height = 0x44000000;
                                    }
                                }
                                if (z - 1 > 0) {
                                    int y = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z - 1) + 1;
                                    iterateDown(world, pos2.set(Numbers.chunkToBlock(chunkX) + x, y, Numbers.chunkToBlock(chunkZ) + z - 1), state);
                                    if (pos2.getY() < pos.getY()) {
                                        height = 0x00000000;
                                    }
                                }
                                if (x - 1 > 0) {
                                    int y = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x - 1, z) + 1;
                                    iterateDown(world, pos2.set(Numbers.chunkToBlock(chunkX) + x - 1, y, Numbers.chunkToBlock(chunkZ) + z), state);
                                    if (pos2.getY() < pos.getY()) {
                                        height = 0x00000000;
                                    }
                                }
                                imageHeight.setColor(pixelX, pixelZ, height);
                            }

                            // water layer
                            {
                                int waterColor;
                                if (isWater != null) {
                                    float depth = 0.025F;
                                    pos2.set(isWater);
                                    do {
                                        pos2.setY(pos2.getY() - 1);
                                        state = chunk.getBlockState(pos2);
                                        color = state.getMapColor(world, pos2).color;
                                        depth += 0.025F;
                                    } while (pos2.getY() > world.getBottomY() && (color == 0 || color == MapColor.WATER_BLUE.color || invisibleBlocks.contains(state.getBlock())));
                                    int water = Colors.lerpARGB(MapColor.WATER_BLUE.color, 0xFF000000, Mathf.clamp(0, 0.45F, Easing.Cubic.out(depth / 1.5F)));
                                    waterColor = Colors.setAlpha((int) (Easing.Quintic.out(Mathf.clamp(0, 1, depth * 5F)) * 0xFF), water);
                                    imageWater.setColor(pixelX, pixelZ, Colors.rgb2bgr(waterColor));
                                }
                            }

                            // light layer
                            {
                                // store light levels as black pixels - will inverse during rendering
                                int blockLight = world.getLightLevel(LightType.BLOCK, pos.up());
                                int alpha = (int) (Mathf.inverseLerp(0, 15, blockLight) * 0xFF);
                                imageLight.setColor(pixelX, pixelZ, Colors.setAlpha(alpha, 0x00000000));
                            }
                        }
                    }

                    dirtyTiles.add(tile);
                }

            }).exceptionally((throwable) -> {
                throwable.printStackTrace();
                return null;
            }).whenComplete((result, throwable) ->
                    dirtyTiles.forEach(Tile::upload)
            );
        }

        @SuppressWarnings("ParameterCanBeLocal") // we pass state around to avoid many allocations
        private void iterateDown(World world, BlockPos.Mutable pos2, BlockState state) {
            int color;
            do {
                pos2.move(Direction.DOWN);
                state = world.getBlockState(pos2);
                color = state.getMapColor(world, pos2).color;
            } while (pos2.getY() > world.getBottomY() && (color == 0 || color == MapColor.WATER_BLUE.color || invisibleBlocks.contains(state.getBlock())));
        }
    }
}
