package net.pl3x.minimap.manager;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.pl3x.minimap.config.Config;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadManager {
    public static final ThreadManager INSTANCE = new ThreadManager();

    private ExecutorService executor;

    private ThreadManager() {
    }

    public void reset() {
        if (this.executor != null) {
            this.executor.shutdown();
            this.executor = null;
        }
    }

    private ExecutorService getExecutor() {
        int threads = Config.getConfig().threads;
        if (threads < 1) {
            threads = Runtime.getRuntime().availableProcessors() / 3;
        }
        if (this.executor == null) {
            this.executor = Executors.newFixedThreadPool(Math.max(1, threads), new ThreadFactoryBuilder().setNameFormat("MinimapUpdater-%d").setThreadFactory(Executors.defaultThreadFactory()).build());
        }
        return this.executor;
    }

    public void runAsync(Runnable task) {
        runAsync(task, null);
    }

    public void runAsync(Runnable task, Runnable whenComplete) {
        CompletableFuture.runAsync(task, getExecutor())
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
