package net.pl3x.minimap.scheduler;

public abstract class Task implements Runnable {
    protected final int delay;
    protected final boolean repeat;

    protected boolean cancelled = false;
    protected long tick;

    public Task(int delay, boolean repeat) {
        this.delay = delay;
        this.repeat = repeat;
    }

    public void cancel() {
        this.cancelled = true;
        this.tick = 0L;
    }

    public boolean cancelled() {
        return this.cancelled;
    }
}
