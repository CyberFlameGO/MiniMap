package net.pl3x.minimap.manager;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.pl3x.minimap.config.Config;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadManager {
    public static final ThreadManager INSTANCE = new ThreadManager();
    public static final String CHUNK_SCANNER_THREAD_NAME = "Minimap-Chunk-Scanner";
    public static final String UPDATER_THREAD_NAME = "Minimap-Layer-Updater";
    public static final String DISK_IO_THREAD_NAME = "Minimap-Disk-IO";

    private ExecutorService chunkScannerExecutor;
    private ExecutorService updaterExecutor;
    private ExecutorService diskIOExecutor;

    private ThreadManager() {
    }

    public ExecutorService getChunkScannerExecutor() {
        int threads = Config.getConfig().threads;
        if (threads < 1) {
            threads = Runtime.getRuntime().availableProcessors() / 3;
        }
        if (this.chunkScannerExecutor == null) {
            this.chunkScannerExecutor = Executors.newFixedThreadPool(Math.max(1, threads), new ThreadFactoryBuilder().setNameFormat(CHUNK_SCANNER_THREAD_NAME + "-%d").build());
        }
        return this.chunkScannerExecutor;
    }

    public ExecutorService getDiskIOExecutor() {
        if (this.diskIOExecutor == null) {
            this.diskIOExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat(DISK_IO_THREAD_NAME).build());
        }
        return this.diskIOExecutor;
    }

    public ExecutorService getUpdaterExecutor() {
        if (this.updaterExecutor == null) {
            this.updaterExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat(UPDATER_THREAD_NAME).build());
        }
        return this.updaterExecutor;
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
}
