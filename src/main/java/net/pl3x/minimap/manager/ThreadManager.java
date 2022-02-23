package net.pl3x.minimap.manager;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.pl3x.minimap.config.Config;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadManager {
    public static final ThreadManager INSTANCE = new ThreadManager();
    public static final String UPDATER_THREAD_NAME = "Minimap-Updater";
    public static final String IO_THREAD_NAME = "Minimap-IO";

    private ExecutorService updaterExecutor;
    private ExecutorService ioExecutor;

    private ThreadManager() {
    }

    public ExecutorService getUpdaterExecutor() {
        int threads = Config.getConfig().threads;
        if (threads < 1) {
            threads = Runtime.getRuntime().availableProcessors() / 3;
        }
        if (this.updaterExecutor == null) {
            this.updaterExecutor = Executors.newFixedThreadPool(Math.max(1, threads), new ThreadFactoryBuilder().setNameFormat(UPDATER_THREAD_NAME + "-%d").build());
        }
        return this.updaterExecutor;
    }

    public ExecutorService getIOExecutor() {
        if (this.ioExecutor == null) {
            this.ioExecutor = Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat(IO_THREAD_NAME + "-%d").build());
        }
        return this.ioExecutor;
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
