package net.pl3x.minimap.scheduler;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Scheduler {
    public static final Scheduler INSTANCE = new Scheduler();

    private final List<Task> tasks = new ArrayList<>();

    private Scheduler() {
    }

    public void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
    }

    private void tick() {
        Iterator<Task> iter = this.tasks.iterator();
        while (iter.hasNext()) {
            Task task = iter.next();
            if (task.tick++ < task.delay) {
                continue;
            }
            if (task.cancelled()) {
                iter.remove();
                continue;
            }
            task.run();
            if (task.repeat) {
                task.tick = 0L;
                continue;
            }
            iter.remove();
        }
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
