package net.pl3x.minimap.manager;

import net.pl3x.minimap.gui.Tile;
import net.pl3x.minimap.scheduler.Scheduler;
import net.pl3x.minimap.scheduler.Task;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TileManager {
    public static final TileManager INSTANCE = new TileManager();

    public final Set<Tile> tiles = new HashSet<>();

    public Task tickTask;

    private TileManager() {
    }

    public void start() {
        this.tickTask = Scheduler.INSTANCE.addTask(0, true, this::tick);
    }

    public void stop() {
        if (this.tickTask != null) {
            this.tickTask.cancel();
            this.tickTask = null;
        }
    }

    public void tick() {
        // todo
    }

    public void unloadStaleTiles() {
        long now = System.currentTimeMillis();
        Iterator<Tile> iter = tiles.iterator();
        while (iter.hasNext()) {
            Tile tile = iter.next();
            if (tile.getLastUsed() + 10000L < now) {
                tile.unload();
                iter.remove();
            }
        }
    }
}
