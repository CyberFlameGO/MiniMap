package net.pl3x.minimap.manager;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.pl3x.minimap.config.Config;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadManager {
    public static final ThreadManager INSTANCE = new ThreadManager();
    public static final String CHUNK_SCANNER_THREAD_NAME = "Minimap-Chunk-Scanner";
    public static final String LAYER_UPDATER_THREAD_NAME = "Minimap-Layer-Updater";
    public static final String TILE_UPDATER_THREAD_NAME = "Minimap-Tile-Updater";
    public static final String READ_IO_THREAD_NAME = "Minimap-IO-Read";
    public static final String WRITE_IO_THREAD_NAME = "Minimap-IO-Write";
    public static final String HTTP_IO_THREAD_NAME = "Minimap-IO-Http";

    private ExecutorService chunkScannerExecutor;
    private ExecutorService layerUpdaterExecutor;
    private ExecutorService tileUpdaterExecutor;
    private ExecutorService readIOExecutor;
    private ExecutorService writeIOExecutor;
    private ExecutorService httpIOExecutor;

    private ThreadManager() {
    }

    public ExecutorService getChunkScannerExecutor() {
        if (this.chunkScannerExecutor == null) {
            this.chunkScannerExecutor = Executors.newFixedThreadPool(Math.max(1, getThreads()), new ThreadFactoryBuilder().setNameFormat(CHUNK_SCANNER_THREAD_NAME + "-%d").build());
        }
        return this.chunkScannerExecutor;
    }

    public ExecutorService getLayerUpdaterExecutor() {
        if (this.layerUpdaterExecutor == null) {
            this.layerUpdaterExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat(LAYER_UPDATER_THREAD_NAME).build());
        }
        return this.layerUpdaterExecutor;
    }

    public ExecutorService getTileUpdaterExecutor() {
        if (this.tileUpdaterExecutor == null) {
            this.tileUpdaterExecutor = Executors.newFixedThreadPool(Math.max(1, getThreads()), new ThreadFactoryBuilder().setNameFormat(TILE_UPDATER_THREAD_NAME).build());
        }
        return this.tileUpdaterExecutor;
    }

    public ExecutorService getReadIOExecutor() {
        if (this.readIOExecutor == null) {
            this.readIOExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat(READ_IO_THREAD_NAME).build());
        }
        return this.readIOExecutor;
    }

    public ExecutorService getWriteIOExecutor() {
        if (this.writeIOExecutor == null) {
            this.writeIOExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat(WRITE_IO_THREAD_NAME).build());
        }
        return this.writeIOExecutor;
    }

    public ExecutorService getHttpIOExecutor() {
        if (this.httpIOExecutor == null) {
            this.httpIOExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat(HTTP_IO_THREAD_NAME).build());
        }
        return this.httpIOExecutor;
    }

    public void runAsync(Runnable task, ExecutorService executor) {
        runAsync(task, null, executor);
    }

    public void runAsync(Runnable task, Runnable whenComplete, ExecutorService executor) {
        CompletableFuture.runAsync(task, executor)
            .exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            })
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    throwable.printStackTrace();
                }
                if (whenComplete != null) {
                    whenComplete.run();
                }
            });
    }

    private int getThreads() {
        int threads = Config.getConfig().threads;
        if (threads < 1) {
            threads = Runtime.getRuntime().availableProcessors() / 3;
        }
        return threads;
    }
}
