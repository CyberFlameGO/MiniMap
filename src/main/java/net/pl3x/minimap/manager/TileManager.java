package net.pl3x.minimap.manager;

import net.minecraft.client.world.ClientWorld;
import net.pl3x.minimap.scheduler.Scheduler;
import net.pl3x.minimap.scheduler.Task;
import net.pl3x.minimap.tile.Tile;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TileManager {
    public static final TileManager INSTANCE = new TileManager();

    public final Map<String, Tile> tiles = new ConcurrentHashMap<>();

    public Task tickTask;

    private TileManager() {
    }

    public void start() {
        this.tiles.clear();
        this.tickTask = Scheduler.INSTANCE.addTask(0, true, this::tick);
    }

    public void stop() {
        if (this.tickTask != null) {
            this.tickTask.cancel();
            this.tickTask = null;
        }
        this.tiles.forEach((key, tile) -> {
            tile.setReady(false);
            tile.save();
        });
        this.tiles.clear();
    }

    public void tick() {
        long now = Scheduler.INSTANCE.getCurrentTick();
        Iterator<Tile> iter = this.tiles.values().iterator();
        while (iter.hasNext()) {
            Tile tile = iter.next();
            if (tile.getLastUsed() + 100 < now) { // ~5 seconds
                tile.save();
                tile.setReady(false);
                iter.remove();
                continue;
            }
            if (tile.getLastUploaded() + 20 < now) { // 1 second
                tile.upload();
            }
            if (tile.getLastSaved() + 600 < now) { // ~30 seconds
                tile.save();
            }
        }
    }

    public Tile getTile(ClientWorld world, int regionX, int regionZ, boolean load) {
        String key = String.format("%s_%d_%d", world.getRegistryKey().getValue(), regionX, regionZ);
        Tile tile = this.tiles.get(key);
        if (tile == null && load) {
            tile = loadTile(world, regionX, regionZ);
            this.tiles.put(key, tile);
        }
        if (tile != null) {
            tile.use();
        }
        return tile;
    }

    private Tile loadTile(ClientWorld world, int regionX, int regionZ) {
        Tile tile = new Tile(world, regionX, regionZ);
        tile.load();
        return tile;
    }
}
