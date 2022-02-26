package net.pl3x.minimap.queue;

import net.pl3x.minimap.tile.Tile;

public interface QueueAction {
    void run();

    static void read(Tile tile) {
        DiskIOQueue.INSTANCE.add(new ReadQueue(tile));
    }

    static void write(Tile tile) {
        DiskIOQueue.INSTANCE.add(new WriteQueue(tile));
    }
}
