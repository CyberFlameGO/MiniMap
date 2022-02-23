package net.pl3x.minimap.tile.queue;

import net.pl3x.minimap.manager.ThreadManager;

import java.util.concurrent.LinkedBlockingQueue;

public class TileQueue {
    public static final TileQueue INSTANCE = new TileQueue();

    private final LinkedBlockingQueue<QueueAction> queue = new LinkedBlockingQueue<>();

    private boolean running;

    private TileQueue() {
    }

    public void add(QueueAction action) {
        this.queue.add(action);
        runAsyncInfiniteLoop();
    }

    private void runAsyncInfiniteLoop() {
        if (this.running) {
            return;
        }
        this.running = true;
        ThreadManager.INSTANCE.runAsync(() -> {
            while (this.running) {
                try {
                    this.queue.take().run();
                } catch (InterruptedException e) {
                    this.running = false;
                    Thread.currentThread().interrupt();
                }
            }
        });
    }
}
