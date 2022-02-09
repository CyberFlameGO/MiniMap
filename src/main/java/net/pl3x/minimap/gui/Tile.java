package net.pl3x.minimap.gui;

import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.world.World;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.manager.FileManager;

import java.nio.file.Path;

public class Tile extends NativeImageBackedTexture {
    private final World world;
    private final int regionX;
    private final int regionZ;

    private final Path directory;

    private long lastUsed;

    public Tile(World world, int x, int z) {
        super(MiniMap.TILE_SIZE, MiniMap.TILE_SIZE, true);

        this.world = world;
        this.regionX = x;
        this.regionZ = z;

        this.directory = FileManager.INSTANCE.getWorldDir(world);

        this.lastUsed = System.currentTimeMillis();
    }

    public long getLastUsed() {
        return this.lastUsed;
    }

    public void unload() {
        this.lastUsed = 0;
    }
}
