package net.pl3x.minimap.scheduler;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Scheduler {
    public static final Scheduler INSTANCE = new Scheduler();

    private final List<Task> tasks = new ArrayList<>();

    private long currentTick;

    private Scheduler() {
    }

    public long getCurrentTick() {
        return this.currentTick;
    }

    public void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
    }

    private void tick() {
        Iterator<Task> iter = this.tasks.iterator();
        while (iter.hasNext()) {
            Task task = iter.next();
            if (task.cancelled()) {
                iter.remove();
                continue;
            }
            if (task.tick++ < task.delay) {
                continue;
            }
            task.run();
            if (task.repeat) {
                task.tick = 0L;
                continue;
            }
            iter.remove();
        }
        this.currentTick++;
    }

    public Task addTask(Task task) {
        this.tasks.add(task);
        return task;
    }

    public Task addTask(int delay, boolean repeat, Runnable runnable) {
        return addTask(new Task(delay, repeat) {
            @Override
            public void run() {
                runnable.run();
            }
        });
    }
}
