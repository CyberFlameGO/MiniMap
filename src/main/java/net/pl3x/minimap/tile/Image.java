package net.pl3x.minimap.tile;

import net.pl3x.minimap.MiniMap;
import net.pl3x.minimap.util.Colors;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public class Image {
    private final Path path;
    private final int[] pixels = new int[Tile.SIZE * Tile.SIZE];

    public Image(Path path) {
        this.path = path;
    }

    public Path path() {
        return this.path;
    }

    public void getPixels(BufferedImage buffer) {
        for (int x = 0; x < buffer.getWidth(); x++) {
            for (int z = 0; z < buffer.getHeight(); z++) {
                buffer.setRGB(x, z, Colors.rgb2bgr(getPixel(x, z)));
            }
        }
    }

    public int getPixel(int x, int z) {
        return this.pixels[z * Tile.SIZE + x];
    }

    public void setPixels(BufferedImage buffer) {
        for (int x = 0; x < Tile.SIZE; x++) {
            for (int z = 0; z < Tile.SIZE; z++) {
                setPixel(x, z, buffer.getRGB(x, z));
            }
        }
    }

    public void setPixel(int x, int z, int color) {
        this.pixels[z * Tile.SIZE + x] = Colors.rgb2bgr(color);
    }
}
