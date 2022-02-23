package net.pl3x.minimap.tile.queue;

import net.pl3x.minimap.tile.Tile;

public interface QueueAction {
    void run();

    static void read(Tile tile) {
        TileQueue.INSTANCE.add(new ReadQueue(tile));
    }

    static void write(Tile tile) {
        TileQueue.INSTANCE.add(new WriteQueue(tile));
    }
}
