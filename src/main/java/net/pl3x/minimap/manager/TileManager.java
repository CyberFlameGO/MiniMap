package net.pl3x.minimap.manager;

import net.pl3x.minimap.gui.Tile;
import net.pl3x.minimap.scheduler.Scheduler;
import net.pl3x.minimap.scheduler.Task;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TileManager {
    public static final TileManager INSTANCE = new TileManager();

    public final Map<Tile.Key, Tile> tiles = new HashMap<>();

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
        this.tiles.clear();
    }

    public void tick() {
        unloadStaleTiles();
    }

    public void unloadStaleTiles() {
        long now = Scheduler.currentTick();
        Iterator<Map.Entry<Tile.Key, Tile>> iter = tiles.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Tile.Key, Tile> entry = iter.next();
            Tile tile = entry.getValue();
            if (tile.getLastUsed() + 100 < now) { // ~5 seconds
                tile.unload();
                iter.remove();
            }
        }
    }

    public Tile getTile(Tile.Key key) {
        Tile tile = this.tiles.get(key);
        if (tile == null) {
            tile = loadTile(key);
            this.tiles.put(key, tile);
        }
        return tile;
    }

    public Tile loadTile(Tile.Key key) {
        Tile tile = new Tile(key);
        Path file = tile.getFile();
        if (Files.exists(file)) {
            try {
                BufferedImage buffer = ImageIO.read(file.toFile());
                if (buffer != null) {
                    //tile.setImage(buffer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tile;
    }
}
