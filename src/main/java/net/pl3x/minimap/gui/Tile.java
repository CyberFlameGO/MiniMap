package net.pl3x.minimap.gui;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.manager.FileManager;
import net.pl3x.minimap.scheduler.Scheduler;
import net.pl3x.minimap.util.Numbers;
import net.pl3x.minimap.util.image.Image;

import java.nio.file.Path;

public class Tile {
    private final Image imageBase;
    private final Image imageBiome;
    private final Image imageHeight;
    private final Image imageWater;
    private final Image imageLight;

    private final Identifier world;
    private final int regionX;
    private final int regionZ;

    private final Path directory;
    private final Path file;

    private long lastUsed;

    public Tile(Tile.Key key) {
        String path = "tile_" + key.identifier.getPath() + "_" + key.regionX + "_" + key.regionZ;

        this.imageBase = new Image(new Identifier(MiniMap.MODID, path + "_base"));
        this.imageBiome = new Image(new Identifier(MiniMap.MODID, path + "_biome"));
        this.imageHeight = new Image(new Identifier(MiniMap.MODID, path + "_height"));
        this.imageWater = new Image(new Identifier(MiniMap.MODID, path + "_water"));
        this.imageLight = new Image(new Identifier(MiniMap.MODID, path + "_light"), true);

        this.world = key.identifier;
        this.regionX = key.regionX;
        this.regionZ = key.regionZ;

        this.directory = FileManager.INSTANCE.getWorldDir(this.world);
        this.file = this.directory.resolve(this.regionX + "_" + this.regionZ + ".png");
    }

    public Image imageBase() {
        markUsed();
        return this.imageBase;
    }

    public Image imageBiome() {
        markUsed();
        return this.imageBiome;
    }

    public Image imageHeight() {
        markUsed();
        return this.imageHeight;
    }

    public Image imageWater() {
        markUsed();
        return this.imageWater;
    }

    public Image imageLight() {
        markUsed();
        return this.imageLight;
    }

    public Path getDirectory() {
        return this.directory;
    }

    public Path getFile() {
        return this.file;
    }

    public void markUsed() {
        this.lastUsed = Scheduler.currentTick();
    }

    public long getLastUsed() {
        return this.lastUsed;
    }

    public void unload() {
        this.lastUsed = 0L;
    }

    public void draw(MatrixStack matrixStack, float delta) {
        float x0 = 196 + Numbers.regionToBlock(this.regionX);
        float y0 = 196 + Numbers.regionToBlock(this.regionZ);
        float x1 = x0 + 512;
        float y1 = y0 + 512;

        this.imageBase.draw(matrixStack, x0, y0, x1, y1);
        //this.imageBiome.draw(matrixStack, x0, y0, x1, y1);
        this.imageHeight.draw(matrixStack, x0, y0, x1, y1);
        this.imageWater.draw(matrixStack, x0, y0, x1, y1);
        this.imageLight.draw(matrixStack, x0, y0, x1, y1);
    }

    public void upload() {
        markUsed();
        this.imageBase.upload(this);
        //this.imageBiome.upload(this);
        this.imageHeight.upload(this);
        this.imageWater.upload(this);
        this.imageLight.upload(this);
    }

    public boolean initialize() {
        if (this.lastUsed == 0) {
            this.imageBase.initialize();
            this.imageBiome.initialize();
            this.imageHeight.initialize();
            this.imageWater.initialize();
            this.imageLight.initialize();
            markUsed();
            return true;
        }
        return false;
    }

    public record Key(Identifier identifier, int regionX, int regionZ) {
    }
}
