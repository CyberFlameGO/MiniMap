package net.pl3x.minimap.queue;

import net.pl3x.minimap.manager.ThreadManager;

import java.util.concurrent.LinkedBlockingQueue;

public class DiskIOQueue {
    public static final DiskIOQueue INSTANCE = new DiskIOQueue();

    private final LinkedBlockingQueue<QueueAction> readQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<QueueAction> writeQueue = new LinkedBlockingQueue<>();

    private boolean reading;
    private boolean writing;

    private DiskIOQueue() {
    }

    public void read(QueueAction action) {
        this.readQueue.add(action);
        runAsyncReadInfiniteLoop();
    }

    public void write(QueueAction action) {
        this.writeQueue.add(action);
        runAsyncWriteInfiniteLoop();
    }

    private void runAsyncReadInfiniteLoop() {
        if (this.reading) {
            return;
        }
        this.reading = true;
        ThreadManager.INSTANCE.runAsync(() -> {
            while (this.reading) {
                try {
                    this.readQueue.take().run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    this.reading = false;
                    Thread.currentThread().interrupt();
                }
            }
        }, ThreadManager.INSTANCE.getReadIOExecutor());
    }

    private void runAsyncWriteInfiniteLoop() {
        if (this.writing) {
            return;
        }
        this.writing = true;
        ThreadManager.INSTANCE.runAsync(() -> {
            while (this.writing) {
                try {
                    this.writeQueue.take().run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    this.writing = false;
                    Thread.currentThread().interrupt();
                }
            }
        }, ThreadManager.INSTANCE.getWriteIOExecutor());
    }
}
