package net.pl3x.minimap.manager;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.scheduler.Scheduler;
import net.pl3x.minimap.scheduler.Task;
import net.pl3x.minimap.tile.Tile;
import net.pl3x.minimap.util.Numbers;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChunkScanner {
    public static final ChunkScanner INSTANCE = new ChunkScanner();

    private final Queue<Chunk> loadedChunks = new ConcurrentLinkedQueue<>();

    private ScanTask scanTask;

    private ChunkScanner() {
        ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            if (MiniMap.INSTANCE.getWorld() != world) {
                stop();
                TileManager.INSTANCE.tiles.forEach((key, tile) -> tile.setReady(false));
                TileManager.INSTANCE.tiles.clear();
                MiniMap.INSTANCE.setWorld(world);
                start();
            }
            this.loadedChunks.add(chunk);
        });
        ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> this.loadedChunks.remove(chunk));
    }

    public void start() {
        stop();
        this.scanTask = new ScanTask();
        Scheduler.INSTANCE.addTask(this.scanTask);
    }

    public void stop() {
        if (this.scanTask != null) {
            this.scanTask.cancel();
            this.scanTask = null;
        }
        this.loadedChunks.clear();
    }

    private static class ScanTask extends Task {
        private final State state;
        private final AsyncScan asyncScanTask;

        private int tick;

        private ScanTask() {
            super(0, true);
            this.state = new State();
            this.asyncScanTask = new AsyncScan(this.state);
        }

        @Override
        public void cancel() {
            super.cancel();
            this.state.cancelled = true;
        }

        @Override
        public void run() {
            // todo - make this interval configurable
            if (this.tick++ < 20) {
                return;
            }

            if (this.state.running) {
                // haven't finished previous run
                return;
            }

            if (MiniMap.INSTANCE.getPlayer() == null) {
                // no player, game not ready
                return;
            }

            this.tick = 0;
            this.state.running = true;

            ThreadManager.INSTANCE.runAsync(
                this.asyncScanTask,
                () -> this.state.running = false,
                ThreadManager.INSTANCE.getChunkScannerExecutor()
            );
        }
    }

    private static class AsyncScan implements Runnable {
        private final State state;

        private AsyncScan(State state) {
            this.state = state;
        }

        @Override
        public void run() {
            if (this.state.cancelled) {
                return;
            }

            ClientWorld world = MiniMap.INSTANCE.getWorld();
            if (world == null) {
                return;
            }

            // iterate each chunk
            for (Chunk chunk : ChunkScanner.INSTANCE.loadedChunks) {
                // check if edge chunk
                if (isEdgeChunk(world, chunk)) {
                    // don't render edge chunks because they contain blocks that
                    // are missing biome information and also because color blending
                    // causes huge sections of incorrect colors
                    continue;
                }

                // get the tile this chunk belongs to
                Tile tile = TileManager.INSTANCE.getTile(
                    world,
                    Numbers.chunkToRegion(chunk.getPos().x),
                    Numbers.chunkToRegion(chunk.getPos().z),
                    true
                );

                if (tile.isReady()) {
                    // scan the chunk
                    tile.scanChunk(chunk, this.state);

                    // mark tile dirty
                    tile.markToSave();
                }
            }
        }

        private boolean isEdgeChunk(ClientWorld world, Chunk chunk) {
            ChunkPos pos = chunk.getPos();
            return world.getChunk(pos.x - 1, pos.z).isEmpty() // west
                || world.getChunk(pos.x + 1, pos.z).isEmpty() // east
                || world.getChunk(pos.x, pos.z - 1).isEmpty()  // north
                || world.getChunk(pos.x, pos.z + 1).isEmpty()  // south
                || world.getChunk(pos.x - 1, pos.z - 1).isEmpty()  // northwest
                || world.getChunk(pos.x - 1, pos.z + 1).isEmpty()  // southwest
                || world.getChunk(pos.x + 1, pos.z - 1).isEmpty()  // northeast
                || world.getChunk(pos.x + 1, pos.z + 1).isEmpty(); // southeast
        }
    }

    public static class State {
        private boolean running;
        private boolean cancelled;

        public boolean isCancelled() {
            return this.cancelled;
        }
    }
}
